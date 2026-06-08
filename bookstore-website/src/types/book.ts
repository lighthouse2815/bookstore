// Request types
export type SearchBooksRequest = {
  keyword?: string
}

// Response types
export type BookResponse = {
  id: string
  title: string
  description: string | null
  price: number
  stockQuantity: number
  imageUrl: string | null
  categoryId: string
  authorId: string
  publisherId: string
  createdAt: string
  updatedAt: string
}

export type CategoryResponse = {
  id: string
  name: string
  description: string | null
  createdAt: string
  updatedAt: string
}

export type AuthorResponse = {
  id: string
  name: string
  biography: string | null
  createdAt: string
  updatedAt: string
}

export type PublisherResponse = {
  id: string
  name: string
  description: string | null
  createdAt: string
  updatedAt: string
}

// Model types
export type Book = {
  id: string
  title: string
  author: string
  category: string
  price: number
  oldPrice?: number
  rating?: number
  reviews?: number
  bestseller?: boolean
  cover: string | null
  description: string | null
  stockQuantity: number
  publisher: string
  categoryId: string
  authorId: string
  publisherId: string
  createdAt: string
  updatedAt: string
}

export type BookCatalog = {
  books: Book[]
  categories: string[]
}
