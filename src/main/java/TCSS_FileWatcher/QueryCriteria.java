package TCSS_FileWatcher;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class QueryCriteria {
    private final Set<String> allowedExtensions; // e.g. "txt", "java"

    public QueryCriteria(Set<String> allowedExtensions) {
        this.allowedExtensions = new HashSet<>();
        if (allowedExtensions != null) {
            for (String ext : allowedExtensions) {
                if (ext != null && !ext.isBlank()) {
                    this.allowedExtensions.add(normalize(ext));
                }
            }
        }
    }

    public Set<String> getAllowedExtensions() {
        return Collections.unmodifiableSet(allowedExtensions);
    }

    public boolean hasExtensionFilter() {
        return !allowedExtensions.isEmpty();
    }

    private static String normalize(String ext) {
        ext = ext.trim().toLowerCase();
        if (ext.startsWith(".")) ext = ext.substring(1);
        return ext;
    }
}
