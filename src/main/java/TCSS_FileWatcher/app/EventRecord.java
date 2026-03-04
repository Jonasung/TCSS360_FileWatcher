package TCSS_FileWatcher.app;

/**
 * One row from a query: file name, path, event type, date/time.
 * Extension is derived from file name for display.
 */
public final class EventRecord {
    private final String fileName;
    private final String absolutePath;
    private final String eventType;
    private final String eventDateTime;

    public EventRecord(String fileName, String absolutePath, String eventType, String eventDateTime) {
        this.fileName = fileName != null ? fileName : "";
        this.absolutePath = absolutePath != null ? absolutePath : "";
        this.eventType = eventType != null ? eventType : "";
        this.eventDateTime = eventDateTime != null ? eventDateTime : "";
    }

    public String getFileName() { return fileName; }
    public String getAbsolutePath() { return absolutePath; }
    public String getEventType() { return eventType; }
    public String getEventDateTime() { return eventDateTime; }

    /** Extension (part after last dot); empty if none. */
    public String getExtension() {
        int i = fileName.lastIndexOf('.');
        if (i < 0 || i == fileName.length() - 1) return "";
        return fileName.substring(i + 1).toLowerCase();
    }
}
