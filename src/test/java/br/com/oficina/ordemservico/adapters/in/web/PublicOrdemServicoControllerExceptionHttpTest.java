package br.com.oficina.ordemservico.adapters.in.web;

import br.com.oficina.ordemservico.application.OrdemServicoObservability;
import br.com.oficina.ordemservico.application.OrdemServicoService;
import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.testsupport.NotificacaoOrdemServicoPortNoop;
import br.com.oficina.shared.api.GlobalExceptionHandler;
import br.com.oficina.shared.domain.BusinessRuleException;
import br.com.oficina.shared.domain.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.mock;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Erros HTTP padronizados (Problem Details) para API publica de OS.
 */
class PublicOrdemServicoControllerExceptionHttpTest {

    @Test
    void consultaTrackingInexistenteRetorna404() throws Exception {
        MockMvc mockMvc = mockMvc(new NotFoundStub());

        mockMvc.perform(get("/public/ordens-servico/NAOEXISTE12"))
                .andExpect(status().isNotFound());
    }

    @Test
    void aprovarComRegraDeNegocioRetorna409() throws Exception {
        MockMvc mockMvc = mockMvc(new ConflictStub());

        mockMvc.perform(post("/public/ordens-servico/TRACK12345678/aprovar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cpfCnpj\":\"52998224725\"}"))
                .andExpect(status().isConflict());
    }

    private static MockMvc mockMvc(OrdemServicoService service) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders
                .standaloneSetup(new PublicOrdemServicoController(service))
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static final class NotFoundStub extends OrdemServicoService {

        NotFoundStub() {
            super(null, null, null, null, null, null, NotificacaoOrdemServicoPortNoop.INSTANCE, mock(OrdemServicoObservability.class));
        }

        @Override
        public OrdemServico obterPorTrackingCode(String trackingCode) {
            throw new NotFoundException("Ordem de Servico nao encontrada");
        }
    }

    private static final class ConflictStub extends OrdemServicoService {

        ConflictStub() {
            super(null, null, null, null, null, null, NotificacaoOrdemServicoPortNoop.INSTANCE, mock(OrdemServicoObservability.class));
        }

        @Override
        public OrdemServico aprovarOrcamentoPublico(String trackingCode, String cpfCnpjRaw) {
            throw new BusinessRuleException("CPF/CNPJ nao confere para esta Ordem de Servico");
        }
    }
}
