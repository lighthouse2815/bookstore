import { useEffect, useState } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import { getBookCatalog } from '@/services/book-service'
import {
  checkInReadingStreak,
  createReadingJournalEntry,
  deleteReadingJournalEntry,
  getMyReadingJournalEntries,
  getMyReadingStreak,
  updateReadingJournalEntry,
} from '@/services/reading-journal-service'
import type { Book } from '@/types/book'
import type {
  ReadingJournalComposerState,
  ReadingJournalEntry,
  ReadingJournalFilter,
  ReadingJournalPageResult,
  ReadingStreakResponse,
} from '@/types/reading-journal'
import {
  createReadingJournalComposerState,
  toNumericFieldValue,
} from '@/utils/reading-journal'
import { getErrorMessage } from '@/utils'

const DEFAULT_PAGE_SIZE = 8

export function useReadingJournal(options?: {
  prefill?: Partial<ReadingJournalComposerState>
}) {
  const { t } = useLanguage()
  const prefill = options?.prefill ?? {}
  const [availableBooks, setAvailableBooks] = useState<Book[]>([])
  const [entriesPage, setEntriesPage] = useState<ReadingJournalPageResult>({
    items: [],
    page: 0,
    size: DEFAULT_PAGE_SIZE,
    totalCount: 0,
    hasNext: false,
    totalPages: 0,
  })
  const [streak, setStreak] = useState<ReadingStreakResponse | null>(null)
  const [filters, setFilters] = useState<ReadingJournalFilter>({
    page: 0,
    size: DEFAULT_PAGE_SIZE,
    bookId: prefill.bookId ?? '',
    from: '',
    to: '',
  })
  const [composer, setComposer] = useState<ReadingJournalComposerState>(
    createReadingJournalComposerState(prefill),
  )
  const [editingEntryId, setEditingEntryId] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isBookOptionsLoading, setIsBookOptionsLoading] = useState(true)
  const [isStreakLoading, setIsStreakLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [refreshVersion, setRefreshVersion] = useState(0)

  useEffect(() => {
    let isCancelled = false

    async function loadAvailableBooks() {
      setIsBookOptionsLoading(true)

      try {
        const catalog = await getBookCatalog()
        if (isCancelled) {
          return
        }

        setAvailableBooks(
          [...catalog.books].sort((first, second) =>
            first.title.localeCompare(second.title),
          ),
        )
      } catch (nextError) {
        if (isCancelled) {
          return
        }

        toast.error(getErrorMessage(nextError, t('readingJournal.booksLoadError')))
      } finally {
        if (!isCancelled) {
          setIsBookOptionsLoading(false)
        }
      }
    }

    void loadAvailableBooks()

    return () => {
      isCancelled = true
    }
  }, [t])

  useEffect(() => {
    let isCancelled = false

    async function loadEntries() {
      setIsLoading(true)

      try {
        const nextEntriesPage = await getMyReadingJournalEntries(filters)
        if (isCancelled) {
          return
        }

        setEntriesPage(nextEntriesPage)
        setError(null)
      } catch (nextError) {
        if (isCancelled) {
          return
        }

        setEntriesPage((currentPage) => ({
          ...currentPage,
          items: [],
          totalCount: 0,
          hasNext: false,
          totalPages: 0,
        }))
        setError(getErrorMessage(nextError, t('readingJournal.loadError')))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadEntries()

    return () => {
      isCancelled = true
    }
  }, [filters, refreshVersion, t])

  useEffect(() => {
    let isCancelled = false

    async function loadStreak() {
      setIsStreakLoading(true)

      try {
        const nextStreak = await getMyReadingStreak()
        if (isCancelled) {
          return
        }

        setStreak(nextStreak)
      } catch (nextError) {
        if (isCancelled) {
          return
        }

        toast.error(getErrorMessage(nextError, t('readingJournal.streakLoadError')))
      } finally {
        if (!isCancelled) {
          setIsStreakLoading(false)
        }
      }
    }

    void loadStreak()

    return () => {
      isCancelled = true
    }
  }, [refreshVersion, t])

  function handleFilterChange(
    field: keyof Pick<ReadingJournalFilter, 'bookId' | 'from' | 'to'>,
    value: string,
  ) {
    setFilters((currentFilters) => ({
      ...currentFilters,
      [field]: value,
      page: 0,
    }))
  }

  function handlePageChange(nextPage: number) {
    setFilters((currentFilters) => ({
      ...currentFilters,
      page: Math.max(0, nextPage),
    }))
  }

  function resetFilters() {
    setFilters({
      page: 0,
      size: DEFAULT_PAGE_SIZE,
      bookId: '',
      from: '',
      to: '',
    })
  }

  function handleComposerFieldChange(
    field: keyof ReadingJournalComposerState,
    value: string,
  ) {
    setComposer((currentComposer) => ({
      ...currentComposer,
      [field]: value,
    }))
  }

  function startEditingEntry(entry: ReadingJournalEntry) {
    setEditingEntryId(entry.id)
    setComposer({
      bookId: entry.book.id,
      entryDate: entry.entryDate,
      note: entry.note ?? '',
      currentPage:
        typeof entry.currentPage === 'number' ? String(entry.currentPage) : '',
      progressPercent:
        typeof entry.progressPercent === 'number'
          ? String(entry.progressPercent)
          : '',
    })
  }

  function cancelEditingEntry() {
    setEditingEntryId(null)
    setComposer(
      createReadingJournalComposerState({
        bookId: composer.bookId,
      }),
    )
  }

  async function submitEntry() {
    if (!composer.bookId) {
      toast.error(t('readingJournal.validation.bookRequired'))
      return
    }

    if (!composer.entryDate) {
      toast.error(t('readingJournal.validation.entryDateRequired'))
      return
    }

    const currentPage = toNumericFieldValue(composer.currentPage)
    if (
      currentPage !== null &&
      (!Number.isInteger(currentPage) || currentPage < 0)
    ) {
      toast.error(t('readingJournal.validation.currentPage'))
      return
    }

    const progressPercent = toNumericFieldValue(composer.progressPercent)
    if (
      progressPercent !== null &&
      (Number.isNaN(progressPercent) || progressPercent < 0 || progressPercent > 100)
    ) {
      toast.error(t('readingJournal.validation.progressPercent'))
      return
    }

    setIsSubmitting(true)

    try {
      if (editingEntryId) {
        await updateReadingJournalEntry(editingEntryId, {
          note: composer.note,
          currentPage,
          progressPercent,
        })
        toast.success(t('readingJournal.updateSuccess'))
      } else {
        await createReadingJournalEntry({
          bookId: composer.bookId,
          entryDate: composer.entryDate,
          note: composer.note,
          currentPage,
          progressPercent,
        })
        toast.success(t('readingJournal.createSuccess'))
      }

      setEditingEntryId(null)
      setComposer(
        createReadingJournalComposerState({
          bookId: composer.bookId,
        }),
      )
      setFilters((currentFilters) => ({
        ...currentFilters,
        page: 0,
      }))
      setRefreshVersion((currentVersion) => currentVersion + 1)
    } catch (nextError) {
      toast.error(
        getErrorMessage(
          nextError,
          editingEntryId
            ? t('readingJournal.updateError')
            : t('readingJournal.createError'),
        ),
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  async function removeEntry(entryId: string) {
    setIsSubmitting(true)

    try {
      await deleteReadingJournalEntry(entryId)
      toast.success(t('readingJournal.deleteSuccess'))
      setRefreshVersion((currentVersion) => currentVersion + 1)
    } catch (nextError) {
      toast.error(getErrorMessage(nextError, t('readingJournal.deleteError')))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function checkInToday() {
    if (!composer.bookId) {
      toast.error(t('readingJournal.validation.bookRequired'))
      return
    }

    const currentPage = toNumericFieldValue(composer.currentPage)
    if (
      currentPage !== null &&
      (!Number.isInteger(currentPage) || currentPage < 0)
    ) {
      toast.error(t('readingJournal.validation.currentPage'))
      return
    }

    const progressPercent = toNumericFieldValue(composer.progressPercent)
    if (
      progressPercent !== null &&
      (Number.isNaN(progressPercent) || progressPercent < 0 || progressPercent > 100)
    ) {
      toast.error(t('readingJournal.validation.progressPercent'))
      return
    }

    setIsSubmitting(true)

    try {
      const nextStreak = await checkInReadingStreak({
        bookId: composer.bookId,
        note: composer.note,
        currentPage,
        progressPercent,
      })

      setStreak(nextStreak)
      toast.success(t('readingJournal.checkInSuccess'))
      setFilters((currentFilters) => ({
        ...currentFilters,
        page: 0,
      }))
      setRefreshVersion((currentVersion) => currentVersion + 1)
    } catch (nextError) {
      toast.error(getErrorMessage(nextError, t('readingJournal.checkInError')))
    } finally {
      setIsSubmitting(false)
    }
  }

  return {
    availableBooks,
    entriesPage,
    streak,
    filters,
    composer,
    editingEntryId,
    isLoading,
    isBookOptionsLoading,
    isStreakLoading,
    isSubmitting,
    error,
    handleFilterChange,
    handlePageChange,
    resetFilters,
    handleComposerFieldChange,
    startEditingEntry,
    cancelEditingEntry,
    submitEntry,
    removeEntry,
    checkInToday,
  }
}
