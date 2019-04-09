package pt.ulisboa.tecnico.sec.client.library.exceptions;

public class InvalidRequestNumberException extends ServerException {

    public InvalidRequestNumberException() {
    }

    public InvalidRequestNumberException(String message) {
        super(message);
    }

    public InvalidRequestNumberException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidRequestNumberException(Throwable cause) {
        super(cause);
    }

    public InvalidRequestNumberException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
