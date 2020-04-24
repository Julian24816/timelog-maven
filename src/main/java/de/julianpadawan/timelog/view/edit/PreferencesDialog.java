package de.julianpadawan.timelog.view.edit;

import de.julianpadawan.common.customFX.CustomBindings;
import de.julianpadawan.common.customFX.DoubleTextField;
import de.julianpadawan.common.customFX.GridPane2C;
import de.julianpadawan.common.customFX.TimeTextField;
import de.julianpadawan.timelog.preferences.PreferenceMap;
import javafx.beans.binding.BooleanExpression;
import javafx.scene.control.*;

public final class PreferencesDialog extends Dialog<Boolean> {

    private static final ButtonType OK_BUTTON = new ButtonType("Restart", ButtonBar.ButtonData.OK_DONE);
    private final BooleanExpression okEnabled;

    public PreferencesDialog() {
        super();
        setTitle("Preferences");
        setHeaderText("Edit Preferences");

        PreferenceMap preferenceMap = new PreferenceMap();
        GridPane2C gridPane2C = new GridPane2C(10);
        getDialogPane().setContent(gridPane2C);

        final DoubleTextField scaling = gridPane2C.addRow("Minute To Pixel Scale", new DoubleTextField(1, false));
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

        gridPane2C.addSeparator();

        final CheckBox useActivityChooser = gridPane2C.addRow("Use Activity Chooser", new CheckBox());
        preferenceMap.mapTo(useActivityChooser, "UseActivityChooser");

        final CheckBox enableGoals = gridPane2C.addRow("Enable Goals", new CheckBox());
        preferenceMap.mapTo(enableGoals, "UseGoals");

        gridPane2C.addSeparator();

        final TextField activityDepth = gridPane2C.addRow("Activity Report Default Depth", new TextField());
        preferenceMap.mapTo(activityDepth, "ActivityStatisticDefaultDepth");

        final CheckBox flattenReport = gridPane2C.addRow("Flatten Activity Report", new CheckBox());
        preferenceMap.mapTo(flattenReport, "FlattenActivityStatistic");

        getDialogPane().getButtonTypes().addAll(OK_BUTTON, ButtonType.CANCEL);
        Button okButton = (Button) getDialogPane().lookupButton(OK_BUTTON);
        okEnabled = CustomBindings.matches(marks, "\\d+")
                .and(scaling.validProperty())
                .and(CustomBindings.matches(marksWidth, "\\d+"))
                .and(startOfDay.valueProperty().isNotNull())
                .and(CustomBindings.matches(sleepID, "-1|\\d+"))
                .and(CustomBindings.matches(sleepLineHeight, "\\d+"))
                .and(CustomBindings.matches(activityDepth, "[1-9]\\d*"));
        okEnabled.addListener(observable -> okButton.setDisable(!okEnabled.getValue()));

        setResultConverter(buttonType -> {
            if (buttonType.equals(OK_BUTTON)) preferenceMap.dumpPreferences();
            return buttonType.equals(OK_BUTTON);
        });
    }

}
