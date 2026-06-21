import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  DigitalAssetResponse,
  DigitalLibraryAssetResponse,
  DigitalLibraryItemResponse,
  PublishedDigitalAssetResponse,
  UpdateReadingProgressRequest,
  UpsertDigitalAssetRequest,
} from '@/types/digital-library'
import { unwrapResponse } from '@/utils'

export async function getMyDigitalLibrary(): Promise<
  DigitalLibraryItemResponse[]
> {
  const response = await api.get<ApiResponse<DigitalLibraryItemResponse[]>>(
    '/digital-library/me/assets',
  )

  return unwrapResponse(response)
}

export async function getMyDigitalLibraryAsset(
  digitalAssetId: string,
): Promise<DigitalLibraryAssetResponse> {
  const response = await api.get<ApiResponse<DigitalLibraryAssetResponse>>(
    `/digital-library/me/assets/${digitalAssetId}`,
  )

  return unwrapResponse(response)
}

export async function updateMyReadingProgress(
  digitalAssetId: string,
  payload: UpdateReadingProgressRequest,
) {
  const response = await api.put<ApiResponse<DigitalLibraryAssetResponse['progress']>>(
    `/digital-library/me/assets/${digitalAssetId}/progress`,
    {
      currentPage:
        typeof payload.currentPage === 'number' ? payload.currentPage : null,
      progressPercent: payload.progressPercent,
      positionData: normalizeOptionalString(payload.positionData),
    },
  )

  return unwrapResponse(response)
}

export async function getPublishedDigitalAssetsByBookId(
  bookId: string,
): Promise<PublishedDigitalAssetResponse[]> {
  const response = await api.get<ApiResponse<PublishedDigitalAssetResponse[]>>(
    `/books/${bookId}/digital-assets`,
  )

  return unwrapResponse(response)
}

export async function getAdminDigitalAssetsByBookId(
  bookId: string,
): Promise<DigitalAssetResponse[]> {
  const response = await api.get<ApiResponse<DigitalAssetResponse[]>>(
    `/admin/books/${bookId}/digital-assets`,
  )

  return unwrapResponse(response)
}

export async function createAdminDigitalAsset(
  bookId: string,
  payload: UpsertDigitalAssetRequest,
): Promise<DigitalAssetResponse> {
  const response = await api.post<ApiResponse<DigitalAssetResponse>>(
    `/admin/books/${bookId}/digital-assets`,
    sanitizeDigitalAssetPayload(payload),
  )

  return unwrapResponse(response)
}

export async function updateAdminDigitalAsset(
  bookId: string,
  digitalAssetId: string,
  payload: UpsertDigitalAssetRequest,
): Promise<DigitalAssetResponse> {
  const response = await api.put<ApiResponse<DigitalAssetResponse>>(
    `/admin/books/${bookId}/digital-assets/${digitalAssetId}`,
    sanitizeDigitalAssetPayload(payload),
  )

  return unwrapResponse(response)
}

export async function deleteAdminDigitalAsset(bookId: string, digitalAssetId: string) {
  await api.delete<ApiResponse<null>>(
    `/admin/books/${bookId}/digital-assets/${digitalAssetId}`,
  )
}

function sanitizeDigitalAssetPayload(payload: UpsertDigitalAssetRequest) {
  return {
    format: payload.format,
    title: payload.title.trim(),
    fileName: payload.fileName.trim(),
    storageKey: payload.storageKey.trim(),
    mimeType: payload.mimeType.trim(),
    fileSize: payload.fileSize,
    checksum: normalizeOptionalString(payload.checksum),
    sampleStorageKey: normalizeOptionalString(payload.sampleStorageKey),
    price: payload.price,
    downloadAllowed: payload.downloadAllowed,
    published: payload.published,
  }
}

function normalizeOptionalString(value?: string | null) {
  const normalizedValue = value?.trim() ?? ''
  return normalizedValue === '' ? null : normalizedValue
}
