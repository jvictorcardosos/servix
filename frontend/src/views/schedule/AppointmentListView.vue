<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>Agendamentos</h1>
        <p>Pesquisa, filtros e paginação dos compromissos.</p>
      </div>
      <div class="actions">
        <RouterLink class="secondary" to="/appointments">Agenda</RouterLink>
        <RouterLink class="primary" to="/appointments/new">Novo agendamento</RouterLink>
      </div>
    </header>

    <AppointmentSearchBar v-model="filters" :customers="customers" :services="services" :employees="employees" @search="search" @reset="resetFilters" />

    <p v-if="appointmentStore.loading">Carregando agendamentos...</p>
    <p v-else-if="appointmentStore.error" class="error">{{ appointmentStore.error }}</p>
    <p v-else-if="appointmentStore.message" class="success">{{ appointmentStore.message }}</p>

    <div v-else class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Data</th>
            <th>Horário</th>
            <th>Cliente</th>
            <th>Serviço</th>
            <th>Funcionário</th>
            <th>Status</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="appointment in appointmentStore.appointments" :key="appointment.id">
            <td>{{ appointment.appointmentDate }}</td>
            <td>{{ appointment.startTime }} - {{ appointment.endTime }}</td>
            <td>{{ appointment.customerName }}</td>
            <td>{{ appointment.serviceName }}</td>
            <td>{{ appointment.employeeName }}</td>
            <td>{{ formatStatus(appointment.status) }}</td>
            <td class="actions">
              <RouterLink :to="`/appointments/${appointment.id}`">Ver</RouterLink>
              <RouterLink :to="`/appointments/${appointment.id}/edit`">Editar</RouterLink>
              <button type="button" @click="askCancel(appointment)" :disabled="appointment.status === 'CANCELLED'">Cancelar</button>
              <button type="button" @click="askDelete(appointment)">Excluir</button>
            </td>
          </tr>
          <tr v-if="appointmentStore.appointments.length === 0">
            <td colspan="7">Nenhum agendamento encontrado.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <BasePagination
      :page="appointmentStore.pagination.page"
      :total-pages="appointmentStore.pagination.totalPages"
      @change="changePage"
    />

    <BaseConfirmDialog
      :visible="cancelDialog.visible"
      title="Cancelar agendamento"
      :message="`Tem certeza que deseja cancelar o agendamento de ${cancelDialog.appointment?.customerName ?? ''}?`"
      confirm-label="Cancelar"
      cancel-label="Voltar"
      @confirm="confirmCancel"
      @cancel="cancelCancel"
    />

    <BaseConfirmDialog
      :visible="deleteDialog.visible"
      title="Excluir agendamento"
      :message="`Tem certeza que deseja excluir o agendamento de ${deleteDialog.appointment?.customerName ?? ''}?`"
      confirm-label="Excluir"
      cancel-label="Voltar"
      @confirm="confirmDelete"
      @cancel="cancelDelete"
    />
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import BaseConfirmDialog from '../../components/common/BaseConfirmDialog.vue'
import BasePagination from '../../components/common/BasePagination.vue'
import AppointmentSearchBar from '../../components/schedule/AppointmentSearchBar.vue'
import { useAppointmentStore } from '../../stores/appointmentStore'
import { customerApi } from '../../services/customerApi'
import { employeeApi } from '../../services/employeeApi'
import { serviceApi } from '../../services/serviceApi'

const appointmentStore = useAppointmentStore()
const customers = ref([])
const services = ref([])
const employees = ref([])

const filters = reactive({
  filter: '',
  customerId: '',
  serviceId: '',
  employeeId: '',
  status: '',
  dateFrom: '',
  dateTo: '',
})

const cancelDialog = reactive({
  visible: false,
  appointment: null,
})

const deleteDialog = reactive({
  visible: false,
  appointment: null,
})

onMounted(async () => {
  await Promise.all([loadOptions(), appointmentStore.loadAppointments()])
})

async function loadOptions() {
  const [customerResponse, serviceResponse, employeeResponse] = await Promise.all([
    customerApi.list({ size: 100 }),
    serviceApi.list({ size: 100 }),
    employeeApi.list({ size: 100 }),
  ])
  customers.value = customerResponse.data.data.content
  services.value = serviceResponse.data.data.content
  employees.value = employeeResponse.data.data.content
}

async function search() {
  appointmentStore.setFilters(filters)
  await appointmentStore.loadAppointments({ page: 0 })
}

async function resetFilters() {
  Object.assign(filters, {
    filter: '',
    customerId: '',
    serviceId: '',
    employeeId: '',
    status: '',
    dateFrom: '',
    dateTo: '',
  })
  appointmentStore.resetFilters()
  await appointmentStore.loadAppointments({ page: 0 })
}

async function changePage(page) {
  await appointmentStore.loadAppointments({ page })
}

function askCancel(appointment) {
  cancelDialog.appointment = appointment
  cancelDialog.visible = true
}

function cancelCancel() {
  cancelDialog.visible = false
  cancelDialog.appointment = null
}

async function confirmCancel() {
  if (!cancelDialog.appointment) {
    return
  }
  await appointmentStore.cancelAppointment(cancelDialog.appointment.id)
  cancelDialog.visible = false
  cancelDialog.appointment = null
  await appointmentStore.loadAppointments()
}

function askDelete(appointment) {
  deleteDialog.appointment = appointment
  deleteDialog.visible = true
}

function cancelDelete() {
  deleteDialog.visible = false
  deleteDialog.appointment = null
}

async function confirmDelete() {
  if (!deleteDialog.appointment) {
    return
  }
  await appointmentStore.deleteAppointment(deleteDialog.appointment.id)
  deleteDialog.visible = false
  deleteDialog.appointment = null
  await appointmentStore.loadAppointments()
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

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.primary,
.secondary {
  padding: 10px 16px;
  border-radius: 10px;
  text-decoration: none;
}

.primary {
  background: #1d4ed8;
  color: #fff;
}

.secondary {
  background: #e5e7eb;
  color: #111827;
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

.error {
  color: #b91c1c;
}

.success {
  color: #166534;
}
</style>
