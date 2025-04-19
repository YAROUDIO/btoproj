package action.managerActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import interfaces.IAction;
import service.ApplicationService;
import service.ProjectService;
import service.RegistrationService;
import service.EnquiryService;
import service.ReportService;
import interfaces.IUserRepository;
import view.ProjectView;
import view.ApplicationView;
import view.OfficerView;
import view.ManagerView;
import view.EnquiryView;
import view.ReportView;
import view.BaseView;
import util.InputUtil;
import common.UserRole;
import common.RegistrationStatus;
import common.ApplicationStatus;
import exception.OperationError;
import exception.IntegrityError;
import model.HDBManager;
import model.Project;
import model.Registration;
import model.Application;
import model.Enquiry;
import model.User;
public class ViewAllProjectsManagerAction implements IAction {

    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        ProjectService projectService = (ProjectService) services.get("project");
        ProjectView projectView = (ProjectView) views.get("project");
        BaseView baseView = (BaseView) views.get("base");

        Map<String, String> userFilters = controllerData != null ? (Map<String, String>) controllerData.get("filters") : new HashMap<>();
        List<Project> allProjects = projectService.getAllProjects();
        List<Project> filteredProjects = ManagerActionUtils.filterProjects(services, allProjects, userFilters);

        baseView.displayMessage("Current Filters: " + (userFilters.isEmpty() ? "None" : userFilters.toString()), true);
        if (filteredProjects.isEmpty()) {
            baseView.displayMessage("No projects match your criteria.", false, false, true);
        } else {
            baseView.displayMessage("Displaying All Projects:", true, true, false);
            for (Project project : filteredProjects) {
                projectView.displayProjectDetails(project, UserRole.HDB_MANAGER);
            }
        }

        if (InputUtil.getYesNoInput("Update filters?")) {
            Map<String, String> newFilters = projectView.promptProjectFilters(userFilters);
            if (controllerData != null) {
                controllerData.put("filters", newFilters);
            }
            baseView.displayMessage("Filters updated. View projects again.", true);
        }

        return null;
    }
}
