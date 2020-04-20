package de.julianpadawan.timelog.view.insight;

import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.preferences.Preferences;
import de.julianpadawan.timelog.view.LogEntryList;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class LookAtDaysDialog extends Alert {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public LookAtDaysDialog(LocalDate from, LocalDate to) {
        this(from, (int) (from.until(to, ChronoUnit.DAYS) + 1));
    }

    public LookAtDaysDialog(LocalDate from, int days) {
        super(AlertType.INFORMATION);
        setTitle("Look at Days");
        setHeaderText(DATE_FORMAT.format(from) + " - " + DATE_FORMAT.format(from.plusDays(days - 1)));

        final HBox daysBox = new HBox();
        for (int i = 0; i < days; i++) {
            VBox list = new VBox();
            final LocalDate date = from.plusDays(i);
            final LocalDateTime startOfDay = date.atTime(Preferences.getTime("StartOfDay"));

            for (LogEntry logEntry : LogEntry.FACTORY.getAllFinishedOnDateOf(LogEntry.atStartOfDay(date))) {
                list.getChildren().add(new LogEntryList.ActivityLine(logEntry, startOfDay));
            }

            list.prefWidthProperty().bind(daysBox.widthProperty().divide(days));
            daysBox.getChildren().add(list);
            HBox.setHgrow(list, Priority.ALWAYS);
        }

        final ScrollPane scrollPane = new ScrollPane(daysBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxWidth(Screen.getPrimary().getVisualBounds().getWidth() - 20);
        scrollPane.setPrefHeight(500);
        scrollPane.setPrefWidth(800);
        getDialogPane().setContent(scrollPane);
        setResizable(true);
    }
}
