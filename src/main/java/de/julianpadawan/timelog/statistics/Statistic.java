package de.julianpadawan.timelog.statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class Statistic<T, D> implements Comparable<Statistic<T, D>> {
    protected final T key;
    private final String name;
    private final StatisticalData<D> aggregateData, data;
    private final Map<T, Statistic<T, D>> children = new HashMap<>();

    protected Statistic(T key, String name, Supplier<StatisticalDatum<D>> zero) {
        this.key = key;
        this.name = name;
        this.aggregateData = new StatisticalData<>(zero.get());
        this.data = new StatisticalData<>(zero.get());
        this.data.addListener(aggregateData);
    }

    protected void add(T key, StatisticalDatum<D> value) {
        if (this.key == null || !this.key.equals(key)) getSubStatistic(key).add(key, value);
        else data.accept(value);
    }

    protected Statistic<T, D> getSubStatistic(T key) {
        if (this.key != null && this.key.equals(key)) throw new IllegalArgumentException();
        if (!children.containsKey(key)) createSubStatistic(key);
        return children.get(key);
    }

    private void createSubStatistic(T key) {
        final Statistic<T, D> statistic = newStatistic(key);
        children.put(key, statistic);
        statistic.aggregateData.addListener(aggregateData);
    }

    protected abstract Statistic<T, D> newStatistic(T key);

    public StatisticalDatum<D> getAggregateData() {
        return aggregateData.get();
    }

    public StatisticalDatum<D> getData() {
        return data.get();
    }

    public Collection<Statistic<T, D>> getSubStatistics() {
        return children.values();
    }

    @Override
    public int compareTo(Statistic<T, D> o) {
        return getName().compareTo(o.getName());
    }

    public String getName() {
        return name;
    }
}
