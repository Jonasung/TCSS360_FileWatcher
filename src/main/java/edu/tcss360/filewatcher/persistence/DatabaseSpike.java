package edu.tcss360.filewatcher.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Standalone spike to confirm SQLite JDBC works. Run main() or runSpike() — no integration with app yet.
 */
public final class DatabaseSpike {

    private static final String JDBC_URL = "jdbc:sqlite:filewatcher_spike.db";

    private static final String CREATE_FILE_EVENTS =
        "CREATE TABLE IF NOT EXISTS file_events ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
        + "file_name TEXT NOT NULL,"
        + "extension TEXT NOT NULL,"
        + "absolute_path TEXT NOT NULL,"
        + "event_type TEXT NOT NULL,"
        + "timestamp TEXT NOT NULL)";

    /**
     * Runs the DB spike: creates file_events, inserts a sample row, selects and prints. Returns true on success.
     */
    public static boolean runSpike() {
        try {
            Connection conn = DriverManager.getConnection(JDBC_URL);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(CREATE_FILE_EVENTS);
                stmt.execute("INSERT INTO file_events (file_name, extension, absolute_path, event_type, timestamp) "
                    + "VALUES ('sample.txt', '.txt', 'C:\\temp\\sample.txt', 'CREATED', '2025-01-01T12:00:00Z')");
                try (ResultSet rs = stmt.executeQuery("SELECT id, file_name, extension, absolute_path, event_type, timestamp FROM file_events")) {
                    while (rs.next()) {
                        System.out.println("id=" + rs.getLong("id")
                            + " file_name=" + rs.getString("file_name")
                            + " extension=" + rs.getString("extension")
                            + " path=" + rs.getString("absolute_path")
                            + " event_type=" + rs.getString("event_type")
                            + " timestamp=" + rs.getString("timestamp"));
                    }
                }
            }
            conn.close();
            System.out.println("SQLite spike OK.");
            return true;
        } catch (Exception e) {
            System.err.println("SQLite spike failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {
        runSpike();
    }
}
