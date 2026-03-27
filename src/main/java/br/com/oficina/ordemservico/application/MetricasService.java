package br.com.oficina.ordemservico.application;

import br.com.oficina.ordemservico.application.port.OrdemServicoPersistencePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class MetricasService {

    private final OrdemServicoPersistencePort ordemServicoPersistence;

    public MetricasService(OrdemServicoPersistencePort ordemServicoPersistence) {
        this.ordemServicoPersistence = ordemServicoPersistence;
    }

    @Transactional(readOnly = true)
    public TempoMedioExecucao calcularTempoMedioExecucao(OffsetDateTime from, OffsetDateTime to) {
        OffsetDateTime fromEff = from != null ? from : OffsetDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        OffsetDateTime toEff = to != null ? to : OffsetDateTime.of(9999, 12, 31, 23, 59, 59, 999_999_999, ZoneOffset.UTC);

        Double avgSeconds = ordemServicoPersistence.avgExecutionSeconds(fromEff, toEff);
        Long count = ordemServicoPersistence.countExecutionMeasured(fromEff, toEff);

        if (avgSeconds == null || count == null || count == 0) {
            return new TempoMedioExecucao(from, to, 0L, BigDecimal.ZERO, "PT0S");
        }

        BigDecimal avgMinutes = BigDecimal.valueOf(avgSeconds / 60.0)
                .setScale(2, RoundingMode.HALF_UP);

        Duration d = Duration.ofSeconds(Math.round(avgSeconds));
        return new TempoMedioExecucao(from, to, count, avgMinutes, d.toString());
    }

    public record TempoMedioExecucao(
            OffsetDateTime from,
            OffsetDateTime to,
            Long countConsidered,
            BigDecimal averageMinutes,
            String averageDurationIso
    ) {
    }
}
