import type {
  ReadingJournalComposerState,
  ReadingJournalEntry,
  ReadingJournalFilter,
} from '@/types/reading-journal'

export type ReadingJournalGroup = {
  date: string
  items: ReadingJournalEntry[]
}

export function buildReadingJournalQueryParams(filter: ReadingJournalFilter) {
  return {
    page: filter.page ?? 0,
    size: filter.size ?? 8,
    bookId: normalizeOptionalString(filter.bookId),
    from: normalizeOptionalString(filter.from),
    to: normalizeOptionalString(filter.to),
  }
}

export function groupReadingJournalEntriesByDate(
  entries: ReadingJournalEntry[],
): ReadingJournalGroup[] {
  const groups = new Map<string, ReadingJournalEntry[]>()

  for (const entry of entries) {
    const bucket = groups.get(entry.entryDate) ?? []
    bucket.push(entry)
    groups.set(entry.entryDate, bucket)
  }

  return Array.from(groups.entries()).map(([date, items]) => ({
    date,
    items,
  }))
}

export function getTodayReadingJournalDate(now = new Date()) {
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

export function createReadingJournalComposerState(
  overrides: Partial<ReadingJournalComposerState> = {},
): ReadingJournalComposerState {
  return {
    bookId: overrides.bookId ?? '',
    entryDate: overrides.entryDate ?? getTodayReadingJournalDate(),
    note: overrides.note ?? '',
    currentPage: overrides.currentPage ?? '',
    progressPercent: overrides.progressPercent ?? '',
  }
}

export function toNumericFieldValue(value: string) {
  const trimmedValue = value.trim()
  if (trimmedValue === '') {
    return null
  }

  const parsedValue = Number(trimmedValue)
  return Number.isFinite(parsedValue) ? parsedValue : NaN
}

function normalizeOptionalString(value?: string) {
  const normalizedValue = value?.trim() ?? ''
  return normalizedValue === '' ? undefined : normalizedValue
}
