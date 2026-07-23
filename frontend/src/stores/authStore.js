import { defineStore } from 'pinia'

const STORAGE_KEY = 'servix.auth.session'

function readStoredSession() {
  if (typeof window === 'undefined') {
    return {}
  }

  const raw = window.localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    return {}
  }

  try {
    return JSON.parse(raw)
  } catch {
    return {}
  }
}

function writeStoredSession(session) {
  if (typeof window === 'undefined') {
    return
  }

  if (!session.accessToken) {
    window.localStorage.removeItem(STORAGE_KEY)
    return
  }

  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(session))
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: null,
    refreshToken: null,
    profiles: [],
    companyId: null,
    userId: null,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.accessToken),
    hasRole: (state) => (role) => state.profiles.includes(role),
  },
  actions: {
    hydrate() {
      const session = readStoredSession()
      this.accessToken = session.accessToken ?? null
      this.refreshToken = session.refreshToken ?? null
      this.profiles = Array.isArray(session.profiles) ? session.profiles : []
      this.companyId = session.companyId ?? null
      this.userId = session.userId ?? null
    },
    setSession(session) {
      this.accessToken = session.accessToken
      this.refreshToken = session.refreshToken
      this.profiles = Array.isArray(session.profiles)
        ? session.profiles
        : session.profile
          ? [session.profile]
          : []
      this.companyId = session.companyId ?? null
      this.userId = session.userId ?? null
      writeStoredSession({
        accessToken: this.accessToken,
        refreshToken: this.refreshToken,
        profiles: this.profiles,
        companyId: this.companyId,
        userId: this.userId,
      })
    },
    clearSession() {
      this.accessToken = null
      this.refreshToken = null
      this.profiles = []
      this.companyId = null
      this.userId = null
      writeStoredSession({ accessToken: null })
    },
  },
})
