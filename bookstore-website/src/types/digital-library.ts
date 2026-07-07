import type { PageResult } from '@/types/pagination'

export type DigitalAssetFormat = 'PDF' | 'EPUB' | 'AUDIO'

export type DigitalAccessType = 'PURCHASED' | 'BORROWED' | 'SUBSCRIPTION'

export type DigitalAccessStatus = 'ACTIVE' | 'EXPIRED' | 'REVOKED'

export type ReadingProgressResponse = {
  id: string
  userId: string
  digitalAssetId: string
  currentPage: number | null
  progressPercent: number
  positionData: string | null
  lastReadAt: string
  createdAt: string
  updatedAt: string
}

export type DigitalLibraryItemResponse = {
  digitalAssetId: string
  bookId: string
  bookTitle: string
  bookImageUrl: string | null
  assetTitle: string
  format: DigitalAssetFormat
  price: number
  downloadAllowed: boolean
  sampleAvailable: boolean
  accessType: DigitalAccessType
  accessStatus: DigitalAccessStatus
  sourceOrderId: string | null
  expiresAt: string | null
  acquiredAt: string
  progress: ReadingProgressResponse | null
}

export type DigitalLibraryAssetResponse = {
  digitalAssetId: string
  bookId: string
  bookTitle: string
  bookDescription: string | null
  bookImageUrl: string | null
  assetTitle: string
  format: DigitalAssetFormat
  fileName: string
  mimeType: string
  fileSize: number
  price: number
  downloadAllowed: boolean
  sampleAvailable: boolean
  accessType: DigitalAccessType
  accessStatus: DigitalAccessStatus
  sourceOrderId: string | null
  expiresAt: string | null
  acquiredAt: string
  assetUpdatedAt: string
  progress: ReadingProgressResponse | null
}

export type PublishedDigitalAssetResponse = {
  id: string
  bookId: string
  format: DigitalAssetFormat
  title: string
  fileName: string
  price: number
  downloadAllowed: boolean
  purchaseAllowed: boolean
  sampleAvailable: boolean
}

export type PublishedDigitalAssetCatalogItemResponse = {
  id: string
  bookId: string
  categoryId: string
  authorId: string
  publisherId: string
  format: DigitalAssetFormat
  title: string
  fileName: string
  price: number
  downloadAllowed: boolean
  purchaseAllowed: boolean
  sampleAvailable: boolean
  bookTitle: string
  bookDescription: string | null
  bookImageUrl: string | null
}

export type PublishedDigitalAssetCatalogItem = PublishedDigitalAssetCatalogItemResponse & {
  categoryName: string
  authorName: string
  publisherName: string
}

export type PublishedDigitalAssetCatalogPageResult =
  PageResult<PublishedDigitalAssetCatalogItemResponse>

export type DigitalAssetResponse = {
  id: string
  bookId: string
  format: DigitalAssetFormat
  title: string
  fileAssetId: string
  sampleFileAssetId: string | null
  fileName: string
  mimeType: string
  fileSize: number
  checksum: string | null
  price: number
  downloadAllowed: boolean
  purchaseAllowed: boolean
  published: boolean
  createdAt: string
  updatedAt: string
  deletedAt: string | null
}

export type UpsertDigitalAssetRequest = {
  format: DigitalAssetFormat
  title: string
  fileAssetId: string
  sampleFileAssetId?: string | null
  price: number
  downloadAllowed: boolean
  purchaseAllowed: boolean
  published: boolean
}

export type UpdateReadingProgressRequest = {
  currentPage?: number | null
  progressPercent: number
  positionData?: string | null
}

export type DigitalLibraryPageResult = {
  items: DigitalLibraryItemResponse[]
  page: number
  size: number
  totalCount: number
  hasNext: boolean
}
