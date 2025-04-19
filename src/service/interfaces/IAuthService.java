package service.interfaces;

import model.User;
import common.UserRole;
import exception.OperationError;

public interface IAuthService {

    /**
     * Attempts to log in a user.
     * 
     * @param nric The NRIC of the user.
     * @param password The password provided by the user.
     * @return The User object on success.
     * @throws OperationError if NRIC is not found, password is wrong, or format is invalid.
     */
    User login(String nric, String password) throws OperationError;

    /**
     * Changes the password for the given user.
     * 
     * @param user The User whose password is to be changed.
     * @param newPassword The new password to be set.
     * @throws OperationError if validation fails or save fails.
     */
    void changePassword(User user, String newPassword) throws OperationError;

    /**
     * Gets the role of the user.
     * 
     * @param user The user whose role is to be fetched.
     * @return The role of the user.
     */
    UserRole getUserRole(User user);
}
