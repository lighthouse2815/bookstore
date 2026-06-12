import { useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { useLanguage } from '@/contexts/language-context'
import {
  getAdminBookStockMovements,
  getAdminStockMovements,
} from '@/services/admin-access-service'
import { getBookCatalog } from '@/services/book-service'
import type {
  AdminStockMovementResponse,
} from '@/types/admin-access'
import type { Book } from '@/types/book'
import { getErrorMessage } from '@/utils'

export function useAdminInventoryPage() {
  const { language, t, formatCurrency, formatDate, formatNumber } =
    useLanguage()
  const isVietnamese = language === 'vi'
  const [books, setBooks] = useState<Book[]>([])
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
      title: isVietnamese ? 'Quan ly ton kho' : 'Inventory management',
      description: isVietnamese
        ? 'Theo doi ton hien tai va lich su bien dong kho cua tung dau sach.'
        : 'Track current stock and movement history for each book title.',
      totalBooks: isVietnamese ? '{count} dau sach' : '{count} books',
      search: isVietnamese
        ? 'Tim theo ten sach, tac gia, the loai...'
        : 'Search by title, author, or category...',
      empty: isVietnamese ? 'Khong co sach trong kho' : 'No inventory found',
      lowStock: isVietnamese ? 'Sap het' : 'Low stock',
      inStock: isVietnamese ? 'Con hang' : 'In stock',
      outOfStock: isVietnamese ? 'Het hang' : 'Out of stock',
      recentMovements: isVietnamese ? 'Bien dong gan day' : 'Recent movements',
      movementHistory: isVietnamese ? 'Lich su bien dong' : 'Movement history',
      latestMovement: isVietnamese ? 'Bien dong moi nhat' : 'Latest movement',
      book: isVietnamese ? 'Sach' : 'Book',
      price: isVietnamese ? 'Gia ban' : 'Price',
      stock: isVietnamese ? 'Ton kho' : 'Stock',
      loadError: isVietnamese
        ? 'Khong tai duoc du lieu ton kho'
        : 'Unable to load inventory',
      historyError: isVietnamese
        ? 'Khong tai duoc lich su ton kho'
        : 'Unable to load stock history',
      noMovement: isVietnamese ? 'Chua co bien dong kho' : 'No stock movement yet',
      reference: isVietnamese ? 'Lien ket nghiep vu' : 'Reference',
      beforeAfter: isVietnamese ? 'Truoc / Sau' : 'Before / After',
      quantity: isVietnamese ? 'So luong' : 'Quantity',
      unknownReference: isVietnamese ? 'Khong co tham chieu' : 'No reference',
    }),
    [isVietnamese],
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
          getBookCatalog(),
          getAdminStockMovements(),
        ])

        if (isCancelled) {
          return
        }

        setBooks(bookResponse.books)
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
  }, [labels.loadError])

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
    isVietnamese,
    books,
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
