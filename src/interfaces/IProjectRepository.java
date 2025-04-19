package interfaces;

import java.util.List;
import java.util.Optional;

import model.Project;

public interface IProjectRepository extends IBaseRepository<Project, String> {

    // Finds a project by its unique name
    Optional<Project> findByName(String name);

    // Finds all projects managed by a specific manager
    List<Project> findByManagerNric(String managerNric);
}
