import { useParams } from 'react-router-dom'
import { useLanguage } from '@/contexts/language-context'
import { useOrderResource } from '@/hooks/use-order-data'

export function useOrderDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { t } = useLanguage()
  return useOrderResource(id, {
    missingError: t('notFound.description'),
  })
}
