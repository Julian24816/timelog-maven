package de.julianpadawan.timelog.view.edit;

import de.julianpadawan.common.customFX.ObjectDialog;
import de.julianpadawan.timelog.model.Person;
import javafx.scene.control.TextField;

class PersonDialog extends ObjectDialog<Person> {
    private final TextField name;

    public PersonDialog() {
        this(null);
    }

    public PersonDialog(Person editedObject) {
        super("Activity", editedObject);

        name = gridPane2C.addRow("Name", new TextField());
        name.setPromptText("enter name");
        addOKRequirement(name.textProperty().isNotEmpty());
        name.requestFocus();

        if (editedObject != null) {
            name.setText(editedObject.getName());
        }
    }

    @Override
    protected Person createNew() {
        return Person.FACTORY.createNew(name.getText());
    }

    @Override
    protected boolean save() {
        editedObject.setName(name.getText());
        return Person.FACTORY.update(editedObject);
    }
}
