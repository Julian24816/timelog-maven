package de.julianpadawan.timelog.model;

import de.julianpadawan.common.db.*;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Activity extends ModelObject<Activity> {
    public static final String DEFAULT_COLOR = "#DDD";
    public static final ActivityFactory FACTORY = new ActivityFactory();
    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final StringProperty color = new SimpleStringProperty(this, "color");
    private final IntegerProperty parentId = new SimpleIntegerProperty(this, "parent");
    private final ObservableStringValue displayName = new StringBinding() {
        {
            bind(name, parentId);
        }

        @Override
        protected String computeValue() {
            if (getId() == 0) return "(" + name.get() + ")";
            if (parentId.get() == 0) return name.get();
            return "  ".repeat(getDepth() - 2) + "- " + name.get();
        }
    };

    private Activity(int id, int parentId, String name, String color) {
        super(id);
        this.parentId.setValue(id == parentId ? 0 : parentId);
        this.name.setValue(Objects.requireNonNull(name));
        this.color.setValue(Objects.requireNonNull(color));
    }

    public static Activity getRoot() {
        return Activity.FACTORY.getForId(0);
    }

    public int getDepth() {
        if (getId() == 0) return 0;
        if (parentId.get() == 0) return 1;
        else return getParent().getDepth() + 1;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.setValue(value);
    }

    public StringProperty colorProperty() {
        return color;
    }

    public String getColor() {
        return color.get();
    }

    public void setColor(String value) {
        color.setValue(value);
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

    public Activity getParent() {
        return FACTORY.getForId(parentId.get());
    }

    public void setParent(Activity parent) {
        if (getId() == 0) return;
        if (getId() == parent.getId()) throw new IllegalArgumentException("activity can't be parent of itself");
        parentId.setValue(Objects.requireNonNull(parent).getId());
    }

    public boolean instanceOf(Activity activity) {
        if (activity.equals(this)) return true;
        if (getDepth() < activity.getDepth()) return false;
        return getParent().instanceOf(activity);
    }

    public static final class ActivityFactory extends ModelFactory<Activity> {
        private final Map<Integer, Activity> activityMap = new HashMap<>();

        private ActivityFactory() {
            super(view -> new Activity(
                            view.getInt("id"),
                            view.getInt("parent"),
                            view.getString("name"),
                            view.getString("color")
                    ),
                    new ModelTableDefinition<Activity>("activity")
                            .withColumn("parent", ColumnType.getForeignKeyColumn(Activity.class), Activity::getParent)
                            .withColumn("name", ColumnType.STRING, Activity::getName)
                            .withColumn("color", ColumnType.STRING, Activity::getColor)
            );

            final boolean rootExists = selectWhere(ResultSet::next, "id=0");
            if (!rootExists)
                Database.execute("INSERT INTO activity VALUES (0, 0, 'Activity', '" + DEFAULT_COLOR + "');",
                        PreparedStatement::execute, null);
        }

        @Override
        public Activity getForId(int id) {
            ensureLoaded();
            if (!activityMap.containsKey(id)) { //just created
                final Activity activity = super.getForId(id);
                if (activity != null) putActivity(activity);
            }
            return activityMap.get(id);
        }

        private void ensureLoaded() {
            if (activityMap.isEmpty()) super.getAll().forEach(this::putActivity);
        }

        private void putActivity(Activity activity) {
            activityMap.put(activity.getId(), activity);
        }

        @Override
        public Collection<Activity> getAll() {
            ensureLoaded();
            return activityMap.values();
        }

        @Override
        public Activity createNew(Object... params) {
            ensureLoaded();
            final Activity activity = super.createNew(params);
            putActivity(activity);
            return activity;
        }

        @Override
        public boolean update(Activity obj) {
            ensureLoaded();
            activityMap.replace(obj.getId(), obj);
            return super.update(obj);
        }

    }
}
