package br.com.oficina.ordemservico.adapters.out.persistence;

import br.com.oficina.ordemservico.application.port.OrdemServicoPersistencePort;
import br.com.oficina.ordemservico.domain.OrdemServico;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrdemServicoPersistenceAdapter implements OrdemServicoPersistencePort {

    private final OrdemServicoJpaRepository jpa;

    public OrdemServicoPersistenceAdapter(OrdemServicoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public OrdemServico save(OrdemServico entity) {
        return jpa.save(entity);
    }

    @Override
    public Optional<OrdemServico> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<OrdemServico> findByTrackingCode(String trackingCode) {
        return jpa.findByTrackingCode(trackingCode);
    }

    @Override
    public Optional<OrdemServico> findDetailedById(UUID id) {
        return jpa.findDetailedById(id);
    }

    @Override
    public Optional<OrdemServico> findDetailedByIdForUpdate(UUID id) {
        return jpa.findDetailedByIdForUpdate(id);
    }

    @Override
    public Optional<OrdemServico> findDetailedByTrackingCode(String trackingCode) {
        return jpa.findDetailedByTrackingCode(trackingCode);
    }

    @Override
    public List<OrdemServico> findAll(Specification<OrdemServico> spec) {
        return jpa.findAll(spec);
    }

    @Override
    public Double avgExecutionSeconds(OffsetDateTime from, OffsetDateTime to) {
        return jpa.avgExecutionSeconds(from, to);
    }

    @Override
    public Long countExecutionMeasured(OffsetDateTime from, OffsetDateTime to) {
        return jpa.countExecutionMeasured(from, to);
    }
}
