import api from './api'
import { getBookReferences } from '@/services/book-service'
import type { ApiResponse } from '@/types/api'
import type {
  AuthorResponse,
  BookResponse,
  CategoryResponse,
  PublisherResponse,
} from '@/types/book'
import type {
  Bookshelf,
  BookshelfBook,
  BookshelfItem,
  BookshelfResponse,
  BookshelfSummary,
  BookshelfSummaryResponse,
} from '@/types/bookshelf'
import { unwrapResponse } from '@/utils'
import { getBookCoverUrl } from '@/utils/book-cover'

type BookReferenceMaps = {
  authorMap: Map<string, string>
  categoryMap: Map<string, string>
  publisherMap: Map<string, string>
}

export async function getMyBookshelves(): Promise<BookshelfSummary[]> {
  const response = await api.get<ApiResponse<BookshelfSummaryResponse[]>>(
    '/bookshelves',
  )

  return unwrapResponse(response).map(mapBookshelfSummaryResponse)
}

export async function getMyBookshelf(shelfId: string): Promise<Bookshelf> {
  const [response, referenceMaps] = await Promise.all([
    api.get<ApiResponse<BookshelfResponse>>(`/bookshelves/${shelfId}`),
    getBookshelfReferenceMaps(),
  ])

  return mapBookshelfResponse(unwrapResponse(response), referenceMaps)
}

export async function createBookshelf(name: string): Promise<Bookshelf> {
  const [response, referenceMaps] = await Promise.all([
    api.post<ApiResponse<BookshelfResponse>>('/bookshelves', { name }),
    getBookshelfReferenceMaps(),
  ])

  return mapBookshelfResponse(unwrapResponse(response), referenceMaps)
}

export async function updateBookshelf(
  shelfId: string,
  name: string,
): Promise<Bookshelf> {
  const [response, referenceMaps] = await Promise.all([
    api.put<ApiResponse<BookshelfResponse>>(`/bookshelves/${shelfId}`, { name }),
    getBookshelfReferenceMaps(),
  ])

  return mapBookshelfResponse(unwrapResponse(response), referenceMaps)
}

export async function deleteBookshelf(shelfId: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/bookshelves/${shelfId}`)
}

export async function addBookToShelf(
  shelfId: string,
  bookId: string,
): Promise<Bookshelf> {
  const [response, referenceMaps] = await Promise.all([
    api.post<ApiResponse<BookshelfResponse>>(
      `/bookshelves/${shelfId}/items/${bookId}`,
    ),
    getBookshelfReferenceMaps(),
  ])

  return mapBookshelfResponse(unwrapResponse(response), referenceMaps)
}

export async function removeBookFromShelf(
  shelfId: string,
  bookId: string,
): Promise<Bookshelf> {
  const [response, referenceMaps] = await Promise.all([
    api.delete<ApiResponse<BookshelfResponse>>(
      `/bookshelves/${shelfId}/items/${bookId}`,
    ),
    getBookshelfReferenceMaps(),
  ])

  return mapBookshelfResponse(unwrapResponse(response), referenceMaps)
}

export async function reorderShelfItems(
  shelfId: string,
  itemIds: string[],
): Promise<Bookshelf> {
  const [response, referenceMaps] = await Promise.all([
    api.put<ApiResponse<BookshelfResponse>>(
      `/bookshelves/${shelfId}/items/reorder`,
      { itemIds },
    ),
    getBookshelfReferenceMaps(),
  ])

  return mapBookshelfResponse(unwrapResponse(response), referenceMaps)
}

function mapBookshelfSummaryResponse(
  bookshelf: BookshelfSummaryResponse,
): BookshelfSummary {
  return {
    id: bookshelf.id,
    name: bookshelf.name,
    bookCount: bookshelf.bookCount,
    createdAt: bookshelf.createdAt,
    updatedAt: bookshelf.updatedAt,
  }
}

function mapBookshelfResponse(
  bookshelf: BookshelfResponse,
  referenceMaps: BookReferenceMaps,
): Bookshelf {
  return {
    id: bookshelf.id,
    name: bookshelf.name,
    bookCount: bookshelf.bookCount,
    items: bookshelf.items.map((item) =>
      mapBookshelfItemResponse(item, referenceMaps),
    ),
    createdAt: bookshelf.createdAt,
    updatedAt: bookshelf.updatedAt,
  }
}

function mapBookshelfItemResponse(
  item: BookshelfResponse['items'][number],
  referenceMaps: BookReferenceMaps,
): BookshelfItem {
  return {
    id: item.id,
    sortOrder: item.sortOrder,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt,
    book: mapBookResponseToBookshelfBook(item.book, referenceMaps),
  }
}

function mapBookResponseToBookshelfBook(
  book: BookResponse,
  referenceMaps: BookReferenceMaps,
): BookshelfBook {
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
    cover: resolveBookshelfBookCover(primaryImage),
    rating: normalizeRatingValue(book.averageRating) ?? undefined,
    reviews: book.reviewCount,
    stockQuantity: book.stockQuantity,
  }
}

async function getBookshelfReferenceMaps(): Promise<BookReferenceMaps> {
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

function resolveBookshelfBookCover(imageUrl: string | null | undefined) {
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

function normalizeRatingValue(value: number | null | undefined) {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return null
  }

  return Number(value.toFixed(1))
}
