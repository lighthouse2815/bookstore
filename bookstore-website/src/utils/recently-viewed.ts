import type { Book, BookCardData } from '@/types/book'

const RECENTLY_VIEWED_STORAGE_KEY = 'bookstore-recently-viewed'
const DEFAULT_RECENTLY_VIEWED_LIMIT = 12

export function getRecentlyViewedBooks(): BookCardData[] {
  if (typeof window === 'undefined') {
    return []
  }

  try {
    const rawValue = window.localStorage.getItem(RECENTLY_VIEWED_STORAGE_KEY)
    if (!rawValue) {
      return []
    }

    const parsedValue: unknown = JSON.parse(rawValue)
    if (!Array.isArray(parsedValue)) {
      return []
    }

    return parsedValue.flatMap((entry) => normalizeBookCardData(entry))
  } catch {
    return []
  }
}

export function pushRecentlyViewedBook(
  book: Book | BookCardData,
  limit = DEFAULT_RECENTLY_VIEWED_LIMIT,
) {
  if (typeof window === 'undefined') {
    return []
  }

  const nextBook = toBookCardData(book)
  const nextItems = [
    nextBook,
    ...getRecentlyViewedBooks().filter((item) => item.id !== nextBook.id),
  ].slice(0, limit)

  window.localStorage.setItem(
    RECENTLY_VIEWED_STORAGE_KEY,
    JSON.stringify(nextItems),
  )

  return nextItems
}

function toBookCardData(book: Book | BookCardData): BookCardData {
  return {
    id: book.id,
    title: book.title,
    author: book.author,
    category: book.category,
    price: book.price,
    cover: book.cover,
    oldPrice: book.oldPrice,
    rating: book.rating,
    reviews: book.reviews,
    bestseller: book.bestseller,
  }
}

function normalizeBookCardData(value: unknown): BookCardData[] {
  if (!value || typeof value !== "object") {
    return []
  }

  const candidate = value as Partial<BookCardData>

  if (
    typeof candidate.id !== 'string' ||
    typeof candidate.title !== 'string' ||
    typeof candidate.author !== 'string' ||
    typeof candidate.category !== 'string' ||
    typeof candidate.price !== 'number'
  ) {
    return []
  }

  return [
    {
      id: candidate.id,
      title: candidate.title,
      author: candidate.author,
      category: candidate.category,
      price: candidate.price,
      cover: typeof candidate.cover === 'string' ? candidate.cover : null,
      oldPrice:
        typeof candidate.oldPrice === 'number' ? candidate.oldPrice : undefined,
      rating: typeof candidate.rating === 'number' ? candidate.rating : undefined,
      reviews:
        typeof candidate.reviews === 'number' ? candidate.reviews : undefined,
      bestseller:
        typeof candidate.bestseller === 'boolean'
          ? candidate.bestseller
          : undefined,
    },
  ]
}
