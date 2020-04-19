package de.julianpadawan.timelog.view.edit;

import de.julianpadawan.common.customFX.CreatingChoiceBox;
import de.julianpadawan.common.customFX.CustomBindings;
import de.julianpadawan.common.customFX.DurationMinutesTextField;
import de.julianpadawan.common.customFX.ObjectDialog;
import de.julianpadawan.timelog.insight.StreakCalculator;
import de.julianpadawan.timelog.model.Activity;
import de.julianpadawan.timelog.model.Goal;
import de.julianpadawan.timelog.model.Person;
import javafx.scene.control.TextField;

import java.time.Duration;


public final class GoalDialog extends ObjectDialog<Goal> {
    private final CreatingChoiceBox<Activity> activity;
    private final TextField interval;
    private final DurationMinutesTextField minDuration;
    private final CreatingChoiceBox<Person> person;

    public GoalDialog() {
        this(null);
    }

    public GoalDialog(Goal editedObject) {
        super("Goal", editedObject);

        activity = gridPane2C.addRow("Activity",
                CreatingChoiceBox.simple(Activity.FACTORY.getAll(), ActivityDialog::new, ActivityDialog::new));
        activity.setValue(Activity.getRoot());

        interval = gridPane2C.addRow("Interval", new TextField());
        interval.setPromptText(StreakCalculator.getPrompt());

        minDuration = gridPane2C.addRow("Min Duration in m", new DurationMinutesTextField(Duration.ZERO));
        person = gridPane2C.addRow("Person",
                CreatingChoiceBox.nullable(Person.FACTORY.getAll(), PersonDialog::new, PersonDialog::new));

        addOKRequirement(activity.valueProperty().isNotNull());
        addOKRequirement(CustomBindings.apply(interval.textProperty(), StreakCalculator::validInterval));
        addOKRequirement(minDuration.valueProperty().isNotNull());

        if (editedObject != null) {
            activity.setValue(editedObject.getActivity());
            interval.setText(editedObject.getInterval());
            minDuration.setValue(editedObject.getMinDuration());
            person.setValue(editedObject.getPerson());
        }
    }

    @Override
    protected Goal createNew() {
        return Goal.FACTORY.createNew(
                activity.getValue(),
                interval.getText(),
                minDuration.getValue(),
                person.getValue()
        );
    }

    @Override
    protected boolean save() {
        editedObject.setActivity(activity.getValue());
        editedObject.setInterval(interval.getText());
        editedObject.setMinDuration(minDuration.getValue());
        editedObject.setPerson(person.getValue());
        return Goal.FACTORY.update(editedObject);
    }
}
