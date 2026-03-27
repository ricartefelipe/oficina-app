# Dicionário de Linguagem Ubíqua

Este documento é o **dicionário** oficial de termos do domínio (Fase 1 do Tech Challenge).  
Para o texto longo com regras e contexto, ver também [ubiquitous-language.md](ubiquitous-language.md) (mesmo conteúdo base, em formato narrativo).

> **Regra:** usar **apenas** os termos abaixo nos fluxos, APIs e código; evitar sinónimos na mesma fronteira.

## Tabela rápida

| Termo oficial | Definição | Não usar |
|---------------|-----------|----------|
| **Ordem de Serviço (OS)** | Registo do atendimento do veículo, com itens, orçamento, status e histórico. | chamado, ticket, OS genérico sem contexto |
| **Cliente** | Pessoa ou empresa dona do veículo; identificado por **CPF/CNPJ**. | usuário, consumidor (quando for o dono do carro) |
| **Veículo** | Carro atendido; identificado pela **Placa** e ligado a um Cliente. | carro (se ambíguo com frota) |
| **Placa** | Identificador do veículo nos padrões brasileiros (antigo ou Mercosul). | matrícula |
| **CPF/CNPJ** | Identificador fiscal; validado por dígitos verificadores no sistema. | documento, doc |
| **Catálogo de Serviços** | Conjunto de serviços oferecidos com preço e tempo estimado. | lista de preços |
| **Serviço (do catálogo)** | Item vendável de mão de obra (ex.: troca de óleo). | serviço genérico |
| **Peça/Insumo** | Item de estoque usado na execução, com preço e quantidade. | material (ambíguo) |
| **Orçamento** | Total calculado a partir dos itens de serviço e peça na OS. | proposta (se não for o termo oficial do negócio) |
| **Tracking Code** | Código para **consulta pública** da OS sem JWT de admin. | token, código de barras |
| **Status da OS** | Estado atual: Recebida, Em diagnóstico, Aguardando aprovação, Em execução, Finalizada, Entregue, Cancelada. | fase, etapa (sem nome do enum) |
| **Transição de status** | Registo imutável de mudança de status (auditoria e métricas). | log |
| **Administrador (Admin)** | Utilizador interno com JWT e role **ADMIN**. | operador, staff |
| **Aprovação do orçamento** | Decisão do cliente (ou integração) que permite entrar em **Em execução** | aceite, OK |

## Atores e sistemas

| Termo | Definição |
|-------|-----------|
| **API administrativa** | Endpoints `/api/admin/**` com JWT Keycloak. |
| **API pública** | Endpoints para cliente com `trackingCode` e validações (ex.: CPF na aprovação). |
| **Keycloak** | Servidor de identidade que emite JWT para admins. |
| **Notificação** | Envio de e-mail (ex.: orçamento enviado) via adaptador SMTP. |
