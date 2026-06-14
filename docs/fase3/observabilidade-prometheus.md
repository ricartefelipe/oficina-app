# Observabilidade — Prometheus e logs (Fase 3)

## Stack ativa (EKS sa-east-1)

Prometheus e Grafana rodam no cluster **oficina-dev** (namespace `oficina`), com scrape em `/api/actuator/prometheus` e alerta **`OficinaAppDown`** (`k8s/aws/prometheus.yaml`).

```bash
kubectl port-forward -n oficina svc/prometheus 9090:9090
kubectl port-forward -n oficina svc/grafana 3000:3000
```

Grafana: datasource Prometheus configurado; login admin/admin (lab).

## Métricas HTTP e JVM

Com `micrometer-registry-prometheus` no classpath, o endpoint **`GET /api/actuator/prometheus`** expõe o formato **Prometheus** (text/plain).

- **Segurança:** em `SecurityConfig`, `/actuator/prometheus` está **permitido** para facilitar scrape no cluster (restringir com **NetworkPolicy** ou autenticação mútua em produção).

## Métricas de domínio

| Métrica (Prometheus) | Descrição |
|---------------------|-----------|
| `oficina_os_criadas_total` | Contador: ordens de serviço criadas (após persistir). |
| `oficina_os_transicoes_total` | Contador por tag **`para`** (destino da transição, ex. `EM_EXECUCAO`). |
| `oficina_os_duracao_fase_seconds_*` | Timer: tempo entre duas transições consecutivas; tag **`fase`** = status em que a OS permaneceu (ex. `EM_DIAGNOSTICO`, `EM_EXECUCAO`). Base para **tempo médio por fase** do enunciário. |
| `oficina_os_notificacao_falhas_total` | Falhas ao enviar e-mail/notificação de OS (integração). |

### PromQL útil (Grafana)

- **Volume diário de OS:** `increase(oficina_os_criadas_total[1d])`
- **Taxa de criação:** `rate(oficina_os_criadas_total[5m])`
- **Transições por destino:** `sum by (para) (rate(oficina_os_transicoes_total[5m]))`
- **Latência / tempo na fase (p95):** `histogram_quantile(0.95, sum(rate(oficina_os_duracao_fase_seconds_bucket[5m])) by (le, fase))`
- **Falhas de notificação:** `rate(oficina_os_notificacao_falhas_total[5m])`

Métricas complementares:

- **Latência HTTP:** histogramas `http_server_requests_*` gerados pelo Spring Boot.

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
- **Falhas no processamento de OS:** alertar sobre `rate(oficina_os_notificacao_falhas_total[5m])` ou logs `level=ERROR` com `correlationId`; opcional contador dedicado para falhas de domínio futuras.

## Dashboards Grafana (sugestão)

1. **Overview:** CPU/mem pods, réplicas, HPA.
2. **APIs:** latência p95 `http_server_requests_seconds`, taxa 4xx/5xx.
3. **Negócio:** `increase(oficina_os_criadas_total[1d])`, histogramas `oficina_os_duracao_fase_seconds_*` por `fase`, `oficina_os_notificacao_falhas_total`.
