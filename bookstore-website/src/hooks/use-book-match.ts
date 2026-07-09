import { useEffect, useState } from 'react'
import { getBookCatalog } from '@/services/book-service'
import {
  getBookMatchRecommendations,
  hasBookMatchWeakPageCountCoverage,
  isBookMatchReady,
} from '@/services/book-match-service'
import type { Book } from '@/types/book'
import type {
  BookMatchBudget,
  BookMatchDraftAnswers,
  BookMatchMood,
  BookMatchRecommendation,
  BookMatchReadingTime,
} from '@/types/book-match'
import {
  BOOK_MATCH_BUDGETS,
  BOOK_MATCH_MOODS,
  BOOK_MATCH_READING_TIMES,
} from '@/types/book-match'
import { getErrorMessage } from '@/utils'

type UseBookMatchResult = {
  answers: BookMatchDraftAnswers
  recommendations: BookMatchRecommendation[]
  isSubmitting: boolean
  hasSubmitted: boolean
  isCatalogEmpty: boolean
  hasWeakPageCountHint: boolean
  error: string | null
  selectMood: (mood: BookMatchMood) => void
  selectBudget: (budget: BookMatchBudget) => void
  selectReadingTime: (readingTime: BookMatchReadingTime) => void
  submit: () => Promise<void>
  resetQuiz: () => void
}

const initialAnswers: BookMatchDraftAnswers = {
  mood: null,
  budget: null,
  readingTime: null,
}

const BOOK_MATCH_STORAGE_KEY = 'bookstore.book-match.latest-answers'

export function useBookMatch(): UseBookMatchResult {
  const [answers, setAnswers] = useState<BookMatchDraftAnswers>(
    readStoredBookMatchAnswers,
  )
  const [catalogBooks, setCatalogBooks] = useState<Book[] | null>(null)
  const [recommendations, setRecommendations] = useState<
    BookMatchRecommendation[]
  >([])
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [hasSubmitted, setHasSubmitted] = useState(false)
  const [isCatalogEmpty, setIsCatalogEmpty] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const hasWeakPageCountHint = hasBookMatchWeakPageCountCoverage(
    recommendations.map((recommendation) => recommendation.book),
  )

  useEffect(() => {
    if (isEmptyAnswers(answers)) {
      clearStoredBookMatchAnswers()
      return
    }

    writeStoredBookMatchAnswers(answers)
  }, [answers])

  function selectMood(mood: BookMatchMood) {
    setAnswers((currentAnswers) => ({ ...currentAnswers, mood }))
    clearTransientState()
  }

  function selectBudget(budget: BookMatchBudget) {
    setAnswers((currentAnswers) => ({ ...currentAnswers, budget }))
    clearTransientState()
  }

  function selectReadingTime(readingTime: BookMatchReadingTime) {
    setAnswers((currentAnswers) => ({ ...currentAnswers, readingTime }))
    clearTransientState()
  }

  async function submit() {
    if (!isBookMatchReady(answers)) {
      return
    }

    setIsSubmitting(true)
    setHasSubmitted(true)
    setError(null)

    try {
      const books = await ensureCatalogLoaded()
      const nextRecommendations = getBookMatchRecommendations(books, answers)

      setRecommendations(nextRecommendations)
      setIsCatalogEmpty(books.length === 0)
    } catch (nextError) {
      setRecommendations([])
      setIsCatalogEmpty(false)
      setError(getErrorMessage(nextError))
    } finally {
      setIsSubmitting(false)
    }
  }

  function resetQuiz() {
    setAnswers(initialAnswers)
    setRecommendations([])
    setHasSubmitted(false)
    setIsCatalogEmpty(false)
    setError(null)
  }

  async function ensureCatalogLoaded() {
    if (catalogBooks) {
      return catalogBooks
    }

    const catalog = await getBookCatalog()
    setCatalogBooks(catalog.books)
    return catalog.books
  }

  function clearTransientState() {
    setRecommendations([])
    setHasSubmitted(false)
    setIsCatalogEmpty(false)
    setError(null)
  }

  return {
    answers,
    recommendations,
    isSubmitting,
    hasSubmitted,
    isCatalogEmpty,
    hasWeakPageCountHint,
    error,
    selectMood,
    selectBudget,
    selectReadingTime,
    submit,
    resetQuiz,
  }
}

function readStoredBookMatchAnswers(): BookMatchDraftAnswers {
  if (typeof window === 'undefined') {
    return initialAnswers
  }

  try {
    const rawValue = window.localStorage.getItem(BOOK_MATCH_STORAGE_KEY)
    if (!rawValue) {
      return initialAnswers
    }

    const parsedValue = JSON.parse(rawValue)
    if (!isRecord(parsedValue)) {
      return initialAnswers
    }

    return {
      mood: isBookMatchValue(BOOK_MATCH_MOODS, parsedValue.mood)
        ? parsedValue.mood
        : null,
      budget: isBookMatchValue(BOOK_MATCH_BUDGETS, parsedValue.budget)
        ? parsedValue.budget
        : null,
      readingTime: isBookMatchValue(
        BOOK_MATCH_READING_TIMES,
        parsedValue.readingTime,
      )
        ? parsedValue.readingTime
        : null,
    }
  } catch {
    return initialAnswers
  }
}

function writeStoredBookMatchAnswers(answers: BookMatchDraftAnswers) {
  if (typeof window === 'undefined') {
    return
  }

  try {
    window.localStorage.setItem(BOOK_MATCH_STORAGE_KEY, JSON.stringify(answers))
  } catch {
    // Ignore storage failures and keep the quiz usable.
  }
}

function clearStoredBookMatchAnswers() {
  if (typeof window === 'undefined') {
    return
  }

  try {
    window.localStorage.removeItem(BOOK_MATCH_STORAGE_KEY)
  } catch {
    // Ignore storage failures and keep the quiz usable.
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function isBookMatchValue<T extends readonly string[]>(
  options: T,
  value: unknown,
): value is T[number] {
  return typeof value === 'string' && options.includes(value)
}

function isEmptyAnswers(answers: BookMatchDraftAnswers) {
  return !answers.mood && !answers.budget && !answers.readingTime
}
