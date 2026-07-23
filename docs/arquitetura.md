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
├── schedule
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
- `schedule_schema`
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
4. Scheduling Service
5. Service Order Service
6. Billing Service
7. Notification Service

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

---

## 13. Módulo de Clientes (Fase 1.3)

### Objetivo
Centralizar o cadastro e a consulta de clientes da empresa autenticada, mantendo isolamento por tenant e base pronta para evolução futura.

### Estrutura implementada
- Entidade `Customer` no schema `customer_schema`.
- Controller, service, repository, mapper, DTOs e validações próprias.
- Uso da infraestrutura compartilhada do `core` para auditoria, tenant, paginação e respostas padronizadas.

### Endpoints
- `POST /api/customers`
- `GET /api/customers`
- `GET /api/customers/{id}`
- `PUT /api/customers/{id}`
- `PATCH /api/customers/{id}/status`
- `DELETE /api/customers/{id}`

### Regras de negócio e segurança
- Apenas `ADMIN`, `GESTOR` e `OPERADOR`.
- Toda operação usa automaticamente a empresa do usuário autenticado.
- Acesso entre tenants é bloqueado; cliente de outra empresa não é exposto.
- Exclusão e edição exigem o contexto do tenant corrente.

### Filtros e paginação
- Filtros por `nome`, `cpfCnpj`, `telefone`, `email`, `ativo` e busca textual (`filter`).
- Ordenação reutiliza a infraestrutura do `core`.
- Resposta paginada segue o envelope padrão da API.

---

## 14. Módulo de Serviços (Fase 1.4)

### Objetivo
Centralizar o catálogo de serviços oferecidos pela empresa autenticada, com isolamento por tenant e base pronta para uso em agenda e ordens de serviço.

### Estrutura implementada
- Entidade `ServiceOffering` no schema `service_order_schema`.
- Controller, service, repository, mapper, DTOs, especificações e validações próprias.
- Uso da infraestrutura compartilhada do `core` para auditoria, tenant, paginação e respostas padronizadas.

### Endpoints
- `POST /api/services`
- `GET /api/services`
- `GET /api/services/{id}`
- `PUT /api/services/{id}`
- `PATCH /api/services/{id}/status`
- `DELETE /api/services/{id}`

### Regras de negócio e segurança
- Apenas `ADMIN`, `GESTOR` e `OPERADOR`.
- Toda operação usa automaticamente a empresa do usuário autenticado.
- Acesso entre tenants é bloqueado; serviço de outra empresa não é exposto.
- Nome obrigatório.
- Duração maior que zero.
- Preço maior que zero.
- Pode ativar/desativar, editar e excluir.

### Filtros e paginação
- Filtros por `name`, `active`, faixa de `price`, faixa de `durationMinutes` e busca textual (`filter`).
- Ordenação reutiliza a infraestrutura do `core`.
- Resposta paginada segue o envelope padrão da API.

---

## 15. Módulo de Agenda (Fase 1.5)

### Objetivo
Gerenciar funcionários, jornadas de trabalho e agendamentos da empresa autenticada, mantendo isolamento por tenant e preparando o terreno para ordens de serviço futuras.

### Estrutura implementada
- Entidades `Employee`, `WorkSchedule` e `Appointment`.
- Controllers, services, repositories, mappers, DTOs e validações próprias.
- Uso da infraestrutura compartilhada do `core` para auditoria, tenant, paginação e respostas padronizadas.

### Modelo de dados
- `employees` no schema `schedule_schema`.
- `work_schedules` no schema `schedule_schema`.
- `appointments` no schema `schedule_schema`.

### Endpoints
- `POST /api/employees`
- `GET /api/employees`
- `GET /api/employees/{id}`
- `PUT /api/employees/{id}`
- `PATCH /api/employees/{id}/status`
- `DELETE /api/employees/{id}`
- `POST /api/appointments`
- `GET /api/appointments`
- `GET /api/appointments/{id}`
- `PUT /api/appointments/{id}`
- `PATCH /api/appointments/{id}/status`
- `DELETE /api/appointments/{id}`
- `GET /api/appointments/day`
- `GET /api/appointments/week`
- `GET /api/appointments/month`
- `GET /api/appointments/employee/{id}`
- `GET /api/appointments/customer/{id}`

### Regras de negócio e segurança
- Apenas `ADMIN`, `GESTOR` e `OPERADOR`.
- Toda operação usa automaticamente a empresa do usuário autenticado.
- Acesso entre tenants é bloqueado; agendamentos, funcionários e jornadas de outra empresa não são expostos.
- Não aceita `company_id` vindo do frontend.
- Agendamento em conflito para funcionário ou cliente é recusado.
- Agendamento no passado é bloqueado por configuração.
- Funcionário, cliente e serviço precisam estar ativos.
- Horário final é calculado automaticamente com base na duração do serviço.

### Filtros e visão de agenda
- Listagem paginada de funcionários e agendamentos.
- Filtros por status, período, funcionário, cliente e serviço.
- Visualização diária, semanal e mensal por endpoints dedicados.
- Estrutura de calendário preparada para expansão futura sem drag-and-drop nesta fase.

---

## 16. Módulo de Ordens de Serviço (Fase 1.6)

