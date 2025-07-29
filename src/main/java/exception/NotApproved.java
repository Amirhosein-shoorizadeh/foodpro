package exception;

public class NotApproved extends RuntimeException {
    public NotApproved(String message) {
        super(message);
    }
}
