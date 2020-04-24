package de.julianpadawan.timelog.view.insight;

import de.julianpadawan.common.customFX.CreatingChoiceBox;
import de.julianpadawan.timelog.model.Activity;
import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.preferences.Preferences;
import de.julianpadawan.timelog.view.App;
import de.julianpadawan.timelog.view.ChooseActivityDialog;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;

public class ListAll extends Alert {
    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public ListAll(String headerText, Activity activity, Collection<LogEntry> logEntries) {
        super(AlertType.INFORMATION);
        setTitle("ListAll");
        setHeaderText(headerText);

        final ScrollPane scrollPane = new ScrollPane(createGridPane(activity, logEntries));
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(500);
        getDialogPane().setContent(scrollPane);
        getDialogPane().setPrefSize(500, 500);
    }

    private GridPane createGridPane(Activity activity, Collection<LogEntry> logEntries) {
        final GridPane grid = new GridPane();
        grid.setHgap(5);
        grid.setVgap(5);
        int row = 0;
        LocalDate accumulatingDate = null;
        Duration accumulatedDuration = Duration.ZERO;
        int accumulated = 0;
        for (LogEntry logEntry : logEntries) {
            if (!logEntry.getActivity().instanceOf(activity)) continue;
            LocalDate date = LogEntry.getDate(logEntry.getEnd());
            Duration duration = Duration.between(logEntry.getStart(), logEntry.getEnd());

            if (date.equals(accumulatingDate)) {
                accumulatedDuration = accumulatedDuration.plus(duration);
                accumulated++;
            } else {
                if (accumulatingDate != null)
                    row = addAccumulatingRow(grid, row, accumulatingDate, accumulatedDuration, accumulated);
                accumulatingDate = date;
                accumulatedDuration = duration;
                accumulated = 1;
            }

            grid.addRow(row++,
                    new Text(FORMAT.format(logEntry.getStart())),
                    new Text(FORMAT.format(logEntry.getEnd())),
                    new Text(App.formatDuration(duration, false)),
                    new Text(logEntry.getActivity().getName()),
                    new Text(logEntry.getWhat())
            );
        }
        addAccumulatingRow(grid, row, accumulatingDate, accumulatedDuration, accumulated);
        return grid;
    }

    private int addAccumulatingRow(GridPane grid, int row, LocalDate accumulatingDate, Duration accumulatedDuration, int accumulated) {
        if (accumulated > 1) {
            grid.add(new Separator(), 0, row++, 5, 1);

            grid.add(new Text(DATE_FORMAT.format(accumulatingDate)), 0, row);
            grid.add(new Text(App.formatDuration(accumulatedDuration, false)), 2, row);
            row++;

        }
        grid.add(new Separator(), 0, row++, 5, 1);
        row++;
        return row;
    }

    public static Optional<ListAll> on(LocalDate date) {
        return getListAll(DATE_FORMAT.format(date), LogEntry.FACTORY.getAllFinishedOnDateOf(LogEntry.atStartOfDay(date)));
    }

    private static Optional<ListAll> getListAll(final String headerText, final Collection<LogEntry> entries) {
        if (Preferences.getBoolean("UseActivityChooser")) {
            return Optional.ofNullable(ChooseActivityDialog.choose(Activity.getRoot()))
                    .map(activity -> new ListAll(headerText, activity, entries));
        } else {
            final CreatingChoiceBox<Activity> choiceBox = CreatingChoiceBox.simple(Activity.FACTORY.getAll());
            choiceBox.setValue(Activity.getRoot());
            final Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("ListAll");
            alert.setHeaderText(headerText);
            alert.getDialogPane().setContent(choiceBox);
            if (alert.showAndWait().filter(buttonType -> buttonType.equals(ButtonType.OK)).isPresent()) {
                return Optional.of(new ListAll(headerText, choiceBox.getValue(), entries));
            }
        }
        return Optional.empty();
    }

    public static Optional<ListAll> between(LocalDate begin, LocalDate end) {
        if (!begin.isBefore(end)) throw new IllegalArgumentException();
        return getListAll(DATE_FORMAT.format(begin) + " - " + DATE_FORMAT.format(end), LogEntry.FACTORY.getAllFinishedBetween(begin, end.plus(1, ChronoUnit.DAYS)));
    }
}
