import { useEffect, useState } from 'react'
import { getBookCatalog, getBookCatalogPage } from '@/services/book-service'
import type { Book } from '@/types/book'
import type { PageRequest } from '@/types/pagination'
import { getErrorMessage } from '@/utils'

type UseBookCatalogResult = {
  books: Book[]
  categories: string[]
  categoryIds: Record<string, string>
  isLoading: boolean
  error: string | null
}

const initialState: UseBookCatalogResult = {
  books: [],
  categories: [],
  categoryIds: {},
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
          categoryIds: catalog.categoryIds,
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
          categoryIds: {},
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


type UseBookCatalogPageOptions = PageRequest & {
  keyword?: string
  categoryId?: string
}

type UseBookCatalogPageResult = UseBookCatalogResult & {
  totalCount: number
  page: number
  size: number
}

export function useBookCatalogPage({
  page = 0,
  size = 12,
  keyword,
  categoryId,
}: UseBookCatalogPageOptions): UseBookCatalogPageResult {
  const [state, setState] = useState<UseBookCatalogPageResult>({
    ...initialState,
    totalCount: 0,
    page,
    size,
  })

  useEffect(() => {
    let isCancelled = false
    setState((currentState) => ({ ...currentState, isLoading: true }))

    async function loadBookCatalogPage() {
      try {
        const catalog = await getBookCatalogPage({
          page,
          size,
          keyword,
          categoryId,
        })

        if (!isCancelled) {
          setState({
            books: catalog.books,
            categories: catalog.categories,
            categoryIds: catalog.categoryIds,
            totalCount: catalog.totalCount,
            page: catalog.page,
            size: catalog.size,
            isLoading: false,
            error: null,
          })
        }
      } catch (error) {
        if (!isCancelled) {
          setState((currentState) => ({
            ...currentState,
            books: [],
            totalCount: 0,
            isLoading: false,
            error: getErrorMessage(error),
          }))
        }
      }
    }

    void loadBookCatalogPage()

    return () => {
      isCancelled = true
    }
  }, [categoryId, keyword, page, size])

  return state
}
