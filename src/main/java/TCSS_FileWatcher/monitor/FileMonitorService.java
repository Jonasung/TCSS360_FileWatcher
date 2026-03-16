package TCSS_FileWatcher.monitor;

import TCSS_FileWatcher.domain.QueryCriteria;

import java.nio.file.Path;

/**
 * Abstraction for a service that watches a directory for file changes.
 */
public interface FileMonitorService {

    void start(Path theDirectory, QueryCriteria theCriteria);

    void stop();

    boolean isRunning();

    void addListener(FileEventListener theListener);

    void removeListener(FileEventListener theListener);
}