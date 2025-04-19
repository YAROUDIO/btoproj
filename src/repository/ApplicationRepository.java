package repository;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;
import common.ApplicationStatus;
import common.FilePath;
import common.FlatType;
import exception.DataLoadError;
import exception.IntegrityError;
import interfaces.IApplicationRepository;
import model.Application;
import storage.IStorageAdapter;

public class ApplicationRepository extends BaseRepository<Application, String> implements IApplicationRepository {

	public ApplicationRepository(IStorageAdapter storageAdapter) {
        super(storageAdapter, 
              Application.class, 
              FilePath.APPLICATION.getPath(), 
              Arrays.asList(Application.HEADERS),  // Convert String[] to List<String>
              app -> app.getApplicantNric() + "-" + app.getProjectName());
    }

    @Override
    protected Application createInstance(Map<String, Object> row) {
        // Create an Application instance based on data in the row
        try {
            String applicantNric = (String) row.get("ApplicantNRIC");
            String projectName = (String) row.get("ProjectName");

            // Parse FlatType and ApplicationStatus
            FlatType flatType = FlatType.fromValue(Integer.parseInt((String) row.get("FlatType")));  // Assuming FlatType is stored as an integer
            ApplicationStatus status = ApplicationStatus.valueOf((String) row.get("Status"));

            // Parse requestWithdrawal as boolean
            boolean requestWithdrawal = Boolean.parseBoolean((String) row.get("RequestWithdrawal"));

            // Return a new Application instance
            return new Application(applicantNric, projectName, flatType, status, requestWithdrawal);
        } catch (Exception e) {
            throw new DataLoadError("Error creating Application from row: " + row + ". Error: " + e);
        }
    }

    @Override
    protected Map<String, Object> toStorageMap(Application item) {
        Map<String, Object> map = new HashMap<>();
        map.put("ApplicantNRIC", item.getApplicantNric());
        map.put("ProjectName", item.getProjectName());
        map.put("FlatType", item.getFlatType().toString());  // Assuming the flatType is an enum and needs to be converted to string
        map.put("Status", item.getStatus().toString());
        map.put("RequestWithdrawal", String.valueOf(item.isRequestWithdrawal()));  // Convert boolean to String for storage
        return map;
    }

    @Override
    protected String getKey(Application item) {
        return item.getApplicantNric() + "-" + item.getProjectName();
    }

    @Override
    public Optional<Application> findByApplicantNric(String nric) {
    	load();
        return data.values().stream()
            .filter(app -> app.getApplicantNric().equals(nric) && app.getStatus() != ApplicationStatus.UNSUCCESSFUL)
            .findFirst(); // returns Optional<Application>
    }

    @Override
    public List<Application> findAllByApplicantNric(String nric) {
        load();
        return data.values().stream()
            .filter(app -> app.getApplicantNric().equals(nric))
            .collect(Collectors.toList());
    }

    @Override
    public List<Application> findByProjectName(String projectName) {
        load();
        return data.values().stream()
            .filter(app -> app.getProjectName().equals(projectName))
            .collect(Collectors.toList());
    }

    @Override
    public void add(Application item) {
        load();
        if (findByApplicantNric(item.getApplicantNric()) != null) {
            throw new IntegrityError("Applicant " + item.getApplicantNric() + " already has an active application.");
        }
        super.add(item);
    }
}