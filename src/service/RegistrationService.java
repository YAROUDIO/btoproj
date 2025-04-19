package service;

import common.enums.RegistrationStatus;
import exception.OperationError;
import exception.IntegrityError;
import model.HDBManager;
import model.HDBOfficer;
import model.Project;
import model.Registration;
import repository.interfaces.IRegistrationRepository;
import repository.interfaces.IApplicationRepository;
import service.interfaces.IRegistrationService;
import service.interfaces.IProjectService;
import utils.DateUtil;
import java.util.*;

public class RegistrationService implements IRegistrationService {
    
    private final IRegistrationRepository _regRepo;
    private final IProjectService _projectService;
    private final IApplicationRepository _appRepo;

    public RegistrationService(IRegistrationRepository registrationRepository, 
                               IProjectService projectService, 
                               IApplicationRepository applicationRepository) {
        this._regRepo = registrationRepository;
        this._projectService = projectService;
        this._appRepo = applicationRepository;
    }

    @Override
    public Optional<Registration> findRegistration(String officerNric, String projectName) {
        return Optional.ofNullable(_regRepo.findByOfficerAndProject(officerNric, projectName));
    }

    @Override
    public List<Registration> getRegistrationsByOfficer(String officerNric) {
        return _regRepo.findByOfficer(officerNric);
    }

    @Override
    public List<Registration> getRegistrationsForProject(String projectName, RegistrationStatus statusFilter) {
        return _regRepo.findByProject(projectName, statusFilter);
    }

    private void checkOfficerRegistrationEligibility(HDBOfficer officer, Project project) {
        if (findRegistration(officer.getNric(), project.getProjectName()).isPresent()) {
            throw new OperationError("Already registered for project '" + project.getProjectName() + "'.");
        }
        if (project.getManagerNric().equals(officer.getNric())) {
            throw new OperationError("Managers cannot register as officers for their own projects.");
        }
        if (_appRepo.findAllByApplicantNric(officer.getNric()).stream()
                .anyMatch(app -> app.getProjectName().equals(project.getProjectName()))) {
            throw new OperationError("Cannot register for a project you have previously applied for.");
        }

        // Check for overlapping approved registrations
        Date targetOd = project.getOpeningDate();
        Date targetCd = project.getClosingDate();
        if (targetOd == null || targetCd == null) {
            throw new OperationError("Target project has invalid dates.");
        }

        for (Registration reg : getRegistrationsByOfficer(officer.getNric())) {
            if (reg.getStatus() == RegistrationStatus.APPROVED) {
                Project otherProject = _projectService.findProjectByName(reg.getProjectName());
                if (otherProject != null && otherProject.getOpeningDate() != null && otherProject.getClosingDate() != null) {
                    if (DateUtil.datesOverlap(targetOd, targetCd, otherProject.getOpeningDate(), otherProject.getClosingDate())) {
                        throw new OperationError("Overlaps with approved registration for '" + otherProject.getProjectName() + "'.");
                    }
                }
            }
        }
    }

    @Override
    public Registration officerRegisterForProject(HDBOfficer officer, Project project) {
        checkOfficerRegistrationEligibility(officer, project);
        Registration newRegistration = new Registration(officer.getNric(), project.getProjectName());
        try {
            _regRepo.add(newRegistration);
            return newRegistration;
        } catch (IntegrityError e) {
            throw new OperationError("Failed to submit registration: " + e.getMessage());
        }
    }

    private boolean managerCanManageReg(HDBManager manager, Registration registration) {
        Project project = _projectService.findProjectByName(registration.getProjectName());
        return project != null && project.getManagerNric().equals(manager.getNric());
    }

    @Override
    public void managerApproveOfficerRegistration(HDBManager manager, Registration registration) {
        if (!managerCanManageReg(manager, registration)) {
            throw new OperationError("You do not manage this project.");
        }
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new OperationError("Registration status is not PENDING.");
        }

        Project project = _projectService.findProjectByName(registration.getProjectName());
        if (project == null) {
            throw new OperationError("Project '" + registration.getProjectName() + "' not found.");
        }
        if (!project.canAddOfficer()) {
            throw new OperationError("No available officer slots in project '" + project.getProjectName() + "'.");
        }

        Date targetOd = project.getOpeningDate();
        Date targetCd = project.getClosingDate();
        if (targetOd == null || targetCd == null) {
            throw new OperationError("Project has invalid dates.");
        }

        for (Registration otherReg : getRegistrationsByOfficer(registration.getOfficerNric())) {
            if (!otherReg.equals(registration) && otherReg.getStatus() == RegistrationStatus.APPROVED) {
                Project otherProject = _projectService.findProjectByName(otherReg.getProjectName());
                if (otherProject != null && otherProject.getOpeningDate() != null && otherProject.getClosingDate() != null) {
                    if (DateUtil.datesOverlap(targetOd, targetCd, otherProject.getOpeningDate(), otherProject.getClosingDate())) {
                        throw new OperationError("Officer approved for overlapping project '" + otherProject.getProjectName() + "'.");
                    }
                }
            }
        }

        boolean officerAdded = false;
        try {
            _projectService.addOfficerToProject(project, registration.getOfficerNric());
            officerAdded = true;

            registration.setStatus(RegistrationStatus.APPROVED);
            _regRepo.update(registration);
        } catch (OperationError | IntegrityError e) {
            registration.setStatus(RegistrationStatus.PENDING);
            try {
                _regRepo.update(registration);
            } catch (Exception rb_e) {
                System.out.println("Warning: Failed rollback reg status: " + rb_e.getMessage());
            }

            if (officerAdded) {
                try {
                    _projectService.removeOfficerFromProject(project, registration.getOfficerNric());
                } catch (Exception rb_e) {
                    System.out.println("CRITICAL: Failed rollback officer from project: " + rb_e.getMessage());
                }
            }

            throw new OperationError("Approval failed: " + e.getMessage() + ". Rollback attempted.");
        }
    }

    @Override
    public void managerRejectOfficerRegistration(HDBManager manager, Registration registration) {
        if (!managerCanManageReg(manager, registration)) {
            throw new OperationError("You do not manage this project.");
        }
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new OperationError("Registration status is not PENDING.");
        }

        try {
            registration.setStatus(RegistrationStatus.REJECTED);
            _regRepo.update(registration);
        } catch (IntegrityError e) {
            throw new OperationError("Failed to save registration rejection: " + e.getMessage());
        }
    }
}

