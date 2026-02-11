package TCSS_FileWatcher;

public class QueryCriteria {

    private EventType eventType;

    public QueryCriteria(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }
}
