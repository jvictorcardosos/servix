# Estrutura Técnica Inicial do Projeto Servix (Planejamento)

> Documento de planejamento técnico.  
> **Não há implementação de código nesta etapa.**

## 1. Estrutura raiz do projeto

```text
servix/
├── AGENTS.md
├── README.md
├── docs/
├── backend/
├── frontend/
├── scripts/
├── docker/
└── .github/
```

### Finalidade de cada diretório
- `backend/`: aplicação Spring Boot em monólito modular.
- `frontend/`: aplicação Vue 3 (Vite + Pinia + Router).
- `docs/`: documentação funcional, arquitetural e técnica.
- `scripts/`: automações locais (setup, build, qualidade, utilitários).
- `docker/`: arquivos de containerização e ambiente local.
- `.github/`: workflows e templates de colaboração.

---

## 2. Backend Spring Boot (Monólito Modular)

## 2.1 Definições Maven

- **Nome do projeto:** `servix-backend`
- **groupId:** `br.com.servix`
- **artifactId:** `servix-backend`
- **packaging:** `jar`
- **Java:** `21`
- **Build tool:** `Maven`

## 2.2 Organização base de pacotes

Pacote raiz:

`br.com.servix`

Pacotes transversais planejados:
- `br.com.servix.core` (infraestrutura compartilhada da aplicação)
- `br.com.servix.security` (configuração de segurança e filtros de autenticação)

## 2.3 Módulos internos do monólito

Módulos funcionais planejados:
- `auth`
- `company`
- `core`
- `customer`
- `service-order` (em pacote Java: `serviceorder`)
- `billing`
- `notification`

Estrutura padrão de cada módulo:

```text
br.com.servix.<modulo>/
├── controller/
├── service/
├── repository/
├── domain/            (entities e value objects)
├── dto/
├── validation/
└── mapper/            (conversão entity <-> dto)
```

## 2.5 Módulo Core (infraestrutura compartilhada)

Estrutura base implementada:

```text
br.com.servix.core/
├── api/          (resposta padrão de sucesso)
├── audit/        (auditoria JPA: createdAt/updatedAt/createdBy/updatedBy)
├── config/       (propriedades e constantes compartilhadas)
├── exception/    (tratamento global de erros)
├── logging/      (filtro de logging e MDC)
├── pagination/   (infra de paginação/ordenação/filtro)
├── security/     (entrypoint e access denied padronizados)
├── tenant/       (contexto de tenant/usuário e utilitários multi-tenant)
└── validation/   (validações/utilitários compartilhados)
```

Responsabilidades centralizadas no `core`:
- Auditoria de entidades reutilizável.
- Contrato único de erros e respostas de sucesso.
- Base de paginação para módulos futuros.
- Resolução de tenant para isolamento multi-empresa.
- Logging padronizado preparado para observabilidade.

## 2.4 Definição por módulo

