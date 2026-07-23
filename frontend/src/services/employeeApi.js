import apiClient from './apiClient'

export const employeeApi = {
  list(params) {
    return apiClient.get('/api/employees', { params })
  },
  getById(id) {
    return apiClient.get(`/api/employees/${id}`)
  },
  create(payload) {
    return apiClient.post('/api/employees', payload)
  },
  update(id, payload) {
    return apiClient.put(`/api/employees/${id}`, payload)
  },
  updateStatus(id, payload) {
    return apiClient.patch(`/api/employees/${id}/status`, payload)
  },
  remove(id) {
    return apiClient.delete(`/api/employees/${id}`)
  },
}
