package TCSS_FileWatcher;

import java.nio.file.Path;

public interface FileMonitorService {
    void start(Path directory, QueryCriteria criteria);
    void stop();
    boolean isRunning();
    void addListener(FileEventListener listener);
    void removeListener(FileEventListener listener);
}
