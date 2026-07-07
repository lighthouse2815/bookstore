export type FileProvider = 'R2' | 'S3'

export type FilePurpose =
  | 'BOOK_IMAGE'
  | 'USER_AVATAR'
  | 'AUTHOR_AVATAR'
  | 'REVIEW_IMAGE'
  | 'EBOOK_FILE'
  | 'SAMPLE_FILE'
  | 'INVOICE'

export type FileVisibility = 'PUBLIC' | 'PRIVATE'

export type FileStatus = 'PENDING' | 'ACTIVE' | 'DELETED'

export type PresignUploadRequest = {
  purpose: FilePurpose
  visibility: FileVisibility
  fileName: string
  contentType: string
  sizeBytes: number
  bookId?: string
  authorId?: string
  digitalAssetId?: string
  reviewId?: string
  orderId?: string
}

export type PresignedUploadResponse = {
  fileAssetId: string
  uploadUrl: string
  method: string
  headers: Record<string, string>
  expiresAt: string
  storageKey: string
}

export type FileAssetResponse = {
  id: string
  provider: FileProvider
  purpose: FilePurpose
  bucket: string | null
  storageKey: string
  publicUrl: string | null
  originalName: string | null
  contentType: string | null
  sizeBytes: number | null
  checksumSha256: string | null
  visibility: FileVisibility
  status: FileStatus
  createdBy: string
  createdAt: string
  updatedAt: string
  deletedAt: string | null
}

export type CompleteFileUploadRequest = {
  fileAssetId: string
  checksumSha256?: string | null
}

export type SignedUrlResponse = {
  url: string
  expiresAt: string
}
