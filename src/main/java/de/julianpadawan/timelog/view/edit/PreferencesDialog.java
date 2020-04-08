package de.julianpadawan.timelog.view.edit;

import de.julianpadawan.common.customFX.CustomBindings;
import de.julianpadawan.common.customFX.GridPane2C;
import de.julianpadawan.common.customFX.TimeTextField;
import de.julianpadawan.timelog.preferences.PreferenceMap;
import javafx.beans.binding.BooleanExpression;
import javafx.scene.control.*;

public class PreferencesDialog extends Dialog<ButtonType> {

    private final BooleanExpression okEnabled;

    public PreferencesDialog() {
        super();
        setTitle("Preferences");
        setHeaderText("Edit Preferences");

        PreferenceMap preferenceMap = new PreferenceMap();
        GridPane2C gridPane2C = new GridPane2C(10);
        getDialogPane().setContent(gridPane2C);

        final TextField scaling = gridPane2C.addRow("Minute To Pixel Scale", new TextField());
        preferenceMap.mapTo(scaling, "MinuteToPixelScale");

        gridPane2C.addSeparator();

        final TextField marks = gridPane2C.addRow("Minute Mark Every", new TextField());
        preferenceMap.mapTo(marks, "MinuteMarkEvery");

        final TextField marksWidth = gridPane2C.addRow("Minute Mark Width", new TextField());
        preferenceMap.mapTo(marksWidth, "MinuteMarkWidth");
        marksWidth.setPromptText("set to 0 to disable");

        final ColorPicker markColor = gridPane2C.addRow("Minute Mark Color", new ColorPicker());
        preferenceMap.mapTo(markColor, "MinuteMarkColor");

        gridPane2C.addSeparator();

        final TimeTextField startOfDay = gridPane2C.addRow("Start of Day", new TimeTextField(null));
        preferenceMap.mapTo(startOfDay, "StartOfDay");

        final TextField sleepID = gridPane2C.addRow("ID of Sleep Activity", new TextField());
        preferenceMap.mapTo(sleepID, "SleepID");
        sleepID.setPromptText("set to -1 to disable");

        final TextField sleepLineHeight = gridPane2C.addRow("Sleep Line Height", new TextField());
        preferenceMap.mapTo(sleepLineHeight, "SleepLineHeight");

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        Button okButton = (Button) getDialogPane().lookupButton(ButtonType.OK);
        okButton.setOnAction(event -> preferenceMap.dumpPreferences());

        okEnabled = CustomBindings.matches(scaling, "\\d+(\\.\\d+)?")
                .and(CustomBindings.matches(marks, "\\d+"))
                .and(CustomBindings.matches(marksWidth, "\\d+"))
                .and(startOfDay.valueProperty().isNotNull())
                .and(CustomBindings.matches(sleepID, "-1|\\d+"))
                .and(CustomBindings.matches(sleepLineHeight, "\\d+"));
        okEnabled.addListener(observable -> okButton.setDisable(!okEnabled.getValue()));
    }

}
