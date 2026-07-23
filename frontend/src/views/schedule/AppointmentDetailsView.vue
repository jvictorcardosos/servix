<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ appointment?.customerName ?? 'Agendamento' }}</h1>
        <p>Detalhes, status e ações do compromisso.</p>
      </div>
      <div class="actions">
        <RouterLink class="secondary" :to="`/appointments/${route.params.id}/edit`">Editar</RouterLink>
        <button type="button" class="danger" @click="openDeleteDialog">Excluir</button>
      </div>
    </header>

    <p v-if="appointmentStore.loading">Carregando agendamento...</p>
    <p v-else-if="appointmentStore.error" class="error">{{ appointmentStore.error }}</p>
    <p v-else-if="appointmentStore.message" class="success">{{ appointmentStore.message }}</p>

    <div v-else class="card">
      <div class="grid">
        <div><strong>Serviço:</strong> {{ appointment?.serviceName }}</div>
        <div><strong>Funcionário:</strong> {{ appointment?.employeeName }}</div>
        <div><strong>Data:</strong> {{ appointment?.appointmentDate }}</div>
        <div><strong>Horário:</strong> {{ appointment?.startTime }} - {{ appointment?.endTime }}</div>
        <div><strong>Status:</strong> {{ formatStatus(appointment?.status) }}</div>
        <div><strong>Duração:</strong> {{ appointment?.durationMinutes }} min</div>
      </div>

      <div class="notes">
        <strong>Observações:</strong>
        <p>{{ appointment?.notes || 'Sem observações' }}</p>
      </div>

      <div class="status-actions">
        <button type="button" class="secondary" @click="markCompleted">Concluir</button>
        <button type="button" class="secondary" @click="markCancelled">Cancelar</button>
      </div>
    </div>

    <BaseConfirmDialog
      :visible="deleteDialog"
      title="Excluir agendamento"
      :message="`Tem certeza que deseja excluir o agendamento de ${appointment?.customerName ?? ''}?`"
      confirm-label="Excluir"
      cancel-label="Cancelar"
      @confirm="confirmDelete"
      @cancel="deleteDialog = false"
    />
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import BaseConfirmDialog from '../../components/common/BaseConfirmDialog.vue'
import { useAppointmentStore } from '../../stores/appointmentStore'

const route = useRoute()
const router = useRouter()
const appointmentStore = useAppointmentStore()
const deleteDialog = ref(false)

const appointment = computed(() => appointmentStore.currentAppointment)

onMounted(async () => {
  await appointmentStore.loadAppointment(route.params.id)
})

function openDeleteDialog() {
  deleteDialog.value = true
}

async function markCompleted() {
  try {
    await appointmentStore.completeAppointment(route.params.id)
    await appointmentStore.loadAppointment(route.params.id)
  } catch {
    return
  }
}

async function markCancelled() {
  try {
    await appointmentStore.cancelAppointment(route.params.id)
    await appointmentStore.loadAppointment(route.params.id)
  } catch {
    return
  }
}

async function confirmDelete() {
  try {
    await appointmentStore.deleteAppointment(route.params.id)
    deleteDialog.value = false
    await router.push('/appointments/list')
  } catch {
    return
  }
}

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
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header,
.actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 20px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.notes {
  margin-top: 16px;
}

.secondary,
.danger {
  padding: 10px 16px;
  border-radius: 10px;
  text-decoration: none;
  border: none;
}

.secondary {
  background: #e5e7eb;
  color: #111827;
}

.danger {
  background: #dc2626;
  color: #fff;
}

.status-actions {
  justify-content: flex-end;
  margin-top: 20px;
}

.error {
  color: #b91c1c;
}

.success {
  color: #166534;
}
</style>
