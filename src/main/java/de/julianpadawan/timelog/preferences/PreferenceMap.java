package de.julianpadawan.timelog.preferences;

import de.julianpadawan.common.customFX.TimeTextField;
import de.julianpadawan.timelog.view.edit.ActivityDialog;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextInputControl;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class PreferenceMap {
    private Map<TextInputControl, String> textInputControls = new HashMap<>();
    private Map<ChoiceBox<?>, String> choiceBoxes = new HashMap<>();
    private Map<CheckBox, String> checkBoxes = new HashMap<>();
    private Map<ColorPicker, String> colorPickers = new HashMap<>();
    private Map<TimeTextField, String> timeControls = new HashMap<>();

    public void mapTo(CheckBox checkBox, String key) {
        checkBoxes.put(checkBox, key);
        checkBox.setSelected(Preferences.getBoolean(key));
    }

    public void mapTo(TimeTextField time, String key) {
        timeControls.put(time, key);
        time.setValue(Preferences.getTime(key));
    }

    public void mapTo(TextInputControl text, String key) {
        textInputControls.put(text, key);
        text.setText(Preferences.get(key));
    }

    public <T> void mapTo(ChoiceBox<T> choiceBox, String key, Function<String, T> fromString) {
        choiceBoxes.put(choiceBox, key);
        choiceBox.setValue(fromString.apply(Preferences.get(key)));
    }


    public void mapTo(ColorPicker colorPicker, String key) {
        colorPickers.put(colorPicker, key);
        colorPicker.setValue(Color.valueOf(Preferences.get(key)));
    }

    public void dumpPreferences() {
        choiceBoxes.forEach((choiceBox, key) -> Preferences.set(key, choiceBox.getValue().toString()));
        textInputControls.forEach((text, key) -> Preferences.set(key, text.getText()));
        checkBoxes.forEach((checkBox, key) -> Preferences.set(key, checkBox.isSelected() ? "true" : "false"));
        colorPickers.forEach((colorPicker, key) -> Preferences.set(key, ActivityDialog.colorToHex(colorPicker.getValue())));
        timeControls.forEach((time, key) -> Preferences.set(key, time.getValue()));
    }
}
