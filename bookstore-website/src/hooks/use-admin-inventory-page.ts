import { useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { useLanguage } from '@/contexts/language-context'
import {
  getAdminBookStockMovements,
  getAdminStockMovements,
} from '@/services/admin-access-service'
import { getBookCatalogPage } from '@/services/book-service'
import type { AdminStockMovementResponse } from '@/types/admin-access'
import type { Book } from '@/types/book'
import { getErrorMessage } from '@/utils'

const PAGE_SIZE = 10

export function useAdminInventoryPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const [books, setBooks] = useState<Book[]>([])
  const [page, setPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
  const [movements, setMovements] = useState<AdminStockMovementResponse[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedBook, setSelectedBook] = useState<Book | null>(null)
  const [bookMovements, setBookMovements] = useState<AdminStockMovementResponse[]>(
    [],
  )
  const [isHistoryLoading, setIsHistoryLoading] = useState(false)
  const [historyError, setHistoryError] = useState<string | null>(null)

  const labels = useMemo(
    () => ({
      title: t('admin.inventoryPage.title'),
      description: t('admin.inventoryPage.description'),
      totalBooks: t('admin.inventoryPage.totalBooks'),
      search: t('admin.inventoryPage.search'),
      empty: t('admin.inventoryPage.empty'),
      lowStock: t('admin.inventoryPage.lowStock'),
      inStock: t('admin.inventoryPage.inStock'),
      outOfStock: t('admin.inventoryPage.outOfStock'),
      recentMovements: t('admin.inventoryPage.recentMovements'),
      movementHistory: t('admin.inventoryPage.movementHistory'),
      latestMovement: t('admin.inventoryPage.latestMovement'),
      book: t('admin.inventoryPage.book'),
      price: t('admin.inventoryPage.price'),
      stock: t('admin.inventoryPage.stock'),
      loadError: t('admin.inventoryPage.loadError'),
      historyError: t('admin.inventoryPage.historyError'),
      noMovement: t('admin.inventoryPage.noMovement'),
      reference: t('admin.inventoryPage.reference'),
      beforeAfter: t('admin.inventoryPage.beforeAfter'),
      quantity: t('admin.inventoryPage.quantity'),
      unknownReference: t('admin.inventoryPage.unknownReference'),
    }),
    [t],
  )

  const movementLookup = useMemo(() => {
    const grouped = new Map<string, AdminStockMovementResponse[]>()

    for (const movement of movements) {
      const currentList = grouped.get(movement.bookId) ?? []
      currentList.push(movement)
      grouped.set(movement.bookId, currentList)
    }

    for (const [bookId, currentList] of grouped.entries()) {
      grouped.set(bookId, sortMovementsByCreatedAtDesc(currentList))
    }

    return grouped
  }, [movements])

  const filteredBooks = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return books
    }

    return books.filter((book) =>
      [book.title, book.author, book.category, book.publisher]
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [books, searchTerm])

  const lowStockCount = books.filter(
    (book) => book.stockQuantity > 0 && book.stockQuantity <= 5,
  ).length
  const outOfStockCount = books.filter((book) => book.stockQuantity === 0).length
  const recentMovementCount = movements.filter((movement) => {
    const movementTime = new Date(movement.createdAt).getTime()
    return movementTime >= Date.now() - 7 * 24 * 60 * 60 * 1000
  }).length

  useEffect(() => {
    let isCancelled = false

    async function loadInventory() {
      setIsLoading(true)

      try {
        const [bookResponse, movementResponse] = await Promise.all([
          getBookCatalogPage({ page, size: PAGE_SIZE }),
          getAdminStockMovements(),
        ])

        if (isCancelled) {
          return
        }

        setBooks(bookResponse.books)
        setTotalCount(bookResponse.totalCount)
        setMovements(movementResponse)
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

    void loadInventory()

    return () => {
      isCancelled = true
    }
  }, [labels.loadError, page])

  useEffect(() => {
    if (!selectedBook) {
      return
    }

    const previousOverflow = document.body.style.overflow

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape' && !isHistoryLoading) {
        closeHistory()
      }
    }

    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [isHistoryLoading, selectedBook])

  function handleSearchTermChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchTerm(event.currentTarget.value)
    setPage(0)
  }

  function handlePageChange(nextPage: number) {
    setPage(nextPage)
  }

  async function openHistory(book: Book) {
    setSelectedBook(book)
    setIsHistoryLoading(true)
    setHistoryError(null)

    try {
      const response = await getAdminBookStockMovements(book.id)
      setBookMovements(sortMovementsByCreatedAtDesc(response))
    } catch (currentError) {
      setHistoryError(getErrorMessage(currentError, labels.historyError))
      setBookMovements([])
    } finally {
      setIsHistoryLoading(false)
    }
  }

  function closeHistory() {
    if (isHistoryLoading) {
      return
    }

    setSelectedBook(null)
    setBookMovements([])
    setHistoryError(null)
  }

  return {
    t,
    formatCurrency,
    formatDate,
    formatNumber,
    labels,
    books,
    page,
    pageSize: PAGE_SIZE,
    totalCount,
    movements,
    searchTerm,
    isLoading,
    error,
    selectedBook,
    bookMovements,
    isHistoryLoading,
    historyError,
    movementLookup,
    filteredBooks,
    lowStockCount,
    outOfStockCount,
    recentMovementCount,
    handleSearchTermChange,
    handlePageChange,
    openHistory,
    closeHistory,
  }
}

function sortMovementsByCreatedAtDesc(
  movements: AdminStockMovementResponse[],
) {
  return [...movements].sort(
    (leftMovement, rightMovement) =>
      new Date(rightMovement.createdAt).getTime() -
      new Date(leftMovement.createdAt).getTime(),
  )
}
