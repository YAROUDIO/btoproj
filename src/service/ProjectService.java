package service;

import common.UserRole;
import common.FlatType;
import common.RegistrationStatus;
import exception.OperationError;
import exception.IntegrityError;
import model.Project;
import model.HDBManager;
import model.Applicant;
import model.Application;
import repository.interfaces.IProjectRepository;
import repository.interfaces.IRegistrationRepository;
import repository.interfaces.IApplicationRepository;
import service.interfaces.IProjectService;
import utils.InputUtil;
import utils.DateUtil;

import java.util.*;

public class ProjectService implements IProjectService {

    private final IProjectRepository _projectRepo;
    private final IRegistrationRepository _regRepo;

    // Constructor
    public ProjectService(IProjectRepository projectRepository, IRegistrationRepository registrationRepository) {
        this._projectRepo = projectRepository;
        this._regRepo = registrationRepository;  // Needed for officer overlap checks
    }

    @Override
    public Optional<Project> findProjectByName(String name) {
        return Optional.ofNullable(_projectRepo.findByName(name));
    }

    @Override
    public List<Project> getAllProjects() {
        List<Project> projects = _projectRepo.getAll();
        projects.sort(Comparator.comparing(Project::getProjectName)); // Sorting by project name
        return projects;
    }

    @Override
    public List<Project> getProjectsByManager(String managerNric) {
        if (!InputUtil.validateNric(managerNric)) return Collections.emptyList();
        List<Project> projects = new ArrayList<>();
        for (Project project : getAllProjects()) {
            if (project.getManagerNric().equals(managerNric)) {
                projects.add(project);
            }
        }
        projects.sort(Comparator.comparing(Project::getProjectName));
        return projects;
    }

    @Override
    public Set<String> getHandledProjectNamesForOfficer(String officerNric) {
        if (!InputUtil.validateNric(officerNric)) return Collections.emptySet();
        Set<String> handledProjects = new HashSet<>();
        for (Project project : getAllProjects()) {
            if (project.getOfficerNrics().contains(officerNric)) {
                handledProjects.add(project.getProjectName());
            }
        }
        return handledProjects;
    }

    @Override
    public List<Project> getViewableProjectsForApplicant(Applicant applicant, Application currentApplication) {
        List<Project> viewable = new ArrayList<>();
        String appProjName = currentApplication != null ? currentApplication.getProjectName() : null;
        boolean isSingle = applicant.getMaritalStatus().equals("Single");
        boolean isMarried = applicant.getMaritalStatus().equals("Married");

        for (Project project : getAllProjects()) {
            if (project.getProjectName().equals(appProjName)) {
                viewable.add(project);
                continue;
            }
            if (!project.isCurrentlyVisibleAndActive()) continue;

            int units2 = project.getFlatDetails(FlatType.TWO_ROOM).getLeft();
            int units3 = project.getFlatDetails(FlatType.THREE_ROOM).getLeft();
            boolean eligible = false;

            if (isSingle && applicant.getAge() >= 35 && units2 > 0) eligible = true;
            else if (isMarried && applicant.getAge() >= 21 && (units2 > 0 || units3 > 0)) eligible = true;

            if (eligible) viewable.add(project);
        }

        Set<String> uniqueViewable = new HashSet<>();
        viewable.removeIf(project -> !uniqueViewable.add(project.getProjectName()));
        viewable.sort(Comparator.comparing(Project::getProjectName));
        return viewable;
    }

    @Override
    public List<Project> filterProjects(List<Project> projects, String location, String flatTypeStr) {
        List<Project> filtered = new ArrayList<>(projects);
        if (location != null) {
            filtered.removeIf(project -> !project.getNeighborhood().equalsIgnoreCase(location));
        }
        if (flatTypeStr != null) {
            try {
                FlatType flatType = FlatType.fromValue(flatTypeStr);
                filtered.removeIf(project -> project.getFlatDetails(flatType).getLeft() <= 0);
            } catch (IllegalArgumentException e) {
                System.out.println("Warning: Invalid flat type filter '" + flatTypeStr + "'. Ignoring.");
            }
        }
        return filtered;
    }

