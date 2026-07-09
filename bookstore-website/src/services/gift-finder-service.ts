import type { Book } from '@/types/book'
import type {
  GiftFinderAnswers,
  GiftFinderBudget,
  GiftFinderDraftAnswers,
  GiftFinderOccasion,
  GiftFinderRecommendation,
  GiftFinderReason,
  GiftFinderRecipient,
  GiftFinderTone,
} from '@/types/gift-finder'

export const GIFT_FINDER_RESULT_LIMIT = 8

type SignalProfile = {
  categories: string[]
  keywords: string[]
}

type ScoredRecommendation = GiftFinderRecommendation & {
  budgetMatched: boolean
  recipientMatched: boolean
  occasionMatched: boolean
  toneMatched: boolean
}

const MAX_RECOMMENDATIONS = 8
const RECENT_BOOK_WINDOW_DAYS = 240
const DISPLAY_REASON_PRIORITY: GiftFinderReason[] = [
  'RECIPIENT',
  'OCCASION',
  'TONE',
  'BUDGET',
  'HIGH_RATING',
  'POPULAR_PICK',
  'GIFTABLE_PICK',
]

const RECIPIENT_PROFILES: Record<GiftFinderRecipient, SignalProfile> = {
  BEST_FRIEND: {
    categories: [
      'van hoc',
      'tieu thuyet',
      'truyen ngan',
      'doi song',
      'fantasy',
      'tam ly',
    ],
    keywords: [
      'feel good',
      'friend',
      'ban than',
      'cozy',
      'cam hung',
      'hanh trinh',
    ],
  },
  PARTNER: {
    categories: [
      'lang man',
      'van hoc',
      'tieu thuyet',
      'tap van',
      'tam ly',
      'song dep',
    ],
    keywords: [
      'lang man',
      'love',
      'yeu',
      'cam xuc',
      'gentle',
      'healing',
    ],
  },
  PARENT: {
    categories: [
      'doi song',
      'ky nang',
      'lich su',
      'tam ly',
      'suc khoe',
      'van hoc',
    ],
    keywords: [
      'gia dinh',
      'gia tri',
      'song dep',
      'gratitude',
      'hanh phuc',
      'healing',
    ],
  },
  COLLEAGUE: {
    categories: [
      'kinh doanh',
      'ky nang',
      'ngoai ngu',
      'giao duc',
      'self help',
      'business',
    ],
    keywords: [
      'productivity',
      'leadership',
      'mindset',
      'career',
      'business',
      'study',
    ],
  },
  YOUNG_READER: {
    categories: [
      'thieu nhi',
      'fantasy',
      'phieu luu',
      'ngoai ngu',
      'ky nang',
      'science fiction',
    ],
    keywords: [
      'adventure',
      'dream',
      'school',
      'learn',
      'coming of age',
      'expedition',
    ],
  },
}

const OCCASION_PROFILES: Record<GiftFinderOccasion, SignalProfile> = {
  BIRTHDAY: {
    categories: ['van hoc', 'tieu thuyet', 'fantasy', 'song dep', 'ky nang'],
    keywords: ['gift', 'special', 'celebrate', 'bat ngo', 'cam hung', 'joy'],
  },
  THANK_YOU: {
    categories: ['doi song', 'tam ly', 'song dep', 'tap van', 'ky nang'],
    keywords: ['gratitude', 'thank', 'cam on', 'gentle', 'healing', 'kindness'],
  },
  CELEBRATION: {
    categories: ['kinh doanh', 'ky nang', 'lich su', 'van hoc', 'fantasy'],
    keywords: ['celebrate', 'milestone', 'thanh cong', 'premium', 'aspire', 'win'],
  },
  ENCOURAGEMENT: {
    categories: ['self help', 'tam ly', 'ky nang', 'giao duc', 'song dep'],
    keywords: ['motivation', 'resilience', 'cam hung', 'healing', 'mindset', 'hope'],
  },
}

