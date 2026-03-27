package br.com.oficina.ordemservico.api.admin;

import br.com.oficina.ordemservico.adapters.in.web.AdminOrdemServicoController;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CriarOrdemServicoRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void servicosVazio_invalido() {
        var req = new AdminOrdemServicoController.CriarOrdemServicoRequest(
                new AdminOrdemServicoController.ClienteInput("Nome", "52998224725"),
                new AdminOrdemServicoController.VeiculoInput("ABC1D23", "Marca", "Modelo", 2020),
                List.of(),
                null
        );
        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void servicosComUmItem_valido() {
        UUID servicoId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        var req = new AdminOrdemServicoController.CriarOrdemServicoRequest(
                new AdminOrdemServicoController.ClienteInput("Nome", "52998224725"),
                new AdminOrdemServicoController.VeiculoInput("ABC1D23", "Marca", "Modelo", 2020),
                List.of(new AdminOrdemServicoController.ItemServicoInput(servicoId, 1)),
                null
        );
        assertThat(validator.validate(req)).isEmpty();
    }
}
