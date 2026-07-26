import { beforeEach, describe, expect, it, vi } from 'vitest'

const mocks = vi.hoisted(() => ({
  apiGet: vi.fn(),
  getBookReferenceData: vi.fn(),
  mapBookResponseToBook: vi.fn(),
}))

vi.mock('@/services/api', () => ({
  default: { get: mocks.apiGet },
}))

vi.mock('@/services/book-service', () => ({
  getBookReferenceData: mocks.getBookReferenceData,
  mapBookResponseToBook: mocks.mapBookResponseToBook,
}))

import { getPersonalizedRecommendations } from './recommendation-service'

describe('recommendation-service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    mocks.getBookReferenceData.mockResolvedValue({
      authors: [{ id: 'author-1', name: 'Author' }],
      categories: [{ id: 'category-1', name: 'Category' }],
      publishers: [{ id: 'publisher-1', name: 'Publisher' }],
    })
    mocks.mapBookResponseToBook.mockReturnValue({ id: 'book-1', title: 'Mapped book' })
  })

  it('requests the authenticated recommendation endpoint with the supplied limit and maps books for BookCard', async () => {
    mocks.apiGet.mockResolvedValue({
      data: {
        success: true,
        data: {
          items: [
            {
              book: { id: 'book-1', authorId: 'author-1', categoryId: 'category-1', publisherId: 'publisher-1' },
              reasonCodes: ['FAVORITE_CATEGORY'],
            },
          ],
          strategy: 'PERSONALIZED',
          hasPersonalSignals: true,
          generatedAt: '2026-07-10T12:00:00Z',
        },
      },
    })

    const recommendation = await getPersonalizedRecommendations(6)

    expect(mocks.apiGet).toHaveBeenCalledWith('/books/recommendations/personalized', {
      params: { limit: 6 },
    })
    expect(mocks.mapBookResponseToBook).toHaveBeenCalledTimes(1)
    expect(recommendation.items[0]).toEqual({
      book: { id: 'book-1', title: 'Mapped book' },
      reasonCodes: ['FAVORITE_CATEGORY'],
    })
  })
})
