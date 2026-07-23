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
  }),
  actions: {
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
      } finally {
        this.loading = false
      }
    },
    async createCustomer(payload) {
      this.saving = true
      this.error = null
      try {
        const { data } = await customerApi.create(payload)
        return data.data
      } finally {
        this.saving = false
      }
    },
    async updateCustomer(id, payload) {
      this.saving = true
      this.error = null
      try {
        const { data } = await customerApi.update(id, payload)
        return data.data
      } finally {
        this.saving = false
      }
    },
    async updateCustomerStatus(id, ativo) {
      this.saving = true
      this.error = null
      try {
        const { data } = await customerApi.updateStatus(id, { ativo })
        return data.data
      } finally {
        this.saving = false
      }
    },
    async deleteCustomer(id) {
      this.deleting = true
      this.error = null
      try {
        await customerApi.remove(id)
      } finally {
        this.deleting = false
      }
    },
  },
})
