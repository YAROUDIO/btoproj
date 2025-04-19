package util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateUtil {
    // Constant for date format
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    // Parses a date string into a LocalDate object
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null; // Return null if input is null or empty
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            return LocalDate.parse(dateStr, formatter); // Parse the date string to LocalDate
        } catch (DateTimeParseException e) {
            return null; // Return null if parsing fails
        }
    }

    // Formats a LocalDate object into a string
    public static String formatDate(Object date) {
        if (date instanceof LocalDate) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            return ((LocalDate) date).format(formatter);  // Format LocalDate directly
        } else if (date instanceof java.util.Date) {
            // Handle java.util.Date (you can use SimpleDateFormat or DateTimeFormatter)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
            java.sql.Date sqlDate = (java.sql.Date) date;
            LocalDate localDate = sqlDate.toLocalDate(); // Convert to LocalDate
            return localDate.format(formatter);
        }
        throw new IllegalArgumentException("Unsupported date type");
    
}

    // Checks if two date ranges overlap
    public static boolean datesOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            // If any date is missing, assume no overlap for safety
            return false;
        }
        
        // Ensure start is before end for comparison
        LocalDate s1 = (start1.isBefore(end1) || start1.isEqual(end1)) ? start1 : end1;
        LocalDate e1 = (end1.isBefore(start1) || end1.isEqual(start1)) ? start1 : end1;
        LocalDate s2 = (start2.isBefore(end2) || start2.isEqual(end2)) ? start2 : end2;
        LocalDate e2 = (end2.isBefore(start2) || end2.isEqual(start2)) ? start2 : end2;

        // Overlap occurs if one period starts before the other ends, and ends after the other starts
        return !s1.isAfter(e2) && !s2.isAfter(e1);
    }
}
