# Servix Roadmap v1.1

## Visão Geral

### Objetivo
Entregar um SaaS web para prestadores de serviço organizarem operação, atendimento e finanças em um único sistema, com foco em ganho de produtividade e aumento de receita.

### Público-alvo
- Prestadores autônomos: eletricistas, encanadores, técnicos de informática, instaladores de ar-condicionado, montadores, pedreiros e pintores.
- Pequenas empresas de manutenção.

### Problemas resolvidos
- Desorganização de clientes, agenda e ordens de serviço.
- Dificuldade de cobrança e controle de recebimentos.
- Falta de visão do fluxo financeiro do negócio.
- Ausência de histórico operacional para tomada de decisão.

### Diferenciais
- Fluxo integrado ponta a ponta: cliente → agenda → ordem de serviço → cobrança → financeiro.
- Arquitetura inicial em monólito modular, preparada para extração futura por domínio.
- Multi-tenancy nativo (dados segregados por empresa).
- Base pronta para evoluções de automação e IA em fases futuras.

### Monetização
- Assinatura mensal por empresa (SaaS).
- Planos em camadas por recursos (MVP, avançado, automações).
- Upsell em funcionalidades de Fase 2 e Fase 3 (ex.: PIX, notificações automáticas, IA).

---

## MVP

### Escopo
- Cadastro de empresas e usuários.
- Login e recuperação de senha.
- Cadastro de clientes e serviços.
- Agenda de atendimentos.
- Ordens de serviço.
- Financeiro básico (lançamentos e status de pagamento).
- Dashboard básico.
- Relatórios básicos.
- Backend em monólito modular com módulos isolados por domínio.
- PostgreSQL único com schemas por domínio.

### Fora do escopo (MVP)
- Apps mobile nativos/híbridos.
- Integrações avançadas (WhatsApp, IA, automações complexas).
- Infraestrutura avançada (Kubernetes, service mesh, mensageria em produção).
- Separação em múltiplos microserviços com deploy independente.
- Comunicação distribuída entre serviços.

### Evoluções futuras
- **Fase 2:** upload de fotos, assinatura digital, cobrança PIX, histórico de atendimento, dashboard avançado, notificações automáticas.
- **Fase 3:** integração WhatsApp, IA para orçamento/descrição/análise financeira/precificação, automações de cobrança e indicadores avançados.

---

## Épicos

1. **Autenticação**: login, recuperação de senha, JWT, sessão segura.
2. **Empresas**: cadastro e contexto do tenant.
3. **Usuários**: perfis, permissões e vínculo com empresa.
4. **Clientes**: cadastro, consulta e histórico.
5. **Agenda**: agendamentos, status e visualização.
6. **Ordens de Serviço**: abertura, execução e fechamento.
7. **Financeiro**: lançamentos, recebimentos e conciliação simples.
8. **Relatórios**: visão operacional e financeira básica.
9. **Notificações**: base para alertas transacionais e comunicação futura.

---

## Backlog por Prioridade

### P0 (fundação obrigatória)
- Estrutura inicial do monólito modular no backend:
  - `auth`
  - `company`
  - `customer`
  - `service-order`
  - `billing`
  - `notification`
- Definição de padrões internos por módulo:
  - Controller próprio
  - Service próprio
  - Repository próprio
  - DTOs próprios
  - Validações próprias
  - Domínio isolado
- PostgreSQL único com schemas por domínio.
- Cadastro de empresa e usuário.
- Login e recuperação de senha.
- Contexto multi-tenant em todas as operações.
- CRUD de clientes e serviços.
- Agenda básica.
- Ordem de serviço básica.
- Financeiro básico.

### P1 (valor comercial imediato)
- Dashboard básico.
- Relatórios básicos.
- Controle de permissões por papel.
- Melhoria de UX dos fluxos principais.

### P2 (crescimento e retenção)
- Upload de fotos.
- Assinatura digital.
- Cobrança PIX.
- Notificações automáticas.
- Dashboard avançado.

### P3 (diferenciação)
- Integração WhatsApp.
- IA para orçamento, descrição de serviço, análise financeira e precificação.
- Automações avançadas de cobrança.

---

