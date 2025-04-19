package exception;

public class DataLoadError extends RuntimeException {
    public DataLoadError(String message) {
        super(message);
    }
    public DataLoadError(String message, Throwable cause) {
        super(message, cause);
    }
}