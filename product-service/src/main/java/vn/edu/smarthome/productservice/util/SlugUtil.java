package vn.edu.smarthome.productservice.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/** Tạo slug giống Str::slug của Laravel: "Nồi cơm điện 1.8L" -> "noi-com-dien-1-8l". */
public final class SlugUtil {

    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

    private SlugUtil() {
    }

    public static String slugify(String input) {
        if (input == null) {
            return "";
        }
        String s = input.trim().toLowerCase(Locale.ROOT).replace('đ', 'd');
        s = DIACRITICS.matcher(Normalizer.normalize(s, Normalizer.Form.NFD)).replaceAll("");
        s = NON_ALNUM.matcher(s).replaceAll("-");
        s = s.replaceAll("^-+|-+$", "");
        return s.isEmpty() ? "item" : s;
    }
}
