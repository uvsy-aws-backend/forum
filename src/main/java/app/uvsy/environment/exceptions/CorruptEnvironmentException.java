package app.uvsy.environment.exceptions;

public class CorruptEnvironmentException extends RuntimeException {

    public CorruptEnvironmentException(String variable) {
        super(String.format("Environment variable not found: %s", variable));
    }
}
