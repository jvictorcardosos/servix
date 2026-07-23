import { defineStore } from 'pinia'
import { employeeApi } from '../services/employeeApi'

const emptyEmployee = () => ({
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

function readApiError(error, fallbackMessage) {
  return error?.response?.data?.message || error?.response?.data?.details?.[0] || fallbackMessage
}

export const useEmployeeStore = defineStore('employee', {
  state: () => ({
    employees: [],
    currentEmployee: emptyEmployee(),
    pagination: {
      page: 0,
      size: 10,
      totalElements: 0,
      totalPages: 0,
      sortBy: 'createdAt',
      direction: 'DESC',
    },
    filters: {
      filter: '',
      name: '',
      email: '',
      phone: '',
      active: '',
    },
    loading: false,
    saving: false,
    deleting: false,
    error: null,
    message: null,
  }),
  actions: {
    clearFeedback() {
      this.error = null
      this.message = null
    },
    resetCurrentEmployee() {
      this.currentEmployee = emptyEmployee()
    },
    setCurrentEmployee(employee) {
      this.currentEmployee = {
        ...emptyEmployee(),
        ...employee,
      }
    },
    setFilters(filters) {
      this.filters = { ...this.filters, ...filters }
    },
    resetFilters() {
      this.filters = {
        filter: '',
        name: '',
        email: '',
        phone: '',
        active: '',
      }
    },
    async loadEmployees(options = {}) {
      this.loading = true
      this.error = null
      try {
        const params = {
          ...this.filters,
          page: options.page ?? this.pagination.page,
          size: options.size ?? this.pagination.size,
          sortBy: options.sortBy ?? this.pagination.sortBy,
          direction: options.direction ?? this.pagination.direction,
        }
        const { data } = await employeeApi.list(params)
        const page = data.data
        this.employees = page.content
        this.pagination = {
          page: page.page,
          size: page.size,
          totalElements: page.totalElements,
          totalPages: page.totalPages,
          sortBy: page.sortBy,
          direction: page.direction,
        }
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar os funcionários.')
        this.employees = []
      } finally {
        this.loading = false
      }
    },
    async loadEmployee(id) {
      this.loading = true
      this.error = null
      try {
        const { data } = await employeeApi.getById(id)
        this.currentEmployee = { ...emptyEmployee(), ...data.data }
        return this.currentEmployee
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar o funcionário.')
        throw error
      } finally {
        this.loading = false
      }
    },
    async createEmployee(payload) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await employeeApi.create(payload)
        this.message = 'Funcionário criado com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível criar o funcionário.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async updateEmployee(id, payload) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await employeeApi.update(id, payload)
        this.message = 'Funcionário atualizado com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível atualizar o funcionário.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async updateEmployeeStatus(id, active) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await employeeApi.updateStatus(id, { active })
        this.message = `Funcionário ${active ? 'ativado' : 'desativado'} com sucesso.`
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível atualizar o status do funcionário.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async deleteEmployee(id) {
      this.deleting = true
      this.clearFeedback()
      try {
        await employeeApi.remove(id)
        this.message = 'Funcionário excluído com sucesso.'
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível excluir o funcionário.')
        throw error
      } finally {
        this.deleting = false
      }
    },
  },
})
