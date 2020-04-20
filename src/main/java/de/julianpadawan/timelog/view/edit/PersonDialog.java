package de.julianpadawan.timelog.view.edit;

import de.julianpadawan.common.customFX.DoubleTextField;
import de.julianpadawan.common.customFX.ObjectDialog;
import de.julianpadawan.timelog.model.Person;
import de.julianpadawan.timelog.preferences.Preferences;
import javafx.scene.control.TextField;

final class PersonDialog extends ObjectDialog<Person> {
    private final TextField name;
    private final DoubleTextField pointsFactor;

    public PersonDialog() {
        this(null);
    }

    public PersonDialog(Person editedObject) {
        super("Activity", editedObject);

        name = gridPane2C.addRow("Name", new TextField());
        name.setPromptText("enter name");
        pointsFactor = new DoubleTextField(1, false);
        if (Preferences.getBoolean("UseGoals")) gridPane2C.addRow("Points Factor", pointsFactor);

        addOKRequirement(name.textProperty().isNotEmpty());
        addOKRequirement(pointsFactor.validProperty());

        name.requestFocus();

        if (editedObject != null) {
            name.setText(editedObject.getName());
            pointsFactor.setValue(editedObject.getPointsFactor());
        }
    }

    @Override
    protected Person createNew() {
        return Person.FACTORY.createNew(name.getText(), pointsFactor.getValue());
    }

    @Override
    protected boolean save() {
        editedObject.setName(name.getText());
        editedObject.setPointsFactor(pointsFactor.getValue());
        return Person.FACTORY.update(editedObject);
    }
}
