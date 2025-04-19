package storage;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import common.Tuple;
import exception.DataLoadError;
import exception.DataSaveError;

public interface IStorageAdapter {

    /**
     * Reads data from the specified source.
     * @param sourceId Identifier for the data source (e.g., file path).
     * @param expectedHeaders List of headers expected in the source.
     * @return A tuple containing:
     * - A List of maps representing the data rows.
     * - The actual headers found in the source.
     * @throws DataLoadError If the source cannot be read or headers are invalid.
     * @throws FileNotFoundException If the source does not exist (implementations may handle creation).
     */
    Tuple<List<Map<String, Object>>, List<String>> readData(String sourceId, List<String> expectedHeaders) throws DataLoadError, FileNotFoundException;

    /**
     * Writes data to the specified source.
     * @param sourceId Identifier for the data source (e.g., file path).
     * @param headers The list of headers to write.
     * @param dataDicts A list of maps representing the data rows.
     * @throws DataSaveError If the data cannot be written.
     */
    void writeData(String sourceId, List<String> headers, List<Map<String, Object>> dataDicts) throws DataSaveError;
}
