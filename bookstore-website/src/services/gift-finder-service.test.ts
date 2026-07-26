import { describe, expect, it } from 'vitest'
import type { Book } from '@/types/book'
import type { GiftFinderAnswers } from '@/types/gift-finder'
import {
  getGiftFinderRecommendations,
  matchesGiftBudget,
} from './gift-finder-service'

const baseAnswers: GiftFinderAnswers = {
  recipient: 'COLLEAGUE',
  occasion: 'ENCOURAGEMENT',
  budget: 'FROM_150_TO_300',
  tone: 'PRACTICAL',
}

describe('gift-finder-service', () => {
  it('ranks books with stronger recipient, occasion, and tone signals above unrelated books', () => {
    const matchedBook = createBook({
      id: 'matched',
      category: 'Kỹ năng',
      description:
        'A practical productivity and mindset gift for a teammate who needs a confidence boost.',
      price: 210_000,
    })
    const unrelatedBook = createBook({
      id: 'unrelated',
      category: 'Kinh dị',
      description: 'A dark haunted mystery with no practical angle.',
      price: 210_000,
    })

    const recommendations = getGiftFinderRecommendations(
      [unrelatedBook, matchedBook],
      baseAnswers,
      8,
    )

    expect(recommendations[0]?.book.id).toBe('matched')
    expect(recommendations[0]?.reasons).toEqual(
      expect.arrayContaining(['RECIPIENT', 'OCCASION', 'TONE', 'BUDGET']),
    )
  })

  it('evaluates gift budget bands correctly', () => {
    expect(matchesGiftBudget(149_000, 'UNDER_150')).toBe(true)
    expect(matchesGiftBudget(150_000, 'UNDER_150')).toBe(false)

    expect(matchesGiftBudget(150_000, 'FROM_150_TO_300')).toBe(true)
    expect(matchesGiftBudget(300_000, 'FROM_150_TO_300')).toBe(true)
    expect(matchesGiftBudget(149_999, 'FROM_150_TO_300')).toBe(false)
    expect(matchesGiftBudget(300_001, 'FROM_150_TO_300')).toBe(false)

    expect(matchesGiftBudget(300_001, 'ABOVE_300')).toBe(true)
    expect(matchesGiftBudget(300_000, 'ABOVE_300')).toBe(false)
  })

  it('keeps only in-stock books in recommendations', () => {
    const outOfStockPerfectMatch = createBook({
      id: 'out-of-stock',
      category: 'Kỹ năng',
      description: 'A perfect practical encouragement gift.',
      price: 210_000,
      stockQuantity: 0,
      rating: 5,
      soldCount: 120,
    })
    const inStockMatch = createBook({
      id: 'in-stock',
      category: 'Kỹ năng',
      description: 'A strong practical encouragement gift.',
      price: 205_000,
      stockQuantity: 7,
    })

    const recommendations = getGiftFinderRecommendations(
      [outOfStockPerfectMatch, inStockMatch],
      baseAnswers,
      8,
    )

    expect(recommendations.map((recommendation) => recommendation.book.id)).toEqual(
      ['in-stock'],
    )
  })

  it('caps the result set to the requested limit', () => {
    const recommendations = getGiftFinderRecommendations(
      Array.from({ length: 12 }, (_, index) =>
        createBook({
          id: `book-${index}`,
          category: 'Kỹ năng',
          description: 'A practical encouragement gift pick.',
          price: 200_000 + index * 1_000,
        }),
      ),
      baseAnswers,
      8,
    )

    expect(recommendations).toHaveLength(8)
  })

  it('uses ratings and popularity as tie-breakers when match signals are similar', () => {
    const strongSocialProof = createBook({
      id: 'social-proof',
      category: 'Kỹ năng',
      description: 'A practical encouragement gift pick.',
      price: 210_000,
      rating: 4.8,
      reviews: 40,
      soldCount: 140,
    })
    const lowerSocialProof = createBook({
      id: 'lower-proof',
      category: 'Kỹ năng',
      description: 'A practical encouragement gift pick.',
      price: 210_000,
      rating: 4.1,
      reviews: 8,
      soldCount: 12,
    })

    const recommendations = getGiftFinderRecommendations(
      [lowerSocialProof, strongSocialProof],
      baseAnswers,
      8,
    )

    expect(recommendations[0]?.book.id).toBe('social-proof')
    expect(recommendations[0]?.reasons).toContain('HIGH_RATING')
    expect(recommendations[0]?.reasons).toContain('POPULAR_PICK')
  })
})

function createBook(overrides: Partial<Book> = {}): Book {
  const id = overrides.id ?? 'book-default'

  return {
    id,
    title: overrides.title ?? `Book ${id}`,
    isbn: overrides.isbn ?? null,
    author: overrides.author ?? 'Author',
    category: overrides.category ?? 'Văn học',
    price: overrides.price ?? 180_000,
    oldPrice: overrides.oldPrice,
    rating: overrides.rating ?? 4.2,
    reviews: overrides.reviews ?? 18,
    soldCount: overrides.soldCount ?? 22,
    starBreakdown: overrides.starBreakdown ?? {},
    bestseller: overrides.bestseller,
    cover: overrides.cover ?? null,
    images: overrides.images ?? [],
    detail: overrides.detail ?? createDetail(260),
    description:
      overrides.description ?? 'A thoughtful and giftable pick for a meaningful occasion.',
    stockQuantity: overrides.stockQuantity ?? 11,
    publisher: overrides.publisher ?? 'Publisher',
    categoryId: overrides.categoryId ?? 'category-id',
    authorId: overrides.authorId ?? 'author-id',
    publisherId: overrides.publisherId ?? 'publisher-id',
    createdAt: overrides.createdAt ?? '2025-01-01T00:00:00.000Z',
    updatedAt: overrides.updatedAt ?? '2025-01-01T00:00:00.000Z',
  }
}

function createDetail(pageCount: number | null): Book['detail'] {
  return {
    id: pageCount == null ? 'detail-missing' : `detail-${pageCount}`,
    bookId: 'book-id',
    pageCount,
    publicationYear: 2024,
    language: 'vi',
    coverType: 'Bìa mềm',
    dimensions: null,
    weight: null,
    translator: null,
    edition: null,
  }
}
