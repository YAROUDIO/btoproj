package action.managerActions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import service.ApplicationService;
import service.ProjectService;
import service.RegistrationService;
import service.EnquiryService;
import service.ReportService;
import interfaces.IAction;
import interfaces.IUserRepository;
import view.ProjectView;
import view.ApplicationView;
import view.OfficerView;
import view.ManagerView;
import view.EnquiryView;
import view.ReportView;
import view.BaseView;
import util.InputUtil;
import common.UserRole;
import common.RegistrationStatus;
import common.ApplicationStatus;
import exception.OperationError;
import exception.IntegrityError;
import model.HDBManager;
import model.Project;
import model.Registration;
import model.Application;
import model.Enquiry;
import model.User;
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

