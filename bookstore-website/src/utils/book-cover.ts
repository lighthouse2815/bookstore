import bookDefaultCover from '@/assets/book-img/book-default.png'

export const BOOK_DEFAULT_COVER = bookDefaultCover

export function getBookCoverUrl(cover?: string | null) {
  const normalizedCover = cover?.trim()

  if (!normalizedCover) {
    return BOOK_DEFAULT_COVER
  }

  return normalizedCover
}
