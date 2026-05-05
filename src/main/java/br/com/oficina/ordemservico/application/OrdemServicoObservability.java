package br.com.oficina.ordemservico.application;

import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.domain.OrdemServicoTransicaoStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

/**
 * Métricas Micrometer para observabilidade (Fase 3): Prometheus / dashboards (volume, fases, falhas).
 */
@Component
public class OrdemServicoObservability {

    private final MeterRegistry registry;
    private final Counter osCriadas;
    private final Counter notificacaoFalhas;

    public OrdemServicoObservability(MeterRegistry registry) {
        this.registry = registry;
        this.osCriadas = Counter.builder("oficina.os.criadas")
                .description("Ordens de servico criadas")
                .register(registry);
        this.notificacaoFalhas = Counter.builder("oficina.os.notificacao_falhas")
                .description("Falhas ao enviar notificacao de OS (integracao externa)")
                .register(registry);
    }

    public void registrarOsCriada() {
        osCriadas.increment();
    }

    public void registrarNotificacaoFalha() {
        notificacaoFalhas.increment();
    }

    /**
     * Registra transicao de status e, quando aplicavel, tempo entre transicoes consecutivas (fase anterior).
     */
    public void registrarFluxoStatus(OrdemServico os) {
        List<OrdemServicoTransicaoStatus> tr = os.getTransicoesStatus();
        if (tr.isEmpty()) {
            return;
        }
        OrdemServicoTransicaoStatus ultima = tr.get(tr.size() - 1);
        registry.counter("oficina.os.transicoes", "para", ultima.getParaStatus().name()).increment();

        if (tr.size() >= 2) {
            OrdemServicoTransicaoStatus anterior = tr.get(tr.size() - 2);
            Duration duracao = Duration.between(anterior.getOcorridoEm(), ultima.getOcorridoEm());
            Timer.builder("oficina.os.duracao_fase")
                    .description("Tempo entre transicoes de status da OS (duracao na fase anterior)")
                    .tag("fase", anterior.getParaStatus().name())
                    .register(registry)
                    .record(duracao);
        }
    }
}
