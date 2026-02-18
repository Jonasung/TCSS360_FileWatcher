package edu.tcss360.filewatcher.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * A single file system event (created, modified, deleted, renamed, moved).
 */
public final class FileEvent {

    private final Long id;
    private final String fileName;
    private final String extension;
    private final String absolutePath;
    private final EventType eventType;
    private final Instant timestamp;

    public FileEvent(String fileName, String extension, String absolutePath,
                     EventType eventType, Instant timestamp) {
        this(null, fileName, extension, absolutePath, eventType, timestamp);
    }

    public FileEvent(Long id, String fileName, String extension, String absolutePath,
                     EventType eventType, Instant timestamp) {
        this.id = id;
        this.fileName = fileName;
        this.extension = extension;
        this.absolutePath = absolutePath;
        this.eventType = eventType;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
    }

    public Long getId() { return id; }
    public String getFileName() { return fileName; }
    public String getExtension() { return extension; }
    public String getAbsolutePath() { return absolutePath; }
    public EventType getEventType() { return eventType; }
    public Instant getTimestamp() { return timestamp; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileEvent fileEvent = (FileEvent) o;
        return Objects.equals(fileName, fileEvent.fileName)
            && Objects.equals(extension, fileEvent.extension)
            && Objects.equals(absolutePath, fileEvent.absolutePath)
            && eventType == fileEvent.eventType
            && Objects.equals(timestamp, fileEvent.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, extension, absolutePath, eventType, timestamp);
    }

    /**
     * Formats this event as a single line for the live event list.
     * Example: "[CREATED] file.txt at C:\path\to\file.txt"
     */
    public String toDisplayLine() {
        return "[" + eventType.name() + "] " + fileName + " at " + absolutePath;
    }
}
