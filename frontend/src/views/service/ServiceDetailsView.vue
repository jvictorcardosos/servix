<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ service?.name ?? 'Serviço' }}</h1>
        <p>Detalhes, status e ações do serviço.</p>
      </div>
      <div class="actions">
        <RouterLink class="secondary" :to="`/services/${route.params.id}/edit`">Editar</RouterLink>
        <button type="button" class="danger" @click="openDeleteDialog">Excluir</button>
      </div>
    </header>

    <p v-if="serviceStore.loading">Carregando serviço...</p>
    <p v-else-if="serviceStore.error" class="error">{{ serviceStore.error }}</p>
    <p v-else-if="serviceStore.message" class="success">{{ serviceStore.message }}</p>

    <div v-else class="card">
      <div class="grid">
        <div><strong>Duração:</strong> {{ service?.durationMinutes }} min</div>
        <div><strong>Preço:</strong> {{ formatCurrency(service?.price) }}</div>
        <div><strong>Status:</strong> {{ service?.active ? 'Ativo' : 'Inativo' }}</div>
      </div>

      <div class="description">
        <strong>Descrição:</strong>
        <p>{{ service?.description || 'Sem descrição' }}</p>
      </div>

      <div class="actions status-actions">
        <button type="button" class="secondary" @click="toggleStatus">
          {{ service?.active ? 'Desativar' : 'Ativar' }}
        </button>
      </div>
    </div>

    <BaseConfirmDialog
      :visible="deleteDialog"
      title="Excluir serviço"
      :message="`Tem certeza que deseja excluir ${service?.name ?? ''}?`"
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
import { useServiceStore } from '../../stores/serviceStore'

const route = useRoute()
const router = useRouter()
const serviceStore = useServiceStore()
const deleteDialog = ref(false)

const service = computed(() => serviceStore.currentService)

onMounted(async () => {
  await serviceStore.loadService(route.params.id)
})

function openDeleteDialog() {
  deleteDialog.value = true
}

async function toggleStatus() {
  try {
    await serviceStore.updateServiceStatus(route.params.id, !service.value.active)
    await serviceStore.loadService(route.params.id)
  } catch {
    return
  }
}

async function confirmDelete() {
  try {
    await serviceStore.deleteService(route.params.id)
    deleteDialog.value = false
    await router.push('/services')
  } catch {
    return
  }
}

function formatCurrency(value) {
  if (value == null) {
    return '-'
  }
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value)
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

.description {
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
