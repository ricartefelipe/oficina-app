# Event Storming (Fase 1)

## Figuras visuais (elementos no quadro)

Versões **SVG** versionadas no repositório (podem ser importadas ou replicadas no **Miro** com post-its coloridos):

- [Contextos / visão geral](diagrams/event-storming-contextos.svg)
- [Lousa com atores, comandos [C], agregado [A], eventos [E], políticas [P], read models [R] e fluxos alternativos](diagrams/event-storming-lousa-elementos.svg)
- [Diagrama de agregado Ordem de Serviço](diagrams/ordem-servico-agregado.svg)

## Conformidade com o feedback da Fase 1

O relatório de avaliação pedia explicitamente: **diagramas visuais** com atores, comandos, eventos, políticas, modelos de leitura e fluxos alternativos, além do **diagrama de agregados** na forma final do Event Storming. Este repositório passa a incluir:

- [Domain Storytelling](domain-storytelling.md) (narrativa em cenas)
- [Dicionário de Linguagem Ubíqua](dicionario-linguagem-ubiqua.md) (tabela)
- Os **SVG** acima como base visual imediata; o texto abaixo detalha comandos e eventos para transcrição ao quadro

---

Este documento representa o **Event Storming completo** dos fluxos obrigatórios do MVP do back-end da oficina, conforme o enunciado do Tech Challenge (Fase 1). 

Escopo do Event Storming:
- Fluxo 1: **Criação e acompanhamento da Ordem de Serviço (OS)**
- Fluxo 2: **Gestão de peças e insumos (estoque)**

## 1) Contexto e objetivos

### Problema
A oficina executa atendimento/diagnóstico/execução/entrega de forma desorganizada, com falhas de priorização, estoque e histórico. O MVP deve organizar esse fluxo com APIs e rastreabilidade.

### Objetivo do MVP
- Registrar OS com cliente/veículo, serviços e peças.
- Gerar orçamento automaticamente.
- Permitir acompanhamento de status e aprovação do cliente.
- Controlar estoque de peças/insumos.
- Medir tempo médio de execução.

## 2) Legenda (como ler este Event Storming)

Para facilitar a migração para Miro (ou ferramenta equivalente), usaremos uma notação textual padronizada:

- **[C] Comando**: ação disparada por um ator (Admin ou Cliente).
- **[A] Agregado**: onde as regras e invariantes são aplicadas.
- **[E] Evento de domínio**: fato que ocorreu (passado), relevante para o negócio.
- **[P] Política**: reação automática a um evento (processo) que dispara novos comandos.
- **[R] Read model/View**: projeção para consultas (listagens, telas, relatórios).
- **[X] Sistema externo**: integração (no MVP pode ser simulado por log).

> Observação: o código do MVP não precisa necessariamente ter classes explícitas de evento; o Event Storming documenta a linguagem do domínio e o comportamento, independente de implementação.

## 3) Fluxo 1 - Criação e acompanhamento da OS

### Atores
- **Admin** (usuário interno): cria OS, inicia diagnóstico, envia orçamento, finaliza execução, registra entrega.
- **Cliente**: consulta andamento e aprova orçamento via API pública.
- **[X] Notificador** (simulado por log): representa “envio de orçamento” ao cliente.

### Linha do tempo (comandos → agregados → eventos)

1. **[C] CriarOrdemServico** (Admin)
   - Identifica cliente por CPF/CNPJ.
   - Cadastra (ou vincula) veículo por placa.
   - Inclui serviços solicitados.
   - Inclui peças/insumos necessários (opcional).
   - Calcula orçamento automaticamente.
   - Gera trackingCode.

   **[A] OrdemServico** aplica regras e registra:
   - **[E] OrdemServicoCriada**
   - **[E] StatusAlterado(RECEBIDA)** (transição inicial)

   **[R] OSDetalhe / OSResumo**
   - Disponibiliza OS para listagem/detalhe no admin.

2. **[C] IniciarDiagnostico** (Admin)
   **[A] OrdemServico**
   - valida transição `RECEBIDA -> EM_DIAGNOSTICO`
   - **[E] DiagnosticoIniciado**
   - **[E] StatusAlterado(EM_DIAGNOSTICO)**

3. **[C] EnviarOrcamento** (Admin)
   **[A] OrdemServico**
   - valida transição `EM_DIAGNOSTICO -> AGUARDANDO_APROVACAO`
   - marca `orcamentoEnviadoAt`
   - **[E] OrcamentoEnviadoAoCliente**
   - **[E] StatusAlterado(AGUARDANDO_APROVACAO)**

   **[P] NotificarClienteSobreOrcamento**
   - **[X] Notificador** envia notificação (no MVP: log estruturado)

4. **[C] AprovarOrcamento** (Cliente)
   - Cliente envia `trackingCode` + `cpfCnpj` para validação adicional.

   **[A] OrdemServico**
   - valida que o status está `AGUARDANDO_APROVACAO`
   - valida que `cpfCnpj` corresponde ao Cliente associado
   - tenta baixar estoque das peças (ver Fluxo 2)
   - marca `aprovadoAt`
   - **[E] OrcamentoAprovado**
   - **[E] StatusAlterado(EM_EXECUCAO)**

5. **[C] FinalizarExecucao** (Admin)
   **[A] OrdemServico**
   - valida transição `EM_EXECUCAO -> FINALIZADA`
   - **[E] ExecucaoFinalizada**
   - **[E] StatusAlterado(FINALIZADA)**

6. **[C] RegistrarEntrega** (Admin)
   **[A] OrdemServico**
   - valida transição `FINALIZADA -> ENTREGUE`
   - **[E] EntregaRegistrada**
   - **[E] StatusAlterado(ENTREGUE)**

