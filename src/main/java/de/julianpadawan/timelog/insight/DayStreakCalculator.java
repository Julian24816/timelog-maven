package de.julianpadawan.timelog.insight;

import de.julianpadawan.timelog.model.Goal;

import java.time.LocalDate;

public final class DayStreakCalculator extends DurationAccumulatingStreakCalculator {
    static final String PATTERN = "[1-9]\\d*d";

    public DayStreakCalculator(Goal goal) {
        super(goal);
    }

    @Override
    protected int getDayInterval(Goal goal) {
        return Integer.parseInt(goal.getInterval().substring(0, goal.getInterval().length() - 1));
    }

    @Override
    protected LocalDate toFirstOfInterval(LocalDate date) {
        return date;
    }

    @Override
    protected String formatStreakDays(long streakDurationDays) {
        return streakDurationDays + 1 + "d";
    }
}
