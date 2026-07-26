import type {
  ReadingChallenge,
  ReadingChallengeDraft,
  ReadingChallengePreset,
  ReadingChallengeStatus,
  ReadingChallengeStorageLike,
} from '@/types/reading-challenge'

export const READING_CHALLENGE_STORAGE_KEY = 'bookstore.reading-challenge'
export const READING_CHALLENGE_URGENT_DAYS = 3
export const READING_CHALLENGE_NEAR_COMPLETION_RATIO = 0.8

const DAY_IN_MILLISECONDS = 24 * 60 * 60 * 1000

export function createReadingChallenge(
  draft: ReadingChallengeDraft,
  now = new Date(),
): ReadingChallenge {
  const title = normalizeChallengeTitle(draft.title)
  const targetBooks = normalizeTargetBooks(draft.targetBooks)
  const startDate = formatDateOnly(now)
  const endDate = resolveReadingChallengeEndDate(draft.preset, startDate, draft.endDate)

  ensureEndDateIsValid(startDate, endDate)

  const timestamp = now.toISOString()

  return {
    id: createReadingChallengeId(),
    title,
    targetBooks,
    completedBooks: 0,
    startDate,
    endDate,
    createdAt: timestamp,
    updatedAt: timestamp,
  }
}

export function updateReadingChallenge(
  challenge: ReadingChallenge,
  draft: ReadingChallengeDraft,
  now = new Date(),
): ReadingChallenge {
  const title = normalizeChallengeTitle(draft.title)
  const targetBooks = normalizeTargetBooks(draft.targetBooks)
  const endDate = resolveReadingChallengeEndDate(
    draft.preset,
    challenge.startDate,
    draft.endDate,
  )

  ensureEndDateIsValid(challenge.startDate, endDate)

  return {
    ...challenge,
    title,
    targetBooks,
    completedBooks: clampCompletedBooks(
      challenge.completedBooks,
      targetBooks,
    ),
    endDate,
    updatedAt: now.toISOString(),
  }
}

export function resetReadingChallengeProgress(
  challenge: ReadingChallenge,
  now = new Date(),
): ReadingChallenge {
  if (challenge.completedBooks === 0) {
    return challenge
  }

  return {
    ...challenge,
    completedBooks: 0,
    updatedAt: now.toISOString(),
  }
}

export function adjustReadingChallengeCompletedBooks(
  challenge: ReadingChallenge,
  delta: number,
  now = new Date(),
): ReadingChallenge {
  const nextCompletedBooks = clampCompletedBooks(
    challenge.completedBooks + delta,
    challenge.targetBooks,
  )

  if (nextCompletedBooks === challenge.completedBooks) {
    return challenge
  }

  return {
    ...challenge,
    completedBooks: nextCompletedBooks,
    updatedAt: now.toISOString(),
  }
}

export function clampCompletedBooks(completedBooks: number, targetBooks: number) {
  const safeTargetBooks = Math.max(1, Math.trunc(targetBooks) || 1)
  const safeCompletedBooks = Number.isFinite(completedBooks)
    ? Math.trunc(completedBooks)
    : 0

  return Math.min(Math.max(safeCompletedBooks, 0), safeTargetBooks)
}

export function getReadingChallengeProgressPercent(challenge: ReadingChallenge) {
  const completedBooks = clampCompletedBooks(
    challenge.completedBooks,
    challenge.targetBooks,
  )

  return Math.round((completedBooks / challenge.targetBooks) * 100)
}

export function getReadingChallengeDaysRemaining(
  endDate: string,
  now = new Date(),
) {
  const normalizedEndDate = parseDateOnly(endDate)
  if (!normalizedEndDate) {
    return 0
  }

  const normalizedNow = toLocalDate(now)
  return Math.round(
    (normalizedEndDate.getTime() - normalizedNow.getTime()) /
      DAY_IN_MILLISECONDS,
  )
}

