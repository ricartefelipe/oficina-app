package br.com.oficina.cadastros.veiculo.application;

import br.com.oficina.cadastros.cliente.application.ClienteService;
import br.com.oficina.cadastros.cliente.domain.Cliente;
import br.com.oficina.cadastros.veiculo.domain.Placa;
import br.com.oficina.cadastros.veiculo.domain.Veiculo;
import br.com.oficina.cadastros.veiculo.infra.persistence.VeiculoJpaRepository;
import br.com.oficina.shared.domain.BusinessRuleException;
import br.com.oficina.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VeiculoService {

    private final VeiculoJpaRepository veiculoRepository;
    private final ClienteService clienteService;

    public VeiculoService(VeiculoJpaRepository veiculoRepository, ClienteService clienteService) {
        this.veiculoRepository = veiculoRepository;
        this.clienteService = clienteService;
    }

    @Transactional
    public Veiculo criar(UUID clienteId, String placaRaw, String marca, String modelo, Integer ano) {
        Cliente cliente = clienteService.obter(clienteId);
        Veiculo v = Veiculo.novo(Placa.of(placaRaw), marca, modelo, ano, cliente);
        return veiculoRepository.save(v);
    }

    @Transactional(readOnly = true)
    public Veiculo obter(UUID id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Veiculo nao encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Veiculo> listar() {
        return veiculoRepository.findAll();
    }

    @Transactional
    public Veiculo atualizar(UUID id, String marca, String modelo, Integer ano) {
        Veiculo v = obter(id);
        v.atualizarDados(marca, modelo, ano);
        return veiculoRepository.save(v);
    }

    @Transactional
    public void remover(UUID id) {
        Veiculo v = obter(id);
        veiculoRepository.delete(v);
    }

    @Transactional(readOnly = true)
    public Optional<Veiculo> buscarPorPlaca(String placaRaw) {
        String normalized = br.com.oficina.shared.domain.Strings.alnumUpper(placaRaw);
        return veiculoRepository.findByPlaca_Value(normalized);
    }

    @Transactional
    public Veiculo obterOuCriarPorPlaca(Cliente cliente, String placaRaw, String marca, String modelo, Integer ano) {
        Placa placa = Placa.of(placaRaw);
        Optional<Veiculo> existing = veiculoRepository.findByPlaca_Value(placa.value());
        if (existing.isPresent()) {
            Veiculo v = existing.get();
            if (!v.getCliente().getId().equals(cliente.getId())) {
                throw new BusinessRuleException("Placa ja cadastrada para outro cliente");
            }
            return v;
        }
        Veiculo novo = Veiculo.novo(placa, marca, modelo, ano, cliente);
        return veiculoRepository.save(novo);
    }
}
