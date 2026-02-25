package TCSS_FileWatcher.database;

import TCSS_FileWatcher.domain.FileEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;

/**
 * SQLite persistence for file events. Each row: file name, absolute path, event type, date/time.
 */
public class EventRepository {

    private static final String DEFAULT_DB_DIR = ".filewatcher";
    private static final String DEFAULT_DB_FILE = "filewatcher.db";

    private final String dbPath;

    public EventRepository() {
        this(dbPathInUserHome());
    }

    public EventRepository(String dbPath) {
        this.dbPath = dbPath;
    }

    private static String dbPathInUserHome() {
        String home = System.getProperty("user.home");
        Path dir = Paths.get(home, DEFAULT_DB_DIR);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not create DB directory: " + dir, e);
        }
        return dir.resolve(DEFAULT_DB_FILE).toAbsolutePath().toString();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbPath);
    }

    /**
     * Creates the file_events table if it does not exist.
     * Columns: file_name, absolute_path, event_type, event_datetime (each in its own field).
     */
    public void initSchema() {
        String sql = "CREATE TABLE IF NOT EXISTS file_events ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "file_name TEXT NOT NULL,"
            + "absolute_path TEXT NOT NULL,"
            + "event_type TEXT NOT NULL,"
            + "event_datetime TEXT NOT NULL"
            + ")";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to init schema", e);
        }
    }

    /**
     * Inserts one file event. Schema must be initialized first (e.g. via initSchema()).
     */
    public void insert(FileEvent event) {
        if (event == null) return;
        String sql = "INSERT INTO file_events (file_name, absolute_path, event_type, event_datetime) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String fileName = event.getPath().getFileName() != null
                ? event.getPath().getFileName().toString()
                : event.getPath().toString();
            String absolutePath = event.getPath().toAbsolutePath().toString();
            String eventType = event.getType().name();
            String dateTime = event.getTimestamp().toString();

            ps.setString(1, fileName);
            ps.setString(2, absolutePath);
            ps.setString(3, eventType);
            ps.setString(4, dateTime);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to insert event: " + event, e);
        }
    }

    /**
     * Inserts multiple events in one transaction.
     */
    public void insertAll(Iterable<FileEvent> events) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            String sql = "INSERT INTO file_events (file_name, absolute_path, event_type, event_datetime) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (FileEvent event : events) {
                    if (event == null) continue;
                    String fileName = event.getPath().getFileName() != null
                        ? event.getPath().getFileName().toString()
                        : event.getPath().toString();
                    ps.setString(1, fileName);
                    ps.setString(2, event.getPath().toAbsolutePath().toString());
                    ps.setString(3, event.getType().name());
                    ps.setString(4, event.getTimestamp().toString());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new IllegalStateException("Failed to insert events", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to open connection for insertAll", e);
        }
    }
}
