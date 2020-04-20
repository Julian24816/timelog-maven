package de.julianpadawan.timelog.insight;

import de.julianpadawan.common.db.Database;
import de.julianpadawan.common.db.ResultView;
import de.julianpadawan.timelog.model.*;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public abstract class StreakCalculator {
    private final Activity activity;
    private final Person person;

    private final StringProperty label = new SimpleStringProperty(this, "label");
    private final StringProperty streak = new SimpleStringProperty(this, "streak");
    private final BooleanProperty complete = new SimpleBooleanProperty(this, "complete");
    private final StringProperty progress = new SimpleStringProperty(this, "progress");


    protected StreakCalculator(Activity activity, Person person) {
        this.activity = activity;
        this.person = person;
        this.label.bind(person != null ? person.nameProperty() : activity.nameProperty());
    }

    public static StreakCalculator of(Goal goal) {
        if (goal.getInterval().matches(DayStreakCalculator.PATTERN))
            return new DayStreakCalculator(goal);
        if (goal.getInterval().matches(WeekStreakCalculator.PATTERN))
            return new WeekStreakCalculator(goal);
        if (goal.getInterval().matches(MonthStreakCalculator.PATTERN))
            return new MonthStreakCalculator(goal);
        throw new IllegalArgumentException("unknown interval");
    }

    public static boolean validInterval(String interval) {
        return interval.matches(DayStreakCalculator.PATTERN)
                || interval.matches(WeekStreakCalculator.PATTERN)
                || interval.matches(MonthStreakCalculator.PATTERN);
    }

    public static String getPrompt() {
        return DayStreakCalculator.PATTERN
                + "|" + WeekStreakCalculator.PATTERN
                + "|" + MonthStreakCalculator.PATTERN;
    }

    public final void init(LocalDateTime referenceTime) {
        preInit(referenceTime);
        Database.execute(LogEntry.LogEntryFactory.TABLE_DEFINITION.getBaseSelectSQL() + " WHERE end < ? AND NOT end IS NULL ORDER BY end DESC", statement -> {
            statement.setTimestamp(1, Timestamp.valueOf(referenceTime));
            try (final ResultSet resultSet = statement.executeQuery()) {
                final ResultView view = new ResultView(resultSet);
                while (resultSet.next()) {
                    LogEntry entry = LogEntry.LogEntryFactory.getFromResultView(view);
                    if (isRelevant(entry))
                        if (!accept(entry)) break;
                }
            }
            return null;
        }, null);
        postInit();
    }

    protected abstract void preInit(LocalDateTime referenceTime);

    protected abstract boolean accept(LogEntry entry);

    protected abstract void postInit();

    private boolean isRelevant(LogEntry newEntry) {
        return newEntry.getActivity().instanceOf(activity) &&
                (person == null || QualityTime.FACTORY.exists(newEntry, person));
    }

    public final void acceptNew(LogEntry newEntry) {
        if (isRelevant(newEntry))
            acceptNewInternal(newEntry);
    }

    protected abstract void acceptNewInternal(LogEntry newEntry);

    public final StringProperty labelProperty() {
        return label;
    }

    public final String getLabel() {
        return label.get();
    }

    protected final void setLabel(String value) {
        label.unbind();
        label.setValue(value);
    }

    public final StringProperty streakProperty() {
        return streak;
    }

    public final String getStreak() {
        return streak.get();
    }

    protected final void setStreak(String value) {
        streak.setValue(value);
    }

    public final BooleanProperty completeProperty() {
        return complete;
    }

    public final boolean isComplete() {
        return complete.get();
    }

    protected final void setComplete(boolean value) {
        complete.setValue(value);
    }

    public final StringProperty progressProperty() {
        return progress;
    }

    public final String getProgress() {
        return progress.get();
    }

    protected final void setProgress(String value) {
        progress.setValue(value);
    }
}
