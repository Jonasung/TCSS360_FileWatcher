package TCSS_FileWatcher.ui.query;

import TCSS_FileWatcher.app.QueryController;
import TCSS_FileWatcher.domain.EventType;
import TCSS_FileWatcher.domain.FileEvent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class QueryWindow extends JFrame {

    private final QueryController queryController;

    // inputs
    private final JTextField extField = new JTextField("txt");
    private final JTextField fromField = new JTextField("2026-01-01 00:00");
    private final JTextField toField = new JTextField("2026-12-31 23:59");
    private final JComboBox<EventType> typeBox = new JComboBox<>(EventType.values());
    private final JTextField pathField = new JTextField();

    private final JButton runBtn = new JButton("Run Query");
    private final JButton clearBtn = new JButton("Clear Results");

    // selection
    private final JRadioButton rbExt = new JRadioButton("By Extension", true);
    private final JRadioButton rbDate = new JRadioButton("By Date Range");
    private final JRadioButton rbType = new JRadioButton("By Activity Type");
    private final JRadioButton rbPath = new JRadioButton("By Path/Directory");

    // table
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"File Name", "Extension", "Path", "Activity", "Date/Time"}, 0
    ) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };

    private final JTable table = new JTable(model);

    private static final DateTimeFormatter INPUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public QueryWindow(QueryController queryController) {
        super("FileWatcher - Query Window");
        this.queryController = queryController;

        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
        wireEvents();
        updateFormEnabled();
    }

    private void buildUI() {
        ButtonGroup group = new ButtonGroup();
        group.add(rbExt);
        group.add(rbDate);
        group.add(rbType);
        group.add(rbPath);

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        left.add(new JLabel("Query Type"));
        left.add(rbExt);
        left.add(rbDate);
        left.add(rbType);
        left.add(rbPath);

        left.add(Box.createVerticalStrut(12));
        left.add(new JLabel("Extension (e.g. txt, java)"));
        left.add(extField);

        left.add(Box.createVerticalStrut(12));
        left.add(new JLabel("From (yyyy-MM-dd HH:mm)"));
        left.add(fromField);
        left.add(new JLabel("To (yyyy-MM-dd HH:mm)"));
        left.add(toField);

        left.add(Box.createVerticalStrut(12));
        left.add(new JLabel("Activity Type"));
        left.add(typeBox);

        left.add(Box.createVerticalStrut(12));
        left.add(new JLabel("Path/Directory"));
        left.add(pathField);

        left.add(Box.createVerticalStrut(12));
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btns.add(runBtn);
        btns.add(clearBtn);
        left.add(btns);

        JScrollPane right = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.30);

        add(split, BorderLayout.CENTER);
    }

    private void wireEvents() {
        rbExt.addActionListener(e -> updateFormEnabled());
        rbDate.addActionListener(e -> updateFormEnabled());
        rbType.addActionListener(e -> updateFormEnabled());
        rbPath.addActionListener(e -> updateFormEnabled());

        runBtn.addActionListener(e -> runQuery());
        clearBtn.addActionListener(e -> clearResults());
    }

    private void updateFormEnabled() {
        boolean byExt = rbExt.isSelected();
        boolean byDate = rbDate.isSelected();
        boolean byType = rbType.isSelected();
        boolean byPath = rbPath.isSelected();

        extField.setEnabled(byExt);
        fromField.setEnabled(byDate);
        toField.setEnabled(byDate);
        typeBox.setEnabled(byType);
        pathField.setEnabled(byPath);
    }

    private void runQuery() {
        try {
            List<FileEvent> results;

            if (rbExt.isSelected()) {
                results = queryController.queryByExtension(extField.getText());
            } else if (rbDate.isSelected()) {
                LocalDateTime from = parseDate(fromField.getText());
                LocalDateTime to = parseDate(toField.getText());
                results = queryController.queryByDateRange(from, to);
            } else if (rbType.isSelected()) {
                EventType type = (EventType) typeBox.getSelectedItem();
                results = queryController.queryByEventType(type);
            } else { // rbPath
                results = queryController.queryByPathPrefix(pathField.getText());
            }

            loadResults(results);

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No results found.", "Query", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            // "No crashes" requirement
            JOptionPane.showMessageDialog(this,
                    "Query failed due to an unexpected error.\nPlease try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private LocalDateTime parseDate(String text) {
        try {
            return LocalDateTime.parse(text.trim(), INPUT_FMT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Use: yyyy-MM-dd HH:mm");
        }
    }

    private void loadResults(List<FileEvent> results) {
        clearResults();
        for (FileEvent e : results) {
            String fileName = e.getPath().getFileName().toString();
            String ext = getExtension(fileName);
            String path = e.getPath().toString();
            String activity = e.getType().name();
            String dateTime = e.getTimestamp().toString();
            model.addRow(new Object[]{fileName, ext, path, activity, dateTime});
        }
    }

    private void clearResults() {
        model.setRowCount(0);
    }

    private static String getExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) return "";
        return fileName.substring(dot + 1).toLowerCase();
    }
}