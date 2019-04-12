package pt.ulisboa.tecnico.sec.library.exceptions;

public class TransactionDoesntExistsException extends ServerException {

    public TransactionDoesntExistsException() {
    }

    public TransactionDoesntExistsException(String message) {
        super(message);
    }

    public TransactionDoesntExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionDoesntExistsException(Throwable cause) {
        super(cause);
    }

    public TransactionDoesntExistsException(String message,
                                            Throwable cause,
                                            boolean enableSuppression,
                                            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
