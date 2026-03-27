package br.com.oficina.support;

import br.com.oficina.shared.security.KeycloakRealmRoleConverter;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * JWT de teste com {@code realm_access.roles}. O {@link SecurityMockMvcRequestPostProcessors#jwt()}
 * não usa o {@code JwtAuthenticationConverter} da aplicação: aplica por defeito
 * {@code JwtGrantedAuthoritiesConverter}, que ignora Keycloak. Por isso forçamos o mesmo
 * {@link KeycloakRealmRoleConverter} como conversor de authorities do post-processor.
 */
public final class KeycloakJwtRequestPostProcessor {

    private KeycloakJwtRequestPostProcessor() {
    }

    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor realmRoles(String... roles) {
        List<String> upper = Arrays.stream(roles).map(String::toUpperCase).toList();
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(j -> j.claim("realm_access", Map.of("roles", upper)))
                .authorities(new KeycloakRealmRoleConverter());
    }
}
