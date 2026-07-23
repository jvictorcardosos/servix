<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>Financeiro</h1>
        <p>Controle de lançamentos, pagamentos e saldos.</p>
      </div>
      <RouterLink class="primary" to="/financial/new">Novo lançamento</RouterLink>
    </header>

    <FinancialSearchBar v-model="filters" :payment-methods="financialStore.paymentMethods" :customers="customers" :professionals="professionals" :services="services" @search="search" @reset="resetFilters" />

    <p v-if="financialStore.loading">Carregando lançamentos...</p>
    <p v-else-if="financialStore.error" class="error">{{ financialStore.error }}</p>
    <p v-else-if="financialStore.message" class="success">{{ financialStore.message }}</p>

    <div v-else class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Vencimento</th>
            <th>Cliente</th>
            <th>Serviço</th>
            <th>Status</th>
            <th>Total</th>
            <th>Pago</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="transaction in financialStore.transactions" :key="transaction.id">
            <td>{{ transaction.dueDate }}</td>
            <td>{{ transaction.customerName }}</td>
            <td>{{ transaction.serviceName }}</td>
            <td>{{ formatStatus(transaction.status) }}</td>
            <td>{{ formatCurrency(transaction.totalAmount) }}</td>
            <td>{{ formatCurrency(transaction.paidAmount) }}</td>
            <td class="actions">
              <RouterLink :to="`/financial/${transaction.id}`">Ver</RouterLink>
              <RouterLink :to="`/financial/${transaction.id}/edit`">Editar</RouterLink>
              <RouterLink :to="`/financial/${transaction.id}/pay`">Pagar</RouterLink>
            </td>
          </tr>
          <tr v-if="financialStore.transactions.length === 0">
            <td colspan="7">Nenhum lançamento encontrado.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <BasePagination :page="financialStore.pagination.page" :total-pages="financialStore.pagination.totalPages" @change="changePage" />
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import BasePagination from '../../components/common/BasePagination.vue'
import FinancialSearchBar from '../../components/financial/FinancialSearchBar.vue'
import { customerApi } from '../../services/customerApi'
import { employeeApi } from '../../services/employeeApi'
import { serviceApi } from '../../services/serviceApi'
import { useFinancialStore } from '../../stores/financialStore'

const financialStore = useFinancialStore()
const customers = ref([])
const professionals = ref([])
const services = ref([])

const filters = reactive({
  filter: '',
  serviceOrderId: '',
  customerId: '',
  professionalId: '',
  serviceId: '',
  paymentMethodId: '',
  transactionType: '',
  status: '',
  dateFrom: '',
  dateTo: '',
})

onMounted(async () => {
  await Promise.all([loadOptions(), financialStore.loadPaymentMethods(), financialStore.loadTransactions()])
})

async function loadOptions() {
  const [customerResponse, professionalResponse, serviceResponse] = await Promise.all([
    customerApi.list({ size: 100 }),
    employeeApi.list({ size: 100 }),
    serviceApi.list({ size: 100 }),
  ])
  customers.value = customerResponse.data.data.content
  professionals.value = professionalResponse.data.data.content
  services.value = serviceResponse.data.data.content
}

async function search() {
  financialStore.setFilters(filters)
  await financialStore.loadTransactions({ page: 0 })
}

async function resetFilters() {
  Object.assign(filters, {
    filter: '',
    serviceOrderId: '',
    customerId: '',
    professionalId: '',
    serviceId: '',
    paymentMethodId: '',
    transactionType: '',
    status: '',
    dateFrom: '',
    dateTo: '',
  })
  financialStore.resetFilters()
  await financialStore.loadTransactions({ page: 0 })
}

async function changePage(page) {
  await financialStore.loadTransactions({ page })
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

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: flex-start;
}

.primary {
  padding: 10px 16px;
  background: #1d4ed8;
  color: #fff;
  text-decoration: none;
  border-radius: 10px;
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

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.error {
  color: #b91c1c;
}

.success {
  color: #166534;
}
</style>
