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

import java.time.Duration;
import java.util.Objects;

public final class Goal extends ModelObject<Goal> {
    public static final GoalFactory FACTORY = new GoalFactory();

    private final ObjectProperty<Activity> activity = new SimpleObjectProperty<>(this, "activity");
    private final StringProperty interval = new SimpleStringProperty(this, "interval");
    private final ObjectProperty<Duration> minDuration = new SimpleObjectProperty<>(this, "minDuration");
    private final ObjectProperty<Person> person = new SimpleObjectProperty<>(this, "person");

    private final ObservableStringValue displayName = new StringBinding() {
        {
            bind(activity, interval, minDuration, person);
        }

        @Override
        protected String computeValue() {
            String result = String.format("Goal(%s,%s", activity.get().getName(), interval.get());
            if (minDuration.get() != null && !minDuration.get().isZero())
                result += String.format(",>=%dm", minDuration.get().toMinutes());
            if (person.get() != null) result += "," + person.get().getName();
            result += ")";
            return result;
        }
    };

    private Goal(int id, Activity activity, String interval, Duration minDuration, Person person) {
        super(id);
        this.activity.setValue(Objects.requireNonNull(activity));
        this.interval.setValue(Objects.requireNonNull(interval));
        this.minDuration.setValue(Objects.requireNonNull(minDuration));
        this.person.setValue(person);
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

    public ObjectProperty<Duration> minDurationProperty() {
        return minDuration;
    }

    public Duration getMinDuration() {
        return minDuration.get();
    }

    public void setMinDuration(Duration value) {
        minDuration.setValue(value);
    }

    public ObjectProperty<Person> personProperty() {
        return person;
    }

    public Person getPerson() {
        return person.get();
    }

    public void setPerson(Person value) {
        person.setValue(value);
    }

    @Override
    public ObservableStringValue displayNameProperty() {
        return displayName;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "activity=" + activity.get() +
                ", interval=" + interval.get() +
                ", minDuration=" + minDuration.get() +
                ", person=" + person.get() +
                '}';
    }

    public static final class GoalFactory extends ModelFactory<Goal> {
        private GoalFactory() {
            super(view -> new Goal(
                            view.getInt("id"),
                            Activity.FACTORY.getForId(view.getInt("activity")),
                            view.getString("interval"),
                            view.getDuration("minDuration"),
                            view.getOptionalInt("person").map(Person.FACTORY::getForId).orElse(null)
                    ),
                    new ModelTableDefinition<Goal>("goal")
                            .withColumn("activity", ColumnType.getForeignKeyColumn(Activity.class), Goal::getActivity)
                            .withColumn("interval", ColumnType.STRING, Goal::getInterval)
                            .withColumn("minDuration", ColumnType.DURATION, Goal::getMinDuration)
                            .withColumn("person", ColumnType.getForeignKeyColumn(Person.class), Goal::getPerson)
            );
        }
    }
}
