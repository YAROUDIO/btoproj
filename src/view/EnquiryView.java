package view;

import model.Enquiry;
import util.InputUtil;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class EnquiryView extends BaseView {
    /** 
     * Displays a brief summary of an enquiry for lists.
     */
    public void displayEnquirySummary(Enquiry enquiry) {
        System.out.println(enquiry.getDisplaySummary());
    }

    /** 
     * Displays full details of an enquiry.
     */
    public void displayEnquiryDetails(Enquiry enquiry, String projectName, String applicantName) {
        Map<String, String> details = new HashMap<>();
        details.put("Enquiry ID", String.valueOf(enquiry.getEnquiryId()));
        details.put("Project", projectName);
        details.put("Submitted by", applicantName + " (" + enquiry.getApplicantNric() + ")");
        details.put("Enquiry Text", enquiry.getText());
        details.put("Reply", enquiry.isReplied() ? enquiry.getReply() : "(No reply yet)");
        
        Map<String, Object> converted = new HashMap<>(details);
        this.displayDict("Enquiry Details", converted);

    }

    /** 
     * Displays a list of enquiries and prompts for selection by ID.
     */
    public Enquiry selectEnquiry(List<Enquiry> enquiries, String actionVerb) {
        if (enquiries.isEmpty()) {
            this.displayMessage("No enquiries available for selection.", true, false, false);
            return null;
        }

        System.out.println("\n--- Select Enquiry (by ID) to " + actionVerb + " ---");
        Map<Integer, Enquiry> enquiryMap = new HashMap<>(); // Map ID to enquiry object
        for (Enquiry enq : enquiries) {
            this.displayEnquirySummary(enq); // Display summary
            enquiryMap.put(enq.getEnquiryId(), enq);
        }
        System.out.println("  ID: 0    | Cancel");
        System.out.println("--------------------------------------------------");

        while (true) {
            int enquiryId = InputUtil.getValidIntegerInput("Enter the ID of the enquiry (or 0 to cancel)");
            if (enquiryId == 0) return null;
            Enquiry selectedEnq = enquiryMap.get(enquiryId);
            if (selectedEnq != null) return selectedEnq;
            this.displayMessage("Invalid enquiry ID.", true, false, false);
        }
    }

    /** 
     * Prompts for enquiry text (new or edit).
     */
    public String promptEnquiryText(String currentText) {
        String prompt = "Enter enquiry text";
        if (currentText != null) {
            String preview = currentText.length() > 30 ? currentText.substring(0, 30) + "..." : currentText;
            prompt = "Enter new enquiry text (current: '" + preview + "')";
        }

        return InputUtil.getNonEmptyInput(prompt);
    }


    /** 
     * Prompts for reply text.
     */
    public String promptReplyText() {
        return InputUtil.getNonEmptyInput("Enter reply text");
    }

}
