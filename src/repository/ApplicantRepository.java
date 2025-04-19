package repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.FilePath;
import exception.DataLoadError;
import model.Applicant;
import storage.IStorageAdapter;

public class ApplicantRepository extends BaseRepository<Applicant, String> {

    public ApplicantRepository(IStorageAdapter storageAdapter) {
        super(storageAdapter, Applicant.class, FilePath.APPLICANT.getPath(), List.of("Name", "NRIC", "Age", "Marital Status", "Password"), Applicant::getNric);
    }

    @Override
    protected Applicant createInstance(Map<String, Object> row) {
        try {
            return new Applicant(
                (String) row.get("Name"),
                (String) row.get("NRIC"),
                Integer.parseInt((String) row.get("Age")),
                (String) row.get("Marital Status"),
                (String) row.getOrDefault("Password", "password") // Default password if missing
            );
        } catch (Exception e) {
            throw new DataLoadError("Error creating Applicant from row: " + row, e);
        }
    }

    @Override
    protected Map<String, Object> toStorageMap(Applicant item) {
        Map<String, Object> map = new HashMap<>();
        map.put("Name", item.getName());
        map.put("NRIC", item.getNric());
        map.put("Age", item.getAge());
        map.put("Marital Status", item.getMaritalStatus());
        map.put("Password", item.getPasswordForStorage());
        return map;
    }

    @Override
    protected String getKey(Applicant item) {
        return item.getNric();
    }
}
