import axios from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types/api'
import type { LoginResponse } from '@/types/auth'

const ACCESS_TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'
const AUTH_USER_KEY = 'auth_user'
const DEPLOY_STARTUP_READY_KEY = 'deploy_startup_backend_ready_at'
const DEPLOY_STARTUP_READY_TTL_MS = 5 * 60 * 1000

const apiBaseURL =
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'

const api = axios.create({
  baseURL: apiBaseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})

type RetriableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean
}

let refreshPromise: Promise<string | null> | null = null

function isLocalHostname(hostname: string) {
  return (
    hostname === 'localhost' ||
    hostname === '127.0.0.1' ||
    hostname === '::1' ||
    hostname.endsWith('.local') ||
    hostname.startsWith('192.168.') ||
    hostname.startsWith('10.') ||
    /^172\.(1[6-9]|2\d|3[01])\./.test(hostname)
  )
}

function clearSession() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(AUTH_USER_KEY)
}

function readRecentBackendReadyTimestamp() {
  if (typeof window === 'undefined') {
    return null
  }

  const storedTimestamp = window.sessionStorage.getItem(DEPLOY_STARTUP_READY_KEY)

  if (!storedTimestamp) {
    return null
  }

  const parsedTimestamp = Number(storedTimestamp)

  if (!Number.isFinite(parsedTimestamp)) {
    window.sessionStorage.removeItem(DEPLOY_STARTUP_READY_KEY)
    return null
  }

  if (Date.now() - parsedTimestamp > DEPLOY_STARTUP_READY_TTL_MS) {
    window.sessionStorage.removeItem(DEPLOY_STARTUP_READY_KEY)
    return null
  }

  return parsedTimestamp
}

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(ACCESS_TOKEN_KEY)

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (!axios.isAxiosError(error)) {
      return Promise.reject(error)
    }

    const originalRequest = error.config as RetriableRequestConfig | undefined
    const requestUrl = originalRequest?.url ?? ''

    if (
      !originalRequest ||
      originalRequest._retry ||
      error.response?.status !== 401 ||
      requestUrl.includes('/auth/login') ||
      requestUrl.includes('/auth/google') ||
      requestUrl.includes('/auth/register') ||
      requestUrl.includes('/auth/forgot-password/') ||
      requestUrl.includes('/otp/request') ||
      requestUrl.includes('/otp/verify') ||
      requestUrl.includes('/auth/refresh')
    ) {
      return Promise.reject(error)
    }

    originalRequest._retry = true

    const nextAccessToken = await getFreshAccessToken()

    if (!nextAccessToken) {
      clearSession()

      if (
        typeof window !== 'undefined' &&
        !window.location.pathname.startsWith('/login')
      ) {
        window.location.assign('/login')
      }

      return Promise.reject(error)
    }

    originalRequest.headers.Authorization = `Bearer ${nextAccessToken}`

    return api(originalRequest)
  },
)

async function getFreshAccessToken() {
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)

  if (!refreshToken) {
    return null
  }

  if (!refreshPromise) {
    refreshPromise = axios
      .post<ApiResponse<LoginResponse>>(
        `${apiBaseURL}/auth/refresh`,
        { refreshToken },
        {
          headers: {
            'Content-Type': 'application/json',
          },
        },
      )
      .then((response) => {
        const session = response.data.data

        localStorage.setItem(ACCESS_TOKEN_KEY, session.accessToken)
        localStorage.setItem(REFRESH_TOKEN_KEY, session.refreshToken)

        return session.accessToken
      })
      .catch(() => {
        clearSession()
        return null
      })
      .finally(() => {
        refreshPromise = null
      })
  }

  return refreshPromise
}

export function shouldUseDeployStartupGate() {
  if (typeof window === 'undefined') {
    return false
  }

  // Enable the cold-start screen on real deployed hosts only.
  return (
    import.meta.env.PROD &&
    !isLocalHostname(window.location.hostname) &&
    !readRecentBackendReadyTimestamp()
  )
}

export function rememberBackendReadyProbe() {
  if (typeof window === 'undefined') {
    return
  }

  window.sessionStorage.setItem(
    DEPLOY_STARTUP_READY_KEY,
    Date.now().toString(),
  )
}

export async function probeBackendReady(signal?: AbortSignal) {
  try {
    const response = await axios.get<ApiResponse<unknown>>(`${apiBaseURL}/books`, {
      params: {
        page: 0,
        size: 1,
      },
      signal,
      timeout: 8000,
      validateStatus: () => true,
    })

    return response.status >= 200 && response.status < 300
  } catch {
    return false
  }
}

export default api
