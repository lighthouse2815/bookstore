import { useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { toast } from 'sonner'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import {
  deleteAdminReview,
  getAdminCustomers,
  getAdminEmployees,
  getAdminReviewsPage,
} from '@/services/admin-access-service'
import { getBookCatalog } from '@/services/book-service'
import type { AdminReviewResponse, AdminUserResponse } from '@/types/admin-access'
import type { Book } from '@/types/book'
import { getErrorMessage } from '@/utils'

type ReviewDialogMode = 'view' | 'delete'

type UserLookup = {
  name: string
  email: string
}

const PAGE_SIZE = 10

export function useAdminReviewsPage() {
  const { user } = useAuth()
  const { t, formatDate, formatNumber } = useLanguage()
  const [reviews, setReviews] = useState<AdminReviewResponse[]>([])
  const [page, setPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
  const [books, setBooks] = useState<Book[]>([])
  const [userLookup, setUserLookup] = useState<Record<string, UserLookup>>({})
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [dialogMode, setDialogMode] = useState<ReviewDialogMode | null>(null)
  const [selectedReview, setSelectedReview] = useState<AdminReviewResponse | null>(
    null,
  )
  const [isDeleting, setIsDeleting] = useState(false)

  const labels = useMemo(
    () => ({
      title: t('admin.reviewsPage.title'),
      description: t('admin.reviewsPage.description'),
      total: t('admin.reviewsPage.total'),
      search: t('admin.reviewsPage.search'),
      empty: t('admin.reviewsPage.empty'),
      loadError: t('admin.reviewsPage.loadError'),
      deleteError: t('admin.reviewsPage.deleteError'),
      deleteSuccess: t('admin.reviewsPage.deleteSuccess'),
      reviewer: t('admin.reviewsPage.reviewer'),
      book: t('admin.reviewsPage.book'),
      rating: t('admin.reviewsPage.rating'),
      comment: t('admin.reviewsPage.comment'),
      noComment: t('admin.reviewsPage.noComment'),
      detailTitle: t('admin.reviewsPage.detailTitle'),
      deleteTitle: t('admin.reviewsPage.deleteTitle'),
      deleteDescription: t('admin.reviewsPage.deleteDescription'),
      cancel: t('common.cancel'),
      delete: t('common.delete'),
      average: t('admin.reviewsPage.average'),
      withComment: t('admin.reviewsPage.withComment'),
      updatedAt: t('admin.reviewsPage.updatedAt'),
      unknownUser: t('admin.reviewsPage.unknownUser'),
      unknownBook: t('admin.reviewsPage.unknownBook'),
    }),
    [t],
  )

  const bookLookup = useMemo(
    () =>
      books.reduce<Record<string, Book>>((currentLookup, book) => {
        currentLookup[book.id] = book
        return currentLookup
      }, {}),
    [books],
  )

  const sortedReviews = useMemo(
    () =>
      [...reviews].sort(
        (leftReview, rightReview) =>
          new Date(rightReview.updatedAt).getTime() -
          new Date(leftReview.updatedAt).getTime(),
      ),
    [reviews],
  )

  const filteredReviews = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return sortedReviews
    }

    return sortedReviews.filter((review) => {
      const reviewer = userLookup[review.userId]
      const book = bookLookup[review.bookId]

      return [
        reviewer?.name ?? '',
        reviewer?.email ?? '',
        book?.title ?? '',
        book?.author ?? '',
        review.comment ?? '',
      ]
        .join(' ')
        .toLowerCase()
        .includes(keyword)
    })
  }, [bookLookup, searchTerm, sortedReviews, userLookup])

  const averageRating =
    reviews.length === 0
      ? 0
      : reviews.reduce((sum, review) => sum + review.rating, 0) / reviews.length

  const commentCount = reviews.filter(
    (review) => review.comment && review.comment.trim() !== '',
  ).length

  useEffect(() => {
    let isCancelled = false

    async function loadData() {
      setIsLoading(true)

      try {
        const [reviewResponse, bookResponse, customers, employees] = await Promise.all([
          getAdminReviewsPage({ page, size: PAGE_SIZE }),
          getBookCatalog(),
          getAdminCustomers(),
          getAdminEmployees(),
        ])

        if (isCancelled) {
          return
        }

        setReviews(reviewResponse.items)
        setTotalCount(reviewResponse.totalCount)
        setBooks(bookResponse.books)
        setUserLookup(
          buildUserLookup(customers, employees, user?.id, {
            name: user?.name,
            email: user?.email,
          }),
        )
        setError(null)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(currentError, labels.loadError))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadData()

    return () => {
      isCancelled = true
    }
  }, [labels.loadError, page, user?.email, user?.id, user?.name])

  useEffect(() => {
    if (!dialogMode) {
      return
    }

    const previousOverflow = document.body.style.overflow

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape' && !isDeleting) {
        closeDialog()
      }
    }

    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [dialogMode, isDeleting])

  function handleSearchTermChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchTerm(event.currentTarget.value)
    setPage(0)
  }

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
  }

  function openViewDialog(review: AdminReviewResponse) {
    setSelectedReview(review)
    setDialogMode('view')
  }

  function openDeleteDialog(review: AdminReviewResponse) {
    setSelectedReview(review)
    setDialogMode('delete')
  }

  function closeDialog() {
    if (isDeleting) {
      return
    }

    setDialogMode(null)
    setSelectedReview(null)
  }

  async function handleDelete() {
    if (!selectedReview) {
      return
    }

    setIsDeleting(true)

    try {
      await deleteAdminReview(selectedReview.reviewId)
      setReviews((currentReviews) =>
        currentReviews.filter((review) => review.reviewId !== selectedReview.reviewId),
      )
      setTotalCount((currentCount) => Math.max(0, currentCount - 1))
      closeDialog()
      toast.success(labels.deleteSuccess)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.deleteError))
    } finally {
      setIsDeleting(false)
    }
  }

  return {
    t,
    formatDate,
    formatNumber,
    labels,
    reviews,
    page,
    pageSize: PAGE_SIZE,
    totalCount,
    searchTerm,
    isLoading,
    error,
    dialogMode,
    selectedReview,
    isDeleting,
    userLookup,
    bookLookup,
    filteredReviews,
    averageRating,
    commentCount,
    handleSearchTermChange,
    handlePageChange,
    openViewDialog,
    openDeleteDialog,
    closeDialog,
    handleDelete,
  }
}

function buildUserLookup(
  customers: AdminUserResponse[],
  employees: AdminUserResponse[],
  currentUserId?: string,
  currentUser?: Partial<UserLookup>,
) {
  const lookup: Record<string, UserLookup> = {}

  for (const account of [...customers, ...employees]) {
    lookup[account.userId] = {
      name: account.username,
      email: account.email,
    }
  }

  if (currentUserId && currentUser?.name && currentUser.email) {
    lookup[currentUserId] = {
      name: currentUser.name,
      email: currentUser.email,
    }
  }

  return lookup
}
