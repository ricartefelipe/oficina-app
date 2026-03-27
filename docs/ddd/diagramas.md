# Diagramas (DDD - Fase 1)

Este documento consolida os diagramas do MVP (Fase 1) do Sistema Integrado de Atendimento e Execução de Serviços de uma oficina.

## Artefatos visuais (SVG - Fase 2)

Versões exportadas para inclusão em PDFs ou apresentações:

- [Agregado Ordem de Serviço](diagrams/ordem-servico-agregado.svg)
- [Event storming - contextos (resumo)](diagrams/event-storming-contextos.svg)
- [Event storming - lousa com elementos C/A/E/P/R e fluxos alternativos](diagrams/event-storming-lousa-elementos.svg)

Conteúdo:
- Context Map (Bounded Contexts em monólito)
- Diagrama de agregados (foco em Ordem de Serviço)
- Sequências principais (Mermaid)

> Observação: os diagramas usam Mermaid para permitir renderização no GitHub/GitLab, e podem ser copiados para o Miro como base.

## 1) Context Map

O MVP é um **monólito em camadas**, porém organizado por **Bounded Contexts** em pacotes.

```mermaid
flowchart LR
  subgraph EXT[Contextos Externos]
    KC[Keycloak\n(Auth Server / JWT)]
    APP[Aplicativo/Portal do Cliente]
  end

  subgraph MONO[Monólito Spring Boot]
    CAD[Cadastros\n(Cliente, Veículo)]
    CAT[Catálogo\n(Serviços, Peças/Insumos)]
    OS[Ordem de Serviço\n(Aggregate Root)]
  end

  APP -->|Consulta/ Aprova Orçamento\n/public| OS
  KC -->|Emite JWT\n(role ADMIN)| MONO

  CAD <--> OS
  CAT <--> OS
```

### Integrações
- O monólito valida JWT emitido pelo Keycloak para rotas administrativas.
- A API pública é consumida pelo app/portal do cliente.

## 2) Diagrama de agregados

### Agregados (pragmático para o MVP)
- **Cliente** (Aggregate Root): identidade por CPF/CNPJ.
- **Veículo** (Aggregate Root): identidade por Placa, associado ao Cliente.
- **Serviço do Catálogo** (Aggregate Root): preço e tempo estimado.
- **Peça/Insumo** (Aggregate Root): preço e controle de estoque.
- **Ordem de Serviço (OS)** (Aggregate Root): itens, orçamento, status e histórico.

> Decisão: a OS referencia Cliente/Veículo/Catálogo via associação (JPA) no MVP, mas o raciocínio de DDD considera invariantes centrais na OS. Em evolução futura, pode-se trocar para referência por ID (anti-corruption boundary) sem afetar a API.

```mermaid
classDiagram
  class Cliente {
    UUID id
    String nome
    CpfCnpj cpfCnpj
  }

  class Veiculo {
    UUID id
    Placa placa
    String marca
    String modelo
    int ano
    UUID clienteId
  }

  class ServicoCatalogo {
    UUID id
    String nome
    BigDecimal preco
    int tempoEstimadoMin
  }

  class PecaInsumo {
    UUID id
    String nome
    BigDecimal preco
    int estoqueAtual
    int estoqueMinimo
    +decrementarEstoque(qtd)
  }

  class OrdemServico {
    UUID id
    String trackingCode
    StatusOrdemServico status
    BigDecimal orcamentoTotal
    OffsetDateTime orcamentoEnviadoAt
    OffsetDateTime aprovadoAt
    +recalcularOrcamento()
    +iniciarDiagnostico()
    +enviarOrcamento()
    +aprovarOrcamento(cpfCnpj)
    +finalizarExecucao()
    +registrarEntrega()
  }

  class OrdemServicoItemServico {
    UUID id
    int quantidade
    BigDecimal precoUnitario
    int tempoEstimadoMin
    BigDecimal subtotal
  }

  class OrdemServicoItemPeca {
    UUID id
    int quantidade
    BigDecimal precoUnitario
    BigDecimal subtotal
  }

  class OrdemServicoTransicaoStatus {
    UUID id
    StatusOrdemServico deStatus
    StatusOrdemServico paraStatus
    OffsetDateTime ocorridoEm
  }

  class CpfCnpj {
    String value
    +validarDV()
  }

  class Placa {
    String value
    +validarFormatoBR()
  }

  Cliente --> CpfCnpj
  Veiculo --> Placa

  OrdemServico "1" --> "1" Cliente
  OrdemServico "1" --> "1" Veiculo
  OrdemServico "1" --> "*" OrdemServicoItemServico
  OrdemServico "1" --> "*" OrdemServicoItemPeca
  OrdemServico "1" --> "*" OrdemServicoTransicaoStatus

  OrdemServicoItemServico "*" --> "1" ServicoCatalogo
  OrdemServicoItemPeca "*" --> "1" PecaInsumo
```

