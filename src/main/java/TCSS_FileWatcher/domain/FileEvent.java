package TCSS_FileWatcher.domain;

import java.nio.file.Path;
import java.time.Instant;

public final class FileEvent {
    private final EventType type;
    private final Path path;
    private final Instant timestamp;

    public FileEvent(EventType type, Path path, Instant timestamp) {
        this.type = type;
        this.path = path;
        this.timestamp = timestamp;
    }

    public EventType getType() {
        return type;
    }

    public Path getPath() {
        return path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + type + " -> " + path;
    }
}
