package br.com.oficina.ordemservico.adapters.in.web;

import br.com.oficina.cadastros.cliente.domain.Cliente;
import br.com.oficina.cadastros.cliente.domain.CpfCnpj;
import br.com.oficina.cadastros.veiculo.domain.Placa;
import br.com.oficina.cadastros.veiculo.domain.Veiculo;
import br.com.oficina.ordemservico.application.OrdemServicoObservability;
import br.com.oficina.ordemservico.application.OrdemServicoService;
import br.com.oficina.ordemservico.testsupport.NotificacaoOrdemServicoPortNoop;
import br.com.oficina.ordemservico.domain.DecisaoRespostaOrcamentoExterna;
import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.domain.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminRespostaOrcamentoExternaHttpTest {

    @Test
    void postComIdempotencyKeyDelegaDecisaoAoServico() throws Exception {
        UUID uuid = UUID.fromString("00000000-0000-0000-0000-000000000001");
        Cliente cliente = Cliente.novo("Cliente", CpfCnpj.of("52998224725"));
        Veiculo veiculo = Veiculo.novo(Placa.of("ABC1D23"), "Marca", "Modelo", 2020, cliente);
        OrdemServico os = OrdemServico.receber(cliente, veiculo, "TRACKTST12");
        os.iniciarDiagnostico();
        os.enviarOrcamento();

        var stub = new RespostaExternaStub(os);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminOrdemServicoController(stub)).build();

        mockMvc.perform(post("/admin/ordens-servico/{id}/orcamento/resposta-externa", uuid)
                        .header("Idempotency-Key", "idem-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decisao\":\"RECUSAR\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(StatusOrdemServico.AGUARDANDO_APROVACAO.name()));

        assertEquals(uuid, stub.lastOsId);
        assertEquals("idem-1", stub.lastKey);
        assertEquals(DecisaoRespostaOrcamentoExterna.RECUSAR, stub.lastDecisao);
    }

    @Test
    void postSemIdempotencyKeyRetorna4xx() throws Exception {
        var stub = new RespostaExternaStub(null);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AdminOrdemServicoController(stub)).build();

        mockMvc.perform(post("/admin/ordens-servico/{id}/orcamento/resposta-externa", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"decisao\":\"APROVAR\"}"))
                .andExpect(status().is4xxClientError());

        assertFalse(stub.chamouServico);
    }

    private static final class RespostaExternaStub extends OrdemServicoService {

        private final OrdemServico resposta;
        UUID lastOsId;
        String lastKey;
        DecisaoRespostaOrcamentoExterna lastDecisao;
        boolean chamouServico;

        RespostaExternaStub(OrdemServico resposta) {
            super(null, null, null, null, null, null, NotificacaoOrdemServicoPortNoop.INSTANCE, mock(OrdemServicoObservability.class));
            this.resposta = resposta;
        }

        @Override
        public OrdemServico processarRespostaOrcamentoExterna(UUID osId, String idempotencyKeyRaw, DecisaoRespostaOrcamentoExterna decisao) {
            chamouServico = true;
            lastOsId = osId;
            lastKey = idempotencyKeyRaw;
            lastDecisao = decisao;
            return resposta;
        }
    }
}
