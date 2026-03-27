package br.com.oficina.ordemservico.application;

import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.domain.StatusOrdemServico;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public final class OrdemServicoSpecifications {

    private OrdemServicoSpecifications() {
    }

    public static Specification<OrdemServico> filtrar(
            StatusOrdemServico status,
            String placaNormalized,
            String cpfCnpjDigits,
            OffsetDateTime from,
            OffsetDateTime to,
            boolean excluirEncerradas,
            OrdemServicoListagemOrdem ordem
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (excluirEncerradas) {
                predicates.add(cb.not(root.get("status").in(
                        StatusOrdemServico.FINALIZADA,
                        StatusOrdemServico.ENTREGUE
                )));
            }

            if (placaNormalized != null && !placaNormalized.isBlank()) {
                Join<Object, Object> veiculo = root.join("veiculo");
                predicates.add(cb.equal(veiculo.get("placa").get("value"), placaNormalized));
            }

            if (cpfCnpjDigits != null && !cpfCnpjDigits.isBlank()) {
                Join<Object, Object> cliente = root.join("cliente");
                predicates.add(cb.equal(cliente.get("cpfCnpj").get("value"), cpfCnpjDigits));
            }

            if (from != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), from));
            }

            if (to != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), to));
            }

            aplicarOrdenacao(root, query, cb, ordem);

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void aplicarOrdenacao(
            Root<OrdemServico> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            OrdemServicoListagemOrdem ordem
    ) {
        switch (ordem) {
            case PRIORIDADE_OPERACAO -> {
                Expression<Integer> prioridade = prioridadeOperacaoExpression(root, cb);
                query.orderBy(cb.asc(prioridade), cb.asc(root.get("createdAt")));
            }
            case CRIADA_MAIS_ANTIGA -> query.orderBy(cb.asc(root.get("createdAt")));
            case CRIADA_MAIS_RECENTE -> query.orderBy(cb.desc(root.get("createdAt")));
        }
    }

    private static Expression<Integer> prioridadeOperacaoExpression(Root<OrdemServico> root, CriteriaBuilder cb) {
        return cb.<Integer>selectCase()
                .when(cb.equal(root.get("status"), StatusOrdemServico.EM_EXECUCAO), 1)
                .when(cb.equal(root.get("status"), StatusOrdemServico.AGUARDANDO_APROVACAO), 2)
                .when(cb.equal(root.get("status"), StatusOrdemServico.EM_DIAGNOSTICO), 3)
                .when(cb.equal(root.get("status"), StatusOrdemServico.RECEBIDA), 4)
                .otherwise(99);
    }
}
