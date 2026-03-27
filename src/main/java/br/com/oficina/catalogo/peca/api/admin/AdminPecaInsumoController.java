package br.com.oficina.catalogo.peca.api.admin;

import br.com.oficina.catalogo.peca.application.PecaInsumoService;
import br.com.oficina.catalogo.peca.domain.PecaInsumo;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/pecas")
@SecurityRequirement(name = "bearerAuth")
public class AdminPecaInsumoController {

    private final PecaInsumoService service;

    public AdminPecaInsumoController(PecaInsumoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PecaResponse> criar(@Valid @RequestBody PecaCreateRequest request) {
        PecaInsumo p = service.criar(request.nome(), request.descricao(), request.preco(), request.estoqueAtual(), request.estoqueMinimo());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(p.getId())
                .toUri();
        return ResponseEntity.created(location).body(PecaResponse.from(p));
    }

    @GetMapping
    public List<PecaResponse> listar() {
        return service.listar().stream().map(PecaResponse::from).toList();
    }

    @GetMapping("/{id}")
    public PecaResponse obter(@PathVariable UUID id) {
        return PecaResponse.from(service.obter(id));
    }

    @PutMapping("/{id}")
    public PecaResponse atualizar(@PathVariable UUID id, @Valid @RequestBody PecaUpdateRequest request) {
        PecaInsumo p = service.atualizar(id, request.nome(), request.descricao(), request.preco(), request.estoqueAtual(), request.estoqueMinimo());
        return PecaResponse.from(p);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    public record PecaCreateRequest(
            @NotBlank @Size(max = 140) String nome,
            @Size(max = 500) String descricao,
            @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal preco,
            @NotNull @Min(0) Integer estoqueAtual,
            @NotNull @Min(0) Integer estoqueMinimo
    ) {
    }

    public record PecaUpdateRequest(
            @NotBlank @Size(max = 140) String nome,
            @Size(max = 500) String descricao,
            @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal preco,
            @NotNull @Min(0) Integer estoqueAtual,
            @NotNull @Min(0) Integer estoqueMinimo
    ) {
    }

    public record PecaResponse(
            UUID id,
            String nome,
            String descricao,
            BigDecimal preco,
            Integer estoqueAtual,
            Integer estoqueMinimo,
            boolean abaixoDoMinimo,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        public static PecaResponse from(PecaInsumo p) {
            return new PecaResponse(
                    p.getId(),
                    p.getNome(),
                    p.getDescricao(),
                    p.getPreco(),
                    p.getEstoqueAtual(),
                    p.getEstoqueMinimo(),
                    p.abaixoDoMinimo(),
                    p.getCreatedAt(),
                    p.getUpdatedAt()
            );
        }
    }
}
