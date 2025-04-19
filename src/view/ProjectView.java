package view;

import model.Project;
import common.FlatType;
import common.UserRole;
import util.DateUtil;
import util.InputUtil;

import java.util.*;

public class ProjectView extends BaseView {

    public void displayProjectSummary(Project project) {
        System.out.println(project.getDisplaySummary());
    }

    public void displayProjectDetails(Project project, UserRole role, String applicantMaritalStatus) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("Neighborhood", project.getNeighborhood());
        details.put("Managed by NRIC", project.getManagerNric());
        details.put("Application Period", DateUtil.formatDate(project.getOpeningDate()) + " to " + DateUtil.formatDate(project.getClosingDate()));
        details.put("Visibility", project.isVisibility() ? "ON" : "OFF");

        if (project.isCurrentlyVisibleAndActive()) {
            details.put("Status", "Active & Visible");
        } else if (project.isVisibility()) {
            details.put("Status", "Visible but Inactive/Closed");
        } else {
            details.put("Status", "Hidden");
        }

        int[] flat2 = project.getFlatDetails(FlatType.TWO_ROOM);
        int[] flat3 = project.getFlatDetails(FlatType.THREE_ROOM);

        details.put(FlatType.TWO_ROOM.toString() + " Flats", flat2[0] + " units @ $" + flat2[1]);

        boolean show3Room = role == UserRole.HDB_MANAGER || role == UserRole.HDB_OFFICER ||
                (role == UserRole.APPLICANT && "Married".equals(applicantMaritalStatus));

        if (show3Room) {
            details.put(FlatType.THREE_ROOM.toString() + " Flats", flat3[0] + " units @ $" + flat3[1]);
        } else {
            details.put(FlatType.THREE_ROOM.toString() + " Flats", "(Not applicable/visible for single applicants)");
        }

        if (role == UserRole.HDB_MANAGER || role == UserRole.HDB_OFFICER) {
            int assigned = project.getOfficerNrics().size();
            int available = project.getAvailableOfficerSlots();
            details.put("Officer Slots", assigned + " / " + project.getOfficerSlot() + " (Available: " + available + ")");
            details.put("Assigned Officers (NRIC)", project.getOfficerNrics().isEmpty() ? "None" : String.join(", ", project.getOfficerNrics()));
        }

        this.displayDict("Project Details: " + project.getProjectName(), details);
    }

    public Map<String, Object> promptProjectFilters(Map<String, Object> currentFilters) {
        displayMessage("Current Filters: " + (currentFilters.isEmpty() ? "None" : currentFilters), false, true, false);
        String location = getInput("Filter by Neighborhood (leave blank to keep/remove)");
        String flatType = getInput("Filter by Flat Type (2 or 3, leave blank to keep/remove)");

        Map<String, Object> newFilters = new HashMap<>(currentFilters);

        if (location != null) {
            if (!location.isEmpty()) {
                newFilters.put("location", location);
            } else {
                newFilters.remove("location");
            }
        }

        if (flatType != null) {
            if (flatType.equals("2") || flatType.equals("3")) {
                newFilters.put("flat_type_str", flatType);
            } else if (flatType.isEmpty()) {
                newFilters.remove("flat_type_str");
            } else {
                displayMessage("Invalid flat type filter. Keeping previous.", false, false, true);
            }
        }

        return newFilters;
    }

    public Project selectProject(List<Project> projects, String actionVerb) {
        if (projects.isEmpty()) {
            displayMessage("No projects available for selection.", false, true, false);
            return null;
        }

        System.out.println("\n--- Select Project to " + actionVerb + " ---");
        Map<Integer, Project> projectMap = new LinkedHashMap<>();
        for (int i = 0; i < projects.size(); i++) {
            System.out.print((i + 1) + ". ");
            displayProjectSummary(projects.get(i));
            projectMap.put(i + 1, projects.get(i));
        }
        System.out.println(" 0. Cancel");

        while (true) {
            int choice = InputUtil.getValidIntegerInput("Enter the number of the project (or 0 to cancel)", 0, projects.size());
            if (choice == 0) return null;
            if (projectMap.containsKey(choice)) {
                return projectMap.get(choice);
            } else {
                displayMessage("Invalid selection.", true, false, false);
            }
        }
    }
}
