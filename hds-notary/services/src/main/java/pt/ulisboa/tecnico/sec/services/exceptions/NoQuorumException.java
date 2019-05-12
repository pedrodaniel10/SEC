package pt.ulisboa.tecnico.sec.services.exceptions;

public class NoQuorumException extends ServerException {

    public NoQuorumException() {
    }

    public NoQuorumException(String message) {
        super(message);
    }

    public NoQuorumException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoQuorumException(Throwable cause) {
        super(cause);
    }

    public NoQuorumException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
