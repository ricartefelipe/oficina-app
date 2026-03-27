package br.com.oficina.ordemservico.adapters.in.web;

import br.com.oficina.cadastros.veiculo.domain.Veiculo;
import br.com.oficina.ordemservico.application.OrdemServicoService;
import br.com.oficina.ordemservico.domain.DecisaoRespostaOrcamentoExterna;
import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.domain.OrdemServicoItemPeca;
import br.com.oficina.ordemservico.domain.OrdemServicoItemServico;
import br.com.oficina.ordemservico.domain.OrdemServicoTransicaoStatus;
import br.com.oficina.ordemservico.domain.StatusOrdemServico;
import br.com.oficina.shared.validation.ValidCpfCnpj;
import br.com.oficina.shared.validation.ValidPlaca;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.springframework.format.annotation.DateTimeFormat.ISO;

@RestController
@RequestMapping("/admin/ordens-servico")
@SecurityRequirement(name = "bearerAuth")
public class AdminOrdemServicoController {

    private final OrdemServicoService osService;

    public AdminOrdemServicoController(OrdemServicoService osService) {
        this.osService = osService;
    }

    @PostMapping
    public ResponseEntity<OrdemServicoDetalheResponse> criar(@Valid @RequestBody CriarOrdemServicoRequest request) {
        OrdemServico os = osService.criarOrdemServico(
                request.cliente().nome(),
                request.cliente().cpfCnpj(),
                request.veiculo().placa(),
                request.veiculo().marca(),
                request.veiculo().modelo(),
                request.veiculo().ano(),
                request.servicos().stream().map(i -> new OrdemServicoService.ItemServico(i.servicoId(), i.quantidade())).toList(),
                request.pecas() == null ? List.of() : request.pecas().stream().map(i -> new OrdemServicoService.ItemPeca(i.pecaId(), i.quantidade())).toList()
        );

        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(os.getId())
                .toUri();

        return ResponseEntity.created(location).body(OrdemServicoDetalheResponse.from(os));
    }

