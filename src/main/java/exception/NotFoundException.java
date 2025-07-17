package exception;

public class NotFoundException extends RuntimeException {
    final static String message = "Not found";

    public NotFoundException(String message) {
        super(message);
    }

    public static String getmessage() {

        return message;
    }
}
