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
public class ViewOfficerRegistrationsAction implements IAction {

    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        RegistrationService regService = (RegistrationService) services.get("reg");
        IUserRepository userRepo = (IUserRepository) services.get("user");
        OfficerView officerView = (OfficerView) views.get("officer");
        BaseView baseView = (BaseView) views.get("base");

        Project projectToView = ManagerActionUtils.selectManagedProject((HDBManager) currentUser, services, views, "view officer registrations for");
        if (projectToView == null) return null;

        List<Registration> registrations = regService.getRegistrationsForProject(projectToView.getProjectName());
        if (registrations.isEmpty()) {
            baseView.displayMessage("No officer registrations for '" + projectToView.getProjectName() + "'.");
            return null;
        }

        baseView.displayMessage("Officer Registrations for '" + projectToView.getProjectName() + "':", true);
        officerView.selectRegistration(registrations, userRepo, "view list");
        return null;
    }
}