## 3) Sequência - Criação da OS (Admin)

```mermaid
sequenceDiagram
  autonumber
  actor Admin
  participant API as Admin API
  participant App as Aplicação (Use Case)
  participant OS as OrdemServico (Aggregate)
  participant CAD as Cadastros (Cliente/Veículo)
  participant CAT as Catálogo (Serviços/Peças)

  Admin->>API: POST /admin/ordens-servico
  API->>App: criarOrdemServico(cmd)
  App->>CAD: localizarOuCriarCliente(cpfCnpj)
  App->>CAD: localizarOuCriarVeiculo(placa)
  App->>CAT: obterServicos/pecas
  App->>OS: criar(...) + recalcularOrcamento
  OS-->>App: OrdemServicoCriada + Status(RECEBIDA)
  App-->>API: detalhe (trackingCode)
  API-->>Admin: 201 Created (OS)
```

## 4) Sequência - Enviar orçamento (Admin)

```mermaid
sequenceDiagram
  autonumber
  actor Admin
  participant API as Admin API
  participant App as Aplicação
  participant OS as OrdemServico
  participant Notif as Notificador (log)

  Admin->>API: POST /admin/ordens-servico/{id}/orcamento/enviar
  API->>App: enviarOrcamento(id)
  App->>OS: enviarOrcamento()
  OS-->>App: Status(AGUARDANDO_APROVACAO)
  App->>Notif: log "orcamento_enviado" (simulado)
  App-->>API: detalhe
  API-->>Admin: 200 OK
```

## 5) Sequência - Aprovar orçamento (Cliente) com baixa de estoque

```mermaid
sequenceDiagram
  autonumber
  actor Cliente
  participant API as Public API
  participant App as Aplicação
  participant OS as OrdemServico
  participant P as PecaInsumo

  Cliente->>API: POST /public/ordens-servico/{trackingCode}/aprovar (cpfCnpj)
  API->>App: aprovarOrcamentoPublico(trackingCode, cpfCnpj)
  App->>OS: validar status e cpfCnpj
  loop para cada item de peça
    App->>P: decrementaEstoque(qtd)
  end
  OS-->>App: Status(EM_EXECUCAO)
  App-->>API: detalhe
  API-->>Cliente: 200 OK
```

## 6) Sequência - Finalizar e entregar (Admin)

```mermaid
sequenceDiagram
  autonumber
  actor Admin
  participant API as Admin API
  participant App as Aplicação
  participant OS as OrdemServico

  Admin->>API: POST /admin/ordens-servico/{id}/execucao/finalizar
  API->>App: finalizarExecucao(id)
  App->>OS: finalizarExecucao()
  OS-->>App: Status(FINALIZADA)
  App-->>API: detalhe

  Admin->>API: POST /admin/ordens-servico/{id}/entrega/registrar
  API->>App: registrarEntrega(id)
  App->>OS: registrarEntrega()
  OS-->>App: Status(ENTREGUE)
  App-->>API: detalhe
```

## 7) Métrica - Tempo médio de execução

Definição do MVP:
- Tempo de execução de uma OS = `timestamp(FINALIZADA) - timestamp(EM_EXECUCAO)`
- Tempo médio = média para todas as OS (com filtro opcional por período)

A fonte de verdade são as transições de status persistidas na tabela `os_transicoes_status`.
