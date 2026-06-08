import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  LoginRequest,
  LoginResponse,
  LogoutRequest,
  RefreshTokenRequest,
  RegisterRequest,
  RegisterResponse,
  UserMeResponse,
} from '@/types/auth'
import { unwrapResponse } from '@/utils'

export async function login(data: LoginRequest): Promise<LoginResponse> {
  const response = await api.post<ApiResponse<LoginResponse>>('/auth/login', data)
  return unwrapResponse(response)
}

export async function register(
  data: RegisterRequest,
): Promise<RegisterResponse> {
  const response = await api.post<ApiResponse<RegisterResponse>>(
    '/auth/register',
    data,
  )
  return unwrapResponse(response)
}

export async function refreshAccessToken(
  data: RefreshTokenRequest,
): Promise<LoginResponse> {
  const response = await api.post<ApiResponse<LoginResponse>>(
    '/auth/refresh',
    data,
  )
  return unwrapResponse(response)
}

export async function logout(data: LogoutRequest): Promise<void> {
  await api.post<ApiResponse<null>>('/auth/logout', data)
}

export async function getCurrentUser(): Promise<UserMeResponse> {
  const response = await api.get<ApiResponse<UserMeResponse>>('/users/me')
  return unwrapResponse(response)
}
