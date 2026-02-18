package edu.tcss360.filewatcher.monitoring;

import edu.tcss360.filewatcher.domain.FileEvent;

/**
 * Listener notified when a file system event is detected.
 * <p>
 * Example: A UI implementation should append the event to the live list on the EDT, e.g.
 * {@code SwingUtilities.invokeLater(() -> eventListModel.addElement(event.toDisplayLine())); }
 * </p>
 */
@FunctionalInterface
public interface FileEventListener {

    void onFileEvent(FileEvent event);

    /**
     * Called when an event was filtered out (e.g. extension not in watch list). Default: no-op; override for logging.
     */
    default void onEventIgnored(FileEvent event) {
        // no-op
    }
}
