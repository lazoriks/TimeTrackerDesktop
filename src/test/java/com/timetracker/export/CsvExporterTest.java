package com.timetracker.export;

import com.timetracker.model.HoursRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CsvExporter")
class CsvExporterTest {

    @TempDir
    Path tempDir;

    private static final LocalDateTime CHECK_IN  = LocalDateTime.of(2026, 5, 9, 9, 0, 0);
    private static final LocalDateTime CHECK_OUT = LocalDateTime.of(2026, 5, 9, 17, 30, 0);

    // ── Helper ─────────────────────────────────────────────────────────────────

    private List<String> exportAndRead(List<HoursRecord> records) throws Exception {
        File out = tempDir.resolve("test.csv").toFile();
        CsvExporter.export(records, out);
        return Files.readAllLines(out.toPath());
    }

    // ── Tests ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("exports header row as first line")
    void export_firstLineIsHeader() throws Exception {
        List<String> lines = exportAndRead(List.of());
        assertEquals("User,Come In,Come Out,Hours", lines.get(0));
    }

    @Test
    @DisplayName("empty list produces only the header")
    void export_emptyList_onlyHeader() throws Exception {
        List<String> lines = exportAndRead(List.of());
        assertEquals(1, lines.size());
    }

    @Test
    @DisplayName("completed session row contains all four fields")
    void export_completedSession_allFields() throws Exception {
        HoursRecord r = new HoursRecord(1, "alice", CHECK_IN, CHECK_OUT, 8.50);
        List<String> lines = exportAndRead(List.of(r));

        assertEquals(2, lines.size());
        String row = lines.get(1);
        assertTrue(row.contains("alice"),                    "should contain username");
        assertTrue(row.contains("2026-05-09 09:00:00"),      "should contain check-in");
        assertTrue(row.contains("2026-05-09 17:30:00"),      "should contain check-out");
        assertTrue(row.contains("8.50"),                     "should contain hours");
    }

    @Test
    @DisplayName("active session has empty ComeOut and Hours columns")
    void export_activeSession_emptyComeOutAndHours() throws Exception {
        HoursRecord r = new HoursRecord(2, "bob", CHECK_IN, null, 0);
        List<String> lines = exportAndRead(List.of(r));

        String row = lines.get(1);
        // ComeOut and Hours both empty → row ends with two consecutive empty quoted fields
        assertTrue(row.contains("\"\",\"\""), "ComeOut and Hours should be empty quoted strings");
    }

    @Test
    @DisplayName("multiple records produce one row each")
    void export_multipleRecords_correctRowCount() throws Exception {
        List<HoursRecord> records = List.of(
            new HoursRecord(1, "alice", CHECK_IN, CHECK_OUT, 8.50),
            new HoursRecord(2, "bob",   CHECK_IN, null,       0)
        );
        List<String> lines = exportAndRead(records);
        assertEquals(3, lines.size(), "header + 2 data rows");
    }

    @Test
    @DisplayName("username containing a double-quote is escaped")
    void export_usernameWithQuote_isEscaped() throws Exception {
        HoursRecord r = new HoursRecord(3, "O'Brien, \"Bob\"", CHECK_IN, null, 0);
        List<String> lines = exportAndRead(List.of(r));

        String row = lines.get(1);
        assertTrue(row.contains("\"\""), "double-quotes inside fields should be doubled");
    }

    @Test
    @DisplayName("produced file exists on disk")
    void export_fileExists() throws Exception {
        File out = tempDir.resolve("report.csv").toFile();
        CsvExporter.export(List.of(), out);
        assertTrue(out.exists());
        assertTrue(out.length() > 0);
    }
}
