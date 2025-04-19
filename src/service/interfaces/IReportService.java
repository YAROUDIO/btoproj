package service.interfaces;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IReportService {

    /**
     * Generates data for the booking report based on optional filters.
     * 
     * @param filterProjectName Optional filter for the project name.
     * @param filterFlatTypeStr Optional filter for the flat type.
     * @param filterMarital Optional filter for marital status.
     * @return A list of maps representing the booking report data.
     */
    List<Map<String, Object>> generateBookingReportData(
        Optional<String> filterProjectName,
        Optional<String> filterFlatTypeStr,
        Optional<String> filterMarital
    );
}
