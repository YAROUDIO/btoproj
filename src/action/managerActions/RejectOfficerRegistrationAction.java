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
public class RejectOfficerRegistrationAction implements IAction {

    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        RegistrationService regService = (RegistrationService) services.get("reg");
        BaseView baseView = (BaseView) views.get("base");
        ManagerView managerView = (ManagerView) views.get("manager");
        IUserRepository userRepo = (IUserRepository) services.get("user");
        ProjectService projectService = (ProjectService) services.get("project");

        Registration regToReject = ManagerActionUtils.selectPendingRegistration((HDBManager) currentUser, services, views, "reject");
        if (regToReject == null) return null;

        User officer = userRepo.findUserByNric(regToReject.getOfficerNric());
        Project project = projectService.findProjectByName(regToReject.getProjectName());
        if (officer == null || project == null) {
            throw new IntegrityError("Officer or Project not found.");
        }

        managerView.displayOfficerRegistrationForApproval(regToReject, officer, project);
        if (InputUtil.getYesNoInput("Reject " + officer.getName() + " for '" + project.getProjectName() + "'?")) {
            regService.managerRejectOfficerRegistration((HDBManager) currentUser, regToReject);
            baseView.displayMessage("Registration for " + officer.getName() + " rejected.", true);
        } else {
            baseView.displayMessage("Rejection cancelled.", true);
        }
        return null;
    }
}

