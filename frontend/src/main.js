import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import { useAuthStore } from './stores/authStore'
import { setupApiClientAuth } from './services/apiClient'
import './style.css'

const app = createApp(App)
const pinia = createPinia()
const authStore = useAuthStore(pinia)

authStore.hydrate()
setupApiClientAuth(authStore)

router.beforeEach((to) => {
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return { path: '/' }
  }

  const allowedRoles = to.meta.roles
  if (Array.isArray(allowedRoles) && allowedRoles.length > 0) {
    const hasAllowedRole = allowedRoles.some((role) => authStore.hasRole(role))
    if (!hasAllowedRole) {
      return { path: '/' }
    }
  }

  return true
})

app.use(pinia)
app.use(router)
app.mount('#app')
