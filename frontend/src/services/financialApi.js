import apiClient from './apiClient'

export const financialApi = {
  list(params) {
    return apiClient.get('/api/financial', { params })
  },
  getById(id) {
    return apiClient.get(`/api/financial/${id}`)
  },
  create(payload) {
    return apiClient.post('/api/financial', payload)
  },
  update(id, payload) {
    return apiClient.put(`/api/financial/${id}`, payload)
  },
  remove(id) {
    return apiClient.delete(`/api/financial/${id}`)
  },
  pay(id, payload) {
    return apiClient.patch(`/api/financial/${id}/pay`, payload)
  },
  cancel(id) {
    return apiClient.patch(`/api/financial/${id}/cancel`)
  },
  discount(id, payload) {
    return apiClient.patch(`/api/financial/${id}/discount`, payload)
  },
  surcharge(id, payload) {
    return apiClient.patch(`/api/financial/${id}/surcharge`, payload)
  },
  byServiceOrder(id) {
    return apiClient.get(`/api/financial/service-order/${id}`)
  },
  byStatus(status, params) {
    return apiClient.get(`/api/financial/status/${status}`, { params })
  },
  due() {
    return apiClient.get('/api/financial/due')
  },
  overdue() {
    return apiClient.get('/api/financial/overdue')
  },
}
