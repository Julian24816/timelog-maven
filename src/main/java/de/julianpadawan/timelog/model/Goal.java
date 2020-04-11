package de.julianpadawan.timelog.model;

import de.julianpadawan.common.db.ColumnType;
import de.julianpadawan.common.db.ModelFactory;
import de.julianpadawan.common.db.ModelObject;
import de.julianpadawan.common.db.ModelTableDefinition;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;

public final class Goal extends ModelObject<Goal> {
    public static final GoalFactory FACTORY = new GoalFactory();

    private final ObjectProperty<Activity> activity = new SimpleObjectProperty<>(this, "activity");
    private final StringProperty interval = new SimpleStringProperty(this, "interval");
    private final ObservableStringValue displayName = new StringBinding() {
        {
            bind(activity, interval);
        }

        @Override
        protected String computeValue() {
            return String.format("Goal(%s,%s)", activity.get().getName(), interval.get());
        }
    };

    private Goal(int id, Activity activity, String interval) {
        super(id);
        this.activity.setValue(activity);
        this.interval.setValue(interval);
    }

    public ObjectProperty<Activity> activityProperty() {
        return activity;
    }

    public Activity getActivity() {
        return activity.get();
    }

    public void setActivity(Activity value) {
        activity.setValue(value);
    }

    public StringProperty intervalProperty() {
        return interval;
    }

    public String getInterval() {
        return interval.get();
    }

    public void setInterval(String value) {
        interval.setValue(value);
    }

    @Override
    public ObservableStringValue displayNameProperty() {
        return displayName;
    }

    @Override
    public String toString() {
        return null;
    }


    public static final class GoalFactory extends ModelFactory<Goal> {
        private GoalFactory() {
            super(view -> new Goal(
                            view.getInt("id"),
                            Activity.FACTORY.getForId(view.getInt("activity")),
                            view.getString("interval")
                    ),
                    new ModelTableDefinition<Goal>("goal")
                            .withColumn("activity", ColumnType.getForeignKeyColumn(Activity.class), Goal::getActivity)
                            .withColumn("interval", ColumnType.STRING, Goal::getInterval)
            );
        }
    }
}
