package de.julianpadawan.timelog.view.insight;

import de.julianpadawan.common.customFX.CreatingChoiceBox;
import de.julianpadawan.timelog.insight.Statistic;
import de.julianpadawan.timelog.insight.StatisticalDatum;
import de.julianpadawan.timelog.model.Activity;
import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.preferences.Preferences;
import de.julianpadawan.timelog.view.App;
import de.julianpadawan.timelog.view.ChooseActivityDialog;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

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
        scrollPane.widthProperty().addListener((observable, oldValue, newValue) -> System.out.println("w:" + newValue));
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

    private static class ReportLine<T, D> extends StackPane {
        private final HBox single;
        private final VBox all;
        private boolean expanded;

        public ReportLine(Statistic<T, D> statistic, int expandedDepth) {
            single = getLine(statistic.getName(), statistic.getAggregateData());
            all = getExpandedView(statistic, expandedDepth);

            getChildren().add(single);
            setOnMouseClicked(this::onclick);
            show(expanded = expandedDepth > 0);
        }

        private HBox getLine(String label, StatisticalDatum<?> datum) {
            final Region spacer = new Region();
            spacer.maxWidth(Double.MAX_VALUE);
            HBox.setHgrow(spacer, Priority.ALWAYS);
            return new HBox(10, new Text(label), spacer, new Text(datum.toString()));
        }

        private VBox getExpandedView(Statistic<T, D> statistic, int expandedDepth) {
            VBox all = new VBox(5);
            all.setPadding(new Insets(0, 0, 10, 0));
            all.getChildren().add(getLine(statistic.getName(), statistic.getData()));

            final List<Statistic<T, D>> subStatistics = new ArrayList<>(statistic.getSubStatistics());
            subStatistics.sort(Comparator.<Statistic<T, D>>naturalOrder().reversed());
            for (Statistic<T, D> subStatistic : subStatistics) {
                final ReportLine<T, D> line = new ReportLine<>(subStatistic, Math.max(0, expandedDepth - 1));
                line.setPadding(new Insets(0, 0, 0, 10));
                all.getChildren().add(line);
            }
            return all.getChildren().size() > 1 ? all : null;
        }

        private void onclick(MouseEvent mouseEvent) {
            if (!mouseEvent.getButton().equals(MouseButton.PRIMARY)) return;
            show(expanded = !expanded);
            mouseEvent.consume();
        }

        private void show(boolean expanded) {
            if (all == null) return;
            getChildren().removeAll(single, all);
            getChildren().add(expanded ? all : single);
        }
    }
}
