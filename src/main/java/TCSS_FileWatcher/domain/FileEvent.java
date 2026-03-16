package TCSS_FileWatcher.domain;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable value object describing a single observed file-system event.
 */
public final class FileEvent {

    private final EventType myType;
    private final Path myPath;
    private final Instant myTimestamp;

    public FileEvent(final EventType theType,
                     final Path thePath,
                     final Instant theTimestamp) {
        myType = Objects.requireNonNull(theType, "Event type cannot be null.");
        myPath = Objects.requireNonNull(thePath, "Path cannot be null.");
        myTimestamp = Objects.requireNonNull(theTimestamp, "Timestamp cannot be null.");
    }

    public EventType getType() {
        return myType;
    }

    public Path getPath() {
        return myPath;
    }

    public Instant getTimestamp() {
        return myTimestamp;
    }

    @Override
    public String toString() {
        return "[" + myTimestamp + "] " + myType + " -> " + myPath;
    }
}