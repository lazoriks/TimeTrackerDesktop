package com.timetracker.ui;

import com.timetracker.App;
import com.timetracker.db.DatabaseManager;
import com.timetracker.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.Optional;

public class UserManagementScreen {

    private final BorderPane root;
    private final ObservableList<User> userData = FXCollections.observableArrayList();
    private final TableView<User> table = new TableView<>();
    private final String loggedInUser;

    public UserManagementScreen(String loggedInUser) {
        this.loggedInUser = loggedInUser;
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F5F5;");
        root.setTop(buildTopBar());
        root.setCenter(buildTable());
        root.setBottom(buildButtons());
        loadUsers();
    }

    // ── Top bar ────────────────────────────────────────────────────────────────

    private HBox buildTopBar() {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(14, 16, 14, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #3F51B5;");

        Label title = new Label("User Management");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button reportBtn = new Button("Report");
        reportBtn.setStyle(btnCss("#FF9800"));
        reportBtn.setOnAction(e -> App.getInstance().showReport());

        Label userLbl = new Label("Logged in: " + loggedInUser);
        userLbl.setStyle("-fx-text-fill: #C5CAE9; -fx-font-size: 12px;");

        Button logoutBtn = new Button("Log Out");
        logoutBtn.setStyle(btnCss("#E53935"));
        logoutBtn.setOnAction(e -> App.getInstance().showLogin());

        bar.getChildren().addAll(title, spacer, reportBtn, userLbl, logoutBtn);
        return bar;
    }

    // ── Table ──────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private VBox buildTable() {
        TableColumn<User, String>  userCol   = col("User (Full Name)", "user",      220);
        TableColumn<User, String>  passCol   = col("Password",          "password", 130);
        TableColumn<User, Boolean> superCol  = col("Super User",        "superUser", 100);
        TableColumn<User, String>  emailCol  = col("Email",             "email",    200);
        TableColumn<User, String>  mobileCol = col("Mobile",            "mobile",   120);

        table.getColumns().addAll(userCol, passCol, superCol, emailCol, mobileCol);
        table.setItems(userData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size: 13px;");
        table.setPlaceholder(new Label("No users found."));

        VBox wrapper = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setMargin(table, new Insets(8, 10, 4, 10));
        return wrapper;
    }

    @SuppressWarnings("unchecked")
    private <T> TableColumn<User, T> col(String title, String property, double width) {
        TableColumn<User, T> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setPrefWidth(width);
        return col;
    }

    // ── Bottom buttons ─────────────────────────────────────────────────────────

    private HBox buildButtons() {
        Button addBtn    = new Button("Add");
        Button editBtn   = new Button("Edit");
        Button deleteBtn = new Button("Delete");

        addBtn.setStyle(btnCss("#4CAF50"));
        editBtn.setStyle(btnCss("#2196F3"));
        deleteBtn.setStyle(btnCss("#E53935"));

        addBtn.setPrefWidth(110);
        editBtn.setPrefWidth(110);
        deleteBtn.setPrefWidth(110);

        addBtn.setOnAction(e -> openDialog(null));
        editBtn.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { alert("Select a user to edit."); return; }
            openDialog(sel);
        });
        deleteBtn.setOnAction(e -> {
            User sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { alert("Select a user to delete."); return; }
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Delete user \"" + sel.getUser() + "\" and all their time records?",
                ButtonType.YES, ButtonType.NO);
            confirm.setHeaderText("Confirm deletion");
            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.YES) {
                DatabaseManager.getInstance().deleteUser(sel.getUser());
                loadUsers();
            }
        });

        HBox box = new HBox(12, addBtn, editBtn, deleteBtn);
        box.setPadding(new Insets(12, 16, 16, 16));
        box.setAlignment(Pos.CENTER);
        return box;
    }

    // ── User dialog (Add / Edit) ───────────────────────────────────────────────

    private void openDialog(User existing) {
        Dialog<User> dlg = new Dialog<>();
        dlg.setTitle(existing == null ? "Add User" : "Edit User");
        dlg.setHeaderText(existing == null ? "Enter new user details:" : "Edit user details:");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        TextField    userField   = new TextField(existing != null ? existing.getUser()     : "");
        PasswordField passField  = new PasswordField();
        if (existing != null) passField.setText(existing.getPassword());
        CheckBox     superCheck  = new CheckBox();
        if (existing != null) superCheck.setSelected(existing.isSuperUser());
        TextField    emailField  = new TextField(existing != null ? existing.getEmail()    : "");
        TextField    mobileField = new TextField(existing != null ? existing.getMobile()   : "");

        userField.setPromptText("Full name  (required, max 150)");
        passField.setPromptText("Password   (required, max 50)");
        emailField.setPromptText("Email  (optional, max 150)");
        mobileField.setPromptText("Mobile (optional, max 15)");

        for (TextField tf : List.of(userField, emailField, mobileField)) tf.setPrefWidth(260);
        passField.setPrefWidth(260);

        Label err = new Label();
        err.setStyle("-fx-text-fill: #E53935; -fx-font-size: 12px;");
        err.setWrapText(true);

        grid.add(new Label("User (Full Name)*:"), 0, 0); grid.add(userField,   1, 0);
        grid.add(new Label("Password*:"),          0, 1); grid.add(passField,   1, 1);
        grid.add(new Label("Super User:"),         0, 2); grid.add(superCheck,  1, 2);
        grid.add(new Label("Email:"),              0, 3); grid.add(emailField,  1, 3);
        grid.add(new Label("Mobile:"),             0, 4); grid.add(mobileField, 1, 4);
        grid.add(err,                              0, 5, 2, 1);

        dlg.getDialogPane().setContent(grid);
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Validation before closing
        Button okBtn = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.addEventFilter(ActionEvent.ACTION, ev -> {
            String u = userField.getText().trim();
            String p = passField.getText();
            String e2 = emailField.getText().trim();
            String m = mobileField.getText().trim();

            if (u.isEmpty())   { err.setText("User name is required."); ev.consume(); return; }
            if (u.length() > 150) { err.setText("User name max 150 characters."); ev.consume(); return; }
            if (p.isEmpty())   { err.setText("Password is required."); ev.consume(); return; }
            if (p.length() > 50)  { err.setText("Password max 50 characters."); ev.consume(); return; }
            if (e2.length() > 150) { err.setText("Email max 150 characters."); ev.consume(); return; }
            if (m.length() > 15)  { err.setText("Mobile max 15 characters."); ev.consume(); return; }
        });

        dlg.setResultConverter(bt -> {
            if (bt != ButtonType.OK) return null;
            return new User(
                userField.getText().trim(),
                passField.getText(),
                superCheck.isSelected(),
                emailField.getText().trim(),
                mobileField.getText().trim()
            );
        });

        dlg.showAndWait().ifPresent(u -> {
            if (existing == null) {
                if (!DatabaseManager.getInstance().addUser(u)) {
                    alert("Could not save user — the username may already exist.");
                    return;
                }
            } else {
                DatabaseManager.getInstance().updateUser(u, existing.getUser());
            }
            loadUsers();
        });
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void loadUsers() {
        List<User> users = DatabaseManager.getInstance().getAllUsers();
        userData.setAll(users);
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    private String btnCss(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: white;" +
               "-fx-font-weight: bold; -fx-background-radius: 5;";
    }

    public BorderPane getRoot() {
        return root;
    }
}
