package TCSS_FileWatcher.export;

import javax.swing.table.TableModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Writes query results to a CSV file.
 */
public final class CsvExportService {

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
            writer.println("Description," + escape(theQueryDescription == null ? "" : theQueryDescription));
            writer.println();

            writeHeader(theModel, writer);
            writeRows(theModel, writer);
            return csvFile;
        } catch (final IOException theException) {
            throw new IllegalStateException("Failed to export CSV: " + theException.getMessage(), theException);
        }
    }

    private File ensureCsvExtension(final File theFile) {
        if (theFile.getName().toLowerCase(Locale.ROOT).endsWith(".csv")) {
            return theFile;
        }
        return new File(theFile.getParentFile(), theFile.getName() + ".csv");
    }

    private void writeHeader(final TableModel theModel,
                             final PrintWriter theWriter) {
        for (int column = 0; column < theModel.getColumnCount(); column++) {
            theWriter.print(escape(theModel.getColumnName(column)));
            if (column < theModel.getColumnCount() - 1) {
                theWriter.print(',');
            }
        }
        theWriter.println();
    }

    private void writeRows(final TableModel theModel,
                           final PrintWriter theWriter) {
        for (int row = 0; row < theModel.getRowCount(); row++) {
            for (int column = 0; column < theModel.getColumnCount(); column++) {
                final Object value = theModel.getValueAt(row, column);
                theWriter.print(escape(value == null ? "" : value.toString()));
                if (column < theModel.getColumnCount() - 1) {
                    theWriter.print(',');
                }
            }
            theWriter.println();
        }
    }

    private String escape(final String theValue) {
        String escaped = theValue.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            escaped = '"' + escaped + '"';
        }
        return escaped;
    }
}