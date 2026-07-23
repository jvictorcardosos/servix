<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>Registrar pagamento</h1>
        <p>Liquidação parcial ou total do lançamento.</p>
      </div>
    </header>

    <p v-if="financialStore.loading">Carregando lançamento...</p>
    <p v-else-if="financialStore.error" class="error">{{ financialStore.error }}</p>
    <p v-else-if="financialStore.message" class="success">{{ financialStore.message }}</p>

    <form class="card" @submit.prevent="save">
      <div class="fields">
        <label>
          Valor
          <input v-model="form.amount" type="number" min="0.01" step="0.01" required />
        </label>
        <label>
          Forma de pagamento
          <select v-model="form.paymentMethodId">
            <option value="">Selecione</option>
            <option v-for="method in financialStore.paymentMethods" :key="method.id" :value="method.id">
              {{ method.name }}
            </option>
          </select>
        </label>
        <label>
          Data do pagamento
          <input v-model="form.paymentDate" type="date" />
        </label>
        <label class="full">
          Referência externa
          <input v-model="form.externalReference" type="text" />
        </label>
        <label class="full">
          Observações
          <textarea v-model="form.description" rows="4" />
        </label>
      </div>

      <div class="actions">
        <RouterLink class="secondary" :to="`/financial/${route.params.id}`">Voltar</RouterLink>
        <button type="submit" class="primary" :disabled="financialStore.saving || financialStore.loading">
          {{ financialStore.saving ? 'Registrando...' : 'Registrar pagamento' }}
        </button>
      </div>
    </form>
  </section>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useFinancialStore } from '../../stores/financialStore'

const route = useRoute()
const router = useRouter()
const financialStore = useFinancialStore()

const form = reactive({
  amount: '',
  paymentMethodId: '',
  paymentDate: '',
  externalReference: '',
  description: '',
})

onMounted(async () => {
  await financialStore.loadPaymentMethods()
  await financialStore.loadTransaction(route.params.id)
})

async function save() {
  try {
    await financialStore.payTransaction(route.params.id, {
      amount: Number(form.amount),
      paymentMethodId: form.paymentMethodId || null,
      paymentDate: form.paymentDate || null,
      externalReference: form.externalReference,
      description: form.description,
    })
    await router.push(`/financial/${route.params.id}`)
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

.fields {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.full {
  grid-column: 1 / -1;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

textarea,
input,
select {
  padding: 10px 12px;
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
