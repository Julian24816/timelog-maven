package de.julianpadawan.timelog.insight;


import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.view.App;

import java.time.Duration;

public final class DurationDatum implements StatisticalDatum<Duration> {
    private final Duration duration;

    public DurationDatum() {
        this(Duration.ZERO);
    }

    public DurationDatum(Duration duration) {
        this.duration = duration;
    }

    public static DurationDatum of(LogEntry entry) {
        if (entry.getEnd() == null) throw new IllegalArgumentException("unfinished entry");
        return new DurationDatum(Duration.between(entry.getStart(), entry.getEnd()));
    }

    @Override
    public String toString() {
        return App.formatDuration(duration, false);
    }

    @Override
    public StatisticalDatum<Duration> plus(StatisticalDatum<Duration> value) {
        return new DurationDatum(duration.plus(value.get()));
    }

    @Override
    public Duration get() {
        return duration;
    }

    @Override
    public boolean isZero() {
        return duration.equals(Duration.ZERO);
    }

    @Override
    public int compareTo(StatisticalDatum<Duration> o) {
        return duration.compareTo(o.get());
    }
}