const TONE_PROFILES: Record<GiftFinderTone, SignalProfile> = {
  COZY: {
    categories: ['van hoc', 'tap van', 'doi song', 'lang man', 'truyen ngan'],
    keywords: ['cozy', 'gentle', 'thu gian', 'binh yen', 'feel good', 'calm'],
  },
  INSPIRING: {
    categories: ['song dep', 'self help', 'ky nang', 'giao duc', 'tam ly'],
    keywords: ['inspire', 'cam hung', 'mindset', 'gratitude', 'hope', 'resilience'],
  },
  PRACTICAL: {
    categories: ['kinh doanh', 'ky nang', 'ngoai ngu', 'giao duc', 'self help'],
    keywords: ['practical', 'business', 'productivity', 'study', 'learn', 'leadership'],
  },
  ESCAPIST: {
    categories: ['fantasy', 'phieu luu', 'vien tuong', 'trinh tham', 'lich su'],
    keywords: ['adventure', 'expedition', 'mystery', 'dragon', 'kingdom', 'odyssey'],
  },
}

export function isGiftFinderReady(
  answers: GiftFinderDraftAnswers,
): answers is GiftFinderAnswers {
  return Boolean(
    answers.recipient && answers.occasion && answers.budget && answers.tone,
  )
}

export function getGiftFinderRecommendations(
  books: Book[],
  answers: GiftFinderAnswers,
  limit = GIFT_FINDER_RESULT_LIMIT,
): GiftFinderRecommendation[] {
  if (books.length === 0) {
    return []
  }

  const candidateBooks = getCandidateBooks(books)
  const scoredRecommendations = candidateBooks.map((book) =>
    scoreBook(book, answers),
  )

  return sortRecommendations(scoredRecommendations)
    .slice(0, normalizeLimit(limit))
    .map(({ book, reasons, score }) => ({
      book,
      reasons,
      score,
    }))
}

export function matchesGiftBudget(price: number, budget: GiftFinderBudget) {
  switch (budget) {
    case 'UNDER_150':
      return price < 150_000
    case 'FROM_150_TO_300':
      return price >= 150_000 && price <= 300_000
    case 'ABOVE_300':
      return price > 300_000
  }
}

function scoreBook(
  book: Book,
  answers: GiftFinderAnswers,
): ScoredRecommendation {
  const searchableText = normalizeText(
    [
      book.title,
      book.author,
      book.category,
      book.publisher,
      book.description ?? '',
    ].join(' '),
  )
  const normalizedCategory = normalizeText(book.category)

  const recipientProfile = RECIPIENT_PROFILES[answers.recipient]
  const occasionProfile = OCCASION_PROFILES[answers.occasion]
  const toneProfile = TONE_PROFILES[answers.tone]

  const recipientSignal = countSignalMatches(
    searchableText,
    normalizedCategory,
    recipientProfile,
  )
  const occasionSignal = countSignalMatches(
    searchableText,
    normalizedCategory,
    occasionProfile,
  )
  const toneSignal = countSignalMatches(
    searchableText,
    normalizedCategory,
    toneProfile,
  )

  const recipientMatched = recipientSignal > 0
  const occasionMatched = occasionSignal > 0
  const toneMatched = toneSignal > 0
  const budgetMatched = matchesGiftBudget(book.price, answers.budget)

  const reasons: GiftFinderReason[] = []
  let score = 0

  if (recipientMatched) {
    score += 8 + recipientSignal * 1.5
    reasons.push('RECIPIENT')
  }

  if (occasionMatched) {
    score += 7 + occasionSignal * 1.3
    reasons.push('OCCASION')
  }

  if (toneMatched) {
    score += 6 + toneSignal * 1.2
    reasons.push('TONE')
  }

  if (budgetMatched) {
    score += 5
    reasons.push('BUDGET')
  }

  score += getBudgetAffinityBonus(book.price, answers.budget)

  if (isGiftableLength(book.detail?.pageCount ?? null)) {
    score += 1.4
    reasons.push('GIFTABLE_PICK')
  }

  const ratingValue = book.rating ?? 0
  const reviewCount = book.reviews ?? 0
  if (ratingValue > 0) {
    score += ratingValue * 0.9 + Math.min(reviewCount / 25, 2)
    if (ratingValue >= 4.2) {
      reasons.push('HIGH_RATING')
    }
  }

  if (book.soldCount > 0) {
    score += Math.min(Math.log10(book.soldCount + 1) * 2.2, 3.5)
    if (book.soldCount >= 12) {
      reasons.push('POPULAR_PICK')
    }
  }

  if (isRecentBook(book.createdAt)) {
    score += 0.5
  }

  return {
    book,
    reasons: dedupeReasons(reasons),
    score,
    budgetMatched,
    recipientMatched,
    occasionMatched,
    toneMatched,
  }
}

