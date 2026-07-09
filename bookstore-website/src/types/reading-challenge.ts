export type ReadingChallenge = {
  id: string
  title: string
  targetBooks: number
  completedBooks: number
  startDate: string
  endDate: string
  createdAt: string
  updatedAt: string
}

export type ReadingChallengeStatus =
  | 'NOT_STARTED'
  | 'IN_PROGRESS'
  | 'NEAR_COMPLETION'
  | 'COMPLETED'
  | 'OVERDUE'

export type ReadingChallengePreset = 'WEEK' | 'MONTH' | 'YEAR' | 'CUSTOM'

export type ReadingChallengeDraft = {
  title: string
  targetBooks: number
  preset: ReadingChallengePreset
  endDate?: string
}

export type ReadingChallengeStorageLike = Pick<
  Storage,
  'getItem' | 'setItem' | 'removeItem'
>
