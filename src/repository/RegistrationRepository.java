package repository;


import model.Registration;
import storage.IStorageAdapter;
import common.FilePath;
import common.RegistrationStatus;
import interfaces.IRegistrationRepository;
import java.util.*;
import java.util.stream.Collectors;

public class RegistrationRepository extends BaseRepository<Registration, String> implements IRegistrationRepository {

    public RegistrationRepository(IStorageAdapter storageAdapter) {
        super(storageAdapter,
              Registration.class,
              FilePath.REGISTRATION.getPath(),
              Arrays.asList(Registration.HEADERS), // Convert String[] to List<String>
              reg -> reg.getOfficerNric() + "-" + reg.getProjectName()); // Composite key
    }

    public Optional<Registration> findByOfficerAndProject(String officerNric, String projectName) {
        String key = officerNric + "-" + projectName;
        return findByKey(key);
    }

    public List<Registration> findByOfficer(String officerNric) {
        if (!loaded) load();
        return data.values().stream()
                   .filter(reg -> reg.getOfficerNric().equals(officerNric))
                   .collect(Collectors.toList());
    }

    public List<Registration> findByProject(String projectName, RegistrationStatus statusFilter) {
        if (!loaded) load();

        return data.values().stream()
                .filter(reg -> reg.getProjectName().equals(projectName))
                .filter(reg -> statusFilter == null || reg.getStatus() == statusFilter)
                .collect(Collectors.toList());
    }

    @Override
    protected Registration createInstance(Map<String, Object> row) {
        // You must implement this based on how your Registration constructor works
        throw new UnsupportedOperationException("createInstance not yet implemented");
    }

    @Override
    protected Map<String, Object> toStorageMap(Registration item) {
        // Implement this based on how your Registration fields should be saved
        throw new UnsupportedOperationException("toStorageMap not yet implemented");
    }

    @Override
    protected String getKey(Registration item) {
        return item.getOfficerNric() + "-" + item.getProjectName();
    }
}
