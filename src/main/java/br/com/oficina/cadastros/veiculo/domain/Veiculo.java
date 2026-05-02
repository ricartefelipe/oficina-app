package br.com.oficina.cadastros.veiculo.domain;

import br.com.oficina.cadastros.cliente.domain.Cliente;
import br.com.oficina.shared.domain.Strings;
import br.com.oficina.shared.domain.ValidationException;
import jakarta.persistence.Embedded;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.time.Year;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "veiculos",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_veiculos_placa", columnNames = {"placa"})
        }
)
public class Veiculo {

    @Id
    @UuidGenerator
    private UUID id;

    @Embedded
    private Placa placa;

    @Column(nullable = false, length = 80)
    private String marca;

    @Column(nullable = false, length = 120)
    private String modelo;

    @Column(nullable = false)
    private Integer ano;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    protected Veiculo() {
        // JPA
    }

    private Veiculo(Placa placa, String marca, String modelo, Integer ano, Cliente cliente) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.cliente = cliente;
    }

    public static Veiculo novo(Placa placa, String marca, String modelo, Integer ano, Cliente cliente) {
        Objects.requireNonNull(placa, "placa nao pode ser null");
        Objects.requireNonNull(cliente, "cliente nao pode ser null");

        String normalizedMarca = Strings.requireNonBlank(marca, "marca");
        String normalizedModelo = Strings.requireNonBlank(modelo, "modelo");

        if (ano == null) {
            throw new ValidationException("ano nao pode ser null");
        }
        int currentYear = Year.now().getValue();
        if (ano < 1900 || ano > currentYear + 1) {
            throw new ValidationException("ano invalido");
        }

        return new Veiculo(placa, normalizedMarca, normalizedModelo, ano, cliente);
    }

    public UUID getId() {
        return id;
    }

    public Placa getPlaca() {
        return placa;
    }

    public String getMarca() {
        return marca;
    }

    public String getModelo() {
        return modelo;
    }

    public Integer getAno() {
        return ano;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void atualizarDados(String marca, String modelo, Integer ano) {
        String normalizedMarca = Strings.requireNonBlank(marca, "marca");
        String normalizedModelo = Strings.requireNonBlank(modelo, "modelo");

        if (ano == null) {
            throw new ValidationException("ano nao pode ser null");
        }
        int currentYear = Year.now().getValue();
        if (ano < 1900 || ano > currentYear + 1) {
            throw new ValidationException("ano invalido");
        }

        this.marca = normalizedMarca;
        this.modelo = normalizedModelo;
        this.ano = ano;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Veiculo veiculo)) return false;
        return id != null && Objects.equals(id, veiculo.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