### Read models / Consultas (exigidas pelo MVP)

- **[R] ConsultaPublicaOSPorTrackingCode**
  - retorna status atual, orçamento, itens, histórico de transições

- **[R] ListagemAdminOS**
  - filtros: status, placa, cpfCnpj, período

- **[R] MetricasTempoMedioExecucao**
  - calcula média de `FINALIZADA.at - EM_EXECUCAO.at`

### Invariantes e regras (resumo)

- Transições permitidas (caminho feliz):
  - `RECEBIDA -> EM_DIAGNOSTICO -> AGUARDANDO_APROVACAO -> EM_EXECUCAO -> FINALIZADA -> ENTREGUE`
- Orçamento é sempre recalculado na criação com base nos itens (preços congelados no item).
- `trackingCode` é gerado na criação e é a chave de consulta pública.
- Aprovação exige validação adicional via CPF/CNPJ (sem login do cliente no MVP).

## 4) Fluxo 2 - Gestão de peças e insumos (estoque)

### Atores
- **Admin**: mantém catálogo de peças/insumos e estoque.
- **Sistema**: baixa estoque automaticamente ao iniciar execução (via aprovação do cliente).

### Linha do tempo

A) **Gestão do catálogo (Admin)**

1. **[C] CadastrarPecaInsumo**
   **[A] PecaInsumo**
   - valida dados
   - **[E] PecaInsumoCadastrada**

2. **[C] AtualizarPecaInsumo**
   **[A] PecaInsumo**
   - atualiza preço, descrição e níveis de estoque
   - **[E] PecaInsumoAtualizada**

3. **[C] RemoverPecaInsumo**
   **[A] PecaInsumo**
   - **[E] PecaInsumoRemovida**

B) **Baixa automática de estoque na execução**

1. **[E] OrcamentoAprovado** (do Fluxo 1)

2. **[P] BaixarEstoqueDasPecasDaOS**
   - para cada item de peça na OS:
     - **[C] DecrementarEstoque(pecaId, quantidade)**

3. **[A] PecaInsumo**
   - valida estoque suficiente
   - decrementa estoque
   - **[E] EstoqueDecrementado**

4. Se qualquer peça estiver com estoque insuficiente:
   - a política falha e o comando de aprovação retorna erro (409) e a OS não entra em execução.

### Read models
- **[R] ListagemAdminPecas** com indicação `abaixoDoMinimo`.

### Fluxos alternativos (obrigatório no quadro final)

- **[C] ResponderOrcamentoExternamente** (integração / admin): aprovar ou recusar via endpoint idempotente; **[E] OrcamentoRecusado** leva a **CANCELADA** quando aplicável.
- **Estoque insuficiente** na aprovação: comando falha; OS permanece **AGUARDANDO_APROVACAO** até correção de estoque ou recusa.
- **Reprocessamento seguro**: mesma decisão externa não aplica efeito duas vezes (**idempotência** por chave).

Ver também representação visual em [event-storming-lousa-elementos.svg](diagrams/event-storming-lousa-elementos.svg).

## 5) Como “colar” no Miro (ou equivalente)

A forma mais prática é transformar cada linha em um sticky note.

### Passo-a-passo
1. Crie um board e desenhe uma linha do tempo horizontal.
2. Crie 6 colunas (uma por status): RECEBIDA, EM_DIAGNOSTICO, AGUARDANDO_APROVACAO, EM_EXECUCAO, FINALIZADA, ENTREGUE.
3. Cole os blocos abaixo como stickies (um por linha) e organize na linha do tempo.
4. Use conectores (setas) entre comando → evento → política.
5. Anexe um “context map” (ver `/docs/ddd/diagramas.md`).

### Bloco pronto para colar (Fluxo 1)

Copie e cole no Miro; cada linha vira um sticky:

```
[C] CriarOrdemServico (Admin)
[E] OrdemServicoCriada
[E] StatusAlterado(RECEBIDA)
[C] IniciarDiagnostico (Admin)
[E] DiagnosticoIniciado
[E] StatusAlterado(EM_DIAGNOSTICO)
[C] EnviarOrcamento (Admin)
[E] OrcamentoEnviadoAoCliente
[E] StatusAlterado(AGUARDANDO_APROVACAO)
[P] NotificarClienteSobreOrcamento
[X] Notificador (log)
[C] AprovarOrcamento (Cliente) [trackingCode + cpfCnpj]
[E] OrcamentoAprovado
[P] BaixarEstoqueDasPecasDaOS
[E] StatusAlterado(EM_EXECUCAO)
[C] FinalizarExecucao (Admin)
[E] ExecucaoFinalizada
[E] StatusAlterado(FINALIZADA)
[C] RegistrarEntrega (Admin)
[E] EntregaRegistrada
[E] StatusAlterado(ENTREGUE)
```

### Bloco pronto para colar (Fluxo 2)

```
[C] CadastrarPecaInsumo (Admin)
[E] PecaInsumoCadastrada
[C] AtualizarPecaInsumo (Admin)
[E] PecaInsumoAtualizada
[C] RemoverPecaInsumo (Admin)
[E] PecaInsumoRemovida
[P] BaixarEstoqueDasPecasDaOS
[C] DecrementarEstoque(pecaId, quantidade)
[E] EstoqueDecrementado
```

## 6) Como apresentar no vídeo

Sugestão objetiva (roteiro detalhado em `/docs/video-script.md`):
- Mostrar rapidamente os dois fluxos no board (ou neste markdown).
- Apontar os status obrigatórios e como cada ação muda o status.
- Explicar que “envio do orçamento” é simulado por log no MVP.
- Explicar que o estoque é baixado quando entra em execução.
- Conectar o histórico de transições com a métrica de tempo médio.
