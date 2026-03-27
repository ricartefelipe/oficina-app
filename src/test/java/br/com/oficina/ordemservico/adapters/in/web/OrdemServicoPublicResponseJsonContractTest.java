package br.com.oficina.ordemservico.adapters.in.web;

import br.com.oficina.ordemservico.domain.StatusOrdemServico;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrdemServicoPublicResponseJsonContractTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @ParameterizedTest
    @EnumSource(StatusOrdemServico.class)
    void respostaJsonIncluiStatusComNomeDoEnum(StatusOrdemServico status) throws Exception {
        OffsetDateTime fixo = OffsetDateTime.parse("2024-06-01T10:00:00Z");
        var response = new PublicOrdemServicoController.OrdemServicoPublicResponse(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                "TRACKTST12",
                status,
                BigDecimal.ZERO.setScale(2),
                null,
                null,
                "Cliente",
                "52998224725",
                "ABC1D23",
                "Marca",
                "Modelo",
                2020,
                List.of(),
                List.of(),
                List.of(),
                fixo,
                fixo
        );
        String json = objectMapper.writeValueAsString(response);
        assertThat(json).contains("\"status\":\"" + status.name() + "\"");
    }
}
