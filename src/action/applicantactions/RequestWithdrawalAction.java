package action.applicantactions;

import common.ApplicationStatus;
import exception.OperationError;
import interfaces.IAction;
import model.Applicant;
import model.Application;
import model.Project;
import service.ApplicationService;
import service.EnquiryService;
import service.ProjectService;
import view.ApplicationView;
import view.BaseView;
import view.EnquiryView;
import view.ProjectView;
import util.InputUtil;

import java.util.List;
import java.util.Map;

public class RequestWithdrawalAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, model.User currentUser, Map<String, Object> controllerData) throws Exception {
        ApplicationService appService = (ApplicationService) services.get("app");
        BaseView baseView = (BaseView) views.get("base");

        Applicant applicant = (Applicant) currentUser;
        Application application = appService.findApplicationByApplicant(applicant.getNric());
        if (application == null) {
            throw new OperationError("You do not have an active BTO application to withdraw.");
        }

        String prompt = "Confirm request withdrawal for application to '" + application.getProjectName() + "'? (Status: " + application.getStatus().toString() + ")";
        if (InputUtil.getYesNoInput(prompt)) {
            appService.requestWithdrawal(application);
            baseView.displayMessage("Withdrawal requested. Pending Manager action.", false, true, false);
        }
        return null;
    }
}