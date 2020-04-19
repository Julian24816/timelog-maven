package de.julianpadawan.timelog.view.edit;

import de.julianpadawan.common.customFX.CreatingChoiceBox;
import de.julianpadawan.common.customFX.ObjectDialog;
import de.julianpadawan.timelog.model.Activity;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collection;

public final class ActivityDialog extends ObjectDialog<Activity> {
    private final CreatingChoiceBox<Activity> parent;
    private final TextField name;
    private final ColorPicker color;

    public ActivityDialog() {
        this(null);
    }

    public ActivityDialog(Activity editedObject) {
        super("Activity", editedObject);

        final Collection<Activity> all = Activity.FACTORY.getAll(), filtered = new ArrayList<>(all);
        if (editedObject != null)
            all.stream().filter(activity -> activity.instanceOf(editedObject)).forEach(filtered::remove);
        parent = gridPane2C.addRow("Parent", CreatingChoiceBox.simple(filtered));
        parent.setValue(Activity.getRoot());

        name = gridPane2C.addRow("Name", new TextField());
        name.setPromptText("enter name");

        color = gridPane2C.addRow("Color", new ColorPicker(Color.valueOf(Activity.DEFAULT_COLOR)));

        addOKRequirement(parent.valueProperty().isNotNull());
        addOKRequirement(name.textProperty().isNotEmpty());

        name.requestFocus();

        if (editedObject != null) {
            parent.setValue(editedObject.getParent());
            name.setText(editedObject.getName());
            color.setValue(Color.valueOf(editedObject.getColor()));
            if (editedObject.equals(Activity.getRoot())) parent.setDisable(true);
        } else {
            parent.valueProperty().addListener(observable -> {
                if (parent.getValue() != null) color.setValue(Color.valueOf(parent.getValue().getColor()));
            });
        }
    }

    @Override
    protected Activity createNew() {
        return Activity.FACTORY.createNew(
                parent.getValue(),
                name.getText(),
                colorToHex(color.getValue())
        );
    }

    public static String colorToHex(Color value) {
        long red = Math.round(value.getRed() * 255);
        long green = Math.round(value.getGreen() * 255);
        long blue = Math.round(value.getBlue() * 255);
        long opacity = Math.round(value.getOpacity() * 255);
        return String.format("#%02X%02X%02X%02X", red, green, blue, opacity);
    }

    @Override
    protected boolean save() {
        editedObject.setParent(parent.getValue());
        editedObject.setName(name.getText());
        editedObject.setColor(colorToHex(color.getValue()));
        return Activity.FACTORY.update(editedObject);
    }
}
