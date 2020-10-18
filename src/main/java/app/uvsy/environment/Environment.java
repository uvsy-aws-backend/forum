package app.uvsy.environment;

import app.uvsy.environment.exceptions.CorruptEnvironmentException;

import java.util.Optional;

public class Environment {


    public static String getDBDriver() {
        return getString("DB_DRIVER");
    }

    public static String getDBProtocol() {
        return getString("DB_PROTOCOL");
    }

    public static String getDBPort() {
        return getString("DB_PORT");
    }

    public static String getDBName() {
        return getString("DB_NAME");
    }

    public static String getDBHost() {
        return getString("DB_HOST");
    }

    public static String getDBURL() {
        return String.format(
                "%s:%s://%s:%s/%s",
                getDBDriver(),
                getDBProtocol(),
                getDBHost(),
                getDBPort(),
                getDBName()
        );
    }


    public static String getDBUsername() {
        return getString("DB_USERNAME");
    }

    public static String getDBPassword() {
        return getString("DB_PASSWORD");
    }

    public static String getStage() {
        return getString("STAGE");
    }

    private static String getString(String variableName) {
        return Optional.ofNullable(System.getenv(variableName))
                .orElseThrow(() -> new CorruptEnvironmentException(variableName));
    }
}
