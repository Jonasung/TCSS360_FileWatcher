package TCSS_FileWatcher.app;

import TCSS_FileWatcher.domain.EventType;
import TCSS_FileWatcher.domain.FileEvent;

import java.nio.file.Path;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class QueryController {

    private final MonitorController monitorController;

    public QueryController(MonitorController monitorController) {
        this.monitorController = Objects.requireNonNull(monitorController);
    }

    /** Source for now = current session events (Iteration 5). Later: replace with DB results. */
    private List<FileEvent> sourceEvents() {
        return monitorController.getCurrentEventsSnapshot();
    }

    public List<FileEvent> queryByExtension(String extRaw) {
        String ext = normalizeExt(extRaw);
        List<FileEvent> out = new ArrayList<>();
        for (FileEvent e : sourceEvents()) {
            String eventExt = getExtension(e.getPath());
            if (eventExt.equals(ext)) out.add(e);
        }
        return out;
    }

    public List<FileEvent> queryByDateRange(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) throw new IllegalArgumentException("From/To must be provided.");
        if (from.isAfter(to)) throw new IllegalArgumentException("From must be <= To.");

        Instant fromI = from.atZone(ZoneId.systemDefault()).toInstant();
        Instant toI = to.atZone(ZoneId.systemDefault()).toInstant();

        List<FileEvent> out = new ArrayList<>();
        for (FileEvent e : sourceEvents()) {
            Instant ts = e.getTimestamp(); // requires getter
            if ((ts.equals(fromI) || ts.isAfter(fromI)) && (ts.equals(toI) || ts.isBefore(toI))) {
                out.add(e);
            }
        }
        return out;
    }

    public List<FileEvent> queryByEventType(EventType type) {
        if (type == null) throw new IllegalArgumentException("Event type must be selected.");
        List<FileEvent> out = new ArrayList<>();
        for (FileEvent e : sourceEvents()) {
            if (e.getType() == type) out.add(e);
        }
        return out;
    }

    public List<FileEvent> queryByPathPrefix(String pathRaw) {
        if (pathRaw == null || pathRaw.isBlank()) throw new IllegalArgumentException("Path must be provided.");
        Path prefix = Path.of(pathRaw.trim()).toAbsolutePath().normalize();

        List<FileEvent> out = new ArrayList<>();
        for (FileEvent e : sourceEvents()) {
            Path p = e.getPath().toAbsolutePath().normalize();
            if (p.equals(prefix) || p.startsWith(prefix)) out.add(e);
        }
        return out;
    }

    private static String normalizeExt(String extRaw) {
        if (extRaw == null) throw new IllegalArgumentException("Extension must be provided.");
        String ext = extRaw.trim().toLowerCase(Locale.ROOT);
        if (ext.startsWith(".")) ext = ext.substring(1);
        if (ext.isBlank()) throw new IllegalArgumentException("Extension must be provided.");
        return ext;
    }

    private static String getExtension(Path path) {
        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return "";
        return name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}