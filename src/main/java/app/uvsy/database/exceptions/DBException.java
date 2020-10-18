package app.uvsy.database.exceptions;

public class DBException extends RuntimeException {
    public DBException(Throwable throwable) {
        super(throwable);
    }
}
