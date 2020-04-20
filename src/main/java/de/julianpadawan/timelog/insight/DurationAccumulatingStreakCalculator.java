package de.julianpadawan.timelog.insight;

import de.julianpadawan.timelog.model.Goal;
import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.view.App;

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
    protected final void preInit(LocalDateTime referenceTime) {
        this.reference = toFirstOfInterval(referenceTime);
    }

    protected abstract LocalDate toFirstOfInterval(LocalDateTime date);

    @Override
    protected final boolean accept(LogEntry entry) {
        final LocalDate date = toFirstOfInterval(entry.getEnd());
        if (isTooLongAgo(date)) return false;
        if (!accumulatedEnough(date, Duration.between(entry.getStart(), entry.getEnd()))) return true;
        if (latest == null) latest = earliest = date;
        earliest = date;
        return true;
    }

    private boolean isTooLongAgo(LocalDate date) {
        return latest == null && date.until(reference, ChronoUnit.DAYS) > interval
                || earliest != null && date.until(earliest, ChronoUnit.DAYS) > interval;
    }

    private boolean accumulatedEnough(LocalDate date, Duration duration) {
        if (date.equals(reference)) referenceDateDuration = referenceDateDuration.plus(duration);
        if (date.equals(currentDate)) currentDuration = currentDuration.plus(duration);
        else {
            currentDate = date;
            currentDuration = duration;
        }
        return currentDuration.compareTo(minDuration) >= 0;
    }

    @Override
    protected final void acceptNewInternal(LogEntry newEntry) {
        final LocalDate date = toFirstOfInterval(newEntry.getEnd());
        if (!reference.equals(date)) return;
        referenceDateDuration = referenceDateDuration.plus(Duration.between(newEntry.getStart(), newEntry.getEnd()));
        if (referenceDateDuration.compareTo(minDuration) >= 0) {
            if (latest == null) earliest = latest = date;
            latest = reference;
        }
        postInit();
    }

    protected abstract String formatStreakDays(long streakDurationDays);

    @Override
    protected final void postInit() {
        setProgress(App.formatDuration(referenceDateDuration) + "/" + App.formatDuration(minDuration));
        if (latest == null) {
            setComplete(false);
            setStreak(formatStreakDays(-1));
            return;
        }
        final long daysLeft = latest.until(reference, ChronoUnit.DAYS);
        assert daysLeft >= 0;
        setComplete(daysLeft == 0);

        final long streakDays = earliest.until(latest, ChronoUnit.DAYS);
        if (daysLeft == 0) setStreak(String.format("%s", formatStreakDays(streakDays)));
        else if (daysLeft < interval)
            setStreak(String.format("(%s) %s", formatStreakDays(streakDays), formatStreakDays(streakDays + daysLeft)));
        else setStreak(String.format("(%s) %s", formatStreakDays(streakDays), formatStreakDays(-1)));
    }
}
