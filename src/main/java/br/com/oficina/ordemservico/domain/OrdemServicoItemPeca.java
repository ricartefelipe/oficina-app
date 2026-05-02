package br.com.oficina.ordemservico.domain;

import br.com.oficina.catalogo.peca.domain.PecaInsumo;
import br.com.oficina.shared.domain.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "os_itens_peca")
public class OrdemServicoItemPeca {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "peca_id", nullable = false)
    private PecaInsumo peca;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    protected OrdemServicoItemPeca() {
        // JPA
    }

    private OrdemServicoItemPeca(OrdemServico ordemServico, PecaInsumo peca, Integer quantidade, BigDecimal precoUnitario) {
        this.ordemServico = ordemServico;
        this.peca = peca;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade)).setScale(2, RoundingMode.HALF_UP);
    }

    public static OrdemServicoItemPeca criar(OrdemServico ordemServico, PecaInsumo peca, int quantidade) {
        Objects.requireNonNull(ordemServico, "ordemServico nao pode ser null");
        Objects.requireNonNull(peca, "peca nao pode ser null");
        if (quantidade <= 0) {
            throw new ValidationException("quantidade deve ser > 0");
        }

        BigDecimal precoUnitario = peca.getPreco();
        if (precoUnitario == null) {
            throw new ValidationException("Peca/Insumo sem preco definido");
        }

        return new OrdemServicoItemPeca(ordemServico, peca, quantidade, precoUnitario);
    }

    public UUID getId() {
        return id;
    }

    public PecaInsumo getPeca() {
        return peca;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
