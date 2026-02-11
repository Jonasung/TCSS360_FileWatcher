package TCSS_FileWatcher;

import java.time.LocalDateTime;

public class FileEvent {

    private String filePath;
    private EventType eventType;
    private LocalDateTime timestamp;

    public FileEvent(String filePath, EventType eventType) {
        this.filePath = filePath;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }

    public String getFilePath() {
        return filePath;
    }

    public EventType getEventType() {
        return eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
