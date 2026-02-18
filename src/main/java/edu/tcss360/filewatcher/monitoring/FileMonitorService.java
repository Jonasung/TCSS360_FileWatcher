package edu.tcss360.filewatcher.monitoring;

import java.nio.file.Path;
import java.util.Set;

/**
 * Service that monitors a directory for file system events and notifies a listener.
 * <p>
 * Events are delivered on a background thread (WatchService). Implementations of
 * {@link FileEventListener#onFileEvent} that update the UI must schedule work on the EDT
 * via {@code SwingUtilities.invokeLater(...)}.
 * </p>
 */
public interface FileMonitorService {

    void setListener(FileEventListener listener);

    void setWatchedExtensions(Set<String> extensions);

    void start(Path directory);

    void stop();

    /**
     * Returns true if monitoring is currently active, so the UI can enable/disable Start vs Stop.
     */
    boolean isRunning();
}
