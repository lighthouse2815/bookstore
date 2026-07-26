import type { Book, BookResponse } from '@/types/book'
import type { PageRequest, PageResult } from '@/types/pagination'

export type ReadingJournalEntryResponse = {
  id: string
  entryDate: string
  note: string | null
  currentPage: number | null
  progressPercent: number | null
  createdAt: string
  updatedAt: string
  book: BookResponse | null
}

export type ReadingStreakResponse = {
  currentStreak: number
  longestStreak: number
  checkedInToday: boolean
  lastActivityDate: string | null
}

export type ReadingJournalBook = Pick<
  Book,
  | 'id'
  | 'title'
  | 'author'
  | 'category'
  | 'categoryInfo'
  | 'price'
  | 'cover'
  | 'stockQuantity'
> &
  Partial<Pick<Book, 'rating' | 'reviews'>>

export type ReadingJournalEntry = {
  id: string
  entryDate: string
  note: string | null
  currentPage: number | null
  progressPercent: number | null
  createdAt: string
  updatedAt: string
  book: ReadingJournalBook
}

export type ReadingJournalPageResult = PageResult<ReadingJournalEntry>

export type ReadingJournalFilter = PageRequest & {
  bookId?: string
  from?: string
  to?: string
}

export type CreateReadingJournalEntryRequest = {
  bookId: string
  entryDate: string
  note?: string | null
  currentPage?: number | null
  progressPercent?: number | null
}

export type UpdateReadingJournalEntryRequest = {
  note?: string | null
  currentPage?: number | null
  progressPercent?: number | null
}

export type CheckInReadingStreakRequest = {
  bookId: string
  note?: string | null
  currentPage?: number | null
  progressPercent?: number | null
}

export type ReadingJournalComposerState = {
  bookId: string
  entryDate: string
  note: string
  currentPage: string
  progressPercent: string
}
