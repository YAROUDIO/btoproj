package service;

package service;

import common.UserRole;
import exception.OperationError;
import exception.IntegrityError;
import model.Applicant;
import model.Enquiry;
import model.Project;
import model.User;
import repository.interfaces.IEnquiryRepository;
import repository.interfaces.IApplicationRepository;
import repository.interfaces.IUserRepository;
import service.interfaces.IEnquiryService;
import service.interfaces.IProjectService;

import java.util.List;
import java.util.stream.Collectors;

public class EnquiryService implements IEnquiryService {
    private final IEnquiryRepository _enqRepo;
    private final IProjectService _projectService;
    private final IUserRepository _userRepo;
    private final IApplicationRepository _appRepo;

    // Constructor
    public EnquiryService(IEnquiryRepository enquiryRepository, IProjectService projectService,
                          IUserRepository userRepository, IApplicationRepository applicationRepository) {
        this._enqRepo = enquiryRepository;
        this._projectService = projectService;
        this._userRepo = userRepository;
        this._appRepo = applicationRepository;
    }

    @Override
    public Enquiry findEnquiryById(int enquiryId) {
        return _enqRepo.findById(enquiryId);
    }

    @Override
    public List<Enquiry> getEnquiriesByApplicant(String applicantNric) {
        return _enqRepo.findByApplicant(applicantNric).stream()
                .sorted((e1, e2) -> Integer.compare(e1.getEnquiryId(), e2.getEnquiryId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Enquiry> getEnquiriesForProject(String projectName) {
        return _enqRepo.findByProject(projectName).stream()
                .sorted((e1, e2) -> Integer.compare(e1.getEnquiryId(), e2.getEnquiryId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Enquiry> getAllEnquiries() {
        return _enqRepo.getAll().stream()
                .sorted((e1, e2) -> Integer.compare(e1.getEnquiryId(), e2.getEnquiryId()))
                .collect(Collectors.toList());
    }

    @Override
    public Enquiry submitEnquiry(Applicant applicant, Project project, String text) throws OperationError {
        if (text == null || text.trim().isEmpty()) {
            throw new OperationError("Enquiry text cannot be empty.");
        }

        // Check if applicant can view the project
        var currentApp = _appRepo.findByApplicantNric(applicant.getNric());
        var viewableProjects = _projectService.getViewableProjectsForApplicant(applicant, currentApp);
        if (!viewableProjects.contains(project)) {
            boolean isApplied = currentApp != null && currentApp.getProjectName().equals(project.getProjectName());
            if (!isApplied) {
                throw new OperationError("You cannot submit an enquiry for a project you cannot view.");
            }
        }

        try {
            // Create enquiry with temporary ID 0, repository add assigns correct ID
            Enquiry newEnquiry = new Enquiry(0, applicant.getNric(), project.getProjectName(), text);
            _enqRepo.add(newEnquiry);  // Add assigns ID
            return newEnquiry;
        } catch (ValueError | IntegrityError e) {
            throw new OperationError("Failed to submit enquiry: " + e.getMessage());
        }
    }

    @Override
    public void editEnquiry(Applicant applicant, Enquiry enquiry, String newText) throws OperationError {
        if (!enquiry.getApplicantNric().equals(applicant.getNric())) {
            throw new OperationError("You can only edit your own enquiries.");
        }
        try {
            enquiry.setText(newText); // Model validates state (not replied) and text
            _enqRepo.update(enquiry);
        } catch (OperationError | ValueError | IntegrityError e) {
            throw new OperationError("Failed to update enquiry: " + e.getMessage());
        }
    }

    @Override
    public void deleteEnquiry(Applicant applicant, Enquiry enquiry) throws OperationError {
        if (!enquiry.getApplicantNric().equals(applicant.getNric())) {
            throw new OperationError("You can only delete your own enquiries.");
        }
        if (enquiry.isReplied()) {
            throw new OperationError("Cannot delete a replied enquiry.");
        }

        try {
            _enqRepo.deleteById(enquiry.getEnquiryId());
        } catch (IntegrityError e) {
            throw new OperationError("Failed to delete enquiry: " + e.getMessage());
        }
    }

    @Override
    public void replyToEnquiry(User replierUser, Enquiry enquiry, String replyText) throws OperationError {
        if (replyText == null || replyText.trim().isEmpty()) {
            throw new OperationError("Reply text cannot be empty.");
        }
        if (enquiry.isReplied()) {
            throw new OperationError("This enquiry has already been replied to.");
        }

        Project project = _projectService.findProjectByName(enquiry.getProjectName());
        if (project == null) {
            throw new OperationError("Project '" + enquiry.getProjectName() + "' not found.");
        }

        UserRole userRole = replierUser.getRole();
        boolean canReply = false;
        String roleStr = "";

        if (userRole == UserRole.HDB_MANAGER && project.getManagerNric().equals(replierUser.getNric())) {
            canReply = true;
            roleStr = "Manager";
        } else if (userRole == UserRole.HDB_OFFICER) {
            var handledProjects = _projectService.getHandledProjectNamesForOfficer(replierUser.getNric());
            if (handledProjects.contains(project.getProjectName())) {
                canReply = true;
                roleStr = "Officer";
            }
        }

        if (!canReply) {
            throw new OperationError("You do not have permission to reply to this enquiry.");
        }

        String formattedReply = String.format("[%s - %s]: %s", roleStr, replierUser.getName(), replyText);
        try {
            enquiry.setReply(formattedReply);
            _enqRepo.update(enquiry);
        } catch (ValueError | IntegrityError e) {
            throw new OperationError("Failed to save reply: " + e.getMessage());
        }
    }
}

