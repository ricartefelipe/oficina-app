package br.com.oficina.catalogo.peca.infra.persistence;

import br.com.oficina.catalogo.peca.domain.PecaInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface PecaInsumoJpaRepository extends JpaRepository<PecaInsumo, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PecaInsumo p where p.id = :id")
    Optional<PecaInsumo> findByIdForUpdate(@Param("id") UUID id);
}
