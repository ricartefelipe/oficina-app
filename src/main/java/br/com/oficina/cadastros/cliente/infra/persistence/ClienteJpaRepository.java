package br.com.oficina.cadastros.cliente.infra.persistence;

import br.com.oficina.cadastros.cliente.domain.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ClienteJpaRepository extends JpaRepository<Cliente, UUID> {

    Optional<Cliente> findByCpfCnpj_Value(String value);
}
