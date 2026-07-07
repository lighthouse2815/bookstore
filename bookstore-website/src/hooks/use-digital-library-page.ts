import { useEffect, useMemo, useState } from 'react'
import { getMyDigitalLibrary } from '@/services/digital-library-service'
import type {
  DigitalAccessStatus,
  DigitalAssetFormat,
  DigitalLibraryItemResponse,
} from '@/types/digital-library'
import { getErrorMessage } from '@/utils'

export function useDigitalLibraryPage() {
  const [items, setItems] = useState<DigitalLibraryItemResponse[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isLoadingMore, setIsLoadingMore] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [page, setPage] = useState(0)
  const [totalCount, setTotalCount] = useState(0)
  const [hasNext, setHasNext] = useState(false)
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedFormat, setSelectedFormat] = useState<DigitalAssetFormat | 'all'>(
    'all',
  )
  const [selectedStatus, setSelectedStatus] = useState<
    DigitalAccessStatus | 'all'
  >('all')

  useEffect(() => {
    let isCancelled = false

    async function loadLibrary() {
      setIsLoading(true)

      try {
        const response = await getMyDigitalLibrary({ page: 0 })

        if (isCancelled) {
          return
        }

        setItems(response.items)
        setPage(response.page)
        setTotalCount(response.totalCount)
        setHasNext(response.hasNext)
        setError(null)
      } catch (currentError) {
        if (!isCancelled) {
          setItems([])
          setPage(0)
          setTotalCount(0)
          setHasNext(false)
          setError(getErrorMessage(currentError))
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadLibrary()

    return () => {
      isCancelled = true
    }
  }, [])

  async function loadMore() {
    if (isLoading || isLoadingMore || !hasNext) {
      return
    }

    setIsLoadingMore(true)

    try {
      const response = await getMyDigitalLibrary({ page: page + 1 })
      setItems((currentItems) => [...currentItems, ...response.items])
      setPage(response.page)
      setTotalCount(response.totalCount)
      setHasNext(response.hasNext)
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError))
    } finally {
      setIsLoadingMore(false)
    }
  }

  const filteredItems = useMemo(() => {
    const normalizedSearch = searchTerm.trim().toLowerCase()

    return items.filter((item) => {
      const matchesSearch =
        normalizedSearch === '' ||
        [item.bookTitle, item.assetTitle, item.format, item.accessType]
          .join(' ')
          .toLowerCase()
          .includes(normalizedSearch)

      const matchesFormat =
        selectedFormat === 'all' || item.format === selectedFormat

      const matchesStatus =
        selectedStatus === 'all' || item.accessStatus === selectedStatus

      return matchesSearch && matchesFormat && matchesStatus
    })
  }, [items, searchTerm, selectedFormat, selectedStatus])

  return {
    items,
    filteredItems,
    isLoading,
    isLoadingMore,
    error,
    totalCount,
    hasNext,
    searchTerm,
    selectedFormat,
    selectedStatus,
    setSearchTerm,
    setSelectedFormat,
    setSelectedStatus,
    loadMore,
  }
}
