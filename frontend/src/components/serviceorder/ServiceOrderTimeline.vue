<template>
  <section class="timeline">
    <article v-for="item in history" :key="item.id" class="entry">
      <div class="dot"></div>
      <div class="content">
        <strong>{{ formatStatus(item.newStatus) }}</strong>
        <p v-if="item.previousStatus">De {{ formatStatus(item.previousStatus) }}</p>
        <p>{{ formatDate(item.changedAt) }}</p>
        <p v-if="item.observation">{{ item.observation }}</p>
      </div>
    </article>
  </section>
</template>

<script setup>
defineProps({
  history: { type: Array, default: () => [] },
})

function formatStatus(status) {
  const labels = {
    OPEN: 'Aberta',
    CONFIRMED: 'Confirmada',
    IN_PROGRESS: 'Em andamento',
    PAUSED: 'Pausada',
    COMPLETED: 'Concluída',
    CANCELLED: 'Cancelada',
    NO_SHOW: 'Não compareceu',
  }
  return labels[status] || status
}

function formatDate(value) {
  if (!value) {
    return ''
  }
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value))
}
</script>

<style scoped>
.timeline {
  display: grid;
  gap: 16px;
}

.entry {
  display: grid;
  grid-template-columns: 16px 1fr;
  gap: 12px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #1d4ed8;
  margin-top: 4px;
}

.content {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 16px;
}

.content p {
  margin: 4px 0 0;
  color: #6b7280;
}
</style>
