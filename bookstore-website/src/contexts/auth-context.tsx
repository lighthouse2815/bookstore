import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react'
import {
  getCurrentUser,
  login as loginRequest,
  logout as logoutRequest,
  refreshAccessToken,
  register as registerRequest,
} from '@/services/auth-service'
import { useLanguage } from '@/contexts/language-context'
import { getCurrentProfile } from '@/services/profile-service'
import type {
  LoginRequest,
  RegisterRequest,
  User,
  UserMeResponse,
} from '@/types/auth'
import type { ProfileResponse } from '@/types/profile'
import { getErrorMessage } from '@/utils'

type AuthContextType = {
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (username: string, password: string) => Promise<void>
  register: (payload: RegisterRequest) => Promise<void>
  logout: () => Promise<void>
  refreshUser: () => Promise<User>
}

const ACCESS_TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'
const AUTH_USER_KEY = 'auth_user'

const AuthContext = createContext<AuthContextType | undefined>(undefined)

function buildAvatar(username: string) {
  return username.trim().charAt(0).toUpperCase() || 'U'
}

function mapUser(
  account: UserMeResponse,
  profile?: ProfileResponse | null,
): User {
  const fullName = [profile?.lastName, profile?.firstName]
    .filter((value) => Boolean(value && value.trim() !== ''))
    .join(' ')

  return {
    id: account.userId,
    username: account.username,
    email: account.email,
    phoneNumber: account.phoneNumber,
    status: account.status,
    locked: account.locked,
    roles: account.roles,
    role: account.roles[0] ?? 'USER',
    name: fullName || account.username,
    avatar: buildAvatar(profile?.firstName || account.username),
    createdAt: account.createdAt,
    updatedAt: account.updatedAt,
  }
}

function getStoredUser() {
  const storedUser = localStorage.getItem(AUTH_USER_KEY)

  if (!storedUser) {
    return null
  }

  try {
    return JSON.parse(storedUser) as User
  } catch {
    localStorage.removeItem(AUTH_USER_KEY)
    return null
  }
}

function persistTokens(accessToken: string, refreshToken: string) {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
}

function persistUser(user: User) {
  localStorage.setItem(AUTH_USER_KEY, JSON.stringify(user))
}

function clearSession() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(AUTH_USER_KEY)
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const { t } = useLanguage()
  const [user, setUser] = useState<User | null>(() => getStoredUser())
  const [isLoading, setIsLoading] = useState(true)

  useEffect(() => {
    void hydrateAuth()
  }, [])

  async function syncCurrentUser() {
    const [account, profileResult] = await Promise.all([
      getCurrentUser(),
      getCurrentProfile().catch(() => null),
    ])
    const nextUser = mapUser(account, profileResult)
    setUser(nextUser)
    persistUser(nextUser)
    return nextUser
  }

  async function applyLoginSession(credentials: LoginRequest) {
    const session = await loginRequest(credentials)
    persistTokens(session.accessToken, session.refreshToken)
    await syncCurrentUser()
  }

  async function hydrateAuth() {
    const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY)
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)

    if (!accessToken && !refreshToken) {
      clearSession()
      setUser(null)
      setIsLoading(false)
      return
    }

    try {
      if (!accessToken) {
        throw new Error('Missing access token')
      }

      await syncCurrentUser()
    } catch {
      if (refreshToken) {
        try {
          const session = await refreshAccessToken({ refreshToken })
          persistTokens(session.accessToken, session.refreshToken)
          await syncCurrentUser()
        } catch {
          clearSession()
          setUser(null)
        }
      } else {
        clearSession()
        setUser(null)
      }
    } finally {
      setIsLoading(false)
    }
  }

  async function login(username: string, password: string) {
    try {
      await applyLoginSession({ username, password })
    } catch (error) {
      throw new Error(getErrorMessage(error, t('auth.login.errorFallback')))
    }
  }

  async function register(payload: RegisterRequest) {
    try {
      await registerRequest(payload)
      await applyLoginSession({
        username: payload.username,
        password: payload.password,
      })
    } catch (error) {
      throw new Error(getErrorMessage(error, t('auth.register.errorFallback')))
    }
  }

  async function logout() {
    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY)

    clearSession()
    setUser(null)

    if (!refreshToken) {
      return
    }

    try {
      await logoutRequest({ refreshToken })
    } catch {
      // Ignore logout API failures because the local session is already cleared.
    }
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isAuthenticated: !!user,
        isLoading,
        login,
        register,
        logout,
        refreshUser: syncCurrentUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)

  if (context === undefined) {
    throw new Error('useAuth must be used within AuthProvider')
  }

  return context
}
