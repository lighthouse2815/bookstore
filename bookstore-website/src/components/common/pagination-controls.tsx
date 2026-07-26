import { useEffect, useId, useState, type FormEvent } from 'react'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
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
  const targetPageInputId = useId()
  const totalPages = totalCount === 0 ? 0 : Math.ceil(totalCount / size)
  const currentPage =
    totalPages === 0 ? 0 : Math.min(Math.max(page + 1, 1), totalPages)
  const [targetPage, setTargetPage] = useState(
    currentPage === 0 ? '' : String(currentPage),
  )

  useEffect(() => {
    setTargetPage(currentPage === 0 ? '' : String(currentPage))
  }, [currentPage])

  const paginationItems = createPaginationItems(currentPage, totalPages)

  const goToPage = (pageNumber: number) => {
    if (
      disabled ||
      pageNumber < 1 ||
      pageNumber > totalPages ||
      pageNumber === currentPage
    ) {
      return
    }

    onPageChange(pageNumber - 1)
  }

  const handleJumpSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()

    const requestedPage = Number(targetPage)
    if (!Number.isInteger(requestedPage)) {
      return
    }

    goToPage(requestedPage)
  }

  return (
    <nav
      aria-label={t('common.pagination.navigation')}
      className="border-t border-border/60 px-4 py-5 sm:px-6"
    >
      <div className="mx-auto flex w-fit max-w-full flex-wrap items-center justify-center gap-3">
        <div className="flex max-w-full items-center justify-center gap-1.5">
          <Button
            type="button"
            variant="outline"
            size="icon"
            aria-label={t('common.pagination.previous')}
            disabled={disabled || currentPage <= 1}
            onClick={() => goToPage(currentPage - 1)}
            className="h-10 w-10 rounded-xl"
          >
            <ChevronLeft className="h-4 w-4" />
          </Button>

          {paginationItems.map((item) =>
            typeof item === 'number' ? (
              <Button
                key={item}
                type="button"
                variant={item === currentPage ? 'default' : 'outline'}
                aria-current={item === currentPage ? 'page' : undefined}
                aria-label={t('common.pagination.goToPage', {
                  page: formatNumber(item),
                })}
                disabled={disabled}
                onClick={() => goToPage(item)}
                className="h-10 min-w-10 rounded-xl px-3 font-semibold tabular-nums"
              >
                {formatNumber(item)}
              </Button>
            ) : (
              <span
                key={item}
                aria-hidden="true"
                className="flex h-10 min-w-7 items-center justify-center text-sm text-muted-foreground"
              >
                &hellip;
              </span>
            ),
          )}

          <Button
            type="button"
            variant="outline"
            size="icon"
            aria-label={t('common.pagination.next')}
            disabled={
              disabled || totalPages === 0 || currentPage >= totalPages
            }
            onClick={() => goToPage(currentPage + 1)}
            className="h-10 w-10 rounded-xl"
          >
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>

        <span
          aria-hidden="true"
          className="hidden h-8 w-px bg-border/80 sm:block"
        />

        <form
          className="flex items-center justify-center gap-2"
          onSubmit={handleJumpSubmit}
        >
          <label
            htmlFor={targetPageInputId}
            className="whitespace-nowrap text-sm text-muted-foreground"
          >
            {t('common.pagination.jumpLabel')}
          </label>
          <Input
            id={targetPageInputId}
            type="number"
            inputMode="numeric"
            min={1}
            max={Math.max(totalPages, 1)}
            required
            value={targetPage}
            disabled={disabled || totalPages === 0}
            aria-label={t('common.pagination.jumpInput')}
            onChange={(event) => setTargetPage(event.currentTarget.value)}
            className="h-10 w-20 rounded-xl text-center font-semibold tabular-nums [appearance:textfield] [&::-webkit-inner-spin-button]:appearance-none [&::-webkit-outer-spin-button]:appearance-none"
          />
          <Button
            type="submit"
            disabled={disabled || totalPages === 0}
            className="h-11 rounded-xl px-4 font-semibold"
          >
            {t('common.pagination.jumpAction')}
          </Button>
        </form>
      </div>
    </nav>
  )
}

type PaginationItem = number | 'ellipsis-start' | 'ellipsis-end'

function createPaginationItems(
  currentPage: number,
  totalPages: number,
): PaginationItem[] {
  if (totalPages <= 7) {
    return Array.from({ length: totalPages }, (_, index) => index + 1)
  }

  if (currentPage <= 4) {
    return [1, 2, 3, 4, 5, 'ellipsis-end', totalPages]
  }

  if (currentPage >= totalPages - 3) {
    return [
      1,
      'ellipsis-start',
      totalPages - 4,
      totalPages - 3,
      totalPages - 2,
      totalPages - 1,
      totalPages,
    ]
  }

  return [
    1,
    'ellipsis-start',
    currentPage - 1,
    currentPage,
    currentPage + 1,
    'ellipsis-end',
    totalPages,
  ]
}
