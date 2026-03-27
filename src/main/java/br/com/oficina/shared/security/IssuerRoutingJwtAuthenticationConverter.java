package br.com.oficina.shared.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class IssuerRoutingJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtAuthenticationConverter keycloakConverter;
    private final String cpfIssuer;

    public IssuerRoutingJwtAuthenticationConverter(String cpfIssuer) {
        this.cpfIssuer = cpfIssuer;
        this.keycloakConverter = new JwtAuthenticationConverter();
        this.keycloakConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String iss = jwt.getIssuer() != null ? jwt.getIssuer().toString() : "";
        if (iss.equals(cpfIssuer)) {
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            List<String> raw = jwt.getClaimAsStringList("authorities");
            if (raw != null) {
                for (String a : raw) {
                    if (a != null && !a.isBlank()) {
                        String r = a.trim();
                        authorities.add(new SimpleGrantedAuthority(r.startsWith("ROLE_") ? r : "ROLE_" + r));
                    }
                }
            }
            return new JwtAuthenticationToken(jwt, authorities);
        }
        return (AbstractAuthenticationToken) keycloakConverter.convert(jwt);
    }
}
