<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ isEdit ? 'Editar agendamento' : 'Novo agendamento' }}</h1>
        <p>{{ isEdit ? 'Atualize os dados do agendamento.' : 'Cadastre um novo agendamento.' }}</p>
      </div>
    </header>

    <p v-if="appointmentStore.loading">Carregando agendamento...</p>
    <p v-else-if="appointmentStore.error" class="error">{{ appointmentStore.error }}</p>
    <p v-else-if="appointmentStore.message" class="success">{{ appointmentStore.message }}</p>

    <form class="card" @submit.prevent="save">
      <AppointmentFormFields v-model="form" :customers="customers" :services="services" :employees="employees" />

      <div class="actions">
        <RouterLink class="secondary" to="/appointments/list">Voltar</RouterLink>
        <button type="submit" class="primary" :disabled="appointmentStore.saving || appointmentStore.loading">
          {{ appointmentStore.saving ? 'Salvando...' : 'Salvar' }}
        </button>
      </div>
    </form>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppointmentFormFields from '../../components/schedule/AppointmentFormFields.vue'
import { useAppointmentStore } from '../../stores/appointmentStore'
import { customerApi } from '../../services/customerApi'
import { employeeApi } from '../../services/employeeApi'
import { serviceApi } from '../../services/serviceApi'

const route = useRoute()
const router = useRouter()
const appointmentStore = useAppointmentStore()
const customers = ref([])
const services = ref([])
const employees = ref([])

const form = reactive({
  customerId: '',
  serviceId: '',
  employeeId: '',
  appointmentDate: '',
  startTime: '08:00',
  notes: '',
})

const isEdit = computed(() => Boolean(route.params.id))

onMounted(async () => {
  await loadOptions()

  if (!isEdit.value) {
    appointmentStore.resetCurrentAppointment()
    return
  }

  const appointment = await appointmentStore.loadAppointment(route.params.id)
  Object.assign(form, appointment)
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

async function save() {
  try {
    if (isEdit.value) {
      await appointmentStore.updateAppointment(route.params.id, form)
    } else {
      await appointmentStore.createAppointment(form)
    }
    await router.push('/appointments/list')
  } catch {
    return
  }
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 20px;
}

.page-header p {
  color: #6b7280;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
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
  border: none;
}

.secondary {
  background: #e5e7eb;
  color: #111827;
}

.error {
  color: #b91c1c;
}

.success {
  color: #166534;
}
</style>
