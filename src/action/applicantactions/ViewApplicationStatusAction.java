package action.applicantactions;

import common.ApplicationStatus;
import interfaces.IAction;
import model.Applicant;
import model.Application;
import model.Project;
import model.User;
import interfaces.IUserRepository;
import service.ApplicationService;
import service.ProjectService;
import view.ApplicationView;
import view.BaseView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ViewApplicationStatusAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        ApplicationService appService = (ApplicationService) services.get("app");
        ProjectService projectService = (ProjectService) services.get("project");
        ApplicationView appView = (ApplicationView) views.get("app");
        BaseView baseView = (BaseView) views.get("base");
        IUserRepository userRepo = (IUserRepository) services.get("user");

        Applicant applicant = (Applicant) currentUser;
        Application app = appService.findApplicationByApplicant(applicant.getNric());
        if (app == null) {
            baseView.displayMessage("You do not have an active BTO application.", false, false, true);
            List<Application> past = appService.getAllApplicationsByApplicant(applicant.getNric())
                                               .stream()
                                               .filter(a -> a.getStatus() == ApplicationStatus.UNSUCCESSFUL)
                                               .toList();
            if (!past.isEmpty()) {
                baseView.displayMessage("You have past unsuccessful applications:", false, true, false);
                appView.selectApplication(past, userRepo, "view past");
            }
            return null;
        }

        Optional<Project> optionalProject = projectService.findProjectByName(app.getProjectName());
        Project project = optionalProject.orElse(null);
        if (project == null) {
            baseView.displayMessage("Error: Project '" + app.getProjectName() + "' not found.", true, false, false);
            Map<String, String> stringMap = app.toCsvDict(); // assuming this is Map<String, String>
            Map<String, Object> objectMap = new HashMap<>();
            for (Map.Entry<String, String> entry : stringMap.entrySet()) {
                objectMap.put(entry.getKey(), entry.getValue());
            }
            appView.displayDict("Application Data (Project Missing)", objectMap);
            return null;
        }


        appView.displayApplicationDetails(app, project, applicant);
        return null;
    }
}
