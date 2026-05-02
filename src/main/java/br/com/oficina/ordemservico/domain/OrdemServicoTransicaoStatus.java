package br.com.oficina.ordemservico.domain;

import br.com.oficina.shared.domain.ValidationException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "os_transicoes_status")
public class OrdemServicoTransicaoStatus {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ordem_servico_id", nullable = false)
    private OrdemServico ordemServico;

    @Enumerated(EnumType.STRING)
    @Column(name = "de_status", length = 40)
    private StatusOrdemServico deStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "para_status", nullable = false, length = 40)
    private StatusOrdemServico paraStatus;

    @Column(name = "ocorrido_em", nullable = false)
    private OffsetDateTime ocorridoEm;

    protected OrdemServicoTransicaoStatus() {
        // JPA
    }

    private OrdemServicoTransicaoStatus(OrdemServico ordemServico, StatusOrdemServico deStatus, StatusOrdemServico paraStatus, OffsetDateTime ocorridoEm) {
        this.ordemServico = ordemServico;
        this.deStatus = deStatus;
        this.paraStatus = paraStatus;
        this.ocorridoEm = ocorridoEm;
    }

    public static OrdemServicoTransicaoStatus registrar(OrdemServico ordemServico, StatusOrdemServico de, StatusOrdemServico para, OffsetDateTime quando) {
        Objects.requireNonNull(ordemServico, "ordemServico nao pode ser null");
        if (para == null) {
            throw new ValidationException("paraStatus nao pode ser null");
        }
        if (quando == null) {
            throw new ValidationException("ocorridoEm nao pode ser null");
        }
        return new OrdemServicoTransicaoStatus(ordemServico, de, para, quando);
    }

    public UUID getId() {
        return id;
    }

    public StatusOrdemServico getDeStatus() {
        return deStatus;
    }

    public StatusOrdemServico getParaStatus() {
        return paraStatus;
    }

    public OffsetDateTime getOcorridoEm() {
        return ocorridoEm;
    }
}
