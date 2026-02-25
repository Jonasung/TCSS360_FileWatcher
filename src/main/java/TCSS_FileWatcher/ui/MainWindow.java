package TCSS_FileWatcher.ui;

import javax.swing.*;

import TCSS_FileWatcher.app.MonitorController;
import TCSS_FileWatcher.domain.FileEvent;
import TCSS_FileWatcher.monitor.FileEventListener;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class MainWindow extends JFrame implements FileEventListener {

    private final MonitorController controller;

    private final JTextField dirField = new JTextField();
    private final JTextField extField = new JTextField("txt,java");
    private final JTextArea logArea = new JTextArea();

    private final JButton chooseBtn = new JButton("Choose Folder");
    private final JButton startBtn = new JButton("Start");
    private final JButton stopBtn = new JButton("Stop");
    private final JButton writeToDbBtn = new JButton("Write to DB");

    public MainWindow(MonitorController controller) {
        super("TCSS360 FileWatcher - Iteration 3 Demo");
        this.controller = controller;
        this.controller.addListener(this);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                MainWindow.this.confirmExit();
            }
        });

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JPanel top = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; c.weightx = 0;
        top.add(new JLabel("Directory:"), c);

        c.gridx = 1; c.gridy = 0; c.weightx = 1;
        top.add(dirField, c);

        c.gridx = 2; c.gridy = 0; c.weightx = 0;
        top.add(chooseBtn, c);

        c.gridx = 0; c.gridy = 1; c.weightx = 0;
        top.add(new JLabel("Extensions (comma):"), c);

        c.gridx = 1; c.gridy = 1; c.weightx = 1;
        top.add(extField, c);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(startBtn);
        buttons.add(stopBtn);
        buttons.add(writeToDbBtn);

        c.gridx = 2; c.gridy = 1; c.weightx = 0;
        top.add(buttons, c);

        writeToDbBtn.setToolTipText("Write current event list to SQLite database");

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        stopBtn.setEnabled(false);

        wireEvents();
    }

    private void wireEvents() {
        chooseBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                dirField.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        });

        startBtn.addActionListener(e -> {
            String dir = dirField.getText().trim();
            if (dir.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please choose a folder first.");
                return;
            }

            Set<String> exts = parseExtensions(extField.getText());
            controller.startMonitoring(Path.of(dir), exts);

            appendLog("=== Monitoring STARTED: " + dir + " | exts=" + exts + " ===");
            startBtn.setEnabled(false);
            stopBtn.setEnabled(true);
        });

        stopBtn.addActionListener(e -> {
            controller.stopMonitoring();
            appendLog("=== Monitoring STOPPED ===");
            startBtn.setEnabled(true);
            stopBtn.setEnabled(false);
        });

        writeToDbBtn.addActionListener(e -> {
            try {
                int count = controller.writeToDatabase();
                if (count > 0) {
                    appendLog("=== Written " + count + " event(s) to database ===");
                    JOptionPane.showMessageDialog(this, "Written " + count + " event(s) to database.");
                } else {
                    JOptionPane.showMessageDialog(this, "No events to write. Start monitoring and generate some events first.");
                }
            } catch (Exception ex) {
                appendLog("=== Write to DB failed: " + ex.getMessage() + " ===");
                JOptionPane.showMessageDialog(this, "Failed to write to database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void confirmExit() {
        if (!controller.hasUnsavedEvents()) {
            doExit();
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
            "Write current contents to the database before exiting?",
            "Unsaved events",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                int count = controller.writeToDatabase();
                appendLog("=== Written " + count + " event(s) to database before exit ===");
                doExit();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to write to database: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (choice == JOptionPane.NO_OPTION) {
            doExit();
        }
        /* CANCEL: do nothing, stay open */
    }

    private void doExit() {
        if (controller.isRunning()) {
            controller.stopMonitoring();
        }
        dispose();
        System.exit(0);
    }

    private static Set<String> parseExtensions(String text) {
        Set<String> set = new HashSet<>();
        if (text == null || text.isBlank()) return set;

        Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.startsWith(".") ? s.substring(1) : s)
                .map(String::toLowerCase)
                .forEach(set::add);

        return set;
    }

    @Override
    public void onFileEvent(FileEvent event) {
        // WatchService thread → UI thread
        SwingUtilities.invokeLater(() -> appendLog(event.toString()));
    }

    private void appendLog(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
