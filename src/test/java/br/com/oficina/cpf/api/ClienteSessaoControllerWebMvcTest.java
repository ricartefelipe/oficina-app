package br.com.oficina.cpf.api;

import br.com.oficina.shared.security.SecurityConfig;
import br.com.oficina.shared.security.SecurityCpfJwtProperties;
import br.com.oficina.shared.security.SecurityJwtProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ClienteSessaoController.class)
@Import(SecurityConfig.class)
@EnableConfigurationProperties({SecurityJwtProperties.class, SecurityCpfJwtProperties.class})
@TestPropertySource(properties = {
        "security.jwt.jwk-set-uri=http://localhost/dummy",
        "security.jwt.allowed-issuers=",
        "security.cpf-jwt.enabled=false",
        "security.cpf-jwt.issuer=https://oficina.local/auth/cpf",
        "security.cpf-jwt.secret="
})
class ClienteSessaoControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sessaoSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/cliente/sessao"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void sessaoComJwtCpfEClienteRetorna200() throws Exception {
        mockMvc.perform(get("/cliente/sessao")
                        .with(jwt()
                                .authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))
                                .jwt(j -> j
                                        .issuer("https://oficina.local/auth/cpf")
                                        .claim("authorities", List.of("ROLE_CLIENTE"))
                                        .claim("cliente_id", "550e8400-e29b-41d4-a716-446655440000")
                                        .subject("550e8400-e29b-41d4-a716-446655440000"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value("550e8400-e29b-41d4-a716-446655440000"));
    }
}
