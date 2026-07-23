import apiClient from './apiClient'

export const serviceApi = {
  list(params) {
    return apiClient.get('/api/services', { params })
  },
  getById(id) {
    return apiClient.get(`/api/services/${id}`)
  },
  create(payload) {
    return apiClient.post('/api/services', payload)
  },
  update(id, payload) {
    return apiClient.put(`/api/services/${id}`, payload)
  },
  updateStatus(id, payload) {
    return apiClient.patch(`/api/services/${id}/status`, payload)
  },
  remove(id) {
    return apiClient.delete(`/api/services/${id}`)
  },
}
