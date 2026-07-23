<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>Ordens de Serviço</h1>
        <p>Núcleo operacional do Servix.</p>
      </div>
      <RouterLink class="primary" to="/service-orders/new">Nova ordem</RouterLink>
    </header>

    <ServiceOrderSearchBar v-model="filters" :customers="customers" :services="services" :professionals="professionals" @search="search" @reset="resetFilters" />

    <p v-if="serviceOrderStore.loading">Carregando ordens de serviço...</p>
    <p v-else-if="serviceOrderStore.error" class="error">{{ serviceOrderStore.error }}</p>
    <p v-else-if="serviceOrderStore.message" class="success">{{ serviceOrderStore.message }}</p>

    <div v-else class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Início previsto</th>
            <th>Cliente</th>
            <th>Serviço</th>
            <th>Profissional</th>
            <th>Status</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="serviceOrder in serviceOrderStore.serviceOrders" :key="serviceOrder.id">
            <td>{{ formatDate(serviceOrder.scheduledStart) }}</td>
            <td>{{ serviceOrder.customerName }}</td>
            <td>{{ serviceOrder.serviceName }}</td>
            <td>{{ serviceOrder.professionalName }}</td>
            <td>{{ formatStatus(serviceOrder.status) }}</td>
            <td class="actions">
              <RouterLink :to="`/service-orders/${serviceOrder.id}`">Ver</RouterLink>
              <RouterLink :to="`/service-orders/${serviceOrder.id}/edit`">Editar</RouterLink>
              <RouterLink :to="`/service-orders/${serviceOrder.id}/timeline`">Histórico</RouterLink>
              <button type="button" @click="askDelete(serviceOrder)">Excluir</button>
            </td>
          </tr>
          <tr v-if="serviceOrderStore.serviceOrders.length === 0">
            <td colspan="6">Nenhuma ordem de serviço encontrada.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <BasePagination :page="serviceOrderStore.pagination.page" :total-pages="serviceOrderStore.pagination.totalPages" @change="changePage" />

    <BaseConfirmDialog
      :visible="deleteDialog.visible"
      title="Excluir ordem de serviço"
      :message="`Tem certeza que deseja excluir a ordem de serviço de ${deleteDialog.serviceOrder?.customerName ?? ''}?`"
      confirm-label="Excluir"
      cancel-label="Cancelar"
      @confirm="confirmDelete"
      @cancel="cancelDelete"
    />
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import BaseConfirmDialog from '../../components/common/BaseConfirmDialog.vue'
import BasePagination from '../../components/common/BasePagination.vue'
import ServiceOrderSearchBar from '../../components/serviceorder/ServiceOrderSearchBar.vue'
import { customerApi } from '../../services/customerApi'
import { employeeApi } from '../../services/employeeApi'
import { serviceApi } from '../../services/serviceApi'
import { useServiceOrderStore } from '../../stores/serviceOrderStore'

const serviceOrderStore = useServiceOrderStore()
const customers = ref([])
const services = ref([])
const professionals = ref([])

const filters = reactive({
  filter: '',
  appointmentId: '',
  customerId: '',
  professionalId: '',
  serviceId: '',
  status: '',
  dateFrom: '',
  dateTo: '',
})

const deleteDialog = reactive({
  visible: false,
  serviceOrder: null,
})

onMounted(async () => {
  await Promise.all([loadOptions(), serviceOrderStore.loadServiceOrders()])
})

async function loadOptions() {
  const [customerResponse, serviceResponse, professionalResponse] = await Promise.all([
    customerApi.list({ size: 100 }),
    serviceApi.list({ size: 100 }),
    employeeApi.list({ size: 100 }),
  ])
  customers.value = customerResponse.data.data.content
  services.value = serviceResponse.data.data.content
  professionals.value = professionalResponse.data.data.content
}

async function search() {
  serviceOrderStore.setFilters(filters)
  await serviceOrderStore.loadServiceOrders({ page: 0 })
}

async function resetFilters() {
  Object.assign(filters, {
    filter: '',
    appointmentId: '',
    customerId: '',
    professionalId: '',
    serviceId: '',
    status: '',
    dateFrom: '',
    dateTo: '',
  })
  serviceOrderStore.resetFilters()
  await serviceOrderStore.loadServiceOrders({ page: 0 })
}

async function changePage(page) {
  await serviceOrderStore.loadServiceOrders({ page })
}

function askDelete(serviceOrder) {
  deleteDialog.serviceOrder = serviceOrder
  deleteDialog.visible = true
}

function cancelDelete() {
  deleteDialog.visible = false
  deleteDialog.serviceOrder = null
}

async function confirmDelete() {
  if (!deleteDialog.serviceOrder) {
    return
  }
  await serviceOrderStore.deleteServiceOrder(deleteDialog.serviceOrder.id)
  deleteDialog.visible = false
  deleteDialog.serviceOrder = null
  await serviceOrderStore.loadServiceOrders()
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