| Módulo | Pacote Java | Entidades principais | Controllers (planejados) | Services (planejados) | Repositories (planejados) | DTOs (planejados) | Validações (planejadas) |
|---|---|---|---|---|---|---|---|
| Auth | `br.com.servix.auth` | `User`, `Role`, `RefreshToken`, `PasswordResetToken` | `AuthController` | `AuthService`, `TokenService` | `UserRepository`, `RoleRepository`, `RefreshTokenRepository` | `LoginRequestDTO`, `LoginResponseDTO`, `RefreshTokenRequestDTO`, `ForgotPasswordDTO` | formato de credenciais, expiração de token, política de senha |
| Company | `br.com.servix.company` | `Company` | `CompanyController` | `CompanyService` | `CompanyRepository` | `CompanyCreateDTO`, `CompanyUpdateDTO`, `CompanyResponseDTO` | CNPJ, status, campos obrigatórios |
| Customer | `br.com.servix.customer` | `Customer`, `ServiceCatalog` | `CustomerController`, `ServiceCatalogController` | `CustomerService`, `ServiceCatalogService` | `CustomerRepository`, `ServiceCatalogRepository` | `CustomerDTO`, `ServiceDTO` | documento, contato, duplicidade por tenant |
| Service Order | `br.com.servix.serviceorder` | `ServiceOrder`, `Appointment`, `ServiceOrderItem` | `ServiceOrderController`, `AppointmentController` | `ServiceOrderService`, `AppointmentService` | `ServiceOrderRepository`, `AppointmentRepository` | `ServiceOrderCreateDTO`, `ServiceOrderStatusDTO`, `AppointmentDTO` | transição de status, agenda, consistência de datas |
| Billing | `br.com.servix.billing` | `Payment`, `Receivable`, `BillingEntry` | `BillingController`, `PaymentController` | `BillingService`, `PaymentService` | `PaymentRepository`, `ReceivableRepository`, `BillingEntryRepository` | `PaymentDTO`, `BillingSummaryDTO` | valor monetário, vencimento, status de pagamento |
| Notification | `br.com.servix.notification` | `Notification`, `NotificationTemplate`, `NotificationLog` | `NotificationController` | `NotificationService` | `NotificationRepository`, `NotificationLogRepository` | `NotificationDTO`, `NotificationStatusDTO` | canal, destinatário, payload mínimo |

### Regras de design entre módulos
- Baixo acoplamento entre módulos.
- Alta coesão dentro do módulo.
- Sem chamadas REST internas entre módulos no MVP.
- Comunicação interna via serviços e contratos de aplicação.
- Todas as entidades de negócio com `empresa_id` (multi-tenancy).

---

## 3. Banco PostgreSQL

## 3.1 Definições iniciais

- **SGBD:** PostgreSQL
- **Banco inicial:** `servix_db`
- **Estratégia:** banco único com separação lógica por schema.

## 3.2 Schemas

- `auth_schema`
- `company_schema`
- `customer_schema`
- `service_order_schema`
- `billing_schema`
- `notification_schema`

## 3.3 Estratégia Flyway

- Flyway único no backend com versionamento incremental.
- Migrações organizadas por domínio, mantendo ordem global de versão.
- Convenção de arquivo:
  - `V<numero>__<descricao>.sql`
  - Ex.: `V1__create_company_schema.sql`

## 3.4 Organização das migrations

Estrutura planejada:

```text
backend/src/main/resources/db/migration/
├── common/
├── auth/
├── company/
├── customer/
├── serviceorder/
├── billing/
└── notification/
```

Diretriz:
- Prefixos por domínio na descrição (ex.: `V5__auth_create_users.sql`).
- Separar DDL inicial, constraints, índices e seeds técnicas quando necessário.

---

## 4. Segurança (JWT)

## 4.1 Fluxo de autenticação

1. Usuário envia credenciais em `/auth/login`.
2. Backend valida usuário + tenant + perfil.
3. Emite `access_token` (curta duração) + `refresh_token` (maior duração).
4. Frontend usa `access_token` no header `Authorization: Bearer`.
5. Quando expirar, frontend usa `/auth/refresh` para renovar.

## 4.2 Login

- Endpoint planejado: `POST /auth/login`
- Entrada: e-mail/usuário + senha
- Saída: access token, refresh token, perfil e contexto de empresa

## 4.3 Refresh token

- Endpoint planejado: `POST /auth/refresh`
- Refresh token persistido com controle de revogação/expiração.
- Rotação de refresh token planejada para elevar segurança.

## 4.4 Autorização por perfil

Perfis iniciais sugeridos:
- `ROLE_ADMIN`
- `ROLE_MANAGER`
- `ROLE_OPERATOR`

Regras:
- Autorização por rota e por ação.
- Isolamento por tenant obrigatório.
- Sem acesso a dados fora do `empresa_id` do token.

---

