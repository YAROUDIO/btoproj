package interfaces;

import java.util.List;
import java.util.Optional;

import common.RegistrationStatus;
import model.Registration;

public interface IRegistrationRepository extends IBaseRepository<Registration, String> {

    // Finds a specific registration by officer and project
    Optional<Registration> findByOfficerAndProject(String officerNric, String projectName);

    // Finds all registrations for a specific officer
    List<Registration> findByOfficer(String officerNric);

    // Finds registrations for a project, optionally filtered by status
    List<Registration> findByProject(String projectName, RegistrationStatus statusFilter);
}

