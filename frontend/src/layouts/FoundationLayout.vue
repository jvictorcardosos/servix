<template>
  <div class="app-shell">
    <header class="app-header">
      <div>
        <strong>Servix</strong>
        <p>Gestão completa para prestadores de serviço</p>
      </div>
      <nav class="app-nav">
        <RouterLink to="/">Início</RouterLink>
        <RouterLink to="/customers">Clientes</RouterLink>
        <RouterLink to="/customers/new">Novo cliente</RouterLink>
        <RouterLink to="/services">Serviços</RouterLink>
        <RouterLink to="/services/new">Novo serviço</RouterLink>
        <RouterLink to="/employees">Funcionários</RouterLink>
        <RouterLink to="/employees/new">Novo funcionário</RouterLink>
        <RouterLink to="/appointments">Agenda</RouterLink>
        <RouterLink to="/appointments/list">Agendamentos</RouterLink>
        <RouterLink to="/appointments/new">Novo agendamento</RouterLink>
        <RouterLink to="/service-orders">Ordens de serviço</RouterLink>
        <RouterLink to="/service-orders/new">Nova OS</RouterLink>
      </nav>
    </header>

    <main class="app-content">
      <RouterView />
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #f7f9fc;
}

.app-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: center;
  padding: 20px 24px;
  background: #ffffff;
  border-bottom: 1px solid #e5e7eb;
}

.app-header p {
  margin: 4px 0 0;
  color: #6b7280;
}

.app-nav {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.app-nav a {
  text-decoration: none;
  color: #1d4ed8;
  font-weight: 600;
}

.app-content {
  padding: 24px;
}
</style>