## 5. Frontend Vue 3

## 5.1 Estrutura de pastas (planejada)

```text
frontend/
└── src/
    ├── assets/
    ├── components/
    │   ├── common/
    │   ├── forms/
    │   ├── layout/
    │   └── tables/
    ├── views/
    │   ├── auth/
    │   ├── dashboard/
    │   ├── company/
    │   ├── customer/
    │   ├── serviceorder/
    │   ├── billing/
    │   └── notification/
    ├── stores/
    ├── services/
    ├── router/
    ├── composables/
    ├── utils/
    ├── types/
    └── validations/
```

## 5.2 Componentes (por domínio)

- `common`: botões, inputs, modais, alertas, loaders.
- `layout`: navbar, sidebar, shell autenticado.
- `forms`: formulários por entidade (cliente, serviço, OS, pagamento).
- `tables`: listagens com filtros e paginação.

## 5.3 Views principais

- `auth`: login, recuperação de senha.
- `dashboard`: visão geral.
- `company`: dados da empresa e usuários.
- `customer`: clientes e serviços.
- `serviceorder`: agenda e ordens de serviço.
- `billing`: financeiro e pagamentos.
- `notification`: histórico e envio manual (quando aplicável).

## 5.4 Stores Pinia (planejadas)

- `authStore`
- `sessionStore` (tenant/contexto)
- `companyStore`
- `customerStore`
- `serviceOrderStore`
- `billingStore`
- `notificationStore`

## 5.5 Services Axios (planejados)

- `apiClient` (instância base)
- `authApi`
- `companyApi`
- `customerApi`
- `serviceOrderApi`
- `billingApi`
- `notificationApi`

Com interceptors para:
- inclusão automática do JWT;
- tentativa de refresh em `401`;
- fallback de logout seguro.

## 5.6 Rotas (planejadas)

- Rotas públicas: `/login`, `/forgot-password`.
- Rotas protegidas: `/dashboard`, `/companies`, `/users`, `/customers`, `/services`, `/appointments`, `/service-orders`, `/billing`, `/notifications`.
- Guard de rota com verificação de autenticação e perfil.

---

## 6. Ambiente e operação local

## 6.1 Arquivos Docker preparados

Planejamento de arquivos:
- `docker/docker-compose.yml` (app + postgres)
- `docker/backend/Dockerfile`
- `docker/frontend/Dockerfile`
- `docker/.env.example`

## 6.2 Variáveis de ambiente (planejadas)

Backend:
- `SPRING_PROFILES_ACTIVE`
- `SERVER_PORT`
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USER`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_ACCESS_EXPIRATION`
- `JWT_REFRESH_EXPIRATION`

Frontend:
- `VITE_API_BASE_URL`
- `VITE_APP_NAME`

## 6.3 Configuração local

- Perfil local para backend (`local`).
- PostgreSQL local via Docker Compose.
- Frontend apontando para API local.
- Arquivo `.env.example` como referência, sem segredos reais.

---

## 7. Git e versionamento

## 7.1 Branch inicial

- `main` como branch base.
- Branch de trabalho inicial sugerida: `chore/estrutura-inicial-servix`.

## 7.2 Padrão de commits

Padrões definidos:
- `feat:`
- `fix:`
- `refactor:`
- `docs:`
- `test:`
- `chore:`

Mensagens em português.

## 7.3 Primeiro commit esperado

- **Tipo:** `docs`
- **Mensagem sugerida:** `docs: definir estrutura técnica inicial do projeto Servix`
- **Conteúdo esperado:** documentação arquitetural e de estrutura inicial aprovada.

---

## 8. Critério para iniciar implementação

A implementação do backend/frontend só deve começar após:
1. Aprovação deste documento.
2. Aprovação dos documentos `docs/arquitetura.md` e `docs/roadmap.md`.
3. Confirmação de início da Fase 1 (MVP em monólito modular).
