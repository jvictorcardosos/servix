<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>Agenda</h1>
        <p>Visualização diária, semanal e mensal dos agendamentos.</p>
      </div>
      <div class="actions">
        <RouterLink class="secondary" to="/appointments/list">Lista</RouterLink>
        <RouterLink class="primary" to="/appointments/new">Novo agendamento</RouterLink>
      </div>
    </header>

    <AppointmentSearchBar v-model="filters" :customers="customers" :services="services" :employees="employees" @search="refreshCalendar" @reset="resetFilters" />

    <ScheduleCalendar
      :view-mode="appointmentStore.calendarMode"
      :appointments="appointmentStore.calendarAppointments"
      :date-label="dateLabel"
      :loading="appointmentStore.loading"
      @change-view-mode="changeViewMode"
      @previous="previousPeriod"
      @next="nextPeriod"
      @today="goToToday"
    />
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import AppointmentSearchBar from '../../components/schedule/AppointmentSearchBar.vue'
import ScheduleCalendar from '../../components/schedule/ScheduleCalendar.vue'
import { customerApi } from '../../services/customerApi'
import { employeeApi } from '../../services/employeeApi'
import { serviceApi } from '../../services/serviceApi'
import { useAppointmentStore } from '../../stores/appointmentStore'

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
})

const dateLabel = computed(() => {
  const date = new Date(`${appointmentStore.calendarDate}T00:00:00`)
  return new Intl.DateTimeFormat('pt-BR', { dateStyle: 'full' }).format(date)
})

onMounted(async () => {
  await Promise.all([loadOptions(), refreshCalendar()])
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

async function refreshCalendar() {
  appointmentStore.setFilters(filters)
  await appointmentStore.loadCalendarAppointments(appointmentStore.calendarMode, appointmentStore.calendarDate, filters)
}

async function changeViewMode(mode) {
  await appointmentStore.loadCalendarAppointments(mode, appointmentStore.calendarDate, filters)
}

function previousPeriod() {
  const nextDate = shiftDate(-1)
  appointmentStore.loadCalendarAppointments(appointmentStore.calendarMode, nextDate, filters)
}

function nextPeriod() {
  const nextDate = shiftDate(1)
  appointmentStore.loadCalendarAppointments(appointmentStore.calendarMode, nextDate, filters)
}

function goToToday() {
  const today = new Date().toISOString().slice(0, 10)
  appointmentStore.loadCalendarAppointments(appointmentStore.calendarMode, today, filters)
}

function resetFilters() {
  Object.assign(filters, {
    filter: '',
    customerId: '',
    serviceId: '',
    employeeId: '',
    status: '',
  })
  appointmentStore.resetFilters()
  refreshCalendar()
}

function shiftDate(direction) {
  const current = new Date(`${appointmentStore.calendarDate}T00:00:00`)
  const days = appointmentStore.calendarMode === 'month' ? 30 : appointmentStore.calendarMode === 'week' ? 7 : 1
  current.setDate(current.getDate() + days * direction)
  return current.toISOString().slice(0, 10)
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
  gap: 12px;
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
</style>
