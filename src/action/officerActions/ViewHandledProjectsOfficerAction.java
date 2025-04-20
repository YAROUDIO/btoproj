package action.officerActions;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Comparator;

import interfaces.IAction;
import service.ProjectService;
import service.ApplicationService;
import service.RegistrationService;
import service.EnquiryService;
import interfaces.IUserRepository;
import view.ProjectView;
import view.ApplicationView;
import view.OfficerView;
import view.EnquiryView;
import view.BaseView;
import util.InputUtil;
import common.UserRole;
import exception.OperationError;
import exception.IntegrityError;
import model.HDBOfficer;
import model.Project;
import model.Registration;
import model.Enquiry;
import model.Application;
import model.User;
public class ViewHandledProjectsOfficerAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        ProjectService projectService = (ProjectService) services.get("project");
        ProjectView projectView = (ProjectView) views.get("project");
        BaseView baseView = (BaseView) views.get("base");

        List<String> handledNames = projectService.getHandledProjectNamesForOfficer(((HDBOfficer) currentUser).getNric());
        List<Project> handledProjects = projectService.getAllProjects().stream()
                .filter(p -> handledNames.contains(p.getProjectName()))
                .collect(Collectors.toList());

        if (handledProjects.isEmpty()) {
            baseView.displayMessage("You are not currently assigned to handle any projects.", false, true, false);
            return null;
        }

        baseView.displayMessage("Projects You Handle (Assigned):", false, true, false);
        handledProjects.sort(Comparator.comparing(Project::getProjectName)); // Sorting by project name
        for (Project project : handledProjects) {
            projectView.displayProjectDetails(project, UserRole.HDB_OFFICER, ((HDBOfficer) currentUser).getMaritalStatus());
        }
        return null;
    }
}

