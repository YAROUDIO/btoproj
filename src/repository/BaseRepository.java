package repository;

import java.util.*;
import java.util.function.Function;

import common.RegistrationStatus;
import exception.IntegrityError;
import model.Project;
import model.Registration;
import storage.IStorageAdapter;

public abstract class BaseRepository<T, K> {

    protected IStorageAdapter storageAdapter;
    protected Class<T> modelClass;
    protected String sourceId;
    protected List<String> headers;
    protected Map<K, T> data;
    protected boolean loaded;

    // Constructor
    public BaseRepository(IStorageAdapter storageAdapter, Class<T> modelClass, String sourceId, List<String> headers, Function<T, K> keyGetter) {
        if (storageAdapter == null || modelClass == null || sourceId == null || headers == null || keyGetter == null) {
            throw new IllegalArgumentException("All arguments must be provided.");
        }
        this.storageAdapter = storageAdapter;
        this.modelClass = modelClass;
        this.sourceId = sourceId;
        this.headers = headers;
        this.data = new HashMap<>();
        this.loaded = false;
    }

    // Method to create an instance of the model class from a row map
    protected abstract T createInstance(Map<String, Object> row);

    // Method to convert a model instance to a map for storage
    protected abstract Map<String, Object> toStorageMap(T item);

    // Load data from storage
    public void load() {
        if (loaded) return;

        try {
            List<Map<String, Object>> storageData = storageAdapter.readData(sourceId, headers).getFirst();
            for (Map<String, Object> row : storageData) {
                T instance = createInstance(row);
                K key = getKey(instance);
                data.put(key, instance);
            }
            loaded = true;
        } catch (Exception e) {
            // Handle error (log, rethrow custom exceptions, etc.)
            System.out.println("Error loading data: " + e.getMessage());
        }
    }

    // Get all entries
    public List<T> getAll() {
        if (!loaded) load();
        return new ArrayList<>(data.values());
    }

    // Find by key
    public Optional<T> findByKey(K key) {
        if (!loaded) load();
        return Optional.ofNullable(data.get(key));
    }

    // Add item
    public void add(T item) {
        if (!loaded) load();
        K key = getKey(item);
        if (data.containsKey(key)) {
            throw new IntegrityError("Duplicate key found: " + key);
        }
        data.put(key, item);
        save();
    }

    // Update item
    public void update(T item) {
        if (!loaded) load();
        K key = getKey(item);
        if (!data.containsKey(key)) {
            throw new IntegrityError("Item not found: " + key);
        }
        data.put(key, item);
        save();
    }

    // Delete item
    public void delete(K key) {
        if (!loaded) load();
        if (!data.containsKey(key)) {
            throw new IntegrityError("Item not found: " + key);
        }
        data.remove(key);
        save();
    }

    // Save changes to storage
    public void save() {
        if (!loaded) load();
        List<Map<String, Object>> storageData = new ArrayList<>();
        for (T item : data.values()) {
            storageData.add(toStorageMap(item));
        }
        storageAdapter.writeData(sourceId, headers, storageData);
    }
    
    public Boolean isloaded() {
    	return loaded;
    }
    
    public Map<K, T> getData(){
    	return data;
    }

    // Abstract method to get key from model instance
    protected abstract K getKey(T item);

	public void deleteByName(String name) {
		// TODO Auto-generated method stub
		
	}

	public Optional<Project> findByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Project> findByManagerNric(String managerNric) {
		// TODO Auto-generated method stub
		return null;
	}

	public Optional<Registration> findByOfficerAndProject(String officerNric, String projectName) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Registration> findByOfficer(String officerNric) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Registration> findByProject(String projectName, RegistrationStatus statusFilter) {
		// TODO Auto-generated method stub
		return null;
	}
}

