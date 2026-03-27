package br.com.oficina.ordemservico.domain;

import java.security.SecureRandom;

/**
 * Gerador de trackingCode para consulta do cliente.
 *
 * Requisito: expor um identificador nao sequencial para o cliente acompanhar a OS.
 */
public final class TrackingCodeGenerator {

    private static final char[] ALPHANUM = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final SecureRandom RNG = new SecureRandom();

    private TrackingCodeGenerator() {
    }

    public static String generate(int length) {
        if (length < 8 || length > 32) {
            throw new IllegalArgumentException("length deve estar entre 8 e 32");
        }
        char[] out = new char[length];
        for (int i = 0; i < length; i++) {
            out[i] = ALPHANUM[RNG.nextInt(ALPHANUM.length)];
        }
        return new String(out);
    }
}
