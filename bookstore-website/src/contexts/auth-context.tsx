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
  loginWithGoogle as loginWithGoogleRequest,
  logout as logoutRequest,
  refreshAccessToken,
  requestPasswordResetOtp as requestPasswordResetOtpRequest,
  requestRegistrationOtp as requestRegistrationOtpRequest,
  resetPassword as resetPasswordRequest,
  register as registerRequest,
  verifyPasswordResetOtp as verifyPasswordResetOtpRequest,
  verifyRegistrationOtp as verifyRegistrationOtpRequest,
} from '@/services/auth-service'
import { useLanguage } from '@/contexts/language-context'
import { getCurrentProfile } from '@/services/profile-service'
import type {
  LoginRequest,
  LoginResponse,
  PasswordResetTokenResponse,
  RegisterRequest,
  RequestPasswordResetOtpRequest,
  RequestRegistrationOtpRequest,
  ResetPasswordRequest,
  User,
  UserMeResponse,
  VerifyOtpRequest,
} from '@/types/auth'
import type { ProfileResponse } from '@/types/profile'
import { getErrorMessage } from '@/utils'
import {
  createLoginRestrictionMessage,
  getLoginRestrictionCopy,
} from '@/utils/login-restrictions'

type AuthContextType = {
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (username: string, password: string) => Promise<void>
  loginWithGoogle: (idToken: string) => Promise<void>
  register: (payload: RegisterRequest) => Promise<void>
  requestRegistrationOtp: (payload: RequestRegistrationOtpRequest) => Promise<void>
  verifyRegistrationOtp: (payload: VerifyOtpRequest) => Promise<void>
  requestPasswordResetOtp: (
    payload: RequestPasswordResetOtpRequest,
  ) => Promise<void>
  verifyPasswordResetOtp: (
    payload: VerifyOtpRequest,
  ) => Promise<PasswordResetTokenResponse>
  resetPassword: (payload: ResetPasswordRequest) => Promise<void>
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
  const { language, t } = useLanguage()
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

  async function applySession(session: LoginResponse) {
    persistTokens(session.accessToken, session.refreshToken)
    const nextUser = await syncCurrentUser()

    if (nextUser.locked) {
      const copy = getLoginRestrictionCopy('locked', language)
      clearSession()
      setUser(null)
      throw new Error(createLoginRestrictionMessage('locked', copy.description))
    }

    if (nextUser.status !== 'ACTIVE') {
      const copy = getLoginRestrictionCopy('inactive', language)
      clearSession()
      setUser(null)
      throw new Error(createLoginRestrictionMessage('inactive', copy.description))
    }
  }

  async function applyLoginSession(credentials: LoginRequest) {
    const session = await loginRequest(credentials)
    await applySession(session)
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

  async function loginWithGoogle(idToken: string) {
    try {
      const session = await loginWithGoogleRequest({ idToken })
      await applySession(session)
    } catch (error) {
      throw new Error(getErrorMessage(error, t('auth.login.errorFallback')))
    }
  }

  async function register(payload: RegisterRequest) {
    try {
      await registerRequest(payload)
    } catch (error) {
      throw new Error(getErrorMessage(error, t('auth.register.errorFallback')))
    }
  }

  async function requestRegistrationOtp(
    payload: RequestRegistrationOtpRequest,
  ) {
    try {
      await requestRegistrationOtpRequest(payload)
    } catch (error) {
      throw new Error(
        getErrorMessage(error, getRegistrationOtpRequestFallback(language)),
      )
    }
  }

  async function verifyRegistrationOtp(payload: VerifyOtpRequest) {
    try {
      await verifyRegistrationOtpRequest(payload)
    } catch (error) {
      throw new Error(
        getErrorMessage(error, t('auth.register.verifyErrorFallback')),
      )
    }
  }

  async function requestPasswordResetOtp(
    payload: RequestPasswordResetOtpRequest,
  ) {
    try {
      await requestPasswordResetOtpRequest(payload)
    } catch (error) {
      throw new Error(
        getErrorMessage(error, t('auth.forgotPassword.requestErrorFallback')),
      )
    }
  }

  async function verifyPasswordResetOtp(payload: VerifyOtpRequest) {
    try {
      return await verifyPasswordResetOtpRequest(payload)
    } catch (error) {
      throw new Error(
        getErrorMessage(error, t('auth.forgotPassword.verifyErrorFallback')),
      )
    }
  }

  async function resetPassword(payload: ResetPasswordRequest) {
    try {
      await resetPasswordRequest(payload)
    } catch (error) {
      throw new Error(
        getErrorMessage(error, t('auth.forgotPassword.resetErrorFallback')),
      )
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
        loginWithGoogle,
        register,
        requestRegistrationOtp,
        verifyRegistrationOtp,
        requestPasswordResetOtp,
        verifyPasswordResetOtp,
        resetPassword,
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

function getRegistrationOtpRequestFallback(language: 'vi' | 'en') {
  return language === 'vi'
    ? 'Không thể gửi lại mã OTP kích hoạt'
    : 'Unable to send a new activation OTP'
}
