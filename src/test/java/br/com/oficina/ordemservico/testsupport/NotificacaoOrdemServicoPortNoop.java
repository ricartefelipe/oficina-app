package br.com.oficina.ordemservico.testsupport;

import br.com.oficina.ordemservico.application.port.NotificacaoOrdemServicoPort;
import br.com.oficina.ordemservico.domain.OrdemServico;

/**
 * Implementação vazia para testes unitários que instanciam {@link br.com.oficina.ordemservico.application.OrdemServicoService} manualmente.
 */
public final class NotificacaoOrdemServicoPortNoop {

    public static final NotificacaoOrdemServicoPort INSTANCE = new NotificacaoOrdemServicoPort() {
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
    };

    private NotificacaoOrdemServicoPortNoop() {
    }
}
