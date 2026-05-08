package com.timetracker.ui;

import com.timetracker.App;
import com.timetracker.db.DatabaseManager;
import com.timetracker.export.CsvExporter;
import com.timetracker.export.ExcelExporter;
import com.timetracker.export.PdfExporter;
import com.timetracker.model.HoursRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportScreen {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BorderPane root;
    private final ObservableList<HoursRecord> reportData = FXCollections.observableArrayList();
    private final TableView<HoursRecord> table = new TableView<>();

    private DatePicker  startPicker;
    private DatePicker  endPicker;
    private ComboBox<String> userCombo;

    public ReportScreen() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #F5F5F5;");
        root.setTop(buildTopBar());

        VBox center = new VBox(10);
        center.setPadding(new Insets(10, 12, 4, 12));
        center.getChildren().addAll(buildFilterBar(), buildTable());
        VBox.setVgrow(table, Priority.ALWAYS);

        root.setCenter(center);
        root.setBottom(buildExportBar());
    }

    // ── Top bar ────────────────────────────────────────────────────────────────

    private HBox buildTopBar() {
        HBox bar = new HBox(10);
        bar.setPadding(new Insets(14, 16, 14, 16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: #3F51B5;");

        Label title = new Label("Report");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: white;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("← Back");
        backBtn.setStyle("-fx-background-color: #7986CB; -fx-text-fill: white;" +
                         "-fx-font-weight: bold; -fx-background-radius: 5;");
        backBtn.setOnAction(e -> App.getInstance().showUserManagement(null));

        bar.getChildren().addAll(title, spacer, backBtn);
        return bar;
    }

    // ── Filter bar ─────────────────────────────────────────────────────────────

    private HBox buildFilterBar() {
        startPicker = new DatePicker(LocalDate.now().withDayOfMonth(1));
        endPicker   = new DatePicker(LocalDate.now());

        List<String> names = DatabaseManager.getInstance().getAllUsernames();
        names.add(0, "");
        userCombo = new ComboBox<>(FXCollections.observableArrayList(names));
        userCombo.setValue("");
        userCombo.setPromptText("All users");
        userCombo.setPrefWidth(180);

        Button genBtn = new Button("Create Report");
        genBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;" +
                        "-fx-font-weight: bold; -fx-background-radius: 5;");
        genBtn.setOnAction(e -> generateReport());

        HBox bar = new HBox(10,
            new Label("Start Period:"), startPicker,
            new Label("End Period:"),   endPicker,
            new Label("User:"),         userCombo,
            genBtn
        );
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(6, 0, 6, 0));
        return bar;
    }

    // ── Table ──────────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private VBox buildTable() {
        TableColumn<HoursRecord, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(new PropertyValueFactory<>("user"));
        userCol.setPrefWidth(180);

        TableColumn<HoursRecord, LocalDateTime> ciCol = dtCol("Come In",  "comeIn");
        TableColumn<HoursRecord, LocalDateTime> coCol = dtCol("Come Out", "comeOut");

        TableColumn<HoursRecord, Double> hrCol = new TableColumn<>("Hours");
        hrCol.setCellValueFactory(new PropertyValueFactory<>("hour"));
        hrCol.setPrefWidth(90);
        hrCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setText(null); return; }
                int idx = getIndex();
                if (idx >= 0 && idx < getTableView().getItems().size()) {
                    HoursRecord r = getTableView().getItems().get(idx);
                    setText(r.getComeOut() != null ? String.format("%.2f", item) : "");
                }
            }
        });

        table.getColumns().addAll(userCol, ciCol, coCol, hrCol);
        table.setItems(reportData);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-font-size: 13px;");
        table.setPlaceholder(new Label("No data. Set filters and press \"Create Report\"."));

        VBox wrap = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        return wrap;
    }

    private TableColumn<HoursRecord, LocalDateTime> dtCol(String title, String prop) {
        TableColumn<HoursRecord, LocalDateTime> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(170);
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.format(DT_FMT));
            }
        });
        return col;
    }

    // ── Export bar ─────────────────────────────────────────────────────────────

    private HBox buildExportBar() {
        Button csvBtn  = new Button("Upload to CSV");
        Button xlsxBtn = new Button("Upload to Excel");
        Button pdfBtn  = new Button("Upload to PDF");

        csvBtn.setStyle(btnCss("#607D8B"));
        xlsxBtn.setStyle(btnCss("#00796B"));
        pdfBtn.setStyle(btnCss("#D32F2F"));

        csvBtn.setOnAction(e  -> doExport("csv"));
        xlsxBtn.setOnAction(e -> doExport("xlsx"));
        pdfBtn.setOnAction(e  -> doExport("pdf"));

        HBox bar = new HBox(12, csvBtn, xlsxBtn, pdfBtn);
        bar.setPadding(new Insets(12, 16, 16, 16));
        bar.setAlignment(Pos.CENTER);
        return bar;
    }

    // ── Logic ──────────────────────────────────────────────────────────────────

    private void generateReport() {
        LocalDate start = startPicker.getValue();
        LocalDate end   = endPicker.getValue();

        if (start == null || end == null) {
            warn("Please select start and end dates.");
            return;
        }
        if (start.isAfter(end)) {
            warn("Start date must be before or equal to end date.");
            return;
        }

        String user = userCombo.getValue();
        List<HoursRecord> records = DatabaseManager.getInstance().getHoursReport(start, end, user);
        reportData.setAll(records);

        if (records.isEmpty()) {
            table.setPlaceholder(new Label("No records found for the selected criteria."));
        }
    }

    private void doExport(String format) {
        if (reportData.isEmpty()) {
            warn("No data to export. Please generate a report first.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Report");

        switch (format) {
            case "csv"  -> { chooser.setInitialFileName("report.csv");
                             chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv")); }
            case "xlsx" -> { chooser.setInitialFileName("report.xlsx");
                             chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx")); }
            case "pdf"  -> { chooser.setInitialFileName("report.pdf");
                             chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf")); }
        }

        File file = chooser.showSaveDialog(App.getInstance().getPrimaryStage());
        if (file == null) return;

        try {
            List<HoursRecord> list = reportData;
            switch (format) {
                case "csv"  -> CsvExporter.export(list, file);
                case "xlsx" -> ExcelExporter.export(list, file);
                case "pdf"  -> PdfExporter.export(list, file);
            }
            new Alert(Alert.AlertType.INFORMATION,
                "Report exported to:\n" + file.getAbsolutePath(), ButtonType.OK).showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
            new Alert(Alert.AlertType.ERROR,
                "Export failed: " + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void warn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    private String btnCss(String color) {
        return "-fx-background-color: " + color + "; -fx-text-fill: white;" +
               "-fx-font-weight: bold; -fx-background-radius: 5; -fx-min-width: 140px;";
    }

    public BorderPane getRoot() {
        return root;
    }
}
