import type { AxiosResponse } from 'axios'
import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  DigitalAssetResponse,
  DigitalLibraryAssetResponse,
  DigitalLibraryItemResponse,
  DigitalLibraryPageResult,
  PublishedDigitalAssetCatalogPageResult,
  PublishedDigitalAssetCatalogItemResponse,
  PublishedDigitalAssetResponse,
  UpdateReadingProgressRequest,
  UpsertDigitalAssetRequest,
} from '@/types/digital-library'
import type { SignedUrlResponse } from '@/types/file'
import { toPageResult } from '@/services/pagination'
import type { PageRequest } from '@/types/pagination'
import { unwrapResponse } from '@/utils'

const DEFAULT_LIBRARY_PAGE_SIZE = 12

export async function getMyDigitalLibrary(params: {
  page?: number
  size?: number
} = {}): Promise<DigitalLibraryPageResult> {
  const page = params.page ?? 0
  const size = params.size ?? DEFAULT_LIBRARY_PAGE_SIZE
  const response = await api.get<ApiResponse<DigitalLibraryItemResponse[]>>(
    '/digital-library/me/assets',
    {
      params: { page, size },
    },
  )

  return parseLibraryPageResponse(response, page, size)
}

export async function getMyDigitalLibraryAsset(
  digitalAssetId: string,
): Promise<DigitalLibraryAssetResponse> {
  const response = await api.get<ApiResponse<DigitalLibraryAssetResponse>>(
    `/digital-library/me/assets/${digitalAssetId}`,
  )

  return unwrapResponse(response)
}

export async function getMyDigitalAssetReadUrl(
  digitalAssetId: string,
): Promise<SignedUrlResponse> {
  const response = await api.get<ApiResponse<SignedUrlResponse>>(
    `/digital-library/me/assets/${digitalAssetId}/read-url`,
  )

  return unwrapResponse(response)
}

export async function getMyDigitalAssetDownloadUrl(
  digitalAssetId: string,
): Promise<SignedUrlResponse> {
  const response = await api.get<ApiResponse<SignedUrlResponse>>(
    `/digital-library/me/assets/${digitalAssetId}/download-url`,
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

export async function getPublishedDigitalAssetCatalog(
  request: PageRequest & {
    keyword?: string
    categoryId?: string
  } = {},
): Promise<PublishedDigitalAssetCatalogPageResult> {
  const pageRequest = {
    page: request.page ?? 0,
    size: request.size ?? DEFAULT_LIBRARY_PAGE_SIZE,
  }
  const response = await api.get<ApiResponse<PublishedDigitalAssetCatalogItemResponse[]>>(
    '/ebooks',
    {
      params: {
        ...pageRequest,
        keyword: request.keyword?.trim() || undefined,
        categoryId: request.categoryId?.trim() || undefined,
      },
    },
  )

  return toPageResult(unwrapResponse(response), response.headers, pageRequest)
}

export async function getPublishedDigitalAssetSampleUrl(
  bookId: string,
  digitalAssetId: string,
): Promise<SignedUrlResponse> {
  const response = await api.get<ApiResponse<SignedUrlResponse>>(
    `/books/${bookId}/digital-assets/${digitalAssetId}/sample-url`,
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
    fileAssetId: payload.fileAssetId,
    sampleFileAssetId: payload.sampleFileAssetId ?? null,
    price: payload.price,
    downloadAllowed: payload.downloadAllowed,
    purchaseAllowed: payload.purchaseAllowed,
    published: payload.published,
  }
}

function normalizeOptionalString(value?: string | null) {
  const normalizedValue = value?.trim() ?? ''
  return normalizedValue === '' ? null : normalizedValue
}

function parseLibraryPageResponse(
  response: AxiosResponse<ApiResponse<DigitalLibraryItemResponse[]>>,
  fallbackPage: number,
  fallbackSize: number,
): DigitalLibraryPageResult {
  const items = unwrapResponse(response)
  const page = parseNumberHeader(response.headers['x-page'], fallbackPage)
  const size = parseNumberHeader(response.headers['x-size'], fallbackSize)
  const totalCount = parseNumberHeader(response.headers['x-total-count'], items.length)
  const hasNext = parseBooleanHeader(
    response.headers['x-has-next'],
    (page + 1) * size < totalCount,
  )

  return {
    items,
    page,
    size,
    totalCount,
    hasNext,
  }
}

function parseNumberHeader(value: unknown, fallbackValue: number) {
  const parsedValue = Number(value)
  return Number.isFinite(parsedValue) ? parsedValue : fallbackValue
}

function parseBooleanHeader(value: unknown, fallbackValue: boolean) {
  if (typeof value === 'string') {
    return value.toLowerCase() === 'true'
  }

  return fallbackValue
}
