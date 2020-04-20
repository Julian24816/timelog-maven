package de.julianpadawan.timelog.view;

import de.julianpadawan.common.customFX.Util;
import de.julianpadawan.timelog.model.Activity;
import de.julianpadawan.timelog.view.edit.ActivityDialog;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.util.Optional;

public class ChooseActivityDialog extends Dialog<Activity> {
    public static final ButtonType NEW_BUTTON = new ButtonType("+", ButtonBar.ButtonData.OTHER);
    public static final ButtonType USE_BUTTON = new ButtonType("Use", ButtonBar.ButtonData.OK_DONE);

    public ChooseActivityDialog(Activity activity) {
        super();
        setTitle("Choose Activity");
        setHeaderText(activity.getName());

        getDialogPane().setContent(getChildrenButtons(activity));
        getDialogPane().getButtonTypes().addAll(NEW_BUTTON, USE_BUTTON, ButtonType.CANCEL);
        setResultConverter(buttonType -> {
            if (buttonType.equals(NEW_BUTTON)) return getNewChild(activity);
            return buttonType.equals(ButtonType.CANCEL) ? null : activity;
        });
    }

    private Node getChildrenButtons(Activity activity) {
        final VBox vBox = new VBox(15);
        for (Activity child : Activity.FACTORY.getAllChildren(activity))
            vBox.getChildren().add(getButtonPane(child, 5));
        if (vBox.getChildren().size() == 0) vBox.getChildren().add(new Label("no child activities"));
        return vBox;
    }

    private Activity getNewChild(Activity activity) {
        return ActivityDialog.forParent(activity).showAndWait().orElse(null);
    }

    private Pane getButtonPane(Activity activity, final int gap) {
        final Pane buttons = new FlowPane(gap, gap);
        buttons.getChildren().add(getButton(activity));
        buttons.getChildren().add(new Text(":"));
        for (Activity child : Activity.FACTORY.getAllChildren(activity))
            buttons.getChildren().add(getButton(child));
        buttons.getChildren().add(Util.button("+", () -> {
            setResult(getNewChild(activity));
            close();
        }));
        buttons.setMaxWidth(300);
        return buttons;
    }

    private Button getButton(Activity child) {
        return Util.button(child.getName(), child.getColor(), () -> {
            hide();
            setResult(child);
            close();
        });
    }

    public static Activity choose(Activity root) {
        final Optional<Activity> optionalActivity = new ChooseActivityDialog(root).showAndWait();
        if (optionalActivity.isEmpty()) return null;
        final Activity activity = optionalActivity.get();
        if (activity.equals(root)) return activity;
        else return choose(activity);
    }

}
