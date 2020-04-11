package de.julianpadawan.timelog.insight;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

public final class StatisticalData<D> implements Consumer<StatisticalDatum<D>> {
    private final Collection<Consumer<StatisticalDatum<D>>> addListeners = new HashSet<>();
    private StatisticalDatum<D> value;

    public StatisticalData(StatisticalDatum<D> startValue) {
        value = startValue;
    }

    @Override
    public void accept(StatisticalDatum<D> datum) {
        addListeners.forEach(durationConsumer -> durationConsumer.accept(datum));
        value = value.plus(datum);
    }

    void addListener(Consumer<StatisticalDatum<D>> listener) {
        addListeners.add(listener);
    }

    public StatisticalDatum<D> get() {
        return value;
    }
}
