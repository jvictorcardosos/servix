<template>
  <section class="foundation">
    <h1>Servix</h1>
    <p>Base técnica pronta. Agora o módulo de Clientes já está disponível.</p>
    <RouterLink class="cta" to="/customers">Abrir clientes</RouterLink>
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
</style>
