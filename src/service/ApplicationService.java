package service;


import common.ApplicationStatus;
import common.FlatType;
import common.UserRole;
import exception.OperationError;
import exception.IntegrityError;
import model.Application;
import model.Applicant;
import model.HDBManager;
import model.Project;
import interfaces.IApplicationRepository;
import service.interfaces.IApplicationService;
import service.interfaces.IProjectService;
import service.interfaces.IRegistrationService;
import interfaces.IUserRepository;
import util.InputUtil;

import java.util.List;
import java.util.Optional;

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

    /**
     * Finds an application by applicant's NRIC.
     * @param applicantNric NRIC of the applicant.
     * @return Application object or null if not found.
     */
    @Override
    public Application findApplicationByApplicant(String applicantNric) {
        // Retrieve the Optional<Application> from the repository
        Optional<Application> optionalApp = appRepo.findByApplicantNric(applicantNric);
        
        // Return the Application object if present, otherwise return null
        return optionalApp.orElse(null); // This is fine, as it will return null if no Application is found.
    }



    /**
     * Finds a booked application by applicant's NRIC.
     * @param applicantNric NRIC of the applicant.
     * @return Application if found, else null.
     */
    @Override
    public Application findBookedApplicationByApplicant(String applicantNric) {
        // Using stream to filter and find the first "BOOKED" application
        return appRepo.findAllByApplicantNric(applicantNric).stream()
                      .filter(app -> app.getStatus() == ApplicationStatus.BOOKED)
                      .findFirst()
                      .orElse(null);  // Returns the first "BOOKED" application or null if not found.
    }

    /**
     * Gets all applications by applicant's NRIC.
     * @param applicantNric NRIC of the applicant.
     * @return List of applications.
     */
    @Override
    public List<Application> getAllApplicationsByApplicant(String applicantNric) {
        return appRepo.findAllByApplicantNric(applicantNric);
    }

    /**
     * Gets all applications for a specific project.
     * @param projectName Name of the project.
     * @return List of applications for the project.
     */
    @Override
    public List<Application> getApplicationsForProject(String projectName) {
        return appRepo.findByProjectName(projectName);
    }

    /**
     * Gets all applications.
     * @return List of all applications.
     */
    @Override
    public List<Application> getAllApplications() {
        return appRepo.getAll();
    }

    /**
     * Checks if the applicant is eligible for a given project and flat type.
     * @param applicant Applicant object.
     * @param project Project object.
     * @param flatType FlatType the applicant is applying for.
     * @throws OperationError if the applicant is not eligible.
     */
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
        int units = project.getFlatDetails(flatType)[0];
        if (units <= 0) {
            throw new OperationError("No " + flatType + " units available in '" + project.getProjectName() + "'.");
        }
    }

    /**
     * Allows an applicant to apply for a project and flat type.
     * @param applicant Applicant object.
     * @param project Project object.
     * @param flatType FlatType the applicant is applying for.
     * @return New application.
     * @throws OperationError if the applicant is not eligible or if the application fails.
     */
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

    /**
     * Requests withdrawal of an application.
     * @param application Application to withdraw.
     * @throws OperationError if withdrawal request has already been made or if saving fails.
     */
    @Override
    public void requestWithdrawal(Application application) throws OperationError {
        if (application.isRequestWithdrawal()) {
            throw new OperationError("Withdrawal already requested.");
        }
        try {
            application.setWithdrawalRequest(true);
            appRepo.update(application);
        } catch (OperationError | IntegrityError e) {
            throw new OperationError("Failed to save withdrawal request: " + e.getMessage());
        }
    }

    private boolean managerCanManageApp(HDBManager manager, Application application) {
        Project project = projectService.findProjectByName(application.getProjectName());
        return project != null && project.getManagerNric().equals(manager.getNric());
    }

    // Implement manager actions for approving/rejecting applications...
}
