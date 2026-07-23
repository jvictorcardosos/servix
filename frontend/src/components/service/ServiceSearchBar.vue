<template>
  <form class="filters" @submit.prevent="$emit('search')">
    <input v-model="model.filter" type="text" placeholder="Busca geral" />
    <input v-model="model.name" type="text" placeholder="Nome" />
    <input v-model="model.minPrice" type="number" step="0.01" min="0" placeholder="Preço mínimo" />
    <input v-model="model.maxPrice" type="number" step="0.01" min="0" placeholder="Preço máximo" />
    <input v-model="model.minDuration" type="number" min="0" placeholder="Duração mínima" />
    <input v-model="model.maxDuration" type="number" min="0" placeholder="Duração máxima" />
    <select v-model="model.active">
      <option value="">Todos</option>
      <option value="true">Ativo</option>
      <option value="false">Inativo</option>
    </select>
    <div class="actions">
      <button type="submit">Pesquisar</button>
      <button type="button" @click="$emit('reset')">Limpar</button>
    </div>
  </form>
</template>

<script setup>
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
