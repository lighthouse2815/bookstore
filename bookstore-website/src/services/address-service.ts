import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  CreateUserAddressRequest,
  UpdateUserAddressRequest,
  UserAddressResponse,
} from '@/types/address'
import { unwrapResponse } from '@/utils'

export async function getMyAddresses(): Promise<UserAddressResponse[]> {
  const response = await api.get<ApiResponse<UserAddressResponse[]>>(
    '/user-addresses',
  )
  return unwrapResponse(response)
}

export async function createAddress(
  data: CreateUserAddressRequest,
): Promise<UserAddressResponse> {
  const response = await api.post<ApiResponse<UserAddressResponse>>(
    '/user-addresses',
    data,
  )
  return unwrapResponse(response)
}

export async function updateAddress(
  id: string,
  data: UpdateUserAddressRequest,
): Promise<UserAddressResponse> {
  const response = await api.put<ApiResponse<UserAddressResponse>>(
    `/user-addresses/${id}`,
    data,
  )
  return unwrapResponse(response)
}

export async function deleteAddress(id: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/user-addresses/${id}`)
}

export async function setDefaultAddress(
  id: string,
): Promise<UserAddressResponse> {
  const response = await api.put<ApiResponse<UserAddressResponse>>(
    `/user-addresses/${id}/default`,
  )
  return unwrapResponse(response)
}
