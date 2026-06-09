import api from './api'
import type { ApiResponse } from '@/types/api'
import type {
  AuthorResponse,
  CategoryResponse,
  PublisherResponse,
  UpsertAuthorRequest,
  UpsertCategoryRequest,
  UpsertPublisherRequest,
} from '@/types/book'
import { unwrapResponse } from '@/utils'

export async function createCategory(
  data: UpsertCategoryRequest,
): Promise<CategoryResponse> {
  const response = await api.post<ApiResponse<CategoryResponse>>(
    '/admin/categories',
    data,
  )
  return unwrapResponse(response)
}

export async function updateCategory(
  id: string,
  data: UpsertCategoryRequest,
): Promise<CategoryResponse> {
  const response = await api.put<ApiResponse<CategoryResponse>>(
    `/admin/categories/${id}`,
    data,
  )
  return unwrapResponse(response)
}

export async function deleteCategory(id: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/admin/categories/${id}`)
}

export async function createAuthor(
  data: UpsertAuthorRequest,
): Promise<AuthorResponse> {
  const response = await api.post<ApiResponse<AuthorResponse>>(
    '/admin/authors',
    data,
  )
  return unwrapResponse(response)
}

export async function updateAuthor(
  id: string,
  data: UpsertAuthorRequest,
): Promise<AuthorResponse> {
  const response = await api.put<ApiResponse<AuthorResponse>>(
    `/admin/authors/${id}`,
    data,
  )
  return unwrapResponse(response)
}

export async function deleteAuthor(id: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/admin/authors/${id}`)
}

export async function createPublisher(
  data: UpsertPublisherRequest,
): Promise<PublisherResponse> {
  const response = await api.post<ApiResponse<PublisherResponse>>(
    '/admin/publishers',
    data,
  )
  return unwrapResponse(response)
}

export async function updatePublisher(
  id: string,
  data: UpsertPublisherRequest,
): Promise<PublisherResponse> {
  const response = await api.put<ApiResponse<PublisherResponse>>(
    `/admin/publishers/${id}`,
    data,
  )
  return unwrapResponse(response)
}

export async function deletePublisher(id: string): Promise<void> {
  await api.delete<ApiResponse<null>>(`/admin/publishers/${id}`)
}
