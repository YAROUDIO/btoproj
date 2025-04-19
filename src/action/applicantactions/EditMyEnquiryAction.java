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
class EditMyEnquiryAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, model.User currentUser, Map<String, Object> controllerData) throws Exception {
        EnquiryService enqService = (EnquiryService) services.get("enq");
        EnquiryView enqView = (EnquiryView) views.get("enq");
        BaseView baseView = (BaseView) views.get("base");

        Applicant applicant = (Applicant) currentUser;
        List<model.Enquiry> myEnquiries = enqService.getEnquiriesByApplicant(applicant.getNric());
        List<model.Enquiry> editable = myEnquiries.stream().filter(e -> !e.isReplied()).toList();

        model.Enquiry enquiryToEdit = enqView.selectEnquiry(editable, "edit");
        if (enquiryToEdit == null) return null;

        String newText = enqView.promptEnquiryText(enquiryToEdit.getText());
        if (newText == null || newText.isEmpty()) return null;

        enqService.editEnquiry(applicant, enquiryToEdit, newText);
        baseView.displayMessage("Enquiry ID " + enquiryToEdit.getEnquiryId() + " updated.", false, true, false);
        return null;
    }
}