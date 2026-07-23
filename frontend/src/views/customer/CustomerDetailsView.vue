<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ customer?.nome ?? 'Cliente' }}</h1>
        <p>Detalhes, status e ações do cliente.</p>
      </div>
      <div class="actions">
        <RouterLink class="secondary" :to="`/customers/${route.params.id}/edit`">Editar</RouterLink>
        <button type="button" class="danger" @click="openDeleteDialog">Excluir</button>
      </div>
    </header>

    <p v-if="customerStore.loading">Carregando cliente...</p>
    <p v-else-if="customerStore.error" class="error">{{ customerStore.error }}</p>

    <div v-else class="card">
      <div class="grid">
        <div><strong>Documento:</strong> {{ customer?.cpfCnpj }}</div>
        <div><strong>Email:</strong> {{ customer?.email }}</div>
        <div><strong>Telefone:</strong> {{ customer?.telefone }}</div>
        <div><strong>Cidade:</strong> {{ customer?.cidade }}</div>
        <div><strong>Estado:</strong> {{ customer?.estado }}</div>
        <div><strong>Status:</strong> {{ customer?.ativo ? 'Ativo' : 'Inativo' }}</div>
      </div>

      <div class="actions status-actions">
        <button type="button" class="secondary" @click="toggleStatus">
          {{ customer?.ativo ? 'Desativar' : 'Ativar' }}
        </button>
      </div>
    </div>

    <BaseConfirmDialog
      :visible="deleteDialog"
      title="Excluir cliente"
      :message="`Tem certeza que deseja excluir ${customer?.nome ?? ''}?`"
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
import { useCustomerStore } from '../../stores/customerStore'

const route = useRoute()
const router = useRouter()
const customerStore = useCustomerStore()
const deleteDialog = ref(false)

const customer = computed(() => customerStore.currentCustomer)

onMounted(async () => {
  await customerStore.loadCustomer(route.params.id)
})

function openDeleteDialog() {
  deleteDialog.value = true
}

async function toggleStatus() {
  await customerStore.updateCustomerStatus(route.params.id, !customer.value.ativo)
  await customerStore.loadCustomer(route.params.id)
}

async function confirmDelete() {
  await customerStore.deleteCustomer(route.params.id)
  deleteDialog.value = false
  await router.push('/customers')
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
</style>
