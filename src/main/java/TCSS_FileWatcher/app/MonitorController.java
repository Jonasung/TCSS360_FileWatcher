package TCSS_FileWatcher.app;

import TCSS_FileWatcher.domain.FileEvent;
import TCSS_FileWatcher.domain.QueryCriteria;
import TCSS_FileWatcher.monitor.FileEventListener;
import TCSS_FileWatcher.monitor.FileMonitorService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Coordinates monitoring and persistence for file events.
 */
public final class MonitorController implements FileEventListener {

    private final FileMonitorService myMonitor;
    private final EventRepository myRepository;
    private final List<FileEvent> myEventBuffer;

    public MonitorController(final FileMonitorService theMonitor,
                             final EventRepository theRepository) {
        myMonitor = Objects.requireNonNull(theMonitor, "Monitor service cannot be null.");
        myRepository = Objects.requireNonNull(theRepository, "Repository cannot be null.");
        myEventBuffer = new CopyOnWriteArrayList<>();
        myMonitor.addListener(this);
    }

    @Override
    public void onFileEvent(final FileEvent theEvent) {
        if (theEvent != null) {
            myEventBuffer.add(theEvent);
        }
    }

    public void startMonitoring(final Path theDirectory,
                                final Set<String> theExtensions) {
        final QueryCriteria criteria = new QueryCriteria(theExtensions);
        myMonitor.start(theDirectory, criteria);
    }

    public void stopMonitoring() {
        myMonitor.stop();
    }

    public boolean isRunning() {
        return myMonitor.isRunning();
    }

    public void addListener(final FileEventListener theListener) {
        myMonitor.addListener(theListener);
    }

    public void removeListener(final FileEventListener theListener) {
        myMonitor.removeListener(theListener);
    }

    /**
     * Writes buffered events to the database and clears the in-memory buffer.
     *
     * @return the number of events written
     */
    public int writeToDatabase() {
        if (myEventBuffer.isEmpty()) {
            return 0;
        }

        myRepository.initSchema();
        final List<FileEvent> snapshot = new ArrayList<>(myEventBuffer);
        myRepository.insertAll(snapshot);
        myEventBuffer.clear();
        return snapshot.size();
    }

    public boolean hasUnsavedEvents() {
        return !myEventBuffer.isEmpty();
    }

    public boolean hasAnyEvents() {
        return !myEventBuffer.isEmpty();
    }

    /**
     * Returns a defensive copy of the current session's events.
     *
     * @return a snapshot of the current event buffer
     */
    public List<FileEvent> getCurrentEventsSnapshot() {
        return new ArrayList<>(myEventBuffer);
    }
}