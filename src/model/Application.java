package model;

import common.ApplicationStatus;
import common.Displayable;
import common.FlatType;
import util.InputUtil;
import java.util.HashMap;
import java.util.Map;
import exception.DataLoadError;
import exception.OperationError;

public class Application implements Displayable{

    // Constants for CSV headers
    public static final String[] HEADERS = { 
        "ApplicantNRIC", "ProjectName", "FlatType", "Status", "RequestWithdrawal" 
    };

    private String applicantNric;
    private String projectName;
    private FlatType flatType;
    private ApplicationStatus status;
    private boolean requestWithdrawal;

    // Constructor
    public Application(String applicantNric, String projectName, FlatType flatType, 
                       ApplicationStatus status, boolean requestWithdrawal) {
        if (!InputUtil.validateNric(applicantNric)) {
            throw new IllegalArgumentException("Invalid Applicant NRIC");
        }
        if (projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException("Project Name cannot be empty");
        }
        if (flatType == null) {
            throw new IllegalArgumentException("Invalid FlatType");
        }
        if (status == null) {
            throw new IllegalArgumentException("Invalid ApplicationStatus");
        }

        this.applicantNric = applicantNric;
        this.projectName = projectName;
        this.flatType = flatType;
        this.status = status;
        this.requestWithdrawal = requestWithdrawal;
    }

    // Getters
    public String getApplicantNric() { return applicantNric; }
    public String getProjectName() { return projectName; }
    public FlatType getFlatType() { return flatType; }
    public ApplicationStatus getStatus() { return status; }
    public boolean isRequestWithdrawal() { return requestWithdrawal; }

    // Setters for status and withdrawal request
    public void setStatus(ApplicationStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Invalid status provided.");
        }

        // Allowed state transitions
        Map<ApplicationStatus, ApplicationStatus[]> allowedTransitions = new HashMap<>();
        allowedTransitions.put(ApplicationStatus.PENDING, new ApplicationStatus[] { 
            ApplicationStatus.SUCCESSFUL, ApplicationStatus.UNSUCCESSFUL, ApplicationStatus.PENDING });
        allowedTransitions.put(ApplicationStatus.SUCCESSFUL, new ApplicationStatus[] { 
            ApplicationStatus.BOOKED, ApplicationStatus.UNSUCCESSFUL, ApplicationStatus.SUCCESSFUL });
        allowedTransitions.put(ApplicationStatus.BOOKED, new ApplicationStatus[] { 
            ApplicationStatus.UNSUCCESSFUL, ApplicationStatus.BOOKED });
        allowedTransitions.put(ApplicationStatus.UNSUCCESSFUL, new ApplicationStatus[] { 
            ApplicationStatus.UNSUCCESSFUL });

        // Validate transition
        boolean validTransition = false;
        for (ApplicationStatus validStatus : allowedTransitions.getOrDefault(this.status, new ApplicationStatus[] {})) {
            if (newStatus == validStatus) {
                validTransition = true;
                break;
            }
        }

        if (!validTransition && !(newStatus == ApplicationStatus.UNSUCCESSFUL && requestWithdrawal)) {
            System.out.println("Warning: Potentially invalid status transition: " + this.status + " -> " + newStatus);
        }

        this.status = newStatus;
    }

    public void setWithdrawalRequest(boolean requested) {
        if (requested && !status.equals(ApplicationStatus.PENDING) && !status.equals(ApplicationStatus.SUCCESSFUL) 
            && !status.equals(ApplicationStatus.BOOKED)) {
            throw new OperationError("Cannot request withdrawal for application with status '" + status + "'.");
        }
        this.requestWithdrawal = requested;
    }

    // To CSV method (converts instance data to a map for CSV output)
    public Map<String, String> toCsvDict() {
        Map<String, String> csvData = new HashMap<>();
        csvData.put("ApplicantNRIC", applicantNric);
        csvData.put("ProjectName", projectName);
        csvData.put("FlatType", flatType.toString());
        csvData.put("Status", status.toString());
        csvData.put("RequestWithdrawal", String.valueOf(requestWithdrawal));
        return csvData;
    }

    // From CSV method (static method to create an instance from a CSV dictionary)
    public static Application fromCsvDict(Map<String, String> rowDict) throws DataLoadError {
        try {
            // Convert the string to an integer
            int flatTypeValue = Integer.parseInt(rowDict.get("FlatType")); // Convert to int

            // Now use the integer to get the corresponding FlatType enum
            FlatType flatType = FlatType.fromValue(flatTypeValue);

            // Convert the string to the corresponding enum for ApplicationStatus
            ApplicationStatus status = ApplicationStatus.valueOf(rowDict.get("Status"));

            // Handle the 'RequestWithdrawal' field with a default value of false
            boolean requestWithdrawal = Boolean.parseBoolean(rowDict.getOrDefault("RequestWithdrawal", "false"));

            // Create and return the Application object
            return new Application(
                rowDict.get("ApplicantNRIC"),
                rowDict.get("ProjectName"),
                flatType,
                status,
                requestWithdrawal
            );
        } catch (Exception e) {
            // Handle exceptions by raising a DataLoadError
            throw new DataLoadError("Error creating Application from CSV row: " + rowDict + ". Error: " + e);
        }
    }


    // Equals and hashCode for comparisons
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Application application = (Application) obj;
        return applicantNric.equals(application.applicantNric) && projectName.equals(application.projectName);
    }

    @Override
    public int hashCode() {
        return applicantNric.hashCode() + projectName.hashCode();
    }

    // Method to get a summary for display
    public String getDisplaySummary(String applicantName) {
        String withdrawalStatus = requestWithdrawal ? " (Withdrawal Requested)" : "";
        return "Project: " + projectName + " | Applicant: " + applicantName + " (" + applicantNric + ") | "
                + "Type: " + flatType.toString() + " | Status: " + status + withdrawalStatus;
    }

    public String getDisplaySummary() {
        return "Project: " + projectName + " | Type: " + flatType.toString() + " | Status: " + status;
    }
    

}
