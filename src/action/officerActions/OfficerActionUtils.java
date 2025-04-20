package action.officerActions;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import interfaces.IUserRepository;
import service.EnquiryService;
import service.ProjectService;
import model.HDBOfficer;
import model.Enquiry;
import model.User;

public class OfficerActionUtils {

    public static List<Enquiry> getEnquiriesForOfficer(HDBOfficer officer, Map<String, Object> services) {
        EnquiryService enqService = (EnquiryService) services.get("enq");
        ProjectService projectService = (ProjectService) services.get("project");
        IUserRepository userRepo = (IUserRepository) services.get("user");

        List<String> handledNames = projectService.getHandledProjectNamesForOfficer(officer.getNric());
        List<Enquiry> relevant = new ArrayList<>();

        if (!handledNames.isEmpty()) {
            for (Enquiry enq : enqService.getAllEnquiries()) {
                if (handledNames.contains(enq.getProjectName())) {
                    User applicant = userRepo.findUserByNric(enq.getApplicantNric());
                    String applicantName = (applicant != null) ? applicant.getName() : "Unknown";
                    relevant.add(enq); // You can adjust this if needed to include applicantName
                }
            }
        }
        return relevant;
    }
}