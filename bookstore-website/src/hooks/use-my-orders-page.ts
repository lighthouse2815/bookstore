import { useMyOrdersResource } from '@/hooks/use-order-data'
import { useState } from 'react'

const PAGE_SIZE = 10

export function useMyOrdersPage() {
  const [page, setPage] = useState(0)
  const resource = useMyOrdersResource(page, PAGE_SIZE)

  return {
    ...resource,
    page,
    pageSize: PAGE_SIZE,
    handlePageChange: setPage,
  }
}
