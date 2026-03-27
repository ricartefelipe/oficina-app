package br.com.oficina.cadastros.veiculo.infra.persistence;

import br.com.oficina.cadastros.veiculo.domain.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VeiculoJpaRepository extends JpaRepository<Veiculo, UUID> {

    Optional<Veiculo> findByPlaca_Value(String value);
}
