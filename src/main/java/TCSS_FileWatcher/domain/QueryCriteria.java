package TCSS_FileWatcher.domain;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Encapsulates optional filters used when monitoring files.
 */
public final class QueryCriteria {

    private final Set<String> myAllowedExtensions;

    public QueryCriteria(final Set<String> theAllowedExtensions) {
        myAllowedExtensions = new HashSet<>();
        if (theAllowedExtensions != null) {
            for (final String extension : theAllowedExtensions) {
                if (extension != null && !extension.isBlank()) {
                    myAllowedExtensions.add(normalize(extension));
                }
            }
        }
    }

    public Set<String> getAllowedExtensions() {
        return Collections.unmodifiableSet(myAllowedExtensions);
    }

    public boolean hasExtensionFilter() {
        return !myAllowedExtensions.isEmpty();
    }

    private static String normalize(final String theExtension) {
        String normalized = theExtension.trim().toLowerCase();
        if (normalized.startsWith(".")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}