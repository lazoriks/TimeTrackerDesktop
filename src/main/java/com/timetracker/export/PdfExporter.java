package com.timetracker.export;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.timetracker.model.HoursRecord;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfExporter {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Color HEADER_BG  = new Color(63, 81, 181);
    private static final Color ALT_BG     = new Color(232, 234, 246);
    private static final Color WHITE      = Color.WHITE;

    public static void export(List<HoursRecord> records, File file) throws Exception {
        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 50, 36);
        PdfWriter.getInstance(doc, new FileOutputStream(file));
        doc.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, HEADER_BG);
        Paragraph title = new Paragraph("Hours Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(16);
        doc.add(title);

        // Table: User | Come In | Come Out | Hours
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3.5f, 3f, 3f, 1.5f});

        Font hFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, WHITE);
        addHeaderCell(table, "User",     hFont);
        addHeaderCell(table, "Come In",  hFont);
        addHeaderCell(table, "Come Out", hFont);
        addHeaderCell(table, "Hours",    hFont);

        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        boolean alt = false;
        for (HoursRecord r : records) {
            Color bg = alt ? ALT_BG : WHITE;
            String ci = r.getComeIn()  != null ? r.getComeIn().format(DT_FMT)  : "";
            String co = r.getComeOut() != null ? r.getComeOut().format(DT_FMT) : "";
            String hr = r.getComeOut() != null ? String.format("%.2f", r.getHour()) : "";

            addDataCell(table, r.getUser(), dataFont, bg, Element.ALIGN_LEFT);
            addDataCell(table, ci,          dataFont, bg, Element.ALIGN_CENTER);
            addDataCell(table, co,          dataFont, bg, Element.ALIGN_CENTER);
            addDataCell(table, hr,          dataFont, bg, Element.ALIGN_RIGHT);
            alt = !alt;
        }

        doc.add(table);
        doc.close();
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(HEADER_BG);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(new Color(48, 63, 159));
        table.addCell(cell);
    }

    private static void addDataCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setHorizontalAlignment(align);
        cell.setBorderColor(new Color(200, 200, 210));
        table.addCell(cell);
    }
}
