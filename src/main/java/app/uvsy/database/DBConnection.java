package app.uvsy.database;

import app.uvsy.environment.Environment;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;

import java.sql.SQLException;

public class DBConnection {

    public static JdbcPooledConnectionSource create() throws SQLException {
        return new JdbcPooledConnectionSource(
                Environment.getDBURL(),
                Environment.getDBUsername(),
                Environment.getDBPassword()
        );
    }
}
