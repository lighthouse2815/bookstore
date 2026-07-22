import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  AuthorResponse,
  Book,
  BookCatalog,
  BookCatalogPage,
  BookDetail,
  BookDetailResponse,
  BookImage,
  BookImageResponse,
  BookPageDetail,
  BookPageDetailResponse,
  BookPromotion,
  BookPromotionResponse,
  BookRatingSummary,
  BookRatingSummaryResponse,
  BookReferenceData,
  BookResponse,
  BookReview,
  BookReviewResponse,
  CategoryResponse,
  PublisherResponse,
  SearchBooksRequest,
  UpsertBookRequest,
} from '@/types/book'
import { getBookCoverUrl } from '@/utils/book-cover'
import { getErrorMessage, unwrapResponse } from '@/utils'
import { toPageResult } from '@/services/pagination'
import type { PageRequest } from '@/types/pagination'

type BookCatalogLoadState = BookCatalog & {
  bookError: string | null
  categoryError: string | null
}

export async function getBookCatalogLoadState(
  request: SearchBooksRequest = {},
): Promise<BookCatalogLoadState> {
  const [bookResponsesResult, categoriesResult, authorsResult, publishersResult] =
    await Promise.allSettled([
      getBookResponses(request),
      getCategoryResponses(),
      getAuthorResponses(),
      getPublisherResponses(),
    ])

  const categories =
    categoriesResult.status === 'fulfilled' ? categoriesResult.value : []
  const referenceMaps = buildBookReferenceMaps({
    categories,
    authors: authorsResult.status === 'fulfilled' ? authorsResult.value : [],
    publishers:
      publishersResult.status === 'fulfilled' ? publishersResult.value : [],
  })

  return {
    books:
      bookResponsesResult.status === 'fulfilled'
        ? bookResponsesResult.value.map((bookResponse) =>
            mapBookResponseToBook(bookResponse, referenceMaps),
          )
        : [],
    categories: sortCategories(categories),
    categoryIds: getCategoryIds(categories),
    bookError:
      bookResponsesResult.status === 'rejected'
        ? getErrorMessage(bookResponsesResult.reason)
        : null,
    categoryError:
      categoriesResult.status === 'rejected'
        ? getErrorMessage(categoriesResult.reason)
        : null,
  }
}

export async function getBookCatalog(
  request: SearchBooksRequest = {},
): Promise<BookCatalog> {
  const [bookResponses, referenceData] = await Promise.all([
    getBookResponses(request),
    getBookReferenceData(),
  ])
  const referenceMaps = buildBookReferenceMaps(referenceData)

  return {
    books: bookResponses.map((bookResponse) =>
      mapBookResponseToBook(bookResponse, referenceMaps),
    ),
    categories: sortCategories(referenceData.categories),
    categoryIds: getCategoryIds(referenceData.categories),
  }
}

export async function getBookCatalogPage(
  request: SearchBooksRequest & PageRequest = {},
): Promise<BookCatalogPage> {
  const [bookPage, referenceData] = await Promise.all([
    getBookResponsesPage(request),
    getBookReferenceData(),
  ])
  const referenceMaps = buildBookReferenceMaps(referenceData)

  return {
    books: bookPage.items.map((bookResponse) =>
      mapBookResponseToBook(bookResponse, referenceMaps),
    ),
    categories: sortCategories(referenceData.categories),
    categoryIds: getCategoryIds(referenceData.categories),
    totalCount: bookPage.totalCount,
    page: bookPage.page,
    size: bookPage.size,
    hasNext: bookPage.hasNext,
    totalPages: bookPage.totalPages,
  }
}

export async function getBookById(id: string): Promise<Book> {
  const [bookResponse, referenceData] = await Promise.all([
    getBookResponseById(id),
    getBookReferenceData(),
  ])
  const referenceMaps = buildBookReferenceMaps(referenceData)

  return mapBookResponseToBook(bookResponse, referenceMaps)
}

export async function getBookPageDetail(id: string): Promise<BookPageDetail> {
  const [pageDetailResponse, referenceData] = await Promise.all([
    getBookPageDetailResponseById(id),
    getBookReferenceData(),
  ])
  const referenceMaps = buildBookReferenceMaps(referenceData)

  return mapBookPageDetailResponseToBookPageDetail(
    pageDetailResponse,
    referenceMaps,
  )
}

