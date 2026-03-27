# ADR 0003 - Observabilidade com OpenTelemetry

## Status

Proposto

## Contexto

O enunciario pede integracao com ferramentas como Datadog ou New Relic, metricas, logs estruturados, tracos e dashboards.

## Decisao

Instrumentar a aplicacao e, quando possivel, o cluster com **OpenTelemetry** (OTel) como **API padrao**. Exportadores para **Datadog** ou **New Relic** ficam como **configuracao** (sem acoplar o codigo de negocio a um vendor).

Logs permanecem **JSON** com **correlation id** e **trace id** quando disponivel.

## Consequencias

- Uma camada de configuracao por ambiente (chaves, endpoints OTLP).
- Dashboards especificos do vendor ainda precisam de configuracao na conta escolhida.
