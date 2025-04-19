package controller;

import service.AuthService;
import service.ApplicationService;
import service.ProjectService;
import view.BaseView;
import view.AuthView;
import model.User;
import exception.OperationError;

public class ApplicationController {

    private User _currentUser;
    private BaseRoleController _roleController;
    private Map<String, Object> _services;
    private Map<String, Object> _views;

    public ApplicationController() {
        // Initialize services and views
    }

    public void run() {
        while (true) {
            if (_currentUser == null) {
                if (!handleLogin()) {
                    break;
                }
            } else {
                _roleController.runMenu();
            }
        }
    }

    private boolean handleLogin() {
        // Handle login process
        AuthService authService = (AuthService) _services.get("auth");
        AuthView authView = (AuthView) _views.get("auth");
        BaseView baseView = (BaseView) _views.get("base");

        // Attempt login
        String nric = authView.promptForNric();
        String password = authView.promptForPassword();

        _currentUser = authService.login(nric, password);

        if (_currentUser != null) {
            // Set the correct role controller based on the user role
            if (_currentUser.getRole() == UserRole.APPLICANT) {
                _roleController = new ApplicantController(_currentUser, _services, _views);
            } else if (_currentUser.getRole() == UserRole.HDB_OFFICER) {
                _roleController = new OfficerController(_currentUser, _services, _views);
            }
            return true;
        } else {
            baseView.displayMessage("Login failed", true);
            return false;
        }
    }

    private void handleLogout() {
        _currentUser = null;
        _roleController = null;
    }
}
