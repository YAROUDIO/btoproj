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

public class ApproveApplicationAction implements IAction {

    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        ApplicationService appService = (ApplicationService) services.get("app");
        BaseView baseView = (BaseView) views.get("base");
        ManagerView managerView = (ManagerView) views.get("manager");
        IUserRepository userRepo = (IUserRepository) services.get("user");
        ProjectService projectService = (ProjectService) services.get("project");

        Application appToApprove = ManagerActionUtils.selectPendingApplication((HDBManager) currentUser, services, views, "approve");
        if (appToApprove == null) return null;

        User applicant = userRepo.findUserByNric(appToApprove.getApplicantNric());
        Project project = projectService.findProjectByName(appToApprove.getProjectName());
        if (applicant == null || project == null) {
            throw new IntegrityError("Applicant or Project not found.");
        }

        managerView.displayApplicationForApproval(appToApprove, applicant, project);
        if (InputUtil.getYesNoInput("Approve " + applicant.getName() + "'s application for '" + project.getProjectName() + "'?")) {
            appService.managerApproveApplication((HDBManager) currentUser, appToApprove);
            baseView.displayMessage("Application for " + applicant.getName() + " approved (Status: " + appToApprove.getStatus().getValue() + ").", true);
        } else {
            baseView.displayMessage("Approval cancelled.", true, false, false);
        }
        return null;
    }
}

