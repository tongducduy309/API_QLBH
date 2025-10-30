package com.gener.qlbh.utils;
import java.text.Normalizer;
import java.util.Locale;

public class StringUtil {
    public static String toSlug(String input) {
        if (input == null) return null;
        // Bỏ dấu tiếng Việt
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{M}", "");
        // Thay space & ký tự đặc biệt
        String slug = withoutAccents
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", "")
                .trim()
                .replaceAll("\\s+", "-");
        return slug;
    }

    public static String normalize(String input) {
        if (input == null) return null;
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }
}

