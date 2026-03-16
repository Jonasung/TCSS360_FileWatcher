package TCSS_FileWatcher.ui;

import TCSS_FileWatcher.app.MonitorController;
import TCSS_FileWatcher.app.QueryController;
import TCSS_FileWatcher.domain.FileEvent;
import TCSS_FileWatcher.monitor.FileEventListener;
import TCSS_FileWatcher.ui.query.QueryWindow;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Main application window for monitoring directories and managing captured events.
 */
public final class MainWindow extends JFrame implements FileEventListener {

    private static final long serialVersionUID = 1L;

    private static final String APPLICATION_TITLE = "TCSS360 FileWatcher";
    private static final String DEFAULT_EXTENSIONS = "txt,java";
    private static final String TOOLBAR_BUTTON_KEY = "toolbarButton";

    private final MonitorController myController;
    private final QueryController myQueryController;

    private final JTextField myDirectoryField;
    private final JTextField myExtensionField;
    private final JTextArea myLogArea;

    private final JButton myChooseButton;
    private final JButton myStartButton;
    private final JButton myStopButton;
    private final JButton myWriteDatabaseButton;

    private JMenuItem myStartMenuItem;
    private JMenuItem myStopMenuItem;
    private JMenuItem myWriteDatabaseMenuItem;

    public MainWindow(final MonitorController theController) {
        super(APPLICATION_TITLE);

        myController = java.util.Objects.requireNonNull(theController, "MonitorController cannot be null.");
        myQueryController = new QueryController(myController);
        myController.addListener(this);

        myDirectoryField = new JTextField();
        myExtensionField = new JTextField(DEFAULT_EXTENSIONS);
        myLogArea = new JTextArea();

        myChooseButton = new JButton("Choose Folder");
        myStartButton = new JButton("Start");
        myStopButton = new JButton("Stop");
        myWriteDatabaseButton = new JButton("Write to DB");

        initializeWindow();
        buildLayout();
        wireEvents();
        updateControls();
    }

