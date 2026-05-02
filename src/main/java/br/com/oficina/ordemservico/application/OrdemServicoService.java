package br.com.oficina.ordemservico.application;

import br.com.oficina.cadastros.cliente.application.ClienteService;
import br.com.oficina.cadastros.cliente.domain.Cliente;
import br.com.oficina.cadastros.cliente.domain.CpfCnpj;
import br.com.oficina.cadastros.veiculo.application.VeiculoService;
import br.com.oficina.cadastros.veiculo.domain.Veiculo;
import br.com.oficina.catalogo.peca.domain.PecaInsumo;
import br.com.oficina.catalogo.peca.infra.persistence.PecaInsumoJpaRepository;
import br.com.oficina.catalogo.servico.application.ServicoCatalogoService;
import br.com.oficina.catalogo.servico.domain.ServicoCatalogo;
import br.com.oficina.ordemservico.adapters.out.persistence.OsOrcamentoRespostaIdempotenciaRepository;
import br.com.oficina.ordemservico.application.port.NotificacaoOrdemServicoPort;
import br.com.oficina.ordemservico.application.port.OrdemServicoPersistencePort;
import br.com.oficina.ordemservico.domain.DecisaoRespostaOrcamentoExterna;
import br.com.oficina.ordemservico.domain.OsOrcamentoRespostaIdempotencia;
import br.com.oficina.ordemservico.domain.OrdemServico;
import br.com.oficina.ordemservico.domain.StatusOrdemServico;
import br.com.oficina.ordemservico.domain.TrackingCodeGenerator;
import br.com.oficina.shared.domain.BusinessRuleException;
import br.com.oficina.shared.domain.NotFoundException;
import br.com.oficina.shared.domain.Strings;
import br.com.oficina.shared.domain.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@Service
public class OrdemServicoService {

    private static final Logger log = LoggerFactory.getLogger(OrdemServicoService.class);

    private final OrdemServicoPersistencePort ordemServicoPersistence;
    private final ClienteService clienteService;
    private final VeiculoService veiculoService;
    private final ServicoCatalogoService servicoCatalogoService;
    private final PecaInsumoJpaRepository pecaRepository;
    private final OsOrcamentoRespostaIdempotenciaRepository orcamentoRespostaIdempotenciaRepository;
    private final NotificacaoOrdemServicoPort notificacaoOrdemServicoPort;
    private final OrdemServicoObservability observability;

    public OrdemServicoService(
            OrdemServicoPersistencePort ordemServicoPersistence,
            ClienteService clienteService,
            VeiculoService veiculoService,
            ServicoCatalogoService servicoCatalogoService,
            PecaInsumoJpaRepository pecaRepository,
            OsOrcamentoRespostaIdempotenciaRepository orcamentoRespostaIdempotenciaRepository,
            NotificacaoOrdemServicoPort notificacaoOrdemServicoPort,
            OrdemServicoObservability observability
    ) {
        this.ordemServicoPersistence = ordemServicoPersistence;
        this.clienteService = clienteService;
        this.veiculoService = veiculoService;
        this.servicoCatalogoService = servicoCatalogoService;
        this.pecaRepository = pecaRepository;
        this.orcamentoRespostaIdempotenciaRepository = orcamentoRespostaIdempotenciaRepository;
        this.notificacaoOrdemServicoPort = notificacaoOrdemServicoPort;
        this.observability = observability;
    }

    @Transactional
    public OrdemServico criarOrdemServico(
            String clienteNome,
            String clienteCpfCnpj,
            String placa,
            String marca,
            String modelo,
            Integer ano,
            List<ItemServico> servicos,
            List<ItemPeca> pecas
    ) {
        if (servicos == null || servicos.isEmpty()) {
            throw new ValidationException("Deve informar ao menos 1 servico");
        }

        Cliente cliente = clienteService.obterOuCriarPorCpfCnpj(clienteNome, clienteCpfCnpj);
        Veiculo veiculo = veiculoService.obterOuCriarPorPlaca(cliente, placa, marca, modelo, ano);

        String trackingCode = TrackingCodeGenerator.generate(12);
        OrdemServico os = OrdemServico.receber(cliente, veiculo, trackingCode);

        for (ItemServico i : servicos) {
            Objects.requireNonNull(i, "itemServico nao pode ser null");
            ServicoCatalogo s = servicoCatalogoService.obter(i.servicoId());
            os.adicionarServico(s, i.quantidade());
        }

        if (pecas != null) {
            for (ItemPeca i : pecas) {
                Objects.requireNonNull(i, "itemPeca nao pode ser null");
                PecaInsumo p = pecaRepository.findById(i.pecaId())
                        .orElseThrow(() -> new NotFoundException("Peca/Insumo nao encontrado"));
                os.adicionarPeca(p, i.quantidade());
            }
        }

        OrdemServico saved = ordemServicoPersistence.save(os);
        observability.registrarOsCriada();
        log.info("os_criada osId={} trackingCode={} status={} orcamentoTotal={}", saved.getId(), saved.getTrackingCode(), saved.getStatus(), saved.getOrcamentoTotal());
        return saved;
    }

