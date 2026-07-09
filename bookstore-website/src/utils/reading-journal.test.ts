import { describe, expect, it } from 'vitest'
import type { ReadingJournalEntry } from '@/types/reading-journal'
import {
  buildReadingJournalQueryParams,
  createReadingJournalComposerState,
  getTodayReadingJournalDate,
  groupReadingJournalEntriesByDate,
  toNumericFieldValue,
} from './reading-journal'

describe('reading-journal utils', () => {
  it('serializes filters into stable query params', () => {
    expect(
      buildReadingJournalQueryParams({
        page: 2,
        size: 12,
        bookId: '  book-1  ',
        from: '2026-07-01',
        to: '2026-07-10',
      }),
    ).toEqual({
      page: 2,
      size: 12,
      bookId: 'book-1',
      from: '2026-07-01',
      to: '2026-07-10',
    })
  })

  it('groups timeline items by entry date without changing order inside a day', () => {
    const entries = [
      createEntry({ id: 'entry-1', entryDate: '2026-07-10' }),
      createEntry({ id: 'entry-2', entryDate: '2026-07-10' }),
      createEntry({ id: 'entry-3', entryDate: '2026-07-09' }),
    ]

    expect(groupReadingJournalEntriesByDate(entries)).toEqual([
      {
        date: '2026-07-10',
        items: [entries[0], entries[1]],
      },
      {
        date: '2026-07-09',
        items: [entries[2]],
      },
    ])
  })

  it('creates a default composer state anchored to today', () => {
    expect(
      createReadingJournalComposerState({
        bookId: 'book-1',
        note: 'Ghi nhanh',
      }),
    ).toEqual({
      bookId: 'book-1',
      entryDate: getTodayReadingJournalDate(),
      note: 'Ghi nhanh',
      currentPage: '',
      progressPercent: '',
    })
  })

  it('normalizes numeric field strings into nullable numbers', () => {
    expect(toNumericFieldValue('')).toBeNull()
    expect(toNumericFieldValue(' 12 ')).toBe(12)
    expect(toNumericFieldValue('42.5')).toBe(42.5)
    expect(Number.isNaN(toNumericFieldValue('abc'))).toBe(true)
  })
})

function createEntry(
  overrides: Partial<ReadingJournalEntry> = {},
): ReadingJournalEntry {
  return {
    id: overrides.id ?? 'entry-default',
    entryDate: overrides.entryDate ?? '2026-07-10',
    note: overrides.note ?? 'Ghi chu',
    currentPage: overrides.currentPage ?? 18,
    progressPercent: overrides.progressPercent ?? 42,
    createdAt: overrides.createdAt ?? '2026-07-10T10:00:00.000Z',
    updatedAt: overrides.updatedAt ?? '2026-07-10T10:00:00.000Z',
    book: overrides.book ?? {
      id: 'book-1',
      title: 'Sach',
      author: 'Tac gia',
      category: 'Ky nang',
      price: 120_000,
      cover: null,
      stockQuantity: 10,
      rating: 4.5,
      reviews: 20,
    },
  }
}
