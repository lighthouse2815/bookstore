export const BOOK_DEFAULT_COVER = '/placeholder.jpg'

export function getBookCoverUrl(cover?: string | null) {
  const normalizedCover = cover?.trim()

  if (!normalizedCover) {
    return BOOK_DEFAULT_COVER
  }

  return normalizedCover
}
