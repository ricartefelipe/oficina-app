package br.com.oficina.catalogo.peca.domain;

import br.com.oficina.shared.domain.BusinessRuleException;
import br.com.oficina.shared.domain.Strings;
import br.com.oficina.shared.domain.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "pecas_insumos")
public class PecaInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 140)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private Integer estoqueAtual;

    @Column(nullable = false)
    private Integer estoqueMinimo;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    protected PecaInsumo() {
        // JPA
    }

    private PecaInsumo(String nome, String descricao, BigDecimal preco, Integer estoqueAtual, Integer estoqueMinimo) {
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.estoqueAtual = estoqueAtual;
        this.estoqueMinimo = estoqueMinimo;
    }

    public static PecaInsumo nova(String nome, String descricao, BigDecimal preco, Integer estoqueAtual, Integer estoqueMinimo) {
        String normalizedNome = Strings.requireNonBlank(nome, "nome");
        String normalizedDescricao = (descricao == null || descricao.trim().isBlank()) ? null : descricao.trim();

        if (preco == null || preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("preco deve ser >= 0");
        }
        BigDecimal normalizedPreco = preco.setScale(2, RoundingMode.HALF_UP);

        if (estoqueAtual == null || estoqueAtual < 0) {
            throw new ValidationException("estoqueAtual deve ser >= 0");
        }
        if (estoqueMinimo == null || estoqueMinimo < 0) {
            throw new ValidationException("estoqueMinimo deve ser >= 0");
        }

        return new PecaInsumo(normalizedNome, normalizedDescricao, normalizedPreco, estoqueAtual, estoqueMinimo);
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

    public Integer getEstoqueAtual() {
        return estoqueAtual;
    }

    public Integer getEstoqueMinimo() {
        return estoqueMinimo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void atualizar(String nome, String descricao, BigDecimal preco, Integer estoqueAtual, Integer estoqueMinimo) {
        String normalizedNome = Strings.requireNonBlank(nome, "nome");
        String normalizedDescricao = (descricao == null || descricao.trim().isBlank()) ? null : descricao.trim();

        if (preco == null || preco.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("preco deve ser >= 0");
        }
        BigDecimal normalizedPreco = preco.setScale(2, RoundingMode.HALF_UP);

        if (estoqueAtual == null || estoqueAtual < 0) {
            throw new ValidationException("estoqueAtual deve ser >= 0");
        }
        if (estoqueMinimo == null || estoqueMinimo < 0) {
            throw new ValidationException("estoqueMinimo deve ser >= 0");
        }

        this.nome = normalizedNome;
        this.descricao = normalizedDescricao;
        this.preco = normalizedPreco;
        this.estoqueAtual = estoqueAtual;
        this.estoqueMinimo = estoqueMinimo;
    }

    public void decrementarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new ValidationException("quantidade deve ser > 0");
        }
        if (estoqueAtual < quantidade) {
            throw new BusinessRuleException("Estoque insuficiente para peca/insumo: " + nome);
        }
        estoqueAtual = estoqueAtual - quantidade;
    }

    public void incrementarEstoque(int quantidade) {
        if (quantidade <= 0) {
            throw new ValidationException("quantidade deve ser > 0");
        }
        estoqueAtual = estoqueAtual + quantidade;
    }

    public boolean abaixoDoMinimo() {
        return estoqueAtual != null && estoqueMinimo != null && estoqueAtual < estoqueMinimo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PecaInsumo that)) return false;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
