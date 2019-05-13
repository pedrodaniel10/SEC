package pt.ulisboa.tecnico.sec.services.exceptions;

public class WrongTimeStampException extends ServerException {

    public WrongTimeStampException() {
    }

    public WrongTimeStampException(String message) {
        super(message);
    }

    public WrongTimeStampException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongTimeStampException(Throwable cause) {
        super(cause);
    }

    public WrongTimeStampException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
