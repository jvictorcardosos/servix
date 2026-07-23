<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ isEdit ? 'Editar funcionário' : 'Novo funcionário' }}</h1>
        <p>{{ isEdit ? 'Atualize os dados e jornadas do funcionário.' : 'Cadastre um novo funcionário e suas jornadas.' }}</p>
      </div>
    </header>

    <p v-if="employeeStore.loading">Carregando funcionário...</p>
    <p v-else-if="employeeStore.error" class="error">{{ employeeStore.error }}</p>
    <p v-else-if="employeeStore.message" class="success">{{ employeeStore.message }}</p>

    <form class="card" @submit.prevent="save">
      <EmployeeFormFields v-model="form" />
      <div class="actions">
        <RouterLink class="secondary" to="/employees">Voltar</RouterLink>
        <button type="submit" class="primary" :disabled="employeeStore.saving || employeeStore.loading">
          {{ employeeStore.saving ? 'Salvando...' : 'Salvar' }}
        </button>
      </div>
    </form>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import EmployeeFormFields from '../../components/schedule/EmployeeFormFields.vue'
import { useEmployeeStore } from '../../stores/employeeStore'

const route = useRoute()
const router = useRouter()
const employeeStore = useEmployeeStore()

const form = reactive({
  name: '',
  email: '',
  phone: '',
  active: true,
  workSchedules: [
    {
      dayOfWeek: 1,
      startTime: '08:00',
      endTime: '17:00',
      active: true,
    },
  ],
})

const isEdit = computed(() => Boolean(route.params.id))

onMounted(async () => {
  if (!isEdit.value) {
    employeeStore.resetCurrentEmployee()
    return
  }

  const employee = await employeeStore.loadEmployee(route.params.id)
  Object.assign(form, employee)
  if (!Array.isArray(form.workSchedules) || form.workSchedules.length === 0) {
    form.workSchedules = [
      {
        dayOfWeek: 1,
        startTime: '08:00',
        endTime: '17:00',
        active: true,
      },
    ]
  }
})

async function save() {
  try {
    if (isEdit.value) {
      await employeeStore.updateEmployee(route.params.id, form)
    } else {
      await employeeStore.createEmployee(form)
    }
    await router.push('/employees')
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
