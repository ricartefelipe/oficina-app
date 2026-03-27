package br.com.oficina.catalogo.peca.application;

import br.com.oficina.catalogo.peca.domain.PecaInsumo;
import br.com.oficina.catalogo.peca.infra.persistence.PecaInsumoJpaRepository;
import br.com.oficina.shared.domain.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class PecaInsumoService {

    private final PecaInsumoJpaRepository repository;

    public PecaInsumoService(PecaInsumoJpaRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public PecaInsumo criar(String nome, String descricao, BigDecimal preco, Integer estoqueAtual, Integer estoqueMinimo) {
        PecaInsumo p = PecaInsumo.nova(nome, descricao, preco, estoqueAtual, estoqueMinimo);
        return repository.save(p);
    }

    @Transactional(readOnly = true)
    public PecaInsumo obter(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Peca/Insumo nao encontrado"));
    }

    @Transactional(readOnly = true)
    public List<PecaInsumo> listar() {
        return repository.findAll();
    }

    @Transactional
    public PecaInsumo atualizar(UUID id, String nome, String descricao, BigDecimal preco, Integer estoqueAtual, Integer estoqueMinimo) {
        PecaInsumo p = obter(id);
        p.atualizar(nome, descricao, preco, estoqueAtual, estoqueMinimo);
        return repository.save(p);
    }

    @Transactional
    public void remover(UUID id) {
        PecaInsumo p = obter(id);
        repository.delete(p);
    }
}
