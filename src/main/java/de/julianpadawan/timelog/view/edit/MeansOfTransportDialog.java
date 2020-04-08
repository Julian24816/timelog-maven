package de.julianpadawan.timelog.view.edit;

import de.julianpadawan.common.customFX.ObjectDialog;
import de.julianpadawan.timelog.model.MeansOfTransport;
import javafx.scene.control.TextField;

class MeansOfTransportDialog extends ObjectDialog<MeansOfTransport> {
    private final TextField name;

    public MeansOfTransportDialog() {
        this(null);
    }

    public MeansOfTransportDialog(MeansOfTransport editedObject) {
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
    protected MeansOfTransport createNew() {
        return MeansOfTransport.FACTORY.createNew(name.getText());
    }

    @Override
    protected boolean save() {
        editedObject.setName(name.getText());
        return MeansOfTransport.FACTORY.update(editedObject);
    }
}
