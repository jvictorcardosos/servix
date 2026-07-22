import { defineStore } from 'pinia'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: null,
    refreshToken: null,
    profile: null,
    companyId: null,
    userId: null,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.accessToken),
  },
  actions: {
    setSession(session) {
      this.accessToken = session.accessToken
      this.refreshToken = session.refreshToken
      this.profile = session.profile ?? null
      this.companyId = session.companyId ?? null
      this.userId = session.userId ?? null
    },
    clearSession() {
      this.accessToken = null
      this.refreshToken = null
      this.profile = null
      this.companyId = null
      this.userId = null
    },
  },
})
