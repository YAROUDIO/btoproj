package action.applicantactions;
import common.ApplicationStatus;
import exception.OperationError;
import interfaces.IAction;
import model.Applicant;
import model.Application;
import model.Project;
import service.ApplicationService;
import service.EnquiryService;
import service.ProjectService;
import view.ApplicationView;
import view.BaseView;
import view.EnquiryView;
import view.ProjectView;
import util.InputUtil;

import java.util.List;
import java.util.Map;
class ViewMyEnquiriesAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, model.User currentUser, Map<String, Object> controllerData) throws Exception {
        EnquiryService enqService = (EnquiryService) services.get("enq");
        ProjectService projectService = (ProjectService) services.get("project");
        EnquiryView enqView = (EnquiryView) views.get("enq");
        BaseView baseView = (BaseView) views.get("base");

        Applicant applicant = (Applicant) currentUser;
        List<model.Enquiry> myEnquiries = enqService.getEnquiriesByApplicant(applicant.getNric());
        if (myEnquiries.isEmpty()) {
            baseView.displayMessage("You have not submitted any enquiries.", false, false, true);
            return null;
        }

        baseView.displayMessage("Your Submitted Enquiries:", false, true, false);
        for (model.Enquiry enquiry : myEnquiries) {
            Project project = projectService.findProjectByName(enquiry.getProjectName());
            String projectName = (project != null) ? project.getProjectName() : "Unknown/Deleted (" + enquiry.getProjectName() + ")";
            enqView.displayEnquiryDetails(enquiry, projectName, applicant.getName());
        }
        return null;
    }
}
