package br.com.oficina.shared.security;

import br.com.oficina.shared.infra.jackson.FallbackObjectMapperConfig;
import br.com.oficina.cadastros.cliente.api.admin.AdminClienteController;
import br.com.oficina.cadastros.cliente.application.ClienteService;
import br.com.oficina.cadastros.cliente.infra.persistence.ClienteJpaRepository;
import br.com.oficina.support.KeycloakJwtRequestPostProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Garante 401/403/200 nos endpoints admin com OAuth2 Resource Server (JWT),
 * sem subir PostgreSQL (corre no perfil {@code ci}).
 */
@WebMvcTest(controllers = AdminClienteController.class)
@Import({SecurityConfig.class, AdminJwtSecurityWebMvcTest.ClienteServiceStubConfig.class, FallbackObjectMapperConfig.class})
@EnableConfigurationProperties({SecurityJwtProperties.class, SecurityCpfJwtProperties.class})
@TestPropertySource(properties = {
        "security.jwt.jwk-set-uri=http://localhost/dummy",
        "security.jwt.allowed-issuers=",
        "security.cpf-jwt.enabled=false",
        "security.cpf-jwt.issuer=https://oficina.local/auth/cpf",
        "security.cpf-jwt.secret="
})
class AdminJwtSecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listarSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/admin/clientes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listarComJwtSemRoleAdminRetorna403() throws Exception {
        mockMvc.perform(get("/admin/clientes")
                        .with(KeycloakJwtRequestPostProcessor.realmRoles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void listarComRoleAdminRetorna200() throws Exception {
        mockMvc.perform(get("/admin/clientes")
                        .with(KeycloakJwtRequestPostProcessor.realmRoles("ADMIN")))
                .andExpect(status().isOk());
    }

    /**
     * Repositório mockado para não inicializar JPA/Hibernate na fatia {@code @WebMvcTest}.
     */
    static class ClienteServiceStubConfig {

        @Bean
        @Primary
        ClienteService clienteServiceStub() {
            ClienteJpaRepository repo = mock(ClienteJpaRepository.class);
            when(repo.findAll()).thenReturn(List.of());
            return new ClienteService(repo);
        }
    }
}
