package service;

package service;

import common.ApplicationStatus;
import common.FlatType;
import common.UserRole;
import exception.OperationError;
import exception.IntegrityError;
import exception.DataSaveError;
import model.Application;
import model.Applicant;
import model.HDBManager;
import model.Project;
import repository.interfaces.IApplicationRepository;
import repository.interfaces.IUserRepository;
import service.interfaces.IApplicationService;
import service.interfaces.IProjectService;
import service.interfaces.IRegistrationService;
import utils.InputUtil;

import java.util.List;

public class ApplicationService implements IApplicationService {
    private IApplicationRepository appRepo;
    private IProjectService projectService;
    private IRegistrationService regService;
    private IUserRepository userRepo;

    // Constructor
    public ApplicationService(IApplicationRepository applicationRepository, 
                              IProjectService projectService,
                              IRegistrationService registrationService, 
                              IUserRepository userRepository) {
        this.appRepo = applicationRepository;
        this.projectService = projectService;
        this.regService = registrationService;
        this.userRepo = userRepository;
    }

    @Override
    public Application findApplicationByApplicant(String applicantNric) {
        return appRepo.findByApplicantNric(applicantNric);
    }

    @Override
    public Application findBookedApplicationByApplicant(String applicantNric) {
        List<Application> apps = appRepo.findAllByApplicantNric(applicantNric);
        for (Application app : apps) {
            if (app.getStatus() == ApplicationStatus.BOOKED) {
                return app;
            }
        }
        return null;
    }

    @Override
    public List<Application> getAllApplicationsByApplicant(String applicantNric) {
        return appRepo.findAllByApplicantNric(applicantNric);
    }

    @Override
    public List<Application> getApplicationsForProject(String projectName) {
        return appRepo.findByProjectName(projectName);
    }

    @Override
    public List<Application> getAllApplications() {
        return appRepo.getAll();
    }

    // Check if applicant is eligible for the project and flat type
    private void checkApplicantEligibility(Applicant applicant, Project project, FlatType flatType) throws OperationError {
        if (!project.isCurrentlyVisibleAndActive()) {
            throw new OperationError("Project '" + project.getProjectName() + "' is not open for applications.");
        }
        if (findApplicationByApplicant(applicant.getNric()) != null) {
            throw new OperationError("You already have an active BTO application.");
        }
        if (applicant.getRole() == UserRole.HDB_MANAGER) {
            throw new OperationError("HDB Managers cannot apply for BTO projects.");
        }
        if (applicant.getRole() == UserRole.HDB_OFFICER) {
            if (regService.findRegistration(applicant.getNric(), project.getProjectName()) != null) {
                throw new OperationError("You cannot apply for a project you have registered for as an officer.");
            }
        }

        boolean isSingle = applicant.getMaritalStatus().equals("Single");
        boolean isMarried = applicant.getMaritalStatus().equals("Married");
        if (isSingle && (applicant.getAge() < 35 || flatType != FlatType.TWO_ROOM)) {
            throw new OperationError("Single applicants must be >= 35 and can only apply for 2-Room.");
        }
        if (isMarried && applicant.getAge() < 21) {
            throw new OperationError("Married applicants must be at least 21 years old.");
        }
        if (!isSingle && !isMarried) {
            throw new OperationError("Unknown marital status: " + applicant.getMaritalStatus());
        }

        // Check for flat availability
        int units = project.getFlatDetails(flatType);
        if (units <= 0) {
            throw new OperationError("No " + flatType + " units available in '" + project.getProjectName() + "'.");
        }
    }

    // Apply for a project
    @Override
    public Application applyForProject(Applicant applicant, Project project, FlatType flatType) throws OperationError {
        checkApplicantEligibility(applicant, project, flatType);
        Application newApplication = new Application(applicant.getNric(), project.getProjectName(), flatType, ApplicationStatus.PENDING, false);
        try {
            appRepo.add(newApplication);
            return newApplication;
        } catch (IntegrityError e) {
            throw new OperationError("Failed to submit application: " + e.getMessage());
        }
    }

    // Request withdrawal for an application
    @Override
    public void requestWithdrawal(Application application) throws OperationError {
        if (application.isRequestWithdrawal()) {
            throw new OperationError("Withdrawal already requested.");
        }
        try {
            application.setRequestWithdrawal(true);
            appRepo.update(application);
        } catch (OperationError | IntegrityError e) {
            throw new OperationError("Failed to save withdrawal request: " + e.getMessage());
        }
    }

