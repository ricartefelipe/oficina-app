package br.com.oficina.ordemservico.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TrackingCodeGeneratorTest {

    @Test
    void deveGerarTrackingCodeNoTamanhoInformado() {
        String code = TrackingCodeGenerator.generate(12);
        assertNotNull(code);
        assertEquals(12, code.length());
    }

    @Test
    void deveGerarApenasCaracteresDoAlfabetoPermitido() {
        String code = TrackingCodeGenerator.generate(16);
        assertTrue(code.matches("[A-Z2-9]{16}"));
    }

    @Test
    void deveValidarTamanho() {
        assertThrows(IllegalArgumentException.class, () -> TrackingCodeGenerator.generate(7));
        assertThrows(IllegalArgumentException.class, () -> TrackingCodeGenerator.generate(33));
    }
}
