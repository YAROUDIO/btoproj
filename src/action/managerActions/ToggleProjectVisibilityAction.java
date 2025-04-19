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
public class ToggleProjectVisibilityAction implements IAction {

    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        ProjectService projectService = (ProjectService) services.get("project");
        BaseView baseView = (BaseView) views.get("base");

        Project projectToToggle = ManagerActionUtils.selectManagedProject((HDBManager) currentUser, services, views, "toggle visibility for");
        if (projectToToggle == null) return null;

        String newStatus = projectService.toggleProjectVisibility((HDBManager) currentUser, projectToToggle);
        baseView.displayMessage("Project '" + projectToToggle.getProjectName() + "' visibility set to " + newStatus + ".", true);
        return null;
    }
}
