package de.julianpadawan.timelog.model;

import de.julianpadawan.common.db.*;
import de.julianpadawan.timelog.preferences.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Objects;

public final class LogEntry extends ModelObject<LogEntry> {
    public static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public static final LogEntryFactory FACTORY = new LogEntryFactory();

    private final ObjectProperty<Activity> activity = new SimpleObjectProperty<>(this, "activity");
    private final StringProperty what = new SimpleStringProperty(this, "what");
    private final ObjectProperty<LocalDateTime> start = new SimpleObjectProperty<>(this, "start");
    private final ObjectProperty<LocalDateTime> end = new SimpleObjectProperty<>(this, "end");
    private final ObjectProperty<MeansOfTransport> meansOfTransport = new SimpleObjectProperty<>(this, "transport");

    private LogEntry(int id, Activity activity, String what, LocalDateTime start, LocalDateTime end, MeansOfTransport transport) {
        super(id);
        this.activity.setValue(Objects.requireNonNull(activity));
        this.what.setValue(Objects.requireNonNull(what));
        this.start.setValue(Objects.requireNonNull(start));
        this.end.setValue(end);
        this.meansOfTransport.setValue(transport);
    }

    public static LocalDate today() {
        return getDate(LocalDateTime.now());
    }

    public static LocalDate getDate(LocalDateTime time) {
        if (!time.toLocalTime().isBefore(Preferences.getTime("StartOfDay"))) return time.toLocalDate();
        return time.toLocalDate().minusDays(1);
    }

    public static LocalDateTime atStartOfDay(LocalDate beginInclusive) {
        return beginInclusive.atTime(Preferences.getTime("StartOfDay"));
    }

    public ObjectProperty<Activity> activityProperty() {
        return activity;
    }

    public Activity getActivity() {
        return activity.get();
    }

    public void setActivity(Activity value) {
        activity.setValue(value);
    }

    public StringProperty whatProperty() {
        return what;
    }

    public String getWhat() {
        return what.getValue();
    }

    public void setWhat(String value) {
        what.setValue(value);
    }

    public ObjectProperty<LocalDateTime> startProperty() {
        return start;
    }

    public LocalDateTime getStart() {
        return start.getValue();
    }

    public void setStart(LocalDateTime value) {
        start.setValue(value);
    }

    public ObjectProperty<LocalDateTime> endProperty() {
        return end;
    }

    public ObjectProperty<MeansOfTransport> meansOfTransportProperty() {
        return meansOfTransport;
    }

    public MeansOfTransport getMeansOfTransport() {
        return meansOfTransport.get();
    }

    public void setMeansOfTransport(MeansOfTransport value) {
        meansOfTransport.setValue(value);
    }

    @Override
    public ObservableStringValue displayNameProperty() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        String result = "LogEntry{" +
                "id=" + getId() +
                ", activity=" + activity.get() +
                ", details=" + what.get() +
                ", start=" + FORMATTER.format(start.get());
        if (end.get() == null) result += ", end=null";
        else result += ", end=" + FORMATTER.format(end.get());
        result += ", transport=" + meansOfTransport.get();
        result += '}';
        return result;
    }

    @Override
    public int compareTo(LogEntry o) {
        if (equals(o)) return 0;
        if (getEnd() == null && o.getEnd() == null) return Integer.compare(getId(), o.getId());
        if (getEnd() == null) return 1;
        if (o.getEnd() == null) return -1;
        return getEnd().compareTo(o.getEnd());
    }

    public LocalDateTime getEnd() {
        return end.getValue();
    }

    public void setEnd(LocalDateTime value) {
        end.setValue(value);
    }

    public static final class LogEntryFactory extends ModelFactory<LogEntry> {

        public static final ModelTableDefinition<LogEntry> TABLE_DEFINITION = new ModelTableDefinition<LogEntry>("log")
                .withColumn("activity", ColumnType.getForeignKeyColumn(Activity.class), LogEntry::getActivity)
                .withColumn("what", ColumnType.STRING, LogEntry::getWhat)
                .withColumn("start", ColumnType.TIMESTAMP, LogEntry::getStart)
                .withColumn("end", ColumnType.TIMESTAMP, LogEntry::getEnd)
                .withColumn("transport", ColumnType.getForeignKeyColumn(MeansOfTransport.class), LogEntry::getMeansOfTransport);

        private LogEntryFactory() {
            super(LogEntryFactory::getFromResultView, TABLE_DEFINITION);
        }

        public static LogEntry getFromResultView(ResultView view) throws SQLException {
            return new LogEntry(
                    view.getInt("id"),
                    Activity.FACTORY.getForId(view.getInt("activity")),
                    view.getString("what"),
                    view.getDateTime("start"),
                    view.getDateTime("end"),
                    view.getOptionalInt("transport").map(MeansOfTransport.FACTORY::getForId).orElse(null)
            );
        }

        public LogEntry getUnfinishedEntry() {
            return selectWhere(this::selectFirst, "end IS NULL", 0, null);
        }

        public Collection<LogEntry> getAllFinishedOnDateOf(LocalDateTime reference) {
            //TODO add second method with Date as parameter
            LocalDate date = getDate(reference);
            return getAllFinishedBetween(date, date.plus(1, ChronoUnit.DAYS));
        }

        public Collection<LogEntry> getAllFinishedBetween(final LocalDate beginInclusive, final LocalDate endExclusive) {
            //TODO remove this function
            return getAllFinishedBetween(atStartOfDay(beginInclusive), atStartOfDay(endExclusive));
        }

        public Collection<LogEntry> getAllFinishedBetween(final LocalDateTime from, final LocalDateTime to) {
            return selectWhere(this::selectAll, "end >= ? AND end < ?", 2, (preparedStatement, param) -> {
                if (param.equals(1)) preparedStatement.setTimestamp(param, Timestamp.valueOf(from));
                if (param.equals(2)) preparedStatement.setTimestamp(param, Timestamp.valueOf(to));
            });
        }

        public LogEntry getLast() {
            return Database.execute(definition.getBaseSelectSQL() + " ORDER BY end DESC", statement -> {
                try (final ResultSet resultSet = statement.executeQuery()) {
                    return this.selectFirst(resultSet);
                }
            }, null);
        }
    }
}