export async function getBookReviews(id: string): Promise<BookReview[]> {
  const response = await api.get<ApiResponse<BookReviewResponse[]>>(
    `/books/${id}/reviews`,
  )

  return unwrapResponse(response).map(mapBookReviewResponseToBookReview)
}

export async function getBookReferences(): Promise<BookReferenceData> {
  return getBookReferenceData()
}

export async function createBook(
  data: UpsertBookRequest,
): Promise<BookResponse> {
  const response = await api.post<ApiResponse<BookResponse>>('/admin/books', data)
  return unwrapResponse(response)
}

export async function updateBook(
  id: string,
  data: UpsertBookRequest,
): Promise<BookResponse> {
  const response = await api.put<ApiResponse<BookResponse>>(
    `/admin/books/${id}`,
    data,
  )
  return unwrapResponse(response)
}

export async function deleteBook(id: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/admin/books/${id}`)
}

async function getBookResponses(
  request: SearchBooksRequest,
): Promise<BookResponse[]> {
  const keyword = request.keyword?.trim()
  const endpoint = keyword ? '/books/search' : '/books'

  const response = await api.get<ApiResponse<BookResponse[]>>(endpoint, {
    params: keyword ? { keyword } : undefined,
  })

  return unwrapResponse(response)
}

async function getBookResponsesPage(request: SearchBooksRequest & PageRequest) {
  const keyword = request.keyword?.trim()
  const categoryId = request.categoryId?.trim()
  const endpoint = keyword || categoryId ? '/books/search' : '/books'
  const pageRequest = { page: request.page ?? 0, size: request.size ?? 10 }
  const response = await api.get<ApiResponse<BookResponse[]>>(endpoint, {
    params:
      keyword || categoryId
        ? { ...pageRequest, keyword, categoryId }
        : pageRequest,
  })

  return toPageResult(unwrapResponse(response), response.headers, pageRequest)
}

async function getBookResponseById(id: string): Promise<BookResponse> {
  const response = await api.get<ApiResponse<BookResponse>>(`/books/${id}`)
  return unwrapResponse(response)
}

async function getBookPageDetailResponseById(
  id: string,
): Promise<BookPageDetailResponse> {
  const response = await api.get<ApiResponse<BookPageDetailResponse>>(
    `/books/${id}/page-detail`,
  )
  return unwrapResponse(response)
}

async function getCategoryResponses(): Promise<CategoryResponse[]> {
  const response = await api.get<ApiResponse<CategoryResponse[]>>('/categories')
  return unwrapResponse(response)
}

async function getAuthorResponses(): Promise<AuthorResponse[]> {
  const response = await api.get<ApiResponse<AuthorResponse[]>>('/authors')
  return unwrapResponse(response)
}

async function getPublisherResponses(): Promise<PublisherResponse[]> {
  const response = await api.get<ApiResponse<PublisherResponse[]>>('/publishers')
  return unwrapResponse(response)
}

export async function getBookReferenceData(): Promise<BookReferenceData> {
  const [categoriesResult, authorsResult, publishersResult] =
    await Promise.allSettled([
      getCategoryResponses(),
      getAuthorResponses(),
      getPublisherResponses(),
    ])

  return {
    categories:
      categoriesResult.status === 'fulfilled' ? categoriesResult.value : [],
    authors: authorsResult.status === 'fulfilled' ? authorsResult.value : [],
    publishers:
      publishersResult.status === 'fulfilled' ? publishersResult.value : [],
  }
}

export function mapBookResponseToBook(
  bookResponse: BookResponse,
  referenceMaps: BookReferenceMaps,
): Book {
  const images = mapBookImageResponses(bookResponse.images ?? [])
  const primaryImage =
    images.find((image) => image.primaryImage)?.imageUrl ??
    images[0]?.imageUrl ??
    resolveBookImageUrl(bookResponse.imageUrl)

  return {
    id: bookResponse.id,
    title: bookResponse.title,
    isbn: bookResponse.isbn,
    author: referenceMaps.authorMap.get(bookResponse.authorId) ?? '',
    category: referenceMaps.categoryMap.get(bookResponse.categoryId)?.name ?? '',
    categoryInfo: referenceMaps.categoryMap.get(bookResponse.categoryId) ?? null,
    price: bookResponse.price,
    oldPrice: undefined,
    rating: normalizeRatingValue(bookResponse.averageRating) ?? undefined,
    reviews: bookResponse.reviewCount,
    soldCount: bookResponse.soldCount,
    starBreakdown: normalizeStarBreakdown(bookResponse.starBreakdown),
    cover: primaryImage,
    images,
    detail: mapBookDetailResponseToBookDetail(bookResponse.detail),
    description: bookResponse.description,
    stockQuantity: bookResponse.stockQuantity,
    publisher: referenceMaps.publisherMap.get(bookResponse.publisherId) ?? '',
    categoryId: bookResponse.categoryId,
    authorId: bookResponse.authorId,
    publisherId: bookResponse.publisherId,
    createdAt: bookResponse.createdAt,
    updatedAt: bookResponse.updatedAt,
  }
}

function mapBookPageDetailResponseToBookPageDetail(
  pageDetailResponse: BookPageDetailResponse,
  referenceMaps: BookReferenceMaps,
): BookPageDetail {
  const images = mapBookImageResponses(pageDetailResponse.book.images ?? [])
  const primaryImage =
    images.find((image) => image.primaryImage)?.imageUrl ??
    images[0]?.imageUrl ??
    getBookCoverUrl()
  const categoryTrail = pageDetailResponse.categoryTrail ?? []
  const leafCategory = categoryTrail[categoryTrail.length - 1]
  const ratingSummary = mapBookRatingSummaryResponseToBookRatingSummary(
    pageDetailResponse.ratingSummary,
  )

  return {
    book: {
      id: pageDetailResponse.book.id,
      title: pageDetailResponse.book.title,
      isbn: pageDetailResponse.book.isbn,
      author: pageDetailResponse.author.name,
      category: leafCategory?.name ?? '',
      categoryInfo: leafCategory ?? null,
      price: pageDetailResponse.book.price,
      oldPrice: pageDetailResponse.book.originalPrice ?? undefined,
      rating:
        normalizeRatingValue(pageDetailResponse.book.averageRating) ??
        ratingSummary.averageRating,
      reviews: pageDetailResponse.book.reviewCount ?? ratingSummary.reviewCount,
      soldCount: pageDetailResponse.book.soldCount,
      starBreakdown: ratingSummary.starBreakdown,
      cover: primaryImage,
      images,
      detail: mapBookDetailResponseToBookDetail(pageDetailResponse.book.detail),
      description: pageDetailResponse.book.description,
      stockQuantity: pageDetailResponse.book.stockQuantity,
      publisher: pageDetailResponse.publisher.name,
      categoryId: leafCategory?.id ?? '',
      authorId: pageDetailResponse.author.id,
      publisherId: pageDetailResponse.publisher.id,
      createdAt: '',
      updatedAt: '',
    },
    author: {
      ...pageDetailResponse.author,
      avatarUrl: pageDetailResponse.author.avatarUrl
        ? resolveBookImageUrl(pageDetailResponse.author.avatarUrl)
        : null,
    },
    publisher: {
      id: pageDetailResponse.publisher.id,
      name: pageDetailResponse.publisher.name,
    },
    categoryTrail: categoryTrail.map((category) => ({
      id: category.id,
      code: category.code,
      name: category.name,
      translations: category.translations,
    })),
    ratingSummary,
    promotions: pageDetailResponse.promotions.map(
      mapBookPromotionResponseToBookPromotion,
    ),
    relatedBooks: pageDetailResponse.relatedBooks.map((relatedBook) =>
      mapBookResponseToBook(relatedBook, referenceMaps),
    ),
  }
}

function mapBookImageResponses(imageResponses: BookImageResponse[]): BookImage[] {
  return imageResponses.map((imageResponse) => ({
    id: imageResponse.id,
    bookId: imageResponse.bookId,
    fileAssetId: imageResponse.fileAssetId,
    imageUrl: resolveBookImageUrl(imageResponse.imageUrl),
    primaryImage: imageResponse.primaryImage,
    sortOrder: imageResponse.sortOrder,
    altText: imageResponse.altText,
    createdAt: imageResponse.createdAt,
  }))
}

function mapBookDetailResponseToBookDetail(
  detailResponse: BookDetailResponse | null,
): BookDetail | null {
  if (!detailResponse) {
    return null
  }

  return {
    id: detailResponse.id,
    bookId: detailResponse.bookId,
    pageCount: detailResponse.pageCount,
    publicationYear: detailResponse.publicationYear,
    language: detailResponse.language,
    coverType: detailResponse.coverType,
    dimensions: detailResponse.dimensions,
    weight: detailResponse.weight,
    translator: detailResponse.translator,
    edition: detailResponse.edition,
  }
}

function mapBookRatingSummaryResponseToBookRatingSummary(
  ratingSummaryResponse: BookRatingSummaryResponse,
): BookRatingSummary {
  return {
    averageRating: normalizeRatingValue(ratingSummaryResponse.averageRating) ?? 0,
    reviewCount: ratingSummaryResponse.reviewCount ?? 0,
    starBreakdown: normalizeStarBreakdown(ratingSummaryResponse.starBreakdown),
  }
}

function mapBookPromotionResponseToBookPromotion(
  promotionResponse: BookPromotionResponse,
): BookPromotion {
  return {
    id: promotionResponse.id,
    code: promotionResponse.code,
    description: promotionResponse.description,
    discountType: promotionResponse.discountType,
    discountValue: promotionResponse.discountValue,
    minOrderAmount: promotionResponse.minOrderAmount,
    maxDiscountAmount: promotionResponse.maxDiscountAmount,
    maxUsageCount: promotionResponse.maxUsageCount,
    usedCount: promotionResponse.usedCount,
    startsAt: promotionResponse.startsAt,
    expiresAt: promotionResponse.expiresAt,
    active: promotionResponse.active,
    createdAt: promotionResponse.createdAt,
    updatedAt: promotionResponse.updatedAt,
  }
}

function mapBookReviewResponseToBookReview(
  reviewResponse: BookReviewResponse,
): BookReview {
  return {
    reviewId: reviewResponse.reviewId,
    userId: reviewResponse.userId,
    bookId: reviewResponse.bookId,
    orderItemId: reviewResponse.orderItemId,
    reviewerName: reviewResponse.reviewerName,
    reviewerAvatarUrl: reviewResponse.reviewerAvatarUrl
      ? resolveBookImageUrl(reviewResponse.reviewerAvatarUrl)
      : null,
    verifiedPurchase: reviewResponse.verifiedPurchase,
    reviewImages: (reviewResponse.reviewImages ?? []).map(resolveBookImageUrl),
    helpfulCount: reviewResponse.helpfulCount,
    rating: reviewResponse.rating,
    comment: reviewResponse.comment,
    createdAt: reviewResponse.createdAt,
    updatedAt: reviewResponse.updatedAt,
  }
}

export type BookReferenceMaps = {
  authorMap: Map<string, string>
  categoryMap: Map<string, CategoryResponse>
  publisherMap: Map<string, string>
}

function buildBookReferenceMaps(
  referenceData: BookReferenceData,
): BookReferenceMaps {
  return {
    authorMap: new Map(
      referenceData.authors.map((author) => [author.id, author.name]),
    ),
    categoryMap: new Map(
      referenceData.categories.map((category) => [category.id, category]),
    ),
    publisherMap: new Map(
      referenceData.publishers.map((publisher) => [publisher.id, publisher.name]),
    ),
  }
}

function sortCategories(categories: CategoryResponse[]) {
  return [...categories].sort((firstCategory, secondCategory) =>
    firstCategory.name.localeCompare(secondCategory.name, 'vi'),
  )
}

function getCategoryIds(categories: CategoryResponse[]) {
  return Object.fromEntries(
    categories.map((category) => [category.code, category.id]),
  )
}

function normalizeRatingValue(rating: number | null | undefined) {
  return typeof rating === 'number' ? rating : null
}

function normalizeStarBreakdown(
  starBreakdown: Record<number, number> | null | undefined,
) {
  const normalizedStarBreakdown: Record<number, number> = {}

  for (const [rating, count] of Object.entries(starBreakdown ?? {})) {
    normalizedStarBreakdown[Number(rating)] = count
  }

  return normalizedStarBreakdown
}

function resolveBookImageUrl(imageUrl?: string | null) {
  const normalizedImageUrl = imageUrl?.trim() ?? ''

  if (
    normalizedImageUrl.startsWith('http://') ||
    normalizedImageUrl.startsWith('https://') ||
    normalizedImageUrl.startsWith('/')
  ) {
    return normalizedImageUrl
  }

  return getBookCoverUrl(normalizedImageUrl)
}
