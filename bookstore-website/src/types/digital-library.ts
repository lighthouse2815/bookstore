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
  sampleStorageKey: string | null
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
  storageKey: string
  sampleStorageKey: string | null
  mimeType: string
  fileSize: number
  checksum: string | null
  price: number
  downloadAllowed: boolean
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
  sampleStorageKey: string | null
  price: number
  downloadAllowed: boolean
}

export type DigitalAssetResponse = {
  id: string
  bookId: string
  format: DigitalAssetFormat
  title: string
  fileName: string
  storageKey: string
  mimeType: string
  fileSize: number
  checksum: string | null
  sampleStorageKey: string | null
  price: number
  downloadAllowed: boolean
  published: boolean
  createdAt: string
  updatedAt: string
  deletedAt: string | null
}

export type UpsertDigitalAssetRequest = {
  format: DigitalAssetFormat
  title: string
  fileName: string
  storageKey: string
  mimeType: string
  fileSize: number
  checksum?: string | null
  sampleStorageKey?: string | null
  price: number
  downloadAllowed: boolean
  published: boolean
}

export type UpdateReadingProgressRequest = {
  currentPage?: number | null
  progressPercent: number
  positionData?: string | null
}
