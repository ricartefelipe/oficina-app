package br.com.oficina.ordemservico.adapters.out.mail;

import br.com.oficina.ordemservico.application.port.NotificacaoOrdemServicoPort;
import br.com.oficina.ordemservico.domain.OrdemServico;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Desliga envio de e-mail quando {@code app.notification.enabled=false}.
 */
@Component
@ConditionalOnProperty(prefix = "app.notification", name = "enabled", havingValue = "false")
public class NoopNotificacaoOrdemServicoAdapter implements NotificacaoOrdemServicoPort {

    @Override
    public void aoEnviarOrcamento(OrdemServico os) {
    }

    @Override
    public void aoOrcamentoAprovado(OrdemServico os) {
    }

    @Override
    public void aoOrcamentoRecusado(OrdemServico os) {
    }

    @Override
    public void aoVeiculoEntregue(OrdemServico os) {
    }
}
