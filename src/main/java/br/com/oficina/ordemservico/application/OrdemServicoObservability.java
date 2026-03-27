package br.com.oficina.ordemservico.application;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Métricas Micrometer para observabilidade (Fase 3): scrape Prometheus / dashboards.
 */
@Component
public class OrdemServicoObservability {

    private final Counter osCriadas;

    public OrdemServicoObservability(MeterRegistry registry) {
        this.osCriadas = Counter.builder("oficina.os.criadas")
                .description("Ordens de serviço criadas")
                .register(registry);
    }

    public void registrarOsCriada() {
        osCriadas.increment();
    }
}
