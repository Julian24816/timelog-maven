package de.julianpadawan.timelog.preferences;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

public final class Preferences {
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    public static final String FILE_NAME = "preferences.properties";
    private static final Properties PROPERTIES = new Properties();

    private Preferences() {
    }

    public static void loadPropertiesFile(String fileName) throws IOException {
        try (final InputStream inputStream = Files.newInputStream(Paths.get(fileName), StandardOpenOption.CREATE)) {
            PROPERTIES.load(inputStream);
        } catch (NoSuchFileException ignored) {
        }
    }

    public static void savePropertiesFile(String fileName) throws IOException {
        try (final OutputStream outputStream = Files.newOutputStream(Paths.get(fileName), StandardOpenOption.CREATE)) {
            PROPERTIES.store(outputStream, "");
        }
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key);
    }

    public static boolean getBoolean(String key) {
        return Boolean.parseBoolean(PROPERTIES.getProperty(key));
    }

    public static int getInt(String key) {
        return Integer.parseInt(PROPERTIES.getProperty(key));
    }

    public static double getDouble(String key) {
        return Double.parseDouble(PROPERTIES.getProperty(key));
    }

    public static LocalTime getTime(String key) {
        return LocalTime.parse(get(key), TIME_FORMATTER);
    }

    public static void set(String key, String value) {
        PROPERTIES.setProperty(key, value);
    }

    public static void set(String key, boolean value) {
        PROPERTIES.setProperty(key, String.valueOf(value));
    }

    public static void set(String key, int value) {
        PROPERTIES.setProperty(key, String.valueOf(value));
    }

    public static void set(String key, double value) {
        PROPERTIES.setProperty(key, String.valueOf(value));
    }

    public static void set(String key, LocalTime time) {
        PROPERTIES.setProperty(key, TIME_FORMATTER.format(time));
    }
}
