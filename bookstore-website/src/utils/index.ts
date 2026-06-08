import axios, { type AxiosResponse } from 'axios'
import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'
import type { ApiResponse } from '@/types/api'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function unwrapResponse<T>(response: AxiosResponse<ApiResponse<T>>): T {
  return response.data.data
}

export function getErrorMessage(
  error: unknown,
  fallbackMessage = 'Something went wrong',
) {
  if (axios.isAxiosError<ApiResponse<unknown>>(error)) {
    return error.response?.data.message || error.message || fallbackMessage
  }

  if (error instanceof Error && error.message.trim() !== '') {
    return error.message
  }

  return fallbackMessage
}
