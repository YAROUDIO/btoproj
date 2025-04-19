package action.officerActions;
import java.util.Map;
import java.util.Set;
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
public class RegisterForProjectOfficerAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        RegistrationService regService = (RegistrationService) services.get("reg");
        ProjectService projectService = (ProjectService) services.get("project");
        ApplicationService appService = (ApplicationService) services.get("app");
        ProjectView projectView = (ProjectView) views.get("project");
        BaseView baseView = (BaseView) views.get("base");

        List<Project> allProjects = projectService.getAllProjects();
        Set<String> myRegs = regService.getRegistrationsByOfficer(((HDBOfficer) currentUser).getNric()).stream()
                .map(Registration::getProjectName)
                .collect(Collectors.toSet());
        Set<String> myAppProjects = appService.getAllApplicationsByApplicant(((HDBOfficer) currentUser).getNric()).stream()
                .map(Application::getProjectName)
                .collect(Collectors.toSet());

        List<Project> selectable = allProjects.stream()
                .filter(p -> !myRegs.contains(p.getProjectName()) && !myAppProjects.contains(p.getProjectName()))
                .filter(p -> !p.getManagerNric().equals(((HDBOfficer) currentUser).getNric()))
                .collect(Collectors.toList());

        Project projectToRegister = projectView.selectProject(selectable, "register for as Officer");
        if (projectToRegister == null) return null;

        regService.officerRegisterForProject((HDBOfficer) currentUser, projectToRegister);
        baseView.displayMessage("Registration submitted for '" + projectToRegister.getProjectName() + "'. Pending Manager approval.", true, false, false);
        return null;
    }
}
