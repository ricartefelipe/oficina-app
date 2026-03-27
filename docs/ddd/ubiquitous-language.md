# Linguagem Ubíqua (Ubiquitous Language)

> **Dicionário resumido (tabela):** [dicionario-linguagem-ubiqua.md](dicionario-linguagem-ubiqua.md) — use-o no PDF do portal se o enunciário pedir o “dicionário” explicitamente.

Este glossário padroniza os termos usados no domínio do MVP (Fase 1) do Sistema Integrado de Atendimento e Execução de Serviços de uma oficina mecânica.

> Regra: os nomes abaixo são os termos oficiais do produto. Evite sinônimos nos fluxos, APIs e código. Ex.: use **Ordem de Serviço** (OS), não “ordem”, “chamado” ou “serviço”.

## Atores

- **Cliente (pessoa/empresa)**: proprietário do veículo, identificado de forma única por **CPF/CNPJ**.
- **Administrador (Admin)**: usuário interno da oficina que opera os módulos administrativos (cadastros e OS). Acesso protegido por JWT e role **ADMIN**.
- **Aplicativo/Portal do Cliente**: consumidor da API pública para consultar progresso e aprovar orçamento.

## Conceitos de negócio

- **CPF/CNPJ**: identificador fiscal brasileiro. No sistema, é um **Value Object** validado por dígitos verificadores e armazenado apenas com dígitos.
- **Veículo**: carro atendido pela oficina. No MVP é identificado pela **Placa** e associado ao Cliente.
- **Placa**: placa brasileira do veículo, aceita nos padrões:
  - **Antigo**: `ABC1234`
  - **Mercosul**: `ABC1D23`
  - No sistema é um **Value Object** normalizado para maiúsculas sem hífen.

- **Catálogo de Serviços**: lista de serviços oferecidos pela oficina, com preço e tempo estimado.
- **Serviço (do catálogo)**: item vendável (ex.: “Troca de óleo”), com:
  - **Preço**
  - **Tempo estimado (minutos)**

- **Peça/Insumo**: item de estoque usado na execução do serviço (ex.: óleo, filtro). Possui:
  - **Preço unitário**
  - **Estoque atual**
  - **Estoque mínimo** (ponto de atenção - não bloqueia automaticamente, mas sinaliza)

- **Ordem de Serviço (OS)**: registro do atendimento do veículo, incluindo itens de serviço e peças, orçamento, status e histórico.
- **Tracking Code**: código gerado na criação da OS para permitir **consulta pública** (cliente) sem login.

- **Orçamento**: valor total calculado automaticamente:
  - `total = soma(subtotal serviços) + soma(subtotal peças)`

## Itens

- **Item de Serviço (da OS)**: referência a um Serviço do Catálogo, com quantidade, preço unitário “congelado” no momento da criação e subtotal.
- **Item de Peça (da OS)**: referência a uma Peça/Insumo, com quantidade, preço unitário “congelado” no momento da criação e subtotal.

## Status da OS

Status obrigatórios do enunciado (ordem principal):

1. **RECEBIDA**: OS criada e registrada.
2. **EM_DIAGNOSTICO**: equipe iniciou análise do veículo.
3. **AGUARDANDO_APROVACAO**: orçamento enviado ao cliente e aguarda aprovação.
4. **EM_EXECUCAO**: cliente aprovou orçamento e a oficina iniciou execução.
5. **FINALIZADA**: execução concluída.
6. **ENTREGUE**: veículo entregue ao cliente.

## Histórico / transições

- **Transição de Status**: registro imutável contendo:
  - status anterior
  - status novo
  - data/hora da transição

Este histórico é a base de auditoria e métricas (tempo médio de execução).

## Regras operacionais (MVP)

- **Baixa de estoque**: ocorre quando a OS entra em **EM_EXECUCAO** (após aprovação do cliente).
  - Se não houver estoque suficiente para alguma peça, a aprovação falha.
- **Tempo médio de execução**: média do tempo entre:
  - `EM_EXECUCAO.ocorridoEm` e `FINALIZADA.ocorridoEm`

## Termos técnicos padronizados

- **Domínio**: regras e entidades/VOs do core (DDD).
- **Caso de uso**: operação de negócio orquestrada na camada de aplicação (services).
- **API pública**: endpoints sem JWT para o cliente acompanhar e aprovar orçamento.
- **API administrativa**: endpoints sob `/api/admin/**` protegidos por JWT (Keycloak).
