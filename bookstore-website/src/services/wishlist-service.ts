import api from './api'
import { getBookReferences } from '@/services/book-service'
import type { ApiResponse } from '@/types/api'
import type { BookCardData, BookResponse } from '@/types/book'
import { unwrapResponse } from '@/utils'
import { getBookCoverUrl } from '@/utils/book-cover'

export async function getMyWishlist(): Promise<BookCardData[]> {
  const [response, referenceData] = await Promise.all([
    api.get<ApiResponse<BookResponse[]>>('/wishlist'),
    getBookReferences(),
  ])

  const authorMap = new Map(
    referenceData.authors.map((author) => [author.id, author.name]),
  )
  const categoryMap = new Map(
    referenceData.categories.map((category) => [category.id, category]),
  )

  return unwrapResponse(response).map((bookResponse) => ({
    id: bookResponse.id,
    title: bookResponse.title,
    author: authorMap.get(bookResponse.authorId) ?? '',
    category: categoryMap.get(bookResponse.categoryId)?.name ?? '',
    categoryInfo: categoryMap.get(bookResponse.categoryId) ?? null,
    price: bookResponse.price,
    rating:
      typeof bookResponse.averageRating === 'number'
        ? bookResponse.averageRating
        : undefined,
    reviews: bookResponse.reviewCount,
    cover: resolveWishlistBookCover(bookResponse),
  }))
}

export async function addWishlistBook(bookId: string): Promise<void> {
  await api.post<ApiResponse<null>>(`/wishlist/items/${bookId}`)
}

export async function removeWishlistBook(bookId: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/wishlist/items/${bookId}`)
}

function resolveWishlistBookCover(bookResponse: BookResponse) {
  const primaryImage =
    (bookResponse.images ?? []).find((image) => image.primaryImage)?.imageUrl ??
    bookResponse.images?.[0]?.imageUrl ??
    bookResponse.imageUrl

  if (
    primaryImage?.startsWith('http://') ||
    primaryImage?.startsWith('https://') ||
    primaryImage?.startsWith('/')
  ) {
    return primaryImage
  }

  return getBookCoverUrl(primaryImage)
}
