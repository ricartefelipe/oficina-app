# Domain Storytelling (Fase 1)

O **Domain Storytelling** descreve o domínio como uma **história** vivida pelos atores, com cenas encadeadas. Complementa o Event Storming e a linguagem ubíqua: aqui o foco é **narrativa** e **motivação** (o “porquê” de cada passo), não só eventos técnicos.

> **Uso académico:** pode ser replicado no **Miro** com ícones simples (pessoa, carro, ferramenta, dinheiro, e-mail) ao lado de cada cena.

## Personagens

| Papel | Quem é | Objetivo no sistema |
|-------|--------|---------------------|
| **Ana (recepção / gestão)** | Administradora | Registrar clientes e veículos, abrir OS, acompanhar diagnóstico, enviar orçamento, fechar execução e registrar entrega. |
| **Carlos (cliente)** | Dono do veículo | Saber em que etapa está o serviço e **aprovar ou não** o orçamento quando a oficina enviar. |
| **Sistema** | Back-end + integrações | Garantir regras de negócio, histórico de status, orçamento automático, estoque e notificações. |

## Cena 1 — Chegada e abertura da OS

Ana atende Carlos, que traz o carro pela primeira vez naquela unidade. Ela identifica Carlos pelo **CPF**, confere a **placa**, anota **serviços** desejados e **peças** que podem ser necessárias. O sistema **calcula o orçamento** na hora e gera um **código de acompanhamento** para Carlos consultar depois, sem precisar de login completo.

**Momento de domínio:** nasce uma **Ordem de Serviço** no estado **Recebida**; cliente e veículo ficam vinculados de forma consistente.

## Cena 2 — Diagnóstico e orçamento formal

A oficina inspeciona o veículo. Ana **inicia o diagnóstico** e, ao terminar a análise, **envia o orçamento** a Carlos. O sistema registra que a OS está **Aguardando aprovação** e dispara uma **notificação** (no MVP, e-mail de demonstração) para o cliente tomar a decisão.

**Momento de domínio:** transparência para o cliente; a OS só avança para execução após decisão explícita.

## Cena 3 — Aprovação e execução

Carlos consulta pelo **código de acompanhamento** e confirma com o **CPF**. Ao **aprovar**, o sistema valida **estoque** das peças; se houver quantidade suficiente, a OS entra em **Execução** e o estoque é **baixado**. Se não houver peça suficiente, a aprovação **falha** e nada é executado até o problema ser resolvido.

**Momento de domínio:** política de **baixa de estoque** ligada à aprovação; evita prometer serviço sem material.

## Cena 4 — Conclusão e entrega

Quando o trabalho termina, Ana **finaliza a execução** e, na retirada do veículo, **registra a entrega**. O histórico de status permanece para auditoria e para a **métrica de tempo médio** entre execução e finalização.

**Momento de domínio:** fecho do ciclo; dados prontos para métricas e melhoria contínua.

## Cena 5 — Caminhos alternativos (não felizes)

- **Orçamento recusado** (cliente ou integração externa): a OS pode ser **cancelada** sem executar serviço pago.  
- **Resposta duplicada** de sistema externo: o mesmo pedido não deve ser aplicado duas vezes (**idempotência**).

## Ligação com outros artefatos DDD

| Artefato | Ficheiro |
|----------|----------|
| Dicionário de termos | [dicionario-linguagem-ubiqua.md](dicionario-linguagem-ubiqua.md) |
| Event Storming (comandos, eventos, políticas) | [event-storming.md](event-storming.md) |
| Diagramas (Mermaid + SVG) | [diagramas.md](diagramas.md) |
