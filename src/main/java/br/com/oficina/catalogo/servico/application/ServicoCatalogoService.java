package br.com.oficina.catalogo.servico.application;

import br.com.oficina.catalogo.servico.domain.ServicoCatalogo;
import br.com.oficina.catalogo.servico.infra.persistence.ServicoCatalogoJpaRepository;
import br.com.oficina.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ServicoCatalogoService {

    private final ServicoCatalogoJpaRepository repository;

    public ServicoCatalogoService(ServicoCatalogoJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public ServicoCatalogo criar(String nome, String descricao, BigDecimal preco, Integer tempoEstimadoMin) {
        ServicoCatalogo s = ServicoCatalogo.novo(nome, descricao, preco, tempoEstimadoMin);
        return repository.save(s);
    }

    @Transactional(readOnly = true)
    public ServicoCatalogo obter(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Servico nao encontrado"));
    }

    @Transactional(readOnly = true)
    public List<ServicoCatalogo> listar() {
        return repository.findAll();
    }

    @Transactional
    public ServicoCatalogo atualizar(UUID id, String nome, String descricao, BigDecimal preco, Integer tempoEstimadoMin) {
        ServicoCatalogo s = obter(id);
        s.atualizar(nome, descricao, preco, tempoEstimadoMin);
        return repository.save(s);
    }

    @Transactional
    public void remover(UUID id) {
        ServicoCatalogo s = obter(id);
        repository.delete(s);
    }
}
