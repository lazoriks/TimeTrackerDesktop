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

@DisplayName("PdfExporter")
class PdfExporterTest {

    @TempDir
    Path tempDir;

    private static final LocalDateTime CHECK_IN  = LocalDateTime.of(2026, 5, 9, 9, 0, 0);
    private static final LocalDateTime CHECK_OUT = LocalDateTime.of(2026, 5, 9, 17, 30, 0);

    // ── Tests ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("exported file is created on disk")
    void export_fileIsCreated() throws Exception {
        File out = tempDir.resolve("report.pdf").toFile();
        PdfExporter.export(List.of(), out);
        assertTrue(out.exists());
    }

    @Test
    @DisplayName("exported file is not empty")
    void export_fileIsNotEmpty() throws Exception {
        File out = tempDir.resolve("report.pdf").toFile();
        PdfExporter.export(List.of(), out);
        assertTrue(out.length() > 0, "PDF file should not be empty");
    }

    @Test
    @DisplayName("PDF magic bytes %%PDF are present")
    void export_hasPdfMagicBytes() throws Exception {
        File out = tempDir.resolve("report.pdf").toFile();
        PdfExporter.export(List.of(), out);

        byte[] header = new byte[4];
        try (var is = Files.newInputStream(out.toPath())) {
            //noinspection ResultOfMethodCallIgnored
            is.read(header);
        }
        assertEquals('%', (char) header[0]);
        assertEquals('P', (char) header[1]);
        assertEquals('D', (char) header[2]);
        assertEquals('F', (char) header[3]);
    }

    @Test
    @DisplayName("exporting with data produces a larger file than exporting nothing")
    void export_withData_largerThanEmpty() throws Exception {
        File emptyPdf = tempDir.resolve("empty.pdf").toFile();
        File dataPdf  = tempDir.resolve("data.pdf").toFile();

        PdfExporter.export(List.of(), emptyPdf);
        PdfExporter.export(List.of(
            new HoursRecord(1, "alice", CHECK_IN, CHECK_OUT, 8.50),
            new HoursRecord(2, "bob",   CHECK_IN, null,      0)
        ), dataPdf);

        assertTrue(dataPdf.length() > emptyPdf.length(),
            "PDF with data rows should be larger than an empty report");
    }

    @Test
    @DisplayName("exporting an active session (null ComeOut) does not throw")
    void export_activeSession_noException() {
        File out = tempDir.resolve("active.pdf").toFile();
        assertDoesNotThrow(() ->
            PdfExporter.export(
                List.of(new HoursRecord(1, "dave", CHECK_IN, null, 0)), out));
    }

    @Test
    @DisplayName("exporting special characters in username does not throw")
    void export_specialCharsInUsername_noException() {
        File out = tempDir.resolve("special.pdf").toFile();
        assertDoesNotThrow(() ->
            PdfExporter.export(
                List.of(new HoursRecord(1, "Ünïcödé & <Spëcial>", CHECK_IN, CHECK_OUT, 1.0)), out));
    }
}
