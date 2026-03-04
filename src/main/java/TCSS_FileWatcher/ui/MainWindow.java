package TCSS_FileWatcher.ui;

import javax.swing.*;

import TCSS_FileWatcher.app.MonitorController;
import TCSS_FileWatcher.domain.FileEvent;
import TCSS_FileWatcher.monitor.FileEventListener;
import TCSS_FileWatcher.ui.query.QueryWindow;

import java.awt.*;
import java.awt.event.*;
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
    private final JButton writeDbBtn = new JButton("Write to DB");

    // Menu items (so we can enable/disable consistently)
    private JMenuItem miStart;
    private JMenuItem miStop;
    private JMenuItem miWriteDb;
    private JMenuItem miExit;

    private QueryWindow queryWindow;

    public MainWindow(MonitorController controller) {
        super("TCSS360 FileWatcher - Iteration 4");
        this.controller = controller;
        this.controller.addListener(this);

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptExit();
            }
        });

        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        setJMenuBar(buildMenuBar());

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
        buttons.add(writeDbBtn);

        c.gridx = 2; c.gridy = 1; c.weightx = 0;
        top.add(buttons, c);

        writeDbBtn.setToolTipText("Write current event list to SQLite database");

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(buildToolBar(), BorderLayout.PAGE_START);
        northPanel.add(top, BorderLayout.CENTER);
        add(northPanel, BorderLayout.NORTH);

        add(new JScrollPane(logArea), BorderLayout.CENTER);

        wireEvents();

        // Initial state
        updateControls();
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu file = new JMenu("File");
        miExit = new JMenuItem("Exit");
        miExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        miExit.addActionListener(e -> attemptExit());
        file.add(miExit);

        JMenu monitor = new JMenu("Monitor");
        miStart = new JMenuItem("Start Monitoring");
        miStart.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        miStart.addActionListener(e -> startFromUI());

        miStop = new JMenuItem("Stop Monitoring");
        miStop.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
        miStop.addActionListener(e -> stopFromUI());

        miWriteDb = new JMenuItem("Write Current List to DB");
        miWriteDb.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
        miWriteDb.addActionListener(e -> writeDbFromUI());

        monitor.add(miStart);
        monitor.add(miStop);
        monitor.addSeparator();
        monitor.add(miWriteDb);

        JMenu database = new JMenu("Database");
        JMenuItem miQuery = new JMenuItem("Query database");
        miQuery.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        miQuery.addActionListener(e -> openQueryWindow());
        database.add(miQuery);

        JMenu help = new JMenu("Help");
        JMenuItem about = new JMenuItem("About");
        about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        about.addActionListener(e -> showAbout());
        help.add(about);

        bar.add(file);
        bar.add(monitor);
        bar.add(database);
        bar.add(help);

        return bar;
    }

    private JToolBar buildToolBar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);

        JButton tbChoose = new JButton("Choose");
        tbChoose.setToolTipText("Choose a directory to monitor");
        tbChoose.addActionListener(e -> chooseFolder());

        JButton tbStart = new JButton("Start");
        tbStart.setToolTipText("Start monitoring (Ctrl+S)");
        tbStart.addActionListener(e -> startFromUI());

        JButton tbStop = new JButton("Stop");
        tbStop.setToolTipText("Stop monitoring (Ctrl+T)");
        tbStop.addActionListener(e -> stopFromUI());

        JButton tbWrite = new JButton("Write DB");
        tbWrite.setToolTipText("Write current event list to DB (Ctrl+D)");
        tbWrite.addActionListener(e -> writeDbFromUI());

        JButton tbQuery = new JButton("Query DB");
        tbQuery.setToolTipText("Query database (Ctrl+Q)");
        tbQuery.addActionListener(e -> openQueryWindow());

        tb.add(tbChoose);
        tb.add(tbStart);
        tb.add(tbStop);
        tb.add(tbWrite);
        tb.add(tbQuery);

        chooseBtn.putClientProperty("TB", tbChoose);
        startBtn.putClientProperty("TB", tbStart);
        stopBtn.putClientProperty("TB", tbStop);
        writeDbBtn.putClientProperty("TB", tbWrite);

        return tb;
    }

    private void openQueryWindow() {
        if (queryWindow == null) {
            queryWindow = new QueryWindow(controller.getRepository(), this);
        }
        queryWindow.open();
    }

    private void wireEvents() {
        chooseBtn.addActionListener(e -> chooseFolder());
        startBtn.addActionListener(e -> startFromUI());
        stopBtn.addActionListener(e -> stopFromUI());
        writeDbBtn.addActionListener(e -> writeDbFromUI());
    }

    private void chooseFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            dirField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
        updateControls();
    }

    private void startFromUI() {
        String dir = dirField.getText().trim();
        if (dir.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please choose a folder first.");
            return;
        }

        Set<String> exts = parseExtensions(extField.getText());
        controller.startMonitoring(Path.of(dir), exts);

        appendLog("=== Monitoring STARTED: " + dir + " | exts=" + exts + " ===");
        updateControls();
    }

    private void stopFromUI() {
        controller.stopMonitoring();
        appendLog("=== Monitoring STOPPED ===");
        updateControls();
    }

    private void writeDbFromUI() {
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
        updateControls();
    }

    private void attemptExit() {
        if (controller.hasUnsavedEvents()) {
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
            return;
        }
        if (controller.isRunning()) {
            int choice = JOptionPane.showConfirmDialog(this,
                "Monitoring is currently running.\nStop monitoring and exit?",
                "Exit",
                JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;
            controller.stopMonitoring();
        } else {
            int choice = JOptionPane.showConfirmDialog(this,
                "Exit the application?",
                "Exit",
                JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) return;
        }
        doExit();
    }

    private void doExit() {
        if (controller.isRunning()) {
            controller.stopMonitoring();
        }
        dispose();
        System.exit(0);
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "TCSS360 FileWatcher\n\n" +
                "Monitor a folder and log file events.\n" +
                "Iteration 4: menus, toolbar, shortcuts, SQLite write, exit prompt.\n\n" +
                "Developers:\n" +
                " - Jonathan Sung\n" +
                " - Abdulrahman Elmi",
            "About",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void updateControls() {
        boolean running = controller.isRunning();

        chooseBtn.setEnabled(!running);
        dirField.setEnabled(!running);
        extField.setEnabled(!running);

        startBtn.setEnabled(!running && !dirField.getText().trim().isEmpty());
        stopBtn.setEnabled(running);

        boolean canWrite = controller.hasAnyEvents();
        writeDbBtn.setEnabled(canWrite);

        if (miStart != null) miStart.setEnabled(startBtn.isEnabled());
        if (miStop != null) miStop.setEnabled(stopBtn.isEnabled());
        if (miWriteDb != null) miWriteDb.setEnabled(writeDbBtn.isEnabled());

        syncToolbarEnabled(chooseBtn);
        syncToolbarEnabled(startBtn);
        syncToolbarEnabled(stopBtn);
        syncToolbarEnabled(writeDbBtn);
    }

    private void syncToolbarEnabled(JButton sourceBtn) {
        Object tb = sourceBtn.getClientProperty("TB");
        if (tb instanceof JButton) {
            ((JButton) tb).setEnabled(sourceBtn.isEnabled());
        }
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
        SwingUtilities.invokeLater(() -> {
            appendLog(event.toString());
            updateControls();
        });
    }

    private void appendLog(String line) {
        logArea.append(line + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }
}
