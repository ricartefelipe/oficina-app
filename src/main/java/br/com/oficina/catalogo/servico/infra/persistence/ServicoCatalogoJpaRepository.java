package br.com.oficina.catalogo.servico.infra.persistence;

import br.com.oficina.catalogo.servico.domain.ServicoCatalogo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServicoCatalogoJpaRepository extends JpaRepository<ServicoCatalogo, UUID> {
}
