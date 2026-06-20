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
  const [error, setError] = useState<string | null>(null)
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
        const response = await getMyDigitalLibrary()

        if (isCancelled) {
          return
        }

        setItems(response)
        setError(null)
      } catch (currentError) {
        if (!isCancelled) {
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
    error,
    searchTerm,
    selectedFormat,
    selectedStatus,
    setSearchTerm,
    setSelectedFormat,
    setSelectedStatus,
  }
}
