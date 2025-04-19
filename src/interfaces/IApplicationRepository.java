package interfaces;

import java.util.List;
import java.util.Optional;

import model.Application;

public interface IApplicationRepository extends IBaseRepository<Application, String> {

    // Finds the current non-unsuccessful application for an applicant
    Optional<Application> findByApplicantNric(String nric);

    // Finds all applications (including unsuccessful) for an applicant
    List<Application> findAllByApplicantNric(String nric);

    // Finds all applications associated with a specific project
    List<Application> findByProjectName(String projectName);
}
