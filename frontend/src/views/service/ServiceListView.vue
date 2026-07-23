<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>Serviços</h1>
        <p>Cadastro, filtros e controle do catálogo da empresa.</p>
      </div>
      <RouterLink class="primary" to="/services/new">Novo serviço</RouterLink>
    </header>

    <ServiceSearchBar v-model="filters" @search="search" @reset="resetFilters" />

    <p v-if="serviceStore.loading">Carregando serviços...</p>
    <p v-else-if="serviceStore.error" class="error">{{ serviceStore.error }}</p>
    <p v-else-if="serviceStore.message" class="success">{{ serviceStore.message }}</p>

    <div v-else class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Nome</th>
            <th>Duração</th>
            <th>Preço</th>
            <th>Status</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="service in serviceStore.services" :key="service.id">
            <td>{{ service.name }}</td>
            <td>{{ service.durationMinutes }} min</td>
            <td>{{ formatCurrency(service.price) }}</td>
            <td>{{ service.active ? 'Ativo' : 'Inativo' }}</td>
            <td class="actions">
              <RouterLink :to="`/services/${service.id}`">Ver</RouterLink>
              <RouterLink :to="`/services/${service.id}/edit`">Editar</RouterLink>
              <button type="button" @click="askDelete(service)">Excluir</button>
            </td>
          </tr>
          <tr v-if="serviceStore.services.length === 0">
            <td colspan="5">Nenhum serviço encontrado.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <BasePagination
      :page="serviceStore.pagination.page"
      :total-pages="serviceStore.pagination.totalPages"
      @change="changePage"
    />

    <BaseConfirmDialog
      :visible="deleteDialog.visible"
      title="Excluir serviço"
      :message="`Tem certeza que deseja excluir ${deleteDialog.service?.name ?? ''}?`"
      confirm-label="Excluir"
      cancel-label="Cancelar"
      @confirm="confirmDelete"
      @cancel="cancelDelete"
    />
  </section>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import BaseConfirmDialog from '../../components/common/BaseConfirmDialog.vue'
import BasePagination from '../../components/common/BasePagination.vue'
import ServiceSearchBar from '../../components/service/ServiceSearchBar.vue'
import { useServiceStore } from '../../stores/serviceStore'

const serviceStore = useServiceStore()

const filters = reactive({
  filter: '',
  name: '',
  active: '',
  minPrice: '',
  maxPrice: '',
  minDuration: '',
  maxDuration: '',
})

const deleteDialog = reactive({
  visible: false,
  service: null,
})

onMounted(async () => {
  await serviceStore.loadServices()
})

async function search() {
  serviceStore.setFilters(filters)
  await serviceStore.loadServices({ page: 0 })
}

async function resetFilters() {
  Object.assign(filters, {
    filter: '',
    name: '',
    active: '',
    minPrice: '',
    maxPrice: '',
    minDuration: '',
    maxDuration: '',
  })
  serviceStore.resetFilters()
  await serviceStore.loadServices({ page: 0 })
}

async function changePage(page) {
  await serviceStore.loadServices({ page })
}

function askDelete(service) {
  deleteDialog.service = service
  deleteDialog.visible = true
}

function cancelDelete() {
  deleteDialog.visible = false
  deleteDialog.service = null
}

async function confirmDelete() {
  if (!deleteDialog.service) {
    return
  }
  await serviceStore.deleteService(deleteDialog.service.id)
  deleteDialog.visible = false
  deleteDialog.service = null
  await serviceStore.loadServices()
}

function formatCurrency(value) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value)
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.primary {
  padding: 10px 16px;
  background: #1d4ed8;
  color: #fff;
  text-decoration: none;
  border-radius: 10px;
}

.table-wrapper {
  overflow-x: auto;
  background: #fff;
  border-radius: 16px;
  border: 1px solid #e5e7eb;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  padding: 12px;
  border-bottom: 1px solid #e5e7eb;
  text-align: left;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.error {
  color: #b91c1c;
}

.success {
  color: #166534;
}
</style>