export function getReadingChallengeStatus(
  challenge: ReadingChallenge,
  now = new Date(),
): ReadingChallengeStatus {
  const completedBooks = clampCompletedBooks(
    challenge.completedBooks,
    challenge.targetBooks,
  )

  if (completedBooks >= challenge.targetBooks) {
    return 'COMPLETED'
  }

  const daysRemaining = getReadingChallengeDaysRemaining(challenge.endDate, now)
  if (daysRemaining < 0) {
    return 'OVERDUE'
  }

  if (completedBooks === 0) {
    return 'NOT_STARTED'
  }

  if (
    completedBooks / challenge.targetBooks >=
    READING_CHALLENGE_NEAR_COMPLETION_RATIO
  ) {
    return 'NEAR_COMPLETION'
  }

  return 'IN_PROGRESS'
}

export function isReadingChallengeUrgent(
  challenge: ReadingChallenge,
  now = new Date(),
) {
  const daysRemaining = getReadingChallengeDaysRemaining(challenge.endDate, now)
  const status = getReadingChallengeStatus(challenge, now)

  return (
    status !== 'COMPLETED' &&
    status !== 'OVERDUE' &&
    daysRemaining >= 0 &&
    daysRemaining <= READING_CHALLENGE_URGENT_DAYS
  )
}

export function resolveReadingChallengeEndDate(
  preset: ReadingChallengePreset,
  baseDate: Date | string,
  customEndDate?: string,
) {
  const normalizedBaseDate =
    typeof baseDate === 'string' ? parseDateOnly(baseDate) : toLocalDate(baseDate)

  if (!normalizedBaseDate) {
    throw new Error('READING_CHALLENGE_BASE_DATE_INVALID')
  }

  switch (preset) {
    case 'WEEK':
      return formatDateOnly(addDays(normalizedBaseDate, 7))
    case 'MONTH':
      return formatDateOnly(addMonths(normalizedBaseDate, 1))
    case 'YEAR':
      return formatDateOnly(addYears(normalizedBaseDate, 1))
    case 'CUSTOM':
      if (!customEndDate) {
        throw new Error('READING_CHALLENGE_END_DATE_REQUIRED')
      }

      if (!parseDateOnly(customEndDate)) {
        throw new Error('READING_CHALLENGE_END_DATE_INVALID')
      }

      return customEndDate
  }
}

export function inferReadingChallengePreset(
  startDate: string,
  endDate: string,
): ReadingChallengePreset {
  if (resolveReadingChallengeEndDate('WEEK', startDate) === endDate) {
    return 'WEEK'
  }

  if (resolveReadingChallengeEndDate('MONTH', startDate) === endDate) {
    return 'MONTH'
  }

  if (resolveReadingChallengeEndDate('YEAR', startDate) === endDate) {
    return 'YEAR'
  }

  return 'CUSTOM'
}

export function loadReadingChallenge(
  storage: ReadingChallengeStorageLike,
  storageKey = READING_CHALLENGE_STORAGE_KEY,
) {
  try {
    const rawValue = storage.getItem(storageKey)
    if (!rawValue) {
      return null
    }

    const challenge = parseStoredReadingChallenge(rawValue)
    if (challenge) {
      return challenge
    }

    try {
      storage.removeItem(storageKey)
    } catch {
      // Ignore cleanup failures and keep the UI usable.
    }

    return null
  } catch {
    return null
  }
}

export function saveReadingChallenge(
  storage: ReadingChallengeStorageLike,
  challenge: ReadingChallenge,
  storageKey = READING_CHALLENGE_STORAGE_KEY,
) {
  try {
    storage.setItem(storageKey, JSON.stringify(challenge))
    return true
  } catch {
    return false
  }
}

export function resetReadingChallenge(
  storage: ReadingChallengeStorageLike,
  storageKey = READING_CHALLENGE_STORAGE_KEY,
) {
  try {
    storage.removeItem(storageKey)
    return true
  } catch {
    return false
  }
}

