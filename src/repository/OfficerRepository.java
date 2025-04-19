package repository;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.Arrays;

import exception.DataLoadError;
import exception.IntegrityError;
import storage.IStorageAdapter;
import model.HDBOfficer;
import common.FilePath;

public class OfficerRepository extends BaseRepository<HDBOfficer, String> {

    private static final String[] HEADERS = {
        "Name", "NRIC", "Age", "Marital Status", "Password"
    };

    public OfficerRepository(IStorageAdapter storageAdapter) {
        super(storageAdapter,
              HDBOfficer.class,
              FilePath.OFFICER.getPath(), // Using getPath() instead of value
              Arrays.asList(HEADERS),  // Convert array to List
              officer -> officer.getNric()); // Key getter for officer
    }

    // Create an instance of HDBOfficer from the row map
    @Override
    protected HDBOfficer createInstance(Map<String, Object> rowDict) {
        try {
            // Validate and retrieve the fields from the rowDict
            int age = Integer.parseInt((String) rowDict.get("Age"));
            if (age < 0) {
                throw new IllegalArgumentException("Age cannot be negative");
            }

            return new HDBOfficer(
                (String) rowDict.get("Name"),
                (String) rowDict.get("NRIC"),
                age,
                (String) rowDict.get("Marital Status"),
                (String) rowDict.getOrDefault("Password", "password")  // Default password if not present
            );
        } catch (Exception e) {
            throw new DataLoadError("Error creating HDBOfficer from row: " + rowDict + ". Error: " + e);
        }
    }

    // Convert the HDBOfficer instance to a map for storage
    @Override
    protected Map<String, Object> toStorageMap(HDBOfficer item) {
        Map<String, Object> map = new HashMap<>();
        map.put("Name", item.getName());
        map.put("NRIC", item.getNric());
        map.put("Age", item.getAge());
        map.put("Marital Status", item.getMaritalStatus());
        map.put("Password", item.getPasswordForStorage());  // Ensure password is securely handled
        return map;
    }

    // Implement the getKey method to retrieve the unique key (NRIC) for HDBOfficer
    @Override
    protected String getKey(HDBOfficer item) {
        return item.getNric();  // Assuming HDBOfficer has a method getNric() to retrieve the NRIC
    }
}
