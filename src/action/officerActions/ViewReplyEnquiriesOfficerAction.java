package action.officerActions;


import java.util.Map;
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
public class ViewReplyEnquiriesOfficerAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        EnquiryService enqService = (EnquiryService) services.get("enq");
        EnquiryView enqView = (EnquiryView) views.get("enq");
        BaseView baseView = (BaseView) views.get("base");

        List<Enquiry> relevantData = OfficerActionUtils.getEnquiriesForOfficer((HDBOfficer) currentUser, services);
        if (relevantData.isEmpty()) {
            baseView.displayMessage("No enquiries found for the projects you handle.", false, false, true);
            return null;
        }

        List<Enquiry> unreplied = relevantData.stream()
                .filter(e -> !e.isReplied())
                .collect(Collectors.toList());

        baseView.displayMessage("Enquiries for Projects You Handle:", false, true, false);
        for (Enquiry enquiry : relevantData) {
            enqView.displayEnquiryDetails(enquiry, enquiry.getProjectName(), currentUser.getName());
        }

        if (unreplied.isEmpty()) {
            baseView.displayMessage("No unreplied enquiries requiring action.", false, true, false);
            return null;
        }

        if (InputUtil.getYesNoInput("Reply to an unreplied enquiry?")) {
            Enquiry enquiryToReply = enqView.selectEnquiry(unreplied, "reply to");
            if (enquiryToReply != null) {
                String replyText = enqView.promptReplyText();
                if (!replyText.isEmpty()) {
                    enqService.replyToEnquiry((HDBOfficer) currentUser, enquiryToReply, replyText);
                    baseView.displayMessage("Reply submitted for Enquiry ID " + enquiryToReply.getEnquiryId(), false, true, false);
                }
            }
        }
        return null;
    }
}

