package de.julianpadawan.timelog.model;

import de.julianpadawan.common.db.ColumnType;
import de.julianpadawan.common.db.ModelFactory;
import de.julianpadawan.common.db.ModelObject;
import de.julianpadawan.common.db.ModelTableDefinition;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;

import java.util.Objects;

public final class Person extends ModelObject<Person> {
    public static final PersonFactory FACTORY = new PersonFactory();

    private final StringProperty name = new SimpleStringProperty(this, "name");

    private Person(int id, String name) {
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
        return "Person{" +
                "id=" + getId() +
                ", name=" + name.get() +
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
                            view.getString("name")
                    ),
                    new ModelTableDefinition<Person>("person")
                            .withColumn("name", ColumnType.STRING, Person::getName)
            );
        }
    }
}
