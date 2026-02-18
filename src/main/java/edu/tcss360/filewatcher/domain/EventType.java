package edu.tcss360.filewatcher.domain;

/**
 * Types of file system events detected by the monitor.
 */
public enum EventType {
    CREATED,
    MODIFIED,
    DELETED,
    RENAMED,
    MOVED;

    /**
     * Returns a user-friendly display label for this event type.
     */
    public String getDisplayLabel() {
        return switch (this) {
            case CREATED -> "Created";
            case MODIFIED -> "Modified";
            case DELETED -> "Deleted";
            case RENAMED -> "Renamed";
            case MOVED -> "Moved";
        };
    }

    /**
     * Maps a WatchService StandardWatchEventKinds-style name to this enum.
     * Supports: ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE; overflow maps to MODIFIED.
     */
    public static EventType fromWatchEventKind(String kindName) {
        if (kindName == null) return MODIFIED;
        return switch (kindName) {
            case "ENTRY_CREATE" -> CREATED;
            case "ENTRY_MODIFY" -> MODIFIED;
            case "ENTRY_DELETE" -> DELETED;
            default -> MODIFIED;
        };
    }
}
