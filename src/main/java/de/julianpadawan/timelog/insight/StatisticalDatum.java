package de.julianpadawan.timelog.insight;

public interface StatisticalDatum<D> extends Comparable<StatisticalDatum<D>> {
    StatisticalDatum<D> plus(StatisticalDatum<D> value);

    D get();

    @Override
    String toString();

    boolean isZero();

    StatisticalDatum<D> dividedBy(int averagedOver);
}
