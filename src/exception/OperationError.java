package exception;

public class OperationError extends RuntimeException {
    public OperationError(String message) {
        super(message);
    }
}