package de.julianpadawan.timelog.model;

import de.julianpadawan.common.customFX.CustomBindings;
import de.julianpadawan.common.db.*;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableObjectValue;
import javafx.beans.value.ObservableStringValue;

import java.sql.ResultSet;
import java.util.*;

public final class Activity extends ModelObject<Activity> {
    public static final String DEFAULT_COLOR = "#e6e6e6";
    public static final ActivityFactory FACTORY = new ActivityFactory();

    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final StringProperty color = new SimpleStringProperty(this, "color");
    private final DoubleProperty pointsPerMinute = new SimpleDoubleProperty(this, "pointsPerMinute");

    private final IntegerProperty parentId = new SimpleIntegerProperty(this, "parent");
    private final ObservableObjectValue<Activity> parent = new ObjectBinding<>() {
        {
            bind(parentId);
        }

        @Override
        protected Activity computeValue() {
            return FACTORY.getForId(parentId.get());
        }
    };
    private final ReadOnlyIntegerWrapper depth = new ReadOnlyIntegerWrapper(this, "depth", 0);
    private final ObservableStringValue displayName = new StringBinding() {
        {
            bind(name, parentId, depth);
        }

        @Override
        protected String computeValue() {
            if (getId() == 0) return "(" + name.get() + ")";
            if (parentId.get() == 0) return name.get();
            return "  ".repeat(getDepth() - 2) + "- " + name.get();
        }
    };

    private Activity(int id, int parentId, String name, String color, double pointsPerMinute) {
        super(id);
        this.parentId.setValue(id == parentId ? 0 : parentId); // security measure, should never happen
        this.name.setValue(Objects.requireNonNull(name));
        this.color.setValue(Objects.requireNonNull(color));
        this.pointsPerMinute.setValue(pointsPerMinute);
        if (id != 0) depth.bind(CustomBindings.selectInt(this.parent, Activity::depthProperty).add(1));
    }

    public ReadOnlyIntegerProperty depthProperty() {
        return depth.getReadOnlyProperty();
    }

    public static Activity getRoot() {
        return Activity.FACTORY.getForId(0);
    }

    public Activity getParent() {
        return parent.get();
    }

    public void setParent(Activity parent) {
        if (getId() == 0) return;
        if (getId() == parent.getId()) throw new IllegalArgumentException("activity can't be parent of itself");
        parentId.setValue(Objects.requireNonNull(parent).getId());
    }

    public ObservableObjectValue<Activity> parentProperty() {
        return parent;
    }

    public int getDepth() {
        return depth.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.setValue(Objects.requireNonNull(value));
    }

    public StringProperty colorProperty() {
        return color;
    }

    public String getColor() {
        return color.get();
    }

    public void setColor(String value) {
        color.setValue(Objects.requireNonNull(value));
    }

    public DoubleProperty pointsPerMinuteProperty() {
        return pointsPerMinute;
    }

    public double getPointsPerMinute() {
        return pointsPerMinute.get();
    }

    public void setPointsPerMinute(double value) {
        pointsPerMinute.setValue(value);
    }

    @Override
    public ObservableStringValue displayNameProperty() {
        return displayName;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + getId() +
                ", parentId=" + parentId.get() +
                ", name=" + name.get() +
                ", color=" + color.get() +
                ", pointsPerMinute=" + pointsPerMinute.get() +
                ", depth=" + depth.get() +
                '}';
    }

    @Override
    public int compareTo(Activity o) {
        if (this.equals(o)) return 0;
        if (getDepth() < o.getDepth()) return -o.compareTo(this);
        if (getDepth() == o.getDepth()) {
            final int parents = getParent().compareTo(o.getParent());
            if (parents == 0) {
                final int names = getName().compareTo(o.getName());
                if (names == 0) return Integer.compare(getId(), o.getId());
                return names;
            }
            return parents;
        }
        // else: getDepth() > o.getDepth()
        final int parent = getParent().compareTo(o);
        if (parent == 0) return 1;
        return parent;
    }

    public boolean instanceOf(Activity activity) {
        if (activity.equals(this)) return true;
        if (getDepth() < activity.getDepth()) return false;
        return getParent().instanceOf(activity);
    }

    public static final class ActivityFactory extends ModelFactory<Activity> {
        private final Map<Integer, Activity> activityMap = new HashMap<>();
        private boolean loaded;

        private ActivityFactory() {
            super(view -> new Activity(
                            view.getInt("id"),
                            view.getInt("parent"),
                            view.getString("name"),
                            view.getString("color"),
                            view.getDouble("pointsPerMinute")
                    ),
                    new ModelTableDefinition<Activity>("activity")
                            .withColumn("parent", ColumnType.getForeignKeyColumn(Activity.class), Activity::getParent)
                            .withColumn("name", ColumnType.STRING, Activity::getName)
                            .withColumn("color", ColumnType.STRING, Activity::getColor)
                            .withColumn("pointsPerMinute", ColumnType.DOUBLE, Activity::getPointsPerMinute)
            );
        }

        @Override
        public Collection<Activity> getAll() {
            if (!loaded) {
                loaded = true;
                super.getAll().forEach(this::putActivity);
            }
            return activityMap.values();
        }

        @Override
        public Activity getForId(int id) {
            if (!activityMap.containsKey(id)) {
                final Activity activity = super.getForId(id);
                if (activity != null) putActivity(activity);
            }
            return activityMap.get(id);
        }

        @Override
        public Activity createNew(Object... params) {
            final Activity activity = super.createNew(params);
            putActivity(activity);
            return activity;
        }

        @Override
        public boolean update(Activity obj) {
            activityMap.put(obj.getId(), obj);
            return super.update(obj);
        }

        public Collection<Activity> getAllChildren(Activity parent) {
            return Database.execute("SELECT id FROM activity WHERE id != 0 AND parent = ? ORDER BY name", statement -> {
                statement.setInt(1, parent.getId());
                try (final ResultSet resultSet = statement.executeQuery()) {
                    List<Activity> result = new LinkedList<>();
                    while (resultSet.next()) result.add(getForId(resultSet.getInt(1)));
                    return result;
                }
            }, Collections.emptyList());
        }

        private void putActivity(Activity activity) {
            activityMap.putIfAbsent(activity.getId(), activity);
        }

        public void clearCache() {
            activityMap.clear();
            loaded = false;
        }
    }
}
