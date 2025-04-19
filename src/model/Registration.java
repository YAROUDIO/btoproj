package model;

import util.InputUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import common.RegistrationStatus;
import exception.DataLoadError;
public class Registration {

    // Constants for CSV headers
    public static final String[] HEADERS = {
        "OfficerNRIC", "ProjectName", "Status"
    };

    private String officerNric;
    private String projectName;
    private RegistrationStatus status;

    // Constructor
    public Registration(String officerNric, String projectName, RegistrationStatus status) {
        if (!InputUtil.validateNric(officerNric)) {
            throw new IllegalArgumentException("Invalid Officer NRIC");
        }
        if (projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException("Project Name cannot be empty");
        }
        if (status == null) {
            throw new IllegalArgumentException("Invalid RegistrationStatus");
        }

        this.officerNric = officerNric;
        this.projectName = projectName;
        this.status = status;
    }

    // Getters
    public String getOfficerNric() { return officerNric; }
    public String getProjectName() { return projectName; }
    public RegistrationStatus getStatus() { return status; }

    // --- State Modifiers ---
    public void setStatus(RegistrationStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Invalid status provided.");
        }
        if (this.status != RegistrationStatus.PENDING && newStatus != this.status) {
            System.out.println("Warning: Changing registration status from non-pending state: "
                    + this.status + " -> " + newStatus);
        }
        this.status = newStatus;
    }

    // Converts Registration to CSV format (returns a Map)
    public Map<String, String> toCsvDict() {
        Map<String, String> csvData = new HashMap<>();
        csvData.put("OfficerNRIC", officerNric);
        csvData.put("ProjectName", projectName);
        csvData.put("Status", status.toString());
        return csvData;
    }

    // Creates a Registration object from a CSV map (static method)
    public static Registration fromCsvDict(Map<String, String> rowDict) throws DataLoadError {
        try {
            RegistrationStatus status = RegistrationStatus.valueOf(rowDict.get("Status"));
            return new Registration(
                rowDict.get("OfficerNRIC"),
                rowDict.get("ProjectName"),
                status
            );
        } catch (Exception e) {
            throw new DataLoadError("Error creating Registration from CSV row: " + rowDict + ". Error: " + e);
        }
    }

    // Equals and hashCode for comparisons
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Registration registration = (Registration) obj;
        return officerNric.equals(registration.officerNric) &&
               projectName.equals(registration.projectName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(officerNric, projectName);
    }

    // Method to get a summary for display
    public String getDisplaySummary(String officerName) {
        return String.format("Project: %s | Officer: %s (%s) | Status: %s", projectName, officerName, officerNric, status);
    }
}
