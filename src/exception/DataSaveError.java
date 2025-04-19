package exception;

public class DataSaveError extends RuntimeException {
    public DataSaveError(String message) {
        super(message);
    }
}
