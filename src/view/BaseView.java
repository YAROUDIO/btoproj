
package view;
import java.util.List;
import java.util.Map;

import common.Displayable;

public class BaseView {

    /**
     * Displays a formatted message with a prefix based on the message type (error, info, warning).
     * 
     * @param message The message to display
     * @param error   Boolean to determine if it's an error message
     * @param info    Boolean to determine if it's an info message
     * @param warning Boolean to determine if it's a warning message
     */
    public void displayMessage(String message, boolean error, boolean info, boolean warning) {
        String prefix = "";
        if (error) prefix = "ERROR: ";
        else if (warning) prefix = "WARNING: ";
        else if (info) prefix = "INFO: ";
        System.out.println("\n" + prefix + message);
    }

    /**
     * Prompts for basic string input from the user.
     * 
     * @param prompt The prompt message
     * @return The user's input
     */
    public String getInput(String prompt) {
        System.out.print(prompt + ": ");
        return new java.util.Scanner(System.in).nextLine().trim();
    }

    /**
     * Prompts for a password from the user (does not mask input).
     * 
     * @param prompt The prompt message (default: "Enter password")
     * @return The user's password
     */
    public String getPassword() {
        return new java.util.Scanner(System.in).nextLine().trim();
    }

    /**
     * Displays a numbered menu and gets a valid choice (1-based index).
     * 
     * @param title   The title of the menu
     * @param options The list of menu options
     * @return The 1-based index of the selected option or null if no valid selection
     */
    public Integer displayMenu(String title, List<String> options) {
        System.out.println("\n--- " + title + " ---");
        if (options.isEmpty()) {
            System.out.println("No options available.");
            return null;
        }

        // Print the options and handle separators
        List<Integer> validIndices = new java.util.ArrayList<>();
        int offset = 0;
        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);
            if (option.startsWith("---")) { // Handle separators
                System.out.println("  " + option);
                offset++;
            } else {
                System.out.println((i + 1 - offset) + ". " + option);
                validIndices.add(i + 1 - offset);
            }
        }
        System.out.println("--------------------");

        if (validIndices.isEmpty()) { // Only separators were present
            System.out.println("No actionable options available.");
            return null;
        }

        int minChoice = validIndices.get(0);
        int maxChoice = validIndices.get(validIndices.size() - 1);

        // Loop until a valid choice is made
        while (true) {
            Integer choice = getValidIntegerInput("Enter your choice", minChoice, maxChoice);
            if (validIndices.contains(choice)) {
                return choice;
            } else {
                displayMessage("Please select one of the numbered options.", false, false, true);
            }
        }
    }

    /**
     * Helper method to get a valid integer input within a specified range.
     * 
     * @param prompt The prompt message
     * @param minVal The minimum valid value
     * @param maxVal The maximum valid value
     * @return The valid integer input
     */
    public Integer getValidIntegerInput(String prompt, int minVal, int maxVal) {
        Integer input = null;
        while (input == null) {
            try {
                System.out.print(prompt + ": ");
                input = Integer.parseInt(new java.util.Scanner(System.in).nextLine().trim());
                if (input < minVal || input > maxVal) {
                    displayMessage("Please enter a number between " + minVal + " and " + maxVal + ".", false, false, true);
                    input = null;
                }
            } catch (NumberFormatException e) {
                displayMessage("Invalid input. Please enter a number.", false, false, true);
            }
        }
        return input;
    }

    /**
     * Displays a numbered list of items using their string representation.
     * 
     * @param title   The title of the list
     * @param items   The list of items to display
     * @param emptyMessage The message when the list is empty
     */
    public void displayList(String title, List<Object> items, String emptyMessage) {
        System.out.println("\n--- " + title + " ---");
        if (items.isEmpty()) {
            System.out.println(emptyMessage);
        } else {
            for (int i = 0; i < items.size(); i++) {
                String displayStr = items.get(i).toString(); // Directly using toString() method
                // If the item has a custom method for display, use that instead
                if (items.get(i) instanceof Displayable) {
                    displayStr = ((Displayable) items.get(i)).getDisplaySummary();
                }
                System.out.println((i + 1) + ". " + displayStr);
            }
        }
        System.out.println("--------------------");
    }

    /**
     * Displays key-value pairs from a dictionary, aligned.
     * 
     * @param title    The title of the dictionary
     * @param dataDict The dictionary to display
     */
    public void displayDict(String title, Map<String, Object> dataDict) {
        System.out.println("\n--- " + title + " ---");
        if (dataDict.isEmpty()) {
            System.out.println("(No details)");
        } else {
            int maxKeyLen = 0;
            for (String key : dataDict.keySet()) {
                maxKeyLen = Math.max(maxKeyLen, key.length());
            }

            for (Map.Entry<String, Object> entry : dataDict.entrySet()) {
                String value = entry.getValue().toString();
                String[] valueLines = value.split("\n");
                System.out.println("  " + String.format("%-" + maxKeyLen + "s", entry.getKey()) + " : " + valueLines[0]);
                for (int i = 1; i < valueLines.length; i++) {
                    System.out.println("  " + " ".repeat(maxKeyLen + 3) + valueLines[i]);
                }
            }
        }
        System.out.println("-".repeat(title.length() + 6));
    }

    /**
     * Pauses execution until the user presses Enter.
     */
    public void pauseForUser() {
        System.out.print("\nPress Enter to continue...");
        new java.util.Scanner(System.in).nextLine();
    }
}

