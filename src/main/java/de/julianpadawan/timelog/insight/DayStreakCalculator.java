package de.julianpadawan.timelog.insight;

import de.julianpadawan.timelog.model.Goal;
import de.julianpadawan.timelog.model.LogEntry;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    protected LocalDate toFirstOfInterval(LocalDateTime dateTime) {
        return LogEntry.getDate(dateTime);
    }

    @Override
    protected String formatStreakDays(long streakDurationDays) {
        return streakDurationDays + 1 + "d";
    }
}
