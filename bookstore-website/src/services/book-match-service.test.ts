import { describe, expect, it } from 'vitest'
import type { Book } from '@/types/book'
import type { BookMatchAnswers } from '@/types/book-match'
import {
  getBookMatchRecommendations,
  hasBookMatchWeakPageCountCoverage,
  matchesBudget,
  matchesReadingTime,
} from './book-match-service'

const baseAnswers: BookMatchAnswers = {
  mood: 'RELAX',
  budget: 'FROM_100_TO_200',
  readingTime: 'MEDIUM',
}

describe('book-match-service', () => {
  it('ranks mood-matching books above non-matching books when other signals are equal', () => {
    const matchingBook = createBook({
      id: 'relax-match',
      category: 'Văn học',
      description: 'Một cuốn sách feel good để thư giãn cuối ngày.',
    })
    const nonMatchingBook = createBook({
      id: 'non-match',
      category: 'Khoa học & công nghệ',
      description: 'Sách thiên về kỹ thuật và lập trình.',
    })

    const recommendations = getBookMatchRecommendations(
      [nonMatchingBook, matchingBook],
      baseAnswers,
      6,
    )

    expect(recommendations[0]?.book.id).toBe('relax-match')
    expect(recommendations[0]?.reasons).toContain('MOOD')
  })

  it('evaluates budget bands correctly', () => {
    expect(matchesBudget(99_000, 'UNDER_100')).toBe(true)
    expect(matchesBudget(100_000, 'UNDER_100')).toBe(false)

    expect(matchesBudget(100_000, 'FROM_100_TO_200')).toBe(true)
    expect(matchesBudget(200_000, 'FROM_100_TO_200')).toBe(true)
    expect(matchesBudget(99_000, 'FROM_100_TO_200')).toBe(false)
    expect(matchesBudget(200_001, 'FROM_100_TO_200')).toBe(false)

    expect(matchesBudget(200_001, 'ABOVE_200')).toBe(true)
    expect(matchesBudget(200_000, 'ABOVE_200')).toBe(false)
  })

  it('evaluates reading times correctly from pageCount', () => {
    expect(matchesReadingTime(220, 'SHORT')).toBe(true)
    expect(matchesReadingTime(221, 'SHORT')).toBe(false)

    expect(matchesReadingTime(221, 'MEDIUM')).toBe(true)
    expect(matchesReadingTime(380, 'MEDIUM')).toBe(true)
    expect(matchesReadingTime(220, 'MEDIUM')).toBe(false)
    expect(matchesReadingTime(381, 'MEDIUM')).toBe(false)

    expect(matchesReadingTime(381, 'LONG')).toBe(true)
    expect(matchesReadingTime(380, 'LONG')).toBe(false)
    expect(matchesReadingTime(null, 'LONG')).toBe(false)
  })

  it('falls back to the next best in-stock books when strict matches are not enough', () => {
    const strictMatch = createBook({
      id: 'strict-match',
      category: 'Văn học',
      price: 135_000,
      detail: createDetail(260),
    })
    const fallbackMatch = createBook({
      id: 'fallback-match',
      category: 'Văn học',
      price: 145_000,
      detail: createDetail(null),
      rating: 5,
      soldCount: 90,
    })

    const recommendations = getBookMatchRecommendations(
      [strictMatch, fallbackMatch],
      baseAnswers,
      6,
    )

    expect(recommendations).toHaveLength(2)
    expect(recommendations[0]?.book.id).toBe('strict-match')
    expect(recommendations[1]?.book.id).toBe('fallback-match')
  })

  it('does not prioritize out-of-stock books when in-stock options exist', () => {
    const outOfStockPerfectMatch = createBook({
      id: 'out-of-stock',
      category: 'Văn học',
      description: 'Cozy thư giãn, đúng mood, nhưng đã hết hàng.',
      stockQuantity: 0,
      rating: 5,
      soldCount: 120,
    })
    const inStockMatch = createBook({
      id: 'in-stock',
      category: 'Văn học',
      stockQuantity: 8,
      rating: 4,
      soldCount: 10,
    })

    const recommendations = getBookMatchRecommendations(
      [outOfStockPerfectMatch, inStockMatch],
      baseAnswers,
      6,
    )

    expect(recommendations.map((recommendation) => recommendation.book.id)).toEqual(
      ['in-stock'],
    )
  })

  it('flags weak page-count coverage when many recommended books lack pageCount', () => {
    expect(
      hasBookMatchWeakPageCountCoverage([
        createBook({ id: 'missing-1', detail: createDetail(null) }),
        createBook({ id: 'missing-2', detail: createDetail(null) }),
        createBook({ id: 'present', detail: createDetail(310) }),
      ]),
    ).toBe(true)

    expect(
      hasBookMatchWeakPageCountCoverage([
        createBook({ id: 'present-1', detail: createDetail(280) }),
        createBook({ id: 'present-2', detail: createDetail(320) }),
        createBook({ id: 'missing-3', detail: createDetail(null) }),
      ]),
    ).toBe(false)
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
    price: overrides.price ?? 150_000,
    oldPrice: overrides.oldPrice,
    rating: overrides.rating ?? 4,
    reviews: overrides.reviews ?? 12,
    soldCount: overrides.soldCount ?? 18,
    starBreakdown: overrides.starBreakdown ?? {},
    bestseller: overrides.bestseller,
    cover: overrides.cover ?? null,
    images: overrides.images ?? [],
    detail: overrides.detail ?? createDetail(260),
    description: overrides.description ?? 'Một cuốn sách đời sống nhẹ nhàng.',
    stockQuantity: overrides.stockQuantity ?? 12,
    publisher: overrides.publisher ?? 'Publisher',
    categoryId: overrides.categoryId ?? 'category-id',
    authorId: overrides.authorId ?? 'author-id',
    publisherId: overrides.publisherId ?? 'publisher-id',
    createdAt: overrides.createdAt ?? '2020-01-01T00:00:00.000Z',
    updatedAt: overrides.updatedAt ?? '2020-01-01T00:00:00.000Z',
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
