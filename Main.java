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





import java.io.; import java.sql.; import java.util.; import org.apache.commons.csv.;

public class RiskDataExporterFinal {

private static final List<String> COMMON_COLUMNS = Arrays.asList("site", "asset_class", "cob_date", "risk_source_id", "pts_source_id",
        "pts_leg_id", "scenario_set_id", "model", "currency_pair", "job_uuid", "unit");

public static void main(String[] args) throws Exception {
    Connection conn = DriverManager.getConnection("jdbc:your-db-url", "username", "password");
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT * FROM your_table");

    CSVPrinter irzPrinter = new CSVPrinter(new FileWriter("irz.csv"),
            CSVFormat.DEFAULT.withHeader(buildHeader(COMMON_COLUMNS, Arrays.asList("tenor", "shift", "risk", "mrmUnit", "value"))));

    CSVPrinter fxPrinter = new CSVPrinter(new FileWriter("fxdelta.csv"),
            CSVFormat.DEFAULT.withHeader(buildHeader(COMMON_COLUMNS, Arrays.asList("column", "tenor", "value", "risk_factor", "unit"))));

    CSVPrinter pvPrinter = new CSVPrinter(new FileWriter("pv.csv"),
            CSVFormat.DEFAULT.withHeader(COMMON_COLUMNS.toArray(new String[0])));

    while (rs.next()) {
        Map<String, String> commonValues = new LinkedHashMap<>();
        for (String col : COMMON_COLUMNS) {
            commonValues.put(col, rs.getString(col));
        }

        boolean hasIRZ = rs.getString("irz.tenor") != null || rs.getString("irz.value") != null;
        boolean hasFX = rs.getString("fxdelta.column") != null || rs.getString("fxdelta.value") != null;

        if (hasIRZ) {
            List<List<String>> tenors = parseNestedArray(rs.getString("irz.tenor"));
            List<List<String>> values = parseNestedArray(rs.getString("irz.value"));
            List<List<String>> units = parseNestedArray(rs.getString("irz.unit"));
            List<List<String>> risks = parseNestedArray(rs.getString("irz.risk"));
            List<List<String>> shifts = parseNestedArray(rs.getString("irz.shift"));

            for (int i = 0; i < tenors.size(); i++) {
                List<String> tList = tenors.get(i);
                List<String> vList = i < values.size() ? values.get(i) : new ArrayList<>();
                List<String> uList = i < units.size() ? units.get(i) : new ArrayList<>();
                List<String> rList = i < risks.size() ? risks.get(i) : new ArrayList<>();
                List<String> sList = i < shifts.size() ? shifts.get(i) : new ArrayList<>();

                for (int j = 0; j < tList.size(); j++) {
                    List<String> row = new ArrayList<>(commonValues.values());
                    row.add(safe(tList, j));
                    row.add(safe(sList, j));
                    row.add(safe(rList, j));
                    row.add(safe(uList, j));
                    row.add(safe(vList, j));
                    irzPrinter.printRecord(row);
                }
            }
        }

        if (hasFX) {
            List<List<String>> columns = parseNestedArray(rs.getString("fxdelta.column"));
            List<List<String>> tenors = parseNestedArray(rs.getString("fxdelta.tenor"));
            List<List<String>> values = parseNestedArray(rs.getString("fxdelta.value"));
            List<List<String>> risks = parseNestedArray(rs.getString("fxdelta.risk_factor"));
            List<List<String>> units = parseNestedArray(rs.getString("fxdelta.unit"));

            for (int i = 0; i < columns.size(); i++) {
                List<String> cList = columns.get(i);
                List<String> tList = i < tenors.size() ? tenors.get(i) : new ArrayList<>();
                List<String> vList = i < values.size() ? values.get(i) : new ArrayList<>();
                List<String> rList = i < risks.size() ? risks.get(i) : new ArrayList<>();
                List<String> uList = i < units.size() ? units.get(i) : new ArrayList<>();

                for (int j = 0; j < cList.size(); j++) {
                    List<String> row = new ArrayList<>(commonValues.values());
                    row.add(safe(cList, j));
                    row.add(safe(tList, j));
                    row.add(safe(vList, j));
                    row.add(safe(rList, j));
                    row.add(safe(uList, j));
                    fxPrinter.printRecord(row);
                }
            }
        }

        if (!hasIRZ && !hasFX) {
            pvPrinter.printRecord(commonValues.values());
        }
    }

    irzPrinter.close();
    fxPrinter.close();
    pvPrinter.close();
    conn.close();
}

private static List<List<String>> parseNestedArray(String input) {
    List<List<String>> result = new ArrayList<>();
    if (input == null || input.trim().isEmpty() || input.equals("[]")) return result;
    input = input.trim().substring(1, input.length() - 1);
    String[] groups = input.split("(?<=),\s*(?=");
    for (String group : groups) {
        group = group.replaceAll("[\"]", "").trim();
        if (!group.isEmpty()) {
            result.add(Arrays.asList(group.split("\s*,\s*")));
        }
    }
    return result;
}

private static String safe(List<String> list, int index) {
    return index < list.size() ? list.get(index).trim() : "";
}

private static String[] buildHeader(List<String> common, List<String> extras) {
    List<String> header = new ArrayList<>(common);
    header.addAll(extras);
    return header.toArray(new String[0]);
}

}




private static List<List<String>> parseNestedArray(String input) {
    try {
        if (input == null || input.trim().isEmpty() || input.equals("[]")) return new ArrayList<>();

        input = input.trim();

        // Fix: remove escaped outer quotes if present
        if ((input.startsWith("\"[") && input.endsWith("]\"")) || 
            (input.startsWith("'[") && input.endsWith("]'"))) {
            input = input.substring(1, input.length() - 1);
        }

        // Handle flat arrays (e.g., ["Bucket", "Bucket"])
        if (!input.contains("[[") && input.startsWith("[") && input.endsWith("]")) {
            List<String> flat = objectMapper.readValue(input, new TypeReference<List<String>>() {});
            List<List<String>> wrapped = new ArrayList<>();
            wrapped.add(flat);
            return wrapped;
        }

        // Handle nested arrays
        return objectMapper.readValue(input, new TypeReference<List<List<String>>>() {});
    } catch (Exception e) {
        e.printStackTrace();
        return new ArrayList<>();
    }
}
