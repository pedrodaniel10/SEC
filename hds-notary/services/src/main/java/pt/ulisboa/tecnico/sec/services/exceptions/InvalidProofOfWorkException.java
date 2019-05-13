package pt.ulisboa.tecnico.sec.services.exceptions;

public class InvalidProofOfWorkException extends ServerException {

    public InvalidProofOfWorkException() {
    }

    public InvalidProofOfWorkException(String message) {
        super(message);
    }

    public InvalidProofOfWorkException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidProofOfWorkException(Throwable cause) {
        super(cause);
    }

    public InvalidProofOfWorkException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
