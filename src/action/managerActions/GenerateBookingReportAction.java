package action.managerActions;

public class GenerateBookingReportAction implements IAction {
    @Override
    public String execute(Map<String, Object> services, Map<String, Object> views, User currentUser, Map<String, Object> controllerData) throws Exception {
        ReportService reportService = (ReportService) services.get("report");
        ReportView reportView = (ReportView) views.get("report");

        Map<String, String> filters = reportView.promptReportFilters();
        List<Map<String, String>> reportData = reportService.generateBookingReportData(filters);
        List<String> headers = Arrays.asList("NRIC", "Applicant Name", "Age", "Marital Status", "Flat Type", "Project Name", "Neighborhood");

        reportView.displayReport("Booking Report", reportData, headers);
        return null;
    }
}

