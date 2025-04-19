package util;

import java.io.*;
import java.util.*;

public class CsvUtil {

    public static List<Map<String, String>> readCsv(String filePath, List<String> headers) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();

        File file = new File(filePath);
        if (!file.exists()) file.createNewFile();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",", -1);
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    row.put(headers.get(i), i < values.length ? values[i].trim() : "");
                }
                records.add(row);
            }
        }
        return records;
    }

    public static void writeCsv(String filePath, List<String> headers, List<Map<String, String>> rows) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Map<String, String> row : rows) {
                List<String> lineValues = new ArrayList<>();
                for (String header : headers) {
                    lineValues.add(row.getOrDefault(header, ""));
                }
                writer.write(String.join(",", lineValues));
                writer.newLine();
            }
        }
    }
}
