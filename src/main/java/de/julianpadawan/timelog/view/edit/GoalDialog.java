package de.julianpadawan.timelog.view.edit;

import de.julianpadawan.common.customFX.CreatingChoiceBox;
import de.julianpadawan.common.customFX.CustomBindings;
import de.julianpadawan.common.customFX.ObjectDialog;
import de.julianpadawan.timelog.insight.StreakCalculator;
import de.julianpadawan.timelog.model.Activity;
import de.julianpadawan.timelog.model.Goal;
import javafx.scene.control.TextField;


public class GoalDialog extends ObjectDialog<Goal> {
    private final CreatingChoiceBox<Activity> activity;
    private final TextField interval;

    public GoalDialog() {
        this(null);
    }

    public GoalDialog(Goal editedObject) {
        super("Goal", editedObject);

        activity = gridPane2C.addRow("Activity", CreatingChoiceBox.simple(Activity.FACTORY.getAll()));
        activity.setValue(Activity.FACTORY.getForId(0));

        interval = gridPane2C.addRow("Interval", new TextField());
        interval.setPromptText("1d");

        addOKRequirement(activity.valueProperty().isNotNull());
        addOKRequirement(CustomBindings.apply(interval.textProperty(), StreakCalculator::validInterval));

        if (editedObject != null) {
            activity.setValue(editedObject.getActivity());
            interval.setText(editedObject.getInterval());
        }
    }

    @Override
    protected Goal createNew() {
        return Goal.FACTORY.createNew(activity.getValue(), interval.getText());
    }

    @Override
    protected boolean save() {
        editedObject.setActivity(activity.getValue());
        editedObject.setInterval(interval.getText());
        return Goal.FACTORY.update(editedObject);
    }
}
