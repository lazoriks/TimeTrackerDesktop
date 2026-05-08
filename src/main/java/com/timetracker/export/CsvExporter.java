package com.timetracker.export;

import com.timetracker.model.HoursRecord;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvExporter {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void export(List<HoursRecord> records, File file) throws Exception {
        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            pw.println("User,Come In,Come Out,Hours");
            for (HoursRecord r : records) {
                String ci = r.getComeIn()  != null ? r.getComeIn().format(DT_FMT)  : "";
                String co = r.getComeOut() != null ? r.getComeOut().format(DT_FMT) : "";
                String hr = r.getComeOut() != null ? String.format("%.2f", r.getHour()) : "";
                pw.printf("\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    escape(r.getUser()), ci, co, hr);
            }
        }
    }

    private static String escape(String s) {
        return s == null ? "" : s.replace("\"", "\"\"");
    }
}
