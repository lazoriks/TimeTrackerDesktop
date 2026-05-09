package com.timetracker.export;

import com.timetracker.model.HoursRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExcelExporter")
class ExcelExporterTest {

    @TempDir
    Path tempDir;

    private static final LocalDateTime CHECK_IN  = LocalDateTime.of(2026, 5, 9, 9, 0, 0);
    private static final LocalDateTime CHECK_OUT = LocalDateTime.of(2026, 5, 9, 17, 30, 0);

    // ── Helper ─────────────────────────────────────────────────────────────────

    private Workbook exportAndOpen(List<HoursRecord> records) throws Exception {
        File out = tempDir.resolve("test.xlsx").toFile();
        ExcelExporter.export(records, out);
        return new XSSFWorkbook(new FileInputStream(out));
    }

    // ── Tests ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("exported file is created on disk")
    void export_fileIsCreated() throws Exception {
        File out = tempDir.resolve("report.xlsx").toFile();
        ExcelExporter.export(List.of(), out);
        assertTrue(out.exists());
        assertTrue(out.length() > 0);
    }

    @Test
    @DisplayName("sheet is named 'Hours Report'")
    void export_sheetName() throws Exception {
        try (Workbook wb = exportAndOpen(List.of())) {
            assertNotNull(wb.getSheet("Hours Report"));
        }
    }

    @Test
    @DisplayName("header row contains the four expected column titles")
    void export_headerRowTitles() throws Exception {
        try (Workbook wb = exportAndOpen(List.of())) {
            Sheet sheet = wb.getSheet("Hours Report");
            Row header = sheet.getRow(0);

            assertEquals("User",      header.getCell(0).getStringCellValue());
            assertEquals("Come In",   header.getCell(1).getStringCellValue());
            assertEquals("Come Out",  header.getCell(2).getStringCellValue());
            assertEquals("Hours",     header.getCell(3).getStringCellValue());
        }
    }

    @Test
    @DisplayName("empty list produces only the header row")
    void export_emptyList_onlyHeaderRow() throws Exception {
        try (Workbook wb = exportAndOpen(List.of())) {
            Sheet sheet = wb.getSheet("Hours Report");
            // Row 0 = header, row 1 should not exist
            assertNull(sheet.getRow(1));
        }
    }

    @Test
    @DisplayName("completed session: user, check-in, check-out, and hours are all written")
    void export_completedSession_allCellsPopulated() throws Exception {
        HoursRecord r = new HoursRecord(1, "alice", CHECK_IN, CHECK_OUT, 8.50);
        try (Workbook wb = exportAndOpen(List.of(r))) {
            Sheet sheet = wb.getSheet("Hours Report");
            Row row = sheet.getRow(1);

            assertEquals("alice",               row.getCell(0).getStringCellValue());
            assertTrue(row.getCell(1).getStringCellValue().contains("2026-05-09 09:00:00"));
            assertTrue(row.getCell(2).getStringCellValue().contains("2026-05-09 17:30:00"));
            assertEquals(8.50, row.getCell(3).getNumericCellValue(), 0.001);
        }
    }

    @Test
    @DisplayName("active session: Hours cell is blank (no ComeOut)")
    void export_activeSession_hoursCellIsBlank() throws Exception {
        HoursRecord r = new HoursRecord(2, "bob", CHECK_IN, null, 0);
        try (Workbook wb = exportAndOpen(List.of(r))) {
            Sheet sheet = wb.getSheet("Hours Report");
            Row row = sheet.getRow(1);

            Cell hoursCell = row.getCell(3);
            // Either null or blank type means no value written
            boolean isBlank = hoursCell == null
                || hoursCell.getCellType() == CellType.BLANK
                || hoursCell.getCellType() == CellType._NONE;
            assertTrue(isBlank, "Hours cell should be blank for an active session");
        }
    }

    @Test
    @DisplayName("multiple records produce correct number of data rows")
    void export_multipleRecords_rowCount() throws Exception {
        List<HoursRecord> records = List.of(
            new HoursRecord(1, "alice", CHECK_IN, CHECK_OUT, 8.50),
            new HoursRecord(2, "bob",   CHECK_IN, null,       0),
            new HoursRecord(3, "carol", CHECK_IN, CHECK_OUT, 7.25)
        );
        try (Workbook wb = exportAndOpen(records)) {
            Sheet sheet = wb.getSheet("Hours Report");
            // header + 3 data rows
            assertEquals(4, sheet.getPhysicalNumberOfRows());
        }
    }
}
