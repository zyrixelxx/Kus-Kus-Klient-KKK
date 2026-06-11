package me.kuskus.addon;

import java.util.Locale;

public record AddonMetadata(String id, String name, String version, String author, String description) {
    public AddonMetadata {
        id = sanitizeId(id, name);
        name = name == null || name.isBlank() ? "Unnamed Addon" : name.trim();
        version = version == null || version.isBlank() ? "unknown" : version.trim();
        author = author == null ? "" : author.trim();
        description = description == null ? "" : description.trim();
    }

    public static AddonMetadata basic(String name) {
        return new AddonMetadata(name, name, "unknown", "", "");
    }

    private static String sanitizeId(String rawId, String fallbackName) {
        String source = rawId == null || rawId.isBlank() ? fallbackName : rawId;
        String normalized = source == null ? "addon" : source.trim().toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9._-]+", "-")
            .replaceAll("^-+|-+$", "");
        return normalized.isBlank() ? "addon" : normalized;
    }
}
