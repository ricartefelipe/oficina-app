package br.com.oficina.ordemservico.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "os_orcamento_resposta_idempotencia")
public class OsOrcamentoRespostaIdempotencia {

    @Id
    private UUID id;

    @Column(name = "idempotency_key", nullable = false, length = 128, unique = true)
    private String idempotencyKey;

    @Column(name = "ordem_servico_id", nullable = false)
    private UUID ordemServicoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DecisaoRespostaOrcamentoExterna decisao;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected OsOrcamentoRespostaIdempotencia() {
    }

    public OsOrcamentoRespostaIdempotencia(UUID id, String idempotencyKey, UUID ordemServicoId, DecisaoRespostaOrcamentoExterna decisao) {
        this.id = id;
        this.idempotencyKey = idempotencyKey;
        this.ordemServicoId = ordemServicoId;
        this.decisao = decisao;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public UUID getOrdemServicoId() {
        return ordemServicoId;
    }

    public DecisaoRespostaOrcamentoExterna getDecisao() {
        return decisao;
    }
}
