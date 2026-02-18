package edu.tcss360.filewatcher.domain;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Criteria for querying stored file events (extension, event type, path, date range).
 */
public final class QueryCriteria {

    private final String extension;
    private final EventType eventType;
    private final String directoryPath;
    private final Instant startTime;
    private final Instant endTime;

    public QueryCriteria(String extension, EventType eventType, String directoryPath,
                         Instant startTime, Instant endTime) {
        this.extension = extension;
        this.eventType = eventType;
        this.directoryPath = directoryPath;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getExtension() { return extension; }
    public EventType getEventType() { return eventType; }
    public String getDirectoryPath() { return directoryPath; }
    public Instant getStartTime() { return startTime; }
    public Instant getEndTime() { return endTime; }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String extension;
        private EventType eventType;
        private String directoryPath;
        private Instant startTime;
        private Instant endTime;

        public Builder extension(String ext) { this.extension = ext; return this; }
        public Builder eventType(EventType type) { this.eventType = type; return this; }
        public Builder directoryPath(String path) { this.directoryPath = path; return this; }
        public Builder startTime(Instant start) { this.startTime = start; return this; }
        public Builder endTime(Instant end) { this.endTime = end; return this; }
        public QueryCriteria build() { return new QueryCriteria(extension, eventType, directoryPath, startTime, endTime); }
    }

    private static final DateTimeFormatter FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    /**
     * Returns a short summary of the query (for CSV header / report title).
     */
    public String getQuerySummary() {
        StringBuilder sb = new StringBuilder("Query: ");
        boolean any = false;
        if (extension != null && !extension.isBlank()) {
            sb.append("extension=").append(extension);
            any = true;
        }
        if (eventType != null) {
            if (any) sb.append(", ");
            sb.append("eventType=").append(eventType.name());
            any = true;
        }
        if (directoryPath != null && !directoryPath.isBlank()) {
            if (any) sb.append(", ");
            sb.append("path=").append(directoryPath);
            any = true;
        }
        if (startTime != null) {
            if (any) sb.append(", ");
            sb.append("from=").append(FORMAT.format(startTime));
            any = true;
        }
        if (endTime != null) {
            if (any) sb.append(", ");
            sb.append("to=").append(FORMAT.format(endTime));
        }
        if (!any) sb.append("(no filters)");
        return sb.toString();
    }
}
