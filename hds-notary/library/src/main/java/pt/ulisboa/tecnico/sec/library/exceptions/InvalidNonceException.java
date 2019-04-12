package pt.ulisboa.tecnico.sec.library.exceptions;

public class InvalidNonceException extends ServerException {

    public InvalidNonceException() {
    }

    public InvalidNonceException(String message) {
        super(message);
    }

    public InvalidNonceException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidNonceException(Throwable cause) {
        super(cause);
    }

    public InvalidNonceException(String message, Throwable cause, boolean enableSuppression,
                                 boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
