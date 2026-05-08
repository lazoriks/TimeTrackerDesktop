package com.timetracker.ui;

import com.timetracker.App;
import com.timetracker.db.DatabaseManager;
import com.timetracker.model.User;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class RegistrationScreen {

    private final VBox root;

    public RegistrationScreen() {
        root = new VBox(16);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(40, 60, 40, 60));
        root.setStyle("-fx-background-color: #ECEFF1;");

        Label title = new Label("Create Account");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setStyle("-fx-text-fill: #3F51B5;");

        Label subtitle = new Label("Fill in the details below to register");
        subtitle.setStyle("-fx-text-fill: #78909C; -fx-font-size: 13px;");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(13);
        form.setAlignment(Pos.CENTER);

        TextField    usernameField  = field("Full name  (required)", 240);
        TextField    emailField     = field("Email address  (required)", 240);
        PasswordField passField     = new PasswordField();
        passField.setPromptText("Password  (required)");
        passField.setPrefWidth(240);
        styleField(passField);
        PasswordField confirmField  = new PasswordField();
        confirmField.setPromptText("Confirm password");
        confirmField.setPrefWidth(240);
        styleField(confirmField);

        form.add(bold("Username:"),        0, 0); form.add(usernameField, 1, 0);
        form.add(bold("Email:"),           0, 1); form.add(emailField,    1, 1);
        form.add(bold("Password:"),        0, 2); form.add(passField,     1, 2);
        form.add(bold("Confirm Password:"),0, 3); form.add(confirmField,  1, 3);

        Label feedbackLabel = new Label();
        feedbackLabel.setWrapText(true);
        feedbackLabel.setMaxWidth(340);
        feedbackLabel.setStyle("-fx-font-size: 12px;");

        Button registerBtn = new Button("Register");
        registerBtn.setPrefWidth(240);
        registerBtn.setStyle(
            "-fx-background-color: #3F51B5; -fx-text-fill: white;" +
            "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 6;");

        registerBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            String username = usernameField.getText().trim();
            String email    = emailField.getText().trim();
            String password = passField.getText();
            String confirm  = confirmField.getText();

            if (username.isEmpty()) {
                error(feedbackLabel, "Username is required."); ev.consume(); return;
            }
            if (username.length() > 150) {
                error(feedbackLabel, "Username must be 150 characters or fewer."); ev.consume(); return;
            }
            if (email.isEmpty()) {
                error(feedbackLabel, "Email is required."); ev.consume(); return;
            }
            if (email.length() > 150) {
                error(feedbackLabel, "Email must be 150 characters or fewer."); ev.consume(); return;
            }
            if (password.isEmpty()) {
                error(feedbackLabel, "Password is required."); ev.consume(); return;
            }
            if (password.length() > 50) {
                error(feedbackLabel, "Password must be 50 characters or fewer."); ev.consume(); return;
            }
            if (!password.equals(confirm)) {
                error(feedbackLabel, "Passwords do not match."); ev.consume(); return;
            }
        });

        registerBtn.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String email    = emailField.getText().trim();
            String password = passField.getText();

            User newUser = new User(username, password, false, email, "");
            boolean ok = DatabaseManager.getInstance().addUser(newUser);
            if (ok) {
                success(feedbackLabel, "Account created! Redirecting to login…");
                registerBtn.setDisable(true);
                // Small delay so the user reads the message, then go to login
                javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(
                    javafx.util.Duration.seconds(1.4));
                pause.setOnFinished(ev -> App.getInstance().showLogin());
                pause.play();
            } else {
                error(feedbackLabel, "Username already exists. Please choose a different one.");
            }
        });

        Hyperlink backLink = new Hyperlink("Already have an account? Sign in");
        backLink.setStyle("-fx-text-fill: #3F51B5; -fx-font-size: 12px;");
        backLink.setOnAction(e -> App.getInstance().showLogin());

        root.getChildren().addAll(title, subtitle, form, registerBtn, feedbackLabel, backLink);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private TextField field(String prompt, double width) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(width);
        styleField(tf);
        return tf;
    }

    private void styleField(TextField tf) {
        tf.setStyle(
            "-fx-background-color: white; -fx-border-color: #B0BEC5;" +
            "-fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 6 10;");
    }

    private Label bold(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold;");
        return l;
    }

    private void error(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setStyle("-fx-text-fill: #E53935; -fx-font-size: 12px;");
    }

    private void success(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setStyle("-fx-text-fill: #388E3C; -fx-font-size: 12px; -fx-font-weight: bold;");
    }

    public VBox getRoot() {
        return root;
    }
}
