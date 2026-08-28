import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

import { getCurrentUser, login, type LoginPayload } from '@/api/auth'
import {
  clearAuthSession,
  readAuthSession,
  writeAuthSession,
  type AuthSession,
  type AuthUser,
} from '@/utils/auth-storage'
import { getDefaultRouteByRole } from '@/router/navigation'

export const useAuthStore = defineStore('auth', () => {
  const token = ref('')
  const user = ref<AuthUser | null>(null)
  const initialized = ref(false)

  const isAuthenticated = computed(() => Boolean(token.value))

  function hydrateFromStorage() {
    if (initialized.value) {
      return
    }

    const session = readAuthSession()
    token.value = session?.token ?? ''
    user.value = session?.user ?? null
    initialized.value = true
  }

  function applySession(session: AuthSession) {
    token.value = session.token
    user.value = session.user
    writeAuthSession(session)
  }

  async function signIn(payload: LoginPayload) {
    const session = await login(payload)
    applySession(session)
    return getDefaultRouteByRole(session.user.role)
  }

  async function refreshCurrentUser() {
    if (!token.value) {
      return null
    }

    const currentUser = await getCurrentUser()
    user.value = currentUser
    writeAuthSession({
      token: token.value,
      user: currentUser,
    })
    return currentUser
  }

  function signOut() {
    token.value = ''
    user.value = null
    clearAuthSession()
  }

  return {
    initialized,
    isAuthenticated,
    token,
    user,
    hydrateFromStorage,
    refreshCurrentUser,
    signIn,
    signOut,
  }
})