    /**
     * Listagem administrativa: por defeito exclui {@code FINALIZADA}, {@code ENTREGUE} e {@code CANCELADA},
     * ordenando por prioridade operacional (execução, depois aguardando aprovação, diagnóstico, recebida)
     * e, dentro do mesmo status, pela data de criação mais antiga primeiro.
     * Use {@code incluirEncerradas=true} para listar todas (ordenadas pela criação mais recente primeiro).
     */
    @GetMapping
    public List<OrdemServicoResumoResponse> listar(
            @RequestParam(required = false) StatusOrdemServico status,
            @RequestParam(required = false) String placa,
            @RequestParam(required = false) String cpfCnpj,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = ISO.DATE_TIME) OffsetDateTime to,
            @RequestParam(required = false, defaultValue = "false") boolean incluirEncerradas
    ) {
        return osService.listar(status, placa, cpfCnpj, from, to, incluirEncerradas).stream()
                .map(OrdemServicoResumoResponse::from)
                .toList();
    }

    @GetMapping("/{id}")
    public OrdemServicoDetalheResponse obter(@PathVariable UUID id) {
        return OrdemServicoDetalheResponse.from(osService.obterDetalhe(id));
    }

    @PostMapping("/{id}/diagnostico/iniciar")
    public OrdemServicoDetalheResponse iniciarDiagnostico(@PathVariable UUID id) {
        return OrdemServicoDetalheResponse.from(osService.iniciarDiagnostico(id));
    }

    @PostMapping("/{id}/orcamento/enviar")
    public OrdemServicoDetalheResponse enviarOrcamento(@PathVariable UUID id) {
        return OrdemServicoDetalheResponse.from(osService.enviarOrcamento(id));
    }

    /**
     * Resposta de sistema externo (ex.: canal de notificação) sobre o orçamento.
     * Idempotência via cabeçalho {@code Idempotency-Key} (ex.: UUID da mensagem do parceiro).
     */
    @PostMapping("/{id}/orcamento/resposta-externa")
    public OrdemServicoDetalheResponse respostaOrcamentoExterna(
            @PathVariable UUID id,
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody RespostaOrcamentoExternaRequest body
    ) {
        return OrdemServicoDetalheResponse.from(
                osService.processarRespostaOrcamentoExterna(id, idempotencyKey, body.decisao())
        );
    }

    @PostMapping("/{id}/execucao/finalizar")
    public OrdemServicoDetalheResponse finalizarExecucao(@PathVariable UUID id) {
        return OrdemServicoDetalheResponse.from(osService.finalizarExecucao(id));
    }

    @PostMapping("/{id}/entrega/registrar")
    public OrdemServicoDetalheResponse registrarEntrega(@PathVariable UUID id) {
        return OrdemServicoDetalheResponse.from(osService.registrarEntrega(id));
    }

    public record RespostaOrcamentoExternaRequest(
            @NotNull DecisaoRespostaOrcamentoExterna decisao
    ) {
    }

    public record CriarOrdemServicoRequest(
            @Valid @NotNull ClienteInput cliente,
            @Valid @NotNull VeiculoInput veiculo,
            @NotEmpty List<@Valid ItemServicoInput> servicos,
            List<@Valid ItemPecaInput> pecas
    ) {
    }

    public record ClienteInput(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @ValidCpfCnpj String cpfCnpj
    ) {
    }

    public record VeiculoInput(
            @NotBlank @ValidPlaca String placa,
            @NotBlank @Size(max = 80) String marca,
            @NotBlank @Size(max = 120) String modelo,
            @NotNull Integer ano
    ) {
    }

    public record ItemServicoInput(
            @NotNull UUID servicoId,
            @NotNull @Min(1) Integer quantidade
    ) {
    }

    public record ItemPecaInput(
            @NotNull UUID pecaId,
            @NotNull @Min(1) Integer quantidade
    ) {
    }

    public record OrdemServicoResumoResponse(
            UUID id,
            String trackingCode,
            StatusOrdemServico status,
            BigDecimal orcamentoTotal,
            UUID clienteId,
            String clienteNome,
            String clienteCpfCnpj,
            UUID veiculoId,
            String placa,
            String marca,
            String modelo,
            Integer ano,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        public static OrdemServicoResumoResponse from(OrdemServico os) {
            return new OrdemServicoResumoResponse(
                    os.getId(),
                    os.getTrackingCode(),
                    os.getStatus(),
                    os.getOrcamentoTotal(),
                    os.getCliente().getId(),
                    os.getCliente().getNome(),
                    os.getCliente().getCpfCnpj().value(),
                    os.getVeiculo().getId(),
                    os.getVeiculo().getPlaca().value(),
                    os.getVeiculo().getMarca(),
                    os.getVeiculo().getModelo(),
                    os.getVeiculo().getAno(),
                    os.getCreatedAt(),
                    os.getUpdatedAt()
            );
        }
    }

    public record OrdemServicoDetalheResponse(
            UUID id,
            String trackingCode,
            StatusOrdemServico status,
            BigDecimal orcamentoTotal,
            OffsetDateTime orcamentoEnviadoAt,
            OffsetDateTime aprovadoAt,
            ClienteDetalheResponse cliente,
            VeiculoDetalheResponse veiculo,
            List<ItemServicoResponse> itensServico,
            List<ItemPecaResponse> itensPeca,
            List<TransicaoStatusResponse> historicoStatus,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        public static OrdemServicoDetalheResponse from(OrdemServico os) {
            return new OrdemServicoDetalheResponse(
                    os.getId(),
                    os.getTrackingCode(),
                    os.getStatus(),
                    os.getOrcamentoTotal(),
                    os.getOrcamentoEnviadoAt(),
                    os.getAprovadoAt(),
                    ClienteDetalheResponse.from(os.getCliente()),
                    VeiculoDetalheResponse.from(os.getVeiculo()),
                    os.getItensServico().stream().map(ItemServicoResponse::from).toList(),
                    os.getItensPeca().stream().map(ItemPecaResponse::from).toList(),
                    os.getTransicoesStatus().stream().map(TransicaoStatusResponse::from).toList(),
                    os.getCreatedAt(),
                    os.getUpdatedAt()
            );
        }
    }

    public record ClienteDetalheResponse(
            UUID id,
            String nome,
            String cpfCnpj
    ) {
        public static ClienteDetalheResponse from(br.com.oficina.cadastros.cliente.domain.Cliente c) {
            return new ClienteDetalheResponse(c.getId(), c.getNome(), c.getCpfCnpj().value());
        }
    }

    public record VeiculoDetalheResponse(
            UUID id,
            String placa,
            String marca,
            String modelo,
            Integer ano
    ) {
        public static VeiculoDetalheResponse from(Veiculo v) {
            return new VeiculoDetalheResponse(v.getId(), v.getPlaca().value(), v.getMarca(), v.getModelo(), v.getAno());
        }
    }

    public record ItemServicoResponse(
            UUID id,
            UUID servicoId,
            String servicoNome,
            Integer quantidade,
            BigDecimal precoUnitario,
            Integer tempoEstimadoMin,
            BigDecimal subtotal
    ) {
        public static ItemServicoResponse from(OrdemServicoItemServico i) {
            return new ItemServicoResponse(
                    i.getId(),
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
            UUID id,
            UUID pecaId,
            String pecaNome,
            Integer quantidade,
            BigDecimal precoUnitario,
            BigDecimal subtotal
    ) {
        public static ItemPecaResponse from(OrdemServicoItemPeca i) {
            return new ItemPecaResponse(
                    i.getId(),
                    i.getPeca().getId(),
                    i.getPeca().getNome(),
                    i.getQuantidade(),
                    i.getPrecoUnitario(),
                    i.getSubtotal()
            );
        }
    }

    public record TransicaoStatusResponse(
            UUID id,
            StatusOrdemServico deStatus,
            StatusOrdemServico paraStatus,
            OffsetDateTime ocorridoEm
    ) {
        public static TransicaoStatusResponse from(OrdemServicoTransicaoStatus t) {
            return new TransicaoStatusResponse(
                    t.getId(),
                    t.getDeStatus(),
                    t.getParaStatus(),
                    t.getOcorridoEm()
            );
        }
    }
}