### Objetivo
Controlar a execução operacional do atendimento, conectando cliente, serviço, profissional e agendamento em um fluxo único e auditável.

### Estrutura implementada
- Entidade `ServiceOrder` no schema `service_order_schema`.
- Entidade `ServiceOrderHistory` para trilha de transições.
- Controller, service, repository, mapper, DTOs, specifications e validações próprias.
- Uso da infraestrutura compartilhada do `core` para auditoria, tenant, paginação e respostas padronizadas.

### Modelo de dados
- `service_orders` no schema `service_order_schema`.
- `service_order_history` no schema `service_order_schema`.

### Estados
- `OPEN`
- `CONFIRMED`
- `IN_PROGRESS`
- `PAUSED`
- `COMPLETED`
- `CANCELLED`
- `NO_SHOW`

### Endpoints
- `POST /api/service-orders`
- `GET /api/service-orders`
- `GET /api/service-orders/{id}`
- `PUT /api/service-orders/{id}`
- `DELETE /api/service-orders/{id}`
- `PATCH /api/service-orders/{id}/start`
- `PATCH /api/service-orders/{id}/pause`
- `PATCH /api/service-orders/{id}/resume`
- `PATCH /api/service-orders/{id}/finish`
- `PATCH /api/service-orders/{id}/cancel`
- `GET /api/service-orders/customer/{id}`
- `GET /api/service-orders/professional/{id}`
- `GET /api/service-orders/status/{status}`
- `GET /api/service-orders/history/{id}`

### Regras de negócio e segurança
- Apenas `ADMIN`, `GESTOR` e `OPERADOR`.
- Toda operação usa automaticamente a empresa do usuário autenticado.
- Acesso entre tenants é bloqueado; ordens de outra empresa não são expostas.
- Ordem pode ser criada manualmente ou a partir de um agendamento.
- Ao criar a partir de um agendamento, cliente, profissional, serviço e horários são reaproveitados.
- O agendamento é sincronizado com a evolução da ordem.
- Não permite concluir sem iniciar.
- Não permite iniciar duas ordens simultâneas para o mesmo profissional.
- Não permite concluir ordem cancelada.
- Não permite alterar ordem concluída.
- Não permite excluir ordem concluída.

### Histórico e timeline
- Cada transição gera um registro em `service_order_history`.
- O histórico permite montar a linha do tempo da ordem.
- A tela de timeline consome o histórico para exibir criação, confirmação, início, pausa, retomada, conclusão e cancelamento.

### Filtros e paginação
- Filtros por cliente, profissional, serviço, status, período e busca textual.
- Ordenação reutiliza a infraestrutura do `core`.
- Resposta paginada segue o envelope padrão da API.

---

## 17. Módulo Financeiro (Fase 1.7)

### Objetivo
Gerenciar lançamentos financeiros vinculados ou não a ordens de serviço, com base para fluxo de caixa, contas a receber e futuras integrações bancárias.

### Estrutura implementada
- Entidade `FinancialTransaction` no schema `billing_schema`.
- Entidade `PaymentMethod` no schema `billing_schema`.
- Controller, service, repository, mapper, DTOs, specifications e validações próprias.
- Uso da infraestrutura compartilhada do `core` para auditoria, tenant, paginação e respostas padronizadas.

### Modelo de dados
- `payment_methods` no schema `billing_schema`.
- `financial_transactions` no schema `billing_schema`.

### Formas de pagamento
- Dinheiro
- PIX
- Cartão de Débito
- Cartão de Crédito
- Transferência
- Boleto

### Estados
- `PENDING`
- `PARTIALLY_PAID`
- `PAID`
- `CANCELLED`
- `OVERDUE`
- `REFUNDED`

### Endpoints
- `POST /api/financial`
- `GET /api/financial`
- `GET /api/financial/{id}`
- `PUT /api/financial/{id}`
- `DELETE /api/financial/{id}`
- `PATCH /api/financial/{id}/pay`
- `PATCH /api/financial/{id}/cancel`
- `PATCH /api/financial/{id}/discount`
- `PATCH /api/financial/{id}/surcharge`
- `GET /api/financial/service-order/{id}`
- `GET /api/financial/status/{status}`
- `GET /api/financial/due`
- `GET /api/financial/overdue`
- `GET /api/payment-methods`

### Regras de negócio e segurança
- Apenas `ADMIN`, `GESTOR` e `OPERADOR`.
- Toda operação usa automaticamente a empresa do usuário autenticado.
- Acesso entre tenants é bloqueado; lançamentos de outra empresa não são expostos.
- Lançamento pode ser criado manualmente ou gerado a partir de uma ordem de serviço.
- Ao concluir uma ordem de serviço, a geração automática pode ocorrer conforme configuração.
- Lançamentos já pagos não podem ser excluídos.
- Pagamento parcial é suportado.
- Descontos e acréscimos recalculam o total automaticamente.

### Integração com Ordem de Serviço
- O lançamento herda ordem de serviço, cliente, profissional e serviço quando gerado a partir da OS.
- A conclusão da OS pode acionar a criação automática do lançamento.

### Filtros e paginação
- Filtros por período, cliente, profissional, serviço, forma de pagamento e status.
- Ordenação reutiliza a infraestrutura do `core`.
- Resposta paginada segue o envelope padrão da API.
