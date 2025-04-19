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
public class ViewMyOfficerRegistrationsAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        RegistrationService regService = (RegistrationService) services.get("reg");
        ProjectService projectService = (ProjectService) services.get("project");
        OfficerView officerView = (OfficerView) views.get("officer");
        BaseView baseView = (BaseView) views.get("base");

        List<Registration> myRegs = regService.getRegistrationsByOfficer(((HDBOfficer) currentUser).getNric());
        if (myRegs.isEmpty()) {
            baseView.displayMessage("You have no officer registrations.", false, true, false);
            return null;
        }

        baseView.displayMessage("Your Officer Registrations:", false, true, true);
        for (Registration reg : myRegs) {
        	Project project = projectService.findProjectByName(reg.getProjectName())
        	        .orElseThrow(() -> new IntegrityError("Project '" + reg.getProjectName() + "' not found."));

            String projectName = project != null ? project.getProjectName() : "Unknown/Deleted (" + reg.getProjectName() + ")";
            officerView.displayRegistrationDetails(reg, projectName, currentUser.getName());
        }
        return null;
    }
}

