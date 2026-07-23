<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ serviceOrder?.customerName ?? 'Ordem de serviço' }}</h1>
        <p>Execução operacional do atendimento.</p>
      </div>
      <div class="actions">
        <RouterLink class="secondary" :to="`/service-orders/${route.params.id}/edit`">Editar</RouterLink>
        <RouterLink class="secondary" :to="`/service-orders/${route.params.id}/timeline`">Histórico</RouterLink>
        <button type="button" class="danger" @click="openDeleteDialog">Excluir</button>
      </div>
    </header>

    <p v-if="serviceOrderStore.loading">Carregando ordem de serviço...</p>
    <p v-else-if="serviceOrderStore.error" class="error">{{ serviceOrderStore.error }}</p>
    <p v-else-if="serviceOrderStore.message" class="success">{{ serviceOrderStore.message }}</p>

    <div v-else class="card">
      <div class="grid">
        <div><strong>Serviço:</strong> {{ serviceOrder?.serviceName }}</div>
        <div><strong>Profissional:</strong> {{ serviceOrder?.professionalName }}</div>
        <div><strong>Início previsto:</strong> {{ formatDate(serviceOrder?.scheduledStart) }}</div>
        <div><strong>Fim previsto:</strong> {{ formatDate(serviceOrder?.scheduledEnd) }}</div>
        <div><strong>Status:</strong> {{ formatStatus(serviceOrder?.status) }}</div>
        <div><strong>Duração prevista:</strong> {{ serviceOrder?.estimatedDuration }} min</div>
        <div><strong>Duração real:</strong> {{ serviceOrder?.actualDuration ?? '-' }} min</div>
      </div>

      <div class="notes">
        <strong>Observações:</strong>
        <p>{{ serviceOrder?.observations || 'Sem observações' }}</p>
      </div>

      <div class="status-actions">
        <button type="button" class="secondary" @click="start" :disabled="!canStart">Iniciar</button>
        <button type="button" class="secondary" @click="pause" :disabled="!canPause">Pausar</button>
        <button type="button" class="secondary" @click="resume" :disabled="!canResume">Retomar</button>
        <button type="button" class="secondary" @click="finish" :disabled="!canFinish">Concluir</button>
        <button type="button" class="secondary" @click="cancel" :disabled="!canCancel">Cancelar</button>
      </div>
    </div>

    <BaseConfirmDialog
      :visible="deleteDialog"
      title="Excluir ordem de serviço"
      :message="`Tem certeza que deseja excluir a ordem de serviço de ${serviceOrder?.customerName ?? ''}?`"
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
import { useServiceOrderStore } from '../../stores/serviceOrderStore'

const route = useRoute()
const router = useRouter()
const serviceOrderStore = useServiceOrderStore()
const deleteDialog = ref(false)

const serviceOrder = computed(() => serviceOrderStore.currentServiceOrder)
const canStart = computed(() => serviceOrder.value && ['OPEN', 'CONFIRMED'].includes(serviceOrder.value.status))
const canPause = computed(() => serviceOrder.value && serviceOrder.value.status === 'IN_PROGRESS')
const canResume = computed(() => serviceOrder.value && serviceOrder.value.status === 'PAUSED')
const canFinish = computed(() => serviceOrder.value && ['IN_PROGRESS', 'PAUSED'].includes(serviceOrder.value.status))
const canCancel = computed(() => serviceOrder.value && !['COMPLETED', 'CANCELLED'].includes(serviceOrder.value.status))

onMounted(async () => {
  await serviceOrderStore.loadServiceOrder(route.params.id)
})

function openDeleteDialog() {
  deleteDialog.value = true
}

async function start() {
  await serviceOrderStore.startServiceOrder(route.params.id)
  await serviceOrderStore.loadServiceOrder(route.params.id)
}

async function pause() {
  await serviceOrderStore.pauseServiceOrder(route.params.id)
  await serviceOrderStore.loadServiceOrder(route.params.id)
}

async function resume() {
  await serviceOrderStore.resumeServiceOrder(route.params.id)
  await serviceOrderStore.loadServiceOrder(route.params.id)
}

async function finish() {
  await serviceOrderStore.finishServiceOrder(route.params.id)
  await serviceOrderStore.loadServiceOrder(route.params.id)
}

async function cancel() {
  await serviceOrderStore.cancelServiceOrder(route.params.id)
  await serviceOrderStore.loadServiceOrder(route.params.id)
}

async function confirmDelete() {
  await serviceOrderStore.deleteServiceOrder(route.params.id)
  deleteDialog.value = false
  await router.push('/service-orders')
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
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

.error {
  color: #b91c1c;
}

.success {
  color: #166534;
}
</style>
