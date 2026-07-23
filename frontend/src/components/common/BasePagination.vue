<template>
  <div class="pagination" v-if="totalPages > 1">
    <button :disabled="page <= 0" @click="$emit('change', page - 1)">Anterior</button>
    <span>Página {{ page + 1 }} de {{ totalPages }}</span>
    <button :disabled="page + 1 >= totalPages" @click="$emit('change', page + 1)">Próxima</button>
  </div>
</template>

<script setup>
defineProps({
  page: { type: Number, required: true },
  totalPages: { type: Number, required: true },
})

defineEmits(['change'])
</script>

<style scoped>
.pagination {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-top: 16px;
}

button {
  padding: 8px 12px;
}
</style>
