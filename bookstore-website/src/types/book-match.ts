import type { Book } from './book'

export const BOOK_MATCH_MOODS = [
  'RELAX',
  'STUDY',
  'ADVENTURE',
  'MYSTERY',
  'HEALING',
] as const

export const BOOK_MATCH_BUDGETS = [
  'UNDER_100',
  'FROM_100_TO_200',
  'ABOVE_200',
] as const

export const BOOK_MATCH_READING_TIMES = ['SHORT', 'MEDIUM', 'LONG'] as const

export type BookMatchMood = (typeof BOOK_MATCH_MOODS)[number]
export type BookMatchBudget = (typeof BOOK_MATCH_BUDGETS)[number]
export type BookMatchReadingTime = (typeof BOOK_MATCH_READING_TIMES)[number]

export type BookMatchDraftAnswers = {
  mood: BookMatchMood | null
  budget: BookMatchBudget | null
  readingTime: BookMatchReadingTime | null
}

export type BookMatchAnswers = {
  mood: BookMatchMood
  budget: BookMatchBudget
  readingTime: BookMatchReadingTime
}

export type BookMatchReason =
  | 'MOOD'
  | 'BUDGET'
  | 'READING_TIME'
  | 'HIGH_RATING'
  | 'POPULAR_PICK'
  | 'FRESH_PICK'

export type BookMatchRecommendation = {
  book: Book
  reasons: BookMatchReason[]
  score: number
}
