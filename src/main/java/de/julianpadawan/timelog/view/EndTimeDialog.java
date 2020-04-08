package de.julianpadawan.timelog.view;

import de.julianpadawan.common.customFX.GridPane2C;
import de.julianpadawan.common.customFX.TimeTextField;
import javafx.beans.Observable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EndTimeDialog extends Dialog<LocalDateTime> {
    private final DatePicker date;
    private final TimeTextField time;
    private final Button okButton;

    public EndTimeDialog() {
        super();
        setTitle("End Time");
        setHeaderText("Choose End Time");

        GridPane2C gridPane2C = new GridPane2C(10);
        gridPane2C.setPadding(new Insets(10));
        date = gridPane2C.addRow("Date", new DatePicker(LocalDate.now()));
        date.valueProperty().addListener(this::invalidated);
        time = gridPane2C.addRow("Time", new TimeTextField(LocalTime.now()));
        time.valueProperty().addListener(this::invalidated);
        getDialogPane().setContent(gridPane2C);

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        setResultConverter(this::getValue);
    }

    private void invalidated(Observable observable) {
        okButton.setDisable(date.getValue() == null || time.getValue() == null);
    }

    private LocalDateTime getValue(ButtonType buttonType) {
        return buttonType.equals(ButtonType.OK) ? LocalDateTime.of(date.getValue(), time.getValue()) : null;
    }
}
