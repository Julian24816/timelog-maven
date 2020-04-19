package de.julianpadawan.timelog.view;

import de.julianpadawan.common.customFX.Util;
import de.julianpadawan.timelog.model.Activity;
import de.julianpadawan.timelog.view.edit.ActivityDialog;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;

import java.util.Optional;

public class ChooseActivityDialog extends Dialog<Activity> {
    public static final ButtonType NEW_BUTTON = new ButtonType("New", ButtonBar.ButtonData.OTHER);
    public static final ButtonType USE_BUTTON = new ButtonType("Use", ButtonBar.ButtonData.OK_DONE);

    public ChooseActivityDialog(Activity activity) {
        super();
        setTitle("Choose Activity");
        setHeaderText(activity.getName());

        getDialogPane().setContent(getChildrenButtons(activity));
        getDialogPane().getButtonTypes().addAll(NEW_BUTTON, USE_BUTTON, ButtonType.CANCEL);
        setResultConverter(buttonType -> {
            if (buttonType.equals(NEW_BUTTON)) {
                return ActivityDialog.forParent(activity).showAndWait().orElse(null);
            }
            return buttonType.equals(ButtonType.CANCEL) ? null : activity;
        });
    }

    private Node getChildrenButtons(Activity activity) {
        final Pane children = new FlowPane(10, 10);
        for (Activity child : Activity.FACTORY.getAllChildren(activity))
            children.getChildren().add(Util.button(child.getName(), child.getColor(), () -> {
                hide();
                setResult(child);
                close();
            }));
        if (children.getChildren().size() == 0) children.getChildren().add(new Label("no child activities"));
        children.setPrefWidth(200);
        return children;
    }

    public static Activity choose(Activity root) {
        final Optional<Activity> optionalActivity = new ChooseActivityDialog(root).showAndWait();
        if (optionalActivity.isEmpty()) return null;
        final Activity activity = optionalActivity.get();
        if (activity.equals(root)) return activity;
        else return choose(activity);
    }

}
