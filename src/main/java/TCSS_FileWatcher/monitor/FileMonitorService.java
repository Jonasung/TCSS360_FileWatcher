package TCSS_FileWatcher.monitor;

import java.nio.file.Path;

import TCSS_FileWatcher.domain.QueryCriteria;

public interface FileMonitorService {
    void start(Path directory, QueryCriteria criteria);
    void stop();
    boolean isRunning();
    void addListener(FileEventListener listener);
    void removeListener(FileEventListener listener);
}
