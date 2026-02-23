package TCSS_FileWatcher.monitor;

import java.nio.file.Path;

import TCSS_FileWatcher.domain.QueryCriteria;

public final class ExtensionFilter {
    private ExtensionFilter() {}

    public static boolean matches(Path path, QueryCriteria criteria) {
        if (criteria == null || !criteria.hasExtensionFilter()) return true;

        String name = path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        if (dot < 0 || dot == name.length() - 1) return false;

        String ext = name.substring(dot + 1).toLowerCase();
        return criteria.getAllowedExtensions().contains(ext);
    }
}
