<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>{{ transaction?.customerName ?? 'Lançamento financeiro' }}</h1>
        <p>Detalhes, pagamentos e ajustes.</p>
      </div>
      <div class="actions">
        <RouterLink class="secondary" :to="`/financial/${route.params.id}/edit`">Editar</RouterLink>
        <RouterLink class="secondary" :to="`/financial/${route.params.id}/pay`">Pagar</RouterLink>
        <button type="button" class="danger" @click="cancel">Cancelar</button>
      </div>
    </header>

    <p v-if="financialStore.loading">Carregando lançamento...</p>
    <p v-else-if="financialStore.error" class="error">{{ financialStore.error }}</p>
    <p v-else-if="financialStore.message" class="success">{{ financialStore.message }}</p>

    <div v-else class="card">
      <div class="grid">
        <div><strong>Status:</strong> {{ formatStatus(transaction?.status) }}</div>
        <div><strong>Vencimento:</strong> {{ transaction?.dueDate }}</div>
        <div><strong>Total:</strong> {{ formatCurrency(transaction?.totalAmount) }}</div>
        <div><strong>Pago:</strong> {{ formatCurrency(transaction?.paidAmount) }}</div>
        <div><strong>Restante:</strong> {{ formatCurrency(transaction?.remainingAmount) }}</div>
        <div><strong>Forma:</strong> {{ transaction?.paymentMethodName || '-' }}</div>
      </div>

      <div class="notes">
        <strong>Descrição:</strong>
        <p>{{ transaction?.description || 'Sem descrição' }}</p>
      </div>

      <div class="adjustments">
        <div class="adjustment-card">
          <h3>Desconto</h3>
          <input v-model="discountAmount" type="number" min="0" step="0.01" placeholder="Valor do desconto" />
          <input v-model="discountDescription" type="text" placeholder="Observação" />
          <button type="button" @click="applyDiscount">Aplicar desconto</button>
        </div>
        <div class="adjustment-card">
          <h3>Acréscimo</h3>
          <input v-model="surchargeAmount" type="number" min="0" step="0.01" placeholder="Valor do acréscimo" />
          <input v-model="surchargeDescription" type="text" placeholder="Observação" />
          <button type="button" @click="applySurcharge">Aplicar acréscimo</button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useFinancialStore } from '../../stores/financialStore'

const route = useRoute()
const financialStore = useFinancialStore()
const discountAmount = ref('')
const discountDescription = ref('')
const surchargeAmount = ref('')
const surchargeDescription = ref('')

const transaction = computed(() => financialStore.currentTransaction)

onMounted(async () => {
  await financialStore.loadTransaction(route.params.id)
})

async function cancel() {
  await financialStore.cancelTransaction(route.params.id)
  await financialStore.loadTransaction(route.params.id)
}

async function applyDiscount() {
  await financialStore.discountTransaction(route.params.id, Number(discountAmount.value), discountDescription.value)
  await financialStore.loadTransaction(route.params.id)
}

async function applySurcharge() {
  await financialStore.surchargeTransaction(route.params.id, Number(surchargeAmount.value), surchargeDescription.value)
  await financialStore.loadTransaction(route.params.id)
}

function formatStatus(status) {
  const labels = {
    PENDING: 'Pendente',
    PARTIALLY_PAID: 'Parcialmente pago',
    PAID: 'Pago',
    CANCELLED: 'Cancelado',
    OVERDUE: 'Vencido',
    REFUNDED: 'Reembolsado',
  }
  return labels[status] || status
}

function formatCurrency(value) {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value || 0)
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.page-header,
.actions {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 16px;
  padding: 20px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 12px;
}

.notes {
  margin-top: 16px;
}

.adjustments {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.adjustment-card {
  display: grid;
  gap: 8px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  border-radius: 14px;
}

.adjustment-card button {
  padding: 10px 12px;
  background: #1d4ed8;
  color: #fff;
  border: none;
  border-radius: 10px;
}

.secondary,
.danger {
  padding: 10px 16px;
  border-radius: 10px;
  text-decoration: none;
  border: none;
}

.secondary {
  background: #e5e7eb;
  color: #111827;
}

.danger {
  background: #dc2626;
  color: #fff;
}

.error {
  color: #b91c1c;
}

.success {
  color: #166534;
}
</style>
