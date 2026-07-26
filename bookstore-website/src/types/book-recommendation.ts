import type { Book, BookResponse } from '@/types/book'

export type RecommendationReasonCode =
  | 'PURCHASE_HISTORY'
  | 'FAVORITE_CATEGORY'
  | 'FAVORITE_AUTHOR'
  | 'WISHLIST_SIGNAL'
  | 'BOOKSHELF_SIGNAL'
  | 'HIGH_RATING_REVIEW'
  | 'READING_JOURNAL_SIGNAL'
  | 'POPULAR_PICK'
  | 'HIGH_RATING'
  | 'NEW_RELEASE'
  | 'FALLBACK_POPULAR'

export type PersonalizedRecommendationResponse = {
  items: Array<{
    book: BookResponse
    reasonCodes: RecommendationReasonCode[]
  }>
  strategy: 'PERSONALIZED' | 'FALLBACK_POPULAR'
  hasPersonalSignals: boolean
  generatedAt: string
}

export type PersonalizedRecommendation = {
  items: Array<{
    book: Book
    reasonCodes: RecommendationReasonCode[]
  }>
  strategy: PersonalizedRecommendationResponse['strategy']
  hasPersonalSignals: boolean
  generatedAt: string
}
