package TCSS_FileWatcher.app;

import javax.swing.*;

import TCSS_FileWatcher.monitor.FileMonitorService;
import TCSS_FileWatcher.monitor.WatchServiceMonitor;
import TCSS_FileWatcher.ui.MainWindow;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileMonitorService monitor = new WatchServiceMonitor();
            MonitorController controller = new MonitorController(monitor);

            MainWindow window = new MainWindow(controller);
            window.setVisible(true);
        });
    }
}
