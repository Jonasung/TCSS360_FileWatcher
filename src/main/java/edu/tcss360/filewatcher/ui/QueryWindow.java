package edu.tcss360.filewatcher.ui;

import edu.tcss360.filewatcher.domain.FileEvent;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Query window: shows query results in a table with columns (file name, extension, path, event type, date/time).
 */
public class QueryWindow extends JFrame {

    private static final String[] COLUMNS = { "File Name", "Extension", "Absolute Path", "Event Type", "Date/Time" };
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private DefaultTableModel tableModel;
    private JTable table;

    public QueryWindow() {
        setTitle("Query Results");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setSize(700, 400);
    }

    /**
     * Builds the window layout: results table and Close button.
     */
    public void render() {
        setLayout(new BorderLayout());
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(table);
        add(scroll, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.addActionListener(e -> dispose());
        JPanel south = new JPanel();
        south.add(closeBtn);
        add(south, BorderLayout.SOUTH);
    }

    /**
     * Displays the given events in the results table (file name, extension, path, event type, date/time).
     */
    public void displayResults(List<FileEvent> results) {
        if (results == null) {
            results = Collections.emptyList();
        }
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        for (FileEvent e : results) {
            tableModel.addRow(new Object[]{
                e.getFileName(),
                e.getExtension(),
                e.getAbsolutePath(),
                e.getEventType().getDisplayLabel(),
                TIME_FMT.format(e.getTimestamp())
            });
        }
    }
}
