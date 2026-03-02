package com.oceanview.integration;

import com.oceanview.dao.DatabaseConnection;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import org.junit.jupiter.api.Assumptions;

public final class DatabaseTestSupport {

    private static volatile boolean initialized = false;

    private DatabaseTestSupport() {
    }

    public static void assumeDatabaseReady() {
        if (initialized) {
            return;
        }

        synchronized (DatabaseTestSupport.class) {
            if (initialized) {
                return;
            }
            try {
                DatabaseConnection.getInstance().getConnection();
                initialized = true;
            } catch (Exception e) {
                Assumptions.assumeTrue(false, "Skipping DB integration tests: " + e.getMessage());
            }
        }
    }

    public static Connection appConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

}
