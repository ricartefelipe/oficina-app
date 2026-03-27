package br.com.oficina.catalogo.peca.domain;

import br.com.oficina.shared.domain.BusinessRuleException;
import br.com.oficina.shared.domain.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PecaInsumoTest {

    @Test
    void deveDecrementarEstoqueQuandoSuficiente() {
        PecaInsumo p = PecaInsumo.nova("Filtro de oleo", null, new BigDecimal("35.00"), 5, 1);
        p.decrementarEstoque(2);
        assertEquals(3, p.getEstoqueAtual());
        assertFalse(p.abaixoDoMinimo());
    }

    @Test
    void deveFalharQuandoEstoqueInsuficiente() {
        PecaInsumo p = PecaInsumo.nova("Pneu", null, new BigDecimal("250.00"), 1, 1);
        assertThrows(BusinessRuleException.class, () -> p.decrementarEstoque(2));
    }

    @Test
    void deveValidarQuantidadeAoDecrementarOuIncrementar() {
        PecaInsumo p = PecaInsumo.nova("Oleo", null, new BigDecimal("50.00"), 1, 1);
        assertThrows(ValidationException.class, () -> p.decrementarEstoque(0));
        assertThrows(ValidationException.class, () -> p.incrementarEstoque(0));

        p.incrementarEstoque(2);
        assertEquals(3, p.getEstoqueAtual());
    }

    @Test
    void deveIndicarQuandoAbaixoDoMinimo() {
        PecaInsumo p = PecaInsumo.nova("Oleo", null, new BigDecimal("50.00"), 1, 2);
        assertTrue(p.abaixoDoMinimo());
    }
}
