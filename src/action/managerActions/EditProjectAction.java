package action.managerActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import service.ApplicationService;
import service.ProjectService;
import service.RegistrationService;
import service.EnquiryService;
import service.ReportService;
import interfaces.IAction;
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
public class EditProjectAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        ProjectService projectService = (ProjectService) services.get("project");
        ProjectView projectView = (ProjectView) views.get("project");
        BaseView baseView = (BaseView) views.get("base");

        Project projectToEdit = ManagerActionUtils.selectManagedProject((HDBManager) currentUser, services, views, "edit");
        if (projectToEdit == null) {
            return null;
        }

        Map<String, String> updates = projectView.promptEditProjectDetails(projectToEdit);
        if (updates == null || updates.isEmpty()) {
            baseView.displayMessage("No changes entered.", false, true, false);
            return null;
        }

        projectService.editProject(currentUser, projectToEdit, updates);
        baseView.displayMessage("Project '" + projectToEdit.getProjectName() + "' updated.", false, true, false);
        return null;
    }
}

