import { useEffect, useState } from 'react'
import {
  createBookshelf,
  deleteBookshelf,
  getMyBookshelves,
  updateBookshelf,
} from '@/services/bookshelf-service'
import type { Bookshelf, BookshelfSummary } from '@/types/bookshelf'
import { getErrorMessage } from '@/utils'

type UseBookshelvesResult = {
  shelves: BookshelfSummary[]
  isLoading: boolean
  isSaving: boolean
  error: string | null
  refresh: () => Promise<void>
  createShelf: (name: string) => Promise<Bookshelf>
  renameShelf: (shelfId: string, name: string) => Promise<Bookshelf>
  removeShelf: (shelfId: string) => Promise<void>
}

export function useBookshelves(): UseBookshelvesResult {
  const [shelves, setShelves] = useState<BookshelfSummary[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    void refresh()
  }, [])

  async function refresh() {
    setIsLoading(true)
    setError(null)

    try {
      setShelves(await getMyBookshelves())
    } catch (nextError) {
      setError(getErrorMessage(nextError))
    } finally {
      setIsLoading(false)
    }
  }

  async function createShelf(name: string) {
    setIsSaving(true)
    setError(null)

    try {
      const createdShelf = await createBookshelf(name)
      await refresh()
      return createdShelf
    } catch (nextError) {
      const message = getErrorMessage(nextError)
      setError(message)
      throw new Error(message)
    } finally {
      setIsSaving(false)
    }
  }

  async function renameShelf(shelfId: string, name: string) {
    setIsSaving(true)
    setError(null)

    try {
      const updatedShelf = await updateBookshelf(shelfId, name)
      await refresh()
      return updatedShelf
    } catch (nextError) {
      const message = getErrorMessage(nextError)
      setError(message)
      throw new Error(message)
    } finally {
      setIsSaving(false)
    }
  }

  async function removeShelf(shelfId: string) {
    setIsSaving(true)
    setError(null)

    try {
      await deleteBookshelf(shelfId)
      await refresh()
    } catch (nextError) {
      const message = getErrorMessage(nextError)
      setError(message)
      throw new Error(message)
    } finally {
      setIsSaving(false)
    }
  }

  return {
    shelves,
    isLoading,
    isSaving,
    error,
    refresh,
    createShelf,
    renameShelf,
    removeShelf,
  }
}
