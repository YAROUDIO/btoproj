package repository;

import java.util.List;
import java.util.ArrayList;

import exception.DataSaveError;
import interfaces.IBaseRepository;

public class PersistenceManager {
    private List<IBaseRepository<?, ?>> repositories;

    // Constructor
    public PersistenceManager(List<IBaseRepository<?, ?>> repositories) {
        this.repositories = repositories;
    }

    // Load data for all managed repositories
    public void loadAll() {
        System.out.println("Loading all data...");
        List<String> errors = new ArrayList<>();
        for (IBaseRepository<?, ?> repo : repositories) {
            try {
                repo.load();  // Assuming IBaseRepository has a load() method
            } catch (Exception e) {
                // Log error but continue loading others
                String errorMsg = "Failed to load data for " + repo.getClass().getSimpleName() + ": " + e.getMessage();
                System.out.println("ERROR: " + errorMsg);
                errors.add(errorMsg);
            }
        }

        if (!errors.isEmpty()) {
            // Report all loading errors
            System.out.println("\n--- Loading Errors Encountered ---");
            for (String error : errors) {
                System.out.println("- " + error);
            }
            System.out.println("---------------------------------");
        } else {
            System.out.println("All data loaded successfully.");
        }
    }

    // Save data for all managed repositories
    public void saveAll() {
        List<String> errors = new ArrayList<>();
        for (IBaseRepository<?, ?> repo : repositories) {
            try {
                repo.save();  // Assuming IBaseRepository has a save() method
            } catch (Exception e) {
                String errorMsg = "Failed to save data for " + repo.getClass().getSimpleName() + ": " + e.getMessage();
                System.out.println("ERROR: " + errorMsg);
                errors.add(errorMsg);
            }
        }

        if (!errors.isEmpty()) {
            // Combine errors into a single exception to signal failure
            throw new DataSaveError("Errors occurred during data save:\n" + String.join("\n", errors));
        }
    }
}
