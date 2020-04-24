package de.julianpadawan.timelog.view;

import de.julianpadawan.common.customFX.DatePickerDialog;
import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.preferences.Preferences;
import de.julianpadawan.timelog.view.edit.AllActivitiesDialog;
import de.julianpadawan.timelog.view.edit.LogEntryDialog;
import de.julianpadawan.timelog.view.edit.PreferencesDialog;
import de.julianpadawan.timelog.view.insight.ListAll;
import de.julianpadawan.timelog.view.insight.LookAtDayDialog;
import de.julianpadawan.timelog.view.insight.LookAtDaysDialog;
import de.julianpadawan.timelog.view.insight.Report;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MainScene extends Scene {

    private final LogEntryList logEntryList = new LogEntryList();
    private final GoalsList goals = new GoalsList();

    public MainScene() {
        super(new BorderPane());

        logEntryList.getEntries().addAll(LogEntry.FACTORY.getAllFinishedOnDateOf(LocalDateTime.now()).stream().sorted().collect(Collectors.toList()));

        LocalDate today = LogEntry.today();
        final CurrentEntryDisplay currentEntryDisplay = new CurrentEntryDisplay(logEntry -> {
            final LocalDate date = LogEntry.getDate(logEntry.getEnd());
            if (date.isAfter(today)) App.restart(true);
            else if (date.equals(today)) {
                logEntryList.getEntries().add(logEntry);
                FXCollections.sort(logEntryList.getEntries());
                goals.acceptEntry(logEntry);
            }
        });

        final BorderPane borderPane = (BorderPane) getRoot();
        borderPane.setTop(new VBox(getMenuBar(), currentEntryDisplay));
        VBox.setMargin(currentEntryDisplay, new Insets(10, 10, 0, 10));
        borderPane.setCenter(logEntryList);
        BorderPane.setMargin(logEntryList, new Insets(10));
        if (Preferences.getBoolean("UseGoals")) borderPane.setBottom(goals);
        BorderPane.setMargin(goals, new Insets(0, 10, 10, 10));
    }

    private MenuBar getMenuBar() {
        return new MenuBar(
                getMenu("LookAt",
                        date -> new LookAtDayDialog(date).show(),
                        (from, days) -> new LookAtDaysDialog(from, days).show()),
                getMenu("Report",
                        date -> Report.on(date).show(),
                        (from, days) -> Report.between(from, from.plusDays(days)).show()),
                getMenu("List All",
                        date -> ListAll.on(date).ifPresent(Dialog::show),
                        (from, days) -> ListAll.between(from, from.plusDays(days)).ifPresent(Dialog::show)),
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

    private Menu getMenu(final String label, final Consumer<LocalDate> dayAction, final BiConsumer<LocalDate, Integer> timeSpanAction) {
        return new Menu(label, null,
                getMenuItem("Today", () -> dayAction.accept(LogEntry.today())),
                getMenuItem("Yesterday", () -> dayAction.accept(LogEntry.today().minusDays(1))),
                getMenuItem("Day ...", () -> chooseDay(dayAction)),
                new SeparatorMenuItem(),
                getMenuItem("Last 4 days", () -> timeSpanAction.accept(LogEntry.today().minusDays(3), 4)),
                getMenuItem("Current Week", () -> timeSpanAction.accept(startOfWeek(), 7)),
                getMenuItem("Last Week", () -> timeSpanAction.accept(startOfWeek().minus(7, ChronoUnit.DAYS), 7)),
                new SeparatorMenuItem(),
                getMenuItem("Current Month", () -> monthAction(timeSpanAction, 0)),
                getMenuItem("Last Month", () -> monthAction(timeSpanAction, -1)),
                new SeparatorMenuItem(),
                getMenuItem("Days From ... To ...",
                        () -> chooseTimeSpan((from, to) -> timeSpanAction.accept(from,
                                (int) from.until(to, ChronoUnit.DAYS))))
        );
    }

    private void chooseDay(final Consumer<LocalDate> action) {
        DatePickerDialog.before(LogEntry.today()).showAndWait().ifPresent(action);
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
                () -> new PreferencesDialog().showAndWait().filter(restart -> restart)
                        .ifPresent(ok -> App.restart(true)));
    }

    private MenuItem getMenuItem(String label, final Runnable eventHandler) {
        final MenuItem menuItem = new MenuItem(label);
        menuItem.setOnAction(event -> eventHandler.run());
        return menuItem;
    }

    private LocalDate startOfWeek() {
        return LogEntry.today().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private void monthAction(BiConsumer<LocalDate, Integer> timeSpanAction, int monthOffset) {
        final LocalDate begin = LogEntry.today().with(TemporalAdjusters.firstDayOfMonth()).plusMonths(monthOffset);
        final int days = YearMonth.from(begin).lengthOfMonth();
        timeSpanAction.accept(begin, days);
    }

    private void chooseTimeSpan(final BiConsumer<LocalDate, LocalDate> action) {
        DatePickerDialog.before("From", LogEntry.today()).showAndWait()
                .ifPresent(from -> DatePickerDialog.between("To", from, LogEntry.today().plusDays(1)).showAndWait()
                        .ifPresent(to -> action.accept(from, to)));
    }
}
