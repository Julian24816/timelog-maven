package de.julianpadawan.timelog.view;

import de.julianpadawan.common.customFX.ErrorAlert;
import de.julianpadawan.common.db.Database;
import de.julianpadawan.timelog.model.Activity;
import de.julianpadawan.timelog.preferences.Preferences;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;

public class App extends Application {
    private static Stage stage;

    public static void main(String[] args) {
        launch(args);
    }

    static void restart(boolean skipLogin) {
        Activity.FACTORY.clearCache();
        stage.setScene(new LoginScene(() -> stage.setScene(new MainScene()), skipLogin));
    }

    public static String formatDuration(Duration duration, final boolean allowShortening) {
        if (duration.equals(Duration.ZERO)) return "";
        final long minutes = Math.floorDiv(duration.getSeconds(), 60) % 60;
        final long hours = Math.floorDiv(duration.getSeconds(), 3600);
        if (minutes == 0 && hours == 0) return "";
        if (allowShortening && minutes == 0) return String.format("%dh", hours);
        return hours == 0 ? String.format("%dm", minutes) : String.format("%dh %02dm", hours, minutes);
    }

    @Override
    public void start(Stage stage) {
        App.stage = stage;
        stage.setTitle("TimeLog");
        stage.setScene(new LoginScene(() -> stage.setScene(new MainScene()), true));
        stage.show();
    }

    @Override
    public void init() throws IOException {
        setDefaultPreferences();
        Preferences.loadPropertiesFile(Preferences.FILE_NAME);
        Database.setErrorHandler(ErrorAlert::show);
    }

    private void setDefaultPreferences() {
        Preferences.set("DatabaseDriver", "SQLite");
        Preferences.set("DatabaseURL", ":memory:");
        Preferences.set("DatabaseUsername", "");
        Preferences.set("DatabasePassword", "");
        Preferences.set("AutomaticLogin", false);

        Preferences.set("MinuteToPixelScale", 1.2f);

        Preferences.set("MinuteMarkEvery", 30);
        Preferences.set("MinuteMarkWidth", 5);
        Preferences.set("MinuteMarkColor", "BLACK");

        Preferences.set("StartOfDay", LocalTime.of(5, 50));
        Preferences.set("SleepID", -1);
        Preferences.set("SleepLineHeight", 20);

        Preferences.set("UseActivityChooser", true);
        Preferences.set("UseGoals", false);
        Preferences.set("ShowPointsRelative", true);
    }

    @Override
    public void stop() throws IOException {
        Preferences.savePropertiesFile(Preferences.FILE_NAME);
    }
}
