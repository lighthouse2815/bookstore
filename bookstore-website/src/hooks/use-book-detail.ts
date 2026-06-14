import axios from 'axios'
import { useEffect, useState } from 'react'
import { getBookPageDetail, getBookReviews } from '@/services/book-service'
import type {
  AuthorResponse,
  Book,
  BookCategoryTrailItem,
  BookPromotion,
  BookRatingSummary,
  BookReview,
} from '@/types/book'
import { getErrorMessage } from '@/utils'

type UseBookDetailResult = {
  book: Book | null
  suggestions: Book[]
  author: AuthorResponse | null
  categoryTrail: BookCategoryTrailItem[]
  promotions: BookPromotion[]
  ratingSummary: BookRatingSummary | null
  reviews: BookReview[]
  isLoading: boolean
  error: string | null
  notFound: boolean
}

const initialState: UseBookDetailResult = {
  book: null,
  suggestions: [],
  author: null,
  categoryTrail: [],
  promotions: [],
  ratingSummary: null,
  reviews: [],
  isLoading: true,
  error: null,
  notFound: false,
}

export function useBookDetail(id?: string) {
  const [state, setState] = useState<UseBookDetailResult>(initialState)

  useEffect(() => {
    if (!id) {
      setState({
        ...initialState,
        isLoading: false,
        notFound: true,
      })
      return
    }

    const bookId = id
    let isCancelled = false

    async function loadBookDetail() {
      try {
        const [pageDetail, reviews] = await Promise.all([
          getBookPageDetail(bookId),
          getBookReviews(bookId).catch(() => []),
        ])

        if (isCancelled) {
          return
        }

        setState({
          book: pageDetail.book,
          suggestions: pageDetail.relatedBooks,
          author: pageDetail.author,
          categoryTrail: pageDetail.categoryTrail,
          promotions: pageDetail.promotions,
          ratingSummary: pageDetail.ratingSummary,
          reviews,
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
          ...initialState,
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
