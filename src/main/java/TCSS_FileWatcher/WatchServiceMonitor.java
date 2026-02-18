package TCSS_FileWatcher;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.nio.file.StandardWatchEventKinds.*;

public class WatchServiceMonitor implements FileMonitorService {

    private final List<FileEventListener> listeners = new CopyOnWriteArrayList<>();
    private volatile boolean running = false;

    private Thread worker;
    private WatchService watchService;

    @Override
    public void start(Path directory, QueryCriteria criteria) {
        if (running) return;
        if (directory == null) throw new IllegalArgumentException("directory cannot be null");
        if (!Files.isDirectory(directory)) throw new IllegalArgumentException("Not a directory: " + directory);

        running = true;

        worker = new Thread(() -> runLoop(directory, criteria), "WatchServiceMonitor-Thread");
        worker.setDaemon(true);
        worker.start();
    }

    private void runLoop(Path directory, QueryCriteria criteria) {
        try (WatchService ws = FileSystems.getDefault().newWatchService()) {
            this.watchService = ws;

            directory.register(ws, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

            while (running) {
                WatchKey key;
                try {
                    key = ws.take(); // blocks
                } catch (InterruptedException e) {
                    break;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (!running) break;

                    WatchEvent.Kind<?> kind = event.kind();
                    if (kind == OVERFLOW) continue;

                    @SuppressWarnings("unchecked")
                    WatchEvent<Path> ev = (WatchEvent<Path>) event;

                    Path relative = ev.context();
                    Path fullPath = directory.resolve(relative);

                    EventType type = toEventType(kind);
                    if (type == null) continue;

                    // filter
                    if (!ExtensionFilter.matches(fullPath, criteria)) continue;

                    FileEvent fileEvent = new FileEvent(type, fullPath, Instant.now());
                    notifyListeners(fileEvent);
                }

                boolean valid = key.reset();
                if (!valid) break;
            }
        } catch (IOException ex) {
            notifyListeners(new FileEvent(EventType.MODIFIED, directory, Instant.now())); // fallback “something happened”
        } finally {
            running = false;
        }
    }

    private static EventType toEventType(WatchEvent.Kind<?> kind) {
        if (kind == ENTRY_CREATE) return EventType.CREATED;
        if (kind == ENTRY_MODIFY) return EventType.MODIFIED;
        if (kind == ENTRY_DELETE) return EventType.DELETED;
        return null;
    }

    private void notifyListeners(FileEvent event) {
        for (FileEventListener l : listeners) {
            try {
                l.onFileEvent(event);
            } catch (Exception ignored) {
                // avoid crashing monitor due to UI exceptions
            }
        }
    }

    @Override
    public void stop() {
        running = false;

        try {
            if (watchService != null) watchService.close();
        } catch (IOException ignored) {}

        if (worker != null) worker.interrupt();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public void addListener(FileEventListener listener) {
        if (listener != null) listeners.add(listener);
    }

    @Override
    public void removeListener(FileEventListener listener) {
        listeners.remove(listener);
    }
}
