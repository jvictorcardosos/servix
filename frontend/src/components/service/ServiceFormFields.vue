<template>
  <div class="fields">
    <label>
      Nome
      <input v-model="model.name" type="text" required />
    </label>
    <label>
      Descrição
      <textarea v-model="model.description" rows="4" />
    </label>
    <label>
      Duração em minutos
      <input v-model.number="model.durationMinutes" type="number" min="1" required />
    </label>
    <label>
      Preço
      <input v-model.number="model.price" type="number" min="0.01" step="0.01" required />
    </label>
    <label class="checkbox">
      <input v-model="model.active" type="checkbox" />
      Ativo
    </label>
  </div>
</template>

<script setup>
const model = defineModel({ type: Object, required: true })
</script>

<style scoped>
.fields {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.checkbox {
  flex-direction: row;
  align-items: center;
}

textarea,
input {
  padding: 10px 12px;
}
</style>
