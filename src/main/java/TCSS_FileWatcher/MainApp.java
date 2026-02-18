package TCSS_FileWatcher;

import javax.swing.*;

public class MainApp {
    static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileMonitorService monitor = new WatchServiceMonitor();
            MonitorController controller = new MonitorController(monitor);

            MainWindow window = new MainWindow(controller);
            window.setVisible(true);
        });
    }
}
