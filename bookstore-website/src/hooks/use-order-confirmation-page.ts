import { useSearchParams } from 'react-router-dom'
import { useOrderResource } from '@/hooks/use-order-data'

export function useOrderConfirmationPage() {
  const [searchParams] = useSearchParams()
  const orderId = searchParams.get('orderId')
  const { order, error } = useOrderResource(orderId)

  return {
    order,
    error,
  }
}
