import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.FlatType;
import common.UserRole;
import exception.OperationError;
import util.InputUtil;
import model.Application;
import model.Enquiry;
import model.Project;
import model.User;
import service.ApplicationService;
import service.EnquiryService;
import service.ProjectService;
import view.ApplicationView;
import view.BaseView;
import view.EnquiryView;
import view.ProjectView;

public class ApplicantController extends BaseRoleController {

    public ApplicantController(User currentUser, Map<String, Object> services, Map<String, Object> views) {
        super(currentUser, services, views);
    }

    @Override
    public void runMenu() {
        BaseView baseView = (BaseView) this.views.get("base");
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("View/Filter Projects", this::handleViewProjects);
        actions.put("Apply for Project", this::handleApplyForProject);
        actions.put("View My Application Status", this::handleViewApplicationStatus);
        actions.put("Request Application Withdrawal", this::handleRequestWithdrawal);
        actions.put("Submit Enquiry", this::handleSubmitEnquiry);
        actions.put("View My Enquiries", this::handleViewMyEnquiries);
        actions.put("Edit My Enquiry", this::handleEditMyEnquiry);
        actions.put("Delete My Enquiry", this::handleDeleteMyEnquiry);
        actions.putAll(getCommonActions());

        // Handle user input for menu selection and run corresponding action
    }

    private void handleViewProjects() {
        handleViewProjectsCommon(UserRole.APPLICANT);
    }

    private void handleApplyForProject() {
        ApplicationService appService = (ApplicationService) this.services.get("app");
        ProjectService projectService = (ProjectService) this.services.get("project");
        ProjectView projectView = (ProjectView) this.views.get("project");
        ApplicationView appView = (ApplicationView) this.views.get("app");
        BaseView baseView = (BaseView) this.views.get("base");

        List<Project> potentialProjects = projectService.getViewableProjectsForApplicant(this.currentUser);
        List<Project> selectableProjects = new ArrayList<>();
        for (Project project : potentialProjects) {
            if (project.isCurrentlyVisibleAndActive()) {
                selectableProjects.add(project);
            }
        }

        Project projectToApply = projectView.selectProject(selectableProjects, "apply for");
        if (projectToApply == null) return;

        FlatType flatType = appView.promptFlatTypeSelection(projectToApply, this.currentUser);
        if (flatType == null) return;

        appService.applyForProject(this.currentUser, projectToApply, flatType);
        baseView.displayMessage("Application submitted successfully for " + flatType.getValue() + "-Room flat in '" + projectToApply.getProjectName() + "'.", false);
    }

    private void handleViewApplicationStatus() {
        ApplicationService appService = (ApplicationService) this.services.get("app");
        ProjectService projectService = (ProjectService) this.services.get("project");
        ApplicationView appView = (ApplicationView) this.views.get("app");
        BaseView baseView = (BaseView) this.views.get("base");

        Application application = appService.findApplicationByApplicant(this.currentUser.getNric());
        if (application == null) {
            baseView.displayMessage("You do not have an active BTO application.");
            List<Application> unsuccessfulApplications = appService.getApplicationsByApplicantStatus(this.currentUser.getNric(), ApplicationStatus.UNSUCCESSFUL);
            if (!unsuccessfulApplications.isEmpty()) {
                baseView.displayMessage("You have past unsuccessful applications:");
                appView.selectApplication(unsuccessfulApplications, "view past");
            }
            return;
        }

        Project project = projectService.findProjectByName(application.getProjectName());
        if (project == null) {
            baseView.displayMessage("Error: Project '" + application.getProjectName() + "' associated with your application not found.", true);
            appView.displayApplicationDetails(application, project, this.currentUser);
            return;
        }

        appView.displayApplicationDetails(application, project, this.currentUser);
    }

    private void handleRequestWithdrawal() {
        ApplicationService appService = (ApplicationService) this.services.get("app");
        BaseView baseView = (BaseView) this.views.get("base");

        Application application = appService.findApplicationByApplicant(this.currentUser.getNric());
        if (application == null) {
            throw new OperationError("You do not have an active BTO application to withdraw.");
        }

        if (InputUtil.getYesNoInput("Confirm request withdrawal for application to '" + application.getProjectName() + "'?")) {
            appService.requestWithdrawal(application);
            baseView.displayMessage("Withdrawal requested. This is pending Manager approval/rejection.", false);
        }
    }

