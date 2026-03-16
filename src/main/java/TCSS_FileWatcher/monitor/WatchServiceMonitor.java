package TCSS_FileWatcher.monitor;

import TCSS_FileWatcher.domain.EventType;
import TCSS_FileWatcher.domain.FileEvent;
import TCSS_FileWatcher.domain.QueryCriteria;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@link FileMonitorService} implementation backed by Java's {@link WatchService}.
 */
public final class WatchServiceMonitor implements FileMonitorService {

    private final List<FileEventListener> myListeners;
    private volatile boolean myRunning;
    private Thread myWorker;
    private WatchService myWatchService;

    public WatchServiceMonitor() {
        myListeners = new CopyOnWriteArrayList<>();
    }

    @Override
    public void start(final Path theDirectory,
                      final QueryCriteria theCriteria) {
        if (myRunning) {
            return;
        }
        if (theDirectory == null) {
            throw new IllegalArgumentException("Directory cannot be null.");
        }
        if (!Files.isDirectory(theDirectory)) {
            throw new IllegalArgumentException("Not a directory: " + theDirectory);
        }

        myRunning = true;
        myWorker = new Thread(() -> runLoop(theDirectory, theCriteria), "WatchServiceMonitor-Thread");
        myWorker.setDaemon(true);
        myWorker.start();
    }

    private void runLoop(final Path theDirectory,
                         final QueryCriteria theCriteria) {
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            myWatchService = watchService;
            theDirectory.register(
                    watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
            );

            while (myRunning) {
                final WatchKey key;
                try {
                    key = watchService.take();
                } catch (final InterruptedException theException) {
                    Thread.currentThread().interrupt();
                    break;
                }

                for (final WatchEvent<?> event : key.pollEvents()) {
                    if (!myRunning) {
                        break;
                    }

                    final WatchEvent.Kind<?> kind = event.kind();
                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    @SuppressWarnings("unchecked")
                    final WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                    final Path fullPath = theDirectory.resolve(pathEvent.context());
                    final EventType eventType = toEventType(kind);

                    if (eventType != null && ExtensionFilter.matches(fullPath, theCriteria)) {
                        notifyListeners(new FileEvent(eventType, fullPath, Instant.now()));
                    }
                }

                if (!key.reset()) {
                    break;
                }
            }
        } catch (final IOException theException) {
            // Keep fallback behavior simple for the course project.
            notifyListeners(new FileEvent(EventType.MODIFIED, theDirectory, Instant.now()));
        } finally {
            myRunning = false;
        }
    }

    private static EventType toEventType(final WatchEvent.Kind<?> theKind) {
        if (theKind == StandardWatchEventKinds.ENTRY_CREATE) {
            return EventType.CREATED;
        }
        if (theKind == StandardWatchEventKinds.ENTRY_MODIFY) {
            return EventType.MODIFIED;
        }
        if (theKind == StandardWatchEventKinds.ENTRY_DELETE) {
            return EventType.DELETED;
        }
        return null;
    }

    private void notifyListeners(final FileEvent theEvent) {
        for (final FileEventListener listener : myListeners) {
            try {
                listener.onFileEvent(theEvent);
            } catch (final RuntimeException ignored) {
                // Prevent UI or listener failures from crashing the monitor thread.
            }
        }
    }

    @Override
    public void stop() {
        myRunning = false;

        try {
            if (myWatchService != null) {
                myWatchService.close();
            }
        } catch (final IOException ignored) {
            // Nothing else to do here.
        }

        if (myWorker != null) {
            myWorker.interrupt();
        }
    }

    @Override
    public boolean isRunning() {
        return myRunning;
    }

    @Override
    public void addListener(final FileEventListener theListener) {
        if (theListener != null) {
            myListeners.add(theListener);
        }
    }

    @Override
    public void removeListener(final FileEventListener theListener) {
        myListeners.remove(theListener);
    }
}