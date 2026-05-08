package com.timetracker.export;

import com.timetracker.model.HoursRecord;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExporter {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void export(List<HoursRecord> records, File file) throws Exception {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Hours Report");

            // Header style
            CellStyle headerStyle = wb.createCellStyle();
            Font hFont = wb.createFont();
            hFont.setBold(true);
            hFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(hFont);
            headerStyle.setFillForegroundColor(IndexedColors.INDIGO.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Alternating row style
            CellStyle altStyle = wb.createCellStyle();
            altStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            altStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Number style
            CellStyle numStyle = wb.createCellStyle();
            DataFormat fmt = wb.createDataFormat();
            numStyle.setDataFormat(fmt.getFormat("0.00"));

            CellStyle numAltStyle = wb.createCellStyle();
            numAltStyle.cloneStyleFrom(numStyle);
            numAltStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            numAltStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header row
            String[] headers = {"User", "Come In", "Come Out", "Hours"};
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(22);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            for (int i = 0; i < records.size(); i++) {
                HoursRecord r = records.get(i);
                Row row = sheet.createRow(i + 1);
                boolean alt = (i % 2 == 1);

                createCell(row, 0, r.getUser(), alt ? altStyle : null);
                createCell(row, 1, r.getComeIn()  != null ? r.getComeIn().format(DT_FMT)  : "", alt ? altStyle : null);
                createCell(row, 2, r.getComeOut() != null ? r.getComeOut().format(DT_FMT) : "", alt ? altStyle : null);

                Cell hourCell = row.createCell(3);
                if (r.getComeOut() != null) {
                    hourCell.setCellValue(r.getHour());
                    hourCell.setCellStyle(alt ? numAltStyle : numStyle);
                } else if (alt) {
                    hourCell.setCellStyle(altStyle);
                }
            }

            // Auto-size
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            // Minimum widths
            sheet.setColumnWidth(0, Math.max(sheet.getColumnWidth(0), 6000));
            sheet.setColumnWidth(1, Math.max(sheet.getColumnWidth(1), 5500));
            sheet.setColumnWidth(2, Math.max(sheet.getColumnWidth(2), 5500));

            try (FileOutputStream fos = new FileOutputStream(file)) {
                wb.write(fos);
            }
        }
    }

    private static void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        if (style != null) cell.setCellStyle(style);
    }
}
