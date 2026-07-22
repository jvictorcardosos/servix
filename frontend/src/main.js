import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import { useAuthStore } from './stores/authStore'
import { setupApiClientAuth } from './services/apiClient'
import './style.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

setupApiClientAuth(useAuthStore(pinia))
app.mount('#app')
