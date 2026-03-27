package br.com.oficina.cadastros.cliente.api.admin;

import br.com.oficina.cadastros.cliente.application.ClienteService;
import br.com.oficina.cadastros.cliente.domain.Cliente;
import br.com.oficina.shared.validation.ValidCpfCnpj;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/admin/clientes")
@SecurityRequirement(name = "bearerAuth")
public class AdminClienteController {

    private final ClienteService clienteService;

    public AdminClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteCreateRequest request) {
        Cliente c = clienteService.criar(request.nome(), request.cpfCnpj());
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(c.getId())
                .toUri();
        return ResponseEntity.created(location).body(ClienteResponse.from(c));
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return clienteService.listar().stream().map(ClienteResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ClienteResponse obter(@PathVariable UUID id) {
        return ClienteResponse.from(clienteService.obter(id));
    }

    @PutMapping("/{id}")
    public ClienteResponse atualizar(@PathVariable UUID id, @Valid @RequestBody ClienteUpdateRequest request) {
        return ClienteResponse.from(clienteService.atualizar(id, request.nome()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remover(@PathVariable UUID id) {
        clienteService.remover(id);
        return ResponseEntity.noContent().build();
    }

    public record ClienteCreateRequest(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @ValidCpfCnpj String cpfCnpj
    ) {
    }

    public record ClienteUpdateRequest(
            @NotBlank @Size(max = 120) String nome
    ) {
    }

    public record ClienteResponse(
            UUID id,
            String nome,
            String cpfCnpj,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        public static ClienteResponse from(Cliente c) {
            return new ClienteResponse(
                    c.getId(),
                    c.getNome(),
                    c.getCpfCnpj().value(),
                    c.getCreatedAt(),
                    c.getUpdatedAt()
            );
        }
    }
}
