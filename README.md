# Servix

Gestão completa para prestadores de serviço.

## Descrição

Servix é um SaaS web para prestadores de serviço autônomos e pequenas empresas organizarem clientes, agenda, ordens de serviço e financeiro em um único sistema.

Esta fase contém apenas a **base técnica** do projeto (fundação), sem funcionalidades de negócio implementadas.

## Tecnologias

### Backend
- Java 21
- Spring Boot 3
- Maven
- Spring Web
- Spring Security
- Spring Data JPA
- Flyway
- PostgreSQL

### Frontend
- Vue 3
- Vite
- Vue Router
- Pinia
- Axios

### Infra
- Docker Compose (ambiente local com PostgreSQL)
- GitHub (estrutura preparada para Actions)

## Como iniciar o ambiente

## 1. Subir PostgreSQL local

```bash
docker compose up -d
```

## 2. Iniciar backend

```bash
cd backend
./mvnw spring-boot:run
```

No Windows PowerShell:

```powershell
cd backend
.\mvnw spring-boot:run
```

## 3. Iniciar frontend

```bash
cd frontend
npm install
npm run dev
```

## 4. Build e validação

Backend:

```bash
cd backend
./mvnw clean verify
```

Frontend:

```bash
cd frontend
npm run build
```

## Status Atual do Projeto

- **Fase atual:** Fase 1.3 - Módulo de Clientes.

### O que está funcionando

- Estrutura raiz do projeto organizada (`backend`, `frontend`, `docs`, `scripts`, `docker`, `.github`).
- Backend Spring Boot 3 com Java 21 e Maven Wrapper configurado.
- Dependências principais do backend configuradas (Web, Security, JPA, Flyway, PostgreSQL, Validation, Lombok).
- Arquitetura modular preparada com os módulos: `auth`, `company`, `customer`, `serviceorder`, `billing`, `notification`.
- Módulo `core` criado com infraestrutura compartilhada:
  - auditoria (`createdAt`, `updatedAt`, `createdBy`, `updatedBy`);
  - multi-tenancy centralizado;
  - respostas padronizadas de sucesso e erro;
  - paginação/ordenação/filtros reutilizáveis;
  - logging de requisição com `requestId`, `tenantId`, `userId`.
- Módulo de Clientes implementado com:
  - cadastro, consulta, edição, alteração de status e exclusão;
  - filtros por nome, documento, telefone, email e ativo;
  - paginação e ordenação;
  - isolamento por tenant com bloqueio de acesso entre empresas.
- Flyway configurado com migration inicial `V1__initial_setup.sql` para criação dos schemas.
- Flyway com estrutura de auth/company e constraints base (`V2`, `V3`).
- Frontend Vue 3 com Vite, Vue Router, Pinia e Axios configurados.
- Build do frontend executando com sucesso (`npm run build`).
- Build/teste do backend executando com sucesso via Maven Wrapper (`.\mvnw.cmd clean verify`).

### Pendências

- Executar ambiente Docker local com PostgreSQL em máquina com Docker instalado e em execução.
- Iniciar os próximos módulos de negócio (Serviços, Agenda, Ordens de Serviço e Financeiro).

### Requisitos para executar Docker localmente

- Docker Desktop instalado.
- Docker Engine em execução.
- Comando de subida do banco:

```bash
docker compose up -d
```