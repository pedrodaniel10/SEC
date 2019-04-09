package pt.ulisboa.tecnico.sec.library.exceptions;

public class GoodWrongOwner extends ServerException {
    public GoodWrongOwner() {
    }

    public GoodWrongOwner(String message) {
        super(message);
    }

    public GoodWrongOwner(String message, Throwable cause) {
        super(message, cause);
    }

    public GoodWrongOwner(Throwable cause) {
        super(cause);
    }

    public GoodWrongOwner(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
