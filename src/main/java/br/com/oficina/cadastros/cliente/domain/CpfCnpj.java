package br.com.oficina.cadastros.cliente.domain;

import br.com.oficina.shared.domain.Strings;
import br.com.oficina.shared.domain.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object CPF/CNPJ com validacao real (digitos verificadores).
 */
@Embeddable
public class CpfCnpj implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "cpf_cnpj", nullable = false, length = 14)
    private String value;

    protected CpfCnpj() {
        // JPA
    }

    private CpfCnpj(String normalizedDigits) {
        this.value = normalizedDigits;
    }

    public static CpfCnpj of(String raw) {
        String digits = Strings.onlyDigits(Strings.requireNonBlank(raw, "cpfCnpj"));
        if (digits.length() == 11) {
            if (!isValidCpf(digits)) {
                throw new ValidationException("CPF invalido");
            }
            return new CpfCnpj(digits);
        }
        if (digits.length() == 14) {
            if (!isValidCnpj(digits)) {
                throw new ValidationException("CNPJ invalido");
            }
            return new CpfCnpj(digits);
        }
        throw new ValidationException("cpfCnpj deve ter 11 (CPF) ou 14 (CNPJ) digitos");
    }

    public String value() {
        return value;
    }

    public boolean isCpf() {
        return value != null && value.length() == 11;
    }

    public boolean isCnpj() {
        return value != null && value.length() == 14;
    }

    private static boolean isAllSameDigits(String digits) {
        char first = digits.charAt(0);
        for (int i = 1; i < digits.length(); i++) {
            if (digits.charAt(i) != first) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return false;
        }
        if (isAllSameDigits(cpf)) {
            return false;
        }

        int sum1 = 0;
        for (int i = 0; i < 9; i++) {
            sum1 += (cpf.charAt(i) - '0') * (10 - i);
        }
        int d1 = 11 - (sum1 % 11);
        if (d1 >= 10) {
            d1 = 0;
        }

        int sum2 = 0;
        for (int i = 0; i < 10; i++) {
            sum2 += (cpf.charAt(i) - '0') * (11 - i);
        }
        int d2 = 11 - (sum2 % 11);
        if (d2 >= 10) {
            d2 = 0;
        }

        return d1 == (cpf.charAt(9) - '0') && d2 == (cpf.charAt(10) - '0');
    }

    private static boolean isValidCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) {
            return false;
        }
        if (isAllSameDigits(cnpj)) {
            return false;
        }

        int[] w1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] w2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int sum1 = 0;
        for (int i = 0; i < 12; i++) {
            sum1 += (cnpj.charAt(i) - '0') * w1[i];
        }
        int r1 = sum1 % 11;
        int d1 = (r1 < 2) ? 0 : (11 - r1);

        int sum2 = 0;
        for (int i = 0; i < 13; i++) {
            sum2 += (cnpj.charAt(i) - '0') * w2[i];
        }
        int r2 = sum2 % 11;
        int d2 = (r2 < 2) ? 0 : (11 - r2);

        return d1 == (cnpj.charAt(12) - '0') && d2 == (cnpj.charAt(13) - '0');
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CpfCnpj that)) return false;
        return Objects.equals(value, that.value);
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
