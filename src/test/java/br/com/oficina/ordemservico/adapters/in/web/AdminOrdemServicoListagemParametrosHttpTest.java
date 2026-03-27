package br.com.oficina.ordemservico.adapters.in.web;

import br.com.oficina.ordemservico.application.OrdemServicoObservability;
import br.com.oficina.ordemservico.application.OrdemServicoService;
import br.com.oficina.ordemservico.testsupport.NotificacaoOrdemServicoPortNoop;
import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.domain.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.Mockito.mock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminOrdemServicoListagemParametrosHttpTest {

    @Test
    void listarSemParametroIncluirEncerradasPassaFalseAoServico() throws Exception {
        var stub = new ListarStub();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminOrdemServicoController(stub)).build();

        mockMvc.perform(get("/admin/ordens-servico"))
                .andExpect(status().isOk());

        assertFalse(stub.ultimoIncluirEncerradas);
    }

    @Test
    void listarComIncluirEncerradasTruePassaTrueAoServico() throws Exception {
        var stub = new ListarStub();
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminOrdemServicoController(stub)).build();

        mockMvc.perform(get("/admin/ordens-servico").param("incluirEncerradas", "true"))
                .andExpect(status().isOk());

        assertTrue(stub.ultimoIncluirEncerradas);
    }

    private static final class ListarStub extends OrdemServicoService {

        boolean ultimoIncluirEncerradas;

        ListarStub() {
            super(null, null, null, null, null, null, NotificacaoOrdemServicoPortNoop.INSTANCE, mock(OrdemServicoObservability.class));
        }

        @Override
        public List<OrdemServico> listar(
                StatusOrdemServico status,
                String placa,
                String cpfCnpj,
                OffsetDateTime from,
                OffsetDateTime to,
                boolean incluirEncerradas
        ) {
            ultimoIncluirEncerradas = incluirEncerradas;
            return List.of();
        }
    }
}