function getCandidateBooks(books: Book[]) {
  return books.filter((book) => book.stockQuantity > 0)
}

function countSignalMatches(
  searchableText: string,
  normalizedCategory: string,
  profile: SignalProfile,
) {
  const categoryMatchCount = profile.categories.filter((category) =>
    normalizedCategory.includes(category),
  ).length
  const keywordMatchCount = profile.keywords.filter((keyword) =>
    searchableText.includes(keyword),
  ).length

  return categoryMatchCount * 2 + keywordMatchCount
}

function sortRecommendations(recommendations: ScoredRecommendation[]) {
  return [...recommendations].sort((first, second) => {
    if (second.score !== first.score) {
      return second.score - first.score
    }

    const firstSignalMatches = Number(first.budgetMatched)
      + Number(first.recipientMatched)
      + Number(first.occasionMatched)
      + Number(first.toneMatched)
    const secondSignalMatches = Number(second.budgetMatched)
      + Number(second.recipientMatched)
      + Number(second.occasionMatched)
      + Number(second.toneMatched)

    if (secondSignalMatches !== firstSignalMatches) {
      return secondSignalMatches - firstSignalMatches
    }

    const secondRating = second.book.rating ?? 0
    const firstRating = first.book.rating ?? 0
    if (secondRating !== firstRating) {
      return secondRating - firstRating
    }

    if (second.book.soldCount !== first.book.soldCount) {
      return second.book.soldCount - first.book.soldCount
    }

    return second.book.updatedAt.localeCompare(first.book.updatedAt)
  })
}

function getBudgetAffinityBonus(price: number, budget: GiftFinderBudget) {
  switch (budget) {
    case 'UNDER_150':
      return price < 150_000 ? (150_000 - price) / 60_000 : 0
    case 'FROM_150_TO_300': {
      const midpointDistance = Math.abs(price - 220_000)
      return price >= 150_000 && price <= 300_000
        ? Math.max(0, 1.5 - midpointDistance / 60_000)
        : 0
    }
    case 'ABOVE_300':
      return price > 300_000 ? Math.min((price - 300_000) / 120_000, 1.8) : 0
  }
}

function isGiftableLength(pageCount: number | null) {
  if (pageCount == null) {
    return false
  }

  return pageCount >= 120 && pageCount <= 420
}

function isRecentBook(createdAt: string) {
  const createdTimestamp = Date.parse(createdAt)
  if (Number.isNaN(createdTimestamp)) {
    return false
  }

  const maxAgeInMilliseconds = RECENT_BOOK_WINDOW_DAYS * 24 * 60 * 60 * 1000
  return Date.now() - createdTimestamp <= maxAgeInMilliseconds
}

function normalizeLimit(limit: number) {
  return Math.min(Math.max(limit, 1), MAX_RECOMMENDATIONS)
}

function dedupeReasons(reasons: GiftFinderReason[]) {
  const uniqueReasons = new Set(reasons)

  return DISPLAY_REASON_PRIORITY.filter((reason) => uniqueReasons.has(reason)).slice(
    0,
    6,
  )
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
