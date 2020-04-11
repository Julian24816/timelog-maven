package de.julianpadawan.timelog.insight;

import de.julianpadawan.common.db.Database;
import de.julianpadawan.common.db.ResultView;
import de.julianpadawan.timelog.model.*;
import de.julianpadawan.timelog.preferences.Preferences;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDate;

public abstract class StreakCalculator {
    private final Activity activity;
    private final Person person;
    private final StringProperty streak = new SimpleStringProperty(this, "streak");

    protected StreakCalculator(Activity activity, Person person) {
        this.activity = activity;
        this.person = person;
    }

    public static StreakCalculator of(Goal goal) {
        if (goal.getInterval().matches(DayStreakCalculator.PATTERN))
            return new DayStreakCalculator(goal);
        if (goal.getInterval().matches(WeekStreakCalculator.PATTERN))
            return new WeekStreakCalculator(goal);
        throw new IllegalArgumentException("unknown interval");
    }

    public static boolean validInterval(String interval) {
        return interval.matches(DayStreakCalculator.PATTERN) || interval.matches(WeekStreakCalculator.PATTERN);
    }

    public static String getPrompt() {
        return DayStreakCalculator.PATTERN + "|" + WeekStreakCalculator.PATTERN;
    }

    public void init(LocalDate reference) {
        preInit(reference);
        Database.execute(LogEntry.LogEntryFactory.TABLE_DEFINITION.getBaseSelectSQL() + " WHERE end < ? AND NOT end IS NULL ORDER BY end DESC", statement -> {
            statement.setTimestamp(1, Timestamp.valueOf(reference.plusDays(1).atTime(Preferences.getTime("StartOfDay"))));
            try (final ResultSet resultSet = statement.executeQuery()) {
                final ResultView view = new ResultView(resultSet);
                while (resultSet.next()) {
                    LogEntry entry = LogEntry.LogEntryFactory.getFromResultView(view);
                    if (instanceOfActivity(entry.getActivity()) &&
                            (person == null || QualityTime.FACTORY.exists(entry, person)))
                        if (!accept(entry)) break;
                }
            }
            return null;
        }, null);
        postInit();
    }

    private boolean instanceOfActivity(Activity entryActivity) {
        if (entryActivity.getDepth() < activity.getDepth()) return false;
        if (entryActivity.equals(activity)) return true;
        return instanceOfActivity(entryActivity.getParent());
    }

    protected abstract void preInit(LocalDate reference);

    protected abstract boolean accept(LogEntry entry);

    protected abstract void postInit();

    public StringProperty streakProperty() {
        return streak;
    }

    public final String getStreak() {
        return streak.get();
    }

    protected void setStreak(String value) {
        streak.setValue(value);
    }
}
