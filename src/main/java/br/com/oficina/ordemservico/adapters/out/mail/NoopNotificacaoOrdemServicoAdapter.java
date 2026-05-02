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
        // envio desabilitado (app.notification.enabled=false)
    }

    @Override
    public void aoOrcamentoAprovado(OrdemServico os) {
        // envio desabilitado (app.notification.enabled=false)
    }

    @Override
    public void aoOrcamentoRecusado(OrdemServico os) {
        // envio desabilitado (app.notification.enabled=false)
    }

    @Override
    public void aoVeiculoEntregue(OrdemServico os) {
        // envio desabilitado (app.notification.enabled=false)
    }
}
