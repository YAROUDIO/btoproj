package service;


import java.util.Optional;

import common.UserRole;
import exception.OperationError;
import exception.DataSaveError;
import model.User;
import interfaces.IUserRepository;
import service.interfaces.IAuthService;
import util.InputUtil;

public class AuthService implements IAuthService {
    private IUserRepository userRepo;

    // Constructor
    public AuthService(IUserRepository userRepository) {
        this.userRepo = userRepository;
    }

    // Login method
    @Override
    public User login(String nric, String password) throws OperationError {
        if (!InputUtil.validateNric(nric)) {
            throw new OperationError("Invalid NRIC format.");
        }

        Optional<User> optionalUser = userRepo.findUserByNric(nric);
        User user = optionalUser.get();  // This will throw an exception if no value is present


        if (user != null && user.checkPassword(password)) {
            return user;
        } else if (user != null) {
            throw new OperationError("Incorrect password.");
        } else {
            throw new OperationError("NRIC not found.");
        }
    }

    // Change password method
    @Override
    public void changePassword(User user, String newPassword) throws OperationError {
        try {
            // Validation and password change handled within User model
            user.changePassword(newPassword);

            // Persist the change using the repository
            userRepo.saveUser(user);  // Use the instance to call saveUser()

        } catch (IllegalArgumentException | OperationError e) {
            // Catch validation errors or operation-related exceptions
            throw new OperationError("Password change failed: " + e.getMessage());
        } catch (DataSaveError e) {
            // Catch errors related to saving the data in the repository
            System.out.println("ERROR: Failed to save new password for " + user.getNric() + ": " + e.getMessage());
            throw new OperationError("Failed to save new password. Please try again later.");
        } catch (Exception e) {
            // Catch any unexpected errors
            System.out.println("Unexpected error during password change for " + user.getNric() + ": " + e.getMessage());
            throw new OperationError("An unexpected error occurred during password change.");
        }
    }


    // Get user role method
    @Override
    public UserRole getUserRole(User user) {
        // Delegate role fetching to the User model
        return user.getRole();
    }
}
