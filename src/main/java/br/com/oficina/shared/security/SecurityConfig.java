package br.com.oficina.shared.security;

import br.com.oficina.shared.infra.http.CorrelationIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Bean
    public CorrelationIdFilter correlationIdFilter() {
        return new CorrelationIdFilter();
    }

    @Bean
    public JwtDecoder jwtDecoder(SecurityJwtProperties props, SecurityCpfJwtProperties cpfJwt) {
        NimbusJwtDecoder keycloak = NimbusJwtDecoder.withJwkSetUri(props.jwkSetUri()).build();
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        OAuth2TokenValidator<Jwt> withIssuer = new AllowedIssuersValidator(props.allowedIssuersList());
        keycloak.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withTimestamp, withIssuer));
        if (!cpfJwt.ready()) {
            return keycloak;
        }
        SecretKeySpec key = new SecretKeySpec(cpfJwt.secret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        NimbusJwtDecoder cpfDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        OAuth2TokenValidator<Jwt> cpfIss = new AllowedIssuersValidator(List.of(cpfJwt.issuer()));
        cpfDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(new JwtTimestampValidator(), cpfIss));
        return new MultiIssuerJwtDecoder(keycloak, cpfDecoder, cpfJwt.issuer());
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter(SecurityCpfJwtProperties cpfJwt) {
        String iss = cpfJwt.ready() ? cpfJwt.issuer() : "";
        return new IssuerRoutingJwtAuthenticationConverter(iss);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   CorrelationIdFilter correlationIdFilter,
                                                   JwtDecoder jwtDecoder,
                                                   Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter,
                                                   ObjectMapper objectMapper) throws Exception {

        var authEntryPoint = new ProblemDetailsAuthEntryPoint(objectMapper);
        var accessDeniedHandler = new ProblemDetailsAccessDeniedHandler(objectMapper);

        // Correlation-id deve existir inclusive para respostas 401/403
        http.addFilterBefore(correlationIdFilter, SecurityContextHolderFilter.class);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(eh -> eh
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        // Publico (cliente)
                        .requestMatchers("/public/**").permitAll()

                        // Swagger/OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/openapi/**",
                                "/webjars/**"
                        ).permitAll()

                        // Health checks
                        .requestMatchers(
                                "/actuator/health/**",
                                "/actuator/info/**",
                                "/actuator/prometheus"
                        ).permitAll()

                        .requestMatchers("/cliente/**").hasAuthority("ROLE_CLIENTE")

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Negar por padrao
                        .anyRequest().denyAll()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder)
                                .jwtAuthenticationConverter(jwtAuthenticationConverter)
                        )
                );

        return http.build();
    }
}
