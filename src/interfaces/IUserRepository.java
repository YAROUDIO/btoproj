package interfaces;

import java.util.List;
import java.util.Optional;

import model.User;

public interface IUserRepository {

    // Finds any user (Applicant, Officer, Manager) by NRIC
    Optional<User> findUserByNric(String nric);

    // Gets a list of all users from all roles
    List<User> getAllUsers();

    // Saves changes to a specific user in the appropriate underlying repository
    void saveUser(User user);

    // Loads users from all underlying repositories
    void loadAllUsers();

    // Saves data for all underlying user repositories
    void saveAllUserTypes();
}
