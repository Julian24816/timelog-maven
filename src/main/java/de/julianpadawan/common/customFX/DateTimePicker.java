package de.julianpadawan.common.customFX;

import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.time.LocalDateTime;

public class DateTimePicker extends HBox {
    private final DatePicker date = new DatePicker();
    private final TimeTextField time = new TimeTextField();

    private final ReadOnlyObjectWrapper<LocalDateTime> value = new ReadOnlyObjectWrapper<>(this, "value");

    public DateTimePicker(LocalDateTime value) {
        this();
        setValue(value);
    }

    public DateTimePicker() {
        super(10);
        getChildren().addAll(date, time);
        Util.applyAfterFocusLost(date);
        HBox.setHgrow(time, Priority.ALWAYS);
        date.valueProperty().addListener(this::invalidated);
        time.valueProperty().addListener(this::invalidated);
    }

    private void invalidated(Observable observable) {
        if (date.getValue() == null || time.getValue() == null) value.set(null);
        else value.set(LocalDateTime.of(date.getValue(), time.getValue()));
    }

    public ReadOnlyObjectProperty<LocalDateTime> valueProperty() {
        return value.getReadOnlyProperty();
    }

    public LocalDateTime getValue() {
        return value.get();
    }

    public void setValue(LocalDateTime value) {
        date.setValue(value == null ? null : value.toLocalDate());
        time.setValue(value == null ? null : value.toLocalTime());
    }
}
