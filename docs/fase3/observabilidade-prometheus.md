# Observabilidade — Prometheus e logs (Fase 3)

## Métricas HTTP e JVM

Com `micrometer-registry-prometheus` no classpath, o endpoint **`GET /api/actuator/prometheus`** expõe o formato **Prometheus** (text/plain).

- **Segurança:** em `SecurityConfig`, `/actuator/prometheus` está **permitido** para facilitar scrape no cluster (restringir com **NetworkPolicy** ou autenticação mútua em produção).

## Métricas de domínio

| Métrica | Descrição |
|---------|-----------|
| `oficina_os_criadas_total` | Contador: ordens de serviço criadas (incrementa após persistir). |

Métricas adicionais úteis para o enunciário (implementar em evolução ou via regras PromQL):

- **Volume diário de OS:** `increase(oficina_os_criadas_total[1d])` ou agregação por tempo no Grafana.
- **Latência HTTP:** histogramas `http_server_requests_*` gerados pelo Spring Boot.
- **Tempo médio por status:** usar dados da API admin de métricas ou expor gauges por `StatusOrdemServico` (evolução).

## Logs estruturados e correlação

- Filtro **`X-Correlation-Id`** preenche MDC `correlationId` (ver `CorrelationIdFilter`).
- Perfil **`k8s`**: `logback-spring.xml` usa **LogstashEncoder** (JSON no stdout) para agregadores (Loki, Datadog, CloudWatch).

```bash
# Exemplo: subir com perfil k8s (Docker / K8s)
SPRING_PROFILES_ACTIVE=k8s
```

## Alertas (exemplo)

- Taxa de erros HTTP 5xx acima de limiar.
- Pod não pronto / falha de **liveness** (`/actuator/health/liveness`).
- **Falhas no processamento de OS:** regra de negócio → incrementar contador dedicado (evolução) ou alerta sobre logs `level=ERROR` com `correlationId`.

## Dashboards Grafana (sugestão)

1. **Overview:** CPU/mem pods, réplicas, HPA.
2. **APIs:** latência p95 `http_server_requests_seconds`, taxa 4xx/5xx.
3. **Negócio:** painel com query à API `/admin/metricas/tempo-medio-execucao` ou métricas exportadas adicionais.
