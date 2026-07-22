import apiClient from './apiClient'

export const authApi = {
  login(payload) {
    return apiClient.post('/api/auth/login', payload)
  },
  refresh(payload) {
    return apiClient.post('/api/auth/refresh', payload)
  },
  logout(payload) {
    return apiClient.post('/api/auth/logout', payload)
  },
}
