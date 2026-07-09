import { useState } from 'react'
import type { ReadingChallenge, ReadingChallengeDraft } from '@/types/reading-challenge'
import {
  adjustReadingChallengeCompletedBooks,
  createReadingChallenge,
  loadReadingChallenge,
  resetReadingChallenge,
  resetReadingChallengeProgress,
  saveReadingChallenge,
  updateReadingChallenge,
} from '@/utils/reading-challenge'

type SaveChallengeResult =
  | {
      success: true
      challenge: ReadingChallenge
    }
  | {
      success: false
      errorCode: string
    }

type UseReadingChallengeResult = {
  challenge: ReadingChallenge | null
  error: string | null
  saveChallenge: (draft: ReadingChallengeDraft) => SaveChallengeResult
  incrementCompletedBooks: () => void
  decrementCompletedBooks: () => void
  resetCompletedBooks: () => void
  deleteChallenge: () => void
  clearError: () => void
}

const READING_CHALLENGE_STORAGE_ERROR =
  'READING_CHALLENGE_STORAGE_UNAVAILABLE'

export function useReadingChallenge(): UseReadingChallengeResult {
  const [challenge, setChallenge] = useState<ReadingChallenge | null>(
    readStoredChallenge,
  )
  const [error, setError] = useState<string | null>(null)

  function saveChallengeDraft(draft: ReadingChallengeDraft): SaveChallengeResult {
    try {
      const nextChallenge = challenge
        ? updateReadingChallenge(challenge, draft)
        : createReadingChallenge(draft)

      persistChallenge(nextChallenge)

      return {
        success: true,
        challenge: nextChallenge,
      }
    } catch (nextError) {
      return {
        success: false,
        errorCode:
          nextError instanceof Error
            ? nextError.message
            : 'READING_CHALLENGE_UNKNOWN_ERROR',
      }
    }
  }

  function incrementCompletedBooks() {
    if (!challenge) {
      return
    }

    persistChallenge(adjustReadingChallengeCompletedBooks(challenge, 1))
  }

  function decrementCompletedBooks() {
    if (!challenge) {
      return
    }

    persistChallenge(adjustReadingChallengeCompletedBooks(challenge, -1))
  }

  function resetCompletedBooks() {
    if (!challenge) {
      return
    }

    persistChallenge(resetReadingChallengeProgress(challenge))
  }

  function deleteStoredChallenge() {
    setChallenge(null)

    if (typeof window === 'undefined') {
      return
    }

    const removed = resetReadingChallenge(window.localStorage)
    setError(removed ? null : READING_CHALLENGE_STORAGE_ERROR)
  }

  function persistChallenge(nextChallenge: ReadingChallenge) {
    setChallenge(nextChallenge)

    if (typeof window === 'undefined') {
      return
    }

    const saved = saveReadingChallenge(window.localStorage, nextChallenge)
    setError(saved ? null : READING_CHALLENGE_STORAGE_ERROR)
  }

  return {
    challenge,
    error,
    saveChallenge: saveChallengeDraft,
    incrementCompletedBooks,
    decrementCompletedBooks,
    resetCompletedBooks,
    deleteChallenge: deleteStoredChallenge,
    clearError: () => setError(null),
  }
}

function readStoredChallenge() {
  if (typeof window === 'undefined') {
    return null
  }

  return loadReadingChallenge(window.localStorage)
}
