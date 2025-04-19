package interfaces;

import java.util.List;
import java.util.Optional;

import model.Enquiry;

public interface IEnquiryRepository extends IBaseRepository<Enquiry, Integer> {

    // Alias for find_by_key for clarity
    Optional<Enquiry> findById(int enquiryId);

    // Finds all enquiries submitted by a specific applicant
    List<Enquiry> findByApplicant(String applicantNric);

    // Finds all enquiries related to a specific project
    List<Enquiry> findByProject(String projectName);

    // Alias for delete for clarity
    void deleteById(int enquiryId);

    // Gets the next available ID for a new enquiry
    int getNextId();
}

