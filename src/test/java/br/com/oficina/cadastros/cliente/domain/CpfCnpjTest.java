package br.com.oficina.cadastros.cliente.domain;

import br.com.oficina.shared.domain.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfCnpjTest {

    @Test
    void deveAceitarCpfValidoComOuSemMascara() {
        CpfCnpj cpf1 = assertDoesNotThrow(() -> CpfCnpj.of("529.982.247-25"));
        assertEquals("52998224725", cpf1.value());
        assertTrue(cpf1.isCpf());
        assertFalse(cpf1.isCnpj());

        CpfCnpj cpf2 = assertDoesNotThrow(() -> CpfCnpj.of("52998224725"));
        assertEquals("52998224725", cpf2.value());
    }

    @Test
    void deveAceitarCnpjValidoComOuSemMascara() {
        CpfCnpj cnpj1 = assertDoesNotThrow(() -> CpfCnpj.of("04.252.011/0001-10"));
        assertEquals("04252011000110", cnpj1.value());
        assertTrue(cnpj1.isCnpj());
        assertFalse(cnpj1.isCpf());

        CpfCnpj cnpj2 = assertDoesNotThrow(() -> CpfCnpj.of("04252011000110"));
        assertEquals("04252011000110", cnpj2.value());
    }

    @Test
    void deveRejeitarCpfCnpjInvalido() {
        assertThrows(ValidationException.class, () -> CpfCnpj.of("123.456.789-10"));
        assertThrows(ValidationException.class, () -> CpfCnpj.of("04.252.011/0001-11"));
        assertThrows(ValidationException.class, () -> CpfCnpj.of("000.000.000-00"));
        assertThrows(ValidationException.class, () -> CpfCnpj.of("00.000.000/0000-00"));
        assertThrows(ValidationException.class, () -> CpfCnpj.of("123"));
        assertThrows(ValidationException.class, () -> CpfCnpj.of(""));
        assertThrows(ValidationException.class, () -> CpfCnpj.of("   "));
    }
}
