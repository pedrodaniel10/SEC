package pt.ulisboa.tecnico.sec.library.exceptions;

public class GoodIsNotOnSaleException extends ServerException {

    public GoodIsNotOnSaleException() {
    }

    public GoodIsNotOnSaleException(String message) {
        super(message);
    }

    public GoodIsNotOnSaleException(String message, Throwable cause) {
        super(message, cause);
    }

    public GoodIsNotOnSaleException(Throwable cause) {
        super(cause);
    }

    public GoodIsNotOnSaleException(String message, Throwable cause, boolean enableSuppression,
        boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
