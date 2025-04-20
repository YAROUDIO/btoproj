package service;


import common.UserRole;
import exception.OperationError;
import exception.DataLoadError;
import exception.DataSaveError;
import exception.IntegrityError;
import model.Applicant;
import model.Application;
import model.Enquiry;
import model.Project;
import model.User;
import interfaces.IEnquiryRepository;
import interfaces.IApplicationRepository;
import interfaces.IUserRepository;
import service.interfaces.IEnquiryService;
import service.interfaces.IProjectService;

import java.util.List;
import java.util.Optional;
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
        Optional<Enquiry> optionalEnquiry = _enqRepo.findById(enquiryId);
        return optionalEnquiry.orElse(null);  // Return the Enquiry if present, otherwise return null
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
        var currentApp = _appRepo.findByApplicantNric(applicant.getNric());  // This is Optional<Application>
        var viewableProjects = _projectService.getViewableProjectsForApplicant(applicant, currentApp);  // Pass Optional<Application> directly

        // Check if the project is in viewable projects
        if (currentApp.isPresent()) {
            Application application = currentApp.get();  // Unwrap the Optional<Application>
            boolean isApplied = application.isForProject(project);  // Now call isForProject() on Application
            if (!isApplied) {
                throw new OperationError("You cannot submit an enquiry for a project you cannot view.");
            }
        } else {
            throw new OperationError("You have no application for this project.");
        }

        try {
            // Create an enquiry with a temporary ID 0; the repository will assign the actual ID
            int newEnquiryId = _enqRepo.getNextId(); // This method should return the next available ID
            Enquiry newEnquiry = new Enquiry(newEnquiryId, applicant.getNric(), project.getProjectName(), text, ""); // Default empty reply

            // Add the new enquiry to the repository (ID will be assigned here)
            _enqRepo.add(newEnquiry);
            
            // Return the created enquiry
            return newEnquiry;
        } catch (DataSaveError | IntegrityError e) {
            // Handle database save or integrity errors
            throw new OperationError("Failed to submit enquiry: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            throw new OperationError("An unexpected error occurred while submitting the enquiry: " + e.getMessage());
        }
    }


    @Override
    public void editEnquiry(Applicant applicant, Enquiry enquiry, String newText) throws OperationError {
        // Check if the applicant is the owner of the enquiry
        if (!enquiry.getApplicantNric().equals(applicant.getNric())) {
            throw new OperationError("You can only edit your own enquiries.");
        }

        // Ensure the enquiry has not been replied to
        if (enquiry.getReply() != null && !enquiry.getReply().isEmpty()) {
            throw new OperationError("You cannot edit an enquiry that has already been replied to.");
        }

        try {
            // Update the enquiry text, with the model validating any business logic (like reply validation)
            enquiry.setText(newText);

            // Update the enquiry in the repository
            _enqRepo.update(enquiry);
        } catch (IntegrityError | DataSaveError | DataLoadError e) {
            // Catch errors related to integrity issues, data save or load problems
            throw new OperationError("Failed to update enquiry: " + e.getMessage());
        } catch (Exception e) {
            // Handle unexpected errors
            throw new OperationError("An unexpected error occurred while updating the enquiry: " + e.getMessage());
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

        Optional<Project> optionalProject = _projectService.findProjectByName(enquiry.getProjectName());
        if (!optionalProject.isPresent()) {
            throw new OperationError("Project '" + enquiry.getProjectName() + "' not found.");
        }
        Project project = optionalProject.get();


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
         } catch (IntegrityError e) {
            // Handle IntegrityError specifically
            throw new OperationError("Failed to save reply due to integrity issue: " + e.getMessage());
        } catch (OperationError e) {
            // Catch the OperationError, which is more general
            throw new OperationError("Operation failed while saving reply: " + e.getMessage());
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            throw new OperationError("Unexpected error occurred while saving reply: " + e.getMessage());
        }

    }
}

