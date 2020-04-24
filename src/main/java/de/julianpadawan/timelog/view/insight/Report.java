package de.julianpadawan.timelog.view.insight;

import de.julianpadawan.timelog.insight.ActivityStatistic;
import de.julianpadawan.timelog.insight.QualityTimeStatistic;
import de.julianpadawan.timelog.insight.Statistic;
import de.julianpadawan.timelog.insight.StatisticalDatum;
import de.julianpadawan.timelog.model.Activity;
import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.preferences.Preferences;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class Report extends Alert {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public Report(String timeFrame, Collection<LogEntry> logEntries) {
        super(AlertType.INFORMATION);
        setTitle("Report");
        setHeaderText("Report for " + timeFrame);

        final VBox vBox = new VBox(20);
        Statistic<Activity, Duration> activityStatistic = ActivityStatistic.of(logEntries);
        if (Preferences.getBoolean("FlattenActivityStatistic")) activityStatistic = activityStatistic.flattened();
        vBox.getChildren().add(new ReportLine<>(activityStatistic, Preferences.getInt("ActivityStatisticDefaultDepth")));
        vBox.getChildren().add(new ReportLine<>(QualityTimeStatistic.of(logEntries), 1));

        final ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(500);
        getDialogPane().setContent(scrollPane);
        getDialogPane().setPrefSize(250, 400);
        setResizable(true);
    }

    public static Report on(LocalDate date) {
        return new Report(DATE_FORMAT.format(date), LogEntry.FACTORY.getAllFinishedOnDateOf(LogEntry.atStartOfDay(date)));
    }

    public static Report between(LocalDate begin, LocalDate end) {
        if (!begin.isBefore(end)) throw new IllegalArgumentException();
        return new Report(DATE_FORMAT.format(begin) + " - " + DATE_FORMAT.format(end),
                LogEntry.FACTORY.getAllFinishedBetween(begin, end.plus(1, ChronoUnit.DAYS)));
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
