package de.julianpadawan.timelog.statistics;


import de.julianpadawan.timelog.model.LogEntry;

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
        if (duration.equals(Duration.ZERO)) return "";
        final long minutes = Math.floorDiv(duration.getSeconds(), 60) % 60;
        final long hours = Math.floorDiv(duration.getSeconds(), 3600);
        return String.format("%dh %02dm", hours, minutes);
    }

    @Override
    public StatisticalDatum<Duration> plus(StatisticalDatum<Duration> value) {
        return new DurationDatum(duration.plus(value.get()));
    }

    @Override
    public Duration get() {
        return duration;
    }
}
