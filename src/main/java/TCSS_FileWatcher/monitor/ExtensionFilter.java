package TCSS_FileWatcher.monitor;

import TCSS_FileWatcher.domain.QueryCriteria;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Utility class for extension-based file filtering.
 */
public final class ExtensionFilter {

    private ExtensionFilter() {
        // Utility class.
    }

    public static boolean matches(final Path thePath,
                                  final QueryCriteria theCriteria) {
        Objects.requireNonNull(thePath, "Path cannot be null.");

        if (theCriteria == null || !theCriteria.hasExtensionFilter()) {
            return true;
        }

        final Path fileName = thePath.getFileName();
        final String name = fileName == null ? thePath.toString() : fileName.toString();
        final int dotIndex = name.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == name.length() - 1) {
            return false;
        }

        final String extension = name.substring(dotIndex + 1).toLowerCase();
        return theCriteria.getAllowedExtensions().contains(extension);
    }
}