    private boolean managerCanManageApp(HDBManager manager, Application application) {
        Project project = projectService.findProjectByName(application.getProjectName());
        return project != null && project.getManagerNric().equals(manager.getNric());
    }

    // Manager approve application
    @Override
    public void managerApproveApplication(HDBManager manager, Application application) throws OperationError {
        if (!managerCanManageApp(manager, application)) {
            throw new OperationError("You do not manage this project.");
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new OperationError("Application status is not PENDING.");
        }
        if (application.isRequestWithdrawal()) {
            throw new OperationError("Cannot approve application with pending withdrawal request.");
        }

        Project project = projectService.findProjectByName(application.getProjectName());
        if (project == null) {
            throw new IntegrityError("Project '" + application.getProjectName() + "' not found.");
        }

        int units = project.getFlatDetails(application.getFlatType());
        if (units <= 0) {
            application.setStatus(ApplicationStatus.UNSUCCESSFUL);
            appRepo.update(application);
            throw new OperationError("No units available. Application rejected.");
        }

        try {
            application.setStatus(ApplicationStatus.SUCCESSFUL);
            appRepo.update(application);
        } catch (IntegrityError e) {
            throw new OperationError("Failed to save application approval: " + e.getMessage());
        }
    }

    // Manager reject application
    @Override
    public void managerRejectApplication(HDBManager manager, Application application) throws OperationError {
        if (!managerCanManageApp(manager, application)) {
            throw new OperationError("You do not manage this project.");
        }
        if (application.getStatus() != ApplicationStatus.PENDING) {
            throw new OperationError("Application status is not PENDING.");
        }

        try {
            application.setStatus(ApplicationStatus.UNSUCCESSFUL);
            appRepo.update(application);
        } catch (IntegrityError e) {
            throw new OperationError("Failed to save application rejection: " + e.getMessage());
        }
    }

    // Manager approve withdrawal
    @Override
    public void managerApproveWithdrawal(HDBManager manager, Application application) throws OperationError {
        if (!managerCanManageApp(manager, application)) {
            throw new OperationError("You do not manage this project.");
        }
        if (!application.isRequestWithdrawal()) {
            throw new OperationError("No withdrawal request is pending.");
        }

        try {
            application.setStatus(ApplicationStatus.UNSUCCESSFUL);
            application.setRequestWithdrawal(false);
            appRepo.update(application);
        } catch (IntegrityError | OperationError e) {
            throw new OperationError("Failed to process withdrawal approval: " + e.getMessage());
        }
    }

    // Manager reject withdrawal
    @Override
    public void managerRejectWithdrawal(HDBManager manager, Application application) throws OperationError {
        if (!managerCanManageApp(manager, application)) {
            throw new OperationError("You do not manage this project.");
        }
        if (!application.isRequestWithdrawal()) {
            throw new OperationError("No withdrawal request is pending.");
        }

        try {
            application.setRequestWithdrawal(false);
            appRepo.update(application);
        } catch (IntegrityError e) {
            throw new OperationError("Failed to save withdrawal rejection: " + e.getMessage());
        }
    }

    // Officer book flat
    @Override
    public void officerBookFlat(HDBOfficer officer, Application application) throws OperationError {
        Project project = projectService.findProjectByName(application.getProjectName());
        if (project == null) {
            throw new OperationError("Project '" + application.getProjectName() + "' not found.");
        }

        if (!projectService.getHandledProjectNamesForOfficer(officer.getNric()).contains(project.getProjectName())) {
            throw new OperationError("You do not handle project '" + project.getProjectName() + "'.");
        }

        if (application.getStatus() != ApplicationStatus.SUCCESSFUL) {
            throw new OperationError("Application status must be SUCCESSFUL to book.");
        }

        Applicant applicant = (Applicant) userRepo.findUserByNric(application.getApplicantNric());
        if (applicant == null) {
            throw new IntegrityError("Applicant " + application.getApplicantNric() + " not found.");
        }

        boolean unitDecreased = false;
        try {
            if (!project.decreaseUnitCount(application.getFlatType())) {
                application.setStatus(ApplicationStatus.UNSUCCESSFUL);
                appRepo.update(application);
                throw new OperationError("No units available. Application marked unsuccessful.");
            }
            unitDecreased = true;

            projectService.updateProject(project);

            application.setStatus(ApplicationStatus.BOOKED);
            appRepo.update(application);

        } catch (OperationError | IntegrityError e) {
            if (unitDecreased) {
                project.increaseUnitCount(application.getFlatType());
                projectService.updateProject(project);
            }
            throw new OperationError("Booking failed: " + e.getMessage());
        }
    }
}
