import api from './api'
import { getBookReferences } from '@/services/book-service'
import { toPageResult } from '@/services/pagination'
import type { ApiResponse } from '@/types/api'
import type {
  AuthorResponse,
  BookResponse,
  CategoryResponse,
  PublisherResponse,
} from '@/types/book'
import type {
  CheckInReadingStreakRequest,
  CreateReadingJournalEntryRequest,
  ReadingJournalBook,
  ReadingJournalEntry,
  ReadingJournalEntryResponse,
  ReadingJournalFilter,
  ReadingJournalPageResult,
  ReadingStreakResponse,
  UpdateReadingJournalEntryRequest,
} from '@/types/reading-journal'
import { unwrapResponse } from '@/utils'
import { getBookCoverUrl } from '@/utils/book-cover'
import { buildReadingJournalQueryParams } from '@/utils/reading-journal'

type BookReferenceMaps = {
  authorMap: Map<string, string>
  categoryMap: Map<string, string>
  publisherMap: Map<string, string>
}

export { buildReadingJournalQueryParams } from '@/utils/reading-journal'

export async function getMyReadingJournalEntries(
  filter: ReadingJournalFilter = {},
): Promise<ReadingJournalPageResult> {
  const request = buildReadingJournalQueryParams(filter)
  const [response, referenceMaps] = await Promise.all([
    api.get<ApiResponse<ReadingJournalEntryResponse[]>>('/reading-journal', {
      params: request,
    }),
    getReadingJournalReferenceMaps(),
  ])

  return toPageResult(
    unwrapResponse(response).map((entry) =>
      mapReadingJournalEntryResponse(entry, referenceMaps),
    ),
    response.headers,
    request,
  )
}

export async function createReadingJournalEntry(
  payload: CreateReadingJournalEntryRequest,
): Promise<ReadingJournalEntry> {
  const [response, referenceMaps] = await Promise.all([
    api.post<ApiResponse<ReadingJournalEntryResponse>>(
      '/reading-journal',
      sanitizeCreatePayload(payload),
    ),
    getReadingJournalReferenceMaps(),
  ])

  return mapReadingJournalEntryResponse(unwrapResponse(response), referenceMaps)
}

export async function updateReadingJournalEntry(
  entryId: string,
  payload: UpdateReadingJournalEntryRequest,
): Promise<ReadingJournalEntry> {
  const [response, referenceMaps] = await Promise.all([
    api.put<ApiResponse<ReadingJournalEntryResponse>>(
      `/reading-journal/${entryId}`,
      sanitizeUpdatePayload(payload),
    ),
    getReadingJournalReferenceMaps(),
  ])

  return mapReadingJournalEntryResponse(unwrapResponse(response), referenceMaps)
}

export async function deleteReadingJournalEntry(entryId: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/reading-journal/${entryId}`)
}

export async function getMyReadingStreak(): Promise<ReadingStreakResponse> {
  const response = await api.get<ApiResponse<ReadingStreakResponse>>(
    '/reading-streak',
  )

  return unwrapResponse(response)
}

export async function checkInReadingStreak(
  payload: CheckInReadingStreakRequest,
): Promise<ReadingStreakResponse> {
  const response = await api.post<ApiResponse<ReadingStreakResponse>>(
    '/reading-streak/check-in',
    sanitizeCheckInPayload(payload),
  )

  return unwrapResponse(response)
}

function sanitizeCreatePayload(payload: CreateReadingJournalEntryRequest) {
  return {
    bookId: payload.bookId,
    entryDate: payload.entryDate,
    note: normalizeOptionalString(payload.note),
    currentPage:
      typeof payload.currentPage === 'number' ? payload.currentPage : null,
    progressPercent:
      typeof payload.progressPercent === 'number'
        ? payload.progressPercent
        : null,
  }
}

function sanitizeUpdatePayload(payload: UpdateReadingJournalEntryRequest) {
  return {
    note: normalizeOptionalString(payload.note),
    currentPage:
      typeof payload.currentPage === 'number' ? payload.currentPage : null,
    progressPercent:
      typeof payload.progressPercent === 'number'
        ? payload.progressPercent
        : null,
  }
}

function sanitizeCheckInPayload(payload: CheckInReadingStreakRequest) {
  return {
    bookId: payload.bookId,
    note: normalizeOptionalString(payload.note),
    currentPage:
      typeof payload.currentPage === 'number' ? payload.currentPage : null,
    progressPercent:
      typeof payload.progressPercent === 'number'
        ? payload.progressPercent
        : null,
  }
}

function normalizeOptionalString(value?: string | null) {
  const normalizedValue = value?.trim() ?? ''
  return normalizedValue === '' ? null : normalizedValue
}

async function getReadingJournalReferenceMaps(): Promise<BookReferenceMaps> {
  const referenceData = await getBookReferences()

  return buildBookReferenceMaps(
    referenceData.categories,
    referenceData.authors,
    referenceData.publishers,
  )
}

function buildBookReferenceMaps(
  categories: CategoryResponse[],
  authors: AuthorResponse[],
  publishers: PublisherResponse[],
): BookReferenceMaps {
  return {
    categoryMap: new Map(categories.map((category) => [category.id, category.name])),
    authorMap: new Map(authors.map((author) => [author.id, author.name])),
    publisherMap: new Map(
      publishers.map((publisher) => [publisher.id, publisher.name]),
    ),
  }
}

function mapReadingJournalEntryResponse(
  entry: ReadingJournalEntryResponse,
  referenceMaps: BookReferenceMaps,
): ReadingJournalEntry {
  return {
    id: entry.id,
    entryDate: entry.entryDate,
    note: entry.note,
    currentPage: entry.currentPage,
    progressPercent: normalizeNumericValue(entry.progressPercent),
    createdAt: entry.createdAt,
    updatedAt: entry.updatedAt,
    book: mapBookResponseToReadingJournalBook(entry.book, referenceMaps),
  }
}

function mapBookResponseToReadingJournalBook(
  book: BookResponse | null,
  referenceMaps: BookReferenceMaps,
): ReadingJournalBook {
  if (!book) {
    return {
      id: 'missing-book',
      title: 'Unknown book',
      author: '',
      category: '',
      price: 0,
      cover: getBookCoverUrl(),
      stockQuantity: 0,
    }
  }

  const images = book.images ?? []
  const primaryImage =
    images.find((image) => image.primaryImage)?.imageUrl ??
    images[0]?.imageUrl ??
    book.imageUrl

  return {
    id: book.id,
    title: book.title,
    author: referenceMaps.authorMap.get(book.authorId) ?? '',
    category: referenceMaps.categoryMap.get(book.categoryId) ?? '',
    price: book.price,
    cover: resolveReadingJournalBookCover(primaryImage),
    stockQuantity: book.stockQuantity,
    rating: normalizeNumericValue(book.averageRating) ?? undefined,
    reviews: book.reviewCount,
  }
}

function resolveReadingJournalBookCover(imageUrl: string | null | undefined) {
  if (!imageUrl) {
    return getBookCoverUrl()
  }

  if (
    imageUrl.startsWith('http://') ||
    imageUrl.startsWith('https://') ||
    imageUrl.startsWith('/')
  ) {
    return imageUrl
  }

  return getBookCoverUrl(imageUrl)
}

function normalizeNumericValue(value: number | null | undefined) {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return null
  }

  return Number(value.toFixed(1))
}
