import axios from 'axios'
import type { InternalAxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types/api'
import type { WebLoginResponse } from '@/types/auth'

const AUTH_USER_KEY = 'auth_user'
const DEPLOY_STARTUP_READY_KEY = 'deploy_startup_backend_ready_at'
const DEPLOY_STARTUP_READY_TTL_MS = 5 * 60 * 1000

const apiBaseURL =
  import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'

const api = axios.create({
  baseURL: apiBaseURL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
})

type RetriableRequestConfig = InternalAxiosRequestConfig & {
  _retry?: boolean
}

let refreshPromise: Promise<string | null> | null = null
let accessToken: string | null = null

export function setAccessToken(token: string | null) {
  accessToken = token
}

export function getAccessToken() {
  return accessToken
}

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
  accessToken = null
  // Remove values left by releases before HttpOnly refresh cookies. New tokens are never persisted.
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem(AUTH_USER_KEY)
}

function readCookie(name: string) {
  if (typeof document === 'undefined') return null

  const prefix = `${name}=`
  return document.cookie
    .split(';')
    .map((value) => value.trim())
    .find((value) => value.startsWith(prefix))
    ?.slice(prefix.length) ?? null
}

async function ensureCsrfToken() {
  if (readCookie('BOOKSTORE_CSRF')) return

  await axios.get(`${apiBaseURL}/auth/web/csrf`, {
    withCredentials: true,
  })
}

async function postWebAuth<T>(path: string, data?: unknown) {
  await ensureCsrfToken()
  const csrfToken = readCookie('BOOKSTORE_CSRF')
  return axios.post<ApiResponse<T>>(`${apiBaseURL}${path}`, data, {
    withCredentials: true,
    headers: {
      'Content-Type': 'application/json',
      ...(csrfToken ? { 'X-CSRF-Token': csrfToken } : {}),
    },
  })
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
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`
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
      requestUrl.includes('/auth/web/') ||
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
  if (!refreshPromise) {
    refreshPromise = postWebAuth<WebLoginResponse>('/auth/web/refresh')
      .then((response) => {
        const session = response.data.data
        if (!session?.accessToken) return null

        accessToken = session.accessToken

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

export async function refreshWebAccessToken() {
  return getFreshAccessToken()
}

export async function webLogin<T>(data: unknown) {
  return postWebAuth<T>('/auth/web/login', data)
}

export async function webGoogleLogin<T>(data: unknown) {
  return postWebAuth<T>('/auth/web/google', data)
}

export async function webLogout() {
  await postWebAuth<null>('/auth/web/logout')
  clearSession()
}

export function clearWebSession() {
  clearSession()
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
