## Resumo

Uma linha que diga **o quê** muda e **para quê** (valor para quem usa ou opera o sistema).

---

## Contexto

Por que esta alteração existe agora? Relacione com problema, oportunidade ou requisito (sem colar histórico inteiro de chat).

---

## O que mudou

- **Código / API:** endpoints, contratos, comportamento observável.
- **Dados / migração:** scripts Liquibase, seeds, impacto em bases existentes.
- **Infra / operação:** Docker, variáveis, health checks, observabilidade.
- **Documentação:** o que foi atualizado e onde.

---

## Fora de escopo

Liste de forma explícita o que **não** entra neste PR (para evitar expectativas erradas).

---

## Como validar

Descreva o que você executou e o resultado esperado:

| Verificação | Comando ou passo | Resultado |
|-------------|------------------|-----------|
| Build       |                  |           |
| Testes      |                  |           |

Se algo não puder ser reproduzido localmente (por exemplo, dependência de serviço externo), diga o motivo e como validar em outro ambiente.

---

## Riscos e compatibilidade

- Quebras de contrato (API, eventos, formato de dados).
- Migrações ou ordem de deploy.
- Feature flags ou comportamento gradual, se houver.

---

## Checklist

- [ ] Branch atualizada com a base acordada (`develop` ou outra definida pelo time).
- [ ] Build e testes relevantes passando.
- [ ] Documentação alinhada quando o uso ou a operação mudam.
- [ ] Sem dados sensíveis, segredos ou credenciais no diff.

---

## Referências

Issues, RFCs, ADRs ou especificações que fundamentam esta mudança (links completos).
