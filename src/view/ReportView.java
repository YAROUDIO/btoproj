package view;

import util.InputUtil;
import java.util.*;

public class ReportView extends BaseView {

    public void displayReport(String title, List<Map<String, Object>> reportData, List<String> headers) {
        System.out.println("\n--- " + title + " ---");

        if (reportData.isEmpty()) {
            System.out.println("No data found for this report.");
            System.out.println("-".repeat(title.length() + 6));
            return;
        }

        // Determine column widths
        Map<String, Integer> widths = new HashMap<>();
        for (String header : headers) {
            widths.put(header, header.length());
        }

        for (Map<String, Object> row : reportData) {
            for (String header : headers) {
                String value = String.valueOf(row.getOrDefault(header, ""));
                widths.put(header, Math.max(widths.get(header), value.length()));
            }
        }

        // Print header row
        StringBuilder headerLine = new StringBuilder();
        for (String header : headers) {
            headerLine.append(String.format("%-" + widths.get(header) + "s | ", header));
        }
        System.out.println(headerLine.toString().stripTrailing());
        System.out.println("-".repeat(headerLine.length()));

        // Print data rows
        for (Map<String, Object> row : reportData) {
            StringBuilder rowLine = new StringBuilder();
            for (String header : headers) {
                String value = String.valueOf(row.getOrDefault(header, ""));
                rowLine.append(String.format("%-" + widths.get(header) + "s | ", value));
            }
            System.out.println(rowLine.toString().stripTrailing());
        }

        System.out.println("-".repeat(headerLine.length()));
        System.out.println("Total Records: " + reportData.size());
        System.out.println("-".repeat(title.length() + 6));
    }

    public Map<String, String> promptReportFilters() {
        displayMessage("\n--- Generate Booking Report Filters ---", false, true, false);
        System.out.println("(Leave blank for no filter on that field)");
        Map<String, String> filters = new HashMap<>();

        try {
            String maritalRaw = getInput("Filter by Marital Status (Single/Married)");
            String projectRaw = getInput("Filter by Project Name");
            String flatTypeRaw = getInput("Filter by Flat Type (2/3)");

            if (maritalRaw != null && !maritalRaw.isBlank()) {
                String marital = maritalRaw.trim().toLowerCase();
                if (marital.equals("single") || marital.equals("married")) {
                    filters.put("filter_marital", capitalize(marital));
                } else {
                    displayMessage("Invalid marital status filter. Ignoring.", false, false, true);
                }
            }

            if (projectRaw != null && !projectRaw.isBlank()) {
                filters.put("filter_project_name", projectRaw.trim());
            }

            if (flatTypeRaw != null && !flatTypeRaw.isBlank()) {
                if (flatTypeRaw.equals("2") || flatTypeRaw.equals("3")) {
                    filters.put("filter_flat_type_str", flatTypeRaw.trim());
                } else {
                    displayMessage("Invalid flat type filter. Ignoring.", false, false, true);
                }
            }

        } catch (Exception e) {
            displayMessage("\nFilter input cancelled.", false, true, false);
        }

        return filters;
    }

    private String capitalize(String word) {
        if (word == null || word.isEmpty()) return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase();
    }
}

