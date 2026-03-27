package br.com.oficina.cadastros.veiculo.api.admin;

import br.com.oficina.cadastros.veiculo.application.VeiculoService;
import br.com.oficina.cadastros.veiculo.domain.Veiculo;
import br.com.oficina.shared.validation.ValidPlaca;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.OffsetDateTime;
import java.time.Year;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/veiculos")
@SecurityRequirement(name = "bearerAuth")
public class AdminVeiculoController {

    private final VeiculoService veiculoService;

    public AdminVeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @PostMapping
    public ResponseEntity<VeiculoResponse> criar(@Valid @RequestBody VeiculoCreateRequest request) {
        Veiculo v = veiculoService.criar(request.clienteId(), request.placa(), request.marca(), request.modelo(), request.ano());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(v.getId())
                .toUri();
        return ResponseEntity.created(location).body(VeiculoResponse.from(v));
    }

    @GetMapping
    public List<VeiculoResponse> listar() {
        return veiculoService.listar().stream().map(VeiculoResponse::from).toList();
    }

    @GetMapping("/{id}")
    public VeiculoResponse obter(@PathVariable UUID id) {
        return VeiculoResponse.from(veiculoService.obter(id));
    }

    @PutMapping("/{id}")
    public VeiculoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody VeiculoUpdateRequest request) {
        return VeiculoResponse.from(veiculoService.atualizar(id, request.marca(), request.modelo(), request.ano()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        veiculoService.remover(id);
        return ResponseEntity.noContent().build();
    }

    public record VeiculoCreateRequest(
            @NotNull UUID clienteId,
            @NotBlank @ValidPlaca String placa,
            @NotBlank @Size(max = 80) String marca,
            @NotBlank @Size(max = 120) String modelo,
            @NotNull @Min(1900) @Max(3000) Integer ano
    ) {
        public VeiculoCreateRequest {
            int currentYearPlus = Year.now().getValue() + 1;
            if (ano != null && ano > currentYearPlus) {
                throw new IllegalArgumentException("ano deve ser <= " + currentYearPlus);
            }
        }
    }

    public record VeiculoUpdateRequest(
            @NotBlank @Size(max = 80) String marca,
            @NotBlank @Size(max = 120) String modelo,
            @NotNull @Min(1900) @Max(3000) Integer ano
    ) {
        public VeiculoUpdateRequest {
            int currentYearPlus = Year.now().getValue() + 1;
            if (ano != null && ano > currentYearPlus) {
                throw new IllegalArgumentException("ano deve ser <= " + currentYearPlus);
            }
        }
    }

    public record VeiculoResponse(
            UUID id,
            String placa,
            String marca,
            String modelo,
            Integer ano,
            UUID clienteId,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        public static VeiculoResponse from(Veiculo v) {
            return new VeiculoResponse(
                    v.getId(),
                    v.getPlaca().value(),
                    v.getMarca(),
                    v.getModelo(),
                    v.getAno(),
                    v.getCliente().getId(),
                    v.getCreatedAt(),
                    v.getUpdatedAt()
            );
        }
    }
}
