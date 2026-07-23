<template>
  <section class="page">
    <header class="page-header">
      <div>
        <h1>Funcionários</h1>
        <p>Cadastro, jornadas e controle do time da empresa.</p>
      </div>
      <RouterLink class="primary" to="/employees/new">Novo funcionário</RouterLink>
    </header>

    <EmployeeSearchBar v-model="filters" @search="search" @reset="resetFilters" />

    <p v-if="employeeStore.loading">Carregando funcionários...</p>
    <p v-else-if="employeeStore.error" class="error">{{ employeeStore.error }}</p>
    <p v-else-if="employeeStore.message" class="success">{{ employeeStore.message }}</p>

    <div v-else class="table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Nome</th>
            <th>Email</th>
            <th>Telefone</th>
            <th>Status</th>
            <th>Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="employee in employeeStore.employees" :key="employee.id">
            <td>{{ employee.name }}</td>
            <td>{{ employee.email }}</td>
            <td>{{ employee.phone || '-' }}</td>
            <td>{{ employee.active ? 'Ativo' : 'Inativo' }}</td>
            <td class="actions">
              <RouterLink :to="`/employees/${employee.id}/edit`">Editar</RouterLink>
              <button type="button" @click="toggleStatus(employee)">
                {{ employee.active ? 'Desativar' : 'Ativar' }}
              </button>
              <button type="button" @click="askDelete(employee)">Excluir</button>
            </td>
          </tr>
          <tr v-if="employeeStore.employees.length === 0">
            <td colspan="5">Nenhum funcionário encontrado.</td>
          </tr>
        </tbody>
      </table>
    </div>

    <BasePagination
      :page="employeeStore.pagination.page"
      :total-pages="employeeStore.pagination.totalPages"
      @change="changePage"
    />

    <BaseConfirmDialog
      :visible="deleteDialog.visible"
      title="Excluir funcionário"
      :message="`Tem certeza que deseja excluir ${deleteDialog.employee?.name ?? ''}?`"
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
import EmployeeSearchBar from '../../components/schedule/EmployeeSearchBar.vue'
import { useEmployeeStore } from '../../stores/employeeStore'

const employeeStore = useEmployeeStore()

const filters = reactive({
  filter: '',
  name: '',
  email: '',
  phone: '',
  active: '',
})

const deleteDialog = reactive({
  visible: false,
  employee: null,
})

onMounted(async () => {
  await employeeStore.loadEmployees()
})

async function search() {
  employeeStore.setFilters(filters)
  await employeeStore.loadEmployees({ page: 0 })
}

async function resetFilters() {
  Object.assign(filters, {
    filter: '',
    name: '',
    email: '',
    phone: '',
    active: '',
  })
  employeeStore.resetFilters()
  await employeeStore.loadEmployees({ page: 0 })
}

async function changePage(page) {
  await employeeStore.loadEmployees({ page })
}

async function toggleStatus(employee) {
  await employeeStore.updateEmployeeStatus(employee.id, !employee.active)
  await employeeStore.loadEmployees()
}

function askDelete(employee) {
  deleteDialog.employee = employee
  deleteDialog.visible = true
}

function cancelDelete() {
  deleteDialog.visible = false
  deleteDialog.employee = null
}

async function confirmDelete() {
  if (!deleteDialog.employee) {
    return
  }
  await employeeStore.deleteEmployee(deleteDialog.employee.id)
  deleteDialog.visible = false
  deleteDialog.employee = null
  await employeeStore.loadEmployees()
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
