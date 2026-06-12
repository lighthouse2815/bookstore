import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  GoogleLoginRequest,
  LoginRequest,
  LoginResponse,
  LogoutRequest,
  PasswordResetTokenResponse,
  RefreshTokenRequest,
  RegisterRequest,
  RegisterResponse,
  RequestPasswordResetOtpRequest,
  RequestRegistrationOtpRequest,
  ResetPasswordRequest,
  UpdateUserRequest,
  UserMeResponse,
  VerifyOtpRequest,
} from '@/types/auth'
import { unwrapResponse } from '@/utils'

export async function login(data: LoginRequest): Promise<LoginResponse> {
  const response = await api.post<ApiResponse<LoginResponse>>('/auth/login', data)
  return unwrapResponse(response)
}

export async function loginWithGoogle(
  data: GoogleLoginRequest,
): Promise<LoginResponse> {
  const response = await api.post<ApiResponse<LoginResponse>>(
    '/auth/google',
    data,
  )
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

export async function verifyRegistrationOtp(
  data: VerifyOtpRequest,
): Promise<void> {
  await api.post<ApiResponse<null>>('/otp/verify', data)
}

export async function requestRegistrationOtp(
  data: RequestRegistrationOtpRequest,
): Promise<void> {
  await api.post<ApiResponse<null>>('/otp/request', data)
}

export async function requestPasswordResetOtp(
  data: RequestPasswordResetOtpRequest,
): Promise<void> {
  await api.post<ApiResponse<null>>('/auth/forgot-password/request-otp', data)
}

export async function verifyPasswordResetOtp(
  data: VerifyOtpRequest,
): Promise<PasswordResetTokenResponse> {
  const response = await api.post<ApiResponse<PasswordResetTokenResponse>>(
    '/auth/forgot-password/verify-otp',
    data,
  )
  return unwrapResponse(response)
}

export async function resetPassword(
  data: ResetPasswordRequest,
): Promise<void> {
  await api.post<ApiResponse<null>>('/auth/forgot-password/reset', data)
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

export async function updateCurrentUser(
  data: UpdateUserRequest,
): Promise<UserMeResponse> {
  const response = await api.put<ApiResponse<UserMeResponse>>('/users/me', data)
  return unwrapResponse(response)
}
