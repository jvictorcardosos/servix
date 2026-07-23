import apiClient from './apiClient'

export const appointmentApi = {
  list(params) {
    return apiClient.get('/api/appointments', { params })
  },
  getById(id) {
    return apiClient.get(`/api/appointments/${id}`)
  },
  create(payload) {
    return apiClient.post('/api/appointments', payload)
  },
  update(id, payload) {
    return apiClient.put(`/api/appointments/${id}`, payload)
  },
  updateStatus(id, payload) {
    return apiClient.patch(`/api/appointments/${id}/status`, payload)
  },
  remove(id) {
    return apiClient.delete(`/api/appointments/${id}`)
  },
  day(params) {
    return apiClient.get('/api/appointments/day', { params })
  },
  week(params) {
    return apiClient.get('/api/appointments/week', { params })
  },
  month(params) {
    return apiClient.get('/api/appointments/month', { params })
  },
  byEmployee(id, params) {
    return apiClient.get(`/api/appointments/employee/${id}`, { params })
  },
  byCustomer(id, params) {
    return apiClient.get(`/api/appointments/customer/${id}`, { params })
  },
}
