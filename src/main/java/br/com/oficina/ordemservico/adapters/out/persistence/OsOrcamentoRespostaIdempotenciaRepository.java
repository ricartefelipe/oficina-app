package br.com.oficina.ordemservico.adapters.out.persistence;

import br.com.oficina.ordemservico.domain.OsOrcamentoRespostaIdempotencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface OsOrcamentoRespostaIdempotenciaRepository extends JpaRepository<OsOrcamentoRespostaIdempotencia, UUID> {

    Optional<OsOrcamentoRespostaIdempotencia> findByIdempotencyKey(String idempotencyKey);
}
