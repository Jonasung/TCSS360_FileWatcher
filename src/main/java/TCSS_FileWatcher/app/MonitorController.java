package TCSS_FileWatcher.app;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import TCSS_FileWatcher.domain.FileEvent;
import TCSS_FileWatcher.domain.QueryCriteria;
import TCSS_FileWatcher.monitor.FileEventListener;
import TCSS_FileWatcher.monitor.FileMonitorService;

public class MonitorController implements FileEventListener {

    private final FileMonitorService monitor;
    private final EventRepository repository;
    private final List<FileEvent> eventBuffer = new CopyOnWriteArrayList<>();

    public MonitorController(FileMonitorService monitor, EventRepository repository) {
        this.monitor = monitor;
        this.repository = repository;
        this.monitor.addListener(this);
    }

    @Override
    public void onFileEvent(FileEvent event) {
        if (event != null) {
            eventBuffer.add(event);
        }
    }

    public void startMonitoring(Path directory, Set<String> extensions) {
        QueryCriteria criteria = new QueryCriteria(extensions);
        monitor.start(directory, criteria);
    }

    public void stopMonitoring() {
        monitor.stop();
    }

    public boolean isRunning() {
        return monitor.isRunning();
    }

    public void addListener(FileEventListener listener) {
        monitor.addListener(listener);
    }

    /**
     * Writes all buffered events to the database and clears the buffer.
     * @return number of events written
     */
    public int writeToDatabase() {
        if (eventBuffer.isEmpty()) {
            return 0;
        }
        repository.initSchema();
        List<FileEvent> snapshot = new ArrayList<>(eventBuffer);
        repository.insertAll(snapshot);
        eventBuffer.clear();
        return snapshot.size();
    }

    /** Returns true if there are events in the buffer not yet written to the database. */
    public boolean hasUnsavedEvents() {
        return !eventBuffer.isEmpty();
    }
}
