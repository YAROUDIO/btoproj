import java.util.HashMap;
import java.util.Map;

public class ManagerController extends BaseRoleController {
    public ManagerController(User currentUser, Map<String, Object> services, Map<String, Object> views) {
        super(currentUser, services, views);
    }

    @Override
    public void runMenu() {
        BaseView baseView = (BaseView) this.views.get("base");
        Map<String, Runnable> actions = new HashMap<>();
        actions.put("Create Project", this::handleCreateProject);
        actions.put("Edit Project", this::handleEditProject);
        actions.put("Delete Project", this::handleDeleteProject);
        actions.put("Toggle Project Visibility", this::handleToggleVisibility);
        actions.put("View All/Filter Projects", this::handleViewAllProjects);
        actions.put("View My Managed Projects", this::handleViewMyProjects);
        actions.put("--- Officer Management ---", () -> {});
        actions.put("View Officer Registrations (Project)", this::handleViewOfficerRegistrations);
        actions.put("Approve Officer Registration", this::handleApproveOfficerRegistration);
        actions.put("Reject Officer Registration", this::handleRejectOfficerRegistration);
        actions.put("--- Application Management ---", () -> {});
        actions.put("View Applications (Project)", this::handleViewApplications);
        actions.put("Approve Application", this::handleApproveApplication);
        actions.put("Reject Application", this::handleRejectApplication);
        actions.put("Approve Withdrawal Request", this::handleApproveWithdrawal);
        actions.put("Reject Withdrawal Request", this::handleRejectWithdrawal);
        actions.put("--- Reporting & Enquiries ---", () -> {});
        actions.put("Generate Booking Report", this::handleGenerateBookingReport);
        actions.put("View All Enquiries", this::handleViewAllEnquiries);
        actions.put("View/Reply Enquiries (Managed Projects)", this::handleViewReplyEnquiriesManager);
        actions.put("--- General Actions ---", () -> {});
        actions.putAll(getCommonActions());

        // Display menu
        String[] options = actions.keySet().toArray(new String[0]);
        int choiceIndex = baseView.displayMenu("HDB Manager Menu", options);
        if (choiceIndex == -1) return;

        String selectedActionName = options[choiceIndex];
        Runnable actionMethod = actions.get(selectedActionName);

        if (actionMethod != null) {
            try {
                actionMethod.run(); // Execute selected action
            } catch (OperationError | IntegrityError e) {
                baseView.displayMessage(e.getMessage(), true);
            } catch (Exception e) {
                baseView.displayMessage("An unexpected error occurred: " + e.getMessage(), true);
            }
        } else {
            baseView.displayMessage("Invalid menu option selected.", true);
        }

        baseView.pauseForUser(); // Pause before showing menu again
    }

    // Helper for selecting managed project
    private Project selectManagedProject(String actionVerb) {
        ProjectService projectService = (ProjectService) this.services.get("project");
        ProjectView projectView = (ProjectView) this.views.get("project");
        BaseView baseView = (BaseView) this.views.get("base");

        List<Project> myProjects = projectService.getProjectsByManager(this.currentUser.getNric());
        if (myProjects.isEmpty()) {
            baseView.displayMessage("You do not manage any projects.");
            return null;
        }

        return projectView.selectProject(myProjects, actionVerb);
    }

    // Project Management Handlers
    private void handleCreateProject() {
        ProjectService projectService = (ProjectService) this.services.get("project");
        ProjectView projectView = (ProjectView) this.views.get("project");
        BaseView baseView = (BaseView) this.views.get("base");

        Map<String, String> details = projectView.promptCreateProjectDetails();
        if (details == null) return;

        Project newProject = projectService.createProject(this.currentUser, details);
        baseView.displayMessage("Project '" + newProject.getProjectName() + "' created successfully.", false);
    }

    private void handleEditProject() {
        ProjectService projectService = (ProjectService) this.services.get("project");
        ProjectView projectView = (ProjectView) this.views.get("project");
        BaseView baseView = (BaseView) this.views.get("base");

        Project projectToEdit = selectManagedProject("edit");
        if (projectToEdit == null) return;

        Map<String, String> updates = projectView.promptEditProjectDetails(projectToEdit);
        if (updates.isEmpty()) {
            baseView.displayMessage("No changes entered.", false);
            return;
        }

        projectService.editProject(this.currentUser, projectToEdit, updates);
        baseView.displayMessage("Project '" + projectToEdit.getProjectName() + "' updated successfully.", false);
    }

