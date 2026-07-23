<template>
  <section class="calendar">
    <header class="toolbar">
      <div class="modes">
        <button type="button" :class="{ active: viewMode === 'day' }" @click="$emit('change-view-mode', 'day')">Dia</button>
        <button type="button" :class="{ active: viewMode === 'week' }" @click="$emit('change-view-mode', 'week')">Semana</button>
        <button type="button" :class="{ active: viewMode === 'month' }" @click="$emit('change-view-mode', 'month')">Mês</button>
      </div>
      <div class="navigator">
        <button type="button" @click="$emit('previous')">Anterior</button>
        <button type="button" @click="$emit('today')">Hoje</button>
        <button type="button" @click="$emit('next')">Próximo</button>
      </div>
    </header>

    <p class="current-date">{{ dateLabel }}</p>
    <p v-if="loading">Carregando agenda...</p>
    <p v-else-if="appointments.length === 0" class="empty">Nenhum agendamento encontrado.</p>
    <div v-else class="items">
      <article v-for="appointment in appointments" :key="appointment.id" class="card">
        <div class="row">
          <strong>{{ appointment.appointmentDate }}</strong>
          <span>{{ appointment.startTime }} - {{ appointment.endTime }}</span>
        </div>
        <p>{{ appointment.customerName }} • {{ appointment.serviceName }} • {{ appointment.employeeName }}</p>
        <p class="status">{{ formatStatus(appointment.status) }}</p>
      </article>
    </div>
  </section>
</template>

<script setup>
defineProps({
  viewMode: { type: String, required: true },
  appointments: { type: Array, default: () => [] },
  dateLabel: { type: String, default: '' },
  loading: { type: Boolean, default: false },
})

defineEmits(['change-view-mode', 'previous', 'next', 'today'])

function formatStatus(status) {
  const labels = {
    SCHEDULED: 'Agendado',
    CONFIRMED: 'Confirmado',
    IN_PROGRESS: 'Em andamento',
    COMPLETED: 'Concluído',
    CANCELLED: 'Cancelado',
    NO_SHOW: 'Não compareceu',
  }
  return labels[status] || status
}
</script>

<style scoped>
.calendar {
  display: grid;
  gap: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.modes,
.navigator {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

button {
  padding: 10px 12px;
}

.active {
  background: #1d4ed8;
  color: #fff;
}

.current-date {
  color: #6b7280;
}

.items {
  display: grid;
  gap: 12px;
}

.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
  padding: 16px;
}

.row {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.status {
  color: #1d4ed8;
}

.empty {
  color: #6b7280;
}
</style>
