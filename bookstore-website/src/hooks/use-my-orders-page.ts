import { useMyOrdersResource } from '@/hooks/use-order-data'

export function useMyOrdersPage() {
  return useMyOrdersResource()
}
