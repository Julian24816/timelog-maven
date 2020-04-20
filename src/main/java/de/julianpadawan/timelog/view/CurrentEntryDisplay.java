package de.julianpadawan.timelog.view;

import de.julianpadawan.common.customFX.CustomBindings;
import de.julianpadawan.common.customFX.JoiningTextFlow;
import de.julianpadawan.common.customFX.TimeTextField;
import de.julianpadawan.timelog.model.Activity;
import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.model.MeansOfTransport;
import de.julianpadawan.timelog.view.edit.LogEntryDialog;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

public class CurrentEntryDisplay extends GridPane {

    private final Timer timer = new Timer(true);
    private final Text startTime = new Text("--:--");
    private final Text duration = new Text("--:--");
    private final Button button = new Button("New");
    private final Text activityName = new Text("No Current Activity");
    private final StringProperty what = new SimpleStringProperty(), transport = new SimpleStringProperty();

    private final ObjectProperty<LogEntry> entry = new SimpleObjectProperty<>(this, "current activity") {
        @Override
        protected void invalidated() {
            if (getValue() == null) {
                startTime.setText("--:--");
                activityName.textProperty().unbind();
                activityName.setText("No Current Activity");
                duration.setText("--:--");
                button.setText("New");
                transport.unbind();
                transport.setValue("");
                what.unbind();
                what.setValue("");
            } else {
                startTime.setText(TimeTextField.TIME_FORMATTER.format(getValue().getStart().toLocalTime()));
                activityName.textProperty().bind(CustomBindings.select(getValue().activityProperty(), Activity::nameProperty));
                timer.scheduleAtFixedRate(new TimerTask() {
                    @Override
                    public void run() {
                        if (getValue() == null) cancel();
                        else {
                            Duration duration;
                            if (getValue().getEnd() == null)
                                duration = Duration.between(getValue().getStart(), LocalDateTime.now());
                            else duration = Duration.between(getValue().getEnd(), getValue().getStart());
                            CurrentEntryDisplay.this.duration.setText(TimeTextField.TIME_FORMATTER.format(duration.addTo(LocalTime.MIN)));
                        }
                    }
                }, 0, 500);
                button.setText("Stop");
                transport.bind(CustomBindings.select(getValue().meansOfTransportProperty(), MeansOfTransport::nameProperty));
                what.bind(getValue().whatProperty());
            }
        }
    };

    private final Consumer<LogEntry> newCompleteEntryCallback;

    public CurrentEntryDisplay(Consumer<LogEntry> newCompleteEntryCallback) {
        super();
        this.newCompleteEntryCallback = newCompleteEntryCallback;

        entry.setValue(LogEntry.FACTORY.getUnfinishedEntry());
        button.setOnAction(this::onButtonPress);
        setOnMouseClicked(this::doubleClick);

        createLayout();
    }

    private void onButtonPress(ActionEvent event) {
        if (entry.get() == null) newEntry();
        else stopEntry();
    }

    private void doubleClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() != 2 || !mouseEvent.getButton().equals(MouseButton.PRIMARY)) return;
        if (entry.get() == null) newEntry();
        else editEntry();
    }

    private void createLayout() {
        activityName.setFont(new Font(16));

        Label title = new Label("Current Activity, ");
        title.setLabelFor(activityName);

        Label startTimeLabel = new Label("started at ");
        startTimeLabel.setLabelFor(startTime);

        Label durationLabel = new Label(", active for ");
        durationLabel.setLabelFor(duration);

        add(new HBox(title, startTimeLabel, startTime, durationLabel, duration), 0, 0);
        add(new JoiningTextFlow(activityName, what, transport), 0, 1);
        add(button, 1, 0, 1, 2);

        setHgap(10);
        GridPane.setHalignment(duration, HPos.CENTER);
        button.setMaxHeight(Double.MAX_VALUE);

        final ColumnConstraints growColumn = new ColumnConstraints();
        growColumn.setHgrow(Priority.ALWAYS);
        getColumnConstraints().add(growColumn);
    }

    private void newEntry() {
        Activity activity = ChooseActivityDialog.choose(Activity.getRoot());
        if (activity != null) new LogEntryDialog(activity).showAndWait().ifPresent(this::entryDialogResult);
    }

    private void entryDialogResult(LogEntry entry) {
        if (entry.getEnd() == null) this.entry.setValue(entry);
        else {
            newCompleteEntryCallback.accept(entry);
            this.entry.set(LogEntry.FACTORY.getUnfinishedEntry());
        }
    }

    private void editEntry() {
        new LogEntryDialog(entry.get()).showAndWait().ifPresent(this::entryDialogResult);
    }

    private void stopEntry() {
        new LogEntryDialog(entry.get(), LocalDateTime.now()).showAndWait().ifPresent(this::entryDialogResult);
    }
}
