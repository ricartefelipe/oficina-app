package br.com.oficina.cadastros.veiculo.domain;

import br.com.oficina.shared.domain.ValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlacaTest {

    @Test
    void deveAceitarPlacaAntigaComOuSemHifen() {
        Placa p1 = assertDoesNotThrow(() -> Placa.of("ABC-1234"));
        assertEquals("ABC1234", p1.value());

        Placa p2 = assertDoesNotThrow(() -> Placa.of("abc1234"));
        assertEquals("ABC1234", p2.value());
    }

    @Test
    void deveAceitarPlacaMercosul() {
        Placa p = assertDoesNotThrow(() -> Placa.of("ABC1D23"));
        assertEquals("ABC1D23", p.value());
    }

    @Test
    void deveRejeitarPlacaInvalida() {
        assertThrows(ValidationException.class, () -> Placa.of("ABCD123"));
        assertThrows(ValidationException.class, () -> Placa.of("AB12345"));
        assertThrows(ValidationException.class, () -> Placa.of("ABC12D3"));
        assertThrows(ValidationException.class, () -> Placa.of(""));
        assertThrows(ValidationException.class, () -> Placa.of("   "));
    }
}
