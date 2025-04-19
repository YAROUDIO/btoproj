package view;

import model.Registration;
import model.User;
import interfaces.IUserRepository;
import util.InputUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class OfficerView extends BaseView {
    /** 
     * Displays a brief summary of an officer registration.
     */
    public void displayRegistrationSummary(Registration registration, String officerName) {
        // Use model's display method
        System.out.println(registration.getDisplaySummary(officerName));
    }

    /** 
     * Displays detailed registration information.
     */
    public void displayRegistrationDetails(Registration registration, String projectName, String officerName) {
        Map<String, Object> details = new HashMap<>();
        details.put("Officer", officerName + " (" + registration.getOfficerNric() + ")");
        details.put("Project", projectName); // Use provided name
        details.put("Registration Status", registration.getStatus().toString());

        // Assuming displayDict method is inherited from BaseView
        this.displayDict("Officer Registration Details", details);
    }

    /** 
     * Displays a list of registrations and prompts the user to select one.
     */
    public Registration selectRegistration(List<Registration> registrations, IUserRepository userRepo, String actionVerb) {
        if (registrations.isEmpty()) {
            this.displayMessage("No registrations available for selection.", true, false, false);
            return null;
        }

        System.out.println("\n--- Select Registration to " + actionVerb + " ---");
        Map<Integer, Registration> regMap = new HashMap<>();
        for (int i = 0; i < registrations.size(); i++) {
            Registration reg = registrations.get(i);
            Optional<User> officerOpt = userRepo.findUserByNric(reg.getOfficerNric());
            String officerName = officerOpt.map(User::getName).orElse("Unknown Officer");

            System.out.print((i + 1) + ". ");
            displayRegistrationSummary(reg, officerName);
            regMap.put(i + 1, reg);
        }

        System.out.println("0. Cancel");
        System.out.println("------------------------------------");

        while (true) {
            int choice = InputUtil.getValidIntegerInput("Enter the number of the registration (or 0 to cancel)", 0, registrations.size());
            if (choice == 0) {
                return null;
            }
            Registration selectedReg = regMap.get(choice);
            if (selectedReg != null) {
                return selectedReg;
            }
            this.displayMessage("Invalid selection.", true, false, false);
        }
    }

    /** 
     * Prompts for an applicant's NRIC for a specific purpose.
     */
    public String promptApplicantNric(String purpose) {
        while (true) {
            try {
                String nric = this.getInput("Enter Applicant's NRIC for " + purpose + " (or type 'cancel')");
                if (nric.equalsIgnoreCase("cancel")) {
                    return null;
                }
                if (InputUtil.validateNric(nric)) {
                    return nric;
                } else {
                    this.displayMessage("Invalid NRIC format. Please try again.", true, false, false);
                }
            } catch (Exception e) {
                this.displayMessage("\nInput cancelled.", true, false, false);
                return null;
            }
        }
    }

    /** 
     * Displays the booking receipt details.
     */
    public void displayReceipt(Map<String, Object> receiptData) {
        this.displayDict("Booking Receipt", receiptData);
    }
}

