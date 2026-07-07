// Request types
export type SearchBooksRequest = {
  keyword?: string
  categoryId?: string
}

export type UpsertBookRequest = {
  title: string
  description?: string | null
  price: number
  stockQuantity: number
  images: UpsertBookImageRequest[]
  categoryId: string
  authorId: string
  publisherId: string
}

export type UpsertBookImageRequest = {
  id?: string
  fileAssetId: string
  primaryImage: boolean
  sortOrder: number
  altText?: string | null
}

export type UpsertCategoryRequest = {
  name: string
  description?: string | null
}

export type UpsertAuthorRequest = {
  name: string
  biography?: string | null
  avatarFileAssetId?: string | null
  birthYear?: number | null
  deathYear?: number | null
}

export type UpsertPublisherRequest = {
  name: string
  description?: string | null
}

// Response types
export type BookResponse = {
  id: string
  title: string
  isbn: string | null
  description: string | null
  price: number
  stockQuantity: number
  soldCount: number
  averageRating: number | null
  reviewCount: number
  starBreakdown: Record<number, number>
  imageUrl: string | null
  images: BookImageResponse[]
  detail: BookDetailResponse | null
  categoryId: string
  authorId: string
  publisherId: string
  createdAt: string
  updatedAt: string
}

export type BookImageResponse = {
  id: string
  bookId: string
  fileAssetId: string
  imageUrl: string
  primaryImage: boolean
  sortOrder: number
  altText: string | null
  createdAt: string
}

export type BookDetailResponse = {
  id: string
  bookId: string
  pageCount: number | null
  publicationYear: number | null
  language: string | null
  coverType: string | null
  dimensions: string | null
  weight: number | null
  translator: string | null
  edition: string | null
}

export type CategoryResponse = {
  id: string
  name: string
  description: string | null
  parentId: string | null
  createdAt: string
  updatedAt: string
}

export type AuthorResponse = {
  id: string
  name: string
  biography: string | null
  avatarFileAssetId: string | null
  avatarUrl: string | null
  birthYear: number | null
  deathYear: number | null
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
  isbn: string | null
  author: string
  category: string
  price: number
  oldPrice?: number
  rating?: number
  reviews?: number
  soldCount: number
  starBreakdown: Record<number, number>
  bestseller?: boolean
  cover: string | null
  images: BookImage[]
  detail: BookDetail | null
  description: string | null
  stockQuantity: number
  publisher: string
  categoryId: string
  authorId: string
  publisherId: string
  createdAt: string
  updatedAt: string
}

export type BookImage = {
  id: string
  bookId: string
  fileAssetId: string
  imageUrl: string
  primaryImage: boolean
  sortOrder: number
  altText: string | null
  createdAt: string
}

export type BookDetail = {
  id: string
  bookId: string
  pageCount: number | null
  publicationYear: number | null
  language: string | null
  coverType: string | null
  dimensions: string | null
  weight: number | null
  translator: string | null
  edition: string | null
}

export type BookCatalog = {
  books: Book[]
  categories: string[]
  categoryIds: Record<string, string>
}

export type BookCatalogPage = BookCatalog & {
  totalCount: number
  page: number
  size: number
  hasNext: boolean
  totalPages: number
}

export type BookReferenceData = {
  categories: CategoryResponse[]
  authors: AuthorResponse[]
  publishers: PublisherResponse[]
}

export type BookReviewResponse = {
  reviewId: string
  userId: string
  bookId: string
  orderItemId: string
  reviewerName: string
  reviewerAvatarUrl: string | null
  verifiedPurchase: boolean
  reviewImages: string[]
  helpfulCount: number
  rating: number
  comment: string | null
  createdAt: string
  updatedAt: string
}

export type BookReview = {
  reviewId: string
  userId: string
  bookId: string
  orderItemId: string
  reviewerName: string
  reviewerAvatarUrl: string | null
  verifiedPurchase: boolean
  reviewImages: string[]
  helpfulCount: number
  rating: number
  comment: string | null
  createdAt: string
  updatedAt: string
}

export type BookPromotionResponse = {
  id: string
  code: string
  description: string | null
  discountType: string
  discountValue: number
  minOrderAmount: number | null
  maxDiscountAmount: number | null
  maxUsageCount: number | null
  usedCount: number
  startsAt: string
  expiresAt: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export type BookPromotion = {
  id: string
  code: string
  description: string | null
  discountType: string
  discountValue: number
  minOrderAmount: number | null
  maxDiscountAmount: number | null
  maxUsageCount: number | null
  usedCount: number
  startsAt: string
  expiresAt: string
  active: boolean
  createdAt: string
  updatedAt: string
}

export type BookCategoryTrailItemResponse = {
  id: string
  name: string
}

export type BookCategoryTrailItem = {
  id: string
  name: string
}

export type BookRatingSummaryResponse = {
  averageRating: number | null
  reviewCount: number
  starBreakdown: Record<number, number>
}

export type BookRatingSummary = {
  averageRating: number
  reviewCount: number
  starBreakdown: Record<number, number>
}

export type BookPublisherSummaryResponse = {
  id: string
  name: string
}

export type BookPublisherSummary = {
  id: string
  name: string
}

export type BookPageDetailBookResponse = {
  id: string
  title: string
  isbn: string | null
  price: number
  originalPrice: number | null
  discountPercent: number | null
  stockQuantity: number
  soldCount: number
  description: string | null
  images: BookImageResponse[]
  detail: BookDetailResponse | null
  averageRating: number | null
  reviewCount: number
}

export type BookPageDetailResponse = {
  book: BookPageDetailBookResponse
  author: AuthorResponse
  publisher: BookPublisherSummaryResponse
  categoryTrail: BookCategoryTrailItemResponse[]
  ratingSummary: BookRatingSummaryResponse
  promotions: BookPromotionResponse[]
  relatedBooks: BookResponse[]
}

export type BookPageDetail = {
  book: Book
  author: AuthorResponse
  publisher: BookPublisherSummary
  categoryTrail: BookCategoryTrailItem[]
  ratingSummary: BookRatingSummary
  promotions: BookPromotion[]
  relatedBooks: Book[]
}
