package br.com.oficina.shared.security;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

public class AllowedIssuersValidator implements OAuth2TokenValidator<Jwt> {

    private final List<String> allowedIssuers;

    public AllowedIssuersValidator(List<String> allowedIssuers) {
        this.allowedIssuers = allowedIssuers == null ? List.of() : List.copyOf(allowedIssuers);
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        if (allowedIssuers.isEmpty()) {
            // Se nao configurado, nao valida issuer (nao recomendado para prod).
            return OAuth2TokenValidatorResult.success();
        }

        String issuer = token.getIssuer() != null ? token.getIssuer().toString() : null;
        if (issuer != null && allowedIssuers.contains(issuer)) {
            return OAuth2TokenValidatorResult.success();
        }

        OAuth2Error error = new OAuth2Error(
                "invalid_token",
                "Invalid issuer: " + (issuer == null ? "(missing)" : issuer),
                null
        );
        return OAuth2TokenValidatorResult.failure(error);
    }
}
