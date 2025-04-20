package action.applicantactions;


import common.UserRole;
import interfaces.IAction;
import model.Applicant;
import model.Application;
import model.Project;
import model.User;
import service.ApplicationService;
import service.ProjectService;
import util.InputUtil;
import view.BaseView;
import view.ProjectView;

import java.util.*;

public class ViewProjectsApplicantAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) {
        ProjectService projectService = (ProjectService) services.get("project");
        ApplicationService appService = (ApplicationService) services.get("app");
        ProjectView projectView = (ProjectView) views.get("project");
        BaseView baseView = (BaseView) views.get("base");

        Applicant applicant = (Applicant) currentUser;
        Map<String, Object> userFilters = controllerData != null && controllerData.containsKey("filters")
                ? (Map<String, Object>) controllerData.get("filters")
                : new HashMap<>();

        // ✅ Cast to string if not null
        String locationFilter = userFilters.get("location") != null ? userFilters.get("location").toString() : null;
        String flatTypeFilter = userFilters.get("flat_type_str") != null ? userFilters.get("flat_type_str").toString() : null;

        Application currentApp = appService.findApplicationByApplicant(applicant.getNric());
        List<Project> viewable = projectService.getViewableProjectsForApplicant(applicant, currentApp);
        List<Project> filtered = projectService.filterProjects(viewable, locationFilter, flatTypeFilter);

        baseView.displayMessage("Current Filters: " + (userFilters.isEmpty() ? "None" : userFilters.toString()), false, true, false);

        if (filtered.isEmpty()) {
            baseView.displayMessage("No projects match your criteria or eligibility.", false, false, true);
        } else {
            baseView.displayMessage("Displaying projects you are eligible to view/apply for:", false, true, false);
            for (Project p : filtered) {
                projectView.displayProjectDetails(p, UserRole.APPLICANT, applicant.getMaritalStatus());
            }
        }

        if (InputUtil.getYesNoInput("Update filters?")) {
            Map<String, Object> newFilters = projectView.promptProjectFilters(userFilters);
            if (controllerData != null) controllerData.put("filters", newFilters);
            baseView.displayMessage("Filters updated. View projects again to see changes.", false, true, false);
        }

        return null;
    }
}