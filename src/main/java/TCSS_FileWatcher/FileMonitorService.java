package TCSS_FileWatcher;

public interface FileMonitorService {

    void startMonitoring();

    void stopMonitoring();

    void addListener(FileEventListener listener);

}
