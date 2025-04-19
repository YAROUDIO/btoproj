package service.interfaces;

import java.util.List;
import java.util.Optional;
import model.Registration;
import model.HDBOfficer;
import model.HDBManager;
import model.Project;
import common.RegistrationStatus;

public interface IRegistrationService {

    /**
     * Finds a registration by officer's NRIC and project name.
     * 
     * @param officerNric The NRIC of the officer.
     * @param projectName The name of the project.
     * @return An Optional containing the Registration if found, otherwise empty.
     */
    Optional<Registration> findRegistration(String officerNric, String projectName);

    /**
     * Gets all registrations by officer's NRIC.
     * 
     * @param officerNric The NRIC of the officer.
     * @return A list of all registrations for the officer.
     */
    List<Registration> getRegistrationsByOfficer(String officerNric);

    /**
     * Gets all registrations for a specific project with an optional status filter.
     * 
     * @param projectName The name of the project.
     * @param statusFilter Optional filter for registration status.
     * @return A list of registrations for the project.
     */
    List<Registration> getRegistrationsForProject(String projectName, Optional<RegistrationStatus> statusFilter);

    /**
     * Registers an officer for a project.
     * 
     * @param officer The officer registering for the project.
     * @param project The project the officer is registering for.
     * @return The created Registration object.
     */
    Registration officerRegisterForProject(HDBOfficer officer, Project project);

    /**
     * Approves an officer's registration for a project by the manager.
     * 
     * @param manager The manager approving the registration.
     * @param registration The registration to approve.
     */
    void managerApproveOfficerRegistration(HDBManager manager, Registration registration);

    /**
     * Rejects an officer's registration for a project by the manager.
     * 
     * @param manager The manager rejecting the registration.
     * @param registration The registration to reject.
     */
    void managerRejectOfficerRegistration(HDBManager manager, Registration registration);
}

