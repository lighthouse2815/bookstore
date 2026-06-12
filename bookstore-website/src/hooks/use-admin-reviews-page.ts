import { useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { toast } from 'sonner'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import {
  deleteAdminReview,
  getAdminCustomers,
  getAdminEmployees,
  getAdminReviews,
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

export function useAdminReviewsPage() {
  const { user } = useAuth()
  const { language, t, formatDate, formatNumber } = useLanguage()
  const isVietnamese = language === 'vi'
  const [reviews, setReviews] = useState<AdminReviewResponse[]>([])
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
      title: isVietnamese ? 'Quan ly danh gia' : 'Review management',
      description: isVietnamese
        ? 'Theo doi danh gia sach tu khach hang, xem chi tiet va go bo noi dung khong phu hop.'
        : 'Review customer book ratings, inspect details, and remove inappropriate feedback.',
      total: isVietnamese ? '{count} danh gia' : '{count} reviews',
      search: isVietnamese
        ? 'Tim theo ten sach, nguoi dung, nhan xet...'
        : 'Search by book, user, or review text...',
      empty: isVietnamese ? 'Chua co danh gia nao' : 'No reviews found',
      loadError: isVietnamese
        ? 'Khong tai duoc danh sach danh gia'
        : 'Unable to load reviews',
      deleteError: isVietnamese
        ? 'Khong xoa duoc danh gia'
        : 'Unable to delete the review',
      deleteSuccess: isVietnamese ? 'Da xoa danh gia' : 'Review deleted',
      reviewer: isVietnamese ? 'Nguoi danh gia' : 'Reviewer',
      book: isVietnamese ? 'Sach' : 'Book',
      rating: isVietnamese ? 'Diem so' : 'Rating',
      comment: isVietnamese ? 'Nhan xet' : 'Comment',
      noComment: isVietnamese ? 'Khong co nhan xet' : 'No comment',
      detailTitle: isVietnamese ? 'Chi tiet danh gia' : 'Review details',
      deleteTitle: isVietnamese ? 'Xac nhan xoa danh gia' : 'Confirm review deletion',
      deleteDescription: isVietnamese
        ? 'Danh gia nay se bi xoa khoi he thong va khong the khoi phuc.'
        : 'This review will be removed from the system and cannot be restored.',
      cancel: t('common.cancel'),
      delete: t('common.delete'),
      average: isVietnamese ? 'Diem trung binh' : 'Average rating',
      withComment: isVietnamese ? 'Co noi dung' : 'With comment',
      updatedAt: isVietnamese ? 'Cap nhat' : 'Updated',
      unknownUser: isVietnamese ? 'Nguoi dung khong xac dinh' : 'Unknown user',
      unknownBook: isVietnamese ? 'Sach khong xac dinh' : 'Unknown book',
    }),
    [isVietnamese, t],
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
          getAdminReviews(),
          getBookCatalog(),
          getAdminCustomers(),
          getAdminEmployees(),
        ])

        if (isCancelled) {
          return
        }

        setReviews(reviewResponse)
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
  }, [labels.loadError, user?.email, user?.id, user?.name])

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
