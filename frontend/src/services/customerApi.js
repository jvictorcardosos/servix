import apiClient from './apiClient'

export const customerApi = {
  list(params) {
    return apiClient.get('/api/customers', { params })
  },
  getById(id) {
    return apiClient.get(`/api/customers/${id}`)
  },
  create(payload) {
    return apiClient.post('/api/customers', payload)
  },
  update(id, payload) {
    return apiClient.put(`/api/customers/${id}`, payload)
  },
  updateStatus(id, payload) {
    return apiClient.patch(`/api/customers/${id}/status`, payload)
  },
  remove(id) {
    return apiClient.delete(`/api/customers/${id}`)
  },
}
