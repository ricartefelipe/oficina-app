package br.com.oficina.shared.security;

import com.nimbusds.jwt.JWTParser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.text.ParseException;

public final class MultiIssuerJwtDecoder implements JwtDecoder {

    private final JwtDecoder keycloakDecoder;
    private final JwtDecoder cpfHmacDecoder;
    private final String cpfIssuer;

    public MultiIssuerJwtDecoder(JwtDecoder keycloakDecoder, JwtDecoder cpfHmacDecoder, String cpfIssuer) {
        this.keycloakDecoder = keycloakDecoder;
        this.cpfHmacDecoder = cpfHmacDecoder;
        this.cpfIssuer = cpfIssuer;
    }

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            String iss = JWTParser.parse(token).getJWTClaimsSet().getIssuer();
            if (iss != null && iss.equals(cpfIssuer)) {
                return cpfHmacDecoder.decode(token);
            }
            return keycloakDecoder.decode(token);
        } catch (ParseException e) {
            throw new JwtException("Token JWT invalido", e);
        }
    }
}
