import { defineStore } from 'pinia'
import { serviceApi } from '../services/serviceApi'

const emptyService = () => ({
  name: '',
  description: '',
  durationMinutes: 60,
  price: '',
  active: true,
})

function readApiError(error, fallbackMessage) {
  return error?.response?.data?.message || error?.response?.data?.details?.[0] || fallbackMessage
}

export const useServiceStore = defineStore('service', {
  state: () => ({
    services: [],
    currentService: emptyService(),
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
      active: '',
      minPrice: '',
      maxPrice: '',
      minDuration: '',
      maxDuration: '',
    },
    loading: false,
    saving: false,
    deleting: false,
    error: null,
    message: null,
  }),
  getters: {
    hasServices: (state) => state.services.length > 0,
  },
  actions: {
    clearFeedback() {
      this.error = null
      this.message = null
    },
    resetCurrentService() {
      this.currentService = emptyService()
    },
    setCurrentService(service) {
      this.currentService = { ...emptyService(), ...service }
    },
    setFilters(filters) {
      this.filters = { ...this.filters, ...filters }
    },
    resetFilters() {
      this.filters = {
        filter: '',
        name: '',
        active: '',
        minPrice: '',
        maxPrice: '',
        minDuration: '',
        maxDuration: '',
      }
    },
    async loadServices(options = {}) {
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
        const { data } = await serviceApi.list(params)
        const page = data.data
        this.services = page.content
        this.pagination = {
          page: page.page,
          size: page.size,
          totalElements: page.totalElements,
          totalPages: page.totalPages,
          sortBy: page.sortBy,
          direction: page.direction,
        }
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar os serviços.')
        this.services = []
      } finally {
        this.loading = false
      }
    },
    async loadService(id) {
      this.loading = true
      this.error = null
      try {
        const { data } = await serviceApi.getById(id)
        this.currentService = { ...emptyService(), ...data.data }
        return this.currentService
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar o serviço.')
        throw error
      } finally {
        this.loading = false
      }
    },
    async createService(payload) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await serviceApi.create(payload)
        this.message = 'Serviço criado com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível criar o serviço.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async updateService(id, payload) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await serviceApi.update(id, payload)
        this.message = 'Serviço atualizado com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível atualizar o serviço.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async updateServiceStatus(id, active) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await serviceApi.updateStatus(id, { active })
        this.message = `Serviço ${active ? 'ativado' : 'desativado'} com sucesso.`
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível atualizar o status do serviço.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async deleteService(id) {
      this.deleting = true
      this.clearFeedback()
      try {
        await serviceApi.remove(id)
        this.message = 'Serviço excluído com sucesso.'
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível excluir o serviço.')
        throw error
      } finally {
        this.deleting = false
      }
    },
  },
})
