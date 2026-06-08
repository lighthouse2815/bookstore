import { useEffect, useState } from 'react'
import { getBookCatalog } from '@/services/book-service'
import type { Book } from '@/types/book'
import { getErrorMessage } from '@/utils'

type UseBookCatalogResult = {
  books: Book[]
  categories: string[]
  isLoading: boolean
  error: string | null
}

const initialState: UseBookCatalogResult = {
  books: [],
  categories: [],
  isLoading: true,
  error: null,
}

export function useBookCatalog() {
  const [state, setState] = useState<UseBookCatalogResult>(initialState)

  useEffect(() => {
    let isCancelled = false

    async function loadBookCatalog() {
      try {
        const catalog = await getBookCatalog()

        if (isCancelled) {
          return
        }

        setState({
          books: catalog.books,
          categories: catalog.categories,
          isLoading: false,
          error: null,
        })
      } catch (error) {
        if (isCancelled) {
          return
        }

        setState({
          books: [],
          categories: [],
          isLoading: false,
          error: getErrorMessage(error),
        })
      }
    }

    loadBookCatalog()

    return () => {
      isCancelled = true
    }
  }, [])

  return state
}
