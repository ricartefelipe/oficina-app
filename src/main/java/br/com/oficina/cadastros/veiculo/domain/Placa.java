package br.com.oficina.cadastros.veiculo.domain;

import br.com.oficina.shared.domain.Strings;
import br.com.oficina.shared.domain.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object Placa.
 *
 * Aceita formatos brasileiros:
 * - Antigo: ABC-1234 (aceita com ou sem hifen)
 * - Mercosul: ABC1D23
 */
@Embeddable
public class Placa implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Pattern PLACA_ANTIGA = Pattern.compile("^[A-Z]{3}-?\\d{4}$");
    private static final Pattern PLACA_MERCOSUL = Pattern.compile("^[A-Z]{3}\\d[A-Z]\\d{2}$");

    @Column(name = "placa", nullable = false, length = 7)
    private String value;

    protected Placa() {
        // JPA
    }

    private Placa(String normalized) {
        this.value = normalized;
    }

    public static Placa of(String raw) {
        String normalized = Strings.alnumUpper(Strings.requireNonBlank(raw, "placa"));

        if (normalized.length() != 7) {
            throw new ValidationException("Placa invalida: tamanho deve ser 7 caracteres");
        }

        if (!PLACA_ANTIGA.matcher(normalized).matches() && !PLACA_MERCOSUL.matcher(normalized).matches()) {
            throw new ValidationException("Placa invalida: formato nao reconhecido");
        }

        return new Placa(normalized);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Placa placa)) return false;
        return Objects.equals(value, placa.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
