import axios from 'axios'

import { clearAuthSession, readAuthSession } from './auth-storage'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080',
  timeout: 10000,
})

request.interceptors.request.use((config) => {
  const session = readAuthSession()
  if (session?.token) {
    config.headers.Authorization = `Bearer ${session.token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearAuthSession()
    }
    return Promise.reject(error)
  },
)

export default request
