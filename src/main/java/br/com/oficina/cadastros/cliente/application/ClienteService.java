package br.com.oficina.cadastros.cliente.application;

import br.com.oficina.cadastros.cliente.domain.Cliente;
import br.com.oficina.cadastros.cliente.domain.CpfCnpj;
import br.com.oficina.cadastros.cliente.infra.persistence.ClienteJpaRepository;
import br.com.oficina.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteJpaRepository clienteRepository;

    public ClienteService(ClienteJpaRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional
    public Cliente criar(String nome, String cpfCnpjRaw) {
        CpfCnpj cpfCnpj = CpfCnpj.of(cpfCnpjRaw);
        Cliente cliente = Cliente.novo(nome, cpfCnpj);
        return clienteRepository.save(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente obter(UUID id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Cliente nao encontrado"));
    }

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clienteRepository.findAll();
    }

    @Transactional
    public Cliente atualizar(UUID id, String nome) {
        Cliente c = obter(id);
        c.renomear(nome);
        return clienteRepository.save(c);
    }

    @Transactional
    public void remover(UUID id) {
        Cliente c = obter(id);
        clienteRepository.delete(c);
    }

    @Transactional(readOnly = true)
    public Optional<Cliente> buscarPorCpfCnpj(String cpfCnpjRaw) {
        String digits = br.com.oficina.shared.domain.Strings.onlyDigits(cpfCnpjRaw);
        return clienteRepository.findByCpfCnpj_Value(digits);
    }

    @Transactional
    public Cliente obterOuCriarPorCpfCnpj(String nomeSeNovo, String cpfCnpjRaw) {
        CpfCnpj cpfCnpj = CpfCnpj.of(cpfCnpjRaw);
        return clienteRepository.findByCpfCnpj_Value(cpfCnpj.value())
                .orElseGet(() -> clienteRepository.save(Cliente.novo(nomeSeNovo, cpfCnpj)));
    }
}
