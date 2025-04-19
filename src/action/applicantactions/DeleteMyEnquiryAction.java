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

class DeleteMyEnquiryAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, model.User currentUser, Map<String, Object> controllerData) throws Exception {
        EnquiryService enqService = (EnquiryService) services.get("enq");
        EnquiryView enqView = (EnquiryView) views.get("enq");
        BaseView baseView = (BaseView) views.get("base");

        Applicant applicant = (Applicant) currentUser;
        List<model.Enquiry> myEnquiries = enqService.getEnquiriesByApplicant(applicant.getNric());
        List<model.Enquiry> deletable = myEnquiries.stream().filter(e -> !e.isReplied()).toList();

        model.Enquiry enquiryToDelete = enqView.selectEnquiry(deletable, "delete");
        if (enquiryToDelete == null) return null;

        if (InputUtil.getYesNoInput("Delete Enquiry ID " + enquiryToDelete.getEnquiryId() + "?")) {
            enqService.deleteEnquiry(applicant, enquiryToDelete);
            baseView.displayMessage("Enquiry ID " + enquiryToDelete.getEnquiryId() + " deleted.", false, true, false);
        }
        return null;
    }
}