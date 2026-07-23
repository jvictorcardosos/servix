<template>
  <form class="filters" @submit.prevent="$emit('search')">
    <input v-model="model.filter" type="text" placeholder="Busca geral" />
    <select v-model="model.status">
      <option value="">Todos os status</option>
      <option value="OPEN">Aberta</option>
      <option value="CONFIRMED">Confirmada</option>
      <option value="IN_PROGRESS">Em andamento</option>
      <option value="PAUSED">Pausada</option>
      <option value="COMPLETED">Concluída</option>
      <option value="CANCELLED">Cancelada</option>
      <option value="NO_SHOW">Não compareceu</option>
    </select>
    <select v-model="model.customerId">
      <option value="">Todos os clientes</option>
      <option v-for="customer in customers" :key="customer.id" :value="customer.id">
        {{ customer.nome }}
      </option>
    </select>
    <select v-model="model.professionalId">
      <option value="">Todos os profissionais</option>
      <option v-for="professional in professionals" :key="professional.id" :value="professional.id">
        {{ professional.name }}
      </option>
    </select>
    <select v-model="model.serviceId">
      <option value="">Todos os serviços</option>
      <option v-for="service in services" :key="service.id" :value="service.id">
        {{ service.name }}
      </option>
    </select>
    <input v-model="model.dateFrom" type="date" />
    <input v-model="model.dateTo" type="date" />
    <div class="actions">
      <button type="submit">Pesquisar</button>
      <button type="button" @click="$emit('reset')">Limpar</button>
    </div>
  </form>
</template>

<script setup>
defineProps({
  customers: { type: Array, default: () => [] },
  services: { type: Array, default: () => [] },
  professionals: { type: Array, default: () => [] },
})

const model = defineModel({ type: Object, required: true })

defineEmits(['search', 'reset'])
</script>

<style scoped>
.filters {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

input,
select,
button {
  padding: 10px 12px;
}
</style>
