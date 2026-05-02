package br.com.oficina.shared.domain;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class Strings {

    private static final Pattern NON_DIGITS = Pattern.compile("\\D");
    private static final Pattern DIACRITIC_MARKS = Pattern.compile("\\p{M}");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^A-Za-z0-9]");

    private Strings() {
    }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.trim().isBlank()) {
            throw new ValidationException(fieldName + " nao pode ser vazio");
        }
        return value.trim();
    }

    /**
     * Remove tudo que nao for digito.
     */
    public static String onlyDigits(String value) {
        if (value == null) {
            return "";
        }
        return NON_DIGITS.matcher(value).replaceAll("");
    }

    /**
     * Mantem somente letras/digitos e normaliza para upper-case (A-Z/0-9).
     */
    public static String alnumUpper(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = DIACRITIC_MARKS.matcher(normalized).replaceAll("");
        return NON_ALPHANUMERIC.matcher(normalized).replaceAll("").toUpperCase();
    }
}
