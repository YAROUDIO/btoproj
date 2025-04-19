package view;

import model.Application;
import model.Project;
import model.User;
import model.Applicant;
import common.FlatType;
import interfaces.IUserRepository;
import util.InputUtil;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class ApplicationView extends BaseView {

    // Method to display brief summary of an application
    public void displayApplicationSummary(Application application, String applicantName) {
        System.out.println(application.getDisplaySummary(applicantName));
    }

    // Method to display detailed information of a specific application
    public void displayApplicationDetails(Application application, Project project, User applicant) {
        System.out.println("\n--- Application Details ---");
        System.out.println("Applicant: " + applicant.getName() + " (" + applicant.getNric() + ")");
        System.out.println("Age: " + applicant.getAge());
        System.out.println("Marital Status: " + applicant.getMaritalStatus());
        System.out.println("Project: " + project.getProjectName() + " (" + project.getNeighborhood() + ")");
        System.out.println("Flat Type Applied For: " + application.getFlatType().toString());
        System.out.println("Application Status: " + application.getStatus());

        if (application.isRequestWithdrawal()) {
            System.out.println("Withdrawal Requested: Yes (Pending Manager Action)");
        }
    }

    // Method to select an application from a list
    public Application selectApplication(List<Application> applications, IUserRepository userRepo, String actionVerb) {
        if (applications.isEmpty()) {
            this.displayMessage("No applications available for selection.", true, false, false);
            return null;
        }

        System.out.println("\n--- Select Application to " + actionVerb + " ---");
        int index = 1;
        for (Application app : applications) {
            Optional<User> applicantOpt = userRepo.findUserByNric(app.getApplicantNric());
            String applicantName = applicantOpt.map(User::getName).orElse("Unknown Applicant");

            System.out.print(index + ". ");
            displayApplicationSummary(app, applicantName);
            index++;
        }

        System.out.println("0. Cancel");
        System.out.println("------------------------------------");

        int choice;
        while (true) {
            choice = InputUtil.getValidIntegerInput("Enter the number of the application (or 0 to cancel)", 0, applications.size());
            if (choice == 0) return null;

            if (choice > 0 && choice <= applications.size()) {
                return applications.get(choice - 1);
            } else {
                displayMessage("Invalid selection.", true, false, false); // error=true, info=false, warning=false
            }
        }
    }

    // Method to prompt the applicant to select a flat type based on eligibility
    public FlatType promptFlatTypeSelection(Project project, Applicant applicant) {
        List<FlatType> availableTypes = new ArrayList<>();
        boolean isSingle = "Single".equals(applicant.getMaritalStatus());
        boolean isMarried = "Married".equals(applicant.getMaritalStatus());

        int units2 = project.getFlatDetails(FlatType.TWO_ROOM)[0];
        int units3 = project.getFlatDetails(FlatType.THREE_ROOM)[0];

        if (units2 > 0 && ((isSingle && applicant.getAge() >= 35) || (isMarried && applicant.getAge() >= 21))) {
            availableTypes.add(FlatType.TWO_ROOM);
        }
        if (units3 > 0 && isMarried && applicant.getAge() >= 21) {
            availableTypes.add(FlatType.THREE_ROOM);
        }

        if (availableTypes.isEmpty()) {
            this.displayMessage("No suitable or available flat types for you in this project.", true, false, false);
            return null;
        } else if (availableTypes.size() == 1) {
            FlatType selectedType = availableTypes.get(0);
            this.displayMessage("Automatically selecting " + selectedType.toString() + " flat (only option).", false, true, false);
            return selectedType;
        } else {
            // Multiple choices, prompt user
            Map<Integer, FlatType> optionMap = new HashMap<>();
            int index = 1;
            for (FlatType type : availableTypes) {
                optionMap.put(index, type);
                System.out.println(index + ". " + type.toString());
                index++;
            }

            int choice;
            while (true) {
                choice = InputUtil.getValidIntegerInput("Select your flat type", 1, availableTypes.size());
                if (optionMap.containsKey(choice)) {
                    return optionMap.get(choice);
                } else {
                    this.displayMessage("Invalid choice. Please select a number from the list.", true, false, false);
                }
            }
        }
    }

    

}

