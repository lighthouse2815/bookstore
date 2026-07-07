import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from '@/components/common/button'
import { useLanguage } from '@/contexts/language-context'

type PaginationControlsProps = {
  page: number
  size: number
  totalCount: number
  disabled?: boolean
  onPageChange: (page: number) => void
}

export function PaginationControls({
  page,
  size,
  totalCount,
  disabled = false,
  onPageChange,
}: PaginationControlsProps) {
  const { t, formatNumber } = useLanguage()
  const totalPages = totalCount === 0 ? 0 : Math.ceil(totalCount / size)
  const displayPage = totalPages === 0 ? 0 : page + 1

  return (
    <div className="flex flex-wrap items-center justify-between gap-4 border-t border-border/60 px-6 py-4">
      <p className="text-sm text-muted-foreground">
        {t('common.pagination.total', { count: formatNumber(totalCount) })}
      </p>
      <div className="flex items-center gap-3">
        <Button
          type="button"
          variant="outline"
          size="icon"
          aria-label={t('common.pagination.previous')}
          disabled={disabled || page <= 0}
          onClick={() => onPageChange(page - 1)}
          className="rounded-xl"
        >
          <ChevronLeft className="h-4 w-4" />
        </Button>
        <span className="min-w-24 text-center text-sm font-semibold text-foreground">
          {t('common.pagination.page', {
            page: formatNumber(displayPage),
            total: formatNumber(totalPages),
          })}
        </span>
        <Button
          type="button"
          variant="outline"
          size="icon"
          aria-label={t('common.pagination.next')}
          disabled={disabled || totalPages === 0 || page + 1 >= totalPages}
          onClick={() => onPageChange(page + 1)}
          className="rounded-xl"
        >
          <ChevronRight className="h-4 w-4" />
        </Button>
      </div>
    </div>
  )
}
