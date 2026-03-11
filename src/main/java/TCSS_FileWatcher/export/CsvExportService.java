package TCSS_FileWatcher.export;

import javax.swing.table.TableModel;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class CsvExportService {

    public File exportTableToCsv(final File theFile,
                                 final String theQueryDescription,
                                 final TableModel theModel) {
        if (theFile == null) {
            throw new IllegalArgumentException("File cannot be null.");
        }
        if (theModel == null) {
            throw new IllegalArgumentException("Table model cannot be null.");
        }

        final File csvFile = ensureCsvExtension(theFile);

        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8))) {

            writer.println("Query Information");
            writer.println("Description," + escape(theQueryDescription));
            writer.println();

            for (int col = 0; col < theModel.getColumnCount(); col++) {
                writer.print(escape(theModel.getColumnName(col)));
                if (col < theModel.getColumnCount() - 1) {
                    writer.print(",");
                }
            }
            writer.println();

            for (int row = 0; row < theModel.getRowCount(); row++) {
                for (int col = 0; col < theModel.getColumnCount(); col++) {
                    Object value = theModel.getValueAt(row, col);
                    writer.print(escape(value == null ? "" : value.toString()));
                    if (col < theModel.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }

            return csvFile;

        } catch (IOException e) {
            throw new RuntimeException("Failed to export CSV: " + e.getMessage(), e);
        }
    }

    private File ensureCsvExtension(final File theFile) {
        if (theFile.getName().toLowerCase().endsWith(".csv")) {
            return theFile;
        }
        return new File(theFile.getParentFile(), theFile.getName() + ".csv");
    }

    private String escape(final String theValue) {
        String value = theValue.replace("\"", "\"\"");
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            value = "\"" + value + "\"";
        }
        return value;
    }
}