package edu.tcss360.filewatcher.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Top-level application: starts the GUI and shuts down cleanly.
 */
public final class FileWatcherApp {

    private static FileWatcherApp instance;

    private JFrame mainFrame;
    private QueryWindow queryWindow;

    public void start() {
        SwingUtilities.invokeLater(() -> {
            mainFrame = new JFrame("FileWatcher");
            mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            mainFrame.setSize(400, 160);
            mainFrame.setLayout(new BorderLayout());

            JMenuBar menuBar = new JMenuBar();
            JMenu fileMenu = new JMenu("File");
            JMenuItem openQueryItem = new JMenuItem("Open Query Window");
            openQueryItem.addActionListener(e -> openQueryWindow());
            fileMenu.add(openQueryItem);
            menuBar.add(fileMenu);

            JMenu helpMenu = new JMenu("Help");
            JMenuItem aboutItem = new JMenuItem("About");
            aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(mainFrame,
                "FileWatcher - Sprint 1\nMonitor file system events.", "About", JOptionPane.INFORMATION_MESSAGE));
            helpMenu.add(aboutItem);
            menuBar.add(helpMenu);
            mainFrame.setJMenuBar(menuBar);

            JButton openQueryBtn = new JButton("Open Query Window");
            openQueryBtn.addActionListener(e -> openQueryWindow());
            mainFrame.add(openQueryBtn, BorderLayout.CENTER);

            mainFrame.setVisible(true);
        });
    }

    public void shutdown() {
        if (mainFrame != null) {
            SwingUtilities.invokeLater(() -> {
                if (queryWindow != null) {
                    queryWindow.dispose();
                }
                mainFrame.dispose();
                mainFrame = null;
            });
        }
    }

    private void openQueryWindow() {
        if (queryWindow == null || !queryWindow.isDisplayable()) {
            queryWindow = new QueryWindow();
            queryWindow.render();
        }
        queryWindow.setVisible(true);
    }

    public static void main(String[] args) {
        instance = new FileWatcherApp();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (instance != null) {
                instance.shutdown();
            }
        }));
        instance.start();
    }
}
