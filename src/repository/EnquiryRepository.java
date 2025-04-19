package repository;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import common.FilePath;
import exception.DataLoadError;
import exception.IntegrityError;
import interfaces.IEnquiryRepository;
import model.Enquiry;
import storage.IStorageAdapter;


public class EnquiryRepository extends BaseRepository<Enquiry, Integer> implements IEnquiryRepository {

    private int nextId;

    public EnquiryRepository(IStorageAdapter storageAdapter) {
        super(storageAdapter,
              Enquiry.class,
              FilePath.ENQUIRY.getPath(),
              Arrays.asList(Enquiry.HEADERS), // Convert String[] to List<String>
              enquiry -> enquiry.getEnquiryId()); // Assuming Enquiry has getEnquiryId method for key
        this.nextId = 0;  // Initialized during load
    }

    // Calculate the next available ID based on current data
    private int calculateNextId() {
        if (this.getData().isEmpty()) {
            return 1;
        }
        try {
            return this.getData().keySet().stream()
                    .max(Integer::compareTo)
                    .orElse(0) + 1; // Finds the max key and adds 1
        } catch (Exception e) {
            System.out.println("Warning: Could not determine max Enquiry ID. Resetting next ID to 1.");
            return 1;
        }
    }

    // Override load to calculate next ID after data is loaded
    @Override
    public void load() {
        super.load(); // Load data using the base class method
        this.nextId = calculateNextId(); // Calculate ID based on loaded data
    }

    // Override add to assign the next available ID
    @Override
    public void add(Enquiry item) {
        if (!this.isloaded()) load();

        if (!(item instanceof Enquiry)) {
            throw new IllegalArgumentException("Item must be of type Enquiry");
        }

        // Assign the next available ID before adding
        item.setEnquiryId(this.nextId); // Assuming Enquiry has setEnquiryId() method for ID assignment

        Integer key = this.getKey(item); // Get the ID as the key

        if (this.getData().containsKey(key)) {
            throw new IntegrityError("Enquiry with generated ID '" + key + "' already exists. ID generation failed?");
        }

        this.getData().put(key, item);
        this.nextId++; // Increment for the next add
        save();
    }

    // Method to get the next available ID
    @Override
    public int getNextId() {
        if (!this.isloaded()) load();
        return this.nextId;
    }

    // Find an enquiry by ID
    @Override
    public Optional<Enquiry> findById(int enquiryId) {
        try {
            Integer key = enquiryId;
            return this.findByKey(key);
        } catch (Exception e) {
            return Optional.empty(); // Invalid ID format
        }
    }

    // Find enquiries by applicant NRIC
    @Override
    public List<Enquiry> findByApplicant(String applicantNric) {
        if (!this.isloaded()) load();
        return this.getData().values().stream()
                .filter(enq -> enq.getApplicantNric().equals(applicantNric))
                .collect(Collectors.toList());
    }

    // Find enquiries by project name
    @Override
    public List<Enquiry> findByProject(String projectName) {
        if (!this.isloaded()) load();
        return this.getData().values().stream()
                .filter(enq -> enq.getProjectName().equals(projectName))
                .collect(Collectors.toList());
    }

    // Delete an enquiry by ID
    @Override
    public void deleteById(int enquiryId) {
        try {
            Integer key = enquiryId;
            this.delete(key); // Base delete handles not found error
        } catch (Exception e) {
            throw new IntegrityError("Invalid Enquiry ID format for deletion: " + enquiryId);
        }
    }

    // Implementation of the createInstance method to create Enquiry from row data

    protected Enquiry createInstance(Map<String, Object> rowDict) {
        try {
            // Retrieve the necessary fields from the rowDict
            int enquiryId = (int) rowDict.get("EnquiryId");
            String applicantNric = (String) rowDict.get("ApplicantNRIC");
            String projectName = (String) rowDict.get("ProjectName");
            String text = (String) rowDict.get("Text");  // Assuming text is in the rowDict
            String reply = (String) rowDict.get("Reply");  // Assuming reply is in the rowDict

            // Create and return the Enquiry object
            return new Enquiry(enquiryId, applicantNric, projectName, text, reply);
        } catch (Exception e) {
            throw new DataLoadError("Error creating Enquiry from row: " + rowDict + ". Error: " + e);
        }
    }


	@Override
	protected Map<String, Object> toStorageMap(Enquiry item) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Integer getKey(Enquiry item) {
		// TODO Auto-generated method stub
		return null;
	}
}


