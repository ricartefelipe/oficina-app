package br.com.oficina.shared.openapi;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@OpenAPIDefinition(
        info = @Info(
                title = "Oficina API",
                version = "0.1.0",
                description = "MVP back-end do Sistema Integrado de Atendimento e Execucao de Servicos (Oficina).",
                contact = @Contact(name = "Equipe (definir em /docs/delivery/submission.md)")
        )
)
public class OpenApiConfig {
}