## Estratégia Arquitetural do Roadmap

### Implementação inicial (MVP): Monólito modular

- Um único deploy de backend.
- Um único PostgreSQL.
- Separação por módulos de domínio.
- Sem REST interno entre módulos.
- Sem infraestrutura distribuída.

### Visão futura: Arquitetura em microserviços

| Serviço futuro | Responsabilidade | Banco futuro | Endpoints-base | Comunicação |
|---|---|---|---|---|
| API Gateway | Entrada única e roteamento | Sem banco de domínio | `/api/*` | REST |
| Auth Service | Identidade e autenticação | Banco próprio | `/auth/*` | REST |
| Customer Service | Empresas, usuários, clientes e serviços | Banco próprio | `/companies/*`, `/users/*`, `/customers/*`, `/services/*` | REST |
| Service Order Service | Agenda e ordens de serviço | Banco próprio | `/appointments/*`, `/service-orders/*` | REST |
| Billing Service | Financeiro e pagamentos | Banco próprio | `/billing/*`, `/payments/*` | REST |
| Notification Service | Notificações | Banco próprio | `/notifications/*` | REST |

---

## Banco de Dados Inicial (MVP)

- Banco único PostgreSQL.
- Schemas por domínio para separação lógica:
  - `auth_schema`
  - `company_schema`
  - `customer_schema`
  - `service_order_schema`
  - `billing_schema`
  - `notification_schema`

Diretriz: modelagem pronta para migração futura de cada domínio para banco próprio.

---

## Modelagem de Dados (Domínios obrigatórios)

### Empresa
- id, nome_fantasia, razao_social, cnpj, status, created_at, updated_at.

### Usuário
- id, empresa_id, nome, email, senha_hash, papel, status, created_at, updated_at.

### Cliente
- id, empresa_id, nome, telefone, email, documento, endereco, created_at, updated_at.

### Serviço
- id, empresa_id, nome, descricao, preco_base, ativo, created_at, updated_at.

### Ordem de Serviço
- id, empresa_id, cliente_id, servico_id, status, descricao, valor_total, data_execucao, created_at, updated_at.

### Agendamento
- id, empresa_id, cliente_id, ordem_servico_id, inicio, fim, status, observacao, created_at, updated_at.

### Pagamento
- id, empresa_id, ordem_servico_id, valor, metodo, status, vencimento, pago_em, created_at, updated_at.

### Notificação
- id, empresa_id, destinatario, canal, assunto, mensagem, status_envio, enviado_em, created_at.

**Regra transversal:** toda entidade possui vínculo obrigatório com `empresa_id` (exceto registros técnicos estritamente globais de autenticação de borda, quando aplicável).

---

## Telas (Web)

| Tela | Objetivo | Funcionalidades | Componentes principais |
|---|---|---|---|
| Login | Autenticar usuário | Login e recuperação de senha | Formulário, validação, feedback |
| Dashboard | Resumo operacional | KPIs básicos e atalhos | Cards, gráficos básicos, lista de pendências |
| Empresas | Gerenciar tenant | Cadastro/edição da empresa | Formulário e dados cadastrais |
| Usuários | Gestão de acesso | Cadastro, edição, papéis | Tabela, formulário, permissões |
| Clientes | Base de clientes | CRUD e busca | Tabela, filtros, formulário |
| Serviços | Catálogo de serviços | CRUD e preços | Tabela, formulário |
| Agenda | Planejamento operacional | Criar e acompanhar agendamentos | Calendário/lista, filtros, status |
| Ordens de Serviço | Execução de trabalho | Abrir, atualizar, concluir OS | Lista, detalhe, histórico de status |
| Financeiro | Controle de caixa | Lançamentos e pagamentos | Tabela, filtros, indicadores |
| Relatórios | Apoio à decisão | Relatórios operacionais/financeiros | Filtros, tabela, exportação futura |

---

## APIs (Macrodefinição)

