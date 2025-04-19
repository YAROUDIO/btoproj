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
public class DeleteProjectAction implements IAction {

    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        ProjectService projectService = (ProjectService) services.get("project");
        BaseView baseView = (BaseView) views.get("base");

        Project projectToDelete = ManagerActionUtils.selectManagedProject((HDBManager) currentUser, services, views, "delete");
        if (projectToDelete == null) return null;

        String warning = "Delete project '" + projectToDelete.getProjectName() + "'? This cannot be undone. Proceed?";
        if (InputUtil.getYesNoInput(warning)) {
            projectService.deleteProject((HDBManager) currentUser, projectToDelete);
            baseView.displayMessage("Project '" + projectToDelete.getProjectName() + "' deleted.", true, false, false);
        } else {
            baseView.displayMessage("Deletion cancelled.", true, false, false);
        }
        return null;
    }
}