    @Transactional(readOnly = true)
    public OrdemServico obterDetalhe(UUID id) {
        return ordemServicoPersistence.findDetailedById(id)
                .orElseThrow(() -> new NotFoundException("Ordem de Servico nao encontrada"));
    }

    @Transactional(readOnly = true)
    public OrdemServico obterPorTrackingCode(String trackingCode) {
        String tc = Strings.requireNonBlank(trackingCode, "trackingCode").trim().toUpperCase();
        return ordemServicoPersistence.findByTrackingCode(tc)
                .orElseThrow(() -> new NotFoundException("Ordem de Servico nao encontrada"));
    }

    @Transactional(readOnly = true)
    public List<OrdemServico> listar(
            StatusOrdemServico status,
            String placa,
            String cpfCnpj,
            OffsetDateTime from,
            OffsetDateTime to,
            boolean incluirEncerradas
    ) {
        String placaNormalized = (placa == null || placa.isBlank()) ? null : Strings.alnumUpper(placa);
        String cpfDigits = (cpfCnpj == null || cpfCnpj.isBlank()) ? null : Strings.onlyDigits(cpfCnpj);

        boolean excluirEncerradas = status == null && !incluirEncerradas;
        OrdemServicoListagemOrdem ordem;
        if (status != null) {
            ordem = OrdemServicoListagemOrdem.CRIADA_MAIS_ANTIGA;
        } else if (excluirEncerradas) {
            ordem = OrdemServicoListagemOrdem.PRIORIDADE_OPERACAO;
        } else {
            ordem = OrdemServicoListagemOrdem.CRIADA_MAIS_RECENTE;
        }

        Specification<OrdemServico> spec = OrdemServicoSpecifications.filtrar(
                status, placaNormalized, cpfDigits, from, to, excluirEncerradas, ordem);
        return ordemServicoPersistence.findAll(spec);
    }

    @Transactional
    public OrdemServico iniciarDiagnostico(UUID osId) {
        OrdemServico os = carregarDetalheParaMutacao(osId);
        os.iniciarDiagnostico();
        OrdemServico saved = ordemServicoPersistence.save(os);
        log.info("os_status_alterado osId={} status={}", saved.getId(), saved.getStatus());
        return saved;
    }

    @Transactional
    public OrdemServico enviarOrcamento(UUID osId) {
        OrdemServico os = carregarDetalheParaMutacao(osId);
        os.enviarOrcamento();
        OrdemServico saved = ordemServicoPersistence.save(os);
        log.info("orcamento_enviado osId={} trackingCode={} orcamentoTotal={}", saved.getId(), saved.getTrackingCode(), saved.getOrcamentoTotal());
        notificarSeguro(n -> n.aoEnviarOrcamento(saved));
        return saved;
    }

    @Transactional
    public OrdemServico finalizarExecucao(UUID osId) {
        OrdemServico os = carregarDetalheParaMutacao(osId);
        os.finalizarExecucao();
        OrdemServico saved = ordemServicoPersistence.save(os);
        log.info("os_status_alterado osId={} status={}", saved.getId(), saved.getStatus());
        return saved;
    }

    @Transactional
    public OrdemServico registrarEntrega(UUID osId) {
        OrdemServico os = carregarDetalheParaMutacao(osId);
        os.registrarEntrega();
        OrdemServico saved = ordemServicoPersistence.save(os);
        log.info("os_status_alterado osId={} status={}", saved.getId(), saved.getStatus());
        notificarSeguro(n -> n.aoVeiculoEntregue(saved));
        return saved;
    }

    @Transactional
    public OrdemServico aprovarOrcamentoPublico(String trackingCode, String cpfCnpjRaw) {
        OrdemServico os = obterPorTrackingCode(trackingCode);

        CpfCnpj informado = CpfCnpj.of(cpfCnpjRaw);
        String esperado = os.getCliente().getCpfCnpj().value();
        if (!esperado.equals(informado.value())) {
            throw new BusinessRuleException("CPF/CNPJ nao confere para esta Ordem de Servico");
        }

        return executarAprovacaoComBaixaEstoque(os);
    }

