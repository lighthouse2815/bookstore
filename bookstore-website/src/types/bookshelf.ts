import type { BookResponse } from './book'

export type BookshelfSummaryResponse = {
  id: string
  name: string
  bookCount: number
  createdAt: string
  updatedAt: string
}

export type BookshelfItemResponse = {
  id: string
  sortOrder: number
  createdAt: string
  updatedAt: string
  book: BookResponse
}

export type BookshelfResponse = {
  id: string
  name: string
  bookCount: number
  items: BookshelfItemResponse[]
  createdAt: string
  updatedAt: string
}

export type BookshelfBook = {
  id: string
  title: string
  author: string
  category: string
  categoryInfo?: import('@/types/book').LocalizedCategory | null
  price: number
  cover: string | null
  rating?: number
  reviews?: number
  stockQuantity: number
}

export type BookshelfSummary = {
  id: string
  name: string
  bookCount: number
  createdAt: string
  updatedAt: string
}

export type BookshelfItem = {
  id: string
  sortOrder: number
  createdAt: string
  updatedAt: string
  book: BookshelfBook
}

export type Bookshelf = {
  id: string
  name: string
  bookCount: number
  items: BookshelfItem[]
  createdAt: string
  updatedAt: string
}

export type UpsertBookshelfRequest = {
  name: string
}

export type ReorderBookshelfItemsRequest = {
  itemIds: string[]
}
