package TCSS_FileWatcher.app;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import TCSS_FileWatcher.domain.FileEvent;
import TCSS_FileWatcher.domain.QueryCriteria;
import TCSS_FileWatcher.monitor.FileEventListener;
import TCSS_FileWatcher.monitor.FileMonitorService;

public class MonitorController {

    private final FileMonitorService monitor;

    // "current list" of events (for UI + DB write)
    private final List<FileEvent> currentEvents = Collections.synchronizedList(new ArrayList<>());

    // internal listener to accumulate events
    private final FileEventListener collector = currentEvents::add;

    public MonitorController(FileMonitorService monitor) {
        this.monitor = monitor;
        this.monitor.addListener(collector);
    }

    public void startMonitoring(Path directory, Set<String> extensions) {
        currentEvents.clear(); // reset for a new run
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

    public void removeListener(FileEventListener listener) {
        monitor.removeListener(listener);
    }

    /** Snapshot of current events for UI / DB export */
    public List<FileEvent> getCurrentEventsSnapshot() {
        synchronized (currentEvents) {
            return new ArrayList<>(currentEvents);
        }
    }

    public boolean hasAnyEvents() {
        synchronized (currentEvents) {
            return !currentEvents.isEmpty();
        }
    }

    /**
     * Iteration 4 stub: "Write current list to DB".
     * Partner can wire this to DatabaseService later.
     */
    public void writeCurrentListToDb() {
        List<FileEvent> snapshot = getCurrentEventsSnapshot();

        // TODO (Iteration 4/5): call DatabaseService here, e.g.
        // databaseService.insertEvents(snapshot);

        // For now: no-op but keeps UI flow complete.
        System.out.println("Write to DB requested. Events count=" + snapshot.size());
    }
}