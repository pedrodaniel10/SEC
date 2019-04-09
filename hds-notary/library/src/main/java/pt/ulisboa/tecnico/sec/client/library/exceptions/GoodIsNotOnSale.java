package pt.ulisboa.tecnico.sec.library.exceptions;

public class GoodIsNotOnSale extends ServerException {
    public GoodIsNotOnSale() {
    }

    public GoodIsNotOnSale(String message) {
        super(message);
    }

    public GoodIsNotOnSale(String message, Throwable cause) {
        super(message, cause);
    }

    public GoodIsNotOnSale(Throwable cause) {
        super(cause);
    }

    public GoodIsNotOnSale(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
