package de.julianpadawan.timelog.view.insight;

import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.view.LogEntryList;
import javafx.scene.control.Alert;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LookAtDayDialog extends Alert {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public LookAtDayDialog(LocalDate date) {
        super(AlertType.INFORMATION);
        setTitle("Look at Day");
        setHeaderText("Day " + DATE_FORMAT.format(date));

        final LogEntryList logEntryList = new LogEntryList();
        logEntryList.getEntries().addAll(LogEntry.FACTORY.getAllFinishedOnDateOf(LogEntry.toStartOfDay(date)));
        getDialogPane().setContent(logEntryList);
        setResizable(true);
    }
}
