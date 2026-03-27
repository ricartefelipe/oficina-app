package br.com.oficina.ordemservico.adapters.in.web;

import br.com.oficina.cadastros.cliente.domain.Cliente;
import br.com.oficina.cadastros.cliente.domain.CpfCnpj;
import br.com.oficina.cadastros.veiculo.domain.Placa;
import br.com.oficina.cadastros.veiculo.domain.Veiculo;
import br.com.oficina.ordemservico.application.OrdemServicoObservability;
import br.com.oficina.ordemservico.application.OrdemServicoService;
import br.com.oficina.ordemservico.testsupport.NotificacaoOrdemServicoPortNoop;
import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.domain.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublicOrdemServicoControllerConsultaHttpTest {

    @Test
    void consultaPorTrackingCodeRetornaStatusAtual() throws Exception {
        Cliente cliente = Cliente.novo("Cliente Teste", CpfCnpj.of("52998224725"));
        Veiculo veiculo = Veiculo.novo(Placa.of("ABC1D23"), "Marca", "Modelo", 2020, cliente);
        OrdemServico os = OrdemServico.receber(cliente, veiculo, "TRACKTST12");
        os.iniciarDiagnostico();

        OrdemServicoService stub = new ObterPorTrackingCodeStub(os);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PublicOrdemServicoController(stub)).build();

        mockMvc.perform(get("/public/ordens-servico/tracktst12"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingCode").value("TRACKTST12"))
                .andExpect(jsonPath("$.status").value(StatusOrdemServico.EM_DIAGNOSTICO.name()));
    }

    private static final class ObterPorTrackingCodeStub extends OrdemServicoService {

        private final OrdemServico ordem;

        ObterPorTrackingCodeStub(OrdemServico ordem) {
            super(null, null, null, null, null, null, NotificacaoOrdemServicoPortNoop.INSTANCE, mock(OrdemServicoObservability.class));
            this.ordem = ordem;
        }

        @Override
        public OrdemServico obterPorTrackingCode(String trackingCode) {
            return ordem;
        }
    }
}
