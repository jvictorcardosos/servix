# Arquitetura do Servix v1.1

## 1. Diretriz arquitetural desta fase

O Servix terá **visão futura orientada a microserviços**, porém o MVP será implementado como **monólito modular** para validar produto rapidamente com equipe pequena, menor custo operacional e maior velocidade de entrega.

---

## 2. Princípios arquiteturais

- Simplicidade operacional primeiro.
- Evolução incremental com baixo risco.
- Separação por domínio desde o início.
- Baixo acoplamento entre módulos.
- Alta coesão dentro de cada módulo.
- Multi-tenancy obrigatório.
- Segurança por padrão (JWT + autorização).

---

## 3. Stack tecnológica obrigatória

### Backend
- Java 21
- Spring Boot 3
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven

### Frontend
- Vue 3
- Vite
- Pinia
- Vue Router
- Axios

### Infraestrutura
- Docker Ready
- GitHub Actions

---

## 4. Arquitetura inicial (MVP): Monólito Modular

### Estrutura inicial do backend

```text
backend/
├── auth
├── company
├── core
├── customer
├── service-order
├── billing
└── notification
```

### Regra de composição de cada módulo

Cada módulo deve conter:
- Controller próprio.
- Service próprio.
- Repository próprio.
- DTOs próprios.
- Validações próprias.
- Domínio isolado.

### Regras do monólito modular

- Baixo acoplamento entre módulos.
- Alta coesão interna por domínio.
- Separação clara de responsabilidades.
- Interfaces explícitas entre módulos.
- Preparação para extração futura sem reescrita ampla.

### O que **não** será feito no MVP

- Múltiplos deploys.
- Múltiplos bancos físicos.
- Comunicação REST interna entre módulos.
- Infraestrutura distribuída.

---

## 5. Banco de dados inicial (MVP)

- Um único PostgreSQL para todo o monólito.
- Separação lógica por domínio via schemas.

### Schemas iniciais
- `auth_schema`
- `company_schema`
- `customer_schema`
- `service_order_schema`
- `billing_schema`
- `notification_schema`

### Diretriz de evolução

Os domínios devem ser modelados de forma a permitir migração futura para bancos independentes, com mínimo impacto de contrato.

---

## 6. Multi-tenancy e isolamento de dados

### Regra central
Todo dado de negócio pertence a uma empresa.

### Entidades obrigatórias com vínculo de tenant
- Empresa
- Usuário
- Cliente
- Serviço
- Ordem de Serviço
- Agendamento
- Pagamento
- Notificação

### Diretrizes
- Toda tabela funcional com `empresa_id`.
- Toda consulta/escrita filtrada por contexto da empresa.
- Autorização sempre vinculada ao tenant do usuário autenticado.

---

## 7. Segurança e controle de acesso

- JWT como padrão de autenticação.
- Autorização por papéis/perfis.
- Controle de acesso por rota e recurso.
- Trilhas de auditoria para ações críticas.

---

## 8. Plano de Evolução para Microserviços

### Quando extrair um módulo

- Crescimento de carga e necessidade de escalar apenas um domínio.
- Ritmo de mudança de um módulo muito superior aos demais.
- Gargalo de time (times distintos bloqueando entregas no mesmo deploy).
- Requisito de disponibilidade isolada por domínio.

### Critérios técnicos

- Fronteira de domínio estável e clara.
- Contratos bem definidos (APIs/eventos).
- Baixo acoplamento residual com outros módulos.
- Observabilidade mínima pronta (logs, métricas, rastreabilidade).
- Plano de dados para separar schema/banco sem perda de consistência.

### Critérios de negócio

- Ganho real de velocidade de entrega.
- Impacto direto em retenção, receita ou expansão comercial.
- Justificativa de custo-benefício operacional.
- Priorização validada no roadmap do produto.

### Benefícios esperados

- Escala independente por domínio.
- Deploy independente quando necessário.
- Maior autonomia por equipe/domínio.
- Redução de risco de mudanças concentradas em um único artefato.

---

## 9. Arquitetura-alvo futura (referência)

Serviços-alvo:
1. API Gateway
2. Auth Service
3. Customer Service
4. Service Order Service
5. Billing Service
6. Notification Service

### Diretrizes da arquitetura-alvo

- Responsabilidade única por serviço.
- Banco próprio por serviço.
- Comunicação principal via REST (mensageria futura quando necessário).
- Preparado para CI/CD e operação em produção.

---

## 10. Restrições arquiteturais (não utilizar nesta fase)

- Kubernetes
- Kafka
- Eureka
- Consul
- Service Mesh
- CQRS
- Event Sourcing
- Programação Reativa
- Arquiteturas excessivamente complexas

---

## 11. Decisões arquiteturais e racional

1. **Monólito modular no MVP**  
Racional: reduz complexidade e acelera validação de mercado com equipe pequena.

2. **Microserviços como evolução, não ponto de partida**  
Racional: preserva visão de escala sem pagar custo operacional antecipado.

3. **PostgreSQL único com schemas por domínio**  
Racional: simplifica operação inicial e mantém fronteiras de dados para extração futura.

4. **Sem comunicação REST interna entre módulos**  
Racional: evitar complexidade distribuída dentro de uma única aplicação.

5. **Web-first com backend centralizando regras de negócio**  
Racional: garante reutilização futura de APIs para novos canais (incluindo mobile).

---

## 12. Infraestrutura compartilhada (Fase 1.2)

Foi criado o módulo **core** para centralizar responsabilidades transversais:

- **Auditoria JPA**: `created_at`, `updated_at`, `created_by`, `updated_by` via Spring Data Auditing.
- **Multi-tenancy**: resolução de tenant/usuário autenticado e utilitários de validação de escopo.
- **Erros padronizados**: `GlobalExceptionHandler` com contrato único (`timestamp`, `status`, `code`, `message`, `path`, `details`).
- **Respostas de sucesso padronizadas**: envelope único para payloads da API.
- **Paginação reutilizável**: DTOs e utilitários para paginação/ordenação/filtros.
- **Validações compartilhadas**: utilitários e mensagens comuns.
- **Configurações compartilhadas**: propriedades e constantes globais.
- **Logging estruturado**: filtro de requisição com `requestId`, `tenantId` e `userId` em MDC, preparado para observabilidade.
