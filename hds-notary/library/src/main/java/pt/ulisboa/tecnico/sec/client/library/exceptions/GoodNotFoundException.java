package pt.ulisboa.tecnico.sec.client.library.exceptions;

public class GoodNotFoundException extends ServerException {

    public GoodNotFoundException() {
    }

    public GoodNotFoundException(String message) {
        super(message);
    }

    public GoodNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoodNotFoundException(Throwable cause) {
        super(cause);
    }

    public GoodNotFoundException(String message,
        Throwable cause,
        boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
