import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  AuthorResponse,
  Book,
  BookCatalog,
  BookReferenceData,
  BookResponse,
  CategoryResponse,
  PublisherResponse,
  SearchBooksRequest,
  UpsertBookRequest,
} from '@/types/book'
import { unwrapResponse } from '@/utils'

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
    categories: getCategoryNames(referenceData.categories),
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

async function getBookResponseById(id: string): Promise<BookResponse> {
  const response = await api.get<ApiResponse<BookResponse>>(`/books/${id}`)
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

async function getBookReferenceData(): Promise<BookReferenceData> {
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

function mapBookResponseToBook(
  bookResponse: BookResponse,
  referenceMaps: BookReferenceMaps,
): Book {
  return {
    id: bookResponse.id,
    title: bookResponse.title,
    author: referenceMaps.authorMap.get(bookResponse.authorId) ?? '',
    category: referenceMaps.categoryMap.get(bookResponse.categoryId) ?? '',
    price: bookResponse.price,
    cover: resolveBookImageUrl(bookResponse.imageUrl),
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

type BookReferenceMaps = {
  authorMap: Map<string, string>
  categoryMap: Map<string, string>
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
      referenceData.categories.map((category) => [category.id, category.name]),
    ),
    publisherMap: new Map(
      referenceData.publishers.map((publisher) => [publisher.id, publisher.name]),
    ),
  }
}

function getCategoryNames(categories: CategoryResponse[]) {
  return [...new Set(categories.map((category) => category.name).filter(Boolean))]
    .sort((firstCategory, secondCategory) =>
      firstCategory.localeCompare(secondCategory, 'vi'),
    )
}

function resolveBookImageUrl(imageUrl: string | null) {
  if (!imageUrl) {
    return null
  }

  if (
    imageUrl.startsWith('http://') ||
    imageUrl.startsWith('https://') ||
    imageUrl.startsWith('/')
  ) {
    return imageUrl
  }

  return imageUrl
}
