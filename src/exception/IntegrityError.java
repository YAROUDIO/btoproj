package exception;

public class IntegrityError extends RuntimeException {
    public IntegrityError(String message) {
        super(message);
    }
}