    private void initializeWindow() {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent theEvent) {
                attemptExit();
            }
        });

        myLogArea.setEditable(false);
        myLogArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        myWriteDatabaseButton.setToolTipText("Write the current event list to the SQLite database.");
        setJMenuBar(buildMenuBar());
    }

    private void buildLayout() {
        final JPanel topPanel = new JPanel(new GridBagLayout());
        final GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 6, 6, 6);
        constraints.fill = GridBagConstraints.HORIZONTAL;

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.weightx = 0;
        topPanel.add(new JLabel("Directory:"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        topPanel.add(myDirectoryField, constraints);

        constraints.gridx = 2;
        constraints.weightx = 0;
        topPanel.add(myChooseButton, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;
        topPanel.add(new JLabel("Extensions (comma):"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        topPanel.add(myExtensionField, constraints);

        final JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actionPanel.add(myStartButton);
        actionPanel.add(myStopButton);
        actionPanel.add(myWriteDatabaseButton);

        constraints.gridx = 2;
        constraints.weightx = 0;
        topPanel.add(actionPanel, constraints);

        final JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(buildToolBar(), BorderLayout.PAGE_START);
        northPanel.add(topPanel, BorderLayout.CENTER);

        add(northPanel, BorderLayout.NORTH);
        add(new JScrollPane(myLogArea), BorderLayout.CENTER);
    }

    private JMenuBar buildMenuBar() {
        final JMenuBar menuBar = new JMenuBar();

        final JMenu fileMenu = new JMenu("File");
        final JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        exitMenuItem.addActionListener(theEvent -> attemptExit());
        fileMenu.add(exitMenuItem);

        final JMenu monitorMenu = new JMenu("Monitor");
        myStartMenuItem = new JMenuItem("Start Monitoring");
        myStartMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        myStartMenuItem.addActionListener(theEvent -> startFromUserInput());

        myStopMenuItem = new JMenuItem("Stop Monitoring");
        myStopMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
        myStopMenuItem.addActionListener(theEvent -> stopFromUserInput());

        myWriteDatabaseMenuItem = new JMenuItem("Write Current List to DB");
        myWriteDatabaseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_DOWN_MASK));
        myWriteDatabaseMenuItem.addActionListener(theEvent -> writeDatabaseFromUserInput());

        final JMenuItem openQueryWindowItem = new JMenuItem("Open Query Window");
        openQueryWindowItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        openQueryWindowItem.addActionListener(theEvent -> openQueryWindow());

        monitorMenu.add(myStartMenuItem);
        monitorMenu.add(myStopMenuItem);
        monitorMenu.addSeparator();
        monitorMenu.add(myWriteDatabaseMenuItem);
        monitorMenu.addSeparator();
        monitorMenu.add(openQueryWindowItem);

        final JMenu helpMenu = new JMenu("Help");
        final JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        aboutMenuItem.addActionListener(theEvent -> showAboutDialog());
        helpMenu.add(aboutMenuItem);

        menuBar.add(fileMenu);
        menuBar.add(monitorMenu);
        menuBar.add(helpMenu);
        return menuBar;
    }

    private JToolBar buildToolBar() {
        final JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        final JButton toolbarChooseButton = new JButton("Choose");
        toolbarChooseButton.setToolTipText("Choose a directory to monitor.");
        toolbarChooseButton.addActionListener(theEvent -> chooseFolder());

        final JButton toolbarStartButton = new JButton("Start");
        toolbarStartButton.setToolTipText("Start monitoring (Ctrl+S).");
        toolbarStartButton.addActionListener(theEvent -> startFromUserInput());

        final JButton toolbarStopButton = new JButton("Stop");
        toolbarStopButton.setToolTipText("Stop monitoring (Ctrl+T).");
        toolbarStopButton.addActionListener(theEvent -> stopFromUserInput());

        final JButton toolbarWriteButton = new JButton("Write DB");
        toolbarWriteButton.setToolTipText("Write the current event list to DB (Ctrl+D).");
        toolbarWriteButton.addActionListener(theEvent -> writeDatabaseFromUserInput());

        toolBar.add(toolbarChooseButton);
        toolBar.add(toolbarStartButton);
        toolBar.add(toolbarStopButton);
        toolBar.add(toolbarWriteButton);

        myChooseButton.putClientProperty(TOOLBAR_BUTTON_KEY, toolbarChooseButton);
        myStartButton.putClientProperty(TOOLBAR_BUTTON_KEY, toolbarStartButton);
        myStopButton.putClientProperty(TOOLBAR_BUTTON_KEY, toolbarStopButton);
        myWriteDatabaseButton.putClientProperty(TOOLBAR_BUTTON_KEY, toolbarWriteButton);

        return toolBar;
    }

    private void wireEvents() {
        myChooseButton.addActionListener(theEvent -> chooseFolder());
        myStartButton.addActionListener(theEvent -> startFromUserInput());
        myStopButton.addActionListener(theEvent -> stopFromUserInput());
        myWriteDatabaseButton.addActionListener(theEvent -> writeDatabaseFromUserInput());
    }

    private void chooseFolder() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        final int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            myDirectoryField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
        updateControls();
    }

    private void startFromUserInput() {
        final String directoryText = myDirectoryField.getText().trim();
        if (directoryText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please choose a folder first.");
            return;
        }

        final Set<String> extensions = parseExtensions(myExtensionField.getText());
        myController.startMonitoring(Path.of(directoryText), extensions);
        appendLog("=== Monitoring STARTED: " + directoryText + " | exts=" + extensions + " ===");
        updateControls();
    }

    private void stopFromUserInput() {
        myController.stopMonitoring();
        appendLog("=== Monitoring STOPPED ===");
        updateControls();
    }

    private void writeDatabaseFromUserInput() {
        try {
            final int count = myController.writeToDatabase();
            if (count > 0) {
                appendLog("=== Written " + count + " event(s) to database ===");
                JOptionPane.showMessageDialog(this, "Written " + count + " event(s) to database.");
            } else {
                JOptionPane.showMessageDialog(this,
                        "No events to write. Start monitoring and generate some events first.");
            }
        } catch (final RuntimeException theException) {
            appendLog("=== Write to DB failed: " + theException.getMessage() + " ===");
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to write to database: " + theException.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
        updateControls();
    }

    private void openQueryWindow() {
        final QueryWindow queryWindow = new QueryWindow(myQueryController);
        queryWindow.setVisible(true);
    }

    private void attemptExit() {
        if (myController.hasUnsavedEvents()) {
            final int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Write current contents to the database before exiting?",
                    "Unsaved events",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                try {
                    final int count = myController.writeToDatabase();
                    appendLog("=== Written " + count + " event(s) to database before exit ===");
                    doExit();
                } catch (final RuntimeException theException) {
                    JOptionPane.showMessageDialog(
                            this,
                            "Failed to write to database: " + theException.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                }
            } else if (choice == JOptionPane.NO_OPTION) {
                doExit();
            }
            return;
        }

        if (myController.isRunning()) {
            final int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Monitoring is currently running.\nStop monitoring and exit?",
                    "Exit",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
            myController.stopMonitoring();
        } else {
            final int choice = JOptionPane.showConfirmDialog(
                    this,
                    "Exit the application?",
                    "Exit",
                    JOptionPane.YES_NO_OPTION
            );
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        doExit();
    }

    private void doExit() {
        if (myController.isRunning()) {
            myController.stopMonitoring();
        }
        dispose();
        System.exit(0);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(
                this,
                APPLICATION_TITLE + "\n\n"
                        + "Monitor a folder and log file events.\n"
                        + "Features include menus, toolbar, SQLite writing, query support, and CSV export.\n\n"
                        + "Developers:\n"
                        + " - Jonathan Sung\n"
                        + " - Abdulrahman Elmi",
                "About",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void updateControls() {
        final boolean running = myController.isRunning();

        myChooseButton.setEnabled(!running);
        myDirectoryField.setEnabled(!running);
        myExtensionField.setEnabled(!running);

        myStartButton.setEnabled(!running && !myDirectoryField.getText().trim().isEmpty());
        myStopButton.setEnabled(running);
        myWriteDatabaseButton.setEnabled(myController.hasAnyEvents());

        if (myStartMenuItem != null) {
            myStartMenuItem.setEnabled(myStartButton.isEnabled());
        }
        if (myStopMenuItem != null) {
            myStopMenuItem.setEnabled(myStopButton.isEnabled());
        }
        if (myWriteDatabaseMenuItem != null) {
            myWriteDatabaseMenuItem.setEnabled(myWriteDatabaseButton.isEnabled());
        }

        syncToolbarEnabled(myChooseButton);
        syncToolbarEnabled(myStartButton);
        syncToolbarEnabled(myStopButton);
        syncToolbarEnabled(myWriteDatabaseButton);
    }

    private void syncToolbarEnabled(final JButton theSourceButton) {
        final Object toolbarButton = theSourceButton.getClientProperty(TOOLBAR_BUTTON_KEY);
        if (toolbarButton instanceof JButton button) {
            button.setEnabled(theSourceButton.isEnabled());
        }
    }

    private static Set<String> parseExtensions(final String theText) {
        final Set<String> extensions = new LinkedHashSet<>();
        if (theText == null || theText.isBlank()) {
            return extensions;
        }

        Arrays.stream(theText.split(","))
                .map(String::trim)
                .filter(text -> !text.isEmpty())
                .map(text -> text.startsWith(".") ? text.substring(1) : text)
                .map(String::toLowerCase)
                .forEach(extensions::add);

        return extensions;
    }

    @Override
    public void onFileEvent(final FileEvent theEvent) {
        SwingUtilities.invokeLater(() -> {
            appendLog(theEvent.toString());
            updateControls();
        });
    }

    private void appendLog(final String theLine) {
        myLogArea.append(theLine + System.lineSeparator());
        myLogArea.setCaretPosition(myLogArea.getDocument().getLength());
    }
}