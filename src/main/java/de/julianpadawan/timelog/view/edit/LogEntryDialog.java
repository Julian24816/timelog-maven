package de.julianpadawan.timelog.view.edit;

import de.julianpadawan.common.customFX.*;
import de.julianpadawan.timelog.model.*;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public final class LogEntryDialog extends ObjectDialog<LogEntry> {
    private final CreatingChoiceBox<Activity> activity;
    private final TextField what;
    private final CreatingChoiceBox<MeansOfTransport> meansOfTransport;
    private final AssociationFlowPane<LogEntry, Person, QualityTime> people;
    private final DateTimePicker start, end;

    public LogEntryDialog() {
        this(null);
    }

    public LogEntryDialog(LogEntry editedObject) {
        super("Log Entry", editedObject);

        activity = gridPane2C.addRow("Type", CreatingChoiceBox.simple(
                Activity.FACTORY.getAll(), ActivityDialog::new, ActivityDialog::new));
        activity.setValue(Activity.getRoot());
        meansOfTransport = gridPane2C.addRow("Transport", CreatingChoiceBox.nullable(
                MeansOfTransport.FACTORY.getAll(), MeansOfTransportDialog::new, MeansOfTransportDialog::new));
        people = gridPane2C.addRow("People", new AssociationFlowPane<>(
                QualityTime.FACTORY, editedObject, Person.FACTORY.getAll(), PersonDialog::new, PersonDialog::new));
        what = gridPane2C.addRow("Details", new TextField());
        gridPane2C.addSeparator();

        start = gridPane2C.addRow("Start", new DateTimePicker(LocalDateTime.now()));
        gridPane2C.addButtonRow(Util.button("After Previous", this::afterPrevious), Util.button("Now", this::nowStart));
        end = gridPane2C.addRow("End", new DateTimePicker(LocalDate.now(), null));
        gridPane2C.addButtonRow(
                Util.button("-10 min", () -> plusMinutes(-10, LocalDateTime.now())),
                Util.button("-1 min", () -> plusMinutes(-1, LocalDateTime.now())),
                Util.button("Now", this::nowEnd),
                Util.button("+1 min", () -> plusMinutes(1, start.getValue())),
                Util.button("+10 min", () -> plusMinutes(10, start.getValue())),
                Util.button("Clear", this::clearEnd));

        addOKRequirement(activity.valueProperty().isNotNull());
        addOKRequirement(start.valueProperty().isNotNull());
        addOKRequirement(end.valueProperty().isNull().or(new BooleanBinding() {
            {
                bind(start.valueProperty(), end.valueProperty());
            }

            @Override
            protected boolean computeValue() {
                if (start.getValue() == null || end.getValue() == null) return false;
                return !end.getValue().isBefore(start.getValue());
            }
        }));

        if (editedObject != null) {
            activity.setValue(editedObject.getActivity());
            what.setText(editedObject.getWhat());
            meansOfTransport.setValue(editedObject.getMeansOfTransport());
            start.setValue(editedObject.getStart());
            end.setValue(editedObject.getEnd());
        }
    }

    private void afterPrevious() {
        LogEntry lastEntry = LogEntry.FACTORY.getLast();
        if (lastEntry != null) start.setValue(lastEntry.getEnd());
    }

    private void nowStart() {
        start.setValue(LocalDateTime.now());
    }

    private void plusMinutes(final int minutes, final LocalDateTime reference) {
        LocalDateTime relativeTo;
        if (end.getValue() != null) relativeTo = end.getValue();
        else if (reference != null) relativeTo = reference;
        else return;
        end.setValue(relativeTo.plus(minutes, ChronoUnit.MINUTES));
    }

    private void nowEnd() {
        end.setValue(LocalDateTime.now());
    }

    private void clearEnd() {
        end.setValue(null);
    }

    @Override
    protected LogEntry createNew() {
        final LogEntry logEntry = LogEntry.FACTORY.createNew(
                activity.getValue(),
                what.getText(),
                start.getValue(),
                end.getValue(),
                meansOfTransport.getValue()
        );
        people.associateAll(logEntry);
        return logEntry;
    }

    @Override
    protected boolean save() {
        editedObject.setActivity(activity.getValue());
        editedObject.setWhat(what.getText());
        editedObject.setStart(start.getValue());
        editedObject.setEnd(end.getValue());
        editedObject.setMeansOfTransport(meansOfTransport.getValue());
        return LogEntry.FACTORY.update(editedObject);
    }
}
