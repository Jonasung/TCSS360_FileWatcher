package TCSS_FileWatcher.monitor;

import TCSS_FileWatcher.domain.FileEvent;

public interface FileEventListener {
    void onFileEvent(FileEvent event);
}
