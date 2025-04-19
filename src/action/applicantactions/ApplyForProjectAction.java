package action.applicantactions;

import interfaces.IAction;
import model.Applicant;
import model.Project;
import model.User;
import service.ApplicationService;
import service.ProjectService;
import view.ApplicationView;
import view.BaseView;
import view.ProjectView;
import java.util.List;
import java.util.Map;

public class ApplyForProjectAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        ProjectService projectService = (ProjectService) services.get("project");
        ApplicationService appService = (ApplicationService) services.get("app");
        ProjectView projectView = (ProjectView) views.get("project");
        ApplicationView appView = (ApplicationView) views.get("app");
        BaseView baseView = (BaseView) views.get("base");

        Applicant applicant = (Applicant) currentUser;
        var currentApp = appService.findApplicationByApplicant(applicant.getNric());
        List<Project> viewableProjects = projectService.getViewableProjectsForApplicant(applicant, currentApp);
        List<Project> selectable = viewableProjects.stream().filter(Project::isCurrentlyVisibleAndActive).toList();

        Project selected = projectView.selectProject(selectable, "apply for");
        if (selected == null) return null;

        var flatType = appView.promptFlatTypeSelection(selected, applicant);
        if (flatType == null) return null;

        appService.applyForProject(applicant, selected, flatType);
        baseView.displayMessage("Application submitted successfully for " + flatType.toString() + " in '" + selected.getProjectName() + "'.", false, true, false);
        return null;
    }
}
