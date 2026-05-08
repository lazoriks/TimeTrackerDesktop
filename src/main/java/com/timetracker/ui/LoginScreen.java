package com.timetracker.ui;

import com.timetracker.App;
import com.timetracker.db.DatabaseManager;
import com.timetracker.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class LoginScreen {

    private final VBox root;

    public LoginScreen() {
        root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50, 60, 50, 60));
        root.setStyle("-fx-background-color: #ECEFF1;");

        Label title = new Label("Time Tracker");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setStyle("-fx-text-fill: #3F51B5;");

        Label subtitle = new Label("Please sign in to continue");
        subtitle.setStyle("-fx-text-fill: #78909C; -fx-font-size: 13px;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);
        form.setAlignment(Pos.CENTER);

        Label userLabel = new Label("User:");
        userLabel.setStyle("-fx-font-weight: bold;");
        TextField userField = new TextField();
        userField.setPromptText("Enter username");
        userField.setPrefWidth(220);
        styleField(userField);

        Label passLabel = new Label("Password:");
        passLabel.setStyle("-fx-font-weight: bold;");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");
        passField.setPrefWidth(220);
        styleField(passField);

        form.add(userLabel, 0, 0);
        form.add(userField, 1, 0);
        form.add(passLabel, 0, 1);
        form.add(passField, 1, 1);

        Button loginBtn = new Button("Log In");
        loginBtn.setPrefWidth(220);
        loginBtn.setStyle(
            "-fx-background-color: #3F51B5; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6;");
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(
            "-fx-background-color: #303F9F; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6;"));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(
            "-fx-background-color: #3F51B5; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6;"));

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #E53935; -fx-font-size: 12px;");
        errorLabel.setWrapText(true);

        loginBtn.setOnAction(e -> doLogin(userField, passField, errorLabel));
        userField.setOnAction(e -> passField.requestFocus());
        passField.setOnAction(e -> loginBtn.fire());

        Hyperlink registerLink = new Hyperlink("Don't have an account? Register here");
        registerLink.setStyle("-fx-text-fill: #3F51B5; -fx-font-size: 12px;");
        registerLink.setOnAction(e -> App.getInstance().showRegistration());

        root.getChildren().addAll(title, subtitle, form, loginBtn, errorLabel, registerLink);
    }

    private void doLogin(TextField userField, PasswordField passField, Label errorLabel) {
        String username = userField.getText().trim();
        String password = passField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password.");
            return;
        }

        User user = DatabaseManager.getInstance().authenticate(username, password);
        if (user == null) {
            errorLabel.setText("Invalid username or password.");
            passField.clear();
        } else if (user.isSuperUser()) {
            App.getInstance().showUserManagement(username);
        } else {
            App.getInstance().showTimeTracking(username);
        }
    }

    private void styleField(TextField field) {
        field.setStyle(
            "-fx-background-color: white; -fx-border-color: #B0BEC5;" +
            "-fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 10;");
    }

    public VBox getRoot() {
        return root;
    }
}
