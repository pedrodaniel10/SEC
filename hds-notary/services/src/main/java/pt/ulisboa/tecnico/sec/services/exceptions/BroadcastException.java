package pt.ulisboa.tecnico.sec.services.exceptions;

public class BroadcastException extends ServerException {

    public BroadcastException() {
    }

    public BroadcastException(String message) {
        super(message);
    }

    public BroadcastException(String message, Throwable cause) {
        super(message, cause);
    }

    public BroadcastException(Throwable cause) {
        super(cause);
    }

    public BroadcastException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
