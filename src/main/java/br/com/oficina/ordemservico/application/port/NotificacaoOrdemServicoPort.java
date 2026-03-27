package br.com.oficina.ordemservico.application.port;

import br.com.oficina.ordemservico.domain.OrdemServico;

/**
 * Porta de saída para notificações ao cliente sobre a ordem de serviço (e-mail ou outros canais).
 */
public interface NotificacaoOrdemServicoPort {

    void aoEnviarOrcamento(OrdemServico os);

    void aoOrcamentoAprovado(OrdemServico os);

    void aoOrcamentoRecusado(OrdemServico os);

    void aoVeiculoEntregue(OrdemServico os);
}
