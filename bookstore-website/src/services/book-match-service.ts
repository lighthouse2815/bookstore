import type { Book } from '@/types/book'
import type {
  BookMatchAnswers,
  BookMatchBudget,
  BookMatchDraftAnswers,
  BookMatchMood,
  BookMatchReadingTime,
  BookMatchRecommendation,
  BookMatchReason,
} from '@/types/book-match'

export const BOOK_MATCH_RESULT_LIMIT = 8
export const BOOK_MATCH_WEAK_PAGE_COUNT_HINT_RATIO = 0.4

type MoodProfile = {
  categories: string[]
  keywords: string[]
}

type ScoredRecommendation = BookMatchRecommendation & {
  budgetMatched: boolean
  moodMatched: boolean
  readingTimeMatched: boolean
}

const MAX_RECOMMENDATIONS = 12
const MIN_RECOMMENDATIONS = 6
const RECENT_BOOK_WINDOW_DAYS = 180
const BOOK_MATCH_WEAK_PAGE_COUNT_MIN_COUNT = 2

const MOOD_PROFILES: Record<BookMatchMood, MoodProfile> = {
  RELAX: {
    categories: [
      'van hoc',
      'truyen ngan',
      'doi song',
      'tap van',
      'tieu thuyet',
      'lang man',
      'literature',
      'novel',
    ],
    keywords: [
      'thu gian',
      'binh yen',
      'doi song',
      'cam hung',
      'lang man',
      'feel good',
      'cozy',
      'gentle',
    ],
  },
  STUDY: {
    categories: [
      'ky nang',
      'ngoai ngu',
      'hoc tap',
      'kinh doanh',
      'giao duc',
      'self help',
      'business',
      'education',
    ],
    keywords: [
      'study',
      'learning',
      'mindset',
      'ky nang',
      'ngoai ngu',
      'business',
      'productivity',
      'marketing',
      'leadership',
    ],
  },
  ADVENTURE: {
    categories: [
      'phieu luu',
      'fantasy',
      'lich su',
      'vien tuong',
      'science fiction',
      'historical',
    ],
    keywords: [
      'hanh trinh',
      'vuong quoc',
      'chien binh',
      'expedition',
      'dragon',
      'kingdom',
      'adventure',
      'odyssey',
    ],
  },
  MYSTERY: {
    categories: [
      'trinh tham',
      'kinh di',
      'bi an',
      'hinh su',
      'mystery',
      'thriller',
      'horror',
    ],
    keywords: [
      'dieu tra',
      'an mang',
      'bi mat',
      'ma',
      'detective',
      'murder',
      'secret',
      'haunted',
    ],
  },
  HEALING: {
    categories: [
      'chua lanh',
      'tam ly',
      'song dep',
      'self help',
      'cam xuc',
      'inspiration',
      'wellness',
    ],
    keywords: [
      'healing',
      'mindfulness',
      'an yen',
      'yeu ban than',
      'gratitude',
      'gentle',
      'calm',
      'resilience',
    ],
  },
}

export function isBookMatchReady(
  answers: BookMatchDraftAnswers,
): answers is BookMatchAnswers {
  return Boolean(answers.mood && answers.budget && answers.readingTime)
}

export function getBookMatchRecommendations(
  books: Book[],
  answers: BookMatchAnswers,
  limit = BOOK_MATCH_RESULT_LIMIT,
): BookMatchRecommendation[] {
  if (books.length === 0) {
    return []
  }

  const candidateBooks = getCandidateBooks(books)
  const scoredRecommendations = candidateBooks.map((book) =>
    scoreBook(book, answers),
  )

  const mergedRecommendations = dedupeRecommendations([
    ...sortRecommendations(
      scoredRecommendations.filter(
        (item) =>
          item.budgetMatched && item.moodMatched && item.readingTimeMatched,
      ),
    ),
    ...sortRecommendations(
      scoredRecommendations.filter(
        (item) => item.budgetMatched && item.moodMatched,
      ),
    ),
    ...sortRecommendations(
      scoredRecommendations.filter(
        (item) => item.budgetMatched && item.readingTimeMatched,
      ),
    ),
    ...sortRecommendations(
      scoredRecommendations.filter((item) => item.budgetMatched),
    ),
    ...sortRecommendations(
      scoredRecommendations.filter((item) => item.moodMatched),
    ),
    ...sortRecommendations(scoredRecommendations),
  ])

  return mergedRecommendations
    .slice(0, normalizeLimit(limit))
    .map(({ book, reasons, score }) => ({
      book,
      reasons,
      score,
    }))
}

