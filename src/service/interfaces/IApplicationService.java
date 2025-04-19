package service.interfaces;
import java.util.List;
import java.util.Optional;

import model.Application;
import model.Applicant;
import model.HDBOfficer;
import model.HDBManager;
import model.Project;
import common.FlatType;
import common.Tuple;

public interface IApplicationService {

    /**
     * Find the application by applicant's NRIC.
     */
    Optional<Application> findApplicationByApplicant(String applicantNric);

    /**
     * Find the booked application by applicant's NRIC.
     */
    Optional<Application> findBookedApplicationByApplicant(String applicantNric);

    /**
     * Get all applications by the applicant's NRIC.
     */
    List<Application> getAllApplicationsByApplicant(String applicantNric);

    /**
     * Get all applications for a specific project.
     */
    List<Application> getApplicationsForProject(String projectName);

    /**
     * Get all applications in the system.
     */
    List<Application> getAllApplications();

    /**
     * Apply for a project by an applicant.
     */
    Application applyForProject(Applicant applicant, Project project, FlatType flatType);

    /**
     * Request withdrawal for an application.
     */
    void requestWithdrawal(Application application);

    /**
     * Manager approve application.
     */
    void managerApproveApplication(HDBManager manager, Application application);

    /**
     * Manager reject application.
     */
    void managerRejectApplication(HDBManager manager, Application application);

    /**
     * Manager approve withdrawal request.
     */
    void managerApproveWithdrawal(HDBManager manager, Application application);

    /**
     * Manager reject withdrawal request.
     */
    void managerRejectWithdrawal(HDBManager manager, Application application);

    /**
     * Officer book flat for the applicant.
     */
    Tuple<Project, Applicant> officerBookFlat(HDBOfficer officer, Application application);
}

