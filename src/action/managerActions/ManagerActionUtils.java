package action.managerActions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import common.RegistrationStatus;
import interfaces.IUserRepository;
import model.Application;
import model.Enquiry;
import model.HDBManager;
import model.Project;
import model.Registration;
import model.User;
import repository.UserRepositoryFacade;
import service.ApplicationService;
import service.EnquiryService;
import service.ProjectService;
import service.RegistrationService;
import view.ApplicationView;
import view.BaseView;
import view.OfficerView;
import view.ProjectView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ManagerActionUtils {

    // Helper function to select a project managed by the manager
    public static Project selectManagedProject(HDBManager manager, ProjectService projectService, ProjectView projectView, BaseView baseView, String actionVerb) {
        List<Project> myProjects = projectService.getProjectsByManager(manager.getNric());
        if (myProjects.isEmpty()) {
            baseView.displayMessage("You do not manage any projects.", false, false, true);
            return null;
        }
        return projectView.selectProject(myProjects, actionVerb);
    }
    
    
    public static Registration selectPendingRegistration(HDBManager manager, Map<String, Object> services, Map<String, Object> views, String actionVerb) {
        RegistrationService regService = (RegistrationService) services.get("reg");
        OfficerView officerView = (OfficerView) views.get("officer");
        BaseView baseView = (BaseView) views.get("base");
        ProjectService projectService = (ProjectService) services.get("project");

        List<Project> myProjects = projectService.getProjectsByManager(manager.getNric());
        List<Registration> pendingRegs = new ArrayList<>();
        for (Project project : myProjects) {
            pendingRegs.addAll(regService.getRegistrationsForProject(project.getProjectName(), RegistrationStatus.PENDING));
        }

        if (pendingRegs.isEmpty()) {
            baseView.displayMessage("No pending officer registrations found for your projects.", false, true, false);
            return null;
        }
        return officerView.selectRegistration(pendingRegs, actionVerb);
    }
    
    
 // Helper function for selecting pending applications
    public static Application selectPendingApplication(HDBManager manager, Map<String, Object> services, Map<String, Object> views, String actionVerb) {
        ApplicationService appService = (ApplicationService) services.get("app");
        IUserRepository userRepo = (IUserRepository) services.get("user");
        ApplicationView appView = (ApplicationView) views.get("app");
        BaseView baseView = (BaseView) views.get("base");
        ProjectService projectService = (ProjectService) services.get("project");

        List<Project> myProjects = projectService.getProjectsByManager(manager.getNric());
        List<Application> pendingApps = new ArrayList<>();
        for (Project project : myProjects) {
            List<Application> apps = appService.getApplicationsForProject(project.getProjectName());
            pendingApps.addAll(apps.stream().filter(app -> app.getStatus() == ApplicationStatus.PENDING && !app.isRequestWithdrawal()).collect(Collectors.toList()));
        }

        if (pendingApps.isEmpty()) {
            baseView.displayMessage("No pending applications found.", false, true, true);
            return null;
        }
        return appView.selectApplication(pendingApps, userRepo, actionVerb);
    }

    // Helper function for selecting applications with withdrawal requests
    public static Application selectWithdrawalRequest(HDBManager manager, Map<String, Object> services, Map<String, Object> views, String actionVerb) {
        ApplicationService appService = (ApplicationService) services.get("app");
        IUserRepository userRepo = (IUserRepository) services.get("user");
        ApplicationView appView = (ApplicationView) views.get("app");
        BaseView baseView = (BaseView) views.get("base");
        ProjectService projectService = (ProjectService) services.get("project");

        List<Project> myProjects = projectService.getProjectsByManager(manager.getNric());
        List<Application> appsWithRequest = new ArrayList<>();
        for (Project project : myProjects) {
            List<Application> apps = appService.getApplicationsForProject(project.getProjectName());
            appsWithRequest.addAll(apps.stream().filter(app -> app.isRequestWithdrawal()).collect(Collectors.toList()));
        }

        if (appsWithRequest.isEmpty()) {
            baseView.displayMessage("No pending withdrawal requests found.");
            return null;
        }
        return appView.selectApplication(appsWithRequest, userRepo, actionVerb);
    }
}

    // Helper function to get enquiries for the manager
    public static List<Enquiry> getEnquiriesForManager(HDBManager manager, EnquiryService enquiryService, ProjectService projectService, UserRepositoryFacade userRepository) {
        Set<String> handledNames = new HashSet<>(projectService.getHandledProjectNamesForManager(manager.getNric()));
        List<Enquiry> relevant = new ArrayList<>();
        if (handledNames.isEmpty()) return relevant;

        for (Enquiry enq : enquiryService.getAllEnquiries()) {
            Optional<String> projectNameOpt = Optional.ofNullable(enq.getProjectName());
            if (projectNameOpt.isPresent() && handledNames.contains(projectNameOpt.get())) {
                Optional<User> applicantOpt = userRepository.findUserByNric(enq.getApplicantNric());
                
                // Use the `orElse` method to provide a fallback value if the applicant is not found
                String applicantName = applicantOpt.map(User::getName).orElse("Unknown");
                
                relevant.add(enq);
            }
        }



        return relevant;
    }

    // Helper function to prepare receipt data for an application
    public static Map<String, String> prepareReceiptData(Application application, Project project, User applicant) {
        Map<String, String> data = new HashMap<>();
        data.put("Applicant Name", applicant.getName());
        data.put("NRIC", applicant.getNric());
        data.put("Age", String.valueOf(applicant.getAge()));
        data.put("Marital Status", applicant.getMaritalStatus());
        data.put("Flat Type Booked", application.getFlatType().toString());
        data.put("Project Name", project.getProjectName());
        data.put("Neighborhood", project.getNeighborhood());
        data.put("Booking Status", application.getStatus().toString());
        return data;
    }
}
