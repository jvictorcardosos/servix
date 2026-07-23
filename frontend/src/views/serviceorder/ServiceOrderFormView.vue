<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ isEdit ? 'Editar ordem de serviço' : 'Nova ordem de serviço' }}</h1>
        <p>{{ appointmentLocked ? 'Ordem vinculada a um agendamento.' : 'Cadastre uma ordem manual ou a partir de um agendamento.' }}</p>
      </div>
    </header>

    <p v-if="serviceOrderStore.loading">Carregando ordem de serviço...</p>
    <p v-else-if="serviceOrderStore.error" class="error">{{ serviceOrderStore.error }}</p>
    <p v-else-if="serviceOrderStore.message" class="success">{{ serviceOrderStore.message }}</p>

    <form class="card" @submit.prevent="save">
      <ServiceOrderFormFields v-model="form" :customers="customers" :services="services" :professionals="professionals" :appointment-locked="appointmentLocked" />

      <div class="actions">
        <RouterLink class="secondary" to="/service-orders">Voltar</RouterLink>
        <button type="submit" class="primary" :disabled="serviceOrderStore.saving || serviceOrderStore.loading">
          {{ serviceOrderStore.saving ? 'Salvando...' : 'Salvar' }}
        </button>
      </div>
    </form>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ServiceOrderFormFields from '../../components/serviceorder/ServiceOrderFormFields.vue'
import { appointmentApi } from '../../services/appointmentApi'
import { customerApi } from '../../services/customerApi'
import { employeeApi } from '../../services/employeeApi'
import { serviceApi } from '../../services/serviceApi'
import { useServiceOrderStore } from '../../stores/serviceOrderStore'

const route = useRoute()
const router = useRouter()
const serviceOrderStore = useServiceOrderStore()
const customers = ref([])
const services = ref([])
const professionals = ref([])
const appointmentLocked = ref(false)
const appointmentId = computed(() => route.query.appointmentId || '')

const form = reactive({
  appointmentId: '',
  customerId: '',
  professionalId: '',
  serviceId: '',
  scheduledStart: '',
  observations: '',
})

const isEdit = computed(() => Boolean(route.params.id))

onMounted(async () => {
  await loadOptions()
  if (appointmentId.value) {
    await loadAppointmentContext(appointmentId.value)
  }

  if (!isEdit.value) {
    if (!appointmentId.value) {
      serviceOrderStore.resetCurrentServiceOrder()
    }
    return
  }

  const serviceOrder = await serviceOrderStore.loadServiceOrder(route.params.id)
  Object.assign(form, {
    appointmentId: serviceOrder.appointmentId || '',
    customerId: serviceOrder.customerId || '',
    professionalId: serviceOrder.professionalId || '',
    serviceId: serviceOrder.serviceId || '',
    scheduledStart: toDateTimeLocal(serviceOrder.scheduledStart),
    observations: serviceOrder.observations || '',
  })
  appointmentLocked.value = Boolean(serviceOrder.appointmentId)
})

watch(appointmentId, async (value) => {
  if (value && !isEdit.value) {
    await loadAppointmentContext(value)
  }
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

async function loadAppointmentContext(id) {
  const { data } = await appointmentApi.getById(id)
  const appointment = data.data
  Object.assign(form, {
    appointmentId: appointment.id,
    customerId: appointment.customerId,
    professionalId: appointment.employeeId,
    serviceId: appointment.serviceId,
    scheduledStart: `${appointment.appointmentDate}T${appointment.startTime.slice(0, 5)}`,
    observations: appointment.notes || '',
  })
  appointmentLocked.value = true
}

async function save() {
  const payload = {
    appointmentId: form.appointmentId || null,
    customerId: form.customerId || null,
    professionalId: form.professionalId || null,
    serviceId: form.serviceId || null,
    scheduledStart: form.scheduledStart || null,
    observations: form.observations,
  }

  try {
    if (isEdit.value) {
      await serviceOrderStore.updateServiceOrder(route.params.id, payload)
    } else {
      await serviceOrderStore.createServiceOrder(payload)
    }
    await router.push('/service-orders')
  } catch {
    return
  }
}

function toDateTimeLocal(value) {
  if (!value) {
    return ''
  }
  return value.slice(0, 16)
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
