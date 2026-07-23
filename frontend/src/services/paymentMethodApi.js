import apiClient from './apiClient'

export const paymentMethodApi = {
  list() {
    return apiClient.get('/api/payment-methods')
  },
}
