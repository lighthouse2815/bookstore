import axios from 'axios'
import { useEffect, useState } from 'react'
import { getBookById, getBookCatalog } from '@/services/book-service'
import type { Book } from '@/types/book'
import { getErrorMessage } from '@/utils'

type UseBookDetailResult = {
  book: Book | null
  suggestions: Book[]
  isLoading: boolean
  error: string | null
  notFound: boolean
}

const initialState: UseBookDetailResult = {
  book: null,
  suggestions: [],
  isLoading: true,
  error: null,
  notFound: false,
}

export function useBookDetail(id?: string) {
  const [state, setState] = useState<UseBookDetailResult>(initialState)

  useEffect(() => {
    if (!id) {
      setState({
        book: null,
        suggestions: [],
        isLoading: false,
        error: null,
        notFound: true,
      })
      return
    }

    const bookId = id
    let isCancelled = false

    async function loadBookDetail() {
      try {
        const [book, catalog] = await Promise.all([
          getBookById(bookId),
          getBookCatalog().catch(() => ({ books: [], categories: [] })),
        ])

        if (isCancelled) {
          return
        }

        const suggestions = getBookSuggestions(book, catalog.books)

        setState({
          book,
          suggestions,
          isLoading: false,
          error: null,
          notFound: false,
        })
      } catch (error) {
        if (isCancelled) {
          return
        }

        const notFound =
          axios.isAxiosError(error) && error.response?.status === 404

        setState({
          book: null,
          suggestions: [],
          isLoading: false,
          error: notFound ? null : getErrorMessage(error),
          notFound,
        })
      }
    }

    loadBookDetail()

    return () => {
      isCancelled = true
    }
  }, [id])

  return state
}

function getBookSuggestions(book: Book, books: Book[]) {
  const relatedBooks = books.filter(
    (currentBook) =>
      currentBook.id !== book.id &&
      currentBook.category !== '' &&
      currentBook.category === book.category,
  )

  if (relatedBooks.length >= 4) {
    return relatedBooks.slice(0, 4)
  }

  const fallbackBooks = books.filter((currentBook) => currentBook.id !== book.id)

  return [...relatedBooks, ...fallbackBooks]
    .filter(
      (currentBook, index, currentBooks) =>
        currentBooks.findIndex(
          (candidateBook) => candidateBook.id === currentBook.id,
        ) === index,
    )
    .slice(0, 4)
}
