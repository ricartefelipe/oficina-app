package br.com.oficina.ordemservico.application;

/**
 * Modo de ordenação da listagem administrativa de ordens de serviço.
 */
public enum OrdemServicoListagemOrdem {
    /** Ordem: EM_EXECUCAO, AGUARDANDO_APROVACAO, EM_DIAGNOSTICO, RECEBIDA; empate por {@code createdAt} ascendente (mais antigas primeiro). */
    PRIORIDADE_OPERACAO,
    /** {@code createdAt} ascendente. */
    CRIADA_MAIS_ANTIGA,
    /** {@code createdAt} descendente. */
    CRIADA_MAIS_RECENTE
}