| Domínio | Endpoints base | Métodos HTTP | Responsabilidade |
|---|---|---|---|
| Auth | `/auth/login`, `/auth/refresh`, `/auth/forgot-password` | POST | Autenticação e tokens |
| Empresas/Usuários | `/companies`, `/users` | GET, POST, PUT, PATCH | Gestão de tenant e acesso |
| Clientes/Serviços | `/customers`, `/services` | GET, POST, PUT, PATCH, DELETE | Cadastro operacional |
| Agenda | `/appointments` | GET, POST, PUT, PATCH, DELETE | Planejamento de atendimentos |
| Ordens de Serviço | `/service-orders` | GET, POST, PUT, PATCH | Ciclo da OS |
| Financeiro | `/billing`, `/payments` | GET, POST, PUT, PATCH | Receitas e pagamentos |
| Notificações | `/notifications` | GET, POST | Comunicação e rastreio |

Observação: no MVP, esses endpoints serão expostos por um backend único (monólito modular), mantendo separação por domínio em camadas internas.

---

## Segurança

- **JWT** como padrão de autenticação.
- **Permissões por papel** (ex.: admin, gestor, operador).
- **Multi-tenancy obrigatório**: escopo por `empresa_id` em leitura/escrita.
- **Controle de acesso** em nível de rota e recurso.
- Preparação para auditoria de ações críticas.

---

## Roadmap de Entrega (Fases)

1. **Fase 0 - Fundação**
   - Documentação.
   - Arquitetura.
   - Estrutura inicial.
2. **Fase 1 - MVP em Monólito Modular**
   - Autenticação JWT.
   - Empresas.
   - Usuários.
   - Clientes.
   - Serviços.
   - Agenda.
   - Ordens de Serviço.
   - Financeiro.
3. **Fase 2 - Evolução do Produto**
   - Evoluções de funcionalidades (dashboard avançado, notificações, etc.).
   - Extração de módulos para microserviços **somente com necessidade real**.
   - Separação de bancos **somente após critérios técnicos e de negócio**.
   - Comunicação entre serviços apenas quando houver extração efetiva.

---

## Análise de Riscos

### Técnicos
- Complexidade prematura de arquitetura.
- Falhas de segregação multi-tenant.
- Débitos de segurança em autenticação/autorização.

### Arquiteturais
- Fronteiras de domínio mal definidas entre serviços.
- Acoplamento excessivo entre frontend e backend.
- Escalabilidade comprometida por decisões não padronizadas.

### Operacionais
- Ausência de observabilidade mínima para suporte.
- Entrega lenta por excesso de escopo no MVP.
- Dificuldade de onboarding sem documentação atualizada.

---

## Plano de Evolução para Microserviços

### Quando extrair um módulo

- Necessidade de escalar domínio específico de forma independente.
- Alto volume de mudanças em um módulo gerando gargalo no deploy único.
- Necessidade de disponibilidade isolada por capacidade de negócio.
- Evidência de ganho de velocidade ou redução de risco com separação.

### Critérios técnicos

- Fronteira de domínio estável e bem definida.
- Baixo acoplamento com demais módulos.
- Contratos de API claros e versionáveis.
- Plano de migração de dados do schema para banco dedicado.
- Observabilidade mínima para operação distribuída.

### Critérios de negócio

- Impacto mensurável em retenção, receita ou eficiência operacional.
- Justificativa de custo-benefício para operação de múltiplos serviços.
- Priorização validada no roadmap e metas comerciais.

### Benefícios esperados

- Escala seletiva por domínio.
- Maior autonomia de evolução por área funcional.
- Menor risco de regressão em deploys com mudanças localizadas.
- Melhor alinhamento entre arquitetura e crescimento do produto.

---

## Decisões Arquiteturais (justificadas)

1. **Monólito modular como ponto de partida**  
   Justificativa: validação rápida do produto com menor complexidade operacional.

2. **Microserviços como evolução planejada**  
   Justificativa: preservar visão de escala sem antecipar custo de arquitetura distribuída.

3. **PostgreSQL único com schemas por domínio no MVP**  
   Justificativa: simplicidade operacional com separação lógica para futura extração.

4. **Sem REST interno entre módulos no MVP**  
   Justificativa: evitar latência e complexidade desnecessária dentro do mesmo deploy.

5. **Web-first e backend centralizando regras de negócio**  
   Justificativa: garantir consistência de domínio e reuso futuro das APIs.
