package TCSS_FileWatcher.app;

import java.nio.file.Path;
import java.util.Set;

import TCSS_FileWatcher.domain.QueryCriteria;
import TCSS_FileWatcher.monitor.FileEventListener;
import TCSS_FileWatcher.monitor.FileMonitorService;

public class MonitorController {

    private final FileMonitorService monitor;

    public MonitorController(FileMonitorService monitor) {
        this.monitor = monitor;
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
}
