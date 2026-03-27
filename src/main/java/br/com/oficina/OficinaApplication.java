package br.com.oficina;

import org.springframework.boot.SpringApplication;
import br.com.oficina.ordemservico.adapters.out.mail.NotificacaoOrdemServicoProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableConfigurationProperties(NotificacaoOrdemServicoProperties.class)
public class OficinaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OficinaApplication.class, args);
    }
}
