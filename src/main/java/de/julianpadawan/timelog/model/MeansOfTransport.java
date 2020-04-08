package de.julianpadawan.timelog.model;

import de.julianpadawan.common.db.ColumnType;
import de.julianpadawan.common.db.ModelFactory;
import de.julianpadawan.common.db.ModelObject;
import de.julianpadawan.common.db.ModelTableDefinition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;

import java.util.Objects;

public final class MeansOfTransport extends ModelObject<MeansOfTransport> {
    public static final MeansOfTransportFactory FACTORY = new MeansOfTransportFactory();

    private final StringProperty name = new SimpleStringProperty(this, "name");

    private MeansOfTransport(int id, String name) {
        super(id);
        this.name.setValue(Objects.requireNonNull(name));
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.setValue(value);
    }

    @Override
    public String toString() {
        return "MeansOfTransport{" +
                "id=" + getId() +
                ", name=" + name.get() +
                '}';
    }

    @Override
    public int compareTo(MeansOfTransport o) {
        return getDisplayName().compareTo(getDisplayName());
    }

    @Override
    public ObservableStringValue displayNameProperty() {
        return name;
    }

    public static final class MeansOfTransportFactory extends ModelFactory<MeansOfTransport> {
        private MeansOfTransportFactory() {
            super(view -> new MeansOfTransport(
                            view.getInt("id"),
                            view.getString("name")
                    ),
                    new ModelTableDefinition<MeansOfTransport>("meansOfTransport")
                            .withColumn("name", ColumnType.STRING, MeansOfTransport::getName)
            );
        }
    }
}
