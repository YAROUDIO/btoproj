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
public class RejectWithdrawalAction implements IAction {

    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        ApplicationService appService = (ApplicationService) services.get("app");
        BaseView baseView = (BaseView) views.get("base");
        ManagerView managerView = (ManagerView) views.get("manager");
        IUserRepository userRepo = (IUserRepository) services.get("user");
        ProjectService projectService = (ProjectService) services.get("project");

        Application appToAction = ManagerActionUtils.selectWithdrawalRequest((HDBManager) currentUser, services, views, "reject withdrawal for");
        if (appToAction == null) return null;

        User applicant = userRepo.findUserByNric(appToAction.getApplicantNric());
        Project project = projectService.findProjectByName(appToAction.getProjectName());
        if (applicant == null || project == null) {
            throw new IntegrityError("Applicant or Project not found.");
        }

        managerView.displayWithdrawalRequestForApproval(appToAction, applicant, project);
        if (InputUtil.getYesNoInput("Reject withdrawal for " + applicant.getName() + " (Project: " + project.getProjectName() + ")?")) {
            appService.managerRejectWithdrawal((HDBManager) currentUser, appToAction);
            baseView.displayMessage("Withdrawal request for " + applicant.getName() + " rejected.", true);
        } else {
            baseView.displayMessage("Rejection cancelled.", true);
        }
        return null;
    }
}
