package de.julianpadawan.timelog.view;

import de.julianpadawan.common.customFX.DatePickerDialog;
import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.view.edit.LogEntryDialog;
import de.julianpadawan.timelog.view.edit.PreferencesDialog;
import de.julianpadawan.timelog.view.insight.LookAtDayDialog;
import de.julianpadawan.timelog.view.insight.LookAtDaysDialog;
import de.julianpadawan.timelog.view.insight.Report;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.function.Supplier;

public class MainScene extends Scene {

    private final LogEntryList logEntryList;

    public MainScene() {
        super(new BorderPane(), 350, Region.USE_COMPUTED_SIZE);

        logEntryList = new LogEntryList();
        logEntryList.getEntries().addAll(LogEntry.FACTORY.getAllFinishedOn(LocalDate.now()));

        final CurrentEntryDisplay currentEntryDisplay = new CurrentEntryDisplay(logEntry -> {
            if (logEntry.getEnd().toLocalDate().equals(LocalDate.now())) logEntryList.getEntries().add(logEntry);
        });
        HBox.setHgrow(currentEntryDisplay, Priority.ALWAYS);

        final BorderPane borderPane = (BorderPane) getRoot();
        borderPane.setTop(getMenuBar());
        borderPane.setCenter(logEntryList);
        BorderPane.setMargin(logEntryList, new Insets(10));
        borderPane.setBottom(currentEntryDisplay);
        BorderPane.setMargin(currentEntryDisplay, new Insets(0, 10, 10, 10));
    }

    private MenuBar getMenuBar() {
        return new MenuBar(
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
                new Menu("LookAt", null,
                        lookAtYesterdayMenuItem(),
                        lookAtCertainDayMenuItem(),
                        new SeparatorMenuItem(),
                        lookAtCurrentWeekMenuItem(),
                        lookAtLastWeekMenuItem(),
                        lookAtCertainTimeSpanMenuItem()
                ),
                new Menu("Tools", null,
                        editAllMenuItem(),
                        reloadMenuItem(),
                        refreshCanvasMenuItem(),
                        preferencesMenuItem()
                )
        );
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

    private MenuItem lookAtYesterdayMenuItem() {
        return getMenuItem("Look At Yesterday",
                () -> new LookAtDayDialog(LocalDate.now().minus(1, ChronoUnit.DAYS)).show());
    }

    private MenuItem lookAtCertainDayMenuItem() {
        return getMenuItem("Look At Day ...",
                () -> DatePickerDialog.before(LocalDate.now()).showAndWait()
                        .ifPresent(date -> new LookAtDayDialog(date).show()));
    }

    private MenuItem lookAtCurrentWeekMenuItem() {
        return getMenuItem("Look At Current Week",
                () -> new LookAtDaysDialog(LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)), 7)
                        .show());
    }

    private MenuItem lookAtLastWeekMenuItem() {
        return getMenuItem("Look At Last Week",
                () -> new LookAtDaysDialog(
                        LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minus(7, ChronoUnit.DAYS), 7)
                        .show());
    }

    private MenuItem lookAtCertainTimeSpanMenuItem() {
        return getMenuItem("Look At Days From ... To ...",
                () -> DatePickerDialog.any("From").showAndWait()
                        .ifPresent(from -> DatePickerDialog.after("To", from).showAndWait()
                                .ifPresent(to -> new LookAtDaysDialog(from, to).show())));
    }

    private MenuItem editAllMenuItem() {
        return getMenuItem("Edit All Entries",
                () -> LogEntry.FACTORY.getAll().forEach(logEntry -> new LogEntryDialog(logEntry).showAndWait()));
    }

    private MenuItem reloadMenuItem() {
        return getMenuItem("Reload List", this::reloadList);
    }

    private MenuItem refreshCanvasMenuItem() {
        return getMenuItem("Redraw Minute Marks", logEntryList::refreshCanvas);
    }

    private MenuItem preferencesMenuItem() {
        return getMenuItem("Preferences",
                () -> new PreferencesDialog().showAndWait()
                        .filter(buttonType -> buttonType.equals(ButtonType.OK))
                        .ifPresent(ok -> {
                            reloadList();
                            logEntryList.refreshCanvas();
                        }));
    }

    private MenuItem getMenuItem(String label, final Runnable eventHandler) {
        final MenuItem menuItem = new MenuItem(label);
        menuItem.setOnAction(event -> eventHandler.run());
        return menuItem;
    }

    private void reloadList() {
        logEntryList.getEntries().clear();
        logEntryList.getEntries().addAll(LogEntry.FACTORY.getAllFinishedOn(LocalDate.now()));
    }

}
