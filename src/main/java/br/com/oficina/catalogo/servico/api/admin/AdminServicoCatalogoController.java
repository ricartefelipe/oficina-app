package br.com.oficina.catalogo.servico.api.admin;

import br.com.oficina.catalogo.servico.application.ServicoCatalogoService;
import br.com.oficina.catalogo.servico.domain.ServicoCatalogo;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
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
@RequestMapping("/admin/servicos")
@SecurityRequirement(name = "bearerAuth")
public class AdminServicoCatalogoController {

    private final ServicoCatalogoService service;

    public AdminServicoCatalogoController(ServicoCatalogoService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ServicoResponse> criar(@Valid @RequestBody ServicoCreateRequest request) {
        ServicoCatalogo s = service.criar(request.nome(), request.descricao(), request.preco(), request.tempoEstimadoMin());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(s.getId())
                .toUri();
        return ResponseEntity.created(location).body(ServicoResponse.from(s));
    }

    @GetMapping
    public List<ServicoResponse> listar() {
        return service.listar().stream().map(ServicoResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ServicoResponse obter(@PathVariable UUID id) {
        return ServicoResponse.from(service.obter(id));
    }

    @PutMapping("/{id}")
    public ServicoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ServicoUpdateRequest request) {
        ServicoCatalogo s = service.atualizar(id, request.nome(), request.descricao(), request.preco(), request.tempoEstimadoMin());
        return ServicoResponse.from(s);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        service.remover(id);
        return ResponseEntity.noContent().build();
    }

    public record ServicoCreateRequest(
            @NotBlank @Size(max = 120) String nome,
            @Size(max = 500) String descricao,
            @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal preco,
            @NotNull Integer tempoEstimadoMin
    ) {
    }

    public record ServicoUpdateRequest(
            @NotBlank @Size(max = 120) String nome,
            @Size(max = 500) String descricao,
            @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal preco,
            @NotNull Integer tempoEstimadoMin
    ) {
    }

    public record ServicoResponse(
            UUID id,
            String nome,
            String descricao,
            BigDecimal preco,
            Integer tempoEstimadoMin,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        public static ServicoResponse from(ServicoCatalogo s) {
            return new ServicoResponse(
                    s.getId(),
                    s.getNome(),
                    s.getDescricao(),
                    s.getPreco(),
                    s.getTempoEstimadoMin(),
                    s.getCreatedAt(),
                    s.getUpdatedAt()
            );
        }
    }
}
