package br.com.oficina.ordemservico.api.admin;

import br.com.oficina.ordemservico.adapters.in.web.AdminOrdemServicoController;
import br.com.oficina.shared.api.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminOrdemServicoControllerHttpTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AdminOrdemServicoController(null))
                .setValidator(validator)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void criar_semServicos_retorna400() throws Exception {
        String body = """
                {"cliente":{"nome":"Joao","cpfCnpj":"52998224725"},"veiculo":{"placa":"ABC1D23","marca":"F","modelo":"Fi","ano":2020},"servicos":[]}
                """;
        mockMvc.perform(post("/admin/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_cpfInvalido_retorna400() throws Exception {
        String body = """
                {"cliente":{"nome":"Joao","cpfCnpj":"11111111111"},"veiculo":{"placa":"ABC1D23","marca":"F","modelo":"Fi","ano":2020},"servicos":[{"servicoId":"00000000-0000-0000-0000-000000000001","quantidade":1}]}
                """;
        mockMvc.perform(post("/admin/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_placaInvalida_retorna400() throws Exception {
        String body = """
                {"cliente":{"nome":"Joao","cpfCnpj":"52998224725"},"veiculo":{"placa":"INVALID","marca":"F","modelo":"Fi","ano":2020},"servicos":[{"servicoId":"00000000-0000-0000-0000-000000000001","quantidade":1}]}
                """;
        mockMvc.perform(post("/admin/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
