<template>
  <section class="foundation">
    <h1>Servix</h1>
    <p>Base técnica pronta. Os módulos de Clientes e Serviços já estão disponíveis.</p>
    <div class="actions">
      <RouterLink class="cta" to="/customers">Abrir clientes</RouterLink>
      <RouterLink class="cta secondary" to="/services">Abrir serviços</RouterLink>
    </div>
  </section>
</template>

<style scoped>
.foundation {
  background: #ffffff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 24px;
}

.cta {
  display: inline-block;
  margin-top: 16px;
  padding: 10px 16px;
  border-radius: 10px;
  background: #1d4ed8;
  color: #ffffff;
  text-decoration: none;
}

.secondary {
  background: #e5e7eb;
  color: #111827;
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
