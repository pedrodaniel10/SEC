package pt.ulisboa.tecnico.sec.library.exceptions;

public class GoodWrongOwnerException extends ServerException {

    public GoodWrongOwnerException() {
    }

    public GoodWrongOwnerException(String message) {
        super(message);
    }

    public GoodWrongOwnerException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoodWrongOwnerException(Throwable cause) {
        super(cause);
    }

    public GoodWrongOwnerException(String message, Throwable cause, boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
