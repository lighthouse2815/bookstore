import type { Book } from './book'

export const GIFT_FINDER_RECIPIENTS = [
  'BEST_FRIEND',
  'PARTNER',
  'PARENT',
  'COLLEAGUE',
  'YOUNG_READER',
] as const

export const GIFT_FINDER_OCCASIONS = [
  'BIRTHDAY',
  'THANK_YOU',
  'CELEBRATION',
  'ENCOURAGEMENT',
] as const

export const GIFT_FINDER_BUDGETS = [
  'UNDER_150',
  'FROM_150_TO_300',
  'ABOVE_300',
] as const

export const GIFT_FINDER_TONES = [
  'COZY',
  'INSPIRING',
  'PRACTICAL',
  'ESCAPIST',
] as const

export type GiftFinderRecipient = (typeof GIFT_FINDER_RECIPIENTS)[number]
export type GiftFinderOccasion = (typeof GIFT_FINDER_OCCASIONS)[number]
export type GiftFinderBudget = (typeof GIFT_FINDER_BUDGETS)[number]
export type GiftFinderTone = (typeof GIFT_FINDER_TONES)[number]

export type GiftFinderDraftAnswers = {
  recipient: GiftFinderRecipient | null
  occasion: GiftFinderOccasion | null
  budget: GiftFinderBudget | null
  tone: GiftFinderTone | null
}

export type GiftFinderAnswers = {
  recipient: GiftFinderRecipient
  occasion: GiftFinderOccasion
  budget: GiftFinderBudget
  tone: GiftFinderTone
}

export type GiftFinderReason =
  | 'RECIPIENT'
  | 'OCCASION'
  | 'BUDGET'
  | 'TONE'
  | 'HIGH_RATING'
  | 'POPULAR_PICK'
  | 'GIFTABLE_PICK'

export type GiftFinderRecommendation = {
  book: Book
  reasons: GiftFinderReason[]
  score: number
}
