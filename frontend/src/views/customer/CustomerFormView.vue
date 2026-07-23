<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ isEdit ? 'Editar cliente' : 'Novo cliente' }}</h1>
        <p>{{ isEdit ? 'Atualize os dados do cliente.' : 'Cadastre um novo cliente para a sua empresa.' }}</p>
      </div>
    </header>

    <form class="card" @submit.prevent="save">
      <CustomerFormFields v-model="form" />

      <div class="actions">
        <RouterLink class="secondary" to="/customers">Voltar</RouterLink>
        <button type="submit" class="primary" :disabled="customerStore.saving">
          {{ customerStore.saving ? 'Salvando...' : 'Salvar' }}
        </button>
      </div>
    </form>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import CustomerFormFields from '../../components/customer/CustomerFormFields.vue'
import { useCustomerStore } from '../../stores/customerStore'

const route = useRoute()
const router = useRouter()
const customerStore = useCustomerStore()

const form = reactive({
  nome: '',
  cpfCnpj: '',
  email: '',
  telefone: '',
  telefoneSecundario: '',
  cep: '',
  logradouro: '',
  numero: '',
  complemento: '',
  bairro: '',
  cidade: '',
  estado: '',
  observacoes: '',
  ativo: true,
})

const isEdit = computed(() => Boolean(route.params.id))

onMounted(async () => {
  if (!isEdit.value) {
    customerStore.resetCurrentCustomer()
    return
  }

  const customer = await customerStore.loadCustomer(route.params.id)
  Object.assign(form, customer)
})

async function save() {
  if (isEdit.value) {
    await customerStore.updateCustomer(route.params.id, form)
  } else {
    await customerStore.createCustomer(form)
  }

  await router.push('/customers')
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
</style>
