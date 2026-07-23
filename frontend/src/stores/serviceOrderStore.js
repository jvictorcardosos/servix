import { defineStore } from 'pinia'
import { serviceOrderApi } from '../services/serviceOrderApi'

const emptyServiceOrder = () => ({
  appointmentId: '',
  customerId: '',
  professionalId: '',
  serviceId: '',
  scheduledStart: '',
  observations: '',
})

function readApiError(error, fallbackMessage) {
  return error?.response?.data?.message || error?.response?.data?.details?.[0] || fallbackMessage
}

export const useServiceOrderStore = defineStore('serviceOrder', {
  state: () => ({
    serviceOrders: [],
    currentServiceOrder: emptyServiceOrder(),
    history: [],
    pagination: {
      page: 0,
      size: 10,
      totalElements: 0,
      totalPages: 0,
      sortBy: 'scheduledStart',
      direction: 'DESC',
    },
    filters: {
      filter: '',
      appointmentId: '',
      customerId: '',
      professionalId: '',
      serviceId: '',
      status: '',
      dateFrom: '',
      dateTo: '',
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
    resetCurrentServiceOrder() {
      this.currentServiceOrder = emptyServiceOrder()
    },
    setCurrentServiceOrder(serviceOrder) {
      this.currentServiceOrder = {
        ...emptyServiceOrder(),
        ...serviceOrder,
      }
    },
    setFilters(filters) {
      this.filters = { ...this.filters, ...filters }
    },
    resetFilters() {
      this.filters = {
        filter: '',
        appointmentId: '',
        customerId: '',
        professionalId: '',
        serviceId: '',
        status: '',
        dateFrom: '',
        dateTo: '',
      }
    },
    async loadServiceOrders(options = {}) {
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
        const { data } = await serviceOrderApi.list(params)
        const page = data.data
        this.serviceOrders = page.content
        this.pagination = {
          page: page.page,
          size: page.size,
          totalElements: page.totalElements,
          totalPages: page.totalPages,
          sortBy: page.sortBy,
          direction: page.direction,
        }
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar as ordens de serviço.')
        this.serviceOrders = []
      } finally {
        this.loading = false
      }
    },
    async loadServiceOrder(id) {
      this.loading = true
      this.error = null
      try {
        const { data } = await serviceOrderApi.getById(id)
        this.currentServiceOrder = { ...emptyServiceOrder(), ...data.data }
        return this.currentServiceOrder
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar a ordem de serviço.')
        throw error
      } finally {
        this.loading = false
      }
    },
    async loadServiceOrderHistory(id) {
      this.loading = true
      this.error = null
      try {
        const { data } = await serviceOrderApi.history(id)
        this.history = data.data
        return this.history
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar o histórico.')
        this.history = []
        throw error
      } finally {
        this.loading = false
      }
    },
    async createServiceOrder(payload) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await serviceOrderApi.create(payload)
        this.message = 'Ordem de serviço criada com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível criar a ordem de serviço.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async updateServiceOrder(id, payload) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await serviceOrderApi.update(id, payload)
        this.message = 'Ordem de serviço atualizada com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível atualizar a ordem de serviço.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async deleteServiceOrder(id) {
      this.deleting = true
      this.clearFeedback()
      try {
        await serviceOrderApi.remove(id)
        this.message = 'Ordem de serviço excluída com sucesso.'
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível excluir a ordem de serviço.')
        throw error
      } finally {
        this.deleting = false
      }
    },
    async startServiceOrder(id) {
      return this.changeStatus(id, 'start', 'Atendimento iniciado com sucesso.')
    },
    async pauseServiceOrder(id) {
      return this.changeStatus(id, 'pause', 'Atendimento pausado com sucesso.')
    },
    async resumeServiceOrder(id) {
      return this.changeStatus(id, 'resume', 'Atendimento retomado com sucesso.')
    },
    async finishServiceOrder(id) {
      return this.changeStatus(id, 'finish', 'Atendimento concluído com sucesso.')
    },
    async cancelServiceOrder(id) {
      return this.changeStatus(id, 'cancel', 'Ordem de serviço cancelada com sucesso.')
    },
    async changeStatus(id, action, message) {
      this.saving = true
      this.clearFeedback()
      try {
        const response = await serviceOrderApi[action](id)
        this.message = message
        return response.data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível alterar o status da ordem de serviço.')
        throw error
      } finally {
        this.saving = false
      }
    },
  },
})
