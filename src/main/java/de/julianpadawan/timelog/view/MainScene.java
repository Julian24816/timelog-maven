package de.julianpadawan.timelog.view;

import de.julianpadawan.common.customFX.DatePickerDialog;
import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.view.edit.LogEntryDialog;
import de.julianpadawan.timelog.view.edit.PreferencesDialog;
import de.julianpadawan.timelog.view.insight.LookAtDayDialog;
import de.julianpadawan.timelog.view.insight.Report;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
                new Menu("Tools", null,
                        lookAtMenuItem(),
                        editAllMenuItem(),
                        new SeparatorMenuItem(),
                        reloadMenuItem(),
                        refreshCanvasMenuItem(),
                        new SeparatorMenuItem(),
                        preferencesMenuItem()
                )
        );
    }

    private MenuItem reportMenuItem(final String label, final Supplier<Report> report) {
        final MenuItem menuItem = new MenuItem(label);
        menuItem.setOnAction(event -> report.get().show());
        return menuItem;
    }

    private MenuItem certainDayReportMenuItem() {
        final MenuItem menuItem = new MenuItem("For Day ...");
        menuItem.setOnAction(event -> DatePickerDialog.before(LocalDate.now().minus(1, ChronoUnit.DAYS))
                .showAndWait().map(Report::on).ifPresent(Dialog::show));
        return menuItem;
    }

    private MenuItem certainTimeSpanReportMenuItem() {
        final MenuItem menuItem = new MenuItem("From ... To ...");
        menuItem.setOnAction(event -> DatePickerDialog.before("From", LocalDate.now())
                .showAndWait().ifPresent(fromDate -> DatePickerDialog.between("To", fromDate, LocalDate.now().plus(1, ChronoUnit.DAYS))
                        .showAndWait().ifPresent(toDate -> Report.between(fromDate, toDate).show())));
        return menuItem;
    }

    private MenuItem lookAtMenuItem() {
        final MenuItem lookAt = new MenuItem("Look At Day ...");
        lookAt.setOnAction(event -> DatePickerDialog.before(LocalDate.now())
                .showAndWait().map(LookAtDayDialog::new).ifPresent(Dialog::show));
        return lookAt;
    }

    private MenuItem editAllMenuItem() {
        final MenuItem editAll = new MenuItem("Edit All Entries");
        editAll.setOnAction(event -> LogEntry.FACTORY.getAll().forEach(logEntry -> new LogEntryDialog(logEntry).showAndWait()));
        return editAll;
    }

    private MenuItem reloadMenuItem() {
        final MenuItem reload = new MenuItem("Reload List");
        reload.setOnAction(event -> {
            reloadList();
        });
        return reload;
    }

    private MenuItem refreshCanvasMenuItem() {
        final MenuItem refreshCanvas = new MenuItem("Redraw Minute Marks");
        refreshCanvas.setOnAction(event -> logEntryList.refreshCanvas());
        return refreshCanvas;
    }

    private MenuItem preferencesMenuItem() {
        final MenuItem preferences = new MenuItem("Preferences");
        preferences.setOnAction(event -> new PreferencesDialog().showAndWait()
                .filter(buttonType -> buttonType.equals(ButtonType.OK))
                .ifPresent(ok -> {
                    reloadList();
                    logEntryList.refreshCanvas();
                }));
        return preferences;
    }

    private void reloadList() {
        logEntryList.getEntries().clear();
        logEntryList.getEntries().addAll(LogEntry.FACTORY.getAllFinishedOn(LocalDate.now()));
    }

}
