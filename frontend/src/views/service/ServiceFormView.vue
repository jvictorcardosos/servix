<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ isEdit ? 'Editar serviço' : 'Novo serviço' }}</h1>
        <p>{{ isEdit ? 'Atualize os dados do serviço.' : 'Cadastre um novo serviço para a sua empresa.' }}</p>
      </div>
    </header>

    <p v-if="serviceStore.loading">Carregando serviço...</p>
    <p v-else-if="serviceStore.error" class="error">{{ serviceStore.error }}</p>
    <p v-else-if="serviceStore.message" class="success">{{ serviceStore.message }}</p>

    <form class="card" @submit.prevent="save">
      <ServiceFormFields v-model="form" />

      <div class="actions">
        <RouterLink class="secondary" to="/services">Voltar</RouterLink>
        <button type="submit" class="primary" :disabled="serviceStore.saving || serviceStore.loading">
          {{ serviceStore.saving ? 'Salvando...' : 'Salvar' }}
        </button>
      </div>
    </form>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ServiceFormFields from '../../components/service/ServiceFormFields.vue'
import { useServiceStore } from '../../stores/serviceStore'

const route = useRoute()
const router = useRouter()
const serviceStore = useServiceStore()

const form = reactive({
  name: '',
  description: '',
  durationMinutes: 60,
  price: '',
  active: true,
})

const isEdit = computed(() => Boolean(route.params.id))

onMounted(async () => {
  if (!isEdit.value) {
    serviceStore.resetCurrentService()
    return
  }

  const service = await serviceStore.loadService(route.params.id)
  Object.assign(form, service)
})

async function save() {
  try {
    if (isEdit.value) {
      await serviceStore.updateService(route.params.id, form)
    } else {
      await serviceStore.createService(form)
    }
    await router.push('/services')
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
