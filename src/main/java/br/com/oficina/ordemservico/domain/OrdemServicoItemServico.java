package br.com.oficina.ordemservico.domain;

import br.com.oficina.catalogo.servico.domain.ServicoCatalogo;
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
@Table(name = "os_itens_servico")
public class OrdemServicoItemServico {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "servico_id", nullable = false)
    private ServicoCatalogo servico;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "tempo_estimado_min", nullable = false)
    private Integer tempoEstimadoMin;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    protected OrdemServicoItemServico() {
        // JPA
    }

    private OrdemServicoItemServico(OrdemServico ordemServico, ServicoCatalogo servico, Integer quantidade, BigDecimal precoUnitario, Integer tempoEstimadoMin) {
        this.ordemServico = ordemServico;
        this.servico = servico;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
        this.tempoEstimadoMin = tempoEstimadoMin;
        this.subtotal = precoUnitario.multiply(BigDecimal.valueOf(quantidade)).setScale(2, RoundingMode.HALF_UP);
    }

    public static OrdemServicoItemServico criar(OrdemServico ordemServico, ServicoCatalogo servico, int quantidade) {
        Objects.requireNonNull(ordemServico, "ordemServico nao pode ser null");
        Objects.requireNonNull(servico, "servico nao pode ser null");
        if (quantidade <= 0) {
            throw new ValidationException("quantidade deve ser > 0");
        }
        BigDecimal precoUnitario = servico.getPreco();
        if (precoUnitario == null) {
            throw new ValidationException("Servico sem preco definido");
        }
        Integer tempoEstimadoMin = servico.getTempoEstimadoMin();
        if (tempoEstimadoMin == null || tempoEstimadoMin <= 0) {
            throw new ValidationException("Servico sem tempo estimado valido");
        }
        return new OrdemServicoItemServico(ordemServico, servico, quantidade, precoUnitario, tempoEstimadoMin);
    }

    public UUID getId() {
        return id;
    }

    public ServicoCatalogo getServico() {
        return servico;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public BigDecimal getPrecoUnitario() {
        return precoUnitario;
    }

    public Integer getTempoEstimadoMin() {
        return tempoEstimadoMin;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
