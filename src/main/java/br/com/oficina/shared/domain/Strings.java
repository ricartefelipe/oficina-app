package br.com.oficina.shared.domain;

import java.text.Normalizer;

public final class Strings {

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
        return value.replaceAll("\\D", "");
    }

    /**
     * Mantem somente letras/digitos e normaliza para upper-case (A-Z/0-9).
     */
    public static String alnumUpper(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return normalized.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
    }
}
