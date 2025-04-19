package model;

import java.util.HashMap;
import java.util.Map;

import exception.DataLoadError;
import exception.OperationError;
import util.InputUtil;

public class Enquiry {

    // Constants for CSV headers
    public static final String[] HEADERS = {
        "EnquiryID", "ApplicantNRIC", "ProjectName", "Text", "Reply"
    };

    private int enquiryId;
    private String applicantNric;
    private String projectName;
    private String text;
    private String reply;

    // Constructor
    public Enquiry(int enquiryId, String applicantNric, String projectName, String text, String reply) {
        if (enquiryId <= 0) {
            throw new IllegalArgumentException("Enquiry ID must be a positive integer.");
        }
        if (!InputUtil.validateNric(applicantNric)) {
            throw new IllegalArgumentException("Invalid Applicant NRIC");
        }
        if (projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException("Project Name cannot be empty");
        }
        if (text == null || text.isEmpty()) {
            throw new IllegalArgumentException("Enquiry text cannot be empty");
        }

        this.enquiryId = enquiryId;
        this.applicantNric = applicantNric;
        this.projectName = projectName;
        this.text = text;
        this.reply = (reply == null) ? "" : reply; // Default reply to empty string if null
    }

    // Getters
    public int getEnquiryId() { return enquiryId; }
    public String getApplicantNric() { return applicantNric; }
    public String getProjectName() { return projectName; }
    public String getText() { return text; }
    public String getReply() { return reply; }

    // Check if the enquiry has been replied to
    public boolean isReplied() {
        return !reply.isEmpty();
    }

    // Setters (State Modifiers)
    public void setText(String newText) {
        if (isReplied()) {
            throw new OperationError("Cannot edit an enquiry that has already been replied to.");
        }
        if (newText == null || newText.isEmpty()) {
            throw new IllegalArgumentException("Enquiry text cannot be empty.");
        }
        this.text = newText;
    }

    public void setReply(String replyText) {
        if (replyText == null || replyText.isEmpty()) {
            throw new IllegalArgumentException("Reply text cannot be empty.");
        }
        this.reply = replyText;
    }

    public void setId(int newId) {
        if (newId <= 0) {
            throw new IllegalArgumentException("New Enquiry ID must be a positive integer.");
        }
        if (this.enquiryId != 0 && this.enquiryId != newId) {
            System.out.println("Warning: Changing existing Enquiry ID from " + this.enquiryId + " to " + newId);
        }
        this.enquiryId = newId;
    }

    // Converts Enquiry to CSV format (returns a Map)
    public Map<String, String> toCsvDict() {
        Map<String, String> csvData = new HashMap<>();
        csvData.put("EnquiryID", String.valueOf(enquiryId));
        csvData.put("ApplicantNRIC", applicantNric);
        csvData.put("ProjectName", projectName);
        csvData.put("Text", text);
        csvData.put("Reply", reply);
        return csvData;
    }

    // Creates an Enquiry object from a CSV map (static method)
    public static Enquiry fromCsvDict(Map<String, String> rowDict) throws DataLoadError {
        try {
            int enquiryId = Integer.parseInt(rowDict.get("EnquiryID"));
            String applicantNric = rowDict.get("ApplicantNRIC");
            String projectName = rowDict.get("ProjectName");
            String text = rowDict.get("Text");
            String reply = rowDict.getOrDefault("Reply", "");

            return new Enquiry(enquiryId, applicantNric, projectName, text, reply);
        } catch (Exception e) {
            throw new DataLoadError("Error creating Enquiry from CSV row: " + rowDict + ". Error: " + e);
        }
    }

    // Equals and hashCode for comparisons
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Enquiry enquiry = (Enquiry) obj;
        return enquiryId == enquiry.enquiryId;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(enquiryId);
    }

    // Method to get a summary for display
    public String getDisplaySummary() {
        String replyStatus = isReplied() ? "Replied" : "Unreplied";
        String textPreview = (text.length() > 50) ? text.substring(0, 47) + "..." : text;
        return String.format("ID: %-4d | Project: %-15s | Status: %-9s | Text: %s", enquiryId, projectName, replyStatus, textPreview);
    }

	public void setEnquiryId(int nextId) {
		enquiryId = nextId;
		
	}
}
