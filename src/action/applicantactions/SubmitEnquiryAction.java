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
class SubmitEnquiryAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, model.User currentUser, Map<String, Object> controllerData) throws Exception {
        EnquiryService enqService = (EnquiryService) services.get("enq");
        ProjectService projectService = (ProjectService) services.get("project");
        ApplicationService appService = (ApplicationService) services.get("app");
        ProjectView projectView = (ProjectView) views.get("project");
        EnquiryView enqView = (EnquiryView) views.get("enq");
        BaseView baseView = (BaseView) views.get("base");

        Applicant applicant = (Applicant) currentUser;
        Application currentApp = appService.findApplicationByApplicant(applicant.getNric());
        List<Project> viewableProjects = projectService.getViewableProjectsForApplicant(applicant, currentApp);
        Project projectToEnquire = projectView.selectProject(viewableProjects, "submit enquiry for");
        if (projectToEnquire == null) return null;

        String text = enqView.promptEnquiryText();
        if (text == null || text.isEmpty()) return null;

        enqService.submitEnquiry(applicant, projectToEnquire, text);
        baseView.displayMessage("Enquiry submitted successfully.", false, true, false);
        return null;
    }
}