function parseStoredReadingChallenge(rawValue: string) {
  try {
    const parsedValue: unknown = JSON.parse(rawValue)
    if (!isRecord(parsedValue)) {
      return null
    }

    if (
      typeof parsedValue.id !== 'string' ||
      typeof parsedValue.title !== 'string' ||
      typeof parsedValue.targetBooks !== 'number' ||
      typeof parsedValue.completedBooks !== 'number' ||
      typeof parsedValue.startDate !== 'string' ||
      typeof parsedValue.endDate !== 'string' ||
      typeof parsedValue.createdAt !== 'string' ||
      typeof parsedValue.updatedAt !== 'string'
    ) {
      return null
    }

    if (
      !parseDateOnly(parsedValue.startDate) ||
      !parseDateOnly(parsedValue.endDate) ||
      Number.isNaN(Date.parse(parsedValue.createdAt)) ||
      Number.isNaN(Date.parse(parsedValue.updatedAt))
    ) {
      return null
    }

    const title = normalizeChallengeTitle(parsedValue.title)
    const targetBooks = normalizeTargetBooks(parsedValue.targetBooks)

    ensureEndDateIsValid(parsedValue.startDate, parsedValue.endDate)

    return {
      id: parsedValue.id,
      title,
      targetBooks,
      completedBooks: clampCompletedBooks(
        parsedValue.completedBooks,
        targetBooks,
      ),
      startDate: parsedValue.startDate,
      endDate: parsedValue.endDate,
      createdAt: parsedValue.createdAt,
      updatedAt: parsedValue.updatedAt,
    } satisfies ReadingChallenge
  } catch {
    return null
  }
}

function normalizeChallengeTitle(title: string) {
  const normalizedTitle = title.trim()
  if (!normalizedTitle) {
    throw new Error('READING_CHALLENGE_TITLE_REQUIRED')
  }

  return normalizedTitle
}

function normalizeTargetBooks(targetBooks: number) {
  if (!Number.isFinite(targetBooks)) {
    throw new Error('READING_CHALLENGE_TARGET_INVALID')
  }

  const normalizedTargetBooks = Math.trunc(targetBooks)
  if (normalizedTargetBooks <= 0) {
    throw new Error('READING_CHALLENGE_TARGET_INVALID')
  }

  return normalizedTargetBooks
}

function ensureEndDateIsValid(startDate: string, endDate: string) {
  const normalizedStartDate = parseDateOnly(startDate)
  const normalizedEndDate = parseDateOnly(endDate)

  if (!normalizedStartDate || !normalizedEndDate) {
    throw new Error('READING_CHALLENGE_END_DATE_INVALID')
  }

  if (normalizedEndDate.getTime() < normalizedStartDate.getTime()) {
    throw new Error('READING_CHALLENGE_END_DATE_BEFORE_START')
  }
}

function createReadingChallengeId() {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }

  return `reading-challenge-${Date.now()}-${Math.random()
    .toString(16)
    .slice(2, 10)}`
}

function parseDateOnly(value: string) {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(value)
  if (!match) {
    return null
  }

  const year = Number(match[1])
  const month = Number(match[2]) - 1
  const day = Number(match[3])
  const candidate = new Date(year, month, day)

  if (
    candidate.getFullYear() !== year ||
    candidate.getMonth() !== month ||
    candidate.getDate() !== day
  ) {
    return null
  }

  return candidate
}

function formatDateOnly(value: Date) {
  const year = value.getFullYear()
  const month = String(value.getMonth() + 1).padStart(2, '0')
  const day = String(value.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

function toLocalDate(value: Date) {
  return new Date(value.getFullYear(), value.getMonth(), value.getDate())
}

function addDays(value: Date, days: number) {
  const nextDate = new Date(value)
  nextDate.setDate(nextDate.getDate() + days)
  return toLocalDate(nextDate)
}

function addMonths(value: Date, months: number) {
  const targetMonth = value.getMonth() + months
  const yearOffset = Math.floor(targetMonth / 12)
  const normalizedMonth = ((targetMonth % 12) + 12) % 12
  const nextYear = value.getFullYear() + yearOffset
  const nextDay = Math.min(value.getDate(), getDaysInMonth(nextYear, normalizedMonth))

  return new Date(nextYear, normalizedMonth, nextDay)
}

function addYears(value: Date, years: number) {
  const nextYear = value.getFullYear() + years
  const nextDay = Math.min(value.getDate(), getDaysInMonth(nextYear, value.getMonth()))

  return new Date(nextYear, value.getMonth(), nextDay)
}

function getDaysInMonth(year: number, month: number) {
  return new Date(year, month + 1, 0).getDate()
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}
