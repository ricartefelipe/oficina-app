package br.com.oficina.ordemservico.adapters.in.web;

import br.com.oficina.ordemservico.application.MetricasService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@RestController
@RequestMapping("/admin/metricas")
@SecurityRequirement(name = "bearerAuth")
public class AdminMetricasController {

    private final MetricasService metricasService;

    public AdminMetricasController(MetricasService metricasService) {
        this.metricasService = metricasService;
    }

    @GetMapping("/tempo-medio-execucao")
    public MetricasService.TempoMedioExecucao tempoMedioExecucao(
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime to
    ) {
        return metricasService.calcularTempoMedioExecucao(from, to);
    }
}
