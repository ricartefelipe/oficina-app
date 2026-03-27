package br.com.oficina.cadastros.cliente.domain;

/**
 * Validador de CPF/CNPJ com digitos verificadores.
 *
 * Observacao: recebe apenas digitos (normalizados).
 */
public final class CpfCnpjValidator {

    private CpfCnpjValidator() {
    }

    public static boolean isValid(String digits) {
        if (digits == null) return false;
        if (!(digits.length() == 11 || digits.length() == 14)) return false;
        if (allSameDigits(digits)) return false;

        if (digits.length() == 11) {
            return isValidCpf(digits);
        }
        return isValidCnpj(digits);
    }

    private static boolean allSameDigits(String digits) {
        char first = digits.charAt(0);
        for (int i = 1; i < digits.length(); i++) {
            if (digits.charAt(i) != first) return false;
        }
        return true;
    }

    private static boolean isValidCpf(String cpf) {
        // cpf: 11 digits
        int d1 = calcCpfDigit(cpf, 9);
        int d2 = calcCpfDigit(cpf, 10);
        return (cpf.charAt(9) - '0') == d1 && (cpf.charAt(10) - '0') == d2;
    }

    private static int calcCpfDigit(String cpf, int length) {
        int sum = 0;
        int weight = length + 1;
        for (int i = 0; i < length; i++) {
            int num = cpf.charAt(i) - '0';
            sum += num * weight;
            weight--;
        }
        int mod = sum % 11;
        return (mod < 2) ? 0 : 11 - mod;
    }

    private static boolean isValidCnpj(String cnpj) {
        int d1 = calcCnpjDigit(cnpj, 12);
        int d2 = calcCnpjDigit(cnpj, 13);
        return (cnpj.charAt(12) - '0') == d1 && (cnpj.charAt(13) - '0') == d2;
    }

    private static int calcCnpjDigit(String cnpj, int length) {
        int[] weights = (length == 12)
                ? new int[]{5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2}
                : new int[]{6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int sum = 0;
        for (int i = 0; i < length; i++) {
            int num = cnpj.charAt(i) - '0';
            sum += num * weights[i];
        }

        int mod = sum % 11;
        return (mod < 2) ? 0 : 11 - mod;
    }
}
