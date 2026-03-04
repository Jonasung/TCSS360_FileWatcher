package TCSS_FileWatcher.ui.query;

import TCSS_FileWatcher.app.EventRecord;
import TCSS_FileWatcher.app.EventRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Separate window to query the database by extension, view results in a table,
 * and clear the database. "Back to monitor" returns to the main window.
 */
public class QueryWindow extends JFrame {

    private static final String[] COLUMNS = { "File Name", "Extension", "Path", "Event Type", "Date/Time" };
    private static final String[] DEFAULT_EXTENSIONS = { "txt", "java", "pdf", "docx", "xml", "json" };

    private final EventRepository repository;
    private final JFrame mainWindow;
    private final JComboBox<String> extensionCombo;
    private final DefaultTableModel tableModel;
    private final JTable table;

    public QueryWindow(EventRepository repository, JFrame mainWindow) {
        super("Query Database");
        this.repository = repository;
        this.mainWindow = mainWindow;

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(mainWindow);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                backToMonitor();
            }
        });

        extensionCombo = new JComboBox<>(DEFAULT_EXTENSIONS);
        extensionCombo.setEditable(true);
        extensionCombo.setSelectedItem("");

        JButton runBtn = new JButton("Run query");
        runBtn.addActionListener(e -> runQuery());

        JButton backBtn = new JButton("Back to monitor");
        backBtn.addActionListener(e -> backToMonitor());

        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Extension:"));
        top.add(extensionCombo);
        top.add(runBtn);
        top.add(backBtn);

        setJMenuBar(buildMenuBar());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu db = new JMenu("Database");
        JMenuItem clearItem = new JMenuItem("Clear contents of the database");
        clearItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        clearItem.addActionListener(e -> clearDatabase());
        db.add(clearItem);
        bar.add(db);
        return bar;
    }

    private void runQuery() {
        try {
            String ext = extensionCombo.getEditor().getItem().toString().trim();
            List<EventRecord> records = ext.isEmpty() ? repository.queryAll() : repository.queryByExtension(ext);
            tableModel.setRowCount(0);
            for (EventRecord r : records) {
                tableModel.addRow(new Object[]{
                    r.getFileName(),
                    r.getExtension(),
                    r.getAbsolutePath(),
                    r.getEventType(),
                    r.getEventDateTime()
                });
            }
            if (records.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No records found.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Query failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearDatabase() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Clear all records from the database?",
            "Clear database",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        try {
            repository.clearAll();
            tableModel.setRowCount(0);
            JOptionPane.showMessageDialog(this, "Database cleared.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Clear failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void backToMonitor() {
        setVisible(false);
        mainWindow.setVisible(true);
    }

    /** Call when opening this window (e.g. from main menu) to show it and hide main. */
    public void open() {
        mainWindow.setVisible(false);
        setVisible(true);
    }
}
