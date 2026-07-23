<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ isEdit ? 'Editar lançamento' : 'Novo lançamento' }}</h1>
        <p>Cadastro manual ou a partir de uma ordem de serviço.</p>
      </div>
    </header>

    <p v-if="financialStore.loading">Carregando lançamento...</p>
    <p v-else-if="financialStore.error" class="error">{{ financialStore.error }}</p>
    <p v-else-if="financialStore.message" class="success">{{ financialStore.message }}</p>

    <form class="card" @submit.prevent="save">
      <div class="fields">
        <label>
          Ordem de serviço
          <input v-model="form.serviceOrderId" type="text" placeholder="UUID opcional" />
        </label>
        <label>
          Valor
          <input v-model="form.amount" type="number" min="0.01" step="0.01" required />
        </label>
        <label>
          Desconto
          <input v-model="form.discount" type="number" min="0" step="0.01" />
        </label>
        <label>
          Acréscimo
          <input v-model="form.surcharge" type="number" min="0" step="0.01" />
        </label>
        <label>
          Vencimento
          <input v-model="form.dueDate" type="date" required />
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
        <label class="full">
          Descrição
          <textarea v-model="form.description" rows="4" />
        </label>
        <label class="full">
          Referência externa
          <input v-model="form.externalReference" type="text" />
        </label>
      </div>

      <div class="actions">
        <RouterLink class="secondary" to="/financial">Voltar</RouterLink>
        <button type="submit" class="primary" :disabled="financialStore.saving || financialStore.loading">
          {{ financialStore.saving ? 'Salvando...' : 'Salvar' }}
        </button>
      </div>
    </form>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useFinancialStore } from '../../stores/financialStore'

const route = useRoute()
const router = useRouter()
const financialStore = useFinancialStore()

const form = reactive({
  serviceOrderId: '',
  amount: '',
  discount: 0,
  surcharge: 0,
  dueDate: '',
  paymentMethodId: '',
  description: '',
  externalReference: '',
})

const isEdit = computed(() => Boolean(route.params.id))

onMounted(async () => {
  await financialStore.loadPaymentMethods()
  if (!isEdit.value) {
    financialStore.resetCurrentTransaction()
    return
  }
  const transaction = await financialStore.loadTransaction(route.params.id)
  Object.assign(form, {
    serviceOrderId: transaction.serviceOrderId || '',
    amount: transaction.amount,
    discount: transaction.discount,
    surcharge: transaction.surcharge,
    dueDate: transaction.dueDate,
    paymentMethodId: transaction.paymentMethodId || '',
    description: transaction.description || '',
    externalReference: transaction.externalReference || '',
  })
})

async function save() {
  const payload = {
    serviceOrderId: form.serviceOrderId || null,
    amount: Number(form.amount),
    discount: Number(form.discount || 0),
    surcharge: Number(form.surcharge || 0),
    dueDate: form.dueDate,
    paymentMethodId: form.paymentMethodId || null,
    description: form.description,
    externalReference: form.externalReference,
  }

  try {
    if (isEdit.value) {
      await financialStore.updateTransaction(route.params.id, payload)
    } else {
      await financialStore.createTransaction(payload)
    }
    await router.push('/financial')
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
