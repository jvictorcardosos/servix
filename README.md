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