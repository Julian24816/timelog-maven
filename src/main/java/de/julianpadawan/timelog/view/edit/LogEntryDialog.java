package de.julianpadawan.timelog.view.edit;

import de.julianpadawan.common.customFX.*;
import de.julianpadawan.timelog.model.*;
import javafx.beans.binding.BooleanBinding;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public final class LogEntryDialog extends ObjectDialog<LogEntry> {
    private final CreatingChoiceBox<Activity> activity;
    private final TextField what;
    private final CreatingChoiceBox<MeansOfTransport> meansOfTransport;
    private final AssociationFlowPane<LogEntry, Person, QualityTime> people;
    private final DatePicker startDate;
    private final TimeTextField startTime;
    private final DatePicker endDate;
    private final TimeTextField endTime;

    public LogEntryDialog() {
        this(null);
    }

    public LogEntryDialog(LogEntry editedObject) {
        super("Log Entry", editedObject);

        activity = gridPane2C.addRow("Type", CreatingChoiceBox.simple(
                Activity.FACTORY.getAll(), ActivityDialog::new, ActivityDialog::new));
        activity.setValue(Activity.FACTORY.getForId(0));
        meansOfTransport = gridPane2C.addRow("Transport", CreatingChoiceBox.nullable(
                MeansOfTransport.FACTORY.getAll(), MeansOfTransportDialog::new, MeansOfTransportDialog::new));
        people = gridPane2C.addRow("People", new AssociationFlowPane<>(
                QualityTime.FACTORY, editedObject, Person.FACTORY.getAll(), PersonDialog::new, PersonDialog::new));
        what = gridPane2C.addRow("Details", new TextField());
        gridPane2C.addSeparator();

        startDate = gridPane2C.addRow("Start", new DatePicker(LocalDate.now()));
        startTime = gridPane2C.addRow("", new TimeTextField(LocalTime.now()));
        gridPane2C.addButtonRow(Util.button("After Previous", this::afterPrevious), Util.button("Now", this::nowStart));
        endDate = gridPane2C.addRow("End", new DatePicker(LocalDate.now()));
        endTime = gridPane2C.addRow("", new TimeTextField(null));
        gridPane2C.addButtonRow(
                Util.button("-10 min", () -> plusMinutes(-10)),
                Util.button("-1 min", () -> plusMinutes(-1)),
                Util.button("Now", this::nowEnd),
                Util.button("+1 min", () -> plusMinutes(1)),
                Util.button("+10 min", () -> plusMinutes(10)),
                Util.button("Clear", this::clear));

        Util.applyAfterFocusLost(startDate);
        Util.applyAfterFocusLost(endDate);

        addOKRequirement(activity.valueProperty().isNotNull());
        addOKRequirement(startDate.valueProperty().isNotNull());
        addOKRequirement(startTime.valueProperty().isNotNull());
        addOKRequirement(endDate.valueProperty().isNull().or(endTime.valueProperty().isNull().or(new BooleanBinding() {
            {
                bind(startDate.valueProperty(), startTime.valueProperty(),
                        endDate.valueProperty(), endTime.valueProperty());
            }

            @Override
            protected boolean computeValue() {
                if (startDate.getValue() == null || startTime.getValue() == null
                        || endDate.getValue() == null || endTime.getValue() == null) return false;
                return !LocalDateTime.of(endDate.getValue(), endTime.getValue())
                        .isBefore(LocalDateTime.of(startDate.getValue(), startTime.getValue()));
            }
        })));

        if (editedObject != null) {
            activity.setValue(editedObject.getActivity());
            what.setText(editedObject.getWhat());
            meansOfTransport.setValue(editedObject.getMeansOfTransport());
            startDate.setValue(editedObject.getStart().toLocalDate());
            startTime.setValue(editedObject.getStart().toLocalTime());
            if (editedObject.getEnd() != null) {
                endDate.setValue(editedObject.getEnd().toLocalDate());
                endTime.setValue(editedObject.getEnd().toLocalTime());
            }
        }
    }

    private void afterPrevious() {
        LogEntry lastEntry = LogEntry.FACTORY.getLast();
        startDate.setValue(lastEntry.getEnd().toLocalDate());
        startTime.setValue(lastEntry.getEnd().toLocalTime());
    }

    private void nowStart() {
        startDate.setValue(LocalDate.now());
        startTime.setValue(LocalTime.now());
    }

    private void plusMinutes(final int minutes) {
        LocalDateTime target;
        if (endDate.getValue() != null && endTime.getValue() != null)
            target = LocalDateTime.of(endDate.getValue(), endTime.getValue());
        else if (startDate.getValue() != null && startTime.getValue() != null)
            target = LocalDateTime.of(startDate.getValue(), startTime.getValue());
        else return;
        target = target.plus(minutes, ChronoUnit.MINUTES);
        endDate.setValue(target.toLocalDate());
        endTime.setValue(target.toLocalTime());
    }

    private void nowEnd() {
        endDate.setValue(LocalDate.now());
        endTime.setValue(LocalTime.now());
    }

    private void clear() {
        endTime.setValue(null);
    }

    @Override
    protected LogEntry createNew() {
        final LogEntry logEntry = LogEntry.FACTORY.createNew(
                activity.getValue(),
                what.getText(),
                LocalDateTime.of(startDate.getValue(), startTime.getValue()),
                endTime.getValue() == null || endDate.getValue() == null ? null : LocalDateTime.of(endDate.getValue(), endTime.getValue()),
                meansOfTransport.getValue()
        );
        people.associateAll(logEntry);
        return logEntry;
    }

    @Override
    protected boolean save() {
        editedObject.setActivity(activity.getValue());
        editedObject.setWhat(what.getText());
        editedObject.setStart(LocalDateTime.of(startDate.getValue(), startTime.getValue()));
        editedObject.setEnd(endTime.getValue() == null ? null : LocalDateTime.of(endDate.getValue(), endTime.getValue()));
        editedObject.setMeansOfTransport(meansOfTransport.getValue());
        return LogEntry.FACTORY.update(editedObject);
    }
}
