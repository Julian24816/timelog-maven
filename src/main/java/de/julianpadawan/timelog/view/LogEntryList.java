package de.julianpadawan.timelog.view;

import de.julianpadawan.common.customFX.CustomBindings;
import de.julianpadawan.common.customFX.JoiningTextFlow;
import de.julianpadawan.common.customFX.ResizableCanvas;
import de.julianpadawan.common.customFX.TimeText;
import de.julianpadawan.timelog.model.Activity;
import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.model.MeansOfTransport;
import de.julianpadawan.timelog.preferences.Preferences;
import de.julianpadawan.timelog.view.edit.LogEntryDialog;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.VLineTo;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Consumer;

public class LogEntryList extends ScrollPane {

    private final ResizableCanvas canvas = new ResizableCanvas() {
        @Override
        public void draw() {
            final double height = getHeight();
            final double width = getWidth();
            final GraphicsContext context = getGraphicsContext2D();
            context.setStroke(Color.valueOf(Preferences.get("MinuteMarkColor")));
            context.setLineWidth(1);

            final int minuteMarkEvery = Preferences.getInt("MinuteMarkEvery");
            final double scale = Preferences.getDouble("MinuteToPixelScale");
            final double markWidth = Preferences.getDouble("MinuteMarkWidth");
            double y = minuteMarkEvery / scale;
            context.clearRect(0, 0, width, height);
            while (y < height) {
                context.strokeLine(width - markWidth, y + 0.5, width, y + 0.5);
                y += minuteMarkEvery / scale;
            }
        }
    };

    private final VBox vBox = new VBox();
    private final Text placeholder = new Text("no activities");

    private final ObservableList<LogEntry> entries = FXCollections.observableArrayList();

    public LogEntryList() {
        super();
        canvas.setWidth(Preferences.getDouble("MinuteMarkWidth"));
        InvalidationListener resizeCanvas = observable ->
                canvas.setHeight(Math.max(vBox.getHeight(), getViewportBounds().getHeight()));
        viewportBoundsProperty().addListener(resizeCanvas);
        vBox.heightProperty().addListener(resizeCanvas);
        vBox.setMaxHeight(Region.USE_PREF_SIZE);

        vBox.getChildren().add(placeholder);
        VBox.setMargin(placeholder, new Insets(20));

        setContent(new StackPane(vBox, canvas));
        StackPane.setAlignment(vBox, Pos.TOP_LEFT);
        StackPane.setAlignment(canvas, Pos.TOP_RIGHT);
        HBox.setHgrow(vBox, Priority.ALWAYS);

        setFitToWidth(true);
        setPrefHeight(400);
        vBox.setPrefWidth(500);

        entries.addListener(this::onListChanged);
    }

    private void onListChanged(ListChangeListener.Change<? extends LogEntry> c) {
        vBox.getChildren().clear();
        vBox.getChildren().add(placeholder);
        entries.forEach(this::addEntry);
    }

    private void addEntry(LogEntry entry) {
        vBox.getChildren().remove(placeholder);
        vBox.getChildren().add(new ActivityLine(entry));
    }

    public ObservableList<LogEntry> getEntries() {
        return entries;
    }

    public static final class ActivityLine extends HBox {
        private static final double TIME_TEXT_WIDTH = 30, TIME_TEXT_HEIGHT = 16,
                DETAILS_VISIBLE_HEIGHT = 16;
        private final LogEntry entry;
        private BooleanBinding detailsVisibility;

        private ActivityLine(LogEntry entry) {
            this(entry, null);
        }

        public ActivityLine(LogEntry entry, LocalDateTime cutoff) {
            super(10);
            this.entry = entry;
            createLayout(entry, cutoff);
            setOnMouseClicked(this::onMouseClicked);
            backgroundProperty().bind(CustomBindings.apply(
                    CustomBindings.select(entry.activityProperty(), Activity::colorProperty),
                    color -> new Background(new BackgroundFill(Color.valueOf(color), null, null))));
        }

        private void createLayout(LogEntry entry, LocalDateTime cutoff) {
            final VBox time = getTimeVBox(entry, cutoff, entry.getActivity().getId() == Preferences.getInt("SleepID"));
            final TextFlow details = getDetails(entry);
            detailsVisibility = time.heightProperty().greaterThanOrEqualTo(DETAILS_VISIBLE_HEIGHT);
            detailsVisibility.addListener(observable -> {
                getChildren().remove(details);
                if (detailsVisibility.get()) getChildren().add(details);
            });
            getChildren().add(time);
        }

        private void onMouseClicked(MouseEvent mouseEvent) {
            if (mouseEvent.getClickCount() != 2 || !mouseEvent.getButton().equals(MouseButton.PRIMARY)) return;
            new LogEntryDialog(entry).show();
        }

        private VBox getTimeVBox(LogEntry entry, LocalDateTime cutoff, boolean sleep) {
            final TimeText start = new TimeText();
            start.valueProperty().bind(entry.startProperty());
            final TimeText end = new TimeText();
            end.valueProperty().bind(entry.endProperty());
            final VLineTo vLineTo = new VLineTo(1);
            final Path line = new Path(new MoveTo(0, 0), vLineTo);
            final VBox time = new VBox(line);
            time.setAlignment(Pos.CENTER);
            time.setPrefWidth(TIME_TEXT_WIDTH);

            applyLineHeight(entry, cutoff, sleep, lineHeight -> {
                time.getChildren().removeAll(start, end);
                if (lineHeight > TIME_TEXT_HEIGHT * 2) {
                    time.getChildren().add(0, start);
                    time.getChildren().add(end);
                    vLineTo.setY(lineHeight - TIME_TEXT_HEIGHT * 2);
                } else if (lineHeight > TIME_TEXT_HEIGHT) {
                    if (sleep) time.getChildren().add(end);
                    else time.getChildren().add(0, start);
                    vLineTo.setY(lineHeight - TIME_TEXT_HEIGHT);
                } else vLineTo.setY(lineHeight);
            });
            return time;
        }

        private JoiningTextFlow getDetails(LogEntry entry) {
            final Text activityName = new Text();
            activityName.textProperty().bind(CustomBindings.select(entry.activityProperty(), Activity::nameProperty));
            return new JoiningTextFlow(activityName,
                    entry.whatProperty(),
                    CustomBindings.select(entry.meansOfTransportProperty(), MeansOfTransport::nameProperty));
        }

        private void applyLineHeight(LogEntry entry, LocalDateTime cutoff, boolean sleep, Consumer<Double> applyLineHeight) {
            if (cutoff != null || !sleep) {
                InvalidationListener invalidated = observable -> {
                    if (entry.getEnd() == null) return;
                    LocalDateTime startTime = this.entry.getStart();
                    if (cutoff != null && startTime.isBefore(cutoff)) startTime = cutoff;
                    final long minutes = startTime.until(entry.getEnd(), ChronoUnit.MINUTES);
                    final double lineHeight = minutes / Preferences.getDouble("MinuteToPixelScale");
                    applyLineHeight.accept(lineHeight);
                };
                entry.startProperty().addListener(invalidated);
                entry.endProperty().addListener(invalidated);
                invalidated.invalidated(null);
            } else applyLineHeight.accept(Preferences.getDouble("SleepLineHeight"));
        }

        @Override
        public boolean equals(Object o) {
            //noinspection ObjectComparison
            if (this == o) return true;
            if (o == null || !getClass().equals(o.getClass())) return false;
            ActivityLine that = (ActivityLine) o;
            return entry.equals(that.entry);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entry);
        }
    }
}
