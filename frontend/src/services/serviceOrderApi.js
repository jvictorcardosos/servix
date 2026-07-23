import apiClient from './apiClient'

export const serviceOrderApi = {
  list(params) {
    return apiClient.get('/api/service-orders', { params })
  },
  getById(id) {
    return apiClient.get(`/api/service-orders/${id}`)
  },
  create(payload) {
    return apiClient.post('/api/service-orders', payload)
  },
  update(id, payload) {
    return apiClient.put(`/api/service-orders/${id}`, payload)
  },
  remove(id) {
    return apiClient.delete(`/api/service-orders/${id}`)
  },
  start(id) {
    return apiClient.patch(`/api/service-orders/${id}/start`)
  },
  pause(id) {
    return apiClient.patch(`/api/service-orders/${id}/pause`)
  },
  resume(id) {
    return apiClient.patch(`/api/service-orders/${id}/resume`)
  },
  finish(id) {
    return apiClient.patch(`/api/service-orders/${id}/finish`)
  },
  cancel(id) {
    return apiClient.patch(`/api/service-orders/${id}/cancel`)
  },
  history(id) {
    return apiClient.get(`/api/service-orders/history/${id}`)
  },
  byCustomer(id, params) {
    return apiClient.get(`/api/service-orders/customer/${id}`, { params })
  },
  byProfessional(id, params) {
    return apiClient.get(`/api/service-orders/professional/${id}`, { params })
  },
  byStatus(status, params) {
    return apiClient.get(`/api/service-orders/status/${status}`, { params })
  },
}
