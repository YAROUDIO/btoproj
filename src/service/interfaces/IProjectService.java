package service.interfaces;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Date;
import model.Project;
import model.HDBManager;
import model.Applicant;
import model.Application;

public interface IProjectService {

    /**
     * Finds a project by its name.
     * 
     * @param name The name of the project.
     * @return An Optional containing the Project if found, otherwise empty.
     */
    Optional<Project> findProjectByName(String name);

    /**
     * Gets all projects in the system.
     * 
     * @return A list of all projects.
     */
    List<Project> getAllProjects();

    /**
     * Gets all projects managed by a specific manager.
     * 
     * @param managerNric The NRIC of the manager.
     * @return A list of projects managed by the given manager.
     */
    List<Project> getProjectsByManager(String managerNric);

    /**
     * Gets all project names handled by an officer.
     * 
     * @param officerNric The NRIC of the officer.
     * @return A set of project names handled by the officer.
     */
    Set<String> getHandledProjectNamesForOfficer(String officerNric);

    /**
     * Gets the list of viewable projects for an applicant.
     * 
     * @param applicant The applicant.
     * @param currentApplication The current application of the applicant.
     * @return A list of projects that the applicant can view.
     */
    List<Project> getViewableProjectsForApplicant(Applicant applicant, Optional<Application> currentApplication);

    /**
     * Filters projects based on location and flat type.
     * 
     * @param projects The list of projects to filter.
     * @param location The location filter.
     * @param flatTypeStr The flat type filter.
     * @return A filtered list of projects.
     */
    List<Project> filterProjects(List<Project> projects, Optional<String> location, Optional<String> flatTypeStr);

    /**
     * Creates a new project.
     * 
     * @param manager The manager creating the project.
     * @param name The name of the project.
     * @param neighborhood The neighborhood of the project.
     * @param n1 The first number for the project.
     * @param p1 The first price for the project.
     * @param n2 The second number for the project.
     * @param p2 The second price for the project.
     * @param od The opening date of the project.
     * @param cd The closing date of the project.
     * @param slot The slot number for the project.
     * @return The newly created Project object.
     */
    Project createProject(HDBManager manager, String name, String neighborhood, int n1, int p1, int n2, int p2, Date od, Date cd, int slot);

    /**
     * Edits an existing project.
     * 
     * @param manager The manager making the edits.
     * @param project The project to be edited.
     * @param updates A map of the updates to be applied to the project.
     */
    void editProject(HDBManager manager, Project project, Map<String, Object> updates);

    /**
     * Deletes a project.
     * 
     * @param manager The manager requesting the deletion.
     * @param project The project to be deleted.
     */
    void deleteProject(HDBManager manager, Project project);

    /**
     * Toggles the visibility of a project.
     * 
     * @param manager The manager requesting the visibility toggle.
     * @param project The project whose visibility is to be toggled.
     * @return A string indicating the new visibility status ("ON" or "OFF").
     */
    String toggleProjectVisibility(HDBManager manager, Project project);

    /**
     * Adds an officer to a project.
     * 
     * @param project The project to add the officer to.
     * @param officerNric The NRIC of the officer.
     * @return true if the officer was added successfully, false otherwise.
     */
    boolean addOfficerToProject(Project project, String officerNric);

    /**
     * Removes an officer from a project.
     * 
     * @param project The project to remove the officer from.
     * @param officerNric The NRIC of the officer to be removed.
     * @return true if the officer was removed successfully, false otherwise.
     */
    boolean removeOfficerFromProject(Project project, String officerNric);
}
