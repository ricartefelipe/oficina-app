package br.com.oficina.ordemservico.adapters.out.persistence;

import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.domain.StatusOrdemServico;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoJpaRepository extends JpaRepository<OrdemServico, UUID>, JpaSpecificationExecutor<OrdemServico> {

    Optional<OrdemServico> findByTrackingCode(String trackingCode);

    List<OrdemServico> findByStatus(StatusOrdemServico status);

    @EntityGraph(attributePaths = {
            "cliente",
            "veiculo"
    })
    List<OrdemServico> findAll(Specification<OrdemServico> spec);

    @EntityGraph(attributePaths = {
            "cliente",
            "veiculo",
            "itensServico",
            "itensServico.servico",
            "itensPeca",
            "itensPeca.peca",
            "transicoesStatus"
    })
    Optional<OrdemServico> findDetailedById(UUID id);

    @EntityGraph(attributePaths = {
            "cliente",
            "veiculo",
            "itensServico",
            "itensServico.servico",
            "itensPeca",
            "itensPeca.peca",
            "transicoesStatus"
    })
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrdemServico o where o.id = :id")
    Optional<OrdemServico> findDetailedByIdForUpdate(@Param("id") UUID id);

    @EntityGraph(attributePaths = {
            "cliente",
            "veiculo",
            "itensServico",
            "itensServico.servico",
            "itensPeca",
            "itensPeca.peca",
            "transicoesStatus"
    })
    Optional<OrdemServico> findDetailedByTrackingCode(String trackingCode);

    @Query(
            value = """
                    select avg(extract(epoch from t_final.ocorrido_em) - extract(epoch from t_exec.ocorrido_em))
                    from os_transicoes_status t_exec
                    join os_transicoes_status t_final
                      on t_final.ordem_servico_id = t_exec.ordem_servico_id
                    where t_exec.para_status = 'EM_EXECUCAO'
                      and t_final.para_status = 'FINALIZADA'
                      and t_final.ocorrido_em >= :from
                      and t_final.ocorrido_em <= :to
                    """,
            nativeQuery = true
    )
    Double avgExecutionSeconds(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);

    @Query(
            value = """
                    select count(distinct t_final.ordem_servico_id)
                    from os_transicoes_status t_exec
                    join os_transicoes_status t_final
                      on t_final.ordem_servico_id = t_exec.ordem_servico_id
                    where t_exec.para_status = 'EM_EXECUCAO'
                      and t_final.para_status = 'FINALIZADA'
                      and t_final.ocorrido_em >= :from
                      and t_final.ocorrido_em <= :to
                    """,
            nativeQuery = true
    )
    Long countExecutionMeasured(@Param("from") OffsetDateTime from, @Param("to") OffsetDateTime to);
}
