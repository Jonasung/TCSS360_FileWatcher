package TCSS_FileWatcher.app;

import TCSS_FileWatcher.monitor.FileMonitorService;
import TCSS_FileWatcher.monitor.WatchServiceMonitor;
import TCSS_FileWatcher.ui.MainWindow;

import javax.swing.SwingUtilities;

/**
 * Application entry point.
 */
public final class MainApp {

    private MainApp() {
        // Utility class.
    }

    public static void main(final String[] theArgs) {
        SwingUtilities.invokeLater(() -> {
            final EventRepository repository = new EventRepository();
            repository.initSchema();

            final FileMonitorService monitor = new WatchServiceMonitor();
            final MonitorController controller = new MonitorController(monitor, repository);

            final MainWindow window = new MainWindow(controller);
            window.setVisible(true);
        });
    }
}