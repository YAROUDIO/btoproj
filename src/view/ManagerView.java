package view;

import model.Registration;
import model.Application;
import model.Project;
import model.User;
import common.FlatType;
import util.InputUtil;
import java.util.HashMap;
import java.util.Map;

public class ManagerView extends BaseView {
    /** 
     * Displays officer registration details in the context of approval/rejection.
     */
    public void displayOfficerRegistrationForApproval(Registration registration, User officer, Project project) {
        System.out.println("\n--- Officer Registration for Review ---");
        Map<String, Object> details = new HashMap<>();
        details.put("Project", project.getProjectName() + " (" + project.getNeighborhood() + ")");
        details.put("Officer", officer.getName() + " (" + officer.getNric() + ")");
        details.put("Current Status", registration.getStatus().toString());
        details.put("Project Officer Slots", project.getOfficerNrics().size() + " / " + project.getOfficerSlot());
        
        this.displayDict("Registration Details", details);
        System.out.println("---------------------------------------");
    }

    /** 
     * Displays application details in the context of approval/rejection.
     */
    public void displayApplicationForApproval(Application application, User applicant, Project project) {
        System.out.println("\n--- Application for Review ---");
        int units = project.getFlatDetails(application.getFlatType())[0]; // [0] = units, [1] = price

        
        Map<String, Object> details = new HashMap<>();
        details.put("Applicant", applicant.getName() + " (" + applicant.getNric() + ")");
        details.put("Project", project.getProjectName());
        details.put("Flat Type", application.getFlatType().toString());
        details.put("Current Status", application.getStatus().toString());
        details.put("Units Remaining (" + application.getFlatType().toString() + ")", String.valueOf(units));
        
        if (application.isRequestWithdrawal()) {
            details.put("** Withdrawal Requested **", "Yes");
        }
        
        this.displayDict("Application Details", details);
        System.out.println("-----------------------------");
    }

    /** 
     * Displays withdrawal request details in the context of approval/rejection.
     */
    public void displayWithdrawalRequestForApproval(Application application, User applicant, Project project) {
        System.out.println("\n--- Withdrawal Request for Review ---");
        Map<String, Object> details = new HashMap<>();
        details.put("Applicant", applicant.getName() + " (" + applicant.getNric() + ")");
        details.put("Project", project.getProjectName());
        details.put("Flat Type", application.getFlatType().toString());
        details.put("Current Status", application.getStatus().toString());
        details.put("** Withdrawal Requested **", "YES");
        
        this.displayDict("Withdrawal Request Details", details);
        System.out.println("------------------------------------");
    }
}

