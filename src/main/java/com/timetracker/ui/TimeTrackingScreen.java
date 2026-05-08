package com.timetracker.ui;

import com.timetracker.App;
import com.timetracker.db.DatabaseManager;
import com.timetracker.model.HoursRecord;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class TimeTrackingScreen {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BorderPane root;
    private final String username;

    private final ObservableList<HoursRecord> sessions = FXCollections.observableArrayList();
    private final TableView<HoursRecord> table = new TableView<>(sessions);

    private Button  checkInBtn;
    private Label   feedbackLabel;
    private Label   clockLabel;

    public TimeTrackingScreen(String username) {
        this.username = username;

        root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F5F5;");
        root.setTop(buildTopBar());
        root.setCenter(buildContent());

        loadSessions();
        startClock();
    }

    // ── Top bar ────────────────────────────────────────────────────────────────

    private HBox buildTopBar() {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(14, 16, 14, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #3F51B5;");

        Label title = new Label("My Dashboard");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label userLbl = new Label("Logged in as: " + username);
        userLbl.setStyle("-fx-text-fill: #C5CAE9; -fx-font-size: 12px;");

        Button logoutBtn = new Button("Log Out");
        logoutBtn.setStyle(btnCss("#E53935"));
        logoutBtn.setOnAction(e -> App.getInstance().showLogin());

        bar.getChildren().addAll(title, spacer, userLbl, logoutBtn);
        return bar;
    }

    // ── Main content ───────────────────────────────────────────────────────────

    private VBox buildContent() {
        VBox content = new VBox(12);
        content.setPadding(new Insets(16, 16, 16, 16));

        // ── Check-in action bar ────────────────────────────────────────────────
        checkInBtn = new Button("Check In");
        checkInBtn.setFont(Font.font("System", FontWeight.BOLD, 14));
        checkInBtn.setPrefWidth(160);
        checkInBtn.setOnAction(e -> doCheckIn());

        clockLabel = new Label();
        clockLabel.setStyle("-fx-text-fill: #546E7A; -fx-font-size: 13px;");

        HBox actionBar = new HBox(16, checkInBtn, clockLabel);
        actionBar.setAlignment(Pos.CENTER_LEFT);
        actionBar.setPadding(new Insets(8, 12, 8, 12));
        actionBar.setStyle(
            "-fx-background-color: white; -fx-background-radius: 6;" +
            "-fx-border-color: #CFD8DC; -fx-border-radius: 6;");

        // ── Feedback message ───────────────────────────────────────────────────
        feedbackLabel = new Label();
        feedbackLabel.setWrapText(true);
        feedbackLabel.setFont(Font.font("System", FontWeight.BOLD, 13));
        feedbackLabel.setVisible(false);
        feedbackLabel.setMaxWidth(Double.MAX_VALUE);
        feedbackLabel.setPadding(new Insets(8, 12, 8, 12));
        feedbackLabel.setStyle(
            "-fx-background-color: #E8F5E9; -fx-background-radius: 5;" +
            "-fx-text-fill: #388E3C;");

        // ── Sessions heading ───────────────────────────────────────────────────
        Label sessionsHeading = new Label("Work Sessions");
        sessionsHeading.setFont(Font.font("System", FontWeight.BOLD, 15));
        sessionsHeading.setStyle("-fx-text-fill: #37474F;");

        // ── Sessions table ─────────────────────────────────────────────────────
        buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        content.getChildren().addAll(actionBar, feedbackLabel, sessionsHeading, table);
        return content;
    }

    // ── Table construction ─────────────────────────────────────────────────────

    private void buildTable() {
        // Check In column
        TableColumn<HoursRecord, String> ciCol = new TableColumn<>("Check In");
        ciCol.setPrefWidth(170);
        ciCol.setCellValueFactory(cd -> new SimpleStringProperty(
            cd.getValue().getComeIn() != null ? cd.getValue().getComeIn().format(DT_FMT) : ""));

        // Check Out column
        TableColumn<HoursRecord, String> coCol = new TableColumn<>("Check Out");
        coCol.setPrefWidth(170);
        coCol.setCellValueFactory(cd -> {
            LocalDateTime co = cd.getValue().getComeOut();
            return new SimpleStringProperty(co != null ? co.format(DT_FMT) : "");
        });
        coCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); setStyle(""); return; }
                if (item == null || item.isEmpty()) {
                    setText("— active —");
                    setStyle("-fx-text-fill: #FB8C00; -fx-font-style: italic;");
                } else {
                    setText(item);
                    setStyle("");
                }
            }
        });

        // Duration column
        TableColumn<HoursRecord, String> durCol = new TableColumn<>("Duration");
        durCol.setPrefWidth(140);
        durCol.setCellValueFactory(cd -> {
            HoursRecord r = cd.getValue();
            if (r.getComeOut() == null) {
                return new SimpleStringProperty("● In progress");
            }
            long totalMins = (long)(r.getHour() * 60);
            long h = totalMins / 60;
            long m = totalMins % 60;
            return new SimpleStringProperty(String.format("%dh %02dm  (%.2f hrs)", h, m, r.getHour()));
        });
        durCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                if (item.startsWith("●")) {
                    setStyle("-fx-text-fill: #43A047; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #37474F;");
                }
            }
        });

        // Action column — Check Out button for active sessions
        TableColumn<HoursRecord, Void> actionCol = new TableColumn<>("");
        actionCol.setPrefWidth(120);
        actionCol.setSortable(false);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Check Out");
            {
                btn.setStyle(btnCss("#E53935"));
                btn.setOnAction(ev -> {
                    HoursRecord r = getTableView().getItems().get(getIndex());
                    doCheckOut(r);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                HoursRecord r = getTableView().getItems().get(getIndex());
                setGraphic(r != null && r.getComeOut() == null ? btn : null);
            }
        });

        table.getColumns().addAll(ciCol, coCol, durCol, actionCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size: 13px;");
        table.setPlaceholder(new Label("No sessions yet. Press \"Check In\" to start tracking."));
    }

    // ── Check In ───────────────────────────────────────────────────────────────

    private void doCheckIn() {
        boolean hasActive = sessions.stream().anyMatch(r -> r.getComeOut() == null);
        if (hasActive) {
            showFeedback("You already have an active session. Please check out before checking in again.", false);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        HoursRecord rec = new HoursRecord(0, username, now, null, 0);
        if (DatabaseManager.getInstance().addHoursRecord(rec)) {
            showFeedback("✓  Check In recorded at " + now.format(DT_FMT), true);
        } else {
            showFeedback("Error: could not save check-in. Please try again.", false);
        }
        loadSessions();
    }

    // ── Check Out ──────────────────────────────────────────────────────────────

    private void doCheckOut(HoursRecord record) {
        LocalDateTime now  = LocalDateTime.now();
        long minutes       = ChronoUnit.MINUTES.between(record.getComeIn(), now);
        double hours       = Math.round(minutes / 60.0 * 100.0) / 100.0;
        long h = minutes / 60;
        long m = minutes % 60;

        record.setComeOut(now);
        record.setHour(hours);

        if (DatabaseManager.getInstance().updateHoursRecord(record)) {
            showFeedback(String.format(
                "✓  Check Out recorded at %s  —  Duration: %dh %02dm (%.2f hrs)",
                now.format(DT_FMT), h, m, hours), true);
        } else {
            showFeedback("Error: could not save check-out. Please try again.", false);
        }
        loadSessions();
    }

    // ── State helpers ──────────────────────────────────────────────────────────

    private void loadSessions() {
        sessions.setAll(DatabaseManager.getInstance().getAllHoursRecordsForUser(username));
        refreshCheckInButton();
    }

    private void refreshCheckInButton() {
        boolean hasActive = sessions.stream().anyMatch(r -> r.getComeOut() == null);
        checkInBtn.setDisable(hasActive);
        if (hasActive) {
            checkInBtn.setStyle(
                "-fx-background-color: #B0BEC5; -fx-text-fill: #607D8B;" +
                "-fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 14px;");
            checkInBtn.setTooltip(new Tooltip("You have an active session — check out first."));
        } else {
            checkInBtn.setStyle(
                "-fx-background-color: #43A047; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-background-radius: 6; -fx-font-size: 14px;");
            checkInBtn.setTooltip(null);
        }
    }

    private void showFeedback(String msg, boolean success) {
        feedbackLabel.setText(msg);
        feedbackLabel.setVisible(true);
        if (success) {
            feedbackLabel.setStyle(
                "-fx-background-color: #E8F5E9; -fx-background-radius: 5;" +
                "-fx-text-fill: #2E7D32; -fx-font-weight: bold; -fx-padding: 8 12;");
        } else {
            feedbackLabel.setStyle(
                "-fx-background-color: #FFEBEE; -fx-background-radius: 5;" +
                "-fx-text-fill: #C62828; -fx-font-weight: bold; -fx-padding: 8 12;");
        }
    }

    private void startClock() {
        clockLabel.setText("Current time: " + LocalDateTime.now().format(DT_FMT));
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e ->
            clockLabel.setText("Current time: " + LocalDateTime.now().format(DT_FMT))));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();
        // Stop the clock when this scene is replaced
        root.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null) clock.stop();
        });
    }

    private String btnCss(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: white;" +
               "-fx-font-weight: bold; -fx-background-radius: 5;";
    }

    public BorderPane getRoot() {
        return root;
    }
}
