import axios from 'axios'

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 10000,
})

export function setupApiClientAuth(authStore) {
  apiClient.interceptors.request.use((config) => {
    const nextConfig = { ...config }
    if (authStore.accessToken) {
      nextConfig.headers = nextConfig.headers ?? {}
      nextConfig.headers.Authorization = `Bearer ${authStore.accessToken}`
      if (authStore.companyId) {
        nextConfig.headers['X-Company-Id'] = authStore.companyId
      }
    }
    return nextConfig
  })
}

export default apiClient
