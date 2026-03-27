package br.com.oficina.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.cpf-jwt")
public record SecurityCpfJwtProperties(
        boolean enabled,
        String issuer,
        String secret
) {
    public boolean ready() {
        return enabled && secret != null && !secret.isBlank();
    }
}
