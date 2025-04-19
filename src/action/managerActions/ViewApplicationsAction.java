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
public class ViewApplicationsAction implements IAction {

    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        ApplicationService appService = (ApplicationService) services.get("app");
        IUserRepository userRepo = (IUserRepository) services.get("user");
        ApplicationView appView = (ApplicationView) views.get("app");
        BaseView baseView = (BaseView) views.get("base");

        Project projectToView = ManagerActionUtils.selectManagedProject((HDBManager) currentUser, services, views, "view applications for");
        if (projectToView == null) return null;

        List<Application> applications = appService.getApplicationsForProject(projectToView.getProjectName());
        if (applications.isEmpty()) {
            baseView.displayMessage("No applications found for '" + projectToView.getProjectName() + "'.");
            return null;
        }

        baseView.displayMessage("Applications for '" + projectToView.getProjectName() + "':", true);
        appView.selectApplication(applications, userRepo, "view list");
        return null;
    }
}
