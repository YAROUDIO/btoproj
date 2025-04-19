import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfficerController extends ApplicantController {

    public OfficerController(User currentUser, Map<String, Object> services, Map<String, Object> views) {
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

        // Officer-specific actions
        actions.put("Register for Project as Officer", this::handleRegisterForProject);
        actions.put("View My Officer Registrations", this::handleViewMyRegistrations);
        actions.put("View Handled Projects Details", this::handleViewHandledProjects);
        actions.put("View/Reply Enquiries (Handled Projects)", this::handleViewReplyEnquiriesOfficer);
        actions.put("Book Flat for Applicant", this::handleBookFlat);
        actions.put("Generate Booking Receipt", this::handleGenerateReceipt);

        actions.putAll(getCommonActions()); // Add common actions

        // Display menu
        String[] options = actions.keySet().toArray(new String[0]);
        int choiceIndex = baseView.displayMenu("HDB Officer Menu", options);
        if (choiceIndex == -1) return;

        String selectedActionName = options[choiceIndex];
        Runnable actionMethod = actions.get(selectedActionName);

        if (actionMethod != null) {
            try {
                actionMethod.run();
            } catch (OperationError | IntegrityError e) {
                baseView.displayMessage(e.getMessage(), true);
            } catch (Exception e) {
                baseView.displayMessage("An unexpected error occurred: " + e.getMessage(), true);
            }
        } else {
            baseView.displayMessage("Invalid menu option selected.", true);
        }

        baseView.pauseForUser();
    }

    // --- Officer-Specific Action Handlers ---

    private void handleRegisterForProject() {
        RegistrationService regService = (RegistrationService) this.services.get("reg");
        ProjectService projectService = (ProjectService) this.services.get("project");
        ApplicationService appService = (ApplicationService) this.services.get("app");
        ProjectView projectView = (ProjectView) this.views.get("project");
        BaseView baseView = (BaseView) this.views.get("base");

        List<Project> allProjects = projectService.getAllProjects();

        // Filter out projects the officer cannot register for
        List<String> myRegs = regService.getRegistrationsByOfficer(this.currentUser.getNric());
        List<Application> myApps = appService.getApplicationsByApplicantNric(this.currentUser.getNric());
        List<String> myAppProjects = myApps.stream().map(Application::getProjectName).toList();

        List<Project> selectableProjects = allProjects.stream()
            .filter(p -> !myRegs.contains(p.getProjectName()) && 
                        !myAppProjects.contains(p.getProjectName()) && 
                        !p.getManagerNric().equals(this.currentUser.getNric()))
            .toList();

        Project projectToRegister = projectView.selectProject(selectableProjects, "register for as Officer");
        if (projectToRegister == null) return;

        regService.officerRegisterForProject(this.currentUser, projectToRegister);
        baseView.displayMessage("Registration submitted successfully for project '" + projectToRegister.getProjectName() + "'. Pending Manager approval.", false);
    }

    private void handleViewMyRegistrations() {
        RegistrationService regService = (RegistrationService) this.services.get("reg");
        ProjectService projectService = (ProjectService) this.services.get("project");
        OfficerView officerView = (OfficerView) this.views.get("officer");
        BaseView baseView = (BaseView) this.views.get("base");

        List<Registration> myRegistrations = regService.getRegistrationsByOfficer(this.currentUser.getNric());
        if (myRegistrations.isEmpty()) {
            baseView.displayMessage("You have no officer registrations.");
            return;
        }

        baseView.displayMessage("Your Officer Registrations:", false);
        for (Registration reg : myRegistrations) {
            Project project = projectService.findProjectByName(reg.getProjectName());
            String projectName = project != null ? project.getProjectName() : "Unknown/Deleted Project";
            officerView.displayRegistrationDetails(reg, projectName, this.currentUser.getName());
        }
    }

    private void handleViewHandledProjects() {
        ProjectService projectService = (ProjectService) this.services.get("project");
        ProjectView projectView = (ProjectView) this.views.get("project");
        BaseView baseView = (BaseView) this.views.get("base");

        List<String> handledProjectNames = projectService.getHandledProjectNamesForOfficer(this.currentUser.getNric());
        List<Project> handledProjects = projectService.getAllProjects().stream()
                .filter(p -> handledProjectNames.contains(p.getProjectName()))
                .toList();

        if (handledProjects.isEmpty()) {
            baseView.displayMessage("You are not currently assigned to handle any projects.");
            return;
        }

        baseView.displayMessage("Projects You Handle (Assigned):", false);
        handledProjects.sort((p1, p2) -> p1.getProjectName().compareTo(p2.getProjectName()));
        for (Project project : handledProjects) {
            projectView.displayProjectDetails(project, UserRole.HDB_OFFICER, this.currentUser.getMaritalStatus());
        }
    }

    private List<Enquiry> getEnquiriesForHandledProjects() {
        EnquiryService enqService = (EnquiryService) this.services.get("enq");
        ProjectService projectService = (ProjectService) this.services.get("project");

        List<String> handledProjectNames = projectService.getHandledProjectNamesForOfficer(this.currentUser.getNric());
        List<Enquiry> relevantEnquiries = enqService.getAllEnquiries().stream()
            .filter(enq -> handledProjectNames.contains(enq.getProjectName()))
            .toList();

        return relevantEnquiries;
    }

    private void handleViewReplyEnquiriesOfficer() {
        EnquiryService enqService = (EnquiryService) this.services.get("enq");
        EnquiryView enqView = (EnquiryView) this.views.get("enq");
        BaseView baseView = (BaseView) this.views.get("base");

        List<Enquiry> relevantEnquiries = getEnquiriesForHandledProjects();

        if (relevantEnquiries.isEmpty()) {
            baseView.displayMessage("No enquiries found for the projects you handle.");
            return;
        }

        List<Enquiry> unrepliedEnquiries = relevantEnquiries.stream()
            .filter(e -> !e.isReplied())
            .toList();

        baseView.displayMessage("Enquiries for Projects You Handle:", false);
        for (Enquiry enquiry : relevantEnquiries) {
            enqView.displayEnquiryDetails(enquiry, enquiry.getProjectName(), this.currentUser.getName());
        }

        if (unrepliedEnquiries.isEmpty()) {
            baseView.displayMessage("No unreplied enquiries requiring action.", false);
            return;
        }

        if (InputUtil.getYesNoInput("Reply to an unreplied enquiry?")) {
            Enquiry enquiryToReply = enqView.selectEnquiry(unrepliedEnquiries, "reply to");
            if (enquiryToReply != null) {
                String replyText = enqView.promptReplyText();
                if (replyText != null) {
                    enqService.replyToEnquiry(this.currentUser, enquiryToReply, replyText);
                    baseView.displayMessage("Reply submitted successfully for Enquiry ID " + enquiryToReply.getEnquiryId() + ".", false);
                }
            }
        }
    }

    private void handleBookFlat() {
        ApplicationService appService = (ApplicationService) this.services.get("app");
        OfficerView officerView = (OfficerView) this.views.get("officer");
        BaseView baseView = (BaseView) this.views.get("base");

        String applicantNric = officerView.promptApplicantNric("booking flat");
        if (applicantNric == null) return;

        User applicant = appService.findUserByNric(applicantNric);
        if (applicant == null) {
            throw new OperationError("Applicant with NRIC " + applicantNric + " not found.");
        }

        Application application = appService.findApplicationByApplicant(applicantNric);
        if (application == null || application.getStatus() != ApplicationStatus.SUCCESSFUL) {
            throw new OperationError("No active application found for applicant " + applicantNric + ".");
        }

        if (!InputUtil.getYesNoInput("Confirm booking " + application.getFlatType().getValue() + "-Room flat in '" + application.getProjectName() + "' for " + applicant.getName() + "?")) {
            baseView.displayMessage("Booking cancelled.");
            return;
        }

        appService.officerBookFlat(this.currentUser, application);
        baseView.displayMessage("Flat booked successfully! Unit count updated.", false);
        Map<String, String> receiptData = prepareReceiptData(application);
        officerView.displayReceipt(receiptData);
    }

    private void handleGenerateReceipt() {
        ApplicationService appService = (ApplicationService) this.services.get("app");
        OfficerView officerView = (OfficerView) this.views.get("officer");
        BaseView baseView = (BaseView) this.views.get("base");

        String applicantNric = officerView.promptApplicantNric("generating receipt");
        if (applicantNric == null) return;

        User applicant = appService.findUserByNric(applicantNric);
        if (applicant == null) {
            throw new OperationError("Applicant with NRIC " + applicantNric + " not found.");
        }

        Application bookedApp = appService.getBookedApplication(applicantNric);
        if (bookedApp == null) {
            throw new OperationError("No booked application found for NRIC " + applicantNric + ".");
        }

        Map<String, String> receiptData = prepareReceiptData(bookedApp);
        officerView.displayReceipt(receiptData);
    }

    private Map<String, String> prepareReceiptData(Application application) {
        ProjectService projectService = (ProjectService) this.services.get("project");
        Project project = projectService.findProjectByName(application.getProjectName());

        Map<String, String> receiptData = new HashMap<>();
        receiptData.put("Applicant Name", application.getApplicantName());
        receiptData.put("Project Name", project.getProjectName());
        receiptData.put("Flat Type", application.getFlatType().getValue());
        // Add other fields as necessary
        return receiptData;
    }
}
