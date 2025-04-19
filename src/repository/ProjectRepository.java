package repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import exception.DataLoadError;
import storage.IStorageAdapter;
import util.DateUtil;
import model.Project;
import common.FilePath;
import common.FlatType;
import interfaces.IProjectRepository;

public class ProjectRepository extends BaseRepository<Project, String> implements IProjectRepository {

    public ProjectRepository(IStorageAdapter storageAdapter) {
        super(storageAdapter,
              Project.class,
              FilePath.PROJECT.getPath(),
              Arrays.asList(Project.HEADERS),  // Convert the HEADERS array to a List<String>
              project -> project.getProjectName());  // Key getter for project
    }

    // Implementing the createInstance method
    @Override
    protected Project createInstance(Map<String, Object> rowDict) {
        try {
            // Retrieve the necessary fields from the rowDict
            String projectName = (String) rowDict.get("Project Name");
            String neighborhood = (String) rowDict.get("Neighborhood");
            int numUnits1 = Integer.parseInt((String) rowDict.get("Number of units for Type 1"));
            int price1 = Integer.parseInt((String) rowDict.get("Selling price for Type 1"));
            int numUnits2 = Integer.parseInt((String) rowDict.get("Number of units for Type 2"));
            int price2 = Integer.parseInt((String) rowDict.get("Selling price for Type 2"));
            String managerNric = (String) rowDict.get("Manager");
            int officerSlot = Integer.parseInt((String) rowDict.get("Officer Slot"));
            boolean visibility = Boolean.parseBoolean((String) rowDict.get("Visibility"));

            // Convert the opening and closing date strings to Date objects (example format: "yyyy-MM-dd")
            String openingDateStr = (String) rowDict.get("Application opening date");
            String closingDateStr = (String) rowDict.get("Application closing date");
            LocalDate oDate = DateUtil.parseDate(openingDateStr);
            LocalDate cDate = DateUtil.parseDate(closingDateStr);
            
            Date openingDate = Date.from(oDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date closingDate = Date.from(cDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            // Check if any required fields are missing
            if (projectName == null || neighborhood == null || managerNric == null) {
                throw new DataLoadError("Missing required fields in row: " + rowDict);
            }

            // Create and return the Project instance
            return new Project(projectName, neighborhood, numUnits1, price1, numUnits2, price2,
                               openingDate, closingDate, managerNric, officerSlot, new ArrayList<>(), visibility);
        } catch (Exception e) {
            throw new DataLoadError("Error creating Project from row: " + rowDict + ". Error: " + e);
        }
    }

    @Override
    public Optional<Project> findByName(String name) {
        return findByKey(name);
    }

    @Override
    public void deleteByName(String name) {
        delete(name);  // Base class handles key not found
    }

    @Override
    public List<Project> findByManagerNric(String managerNric) {
        return getAll().stream()
                .filter(project -> project.getManagerNric().equals(managerNric))
                .collect(Collectors.toList());
    }

    @Override
    protected Map<String, Object> toStorageMap(Project item) {
        Map<String, Object> map = new HashMap<>();
        map.put("Project Name", item.getProjectName());
        map.put("Neighborhood", item.getNeighborhood());
        map.put("Type 1", item.getFlatDetails(FlatType.TWO_ROOM)[0]);
        map.put("Number of units for Type 1", item.getFlatDetails(FlatType.TWO_ROOM)[0]);
        map.put("Selling price for Type 1", item.getFlatDetails(FlatType.TWO_ROOM)[1]);
        map.put("Type 2", item.getFlatDetails(FlatType.THREE_ROOM)[0]);
        map.put("Number of units for Type 2", item.getFlatDetails(FlatType.THREE_ROOM)[0]);
        map.put("Selling price for Type 2", item.getFlatDetails(FlatType.THREE_ROOM)[1]);
        map.put("Application opening date", DateUtil.formatDate(item.getOpeningDate()));
        map.put("Application closing date", DateUtil.formatDate(item.getClosingDate()));
        map.put("Manager", item.getManagerNric());
        map.put("Officer Slot", item.getOfficerSlot());
        map.put("Officer", String.join(",", item.getOfficerNrics()));
        map.put("Visibility", item.isVisibility());
        return map;
    }

    @Override
    protected String getKey(Project item) {
        return item.getProjectName();  // Assuming project name is unique, it can be used as the key
    }

	
}

