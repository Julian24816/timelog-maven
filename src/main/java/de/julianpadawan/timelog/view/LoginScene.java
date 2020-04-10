package de.julianpadawan.timelog.view;

import de.julianpadawan.common.customFX.ErrorAlert;
import de.julianpadawan.common.customFX.GridPane2C;
import de.julianpadawan.common.db.Database;
import de.julianpadawan.timelog.preferences.PreferenceMap;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;

import java.io.IOException;

public class LoginScene extends Scene {

    private final PreferenceMap preferenceMap;
    private final ChoiceBox<DriverChoice> driver;
    private final TextField database;
    private final TextField username;
    private final PasswordField password;
    private final Runnable onLogin;

    public LoginScene(Runnable onLogin, boolean skipAllowed) {
        super(new GridPane2C(10));
        this.onLogin = onLogin;
        GridPane2C gridPane2C = (GridPane2C) getRoot();
        gridPane2C.setPadding(new Insets(10));
        preferenceMap = new PreferenceMap();

        driver = gridPane2C.addRow("Driver", new ChoiceBox<>());
        driver.getItems().addAll(DriverChoice.values());
        preferenceMap.mapTo(driver, "DatabaseDriver", DriverChoice::valueOf);

        database = gridPane2C.addRow("Database", new TextField());
        database.setPrefColumnCount(20);
        preferenceMap.mapTo(database, "DatabaseURL");

        username = gridPane2C.addRow("Username", new TextField());
        preferenceMap.mapTo(username, "DatabaseUsername");

        password = gridPane2C.addRow("Password", new PasswordField());
        preferenceMap.mapTo(password, "DatabasePassword");

        CheckBox automaticLogin = gridPane2C.addRow("", new CheckBox("login automatically"));
        preferenceMap.mapTo(automaticLogin, "AutomaticLogin");

        Button login = gridPane2C.addRow("", new Button("Login"));
        login.setDefaultButton(true);
        login.setOnAction(this::login);

        if (skipAllowed && automaticLogin.isSelected()) Platform.runLater(login::fire);
    }

    private void login(ActionEvent event) {
        Database.init(driver.getValue().name + ":" + database.getText(), username.getText(), password.getText());
        try {
            Database.execFile("db/timelog.sql");
            preferenceMap.dumpPreferences();
            onLogin.run();
        } catch (IOException e) {
            ErrorAlert.show("Datebase Init", e);
        }
    }

    private enum DriverChoice {
        SQLite("jdbc:sqlite");

        private final String name;

        DriverChoice(String name) {
            this.name = name;
        }
    }
}
