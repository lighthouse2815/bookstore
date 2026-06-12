import { useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { useLanguage } from '@/contexts/language-context'
import { getAdminPromotions } from '@/services/admin-access-service'
import type { AdminPromotionResponse } from '@/types/admin-access'
import { getErrorMessage } from '@/utils'

export function useAdminPromotionsPage() {
  const { t, formatCurrency, formatDate, formatNumber } = useLanguage()
  const [promotions, setPromotions] = useState<AdminPromotionResponse[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let isCancelled = false

    async function loadPromotions() {
      try {
        const response = await getAdminPromotions()

        if (isCancelled) {
          return
        }

        setPromotions(response)
        setError(null)
      } catch (currentError) {
        if (!isCancelled) {
          setPromotions([])
          setError(getErrorMessage(currentError, t('admin.promotionsPage.loadError')))
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadPromotions()

    return () => {
      isCancelled = true
    }
  }, [t])

  const filteredPromotions = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return promotions
    }

    return promotions.filter((promotion) =>
      [
        promotion.name,
        promotion.code,
        promotion.description ?? '',
        promotion.discountType,
      ]
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [promotions, searchTerm])

  function handleSearchTermChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchTerm(event.currentTarget.value)
  }

  return {
    t,
    formatCurrency,
    formatDate,
    formatNumber,
    filteredPromotions,
    searchTerm,
    isLoading,
    error,
    handleSearchTermChange,
  }
}
