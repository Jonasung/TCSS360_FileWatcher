package TCSS_FileWatcher.app;

import TCSS_FileWatcher.domain.FileEvent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

/**
 * Persists {@link FileEvent} records in a local SQLite database.
 */
public final class EventRepository {

    private static final String DEFAULT_DB_DIR = ".filewatcher";
    private static final String DEFAULT_DB_FILE = "filewatcher.db";
    private static final String JDBC_PREFIX = "jdbc:sqlite:";
    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS file_events ("
            + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
            + "file_name TEXT NOT NULL,"
            + "absolute_path TEXT NOT NULL,"
            + "event_type TEXT NOT NULL,"
            + "event_datetime TEXT NOT NULL"
            + ")";
    private static final String INSERT_SQL =
            "INSERT INTO file_events (file_name, absolute_path, event_type, event_datetime) VALUES (?, ?, ?, ?)";

    private final String myDbPath;

    public EventRepository() {
        this(buildDefaultDbPath());
    }

    public EventRepository(final String theDbPath) {
        myDbPath = Objects.requireNonNull(theDbPath, "Database path cannot be null.");
    }

    private static String buildDefaultDbPath() {
        final String home = System.getProperty("user.home");
        final Path directory = Paths.get(home, DEFAULT_DB_DIR);
        try {
            Files.createDirectories(directory);
        } catch (final IOException theException) {
            throw new IllegalStateException("Could not create DB directory: " + directory, theException);
        }
        return directory.resolve(DEFAULT_DB_FILE).toAbsolutePath().toString();
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_PREFIX + myDbPath);
    }

    /**
     * Creates the required database table if it does not already exist.
     */
    public void initSchema() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute(CREATE_TABLE_SQL);
        } catch (final SQLException theException) {
            throw new IllegalStateException("Failed to initialize the database schema.", theException);
        }
    }

    /**
     * Inserts a single event.
     *
     * @param theEvent the event to persist
     */
    public void insert(final FileEvent theEvent) {
        if (theEvent == null) {
            return;
        }

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            populateInsertStatement(statement, theEvent);
            statement.executeUpdate();
        } catch (final SQLException theException) {
            throw new IllegalStateException("Failed to insert event: " + theEvent, theException);
        }
    }

    /**
     * Inserts multiple events within a single transaction.
     *
     * @param theEvents the events to persist
     */
    public void insertAll(final Iterable<FileEvent> theEvents) {
        Objects.requireNonNull(theEvents, "Event collection cannot be null.");

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            connection.setAutoCommit(false);
            try {
                for (final FileEvent event : theEvents) {
                    if (event == null) {
                        continue;
                    }
                    populateInsertStatement(statement, event);
                    statement.executeUpdate();
                }
                connection.commit();
            } catch (final SQLException theException) {
                connection.rollback();
                throw new IllegalStateException("Failed to insert events.", theException);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (final SQLException theException) {
            throw new IllegalStateException("Failed to open database connection for batch insert.", theException);
        }
    }

    private void populateInsertStatement(final PreparedStatement theStatement,
                                         final FileEvent theEvent) throws SQLException {
        final String fileName = theEvent.getPath().getFileName() != null
                ? theEvent.getPath().getFileName().toString()
                : theEvent.getPath().toString();

        theStatement.setString(1, fileName);
        theStatement.setString(2, theEvent.getPath().toAbsolutePath().toString());
        theStatement.setString(3, theEvent.getType().name());
        theStatement.setString(4, theEvent.getTimestamp().toString());
    }
}