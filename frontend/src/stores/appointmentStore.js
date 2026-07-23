import { defineStore } from 'pinia'
import { appointmentApi } from '../services/appointmentApi'

const emptyAppointment = () => ({
  customerId: '',
  serviceId: '',
  employeeId: '',
  appointmentDate: '',
  startTime: '08:00',
  notes: '',
})

function readApiError(error, fallbackMessage) {
  return error?.response?.data?.message || error?.response?.data?.details?.[0] || fallbackMessage
}

export const useAppointmentStore = defineStore('appointment', {
  state: () => ({
    appointments: [],
    calendarAppointments: [],
    currentAppointment: emptyAppointment(),
    pagination: {
      page: 0,
      size: 10,
      totalElements: 0,
      totalPages: 0,
      sortBy: 'appointmentDate',
      direction: 'ASC',
    },
    filters: {
      filter: '',
      customerId: '',
      serviceId: '',
      employeeId: '',
      status: '',
      dateFrom: '',
      dateTo: '',
    },
    calendarMode: 'week',
    calendarDate: new Date().toISOString().slice(0, 10),
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
    resetCurrentAppointment() {
      this.currentAppointment = emptyAppointment()
    },
    setCurrentAppointment(appointment) {
      this.currentAppointment = {
        ...emptyAppointment(),
        ...appointment,
      }
    },
    setFilters(filters) {
      this.filters = { ...this.filters, ...filters }
    },
    resetFilters() {
      this.filters = {
        filter: '',
        customerId: '',
        serviceId: '',
        employeeId: '',
        status: '',
        dateFrom: '',
        dateTo: '',
      }
    },
    setCalendarMode(mode) {
      this.calendarMode = mode
    },
    setCalendarDate(date) {
      this.calendarDate = date
    },
    async loadAppointments(options = {}) {
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
        const { data } = await appointmentApi.list(params)
        const page = data.data
        this.appointments = page.content
        this.pagination = {
          page: page.page,
          size: page.size,
          totalElements: page.totalElements,
          totalPages: page.totalPages,
          sortBy: page.sortBy,
          direction: page.direction,
        }
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar os agendamentos.')
        this.appointments = []
      } finally {
        this.loading = false
      }
    },
    async loadCalendarAppointments(mode = this.calendarMode, date = this.calendarDate, filters = {}) {
      this.loading = true
      this.error = null
      try {
        const params = {
          ...this.filters,
          ...filters,
          date,
        }
        const endpoint = mode === 'day' ? appointmentApi.day : mode === 'month' ? appointmentApi.month : appointmentApi.week
        const { data } = await endpoint(params)
        this.calendarAppointments = data.data
        this.calendarMode = mode
        this.calendarDate = date
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar a agenda.')
        this.calendarAppointments = []
      } finally {
        this.loading = false
      }
    },
    async loadAppointment(id) {
      this.loading = true
      this.error = null
      try {
        const { data } = await appointmentApi.getById(id)
        this.currentAppointment = { ...emptyAppointment(), ...data.data }
        return this.currentAppointment
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar o agendamento.')
        throw error
      } finally {
        this.loading = false
      }
    },
    async createAppointment(payload) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await appointmentApi.create(payload)
        this.message = 'Agendamento criado com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível criar o agendamento.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async updateAppointment(id, payload) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await appointmentApi.update(id, payload)
        this.message = 'Agendamento atualizado com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível atualizar o agendamento.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async updateAppointmentStatus(id, status) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await appointmentApi.updateStatus(id, { status })
        this.message = 'Status do agendamento atualizado com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível atualizar o status do agendamento.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async cancelAppointment(id) {
      return this.updateAppointmentStatus(id, 'CANCELLED')
    },
    async completeAppointment(id) {
      return this.updateAppointmentStatus(id, 'COMPLETED')
    },
    async deleteAppointment(id) {
      this.deleting = true
      this.clearFeedback()
      try {
        await appointmentApi.remove(id)
        this.message = 'Agendamento excluído com sucesso.'
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível excluir o agendamento.')
        throw error
      } finally {
        this.deleting = false
      }
    },
  },
})
