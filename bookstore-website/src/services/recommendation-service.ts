import api from '@/services/api'
import {
  getBookReferenceData,
  mapBookResponseToBook,
} from '@/services/book-service'
import type { ApiResponse } from '@/types/api'
import type {
  PersonalizedRecommendation,
  PersonalizedRecommendationResponse,
} from '@/types/book-recommendation'
import { unwrapResponse } from '@/utils'

export async function getPersonalizedRecommendations(
  limit = 12,
): Promise<PersonalizedRecommendation> {
  const [response, referenceData] = await Promise.all([
    api.get<ApiResponse<PersonalizedRecommendationResponse>>(
      '/books/recommendations/personalized',
      { params: { limit } },
    ),
    getBookReferenceData(),
  ])
  const recommendation = unwrapResponse(response)
  const referenceMaps = {
    authorMap: new Map(referenceData.authors.map((author) => [author.id, author.name])),
    categoryMap: new Map(
      referenceData.categories.map((category) => [category.id, category.name]),
    ),
    publisherMap: new Map(
      referenceData.publishers.map((publisher) => [publisher.id, publisher.name]),
    ),
  }

  return {
    ...recommendation,
    items: recommendation.items.map((item) => ({
      ...item,
      book: mapBookResponseToBook(item.book, referenceMaps),
    })),
  }
}
