package de.julianpadawan.timelog.view;

import de.julianpadawan.common.customFX.CustomBindings;
import de.julianpadawan.common.customFX.ErrorAlert;
import de.julianpadawan.common.customFX.Util;
import de.julianpadawan.timelog.insight.StreakCalculator;
import de.julianpadawan.timelog.model.Activity;
import de.julianpadawan.timelog.model.Goal;
import de.julianpadawan.timelog.model.LogEntry;
import de.julianpadawan.timelog.model.QualityTime;
import de.julianpadawan.timelog.view.edit.GoalDialog;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class GoalsList extends VBox {
    private final ObservableList<Goal> goals = FXCollections.observableArrayList();
    private final Text pointsText = new Text("0");
    private final Pane pane = new FlowPane(10, 10);
    private double points = 0;

    public GoalsList() {
        super(10);
        setAlignment(Pos.CENTER);

        final Button newButton = Util.button("New Goal", () -> new GoalDialog().showAndWait().ifPresent(goals::add));
        final HBox hBox = new HBox(10, pointsText, new Text("Points"), newButton);
        pointsText.setFont(new Font(20));
        hBox.setAlignment(Pos.BASELINE_CENTER);
        getChildren().add(hBox);

        pane.setPrefWidth(250);
        final ScrollPane scrollPane = new ScrollPane(pane);
        scrollPane.setFitToWidth(true);
        getChildren().add(scrollPane);
        setVgrow(scrollPane, Priority.ALWAYS);

        goals.addListener(this::onListChanged);
        goals.addAll(Goal.FACTORY.getAll());
        calculatePoints();
    }

    private void onListChanged(ListChangeListener.Change<? extends Goal> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                ErrorAlert.show("ListChange", new UnsupportedOperationException("entries list must not be permutated"));
            } else if (c.wasUpdated()) {
                ErrorAlert.show("ListChange", new UnsupportedOperationException("entries list must not be updated"));
            } else {
                for (Goal added : c.getAddedSubList()) add(added);
                for (Goal removed : c.getRemoved()) remove(removed);
                FXCollections.sort(pane.getChildren(), GoalsList::sort);
            }
        }
    }

    private void calculatePoints() {
        LogEntry.FACTORY.getAllFinishedOnDateOf(LocalDateTime.now()).forEach(this::addPointsOf);
    }

    private void add(Goal added) {
        pane.getChildren().add(new GoalLine(added, true));
    }

    private static int sort(Node o1, Node o2) {
        if (o1 instanceof GoalLine && o2 instanceof GoalLine)
            return ((GoalLine) o1).goal.compareTo(((GoalLine) o2).goal);
        if (o1 instanceof GoalLine) return -1;
        if (o2 instanceof GoalLine) return 1;
        return 0;
    }

    public ObservableList<Goal> getGoals() {
        return goals;
    }

    private void remove(Goal removed) {
        pane.getChildren().remove(new GoalLine(removed, false));
    }

    private void addPointsOf(LogEntry entry) {
        final long minutes = entry.getStart().until(entry.getEnd(), ChronoUnit.MINUTES);
        double points = minutes * entry.getActivity().getPointsPerMinute();
        for (QualityTime qualityTime : QualityTime.FACTORY.getAll(entry)) {
            points *= qualityTime.getSecond().getPointsFactor();
        }
        this.points += points;
        pointsText.setText(Long.toString(Math.round(this.points)));
    }

    public void reload() {
        getChildren().forEach(node -> {
            if (node instanceof GoalLine) ((GoalLine) node).load(null);
            calculatePoints();
        });
    }

    public void acceptEntry(LogEntry newEntry) {
        getChildren().forEach(node -> {
            if (node instanceof GoalLine) ((GoalLine) node).accept(newEntry);
        });
        addPointsOf(newEntry);
    }

    private static class GoalLine extends HBox {
        private final Goal goal;
        private final Text streak = new Text();
        private StreakCalculator calculator;

        private GoalLine(Goal goal, boolean load) {
            super();
            setPadding(new Insets(5));
            this.goal = goal;

            Text goalName = new Text();
            goalName.textProperty().bind(goal.displayNameProperty());

            Region spacer = new Region();
            spacer.setMinWidth(10);
            spacer.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(spacer, Priority.ALWAYS);

            getChildren().addAll(goalName, spacer, streak);

            backgroundProperty().bind(CustomBindings.apply(
                    CustomBindings.select(goal.activityProperty(), Activity::colorProperty),
                    color -> new Background(new BackgroundFill(Color.valueOf(color), null, null))));
            setOnMouseClicked(this::doubleClick);

            if (load) {
                goal.displayNameProperty().addListener(this::load);
                this.load(null);
            }
        }

        private void doubleClick(MouseEvent mouseEvent) {
            if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getClickCount() == 2)
                new GoalDialog(goal).show();
        }

        private void load(Observable ignored) {
            calculator = StreakCalculator.of(goal);
            calculator.init(LocalDate.now());
            streak.textProperty().bind(calculator.streakProperty());
        }

        private void accept(LogEntry newEntry) {
            if (calculator == null) load(null);
            calculator.acceptNew(newEntry);
        }

        @Override
        public boolean equals(Object o) {
            //noinspection ObjectComparison
            if (this == o) return true;
            if (o == null || !getClass().equals(o.getClass())) return false;
            GoalLine goalLine = (GoalLine) o;
            return goal.equals(goalLine.goal);
        }

        @Override
        public int hashCode() {
            return Objects.hash(goal);
        }
    }
}
