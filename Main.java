// Main Class (Spring Boot Entry Point)
// Here’s the main Spring Boot application class:

package com.example.xmlparser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }
}




import java.sql.*;
import java.io.*;
import java.util.*;
import org.apache.commons.csv.*;

public class MRMExporterFinal {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:clickhouse://<HOST>:<PORT>/<DATABASE>";
        String user = "<USER>";
        String password = "<PASSWORD>";

        // Define common columns
        List<String> commonColumns = Arrays.asList(
            "site", "asset_class", "cob_date", "risk_source_id", "pts_source_id",
            "pts_leg_id", "scenario_set_id", "model", "currency_pair", "job_uuid", "unit"
        );

        try (
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM your_table");
            BufferedWriter irzWriter = new BufferedWriter(new FileWriter("IRZ.csv"));
            BufferedWriter fxWriter = new BufferedWriter(new FileWriter("FXDELTA.csv"));
        ) {
            CSVPrinter irzPrinter = new CSVPrinter(irzWriter,
                    CSVFormat.DEFAULT.withHeader(getIRZHeader(commonColumns)));
            CSVPrinter fxPrinter = new CSVPrinter(fxWriter,
                    CSVFormat.DEFAULT.withHeader(getFXHeader(commonColumns)));

            while (rs.next()) {
                Map<String, String> commonValues = new HashMap<>();
                for (String col : commonColumns) {
                    commonValues.put(col, rs.getString(col));
                }

                // ---- IRZ ----
                String[] irzTenors = getArray(rs, "IRZ.tenor");
                String[] irzValues = getArray(rs, "IRZ.value");
                String[] irzUnits = getArray(rs, "IRZ.unit");
                String[] irzRisks = getArray(rs, "IRZ.risk");
                String[] irzShifts = getArray(rs, "IRZ.shift");

                int irzLen = maxLength(irzTenors, irzValues, irzUnits, irzRisks, irzShifts);
                if (irzLen > 0) {
                    for (int i = 0; i < irzLen; i++) {
                        List<String> row = new ArrayList<>();
                        for (String col : commonColumns) row.add(commonValues.get(col));
                        row.add(safe(irzTenors, i));
                        row.add(safe(irzShifts, i));
                        row.add(safe(irzRisks, i));
                        row.add(safe(irzUnits, i));
                        row.add(safe(irzValues, i));
                        irzPrinter.printRecord(row);
                    }
                }

                // ---- FXDELTA ----
                String[] fxTenors = getArray(rs, "FXDELTA.tenor");
                String[] fxValues = getArray(rs, "FXDELTA.value");
                String[] fxUnits = getArray(rs, "FXDELTA.unit");
                String[] fxRisks = getArray(rs, "FXDELTA.risk");

                int fxLen = maxLength(fxTenors, fxValues, fxUnits, fxRisks);
                if (fxLen > 0) {
                    for (int i = 0; i < fxLen; i++) {
                        List<String> row = new ArrayList<>();
                        for (String col : commonColumns) row.add(commonValues.get(col));
                        row.add(safe(fxTenors, i));
                        row.add(safe(fxRisks, i));
                        row.add(safe(fxUnits, i));
                        row.add(safe(fxValues, i));
                        fxPrinter.printRecord(row);
                    }
                }
            }

            irzPrinter.flush();
            fxPrinter.flush();
        }
    }

    // ----------- Helper Methods ------------

    private static String[] getArray(ResultSet rs, String col) throws SQLException {
        String raw = null;
        try {
            raw = rs.getString(col);
        } catch (SQLException e) {
            return new String[0]; // column not found
        }
        if (raw == null || raw.trim().isEmpty() || raw.equals("[]")) return new String[0];
        raw = raw.replace("[", "").replace("]", "").replace("\"", "");
        return raw.split(",");
    }

    private static String safe(String[] arr, int i) {
        return i < arr.length ? arr[i].trim() : "";
    }

    private static int maxLength(String[]... arrays) {
        int max = 0;
        for (String[] arr : arrays) {
            if (arr != null) max = Math.max(max, arr.length);
        }
        return max;
    }

    private static String[] getIRZHeader(List<String> common) {
        List<String> header = new ArrayList<>(common);
        header.addAll(Arrays.asList("tenor", "shift", "risk", "mrm_unit", "value"));
        return header.toArray(new String[0]);
    }

    private static String[] getFXHeader(List<String> common) {
        List<String> header = new ArrayList<>(common);
        header.addAll(Arrays.asList("tenor", "risk", "mrm_unit", "value"));
        return header.toArray(new String[0]);
    }
}