    private void handleSubmitEnquiry() {
        EnquiryService enqService = (EnquiryService) this.services.get("enq");
        ProjectService projectService = (ProjectService) this.services.get("project");
        ApplicationService appService = (ApplicationService) this.services.get("app");
        ProjectView projectView = (ProjectView) this.views.get("project");
        EnquiryView enqView = (EnquiryView) this.views.get("enq");
        BaseView baseView = (BaseView) this.views.get("base");

        Application currentApp = appService.findApplicationByApplicant(this.currentUser.getNric());
        List<Project> viewableProjects = projectService.getViewableProjectsForApplicant(this.currentUser, currentApp);

        Project projectToEnquire = projectView.selectProject(viewableProjects, "submit enquiry for");
        if (projectToEnquire == null) return;

        String text = enqView.promptEnquiryText();
        if (text == null) return;

        enqService.submitEnquiry(this.currentUser, projectToEnquire, text);
        baseView.displayMessage("Enquiry submitted successfully.", false);
    }

    private void handleViewMyEnquiries() {
        EnquiryService enqService = (EnquiryService) this.services.get("enq");
        ProjectService projectService = (ProjectService) this.services.get("project");
        EnquiryView enqView = (EnquiryView) this.views.get("enq");
        BaseView baseView = (BaseView) this.views.get("base");

        List<Enquiry> myEnquiries = enqService.getEnquiriesByApplicant(this.currentUser.getNric());
        if (myEnquiries.isEmpty()) {
            baseView.displayMessage("You have not submitted any enquiries.");
            return;
        }

        baseView.displayMessage("Your Submitted Enquiries:", false);
        for (Enquiry enquiry : myEnquiries) {
            Project project = projectService.findProjectByName(enquiry.getProjectName());
            String projectName = project != null ? project.getProjectName() : "Unknown/Deleted Project (" + enquiry.getProjectName() + ")";
            enqView.displayEnquiryDetails(enquiry, projectName, this.currentUser.getName());
        }
    }

    private void handleEditMyEnquiry() {
        EnquiryService enqService = (EnquiryService) this.services.get("enq");
        EnquiryView enqView = (EnquiryView) this.views.get("enq");
        BaseView baseView = (BaseView) this.views.get("base");

        List<Enquiry> myEnquiries = enqService.getEnquiriesByApplicant(this.currentUser.getNric());
        List<Enquiry> editableEnquiries = new ArrayList<>();
        for (Enquiry enquiry : myEnquiries) {
            if (!enquiry.isReplied()) {
                editableEnquiries.add(enquiry);
            }
        }

        Enquiry enquiryToEdit = enqView.selectEnquiry(editableEnquiries, "edit");
        if (enquiryToEdit == null) return;

        String newText = enqView.promptEnquiryText(enquiryToEdit.getText());
        if (newText == null) return;

        enqService.editEnquiry(this.currentUser, enquiryToEdit, newText);
        baseView.displayMessage("Enquiry ID " + enquiryToEdit.getEnquiryId() + " updated successfully.", false);
    }

    private void handleDeleteMyEnquiry() {
        EnquiryService enqService = (EnquiryService) this.services.get("enq");
        EnquiryView enqView = (EnquiryView) this.views.get("enq");
        BaseView baseView = (BaseView) this.views.get("base");

        List<Enquiry> myEnquiries = enqService.getEnquiriesByApplicant(this.currentUser.getNric());
        List<Enquiry> deletableEnquiries = new ArrayList<>();
        for (Enquiry enquiry : myEnquiries) {
            if (!enquiry.isReplied()) {
                deletableEnquiries.add(enquiry);
            }
        }

        Enquiry enquiryToDelete = enqView.selectEnquiry(deletableEnquiries, "delete");
        if (enquiryToDelete == null) return;

        if (InputUtil.getYesNoInput("Are you sure you want to delete Enquiry ID " + enquiryToDelete.getEnquiryId() + "?")) {
            enqService.deleteEnquiry(this.currentUser, enquiryToDelete);
            baseView.displayMessage("Enquiry ID " + enquiryToDelete.getEnquiryId() + " deleted successfully.", false);
        }
    }
}

