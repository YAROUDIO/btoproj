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
public class CreateProjectAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        ProjectService projectService = (ProjectService) services.get("project");
        ProjectView projectView = (ProjectView) views.get("project");
        BaseView baseView = (BaseView) views.get("base");

        Map<String, String> details = projectView.promptCreateProjectDetails();
        if (details == null) {
            return null;
        }

        Project newProject = projectService.createProject(
            currentUser, details.get("name"), details.get("neighborhood"),
            Integer.parseInt(details.get("n1")), Integer.parseInt(details.get("p1")),
            Integer.parseInt(details.get("n2")), Integer.parseInt(details.get("p2")),
            Integer.parseInt(details.get("od")), Integer.parseInt(details.get("cd")),
            Integer.parseInt(details.get("slot"))
        );
        baseView.displayMessage("Project '" + newProject.getProjectName() + "' created.", true, false, false);
        return null;
    }
}
