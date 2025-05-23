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
import common.Tuple;
import common.ApplicationStatus;
import exception.OperationError;
import exception.IntegrityError;
import model.HDBManager;
import model.Project;
import model.Registration;
import model.Application;
import model.Enquiry;
import model.User;
import repository.UserRepositoryFacade;

public class ViewReplyEnquiriesManagerAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        // Retrieve necessary services and views from the provided maps
        EnquiryService enqService = (EnquiryService) services.get("enq");
        EnquiryView enqView = (EnquiryView) views.get("enq");
        BaseView baseView = (BaseView) views.get("base");
        ProjectService projectService = (ProjectService) services.get("project");
        UserRepositoryFacade userRepo = (UserRepositoryFacade) services.get("userRepo");

        // Make sure the required services and views are correctly retrieved
        if (enqService == null || enqView == null || baseView == null || projectService == null || userRepo == null) {
            throw new Exception("One or more required services are missing.");
        }

        // Call the method with the required arguments
        List<Tuple<Enquiry, String>> relevantData = ManagerActionUtils.getEnquiriesForManager(
            (HDBManager) currentUser, // Cast currentUser to HDBManager
            enqService,               // Pass EnquiryService
            projectService,           // Pass ProjectService
            userRepo                  // Pass UserRepositoryFacade
        );

        // Check if there are any relevant data and handle accordingly
        if (relevantData.isEmpty()) {
            baseView.displayMessage("No enquiries found for the projects you manage.", false, true, false);
            return null;
        }

        List<Enquiry> unreplied = relevantData.stream()
                .filter(enq -> !enq.getFirst().isReplied())
                .map(Tuple::getFirst)
                .collect(Collectors.toList());

        baseView.displayMessage("Enquiries for Projects You Manage:", true, false, false);
        for (Tuple<Enquiry, String> entry : relevantData) {
            Enquiry enquiry = entry.getFirst();
            String applicantName = entry.getSecond();
            Project project = projectService.findProjectByName(enquiry.getProjectName()).orElse(null);
            String projectName = (project != null) ? project.getProjectName() : "Unknown/Deleted";
            enqView.displayEnquiryDetails(enquiry, projectName, applicantName);
        }

        if (unreplied.isEmpty()) {
            baseView.displayMessage("No unreplied enquiries requiring action.", true, false, false);
            return null;
        }

        if (InputUtil.getYesNoInput("Reply to an unreplied enquiry?")) {
            Enquiry enquiryToReply = enqView.selectEnquiry(unreplied, "reply to");
            if (enquiryToReply != null) {
                String replyText = enqView.promptReplyText();
                if (replyText != null) {
                    enqService.replyToEnquiry((HDBManager) currentUser, enquiryToReply, replyText);
                    baseView.displayMessage("Reply submitted for Enquiry ID " + enquiryToReply.getEnquiryId(), true, false, false);
                }
            }
        }
        return null;
    }
}

