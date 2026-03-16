package TCSS_FileWatcher.ui.query;

import TCSS_FileWatcher.app.QueryController;
import TCSS_FileWatcher.domain.EventType;
import TCSS_FileWatcher.domain.FileEvent;
import TCSS_FileWatcher.export.CsvExportService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;

/**
 * UI for running queries over captured file events.
 */
public final class QueryWindow extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String DEFAULT_FROM_DATE = "2026-01-01 00:00";
    private static final String DEFAULT_TO_DATE = "2026-12-31 23:59";

    private final QueryController myQueryController;

    private final JTextField myExtensionField;
    private final JTextField myFromField;
    private final JTextField myToField;
    private final JComboBox<EventType> myTypeBox;
    private final JTextField myPathField;

    private final JButton myRunButton;
    private final JButton myClearButton;
    private final JButton myExportButton;

    private final JRadioButton myByExtensionButton;
    private final JRadioButton myByDateButton;
    private final JRadioButton myByTypeButton;
    private final JRadioButton myByPathButton;

    private final DefaultTableModel myTableModel;
    private final JTable myResultsTable;

    private String myLastQueryDescription;

    public QueryWindow(final QueryController theQueryController) {
        super("FileWatcher - Query Window");

        myQueryController = Objects.requireNonNull(theQueryController, "QueryController cannot be null.");
        myExtensionField = new JTextField("txt");
        myFromField = new JTextField(DEFAULT_FROM_DATE);
        myToField = new JTextField(DEFAULT_TO_DATE);
        myTypeBox = new JComboBox<>(EventType.values());
        myPathField = new JTextField();

        myRunButton = new JButton("Run Query");
        myClearButton = new JButton("Clear Results");
        myExportButton = new JButton("Export to CSV");

        myByExtensionButton = new JRadioButton("By Extension", true);
        myByDateButton = new JRadioButton("By Date Range");
        myByTypeButton = new JRadioButton("By Activity Type");
        myByPathButton = new JRadioButton("By Path/Directory");

        myTableModel = new DefaultTableModel(
                new Object[]{"File Name", "Extension", "Path", "Activity", "Date/Time"},
                0) {
            @Override
            public boolean isCellEditable(final int theRow, final int theColumn) {
                return false;
            }
        };
        myResultsTable = new JTable(myTableModel);
        myLastQueryDescription = "No query has been run yet.";

        initializeWindow();
        buildUserInterface();
        wireEvents();
        updateFormEnabled();
    }

    private void initializeWindow() {
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    private void buildUserInterface() {
        final ButtonGroup group = new ButtonGroup();
        group.add(myByExtensionButton);
        group.add(myByDateButton);
        group.add(myByTypeButton);
        group.add(myByPathButton);

        final JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        leftPanel.add(new JLabel("Query Type"));
        leftPanel.add(myByExtensionButton);
        leftPanel.add(myByDateButton);
        leftPanel.add(myByTypeButton);
        leftPanel.add(myByPathButton);

        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(new JLabel("Extension (e.g. txt, java)"));
        leftPanel.add(myExtensionField);

        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(new JLabel("From (yyyy-MM-dd HH:mm)"));
        leftPanel.add(myFromField);
        leftPanel.add(new JLabel("To (yyyy-MM-dd HH:mm)"));
        leftPanel.add(myToField);

        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(new JLabel("Activity Type"));
        leftPanel.add(myTypeBox);

        leftPanel.add(Box.createVerticalStrut(12));
        leftPanel.add(new JLabel("Path/Directory"));
        leftPanel.add(myPathField);

        leftPanel.add(Box.createVerticalStrut(12));
        final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(myRunButton);
        buttonPanel.add(myClearButton);
        buttonPanel.add(myExportButton);
        leftPanel.add(buttonPanel);

        myResultsTable.setFillsViewportHeight(true);
        final JScrollPane rightPanel = new JScrollPane(myResultsTable);
        final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setResizeWeight(0.30);

        add(splitPane, BorderLayout.CENTER);
    }

    private void wireEvents() {
        myByExtensionButton.addActionListener(theEvent -> updateFormEnabled());
        myByDateButton.addActionListener(theEvent -> updateFormEnabled());
        myByTypeButton.addActionListener(theEvent -> updateFormEnabled());
        myByPathButton.addActionListener(theEvent -> updateFormEnabled());

        myRunButton.addActionListener(theEvent -> runQuery());
        myClearButton.addActionListener(theEvent -> clearResults());
        myExportButton.addActionListener(theEvent -> exportResults());
    }

    private void updateFormEnabled() {
        myExtensionField.setEnabled(myByExtensionButton.isSelected());
        myFromField.setEnabled(myByDateButton.isSelected());
        myToField.setEnabled(myByDateButton.isSelected());
        myTypeBox.setEnabled(myByTypeButton.isSelected());
        myPathField.setEnabled(myByPathButton.isSelected());
    }

    private void runQuery() {
        try {
            final List<FileEvent> results = determineQueryResults();
            loadResults(results);

            if (results.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "No results found.",
                        "Query",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (final IllegalArgumentException theException) {
            JOptionPane.showMessageDialog(
                    this,
                    theException.getMessage(),
                    "Invalid Input",
                    JOptionPane.WARNING_MESSAGE
            );
        } catch (final RuntimeException theException) {
            JOptionPane.showMessageDialog(
                    this,
                    "Query failed due to an unexpected error.\nPlease try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
            theException.printStackTrace();
        }
    }

    private List<FileEvent> determineQueryResults() {
        if (myByExtensionButton.isSelected()) {
            myLastQueryDescription = "Query Type: Extension | Value: " + myExtensionField.getText().trim();
            return myQueryController.queryByExtension(myExtensionField.getText());
        }
        if (myByDateButton.isSelected()) {
            final LocalDateTime from = parseDate(myFromField.getText());
            final LocalDateTime to = parseDate(myToField.getText());
            myLastQueryDescription = "Query Type: Date Range | From: " + from + " | To: " + to;
            return myQueryController.queryByDateRange(from, to);
        }
        if (myByTypeButton.isSelected()) {
            final EventType selectedType = (EventType) myTypeBox.getSelectedItem();
            myLastQueryDescription = "Query Type: Activity | Value: " + selectedType;
            return myQueryController.queryByEventType(selectedType);
        }

        myLastQueryDescription = "Query Type: Path/Directory | Value: " + myPathField.getText().trim();
        return myQueryController.queryByPathPrefix(myPathField.getText());
    }

    private void exportResults() {
        if (myTableModel.getRowCount() == 0) {
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

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        try {
            final CsvExportService exportService = new CsvExportService();
            final File exportedFile = exportService.exportTableToCsv(
                    chooser.getSelectedFile(),
                    myLastQueryDescription,
                    myTableModel
            );

            JOptionPane.showMessageDialog(
                    this,
                    "CSV exported successfully:\n" + exportedFile.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (final RuntimeException theException) {
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to export CSV:\n" + theException.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private LocalDateTime parseDate(final String theText) {
        try {
            return LocalDateTime.parse(theText.trim(), INPUT_FORMAT);
        } catch (final DateTimeParseException theException) {
            throw new IllegalArgumentException("Invalid date format. Use: yyyy-MM-dd HH:mm");
        }
    }

    private void loadResults(final List<FileEvent> theResults) {
        clearResults();

        for (final FileEvent event : theResults) {
            final String fileName = event.getPath().getFileName() == null
                    ? event.getPath().toString()
                    : event.getPath().getFileName().toString();
            final String extension = getExtension(fileName);
            final String path = event.getPath().toString();
            final String activity = event.getType().name();
            final String dateTime = event.getTimestamp().toString();

            myTableModel.addRow(new Object[]{fileName, extension, path, activity, dateTime});
        }
    }

    private void clearResults() {
        myTableModel.setRowCount(0);
    }

    private static String getExtension(final String theFileName) {
        final int dotIndex = theFileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == theFileName.length() - 1) {
            return "";
        }
        return theFileName.substring(dotIndex + 1).toLowerCase();
    }
}