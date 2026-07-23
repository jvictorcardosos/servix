<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>Clientes</h1>
        <p>Pesquisa, filtros, paginação e isolamento por empresa.</p>
      </div>
      <RouterLink class="primary" to="/customers/new">Novo cliente</RouterLink>
    </header>

    <CustomerSearchBar v-model="filters" @search="search" @reset="resetFilters" />

    <p v-if="customerStore.loading">Carregando clientes...</p>
    <p v-else-if="customerStore.error" class="error">{{ customerStore.error }}</p>

    <div v-else class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Nome</th>
            <th>Documento</th>
            <th>Email</th>
            <th>Telefone</th>
            <th>Cidade</th>
            <th>Status</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="customer in customerStore.customers" :key="customer.id">
            <td>{{ customer.nome }}</td>
            <td>{{ customer.cpfCnpj }}</td>
            <td>{{ customer.email }}</td>
            <td>{{ customer.telefone }}</td>
            <td>{{ customer.cidade }}</td>
            <td>{{ customer.ativo ? 'Ativo' : 'Inativo' }}</td>
            <td class="actions">
              <RouterLink :to="`/customers/${customer.id}`">Ver</RouterLink>
              <RouterLink :to="`/customers/${customer.id}/edit`">Editar</RouterLink>
              <button type="button" @click="askDelete(customer)">Excluir</button>
            </td>
          </tr>
          <tr v-if="customerStore.customers.length === 0">
            <td colspan="7">Nenhum cliente encontrado.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <BasePagination
      :page="customerStore.pagination.page"
      :total-pages="customerStore.pagination.totalPages"
      @change="changePage"
    />

    <BaseConfirmDialog
      :visible="deleteDialog.visible"
      title="Excluir cliente"
      :message="`Tem certeza que deseja excluir ${deleteDialog.customer?.nome ?? ''}?`"
      confirm-label="Excluir"
      cancel-label="Cancelar"
      @confirm="confirmDelete"
      @cancel="cancelDelete"
    />
  </section>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import BaseConfirmDialog from '../../components/common/BaseConfirmDialog.vue'
import BasePagination from '../../components/common/BasePagination.vue'
import CustomerSearchBar from '../../components/customer/CustomerSearchBar.vue'
import { useCustomerStore } from '../../stores/customerStore'

const customerStore = useCustomerStore()

const filters = reactive({
  filter: '',
  nome: '',
  cpfCnpj: '',
  telefone: '',
  email: '',
  ativo: '',
})

const deleteDialog = reactive({
  visible: false,
  customer: null,
})

onMounted(async () => {
  await customerStore.loadCustomers()
})

async function search() {
  customerStore.setFilters(filters)
  await customerStore.loadCustomers({ page: 0 })
}

async function resetFilters() {
  Object.assign(filters, {
    filter: '',
    nome: '',
    cpfCnpj: '',
    telefone: '',
    email: '',
    ativo: '',
  })
  customerStore.resetFilters()
  await customerStore.loadCustomers({ page: 0 })
}

async function changePage(page) {
  await customerStore.loadCustomers({ page })
}

function askDelete(customer) {
  deleteDialog.customer = customer
  deleteDialog.visible = true
}

function cancelDelete() {
  deleteDialog.visible = false
  deleteDialog.customer = null
}

async function confirmDelete() {
  if (!deleteDialog.customer) {
    return
  }

  await customerStore.deleteCustomer(deleteDialog.customer.id)
  deleteDialog.visible = false
  deleteDialog.customer = null
  await customerStore.loadCustomers()
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
</style>