    /**
     * Integração externa (ex.: notificação de canal parceiro): aprova ou recusa o orçamento com idempotência por chave.
     */
    @Transactional
    public OrdemServico processarRespostaOrcamentoExterna(UUID osId, String idempotencyKeyRaw, DecisaoRespostaOrcamentoExterna decisao) {
        String key = Strings.requireNonBlank(idempotencyKeyRaw, "Idempotency-Key").trim();
        if (key.length() > 128) {
            throw new ValidationException("Idempotency-Key deve ter no maximo 128 caracteres");
        }

        Optional<OsOrcamentoRespostaIdempotencia> jaRegistrado = orcamentoRespostaIdempotenciaRepository.findByIdempotencyKey(key);
        if (jaRegistrado.isPresent()) {
            validarMesmaOperacaoIdempotente(osId, decisao, jaRegistrado.get());
            return obterDetalhe(osId);
        }

        OrdemServico os = ordemServicoPersistence.findDetailedByIdForUpdate(osId)
                .orElseThrow(() -> new NotFoundException("Ordem de Servico nao encontrada"));

        if (os.getStatus() != StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new BusinessRuleException("Ordem nao esta aguardando aprovacao do orcamento");
        }

        try {
            orcamentoRespostaIdempotenciaRepository.saveAndFlush(
                    new OsOrcamentoRespostaIdempotencia(UUID.randomUUID(), key, osId, decisao));
        } catch (DataIntegrityViolationException e) {
            OsOrcamentoRespostaIdempotencia row = orcamentoRespostaIdempotenciaRepository.findByIdempotencyKey(key)
                    .orElseThrow(() -> e);
            validarMesmaOperacaoIdempotente(osId, decisao, row);
            return obterDetalhe(osId);
        }

        if (decisao == DecisaoRespostaOrcamentoExterna.APROVAR) {
            return executarAprovacaoComBaixaEstoque(os);
        }

        os.recusarOrcamento();
        OrdemServico saved = ordemServicoPersistence.save(os);
        log.info("orcamento_recusado_externo osId={} trackingCode={} status={}", saved.getId(), saved.getTrackingCode(), saved.getStatus());
        notificarSeguro(n -> n.aoOrcamentoRecusado(saved));
        return saved;
    }

    private void validarMesmaOperacaoIdempotente(
            UUID osId,
            DecisaoRespostaOrcamentoExterna decisao,
            OsOrcamentoRespostaIdempotencia row
    ) {
        if (!row.getOrdemServicoId().equals(osId) || row.getDecisao() != decisao) {
            throw new BusinessRuleException("Chave de idempotencia ja utilizada para outra operacao");
        }
    }

    private OrdemServico executarAprovacaoComBaixaEstoque(OrdemServico os) {
        Map<UUID, Integer> qtyByPeca = new HashMap<>();
        for (var item : os.getItensPeca()) {
            UUID pecaId = item.getPeca().getId();
            if (pecaId == null) {
                throw new BusinessRuleException("Item de peca sem identificador");
            }
            qtyByPeca.merge(pecaId, item.getQuantidade(), Integer::sum);
        }

        for (Map.Entry<UUID, Integer> e : qtyByPeca.entrySet()) {
            UUID pecaId = e.getKey();
            int qtd = e.getValue();
            PecaInsumo peca = pecaRepository.findByIdForUpdate(pecaId)
                    .orElseThrow(() -> new NotFoundException("Peca/Insumo nao encontrado"));
            peca.decrementarEstoque(qtd);
        }

        os.aprovarOrcamento();
        OrdemServico saved = ordemServicoPersistence.save(os);
        log.info("orcamento_aprovado osId={} trackingCode={} status={}", saved.getId(), saved.getTrackingCode(), saved.getStatus());
        notificarSeguro(n -> n.aoOrcamentoAprovado(saved));
        return saved;
    }

    /**
     * Carrega OS com grafo de detalhe dentro da mesma transação de escrita (sem auto-invocação de
     * {@link #obterDetalhe(UUID)} com {@code readOnly=true}).
     */
    private OrdemServico carregarDetalheParaMutacao(UUID osId) {
        // findDetailedById usa @EntityGraph; em alguns cenários o Hibernate pode marcar o resultado como
        // somente leitura, falhando no flush (InvalidDataAccessApiUsageException). Para mutação usamos findById simples.
        return ordemServicoPersistence.findById(osId)
                .orElseThrow(() -> new NotFoundException("Ordem de Servico nao encontrada"));
    }

    private void notificarSeguro(Consumer<NotificacaoOrdemServicoPort> acao) {
        try {
            acao.accept(notificacaoOrdemServicoPort);
        } catch (RuntimeException e) {
            log.warn("notificacao_ordem_servico_falhou", e);
        }
    }

    public record ItemServico(UUID servicoId, int quantidade) {
    }

    public record ItemPeca(UUID pecaId, int quantidade) {
    }
}
