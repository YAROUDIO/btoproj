package repository;



import model.User;
import model.Applicant;
import model.HDBOfficer;
import model.HDBManager;
import interfaces.IBaseRepository;
import interfaces.IUserRepository;
import exception.DataSaveError;
import exception.IntegrityError;
import exception.OperationError;

import java.util.*;

public class UserRepositoryFacade implements IUserRepository {

    private final IBaseRepository<Applicant, String> applicantRepo;
    private final IBaseRepository<HDBOfficer, String> officerRepo;
    private final IBaseRepository<HDBManager, String> managerRepo;

    private final Map<String, User> allUsers = new HashMap<>();
    private boolean loaded = false;

    public UserRepositoryFacade(IBaseRepository<Applicant, String> applicantRepo,
                                IBaseRepository<HDBOfficer, String> officerRepo,
                                IBaseRepository<HDBManager, String> managerRepo) {
        this.applicantRepo = applicantRepo;
        this.officerRepo = officerRepo;
        this.managerRepo = managerRepo;
    }

    @Override
    public void loadAllUsers() {
        if (loaded) return;

        allUsers.clear();
        int duplicates = 0;

        List<IBaseRepository<? extends User, String>> repos = List.of(applicantRepo, officerRepo, managerRepo);
        for (IBaseRepository<? extends User, String> repo : repos) {
            try {
                repo.load();
                List<? extends User> users = repo.getAll();
                for (User user : users) {
                    if (allUsers.containsKey(user.getNric())) {
                        String existingType = allUsers.get(user.getNric()).getClass().getSimpleName();
                        String newType = user.getClass().getSimpleName();
                        System.out.printf("Warning: Duplicate NRIC '%s'. Overwriting %s with %s.%n", user.getNric(), existingType, newType);
                        duplicates++;
                    }
                    allUsers.put(user.getNric(), user);
                }
            } catch (Exception e) {
                System.out.printf("Error loading users from %s: %s%n", repo.getClass().getSimpleName(), e.getMessage());
            }
        }

        loaded = true;
        System.out.printf("Total unique users loaded into facade: %d (encountered %d duplicates).%n", allUsers.size(), duplicates);
    }

    @Override
    public Optional<User> findUserByNric(String nric) {
        if (!loaded) loadAllUsers();
        return Optional.ofNullable(allUsers.get(nric));
    }

    @Override
    public List<User> getAllUsers() {
        if (!loaded) loadAllUsers();
        return new ArrayList<>(allUsers.values());
    }

    @Override
    public void saveUser(User user) {
        if (!loaded) throw new OperationError("User data not loaded. Cannot save user.");

        IBaseRepository repo;
        if (user instanceof HDBManager) {
            repo = managerRepo;
        } else if (user instanceof HDBOfficer) {
            repo = officerRepo;
        } else if (user instanceof Applicant) {
            repo = applicantRepo;
        } else {
            throw new IllegalArgumentException("Unknown user type: " + user.getClass().getSimpleName());
        }

        try {
            repo.update(user);
        } catch (IntegrityError e) {
            try {
                repo.add(user);
            } catch (IntegrityError addErr) {
                throw new OperationError("Failed to add user " + user.getNric() + " after update failed: " + addErr.getMessage());
            } catch (Exception ex) {
                throw new DataSaveError("Failed to add user " + user.getNric() + ": " + ex.getMessage());
            }
        } catch (Exception ex) {
            throw new DataSaveError("Failed to update user " + user.getNric() + ": " + ex.getMessage());
        }

        allUsers.put(user.getNric(), user);
    }

    @Override
    public void saveAllUserTypes() {
        System.out.println("Saving user data...");
        List<String> errors = new ArrayList<>();

        List<IBaseRepository<? extends User, String>> repos = List.of(applicantRepo, officerRepo, managerRepo);
        for (IBaseRepository<? extends User, String> repo : repos) {
            try {
                repo.save();
            } catch (Exception e) {
                errors.add("Failed to save " + repo.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }

        if (!errors.isEmpty()) {
            throw new DataSaveError("Errors occurred during user save:\n" + String.join("\n", errors));
        }

        System.out.println("User data saved.");
    }
}
