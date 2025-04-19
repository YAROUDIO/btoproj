
package util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.time.format.DateTimeParseException;
public class InputUtil {
    private static final Scanner scanner = new Scanner(System.in);
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final String NRIC_REGEX = "^[STFG]\\d{7}[A-Z]$";
    public static boolean validateNric(String nric) {
        if (nric == null || nric.length() != 9) return false;

        char firstChar = Character.toUpperCase(nric.charAt(0));
        if (firstChar != 'S' && firstChar != 'T') return false;

        String digits = nric.substring(1, 8);
        if (!digits.matches("\\d{7}")) return false;

        char lastChar = nric.charAt(8);
        return Character.isLetter(lastChar);
    }

    // Gets a non-empty string input
    public static String getNonEmptyInput(String prompt) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("ERROR: Input cannot be empty.");
        }
    }

    // Gets an integer input within optional min and max bounds
    public static int getValidIntegerInput(String prompt, Integer minVal, Integer maxVal) {
        while (true) {
            try {
                System.out.print(prompt + ": ");
                String input = scanner.nextLine().trim();
                int value = Integer.parseInt(input);

                boolean outOfRange = (minVal != null && value < minVal) || (maxVal != null && value > maxVal);
                if (outOfRange) {
                    String rangeMsg = "";
                    if (minVal != null && maxVal != null) {
                        rangeMsg = " between " + minVal + " and " + maxVal;
                    } else if (minVal != null) {
                        rangeMsg = " >= " + minVal;
                    } else if (maxVal != null) {
                        rangeMsg = " <= " + maxVal;
                    }
                    System.out.println("ERROR: Input must be an integer" + rangeMsg + ".");
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                System.out.println("ERROR: Invalid input. Please enter an integer.");
            }
        }
    }

    // Overload for no bounds
    public static int getValidIntegerInput(String prompt) {
        return getValidIntegerInput(prompt, null, null);
    }

    // Gets a LocalDate input using the standard format
    public static LocalDate getValidDateInput(String prompt) {
        while (true) {
            System.out.print(prompt + " (" + DateUtil.DATE_FORMAT + "): ");
            String input = scanner.nextLine().trim();
            LocalDate date = DateUtil.parseDate(input);
            if (date != null) {
                return date;
            } else {
                System.out.println("ERROR: Invalid date format. Please use " + DateUtil.DATE_FORMAT + ".");
            }
        }
    }

    // Gets a yes/no confirmation
    public static boolean getYesNoInput(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y")) return true;
            if (input.equals("n")) return false;
            System.out.println("ERROR: Please enter 'y' or 'n'.");
        }
    }
    
    public static String getChoiceInput(String prompt, List<String> validOptions) {
        while (true) {
            System.out.print(prompt + ": ");
            String input = scanner.nextLine().trim().toLowerCase();

            for (String option : validOptions) {
                if (input.equalsIgnoreCase(option)) {
                    return option;
                }
            }

            System.out.println("ERROR: Invalid option. Valid options are: " + String.join(", ", validOptions));
        }
    }

}
