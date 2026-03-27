package br.com.oficina.ordemservico.adapters.in.web;

import br.com.oficina.ordemservico.application.MetricasService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AdminMetricasControllerHttpTest {

    private MockMvc mockMvc;
    private MetricasService metricasService;

    @BeforeEach
    void setUp() {
        metricasService = mock(MetricasService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AdminMetricasController(metricasService)).build();
    }

    @Test
    void tempoMedio_semParametros_retorna200() throws Exception {
        when(metricasService.calcularTempoMedioExecucao(isNull(), isNull()))
                .thenReturn(new MetricasService.TempoMedioExecucao(
                        null, null, 2L, new BigDecimal("15.50"), "PT15M30S"));

        mockMvc.perform(get("/admin/metricas/tempo-medio-execucao").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countConsidered").value(2))
                .andExpect(jsonPath("$.averageMinutes").value(15.5));
    }
}
