package view;

import util.InputUtil;
import java.util.Scanner;

public class AuthView extends BaseView {
    // Create Scanner instance for reading user input
    private static final Scanner scanner = new Scanner(System.in);

    /** 
     * Prompts for NRIC and password and returns both values
     */
    public String[] promptLogin() {
        this.displayMessage("\n--- Login ---", false, true, false);
        String nric = getInput("Enter NRIC");
        String password = getPassword();
        return new String[]{nric, password};
    }

    /**
     * Prompts for new password and confirmation. Returns new password if they match.
     */
    public String promptChangePassword() {
        this.displayMessage("\n--- Change Password ---", false, true, false);
        
        // Note: Verification of the *current* password should happen in the Action/Controller
        // This view only collects the *new* password details.
        String newPwd = getPassword("Enter your new password");
        String confirmPwd = getPassword("Confirm your new password");

        if (newPwd.isEmpty()) {
            this.displayMessage("New password cannot be empty.", true, false, false);
            return null;
        }
        if (!newPwd.equals(confirmPwd)) {
            this.displayMessage("New passwords do not match.", true, false, false);
            return null;
        }

        // Return the validated new password
        return newPwd;
    }

    /**
     * Helper method to get input from the user.
     */
    public String getInput(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }

    /**
     * Helper method to get a password (could be plain text for simplicity).
     */
    public String getPassword(String prompt) {
        System.out.print(prompt + ": ");
        return scanner.nextLine().trim();
    }
}

