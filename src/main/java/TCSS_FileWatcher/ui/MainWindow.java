package TCSS_FileWatcher.ui;

import javax.swing.*;

import TCSS_FileWatcher.app.MonitorController;
import TCSS_FileWatcher.domain.FileEvent;
import TCSS_FileWatcher.monitor.FileEventListener;

import java.awt.*;
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

    public MainWindow(MonitorController controller) {
        super("TCSS360 FileWatcher - Iteration 3 Demo");
        this.controller = controller;
        this.controller.addListener(this);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

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

        c.gridx = 2; c.gridy = 1; c.weightx = 0;
        top.add(buttons, c);

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
