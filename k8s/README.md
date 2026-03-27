# Kubernetes - aplicação Oficina

Manifestos para o namespace `oficina`: **Deployment**, **Service**, **ConfigMap**, **Secret** (exemplo), **HPA** e probes HTTP nos endpoints do Actuator (`/actuator/health/*`).

## Pré-requisitos

- Cluster Kubernetes funcional (EKS, GKE, AKS, kind, minikube, etc.).
- **PostgreSQL** acessível a partir do cluster (serviço no cluster, CNPG, RDS, Cloud SQL, etc.). O JDBC em `secret.example.yaml` é apenas ilustrativo.
- **Keycloak** (ou outro IdP) com JWK e *issuers* coerentes com o que a aplicação valida (`JWT_JWK_SET_URI`, `JWT_ALLOWED_ISSUERS` no ConfigMap).
- Imagem da API publicada num registry que o cluster consiga puxar, ou imagem carregada localmente (kind/minikube).

## Ordem sugerida de apply

1. Namespace e ConfigMap:

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
```

2. Secret com credenciais reais (não commite):

```bash
cp k8s/secret.example.yaml k8s/secret.yaml
# Edite k8s/secret.yaml (DB_URL, DB_USER, DB_PASS)
kubectl apply -f k8s/secret.yaml
```

Se criou **RDS** com Terraform (`enable_rds = true` em `infra/`), use os outputs `rds_jdbc_url`, `rds_master_password` e o utilizador configurado (`db_username`, por defeito `oficina`) para preencher `DB_URL`, `DB_PASS` e `DB_USER`. Ver [`../infra/README.md`](../infra/README.md).

3. Deployment, Service e HPA:

```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/hpa.yaml
```

4. Ajustar imagem (se necessário):

```bash
kubectl -n oficina set image deployment/oficina-app app=SEU_REGISTRY/oficina-springboot-mvp:VERSAO
```

## Rollback

Listar revisões:

```bash
kubectl -n oficina rollout history deployment/oficina-app
```

Voltar à revisão anterior:

```bash
kubectl -n oficina rollout undo deployment/oficina-app
```

Para uma revisão específica:

```bash
kubectl -n oficina rollout undo deployment/oficina-app --to-revision=2
```

## Verificação rápida

```bash
kubectl -n oficina get pods,svc,hpa
kubectl -n oficina logs -l app=oficina-app --tail=100
```

Port-forward local (sem Ingress):

```bash
kubectl -n oficina port-forward svc/oficina-app 8080:8080
# Ex.: http://localhost:8080/actuator/health
```

## Notas

- O **MailHog** do docker-compose não existe por defeito no cluster; ajuste `MAIL_HOST` / `MAIL_PORT` no ConfigMap para o seu SMTP ou desative notificações com `NOTIFICATION_ENABLED=false`.
- `PUBLIC_BASE_URL` deve refletir o URL público usado nos e-mails (links para o cliente).
- O HPA requer **metrics-server** instalado no cluster para métricas de CPU/memória.
