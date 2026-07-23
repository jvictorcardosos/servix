import { defineStore } from 'pinia'
import { customerApi } from '../services/customerApi'

const emptyCustomer = () => ({
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

export const useCustomerStore = defineStore('customer', {
  state: () => ({
    customers: [],
    currentCustomer: emptyCustomer(),
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
      nome: '',
      cpfCnpj: '',
      telefone: '',
      email: '',
      ativo: '',
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
    resetCurrentCustomer() {
      this.currentCustomer = emptyCustomer()
    },
    setCurrentCustomer(customer) {
      this.currentCustomer = { ...emptyCustomer(), ...customer }
    },
    setFilters(filters) {
      this.filters = { ...this.filters, ...filters }
    },
    resetFilters() {
      this.filters = {
        filter: '',
        nome: '',
        cpfCnpj: '',
        telefone: '',
        email: '',
        ativo: '',
      }
    },
    async loadCustomers(options = {}) {
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
        const { data } = await customerApi.list(params)
        const page = data.data
        this.customers = page.content
        this.pagination = {
          page: page.page,
          size: page.size,
          totalElements: page.totalElements,
          totalPages: page.totalPages,
          sortBy: page.sortBy,
          direction: page.direction,
        }
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar os clientes.')
        this.customers = []
      } finally {
        this.loading = false
      }
    },
    async loadCustomer(id) {
      this.loading = true
      this.error = null
      try {
        const { data } = await customerApi.getById(id)
        this.currentCustomer = { ...emptyCustomer(), ...data.data }
        return this.currentCustomer
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar o cliente.')
        throw error
      } finally {
        this.loading = false
      }
    },
    async createCustomer(payload) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await customerApi.create(payload)
        this.message = 'Cliente criado com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível criar o cliente.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async updateCustomer(id, payload) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await customerApi.update(id, payload)
        this.message = 'Cliente atualizado com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível atualizar o cliente.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async updateCustomerStatus(id, ativo) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await customerApi.updateStatus(id, { ativo })
        this.message = `Cliente ${ativo ? 'ativado' : 'desativado'} com sucesso.`
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível atualizar o status do cliente.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async deleteCustomer(id) {
      this.deleting = true
      this.clearFeedback()
      try {
        await customerApi.remove(id)
        this.message = 'Cliente excluído com sucesso.'
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível excluir o cliente.')
        throw error
      } finally {
        this.deleting = false
      }
    },
  },
})

function readApiError(error, fallbackMessage) {
  return error?.response?.data?.message || error?.response?.data?.details?.[0] || fallbackMessage
}
