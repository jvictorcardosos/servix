import { defineStore } from 'pinia'
import { financialApi } from '../services/financialApi'
import { paymentMethodApi } from '../services/paymentMethodApi'

const emptyFinancial = () => ({
  serviceOrderId: '',
  amount: '',
  discount: 0,
  surcharge: 0,
  dueDate: '',
  paymentMethodId: '',
  description: '',
  externalReference: '',
})

function readApiError(error, fallbackMessage) {
  return error?.response?.data?.message || error?.response?.data?.details?.[0] || fallbackMessage
}

export const useFinancialStore = defineStore('financial', {
  state: () => ({
    transactions: [],
    currentTransaction: emptyFinancial(),
    paymentMethods: [],
    pagination: {
      page: 0,
      size: 10,
      totalElements: 0,
      totalPages: 0,
      sortBy: 'dueDate',
      direction: 'DESC',
    },
    filters: {
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
    resetCurrentTransaction() {
      this.currentTransaction = emptyFinancial()
    },
    setCurrentTransaction(transaction) {
      this.currentTransaction = {
        ...emptyFinancial(),
        ...transaction,
      }
    },
    setFilters(filters) {
      this.filters = { ...this.filters, ...filters }
    },
    resetFilters() {
      this.filters = {
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
      }
    },
    async loadPaymentMethods() {
      try {
        const { data } = await paymentMethodApi.list()
        this.paymentMethods = data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar as formas de pagamento.')
        this.paymentMethods = []
      }
    },
    async loadTransactions(options = {}) {
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
        const { data } = await financialApi.list(params)
        const page = data.data
        this.transactions = page.content
        this.pagination = {
          page: page.page,
          size: page.size,
          totalElements: page.totalElements,
          totalPages: page.totalPages,
          sortBy: page.sortBy,
          direction: page.direction,
        }
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar os lançamentos.')
        this.transactions = []
      } finally {
        this.loading = false
      }
    },
    async loadTransaction(id) {
      this.loading = true
      this.error = null
      try {
        const { data } = await financialApi.getById(id)
        this.currentTransaction = { ...emptyFinancial(), ...data.data }
        return this.currentTransaction
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível carregar o lançamento.')
        throw error
      } finally {
        this.loading = false
      }
    },
    async createTransaction(payload) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await financialApi.create(payload)
        this.message = 'Lançamento financeiro criado com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível criar o lançamento.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async updateTransaction(id, payload) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await financialApi.update(id, payload)
        this.message = 'Lançamento financeiro atualizado com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível atualizar o lançamento.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async deleteTransaction(id) {
      this.deleting = true
      this.clearFeedback()
      try {
        await financialApi.remove(id)
        this.message = 'Lançamento financeiro removido com sucesso.'
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível excluir o lançamento.')
        throw error
      } finally {
        this.deleting = false
      }
    },
    async payTransaction(id, payload) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await financialApi.pay(id, payload)
        this.message = 'Pagamento registrado com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível registrar o pagamento.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async cancelTransaction(id) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await financialApi.cancel(id)
        this.message = 'Lançamento cancelado com sucesso.'
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível cancelar o lançamento.')
        throw error
      } finally {
        this.saving = false
      }
    },
    async discountTransaction(id, amount, description) {
      return this.applyAdjustment('discount', id, amount, description, 'Desconto aplicado com sucesso.')
    },
    async surchargeTransaction(id, amount, description) {
      return this.applyAdjustment('surcharge', id, amount, description, 'Acréscimo aplicado com sucesso.')
    },
    async applyAdjustment(action, id, amount, description, message) {
      this.saving = true
      this.clearFeedback()
      try {
        const { data } = await financialApi[action](id, { amount, description })
        this.message = message
        return data.data
      } catch (error) {
        this.error = readApiError(error, 'Não foi possível aplicar a alteração.')
        throw error
      } finally {
        this.saving = false
      }
    },
  },
})
