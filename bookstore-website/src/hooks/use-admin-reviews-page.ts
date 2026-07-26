import { useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { toast } from 'sonner'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import {
  approveAdminReview,
  deleteAdminReview,
  getAdminCustomers,
  getAdminEmployees,
  getAdminReviewsPage,
  hideAdminReview,
} from '@/services/admin-access-service'
import { getBookCatalog } from '@/services/book-service'
import type {
  AdminReviewResponse,
  AdminReviewStatus,
  AdminUserResponse,
} from '@/types/admin-access'
import type { Book } from '@/types/book'
import { getErrorMessage } from '@/utils'

type ReviewDialogMode = 'view' | 'delete' | 'hide'

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
  const [selectedStatus, setSelectedStatus] = useState<AdminReviewStatus | ''>('')
  const [selectedRating, setSelectedRating] = useState<number | ''>('')
  const [selectedBookId, setSelectedBookId] = useState('')
  const [selectedUserId, setSelectedUserId] = useState('')
  const [hideReason, setHideReason] = useState('')
  const [refreshSeed, setRefreshSeed] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [dialogMode, setDialogMode] = useState<ReviewDialogMode | null>(null)
  const [selectedReview, setSelectedReview] = useState<AdminReviewResponse | null>(
    null,
  )
  const [isMutating, setIsMutating] = useState(false)
  const [activeReviewId, setActiveReviewId] = useState<string | null>(null)

  function resolveLabel(key: string, fallback: string) {
    const translated = t(key)
    return translated === key ? fallback : translated
  }

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
      hideError: resolveLabel(
        'admin.reviewsPage.hideError',
        'Khong an duoc danh gia',
      ),
      hideSuccess: resolveLabel(
        'admin.reviewsPage.hideSuccess',
        'Da an danh gia',
      ),
      approveError: resolveLabel(
        'admin.reviewsPage.approveError',
        'Khong phe duyet duoc danh gia',
      ),
      approveSuccess: resolveLabel(
        'admin.reviewsPage.approveSuccess',
        'Da phe duyet danh gia',
      ),
      reviewer: t('admin.reviewsPage.reviewer'),
      book: t('admin.reviewsPage.book'),
      rating: t('admin.reviewsPage.rating'),
      comment: t('admin.reviewsPage.comment'),
      noComment: t('admin.reviewsPage.noComment'),
      detailTitle: t('admin.reviewsPage.detailTitle'),
      deleteTitle: resolveLabel(
        'admin.reviewsPage.deleteSoftTitle',
        'Xac nhan xoa mem danh gia',
      ),
      deleteDescription: resolveLabel(
        'admin.reviewsPage.deleteSoftDescription',
        'Danh gia nay se bi an khoi public list va duoc danh dau xoa mem trong he thong.',
      ),
      hideTitle: resolveLabel('admin.reviewsPage.hideTitle', 'An danh gia'),
      hideDescription: resolveLabel(
        'admin.reviewsPage.hideDescription',
        'Nhap ly do moderation neu can. Review hidden se bien khoi trang public.',
      ),
      cancel: t('common.cancel'),
      delete: t('common.delete'),
      view: t('common.view'),
      approve: resolveLabel('admin.reviewsPage.approve', 'Duyet'),
      hide: resolveLabel('admin.reviewsPage.hide', 'An review'),
      average: t('admin.reviewsPage.average'),
      withComment: t('admin.reviewsPage.withComment'),
      updatedAt: t('admin.reviewsPage.updatedAt'),
      unknownUser: t('admin.reviewsPage.unknownUser'),
      unknownBook: t('admin.reviewsPage.unknownBook'),
      status: resolveLabel('admin.reviewsPage.status', 'Trang thai'),
      moderationReason: resolveLabel(
        'admin.reviewsPage.moderationReason',
        'Ly do moderation',
      ),
      moderatedBy: resolveLabel('admin.reviewsPage.moderatedBy', 'Moderated by'),
      moderatedAt: resolveLabel('admin.reviewsPage.moderatedAt', 'Moderated at'),
      noReason: resolveLabel('admin.reviewsPage.noReason', 'Khong co'),
      reason: resolveLabel('admin.reviewsPage.reason', 'Ly do an review'),
      reasonPlaceholder: resolveLabel(
        'admin.reviewsPage.reasonPlaceholder',
        'Vi du: spam, cong kich, sai muc dich...',
      ),
      filterStatus: resolveLabel('admin.reviewsPage.filterStatus', 'Loc trang thai'),
      filterRating: resolveLabel('admin.reviewsPage.filterRating', 'Loc diem'),
      filterBook: resolveLabel('admin.reviewsPage.filterBook', 'Loc sach'),
      filterUser: resolveLabel('admin.reviewsPage.filterUser', 'Loc user'),
      clearFilters: resolveLabel('admin.reviewsPage.clearFilters', 'Xoa loc'),
      allStatuses: resolveLabel('admin.reviewsPage.allStatuses', 'Tat ca trang thai'),
      allRatings: resolveLabel('admin.reviewsPage.allRatings', 'Tat ca diem'),
      allBooks: resolveLabel('admin.reviewsPage.allBooks', 'Tat ca sach'),
      allUsers: resolveLabel('admin.reviewsPage.allUsers', 'Tat ca user'),
      statuses: {
        APPROVED: resolveLabel(
          'admin.reviewsPage.statusApproved',
          'Da duyet',
        ),
        HIDDEN: resolveLabel('admin.reviewsPage.statusHidden', 'Da an'),
        PENDING: resolveLabel(
          'admin.reviewsPage.statusPending',
          'Cho duyet',
        ),
      },
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

  const filteredReviews = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return reviews
    }

    return reviews.filter((review) => {
      const reviewer = userLookup[review.userId]
      const book = bookLookup[review.bookId]

      return [
        review.reviewerName ?? '',
        reviewer?.name ?? '',
        reviewer?.email ?? '',
        book?.title ?? '',
        book?.author ?? '',
        review.comment ?? '',
        review.moderationReason ?? '',
      ]
        .join(' ')
        .toLowerCase()
        .includes(keyword)
    })
  }, [bookLookup, reviews, searchTerm, userLookup])

  const bookOptions = useMemo(
    () =>
      [...books].sort((leftBook, rightBook) =>
        leftBook.title.localeCompare(rightBook.title, 'vi'),
      ),
    [books],
  )

  const userOptions = useMemo(
    () =>
      Object.entries(userLookup)
        .map(([userId, value]) => ({
          userId,
          name: value.name,
          email: value.email,
        }))
        .sort((leftUser, rightUser) =>
          leftUser.name.localeCompare(rightUser.name, 'vi'),
        ),
    [userLookup],
  )

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
          getAdminReviewsPage({
            page,
            size: PAGE_SIZE,
            status: selectedStatus || undefined,
            bookId: selectedBookId || undefined,
            userId: selectedUserId || undefined,
            rating: typeof selectedRating === 'number' ? selectedRating : undefined,
          }),
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
  }, [
    labels.loadError,
    page,
    refreshSeed,
    selectedBookId,
    selectedRating,
    selectedStatus,
    selectedUserId,
    user?.email,
    user?.id,
    user?.name,
  ])

  useEffect(() => {
    if (!dialogMode) {
      return
    }

    const previousOverflow = document.body.style.overflow

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape' && !isMutating) {
        closeDialog()
      }
    }

    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [dialogMode, isMutating])

  function requestReload() {
    setRefreshSeed((currentValue) => currentValue + 1)
  }

  function handleSearchTermChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchTerm(event.currentTarget.value)
  }

  function handleStatusChange(event: ChangeEvent<HTMLSelectElement>) {
    setSelectedStatus((event.currentTarget.value as AdminReviewStatus) || '')
    setPage(0)
  }

  function handleRatingChange(event: ChangeEvent<HTMLSelectElement>) {
    const value = event.currentTarget.value
    setSelectedRating(value === '' ? '' : Number(value))
    setPage(0)
  }

  function handleBookChange(event: ChangeEvent<HTMLSelectElement>) {
    setSelectedBookId(event.currentTarget.value)
    setPage(0)
  }

  function handleUserChange(event: ChangeEvent<HTMLSelectElement>) {
    setSelectedUserId(event.currentTarget.value)
    setPage(0)
  }

  function handleHideReasonChange(event: ChangeEvent<HTMLTextAreaElement>) {
    setHideReason(event.currentTarget.value)
  }

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
  }

  function clearFilters() {
    setSelectedStatus('')
    setSelectedRating('')
    setSelectedBookId('')
    setSelectedUserId('')
    setPage(0)
  }

  function openViewDialog(review: AdminReviewResponse) {
    setSelectedReview(review)
    setDialogMode('view')
  }

  function openHideDialog(review: AdminReviewResponse) {
    setSelectedReview(review)
    setHideReason(review.moderationReason ?? '')
    setDialogMode('hide')
  }

  function openDeleteDialog(review: AdminReviewResponse) {
    setSelectedReview(review)
    setDialogMode('delete')
  }

  function closeDialog() {
    if (isMutating) {
      return
    }

    setDialogMode(null)
    setSelectedReview(null)
    setHideReason('')
    setActiveReviewId(null)
  }

  async function handleApprove(review: AdminReviewResponse) {
    setIsMutating(true)
    setActiveReviewId(review.reviewId)

    try {
      await approveAdminReview(review.reviewId)
      toast.success(labels.approveSuccess)
      requestReload()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.approveError))
    } finally {
      setIsMutating(false)
      setActiveReviewId(null)
    }
  }

  async function handleHide() {
    if (!selectedReview) {
      return
    }

    setIsMutating(true)
    setActiveReviewId(selectedReview.reviewId)

    try {
      await hideAdminReview(selectedReview.reviewId, { reason: hideReason })
      toast.success(labels.hideSuccess)
      closeDialog()
      requestReload()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.hideError))
      setIsMutating(false)
      setActiveReviewId(null)
    }
  }

  async function handleDelete() {
    if (!selectedReview) {
      return
    }

    setIsMutating(true)
    setActiveReviewId(selectedReview.reviewId)

    try {
      await deleteAdminReview(selectedReview.reviewId)
      toast.success(labels.deleteSuccess)
      closeDialog()
      requestReload()
    } catch (currentError) {
      toast.error(getErrorMessage(currentError, labels.deleteError))
      setIsMutating(false)
      setActiveReviewId(null)
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
    selectedStatus,
    selectedRating,
    selectedBookId,
    selectedUserId,
    hideReason,
    isLoading,
    error,
    dialogMode,
    selectedReview,
    isMutating,
    activeReviewId,
    userLookup,
    bookLookup,
    filteredReviews,
    bookOptions,
    userOptions,
    averageRating,
    commentCount,
    handleSearchTermChange,
    handleStatusChange,
    handleRatingChange,
    handleBookChange,
    handleUserChange,
    handleHideReasonChange,
    handlePageChange,
    clearFilters,
    openViewDialog,
    openHideDialog,
    openDeleteDialog,
    closeDialog,
    handleApprove,
    handleHide,
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
