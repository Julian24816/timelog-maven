package de.julianpadawan.timelog.insight;

import de.julianpadawan.timelog.model.Goal;
import de.julianpadawan.timelog.model.LogEntry;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public final class WeekStreakCalculator extends DurationAccumulatingStreakCalculator {
    static final String PATTERN = "[1-9]\\d*w";

    public WeekStreakCalculator(Goal goal) {
        super(goal);
    }

    @Override
    protected int getDayInterval(Goal goal) {
        return Integer.parseInt(goal.getInterval().substring(0, goal.getInterval().length() - 1)) * 7;
    }

    @Override
    protected LocalDate toFirstOfInterval(LocalDateTime dateTime) {
        return LogEntry.getDate(dateTime).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    @Override
    protected String formatStreakDays(long streakDurationDays) {
        if (streakDurationDays == -1) return "0w";
        return streakDurationDays / 7 + 1 + "w";
    }
}
