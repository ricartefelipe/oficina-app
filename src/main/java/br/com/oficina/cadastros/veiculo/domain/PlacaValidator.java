package br.com.oficina.cadastros.veiculo.domain;

import java.util.regex.Pattern;

public final class PlacaValidator {

    private PlacaValidator() {
    }

    // Antigo: ABC1234
    private static final Pattern PLACA_ANTIGA = Pattern.compile("^[A-Z]{3}[0-9]{4}$");

    // Mercosul: ABC1D23
    private static final Pattern PLACA_MERCOSUL = Pattern.compile("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");

    public static boolean isValid(String normalizedUpperNoDash) {
        if (normalizedUpperNoDash == null) return false;
        return PLACA_ANTIGA.matcher(normalizedUpperNoDash).matches()
                || PLACA_MERCOSUL.matcher(normalizedUpperNoDash).matches();
    }
}
