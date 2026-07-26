import { useEffect, useState } from 'react'
import { getBookCatalog } from '@/services/book-service'
import {
  getGiftFinderRecommendations,
  isGiftFinderReady,
} from '@/services/gift-finder-service'
import type { Book } from '@/types/book'
import type {
  GiftFinderBudget,
  GiftFinderDraftAnswers,
  GiftFinderOccasion,
  GiftFinderRecommendation,
  GiftFinderRecipient,
  GiftFinderTone,
} from '@/types/gift-finder'
import {
  GIFT_FINDER_BUDGETS,
  GIFT_FINDER_OCCASIONS,
  GIFT_FINDER_RECIPIENTS,
  GIFT_FINDER_TONES,
} from '@/types/gift-finder'
import { getErrorMessage } from '@/utils'

type UseGiftFinderResult = {
  answers: GiftFinderDraftAnswers
  recommendations: GiftFinderRecommendation[]
  isSubmitting: boolean
  hasSubmitted: boolean
  isCatalogEmpty: boolean
  error: string | null
  selectRecipient: (recipient: GiftFinderRecipient) => void
  selectOccasion: (occasion: GiftFinderOccasion) => void
  selectBudget: (budget: GiftFinderBudget) => void
  selectTone: (tone: GiftFinderTone) => void
  submit: () => Promise<void>
  resetQuiz: () => void
}

const initialAnswers: GiftFinderDraftAnswers = {
  recipient: null,
  occasion: null,
  budget: null,
  tone: null,
}

const GIFT_FINDER_STORAGE_KEY = 'bookstore.gift-finder.latest-answers'

export function useGiftFinder(): UseGiftFinderResult {
  const [answers, setAnswers] = useState<GiftFinderDraftAnswers>(
    readStoredGiftFinderAnswers,
  )
  const [catalogBooks, setCatalogBooks] = useState<Book[] | null>(null)
  const [recommendations, setRecommendations] = useState<
    GiftFinderRecommendation[]
  >([])
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [hasSubmitted, setHasSubmitted] = useState(false)
  const [isCatalogEmpty, setIsCatalogEmpty] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (isEmptyAnswers(answers)) {
      clearStoredGiftFinderAnswers()
      return
    }

    writeStoredGiftFinderAnswers(answers)
  }, [answers])

  function selectRecipient(recipient: GiftFinderRecipient) {
    setAnswers((currentAnswers) => ({ ...currentAnswers, recipient }))
    clearTransientState()
  }

  function selectOccasion(occasion: GiftFinderOccasion) {
    setAnswers((currentAnswers) => ({ ...currentAnswers, occasion }))
    clearTransientState()
  }

  function selectBudget(budget: GiftFinderBudget) {
    setAnswers((currentAnswers) => ({ ...currentAnswers, budget }))
    clearTransientState()
  }

  function selectTone(tone: GiftFinderTone) {
    setAnswers((currentAnswers) => ({ ...currentAnswers, tone }))
    clearTransientState()
  }

  async function submit() {
    if (!isGiftFinderReady(answers)) {
      return
    }

    setIsSubmitting(true)
    setHasSubmitted(true)
    setError(null)

    try {
      const books = await ensureCatalogLoaded()
      const nextRecommendations = getGiftFinderRecommendations(books, answers)

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
    error,
    selectRecipient,
    selectOccasion,
    selectBudget,
    selectTone,
    submit,
    resetQuiz,
  }
}

function readStoredGiftFinderAnswers(): GiftFinderDraftAnswers {
  if (typeof window === 'undefined') {
    return initialAnswers
  }

  try {
    const rawValue = window.localStorage.getItem(GIFT_FINDER_STORAGE_KEY)
    if (!rawValue) {
      return initialAnswers
    }

    const parsedValue = JSON.parse(rawValue)
    if (!isRecord(parsedValue)) {
      return initialAnswers
    }

    return {
      recipient: isGiftFinderValue(GIFT_FINDER_RECIPIENTS, parsedValue.recipient)
        ? parsedValue.recipient
        : null,
      occasion: isGiftFinderValue(GIFT_FINDER_OCCASIONS, parsedValue.occasion)
        ? parsedValue.occasion
        : null,
      budget: isGiftFinderValue(GIFT_FINDER_BUDGETS, parsedValue.budget)
        ? parsedValue.budget
        : null,
      tone: isGiftFinderValue(GIFT_FINDER_TONES, parsedValue.tone)
        ? parsedValue.tone
        : null,
    }
  } catch {
    return initialAnswers
  }
}

function writeStoredGiftFinderAnswers(answers: GiftFinderDraftAnswers) {
  if (typeof window === 'undefined') {
    return
  }

  try {
    window.localStorage.setItem(
      GIFT_FINDER_STORAGE_KEY,
      JSON.stringify(answers),
    )
  } catch {
    // Ignore storage failures and keep the quiz usable.
  }
}

function clearStoredGiftFinderAnswers() {
  if (typeof window === 'undefined') {
    return
  }

  try {
    window.localStorage.removeItem(GIFT_FINDER_STORAGE_KEY)
  } catch {
    // Ignore storage failures and keep the quiz usable.
  }
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}

function isGiftFinderValue<T extends readonly string[]>(
  options: T,
  value: unknown,
): value is T[number] {
  return typeof value === 'string' && options.includes(value)
}

function isEmptyAnswers(answers: GiftFinderDraftAnswers) {
  return (
    !answers.recipient && !answers.occasion && !answers.budget && !answers.tone
  )
}
