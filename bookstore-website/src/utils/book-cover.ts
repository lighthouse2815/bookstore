export const BOOK_DEFAULT_COVER = '/book-placeholder.svg'

const DIRECT_RESOURCE_PREFIXES = ['http://', 'https://', 'blob:', 'data:'] as const

function getBackendOrigin() {
  const apiBaseUrl =
    import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api'

  try {
    return new URL(apiBaseUrl).origin
  } catch {
    return null
  }
}

export function getBookCoverUrl(cover?: string | null) {
  const normalizedCover = cover?.trim()

  if (!normalizedCover) {
    return BOOK_DEFAULT_COVER
  }

  if (normalizedCover === BOOK_DEFAULT_COVER) {
    return normalizedCover
  }

  if (
    DIRECT_RESOURCE_PREFIXES.some((prefix) => normalizedCover.startsWith(prefix))
  ) {
    return normalizedCover
  }

  const backendOrigin = getBackendOrigin()

  if (!backendOrigin) {
    return normalizedCover.startsWith('/') ? normalizedCover : `/${normalizedCover}`
  }

  const normalizedPath = normalizedCover.startsWith('/')
    ? normalizedCover
    : `/${normalizedCover}`

  return new URL(normalizedPath, backendOrigin).toString()
}

export function setBookCoverFallback(image: HTMLImageElement) {
  if (image.dataset.coverFallback === 'true') {
    return
  }

  image.dataset.coverFallback = 'true'
  image.src = BOOK_DEFAULT_COVER
}
