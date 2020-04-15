package de.julianpadawan.timelog.insight;

import de.julianpadawan.timelog.model.Goal;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public final class MonthStreakCalculator extends DurationAccumulatingStreakCalculator {
    static final String PATTERN = "[1-9]\\d*m";

    public MonthStreakCalculator(Goal goal) {
        super(goal);
    }

    @Override
    protected int getDayInterval(Goal goal) {
        return Integer.parseInt(goal.getInterval().substring(0, goal.getInterval().length() - 1)) * 31;
    }

    @Override
    protected LocalDate toFirstOfInterval(LocalDate date) {
        return date.with(TemporalAdjusters.firstDayOfMonth());
    }

    @Override
    protected String formatStreakDays(long streakDurationDays) {
        if (streakDurationDays == -1) return "0m";
        final long durationMonths = Math.round(streakDurationDays / 365.25 * 12);
        return durationMonths + 1 + "m";
    }
}
