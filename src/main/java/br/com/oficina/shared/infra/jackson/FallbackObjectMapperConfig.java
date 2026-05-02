package br.com.oficina.shared.infra.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@link ObjectMapper} para {@link br.com.oficina.shared.security.SecurityConfig}
 * quando auto-config Jackson não expõe o bean (ex.: fatias {@code @WebMvcTest}).
 */
@Configuration
public class FallbackObjectMapperConfig {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
