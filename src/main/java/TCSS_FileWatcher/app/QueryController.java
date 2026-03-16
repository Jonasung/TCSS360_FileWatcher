package TCSS_FileWatcher.app;

import TCSS_FileWatcher.domain.EventType;
import TCSS_FileWatcher.domain.FileEvent;

import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Performs in-memory querying over the current session's buffered events.
 */
public final class QueryController {

    private final MonitorController myMonitorController;

    public QueryController(final MonitorController theMonitorController) {
        myMonitorController = Objects.requireNonNull(theMonitorController, "MonitorController cannot be null.");
    }

    private List<FileEvent> sourceEvents() {
        return myMonitorController.getCurrentEventsSnapshot();
    }

    public List<FileEvent> queryByExtension(final String theRawExtension) {
        final String extension = normalizeExtension(theRawExtension);
        final List<FileEvent> matches = new ArrayList<>();

        for (final FileEvent event : sourceEvents()) {
            if (getExtension(event.getPath()).equals(extension)) {
                matches.add(event);
            }
        }
        return matches;
    }

    public List<FileEvent> queryByDateRange(final LocalDateTime theFrom,
                                            final LocalDateTime theTo) {
        if (theFrom == null || theTo == null) {
            throw new IllegalArgumentException("From and To dates must both be provided.");
        }
        if (theFrom.isAfter(theTo)) {
            throw new IllegalArgumentException("From date must be earlier than or equal to To date.");
        }

        final Instant fromInstant = theFrom.atZone(ZoneId.systemDefault()).toInstant();
        final Instant toInstant = theTo.atZone(ZoneId.systemDefault()).toInstant();
        final List<FileEvent> matches = new ArrayList<>();

        for (final FileEvent event : sourceEvents()) {
            final Instant timestamp = event.getTimestamp();
            final boolean inRange = (timestamp.equals(fromInstant) || timestamp.isAfter(fromInstant))
                    && (timestamp.equals(toInstant) || timestamp.isBefore(toInstant));
            if (inRange) {
                matches.add(event);
            }
        }
        return matches;
    }

    public List<FileEvent> queryByEventType(final EventType theType) {
        if (theType == null) {
            throw new IllegalArgumentException("An event type must be selected.");
        }

        final List<FileEvent> matches = new ArrayList<>();
        for (final FileEvent event : sourceEvents()) {
            if (event.getType() == theType) {
                matches.add(event);
            }
        }
        return matches;
    }

    public List<FileEvent> queryByPathPrefix(final String theRawPath) {
        if (theRawPath == null || theRawPath.isBlank()) {
            throw new IllegalArgumentException("A path must be provided.");
        }

        final Path prefix = Path.of(theRawPath.trim()).toAbsolutePath().normalize();
        final List<FileEvent> matches = new ArrayList<>();
        for (final FileEvent event : sourceEvents()) {
            final Path eventPath = event.getPath().toAbsolutePath().normalize();
            if (eventPath.equals(prefix) || eventPath.startsWith(prefix)) {
                matches.add(event);
            }
        }
        return matches;
    }

    private static String normalizeExtension(final String theRawExtension) {
        if (theRawExtension == null) {
            throw new IllegalArgumentException("An extension must be provided.");
        }

        String extension = theRawExtension.trim().toLowerCase(Locale.ROOT);
        if (extension.startsWith(".")) {
            extension = extension.substring(1);
        }
        if (extension.isBlank()) {
            throw new IllegalArgumentException("An extension must be provided.");
        }
        return extension;
    }

    private static String getExtension(final Path thePath) {
        final Path fileName = thePath.getFileName();
        final String name = fileName == null ? thePath.toString() : fileName.toString();
        final int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == name.length() - 1) {
            return "";
        }
        return name.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}