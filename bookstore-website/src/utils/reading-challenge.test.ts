import { describe, expect, it } from 'vitest'
import type {
  ReadingChallenge,
  ReadingChallengeStorageLike,
} from '@/types/reading-challenge'
import {
  clampCompletedBooks,
  createReadingChallenge,
  getReadingChallengeDaysRemaining,
  getReadingChallengeProgressPercent,
  getReadingChallengeStatus,
  loadReadingChallenge,
  resetReadingChallenge,
  saveReadingChallenge,
} from '@/utils/reading-challenge'

const NOW = new Date('2026-07-09T09:00:00.000Z')

describe('reading-challenge utils', () => {
  it('creates a valid challenge from a preset draft', () => {
    const challenge = createReadingChallenge(
      {
        title: 'Đọc 5 cuốn tháng này',
        targetBooks: 5,
        preset: 'MONTH',
      },
      NOW,
    )

    expect(challenge.title).toBe('Đọc 5 cuốn tháng này')
    expect(challenge.targetBooks).toBe(5)
    expect(challenge.completedBooks).toBe(0)
    expect(challenge.startDate).toBe('2026-07-09')
    expect(challenge.endDate).toBe('2026-08-09')
    expect(challenge.id).toBeTruthy()
  })

  it('calculates progress percentage from the current totals', () => {
    expect(
      getReadingChallengeProgressPercent(
        createChallenge({ completedBooks: 2, targetBooks: 5 }),
      ),
    ).toBe(40)

    expect(
      getReadingChallengeProgressPercent(
        createChallenge({ completedBooks: 7, targetBooks: 5 }),
      ),
    ).toBe(100)
  })

  it('clamps completed books into the 0..target range', () => {
    expect(clampCompletedBooks(-2, 5)).toBe(0)
    expect(clampCompletedBooks(8, 5)).toBe(5)
  })

  it('calculates the remaining days from the deadline', () => {
    expect(getReadingChallengeDaysRemaining('2026-07-14', NOW)).toBe(5)
    expect(getReadingChallengeDaysRemaining('2026-07-09', NOW)).toBe(0)
    expect(getReadingChallengeDaysRemaining('2026-07-08', NOW)).toBe(-1)
  })

  it('determines the status badge from progress and deadlines', () => {
    expect(
      getReadingChallengeStatus(
        createChallenge({
          completedBooks: 0,
          targetBooks: 5,
          endDate: '2026-07-20',
        }),
        NOW,
      ),
    ).toBe('NOT_STARTED')

    expect(
      getReadingChallengeStatus(
        createChallenge({
          completedBooks: 2,
          targetBooks: 5,
          endDate: '2026-07-20',
        }),
        NOW,
      ),
    ).toBe('IN_PROGRESS')

    expect(
      getReadingChallengeStatus(
        createChallenge({
          completedBooks: 4,
          targetBooks: 5,
          endDate: '2026-07-20',
        }),
        NOW,
      ),
    ).toBe('NEAR_COMPLETION')

    expect(
      getReadingChallengeStatus(
        createChallenge({
          completedBooks: 5,
          targetBooks: 5,
          endDate: '2026-07-20',
        }),
        NOW,
      ),
    ).toBe('COMPLETED')

    expect(
      getReadingChallengeStatus(
        createChallenge({
          completedBooks: 2,
          targetBooks: 5,
          endDate: '2026-07-08',
        }),
        NOW,
      ),
    ).toBe('OVERDUE')
  })

  it('safely saves, loads, and resets local storage values', () => {
    const storage = createStorageMock()
    const challenge = createChallenge()

    expect(saveReadingChallenge(storage, challenge)).toBe(true)
    expect(loadReadingChallenge(storage)).toEqual(challenge)
    expect(resetReadingChallenge(storage)).toBe(true)
    expect(loadReadingChallenge(storage)).toBeNull()

    const throwingStorage = createThrowingStorage()
    expect(saveReadingChallenge(throwingStorage, challenge)).toBe(false)
    expect(loadReadingChallenge(throwingStorage)).toBeNull()
    expect(resetReadingChallenge(throwingStorage)).toBe(false)
  })
})

function createChallenge(
  overrides: Partial<ReadingChallenge> = {},
): ReadingChallenge {
  return {
    id: overrides.id ?? 'challenge-1',
    title: overrides.title ?? 'Đọc 5 cuốn tháng này',
    targetBooks: overrides.targetBooks ?? 5,
    completedBooks: overrides.completedBooks ?? 0,
    startDate: overrides.startDate ?? '2026-07-09',
    endDate: overrides.endDate ?? '2026-07-31',
    createdAt: overrides.createdAt ?? '2026-07-09T09:00:00.000Z',
    updatedAt: overrides.updatedAt ?? '2026-07-09T09:00:00.000Z',
  }
}

function createStorageMock(): ReadingChallengeStorageLike {
  const storage = new Map<string, string>()

  return {
    getItem: (key) => storage.get(key) ?? null,
    setItem: (key, value) => {
      storage.set(key, value)
    },
    removeItem: (key) => {
      storage.delete(key)
    },
  }
}

function createThrowingStorage(): ReadingChallengeStorageLike {
  return {
    getItem: () => {
      throw new Error('localStorage disabled')
    },
    setItem: () => {
      throw new Error('localStorage disabled')
    },
    removeItem: () => {
      throw new Error('localStorage disabled')
    },
  }
}
