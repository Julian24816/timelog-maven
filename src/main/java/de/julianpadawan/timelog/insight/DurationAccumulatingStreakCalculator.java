package de.julianpadawan.timelog.insight;

import de.julianpadawan.timelog.model.Goal;
import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.preferences.Preferences;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public abstract class DurationAccumulatingStreakCalculator extends StreakCalculator {
    private final int interval;
    private final Duration minDuration;

    private LocalDate reference;
    private LocalDate latest = null;
    private LocalDate earliest = null;

    private LocalDate currentDate = null;
    private Duration currentDuration = Duration.ZERO;
    private Duration referenceDateDuration = Duration.ZERO;

    public DurationAccumulatingStreakCalculator(Goal goal) {
        super(goal.getActivity(), goal.getPerson());
        this.interval = getDayInterval(goal);
        this.minDuration = goal.getMinDuration();
    }

    protected abstract int getDayInterval(Goal goal);

    @Override
    protected final void preInit(LocalDate reference) {
        this.reference = toFirstOfInterval(reference);
    }

    protected abstract LocalDate toFirstOfInterval(LocalDate date);

    @Override
    protected final boolean accept(LogEntry entry) {
        final LocalDate date = toFirstOfInterval(getDate(entry.getEnd()));
        if (!accumulate(date, Duration.between(entry.getStart(), entry.getEnd()))) return true;
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

    private LocalDate getDate(LocalDateTime time) {
        if (!time.toLocalTime().isBefore(Preferences.getTime("StartOfDay"))) return time.toLocalDate();
        return time.toLocalDate().minusDays(1);
    }

    private boolean accumulate(LocalDate date, Duration duration) {
        if (date.equals(reference)) referenceDateDuration = referenceDateDuration.plus(duration);
        if (date.equals(currentDate)) currentDuration = currentDuration.plus(duration);
        else {
            currentDate = date;
            currentDuration = duration;
        }
        return currentDuration.compareTo(minDuration) >= 0;
    }

    @Override
    protected final void postInit() {
        if (latest == null) {
            setStreak(formatStreakDays(-1));
            return;
        }
        final long daysLeft = latest.until(reference, ChronoUnit.DAYS);
        if (daysLeft < 0) throw new IllegalStateException("accept was called with entry later than reference");

        final long streakDays = earliest.until(latest, ChronoUnit.DAYS);
        if (daysLeft == 0) setStreak(String.format("%s", formatStreakDays(streakDays)));
        else if (daysLeft < interval)
            setStreak(String.format("(%s) %s", formatStreakDays(streakDays), formatStreakDays(streakDays + daysLeft)));
        else setStreak(String.format("(%s) %s", formatStreakDays(streakDays), formatStreakDays(-1)));
    }

    protected abstract String formatStreakDays(long streakDurationDays);
}