    private void handleDeleteProject() {
        ProjectService projectService = (ProjectService) this.services.get("project");
        BaseView baseView = (BaseView) this.views.get("base");

        Project projectToDelete = selectManagedProject("delete");
        if (projectToDelete == null) return;

        String warningMsg = String.format("WARNING: Deleting project '%s' cannot be undone.\n" +
                "Related applications, registrations, and enquiries will remain but may refer to a deleted project.\n" +
                "Proceed with deletion?", projectToDelete.getProjectName());
        if (InputUtil.getYesNoInput(warningMsg)) {
            projectService.deleteProject(this.currentUser, projectToDelete);
            baseView.displayMessage("Project '" + projectToDelete.getProjectName() + "' deleted.", false);
        } else {
            baseView.displayMessage("Deletion cancelled.", false);
        }
    }

    private void handleToggleVisibility() {
        ProjectService projectService = (ProjectService) this.services.get("project");
        BaseView baseView = (BaseView) this.views.get("base");

        Project projectToToggle = selectManagedProject("toggle visibility for");
        if (projectToToggle == null) return;

        String newStatus = projectService.toggleProjectVisibility(this.currentUser, projectToToggle);
        baseView.displayMessage("Project '" + projectToToggle.getProjectName() + "' visibility set to " + newStatus + ".", false);
    }

    private void handleViewAllProjects() {
        ProjectService projectService = (ProjectService) this.services.get("project");
        ProjectView projectView = (ProjectView) this.views.get("project");
        BaseView baseView = (BaseView) this.views.get("base");

        List<Project> allProjects = projectService.getAllProjects();
        List<Project> filteredProjects = projectService.filterProjects(allProjects, this.userFilters);

        baseView.displayMessage("Current Filters: " + (this.userFilters.isEmpty() ? "None" : this.userFilters), false);
        if (filteredProjects.isEmpty()) {
            baseView.displayMessage("No projects match your criteria.", false);
        } else {
            baseView.displayMessage("Displaying All Projects:", false);
            filteredProjects.forEach(project -> projectView.displayProjectDetails(project, UserRole.HDB_MANAGER));
        }

        if (InputUtil.getYesNoInput("Update filters?")) {
            this.userFilters = projectView.promptProjectFilters(this.userFilters);
            baseView.displayMessage("Filters updated. View projects again to see changes.", false);
        }
    }

    private void handleViewMyProjects() {
        ProjectService projectService = (ProjectService) this.services.get("project");
        ProjectView projectView = (ProjectView) this.views.get("project");
        BaseView baseView = (BaseView) this.views.get("base");

        List<Project> myProjects = projectService.getProjectsByManager(this.currentUser.getNric());
        if (myProjects.isEmpty()) {
            baseView.displayMessage("You are not managing any projects.", false);
            return;
        }

        baseView.displayMessage("Projects You Manage:", false);
        myProjects.forEach(project -> projectView.displayProjectDetails(project, UserRole.HDB_MANAGER));
    }

    // Officer Registration Management Handlers
    private void handleViewOfficerRegistrations() {
        RegistrationService regService = (RegistrationService) this.services.get("reg");
        OfficerView officerView = (OfficerView) this.views.get("officer");
        BaseView baseView = (BaseView) this.views.get("base");

        Project projectToView = selectManagedProject("view officer registrations for");
        if (projectToView == null) return;

        List<Registration> registrations = regService.getRegistrationsForProject(projectToView.getProjectName());
        if (registrations.isEmpty()) {
            baseView.displayMessage("No officer registrations found for project '" + projectToView.getProjectName() + "'.", false);
        } else {
            baseView.displayMessage("Officer Registrations for '" + projectToView.getProjectName() + "':", false);
            officerView.selectRegistration(registrations);
        }
    }

    private void handleApproveOfficerRegistration() {
        RegistrationService regService = (RegistrationService) this.services.get("reg");
        BaseView baseView = (BaseView) this.views.get("base");
        OfficerView officerView = (OfficerView) this.views.get("officer");

        Registration registrationToApprove = selectPendingRegistrationForAction("approve");
        if (registrationToApprove == null) return;

        officerView.displayRegistrationDetails(registrationToApprove);
        if (InputUtil.getYesNoInput("Confirm approval?")) {
            regService.approveOfficerRegistration(this.currentUser, registrationToApprove);
            baseView.displayMessage("Officer registration approved.", false);
        }
    }

    private void handleRejectOfficerRegistration() {
        RegistrationService regService = (RegistrationService) this.services.get("reg");
        BaseView baseView = (BaseView) this.views.get("base");
        OfficerView officerView = (OfficerView) this.views.get("officer");

        Registration registrationToReject = selectPendingRegistrationForAction("reject");
        if (registrationToReject == null) return;

        officerView.displayRegistrationDetails(registrationToReject);
        if (InputUtil.getYesNoInput("Confirm rejection?")) {
            regService.rejectOfficerRegistration(this.currentUser, registrationToReject);
            baseView.displayMessage("Officer registration rejected.", false);
        }
    }

