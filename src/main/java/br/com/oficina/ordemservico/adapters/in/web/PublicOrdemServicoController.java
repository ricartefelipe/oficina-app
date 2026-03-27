package br.com.oficina.ordemservico.adapters.in.web;

import br.com.oficina.ordemservico.application.OrdemServicoService;
import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.domain.OrdemServicoItemPeca;
import br.com.oficina.ordemservico.domain.OrdemServicoItemServico;
import br.com.oficina.ordemservico.domain.OrdemServicoTransicaoStatus;
import br.com.oficina.ordemservico.domain.StatusOrdemServico;
import br.com.oficina.shared.validation.ValidCpfCnpj;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/public/ordens-servico")
public class PublicOrdemServicoController {

    private final OrdemServicoService osService;

    public PublicOrdemServicoController(OrdemServicoService osService) {
        this.osService = osService;
    }

    @GetMapping("/{trackingCode}")
    public OrdemServicoPublicResponse consultar(@PathVariable String trackingCode) {
        return OrdemServicoPublicResponse.from(osService.obterPorTrackingCode(trackingCode));
    }

    @PostMapping("/{trackingCode}/aprovar")
    public OrdemServicoPublicResponse aprovar(@PathVariable String trackingCode, @Valid @RequestBody AprovarOrcamentoRequest request) {
        return OrdemServicoPublicResponse.from(osService.aprovarOrcamentoPublico(trackingCode, request.cpfCnpj()));
    }

    public record AprovarOrcamentoRequest(
            @NotBlank @ValidCpfCnpj String cpfCnpj
    ) {
    }

    public record OrdemServicoPublicResponse(
            UUID id,
            String trackingCode,
            StatusOrdemServico status,
            BigDecimal orcamentoTotal,
            OffsetDateTime orcamentoEnviadoAt,
            OffsetDateTime aprovadoAt,
            String clienteNome,
            String clienteCpfCnpj,
            String placa,
            String marca,
            String modelo,
            Integer ano,
            List<ItemServicoResponse> itensServico,
            List<ItemPecaResponse> itensPeca,
            List<TransicaoStatusResponse> historicoStatus,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        public static OrdemServicoPublicResponse from(OrdemServico os) {
            return new OrdemServicoPublicResponse(
                    os.getId(),
                    os.getTrackingCode(),
                    os.getStatus(),
                    os.getOrcamentoTotal(),
                    os.getOrcamentoEnviadoAt(),
                    os.getAprovadoAt(),
                    os.getCliente().getNome(),
                    os.getCliente().getCpfCnpj().value(),
                    os.getVeiculo().getPlaca().value(),
                    os.getVeiculo().getMarca(),
                    os.getVeiculo().getModelo(),
                    os.getVeiculo().getAno(),
                    os.getItensServico().stream().map(ItemServicoResponse::from).toList(),
                    os.getItensPeca().stream().map(ItemPecaResponse::from).toList(),
                    os.getTransicoesStatus().stream().map(TransicaoStatusResponse::from).toList(),
                    os.getCreatedAt(),
                    os.getUpdatedAt()
            );
        }
    }

    public record ItemServicoResponse(
            UUID servicoId,
            String servicoNome,
            Integer quantidade,
            BigDecimal precoUnitario,
            Integer tempoEstimadoMin,
            BigDecimal subtotal
    ) {
        public static ItemServicoResponse from(OrdemServicoItemServico i) {
            return new ItemServicoResponse(
                    i.getServico().getId(),
                    i.getServico().getNome(),
                    i.getQuantidade(),
                    i.getPrecoUnitario(),
                    i.getTempoEstimadoMin(),
                    i.getSubtotal()
            );
        }
    }

    public record ItemPecaResponse(
            UUID pecaId,
            String pecaNome,
            Integer quantidade,
            BigDecimal precoUnitario,
            BigDecimal subtotal
    ) {
        public static ItemPecaResponse from(OrdemServicoItemPeca i) {
            return new ItemPecaResponse(
                    i.getPeca().getId(),
                    i.getPeca().getNome(),
                    i.getQuantidade(),
                    i.getPrecoUnitario(),
                    i.getSubtotal()
            );
        }
    }

    public record TransicaoStatusResponse(
            StatusOrdemServico deStatus,
            StatusOrdemServico paraStatus,
            OffsetDateTime ocorridoEm
    ) {
        public static TransicaoStatusResponse from(OrdemServicoTransicaoStatus t) {
            return new TransicaoStatusResponse(t.getDeStatus(), t.getParaStatus(), t.getOcorridoEm());
        }
    }
}
