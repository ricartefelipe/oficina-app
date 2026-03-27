package br.com.oficina.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "security.jwt")
public record SecurityJwtProperties(
        String jwkSetUri,
        String allowedIssuers
) {

    public List<String> allowedIssuersList() {
        if (allowedIssuers == null || allowedIssuers.isBlank()) {
            return List.of();
        }
        return Arrays.stream(allowedIssuers.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }
}
