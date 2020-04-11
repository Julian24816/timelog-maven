package de.julianpadawan.timelog.insight;

import de.julianpadawan.timelog.model.Activity;
import de.julianpadawan.timelog.model.LogEntry;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public final class DayStreakCalculator extends StreakCalculator {
    static final String PATTERN = "[1-9]\\d*d";

    private final int interval;

    private LocalDate reference;
    private LocalDate latest = null;
    private LocalDate earliest = null;

    public DayStreakCalculator(Activity activity, String interval) {
        super(activity);
        this.interval = Integer.parseInt(interval.substring(0, interval.length() - 1));
    }

    @Override
    protected void preInit(LocalDate reference) {
        this.reference = reference;
    }

    @Override
    protected boolean accept(LogEntry entry) {
        final LocalDate date = entry.getEnd().toLocalDate();
        if (latest == null) latest = earliest = date;

        final long daysBefore = date.until(earliest, ChronoUnit.DAYS);
        if (daysBefore < 0) throw new IllegalStateException();
        if (daysBefore == 0) return true;
        if (daysBefore <= interval) {
            earliest = date;
            return true;
        }
        return false;
    }

    @Override
    protected void postInit() {
        final long daysLeft = latest.until(reference, ChronoUnit.DAYS);
        if (daysLeft < 0) throw new IllegalStateException("accept was called with entry later than reference");

        final long streakDays = earliest.until(latest, ChronoUnit.DAYS) + 1;
        if (daysLeft == 0) setStreak(String.format("%dd", streakDays));
        else if (daysLeft < interval) setStreak(String.format("(%d) %dd", streakDays, streakDays + daysLeft));
        else setStreak(String.format("(%d) 0d", streakDays));
    }
}
