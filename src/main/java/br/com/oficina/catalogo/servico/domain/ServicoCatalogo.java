package br.com.oficina.catalogo.servico.domain;

import br.com.oficina.shared.domain.Strings;
import br.com.oficina.shared.domain.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "servicos_catalogo")
public class ServicoCatalogo {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer tempoEstimadoMin;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    protected ServicoCatalogo() {
        // JPA
    }

    private ServicoCatalogo(String nome, String descricao, BigDecimal preco, Integer tempoEstimadoMin) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.tempoEstimadoMin = tempoEstimadoMin;
    }

    public static ServicoCatalogo novo(String nome, String descricao, BigDecimal preco, Integer tempoEstimadoMin) {
        String normalizedNome = Strings.requireNonBlank(nome, "nome");

        if (preco == null || preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("preco deve ser >= 0");
        }
        BigDecimal normalizedPreco = preco.setScale(2, RoundingMode.HALF_UP);

        if (tempoEstimadoMin == null || tempoEstimadoMin <= 0) {
            throw new ValidationException("tempoEstimadoMin deve ser > 0");
        }

        String normalizedDescricao = (descricao == null || descricao.trim().isBlank()) ? null : descricao.trim();

        return new ServicoCatalogo(normalizedNome, normalizedDescricao, normalizedPreco, tempoEstimadoMin);
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public Integer getTempoEstimadoMin() {
        return tempoEstimadoMin;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void atualizar(String nome, String descricao, BigDecimal preco, Integer tempoEstimadoMin) {
        String normalizedNome = Strings.requireNonBlank(nome, "nome");
        String normalizedDescricao = (descricao == null || descricao.trim().isBlank()) ? null : descricao.trim();

        if (preco == null || preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("preco deve ser >= 0");
        }
        BigDecimal normalizedPreco = preco.setScale(2, RoundingMode.HALF_UP);

        if (tempoEstimadoMin == null || tempoEstimadoMin <= 0) {
            throw new ValidationException("tempoEstimadoMin deve ser > 0");
        }

        this.nome = normalizedNome;
        this.descricao = normalizedDescricao;
        this.preco = normalizedPreco;
        this.tempoEstimadoMin = tempoEstimadoMin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServicoCatalogo that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
