package repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import exception.DataLoadError;
import exception.IntegrityError;
import storage.IStorageAdapter;
import model.HDBManager;
import common.FilePath;

public class ManagerRepository extends BaseRepository<HDBManager, String> {

    private static final String[] HEADERS = {
        "Name", "NRIC", "Age", "Marital Status", "Password"
    };

    public ManagerRepository(IStorageAdapter storageAdapter) {
        super(storageAdapter,
              HDBManager.class,
              FilePath.MANAGER.getPath(), // Using getPath() instead of value
              Arrays.asList(HEADERS),  // Convert array to List
              manager -> manager.getNric()); // Key getter for manager
    }

    // Create an instance of HDBManager from the row map
    @Override
    protected HDBManager createInstance(Map<String, Object> rowDict) {
        try {
            // Validate and retrieve the fields from the rowDict
            int age = Integer.parseInt((String) rowDict.get("Age"));
            if (age < 0) {
                throw new IllegalArgumentException("Age cannot be negative");
            }

            return new HDBManager(
                (String) rowDict.get("Name"),
                (String) rowDict.get("NRIC"),
                age,
                (String) rowDict.get("Marital Status"),
                (String) rowDict.getOrDefault("Password", "password") // Default password if not present
            );
        } catch (Exception e) {
            throw new DataLoadError("Error creating HDBManager from row: " + rowDict + ". Error: " + e);
        }
    }

    // Convert the HDBManager instance to a map for storage
    @Override
    protected Map<String, Object> toStorageMap(HDBManager item) {
        Map<String, Object> map = new HashMap<>();
        map.put("Name", item.getName());
        map.put("NRIC", item.getNric());
        map.put("Age", item.getAge());
        map.put("Marital Status", item.getMaritalStatus());
        map.put("Password", item.getPasswordForStorage()); // Ensure password is securely handled
        return map;
    }
    protected String getKey(HDBManager item) {
        return item.getNric();  // Assuming HDBManager has a method getNric() to retrieve the NRIC
    }
}

