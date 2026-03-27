package br.com.oficina.ordemservico.application.port;

import br.com.oficina.ordemservico.domain.OrdemServico;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoPersistencePort {

    OrdemServico save(OrdemServico entity);

    Optional<OrdemServico> findById(UUID id);

    Optional<OrdemServico> findByTrackingCode(String trackingCode);

    Optional<OrdemServico> findDetailedById(UUID id);

    /**
     * Carrega a OS com grafo completo e bloqueio pessimista (para transições e idempotência).
     */
    Optional<OrdemServico> findDetailedByIdForUpdate(UUID id);

    Optional<OrdemServico> findDetailedByTrackingCode(String trackingCode);

    List<OrdemServico> findAll(Specification<OrdemServico> spec);

    Double avgExecutionSeconds(OffsetDateTime from, OffsetDateTime to);

    Long countExecutionMeasured(OffsetDateTime from, OffsetDateTime to);
}