function scoreBook(book: Book, answers: BookMatchAnswers): ScoredRecommendation {
  const moodProfile = MOOD_PROFILES[answers.mood]
  const normalizedCategory = normalizeText(book.category)
  const searchableText = normalizeText(
    [
      book.title,
      book.category,
      book.author,
      book.publisher,
      book.description ?? '',
    ].join(' '),
  )

  const categoryMatchCount = moodProfile.categories.filter((category) =>
    normalizedCategory.includes(category),
  ).length
  const keywordMatchCount = moodProfile.keywords.filter((keyword) =>
    searchableText.includes(keyword),
  ).length

  const moodMatched = categoryMatchCount > 0 || keywordMatchCount > 0
  const budgetMatched = matchesBudget(book.price, answers.budget)
  const readingTimeMatched = matchesReadingTime(
    book.detail?.pageCount ?? null,
    answers.readingTime,
  )

  const reasons: BookMatchReason[] = []
  let score = 0

  if (moodMatched) {
    score += categoryMatchCount * 6 + keywordMatchCount * 2.5
    reasons.push('MOOD')
  }

  if (budgetMatched) {
    score += 5
    reasons.push('BUDGET')
  }

  if (readingTimeMatched) {
    score += 4
    reasons.push('READING_TIME')
  }

  const ratingValue = book.rating ?? 0
  const reviewCount = book.reviews ?? 0
  if (ratingValue > 0) {
    score += ratingValue * 0.9 + Math.min(reviewCount / 30, 1.5)
    if (ratingValue >= 4) {
      reasons.push('HIGH_RATING')
    }
  }

  if (book.soldCount > 0) {
    score += Math.min(Math.log10(book.soldCount + 1) * 2, 3)
    if (book.soldCount >= 10) {
      reasons.push('POPULAR_PICK')
    }
  }

  if (isRecentBook(book.createdAt)) {
    score += 0.75
    reasons.push('FRESH_PICK')
  }

  if (book.detail?.pageCount == null) {
    score += 0.25
  }

  return {
    book,
    reasons: dedupeReasons(reasons),
    score,
    budgetMatched,
    moodMatched,
    readingTimeMatched,
  }
}

function sortRecommendations(recommendations: ScoredRecommendation[]) {
  return [...recommendations].sort((first, second) => {
    if (second.score !== first.score) {
      return second.score - first.score
    }

    const secondRating = second.book.rating ?? 0
    const firstRating = first.book.rating ?? 0
    if (secondRating !== firstRating) {
      return secondRating - firstRating
    }

    if (second.book.soldCount !== first.book.soldCount) {
      return second.book.soldCount - first.book.soldCount
    }

    return second.book.createdAt.localeCompare(first.book.createdAt)
  })
}

function dedupeRecommendations(recommendations: ScoredRecommendation[]) {
  const recommendationsByBookId = new Map<string, ScoredRecommendation>()

  for (const recommendation of recommendations) {
    if (!recommendationsByBookId.has(recommendation.book.id)) {
      recommendationsByBookId.set(recommendation.book.id, recommendation)
    }
  }

  return [...recommendationsByBookId.values()]
}

function dedupeReasons(reasons: BookMatchReason[]) {
  return [...new Set(reasons)].slice(0, 3)
}

function normalizeLimit(limit: number) {
  return Math.min(Math.max(limit, MIN_RECOMMENDATIONS), MAX_RECOMMENDATIONS)
}

function getCandidateBooks(books: Book[]) {
  const inStockBooks = books.filter((book) => book.stockQuantity > 0)
  return inStockBooks.length > 0 ? inStockBooks : books
}

export function matchesBudget(price: number, budget: BookMatchBudget) {
  switch (budget) {
    case 'UNDER_100':
      return price < 100_000
    case 'FROM_100_TO_200':
      return price >= 100_000 && price <= 200_000
    case 'ABOVE_200':
      return price > 200_000
  }
}

export function matchesReadingTime(
  pageCount: number | null,
  readingTime: BookMatchReadingTime,
) {
  if (pageCount == null) {
    return false
  }

  switch (readingTime) {
    case 'SHORT':
      return pageCount <= 220
    case 'MEDIUM':
      return pageCount >= 221 && pageCount <= 380
    case 'LONG':
      return pageCount >= 381
  }
}

export function hasBookMatchWeakPageCountCoverage(
  books: Pick<Book, 'detail'>[],
) {
  if (books.length === 0) {
    return false
  }

  const missingPageCountBooks = books.filter(
    (book) => book.detail?.pageCount == null,
  ).length

  return (
    missingPageCountBooks >= BOOK_MATCH_WEAK_PAGE_COUNT_MIN_COUNT &&
    missingPageCountBooks / books.length >= BOOK_MATCH_WEAK_PAGE_COUNT_HINT_RATIO
  )
}

function isRecentBook(createdAt: string) {
  const createdTimestamp = Date.parse(createdAt)
  if (Number.isNaN(createdTimestamp)) {
    return false
  }

  const ageInMilliseconds = Date.now() - createdTimestamp
  const maxAgeInMilliseconds = RECENT_BOOK_WINDOW_DAYS * 24 * 60 * 60 * 1000
  return ageInMilliseconds <= maxAgeInMilliseconds
}

function normalizeText(value: string) {
  return value
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .replace(/\u0111/g, 'd')
    .replace(/\u0110/g, 'D')
    .toLowerCase()
    .trim()
}