    // Application Management Handlers
    private void handleViewApplications() {
        ApplicationService appService = (ApplicationService) this.services.get("app");
        ApplicationView appView = (ApplicationView) this.views.get("app");
        BaseView baseView = (BaseView) this.views.get("base");

        Project projectToView = selectManagedProject("view applications for");
        if (projectToView == null) return;

        List<Application> applications = appService.getApplicationsForProject(projectToView.getProjectName());
        if (applications.isEmpty()) {
            baseView.displayMessage("No applications found for project '" + projectToView.getProjectName() + "'.", false);
        } else {
            baseView.displayMessage("Applications for '" + projectToView.getProjectName() + "':", false);
            appView.selectApplication(applications);
        }
    }

    private void handleApproveApplication() {
        ApplicationService appService = (ApplicationService) this.services.get("app");
        BaseView baseView = (BaseView) this.views.get("base");
        ApplicationView appView = (ApplicationView) this.views.get("app");

        Application applicationToApprove = selectPendingApplicationForAction("approve");
        if (applicationToApprove == null) return;

        appView.displayApplicationDetails(applicationToApprove);
        if (InputUtil.getYesNoInput("Confirm approval?")) {
            appService.approveApplication(this.currentUser, applicationToApprove);
            baseView.displayMessage("Application approved.", false);
        }
    }

    private void handleRejectApplication() {
        ApplicationService appService = (ApplicationService) this.services.get("app");
        BaseView baseView = (BaseView) this.views.get("base");
        ApplicationView appView = (ApplicationView) this.views.get("app");

        Application applicationToReject = selectPendingApplicationForAction("reject");
        if (applicationToReject == null) return;

        appView.displayApplicationDetails(applicationToReject);
        if (InputUtil.getYesNoInput("Confirm rejection?")) {
            appService.rejectApplication(this.currentUser, applicationToReject);
            baseView.displayMessage("Application rejected.", false);
        }
    }

    private void handleApproveWithdrawal() {
        ApplicationService appService = (ApplicationService) this.services.get("app");
        BaseView baseView = (BaseView) this.views.get("base");

        Application applicationToApprove = selectPendingApplicationForAction("approve withdrawal for");
        if (applicationToApprove == null) return;

        if (InputUtil.getYesNoInput("Confirm approval?")) {
            appService.approveWithdrawal(this.currentUser, applicationToApprove);
            baseView.displayMessage("Withdrawal approved.", false);
        }
    }

    private void handleRejectWithdrawal() {
        ApplicationService appService = (ApplicationService) this.services.get("app");
        BaseView baseView = (BaseView) this.views.get("base");

        Application applicationToReject = selectPendingApplicationForAction("reject withdrawal for");
        if (applicationToReject == null) return;

        if (InputUtil.getYesNoInput("Confirm rejection?")) {
            appService.rejectWithdrawal(this.currentUser, applicationToReject);
            baseView.displayMessage("Withdrawal rejected.", false);
        }
    }

    // Reporting Handlers
    private void handleGenerateBookingReport() {
        ReportService reportService = (ReportService) this.services.get("report");
        ReportView reportView = (ReportView) this.views.get("report");

        Map<String, Object> filters = reportView.promptReportFilters();
        List<Map<String, Object>> reportData = reportService.generateBookingReportData(filters);
        reportView.displayReport("Booking Report", reportData);
    }

    private void handleViewAllEnquiries() {
        EnquiryService enqService = (EnquiryService) this.services.get("enq");
        BaseView baseView = (BaseView) this.views.get("base");

        List<Enquiry> allEnquiries = enqService.getAllEnquiries();
        if (allEnquiries.isEmpty()) {
            baseView.displayMessage("There are no enquiries in the system.", false);
        } else {
            baseView.displayMessage("All System Enquiries:", false);
            allEnquiries.forEach(enquiry -> baseView.displayMessage(enquiry.getText(), false));
        }
    }

    private void handleViewReplyEnquiriesManager() {
        EnquiryService enqService = (EnquiryService) this.services.get("enq");
        BaseView baseView = (BaseView) this.views.get("base");

        List<Enquiry> relevantEnquiries = enqService.getEnquiriesForManagedProjects(this.currentUser.getNric());
        if (relevantEnquiries.isEmpty()) {
            baseView.displayMessage("No enquiries found for the projects you manage.", false);
        } else {
            baseView.displayMessage("Enquiries for Managed Projects:", false);
            relevantEnquiries.forEach(enquiry -> baseView.displayMessage(enquiry.getText(), false));
        }
    }
}
