import api from './api'
import type { ApiResponse } from '@/types/api'
import type { ProfileResponse, UpdateProfileRequest } from '@/types/profile'
import { unwrapResponse } from '@/utils'

export async function getCurrentProfile(): Promise<ProfileResponse> {
  const response = await api.get<ApiResponse<ProfileResponse>>('/profiles/me')
  return unwrapResponse(response)
}

export async function updateCurrentProfile(
  data: UpdateProfileRequest,
): Promise<ProfileResponse> {
  const response = await api.put<ApiResponse<ProfileResponse>>(
    '/profiles/me',
    data,
  )
  return unwrapResponse(response)
}
