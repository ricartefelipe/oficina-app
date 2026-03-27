package br.com.oficina.ordemservico.domain;

import br.com.oficina.shared.domain.BusinessRuleException;

import java.util.EnumSet;
import java.util.Set;

/**
 * Status obrigatorios do enunciado.
 */
public enum StatusOrdemServico {
    RECEBIDA,
    EM_DIAGNOSTICO,
    AGUARDANDO_APROVACAO,
    EM_EXECUCAO,
    FINALIZADA,
    ENTREGUE,
    /** Orçamento recusado pelo cliente ou por sistema externo; OS encerrada sem execução. */
    CANCELADA;

    public Set<StatusOrdemServico> proximosPermitidos() {
        return switch (this) {
            case RECEBIDA -> EnumSet.of(EM_DIAGNOSTICO);
            case EM_DIAGNOSTICO -> EnumSet.of(AGUARDANDO_APROVACAO);
            case AGUARDANDO_APROVACAO -> EnumSet.of(EM_EXECUCAO, CANCELADA);
            case EM_EXECUCAO -> EnumSet.of(FINALIZADA);
            case FINALIZADA -> EnumSet.of(ENTREGUE);
            case ENTREGUE -> EnumSet.noneOf(StatusOrdemServico.class);
            case CANCELADA -> EnumSet.noneOf(StatusOrdemServico.class);
        };
    }

    public void validarTransicaoPara(StatusOrdemServico novoStatus) {
        if (novoStatus == null) {
            throw new BusinessRuleException("novoStatus nao pode ser null");
        }
        if (this == novoStatus) {
            throw new BusinessRuleException("Transicao invalida: status ja esta em " + this);
        }
        if (!proximosPermitidos().contains(novoStatus)) {
            throw new BusinessRuleException("Transicao invalida: " + this + " -> " + novoStatus);
        }
    }
}
