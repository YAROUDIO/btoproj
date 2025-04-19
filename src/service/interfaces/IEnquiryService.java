package service.interfaces;

import java.util.List;
import model.Enquiry;
import model.Applicant;
import model.User; // For replier
import model.Project;

public interface IEnquiryService {

    /**
     * Finds an enquiry by its ID.
     * 
     * @param enquiryId The ID of the enquiry.
     * @return The Enquiry object if found, otherwise null.
     */
    Enquiry findEnquiryById(int enquiryId);

    /**
     * Gets all enquiries made by an applicant.
     * 
     * @param applicantNric The NRIC of the applicant.
     * @return A list of Enquiries made by the applicant.
     */
    List<Enquiry> getEnquiriesByApplicant(String applicantNric);

    /**
     * Gets all enquiries for a specific project.
     * 
     * @param projectName The name of the project.
     * @return A list of Enquiries related to the project.
     */
    List<Enquiry> getEnquiriesForProject(String projectName);

    /**
     * Gets all enquiries in the system.
     * 
     * @return A list of all enquiries.
     */
    List<Enquiry> getAllEnquiries();

    /**
     * Submits an enquiry for a specific project by an applicant.
     * 
     * @param applicant The applicant submitting the enquiry.
     * @param project The project for which the enquiry is made.
     * @param text The text of the enquiry.
     * @return The submitted Enquiry object.
     */
    Enquiry submitEnquiry(Applicant applicant, Project project, String text);

    /**
     * Edits an existing enquiry.
     * 
     * @param applicant The applicant who made the enquiry.
     * @param enquiry The enquiry to edit.
     * @param newText The new text for the enquiry.
     */
    void editEnquiry(Applicant applicant, Enquiry enquiry, String newText);

    /**
     * Deletes an enquiry.
     * 
     * @param applicant The applicant who made the enquiry.
     * @param enquiry The enquiry to delete.
     */
    void deleteEnquiry(Applicant applicant, Enquiry enquiry);

    /**
     * Replies to an enquiry.
     * 
     * @param replierUser The user who is replying to the enquiry.
     * @param enquiry The enquiry to reply to.
     * @param replyText The text of the reply.
     */
    void replyToEnquiry(User replierUser, Enquiry enquiry, String replyText);
}
