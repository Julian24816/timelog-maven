package de.julianpadawan.timelog.view;

import de.julianpadawan.common.customFX.DatePickerDialog;
import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.preferences.Preferences;
import de.julianpadawan.timelog.view.edit.AllActivitiesDialog;
import de.julianpadawan.timelog.view.edit.LogEntryDialog;
import de.julianpadawan.timelog.view.edit.PreferencesDialog;
import de.julianpadawan.timelog.view.insight.LookAtDayDialog;
import de.julianpadawan.timelog.view.insight.LookAtDaysDialog;
import de.julianpadawan.timelog.view.insight.Report;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.function.Supplier;

public class MainScene extends Scene {

    private final LogEntryList logEntryList = new LogEntryList();
    private final GoalsList goals = new GoalsList();

    public MainScene() {
        super(new BorderPane());

        logEntryList.getEntries().addAll(LogEntry.FACTORY.getAllFinishedOnDateOf(LocalDateTime.now()));

        final CurrentEntryDisplay currentEntryDisplay = new CurrentEntryDisplay(logEntry -> {
            if (logEntry.getEnd().isAfter(LocalDate.now().atTime(Preferences.getTime("StartOfDay")))) {
                logEntryList.getEntries().add(logEntry);
                goals.acceptEntry(logEntry);
            }
        });

        final BorderPane borderPane = (BorderPane) getRoot();
        borderPane.setTop(getMenuBar());
        borderPane.setCenter(logEntryList);
        BorderPane.setMargin(logEntryList, new Insets(10));
        borderPane.setBottom(currentEntryDisplay);
        BorderPane.setMargin(currentEntryDisplay, new Insets(0, 10, 10, 10));
        if (Preferences.getBoolean("UseGoals")) borderPane.setRight(goals);
        BorderPane.setMargin(goals, new Insets(10, 10, 10, 0));
    }

    private MenuBar getMenuBar() {
        return new MenuBar(
                new Menu("LookAt", null,
                        lookAtYesterdayMenuItem(),
                        lookAtCertainDayMenuItem(),
                        new SeparatorMenuItem(),
                        lootAtLast4DaysMenuItem(),
                        lookAtCurrentWeekMenuItem(),
                        lookAtLastWeekMenuItem(),
                        lookAtCertainTimeSpanMenuItem()
                ),
                new Menu("Report", null,
                        reportMenuItem("Today", Report::today),
                        reportMenuItem("Yesterday", Report::yesterday),
                        certainDayReportMenuItem(),
                        new SeparatorMenuItem(),
                        reportMenuItem("Last 7 days", Report::last7days),
                        reportMenuItem("Current Week", Report::currentWeek),
                        reportMenuItem("Previous Week", Report::previousWeek),
                        certainTimeSpanReportMenuItem()
                ),
                new Menu("Tools", null,
                        editAllMenuItem(),
                        editActivitiesMenuItem(),
                        reloadGoalsMenuItem(),
                        restartMenuItem(),
                        loginMenuItem(),
                        preferencesMenuItem()
                )
        );
    }

    private MenuItem lookAtYesterdayMenuItem() {
        return getMenuItem("Yesterday",
                () -> new LookAtDayDialog(LocalDate.now().minus(1, ChronoUnit.DAYS)).show());
    }

    private MenuItem lookAtCertainDayMenuItem() {
        return getMenuItem("Day ...",
                () -> DatePickerDialog.before(LocalDate.now()).showAndWait()
                        .ifPresent(date -> new LookAtDayDialog(date).show()));
    }

    private MenuItem lootAtLast4DaysMenuItem() {
        return getMenuItem("Last 4 days", () -> new LookAtDaysDialog(LocalDate.now().minusDays(3), 4).show());
    }

    private MenuItem lookAtCurrentWeekMenuItem() {
        return getMenuItem("Current Week",
                () -> new LookAtDaysDialog(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), 7)
                        .show());
    }

    private MenuItem lookAtLastWeekMenuItem() {
        return getMenuItem("Last Week",
                () -> new LookAtDaysDialog(
                        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minus(7, ChronoUnit.DAYS), 7)
                        .show());
    }

    private MenuItem lookAtCertainTimeSpanMenuItem() {
        return getMenuItem("Days From ... To ...",
                () -> DatePickerDialog.any("From").showAndWait()
                        .ifPresent(from -> DatePickerDialog.after("To", from).showAndWait()
                                .ifPresent(to -> new LookAtDaysDialog(from, to).show())));
    }

    private MenuItem reportMenuItem(final String label, final Supplier<Report> report) {
        return getMenuItem(label, () -> report.get().show());
    }

    private MenuItem certainDayReportMenuItem() {
        return getMenuItem("For Day ...",
                () -> DatePickerDialog.before(LocalDate.now().minus(1, ChronoUnit.DAYS))
                        .showAndWait().map(Report::on).ifPresent(Dialog::show));
    }

    private MenuItem certainTimeSpanReportMenuItem() {
        return getMenuItem("From ... To ...",
                () -> DatePickerDialog.before("From", LocalDate.now())
                        .showAndWait().ifPresent(fromDate -> DatePickerDialog.between("To", fromDate, LocalDate.now().plus(1, ChronoUnit.DAYS))
                                .showAndWait().ifPresent(toDate -> Report.between(fromDate, toDate).show())));
    }

    private MenuItem editAllMenuItem() {
        return getMenuItem("Edit All Entries",
                () -> LogEntry.FACTORY.getAll().forEach(logEntry -> new LogEntryDialog(logEntry).showAndWait()));
    }

    private MenuItem editActivitiesMenuItem() {
        return getMenuItem("Edit Activities", () -> new AllActivitiesDialog().show());
    }

    private MenuItem reloadGoalsMenuItem() {
        return getMenuItem("Reload Goals", goals::reload);
    }

    private MenuItem restartMenuItem() {
        return getMenuItem("Restart", () -> App.restart(true));
    }

    private MenuItem loginMenuItem() {
        return getMenuItem("Change Login Information", () -> App.restart(false));
    }

    private MenuItem preferencesMenuItem() {
        return getMenuItem("Preferences",
                () -> new PreferencesDialog().showAndWait()
                        .filter(buttonType -> buttonType.equals(PreferencesDialog.OK_BUTTON))
                        .ifPresent(ok -> App.restart(true)));
    }

    private MenuItem getMenuItem(String label, final Runnable eventHandler) {
        final MenuItem menuItem = new MenuItem(label);
        menuItem.setOnAction(event -> eventHandler.run());
        return menuItem;
    }

}
