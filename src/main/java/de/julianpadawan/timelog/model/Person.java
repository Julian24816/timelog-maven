package de.julianpadawan.timelog.model;

import de.julianpadawan.common.db.ColumnType;
import de.julianpadawan.common.db.ModelFactory;
import de.julianpadawan.common.db.ModelObject;
import de.julianpadawan.common.db.ModelTableDefinition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;

import java.util.Objects;

public final class Person extends ModelObject<Person> {
    public static final PersonFactory FACTORY = new PersonFactory();

    private final StringProperty name = new SimpleStringProperty(this, "name");
    private final DoubleProperty pointsFactor = new SimpleDoubleProperty(this, "pointsFactor");

    private Person(int id, String name, double pointsFactor) {
        super(id);
        this.name.setValue(Objects.requireNonNull(name));
        this.pointsFactor.setValue(pointsFactor);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.setValue(Objects.requireNonNull(value));
    }

    public DoubleProperty pointsFactorProperty() {
        return pointsFactor;
    }

    public double getPointsFactor() {
        return pointsFactor.get();
    }

    public void setPointsFactor(double value) {
        pointsFactor.setValue(value);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + getId() +
                ", name=" + name.get() +
                ", pointsFactor=" + pointsFactor.get() +
                '}';
    }

    @Override
    public int compareTo(Person o) {
        return getDisplayName().compareTo(o.getDisplayName());
    }

    @Override
    public ObservableStringValue displayNameProperty() {
        return name;
    }

    public static final class PersonFactory extends ModelFactory<Person> {
        private PersonFactory() {
            super(view -> new Person(
                            view.getInt("id"),
                            view.getString("name"),
                            view.getDouble("pointsFactor")
                    ),
                    new ModelTableDefinition<Person>("person")
                            .withColumn("name", ColumnType.STRING, Person::getName)
                            .withColumn("pointsFactor", ColumnType.DOUBLE, Person::getPointsFactor)
            );
        }
    }
}
