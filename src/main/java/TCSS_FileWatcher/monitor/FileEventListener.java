package TCSS_FileWatcher.monitor;

import TCSS_FileWatcher.domain.FileEvent;

/**
 * Listener for file-system events emitted by the monitor.
 */
@FunctionalInterface
public interface FileEventListener {

    void onFileEvent(FileEvent theEvent);
}