package br.com.oficina.ordemservico;

import br.com.oficina.support.KeycloakJwtRequestPostProcessor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PROBLEM_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrdemServicoFlowIntegrationTest {

    private static final UUID SERVICO_TROCA_OLEO_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PECA_FILTRO_OLEO_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("oficina")
            .withUsername("oficina")
            .withPassword("oficina");

    @DynamicPropertySource
    static void registerPgProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("security.jwt.jwk-set-uri", () -> "http://localhost/dummy");
        registry.add("security.jwt.allowed-issuers", () -> "");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void endpointsAdminDevemExigirJwtComRoleAdmin() throws Exception {
        // Sem token -> 401
        mockMvc.perform(get("/admin/clientes"))
                .andExpect(status().isUnauthorized());

        // Com token, mas sem role ADMIN -> 403
        mockMvc.perform(get("/admin/clientes")
                        .with(KeycloakJwtRequestPostProcessor.realmRoles("USER")))
                .andExpect(status().isForbidden());

        // Com role ADMIN -> 200
        mockMvc.perform(get("/admin/clientes")
                        .with(KeycloakJwtRequestPostProcessor.realmRoles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void fluxoCompletoDaOrdemDeServicoDeveFuncionarEDecrementarEstoque() throws Exception {
        String cpf = "529.982.247-25";

        Map<String, Object> body = Map.of(
                "cliente", Map.of(
                        "nome", "Joao da Silva",
                        "cpfCnpj", cpf
                ),
                "veiculo", Map.of(
                        "placa", "ABC-1234",
                        "marca", "VW",
                        "modelo", "Gol",
                        "ano", 2018
                ),
                "servicos", List.of(
                        Map.of(
                                "servicoId", SERVICO_TROCA_OLEO_ID.toString(),
                                "quantidade", 1
                        )
                ),
                "pecas", List.of(
                        Map.of(
                                "pecaId", PECA_FILTRO_OLEO_ID.toString(),
                                "quantidade", 2
                        )
                )
        );

        // 1) Criar OS (RECEBIDA)
        MvcResult createdResult = mockMvc.perform(post("/admin/ordens-servico")
                        .with(KeycloakJwtRequestPostProcessor.realmRoles("ADMIN"))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                // Pode variar se o teste considerar ou nao o context-path (/api) na Location
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("/admin/ordens-servico/")))
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andReturn();

        JsonNode created = objectMapper.readTree(createdResult.getResponse().getContentAsString());
        UUID osId = UUID.fromString(created.get("id").asText());
        String trackingCode = created.get("trackingCode").asText();
        BigDecimal orcamentoTotal = created.get("orcamentoTotal").decimalValue();

        // 150.00 + (35.00*2) = 220.00
        assertEquals(0, new BigDecimal("220.00").compareTo(orcamentoTotal));

        // 2) Iniciar diagnostico
        mockMvc.perform(post("/admin/ordens-servico/{id}/diagnostico/iniciar", osId)
                        .with(KeycloakJwtRequestPostProcessor.realmRoles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"));

        // 3) Enviar orcamento
        mockMvc.perform(post("/admin/ordens-servico/{id}/orcamento/enviar", osId)
                        .with(KeycloakJwtRequestPostProcessor.realmRoles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.orcamentoEnviadoAt").isNotEmpty());

        // 4) Consulta publica por trackingCode
        mockMvc.perform(get("/public/ordens-servico/{trackingCode}", trackingCode))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackingCode").value(trackingCode))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"));

        // 5) Aprovar orcamento (public). Ao aprovar entra em EM_EXECUCAO e decrementa estoque
        mockMvc.perform(post("/public/ordens-servico/{trackingCode}/aprovar", trackingCode)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cpfCnpj", cpf))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$.aprovadoAt").isNotEmpty());

        // Estoque da peca deve ter decrementado (seed: 50, usado: 2 => 48)
        mockMvc.perform(get("/admin/pecas/{id}", PECA_FILTRO_OLEO_ID)
                        .with(KeycloakJwtRequestPostProcessor.realmRoles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estoqueAtual").value(48));

        // 6) Finalizar execucao
        mockMvc.perform(post("/admin/ordens-servico/{id}/execucao/finalizar", osId)
                        .with(KeycloakJwtRequestPostProcessor.realmRoles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZADA"));

        // 7) Registrar entrega
        mockMvc.perform(post("/admin/ordens-servico/{id}/entrega/registrar", osId)
                        .with(KeycloakJwtRequestPostProcessor.realmRoles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"));

        // 8) Metricas (tempo medio execucao) deve considerar ao menos 1 OS
        MvcResult metricasResult = mockMvc.perform(get("/admin/metricas/tempo-medio-execucao")
                        .with(KeycloakJwtRequestPostProcessor.realmRoles("ADMIN")))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode metricas = objectMapper.readTree(metricasResult.getResponse().getContentAsString());
        assertTrue(metricas.get("countConsidered").asLong() >= 1);
    }

    @Test
    void aprovarOrcamentoDeveValidarCpfCnpjDoCliente() throws Exception {
        // cria OS
        Map<String, Object> body = Map.of(
                "cliente", Map.of(
                        "nome", "Cliente",
                        "cpfCnpj", "529.982.247-25"
                ),
                "veiculo", Map.of(
                        "placa", "ABC1D23",
                        "marca", "VW",
                        "modelo", "Polo",
                        "ano", 2020
                ),
                "servicos", List.of(
                        Map.of(
                                "servicoId", SERVICO_TROCA_OLEO_ID.toString(),
                                "quantidade", 1
                        )
                )
        );

        MvcResult createdResult = mockMvc.perform(post("/admin/ordens-servico")
                        .with(KeycloakJwtRequestPostProcessor.realmRoles("ADMIN"))
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode created = objectMapper.readTree(createdResult.getResponse().getContentAsString());
        UUID osId = UUID.fromString(created.get("id").asText());
        String trackingCode = created.get("trackingCode").asText();

        // diagnostico + enviar orcamento
        mockMvc.perform(post("/admin/ordens-servico/{id}/diagnostico/iniciar", osId)
                        .with(KeycloakJwtRequestPostProcessor.realmRoles("ADMIN")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/admin/ordens-servico/{id}/orcamento/enviar", osId)
                        .with(KeycloakJwtRequestPostProcessor.realmRoles("ADMIN")))
                .andExpect(status().isOk());

        // cpf errado -> 409
        mockMvc.perform(post("/public/ordens-servico/{trackingCode}/aprovar", trackingCode)
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("cpfCnpj", "111.444.777-35"))))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_PROBLEM_JSON));
    }
}