    private void checkManagerProjectOverlap(String managerNric, Date od, Date cd, String excludeName) {
        for (Project project : getProjectsByManager(managerNric)) {
            if (excludeName != null && project.getProjectName().equals(excludeName)) continue;
            if (project.getOpeningDate() != null && project.getClosingDate() != null) {
                if (DateUtil.datesOverlap(od, cd, project.getOpeningDate(), project.getClosingDate())) {
                    throw new OperationError("Manager handles overlapping project '" + project.getProjectName() + "'");
                }
            }
        }
    }

    @Override
    public Project createProject(HDBManager manager, String name, String neighborhood, int n1, int p1, int n2, int p2, Date od, Date cd, int slot) {
        if (findProjectByName(name).isPresent()) {
            throw new OperationError("Project name '" + name + "' already exists.");
        }
        checkManagerProjectOverlap(manager.getNric(), od, cd, null);

        try {
            Project newProject = new Project(name, neighborhood, n1, p1, n2, p2, od, cd, manager.getNric(), slot, true);
            _projectRepo.add(newProject);
            return newProject;
        } catch (Exception e) {
            throw new OperationError("Failed to create project: " + e.getMessage());
        }
    }

    @Override
    public void editProject(HDBManager manager, Project project, Map<String, Object> updates) {
        if (!project.getManagerNric().equals(manager.getNric())) {
            throw new OperationError("You can only edit projects you manage.");
        }

        String originalName = project.getProjectName();
        String newName = (String) updates.getOrDefault("project_name", originalName);

        if (!newName.equals(originalName) && findProjectByName(newName).isPresent()) {
            throw new OperationError("Project name '" + newName + "' already exists.");
        }

        Date newOd = (Date) updates.getOrDefault("opening_date", project.getOpeningDate());
        Date newCd = (Date) updates.getOrDefault("closing_date", project.getClosingDate());
        if (!newOd.equals(project.getOpeningDate()) || !newCd.equals(project.getClosingDate())) {
            checkManagerProjectOverlap(manager.getNric(), newOd, newCd, originalName);
        }

        try {
            project.updateDetails(updates);
            if (!project.getProjectName().equals(originalName)) {
                _projectRepo.delete(originalName);
                _projectRepo.add(project);
            } else {
                _projectRepo.update(project);
            }
        } catch (Exception e) {
            throw new OperationError("Failed to update project: " + e.getMessage());
        }
    }

    @Override
    public void deleteProject(HDBManager manager, Project project) {
        if (!project.getManagerNric().equals(manager.getNric())) {
            throw new OperationError("You can only delete projects you manage.");
        }
        try {
            _projectRepo.deleteByName(project.getProjectName());
        } catch (Exception e) {
            throw new OperationError("Failed to delete project: " + e.getMessage());
        }
    }

    @Override
    public String toggleProjectVisibility(HDBManager manager, Project project) {
        if (!project.getManagerNric().equals(manager.getNric())) {
            throw new OperationError("You can only toggle visibility for projects you manage.");
        }
        try {
            project.setVisibility(!project.isVisibility());
            _projectRepo.update(project);
            return project.isVisibility() ? "ON" : "OFF";
        } catch (Exception e) {
            project.setVisibility(!project.isVisibility());
            throw new OperationError("Failed to update project visibility: " + e.getMessage());
        }
    }

    @Override
    public boolean addOfficerToProject(Project project, String officerNric) {
        try {
            if (project.addOfficer(officerNric)) {
                _projectRepo.update(project);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new OperationError("Cannot add officer: " + e.getMessage());
        }
    }

    @Override
    public boolean removeOfficerFromProject(Project project, String officerNric) {
        try {
            if (project.removeOfficer(officerNric)) {
                _projectRepo.update(project);
                return true;
            }
            return false;
        } catch (Exception e) {
            throw new OperationError("Failed to update project after removing officer: " + e.getMessage());
        }
    }
}
