package interfaces;

import java.util.List;
import java.util.Optional;

import exception.IntegrityError;

public interface IBaseRepository<T, K> {

    // Returns a list of all items
    List<T> getAll();

    // Finds an item by its primary key
    Optional<T> findByKey(K key);

    // Adds a new item. Throws an IntegrityError if the key exists.
    void add(T item) throws IntegrityError;

    // Updates an existing item. Throws an IntegrityError if the key is not found.
    void update(T item) throws IntegrityError;

    // Deletes an item by its primary key. Throws an IntegrityError if the key is not found.
    void delete(K key) throws IntegrityError;

    // Persists the current state of the repository's data to storage
    void save();

    // Loads data from storage into the repository
    void load();
}

