package com.timetracker;

import com.timetracker.db.DatabaseManager;
import com.timetracker.ui.LoginScreen;
import com.timetracker.ui.RegistrationScreen;
import com.timetracker.ui.ReportScreen;
import com.timetracker.ui.TimeTrackingScreen;
import com.timetracker.ui.UserManagementScreen;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    private static App instance;
    private Stage primaryStage;
    private String currentUser;

    public static App getInstance() {
        return instance;
    }

    @Override
    public void start(Stage stage) {
        instance = this;
        primaryStage = stage;
        DatabaseManager.getInstance().initDatabase();

        stage.setTitle("Time Tracker");
        stage.setMinWidth(500);
        stage.setMinHeight(400);
        stage.setOnCloseRequest(e -> System.exit(0));

        showLogin();
        stage.show();
    }

    public void showLogin() {
        currentUser = null;
        LoginScreen screen = new LoginScreen();
        Scene scene = new Scene(screen.getRoot(), 420, 320);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Time Tracker — Login");
        primaryStage.setWidth(420);
        primaryStage.setHeight(320);
    }

    public void showUserManagement(String username) {
        if (username != null && !username.isEmpty()) {
            currentUser = username;
        }
        UserManagementScreen screen = new UserManagementScreen(currentUser != null ? currentUser : "");
        Scene scene = new Scene(screen.getRoot(), 860, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Time Tracker — User Management");
        primaryStage.setWidth(860);
        primaryStage.setHeight(600);
    }

    public void showReport() {
        ReportScreen screen = new ReportScreen();
        Scene scene = new Scene(screen.getRoot(), 960, 640);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Time Tracker — Report");
        primaryStage.setWidth(960);
        primaryStage.setHeight(640);
    }

    public void showRegistration() {
        RegistrationScreen screen = new RegistrationScreen();
        Scene scene = new Scene(screen.getRoot(), 460, 460);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Time Tracker — Register");
        primaryStage.setWidth(460);
        primaryStage.setHeight(460);
    }

    public void showTimeTracking(String username) {
        if (username != null && !username.isEmpty()) {
            currentUser = username;
        }
        TimeTrackingScreen screen = new TimeTrackingScreen(currentUser != null ? currentUser : "");
        Scene scene = new Scene(screen.getRoot(), 780, 580);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Time Tracker — " + currentUser);
        primaryStage.setWidth(780);
        primaryStage.setHeight(580);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public String getCurrentUser() {
        return currentUser;
    }
}
