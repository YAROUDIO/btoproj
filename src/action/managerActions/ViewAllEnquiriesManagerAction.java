package action.managerActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import interfaces.IAction;
import service.ApplicationService;
import service.ProjectService;
import service.RegistrationService;
import service.EnquiryService;
import service.ReportService;
import interfaces.IUserRepository;
import view.ProjectView;
import view.ApplicationView;
import view.OfficerView;
import view.ManagerView;
import view.EnquiryView;
import view.ReportView;
import view.BaseView;
import util.InputUtil;
import common.UserRole;
import common.RegistrationStatus;
import common.ApplicationStatus;
import exception.OperationError;
import exception.IntegrityError;
import model.HDBManager;
import model.Project;
import model.Registration;
import model.Application;
import model.Enquiry;
import model.User;
public class ViewAllEnquiriesManagerAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        EnquiryService enqService = (EnquiryService) services.get("enq");
        IUserRepository userRepo = (IUserRepository) services.get("user");
        ProjectService projectService = (ProjectService) services.get("project");
        EnquiryView enqView = (EnquiryView) views.get("enq");
        BaseView baseView = (BaseView) views.get("base");

        List<Enquiry> allEnquiries = enqService.getAllEnquiries();
        if (allEnquiries.isEmpty()) {
            baseView.displayMessage("There are no enquiries in the system.");
            return null;
        }

        baseView.displayMessage("All System Enquiries:", true);
        for (Enquiry enquiry : allEnquiries) {
            User applicant = userRepo.findUserByNric(enquiry.getApplicantNric()).orElse(null);
            String applicantName = (applicant != null) ? applicant.getName() : "Unknown";
            Project project = projectService.findProjectByName(enquiry.getProjectName()).orElse(null);
            String projectName = (project != null) ? project.getProjectName() : "Unknown/Deleted";
            enqView.displayEnquiryDetails(enquiry, projectName, applicantName);
        }
        return null;
    }
}
