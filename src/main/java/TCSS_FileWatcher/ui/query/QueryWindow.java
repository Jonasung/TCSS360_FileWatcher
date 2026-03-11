package TCSS_FileWatcher.ui.query;

import TCSS_FileWatcher.app.QueryController;
import TCSS_FileWatcher.domain.EventType;
import TCSS_FileWatcher.domain.FileEvent;
import TCSS_FileWatcher.export.CsvExportService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class QueryWindow extends JFrame {

    private final QueryController myQueryController;

    // inputs
    private final JTextField myExtField = new JTextField("txt");
    private final JTextField myFromField = new JTextField("2026-01-01 00:00");
    private final JTextField myToField = new JTextField("2026-12-31 23:59");
    private final JComboBox<EventType> myTypeBox = new JComboBox<>(EventType.values());
    private final JTextField myPathField = new JTextField();

    private final JButton myRunBtn = new JButton("Run Query");
    private final JButton myClearBtn = new JButton("Clear Results");
    private final JButton myExportBtn = new JButton("Export to CSV");

    // selection
    private final JRadioButton myRbExt = new JRadioButton("By Extension", true);
    private final JRadioButton myRbDate = new JRadioButton("By Date Range");
    private final JRadioButton myRbType = new JRadioButton("By Activity Type");
    private final JRadioButton myRbPath = new JRadioButton("By Path/Directory");

    // table
    private final DefaultTableModel myModel = new DefaultTableModel(
            new Object[]{"File Name", "Extension", "Path", "Activity", "Date/Time"}, 0
    ) {
        @Override
        public boolean isCellEditable(final int theRow, final int theColumn) {
            return false;
        }
    };

    private final JTable myTable = new JTable(myModel);

    private static final DateTimeFormatter INPUT_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // store last query info for CSV header
    private String myLastQueryDescription = "No query has been run yet.";

    public QueryWindow(final QueryController theQueryController) {
        super("FileWatcher - Query Window");

        if (theQueryController == null) {
            throw new IllegalArgumentException("QueryController cannot be null.");
        }

        myQueryController = theQueryController;

        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        buildUI();
        wireEvents();
        updateFormEnabled();
    }

    private void buildUI() {
        final ButtonGroup group = new ButtonGroup();
        group.add(myRbExt);
        group.add(myRbDate);
        group.add(myRbType);
        group.add(myRbPath);

        final JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        left.add(new JLabel("Query Type"));
        left.add(myRbExt);
        left.add(myRbDate);
        left.add(myRbType);
        left.add(myRbPath);

        left.add(Box.createVerticalStrut(12));
        left.add(new JLabel("Extension (e.g. txt, java)"));
        left.add(myExtField);

        left.add(Box.createVerticalStrut(12));
        left.add(new JLabel("From (yyyy-MM-dd HH:mm)"));
        left.add(myFromField);
        left.add(new JLabel("To (yyyy-MM-dd HH:mm)"));
        left.add(myToField);

        left.add(Box.createVerticalStrut(12));
        left.add(new JLabel("Activity Type"));
        left.add(myTypeBox);

        left.add(Box.createVerticalStrut(12));
        left.add(new JLabel("Path/Directory"));
        left.add(myPathField);

        left.add(Box.createVerticalStrut(12));
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(myRunBtn);
        buttons.add(myClearBtn);
        buttons.add(myExportBtn);
        left.add(buttons);

        final JScrollPane right = new JScrollPane(myTable);
        myTable.setFillsViewportHeight(true);

        final JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.30);

        add(split, BorderLayout.CENTER);
    }

    private void wireEvents() {
        myRbExt.addActionListener(e -> updateFormEnabled());
        myRbDate.addActionListener(e -> updateFormEnabled());
        myRbType.addActionListener(e -> updateFormEnabled());
        myRbPath.addActionListener(e -> updateFormEnabled());

        myRunBtn.addActionListener(e -> runQuery());
        myClearBtn.addActionListener(e -> clearResults());
        myExportBtn.addActionListener(e -> exportResults());
    }

    private void updateFormEnabled() {
        final boolean byExt = myRbExt.isSelected();
        final boolean byDate = myRbDate.isSelected();
        final boolean byType = myRbType.isSelected();
        final boolean byPath = myRbPath.isSelected();

        myExtField.setEnabled(byExt);
        myFromField.setEnabled(byDate);
        myToField.setEnabled(byDate);
        myTypeBox.setEnabled(byType);
        myPathField.setEnabled(byPath);
    }

    private void runQuery() {
        try {
            final List<FileEvent> results;

            if (myRbExt.isSelected()) {
                myLastQueryDescription = "Query Type: Extension | Value: " + myExtField.getText().trim();
                results = myQueryController.queryByExtension(myExtField.getText());
            } else if (myRbDate.isSelected()) {
                final LocalDateTime from = parseDate(myFromField.getText());
                final LocalDateTime to = parseDate(myToField.getText());
                myLastQueryDescription = "Query Type: Date Range | From: " + from + " | To: " + to;
                results = myQueryController.queryByDateRange(from, to);
            } else if (myRbType.isSelected()) {
                final EventType type = (EventType) myTypeBox.getSelectedItem();
                myLastQueryDescription = "Query Type: Activity | Value: " + type;
                results = myQueryController.queryByEventType(type);
            } else {
                myLastQueryDescription = "Query Type: Path/Directory | Value: " + myPathField.getText().trim();
                results = myQueryController.queryByPathPrefix(myPathField.getText());
            }

            loadResults(results);

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "No results found.",
                        "Query",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Query failed due to an unexpected error.\nPlease try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            ex.printStackTrace();
        }
    }

    private void exportResults() {
        if (myModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "There are no query results to export.",
                    "Export to CSV",
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Query Results as CSV");
        chooser.setSelectedFile(new File("query_results.csv"));

        final int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            final CsvExportService exportService = new CsvExportService();
            final File exportedFile = exportService.exportTableToCsv(
                    chooser.getSelectedFile(),
                    myLastQueryDescription,
                    myModel
            );

            JOptionPane.showMessageDialog(
                    this,
                    "CSV exported successfully:\n" + exportedFile.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to export CSV:\n" + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private LocalDateTime parseDate(final String theText) {
        try {
            return LocalDateTime.parse(theText.trim(), INPUT_FMT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid date format. Use: yyyy-MM-dd HH:mm");
        }
    }

    private void loadResults(final List<FileEvent> theResults) {
        clearResults();

        for (final FileEvent event : theResults) {
            final String fileName = event.getPath().getFileName().toString();
            final String ext = getExtension(fileName);
            final String path = event.getPath().toString();
            final String activity = event.getType().name();
            final String dateTime = event.getTimestamp().toString();

            myModel.addRow(new Object[]{fileName, ext, path, activity, dateTime});
        }
    }

    private void clearResults() {
        myModel.setRowCount(0);
    }

    private static String getExtension(final String theFileName) {
        final int dot = theFileName.lastIndexOf('.');
        if (dot < 0 || dot == theFileName.length() - 1) {
            return "";
        }
        return theFileName.substring(dot + 1).toLowerCase();
    }
}