package de.julianpadawan.timelog.view.edit;

import de.julianpadawan.common.customFX.CustomBindings;
import de.julianpadawan.common.customFX.Util;
import de.julianpadawan.timelog.model.Activity;
import de.julianpadawan.timelog.preferences.Preferences;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Alert;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Comparator;
import java.util.function.Function;

public final class AllActivitiesDialog extends Alert {
    private final TableView<Activity> table = new TableView<>();

    public AllActivitiesDialog() {
        super(AlertType.INFORMATION);
        setTitle("All Activities");
        setHeaderText("Edit All Activities");
        setResizable(true);

        addColumn("ID", Activity::idProperty, 30, false);
        addColumn("name", Activity::displayNameProperty, 200, true);
        if (Preferences.getBoolean("UseGoals")) addColumn("points", Activity::pointsPerMinuteProperty, 50, false);

        table.getItems().addAll(Activity.FACTORY.getAll());
        table.getItems().sort(Comparator.naturalOrder());
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setSelectionModel(null);
        getDialogPane().setContent(new VBox(10, table, Util.button("New", () ->
                new ActivityDialog().showAndWait().ifPresent(activity -> {
                    table.getItems().add(activity);
                    table.getItems().sort(Comparator.naturalOrder());
                })
        )));
        VBox.setVgrow(table, Priority.ALWAYS);
    }

    private <T> void addColumn(final String name, Function<Activity, ObservableValue<T>> property,
                               final int minWidth, final boolean expand) {
        final TableColumn<Activity, T> column = new TableColumn<>(name);
        column.setMinWidth(minWidth);
        if (expand) column.setMaxWidth(Double.MAX_VALUE);
        table.getColumns().add(column);

        column.setCellValueFactory(activityRow -> property.apply(activityRow.getValue()));
        column.setCellFactory(this::cellFactory);
    }

    private <T> TableCell<Activity, T> cellFactory(TableColumn<Activity, T> column) {
        final TableCell<Activity, T> tableCell = new TableCell<>() {
            final Background defaultBackground = getBackground();

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    backgroundProperty().unbind();
                    setBackground(defaultBackground);
                } else {
                    final Activity activity = table.getItems().get(getIndex());
                    backgroundProperty().bind(CustomBindings.apply(activity.colorProperty(),
                            color -> new Background(new BackgroundFill(Color.valueOf(color), null, null))));
                    setText(item == null ? "" : item.toString());
                }
            }
        };
        tableCell.setOnMouseClicked(mouseEvent -> {
            if (!mouseEvent.getButton().equals(MouseButton.PRIMARY) || mouseEvent.getClickCount() != 2) return;
            final int index = tableCell.getIndex();
            if (table.getItems().size() <= index) return;
            new ActivityDialog(table.getItems().get(index)).showAndWait();
            table.getItems().sort(Comparator.naturalOrder());
        });
        return tableCell;
    }
}
