import { Check, ChevronDown, Search, SlidersHorizontal, X } from 'lucide-react'
import { useMemo, useState } from 'react'
import { BookCard } from '@/components/book/book-card'
import { PaginationControls } from '@/components/common/pagination-controls'
import {
  PageHeader,
  StatePanel,
  SurfaceCard,
} from '@/components/common/page-shell'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/common/select'
import { useBookListing } from '@/hooks/use-book-listing'
import { cn } from '@/utils'
import { getCategoryLabel } from '@/utils/i18n'

export function BookListing() {
  const [categoryQuery, setCategoryQuery] = useState('')
  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false)
  const {
    t,
    formatNumber,
    isLoading,
    error,
    filteredBooks,
    totalCount,
    page,
    pageSize,
    category,
    query,
    sort,
    allCategoriesValue,
    categoryOptions,
    handleQueryChange,
    handleCategorySelect,
    handleSortChange,
    handlePageChange,
  } = useBookListing()
  const totalCategoryCount = Math.max(categoryOptions.length - 1, 0)
  const selectedCategoryLabel =
    category === allCategoriesValue
      ? t('categories.all')
      : getCategoryLabel(category, t)
  const selectedSortLabel = {
    popular: t('book.listing.sortPopular'),
    rating: t('book.listing.sortRating'),
    'price-asc': t('book.listing.sortPriceAsc'),
    'price-desc': t('book.listing.sortPriceDesc'),
  }[sort]

  const matchingCategories = useMemo(() => {
    const allCategories = categoryOptions.filter(
      (option) => option !== allCategoriesValue,
    )
    const normalizedQuery = normalizeCategoryText(categoryQuery)

    if (!normalizedQuery) {
      return allCategories
    }

    const matchedCategories = allCategories.filter((option) =>
      normalizeCategoryText(getCategoryLabel(option, t)).includes(
        normalizedQuery,
      ),
    )

    if (
      category !== allCategoriesValue &&
      !matchedCategories.includes(category)
    ) {
      return [category, ...matchedCategories]
    }

    return matchedCategories
  }, [allCategoriesValue, category, categoryOptions, categoryQuery, t])

  const matchingCategoryCount = useMemo(() => {
    const normalizedQuery = normalizeCategoryText(categoryQuery)

    if (!normalizedQuery) {
      return totalCategoryCount
    }

    return categoryOptions
      .filter((option) => option !== allCategoriesValue)
      .filter((option) =>
        normalizeCategoryText(getCategoryLabel(option, t)).includes(
          normalizedQuery,
        ),
      ).length
  }, [allCategoriesValue, categoryOptions, categoryQuery, t, totalCategoryCount])

  return (
    <div>
      <PageHeader
        className="mb-6"
        title={t('book.listing.title')}
        description={
          isLoading ? (
            <span className="block h-5 w-36 animate-pulse rounded-full bg-muted" />
          ) : error ? null : (
            t('book.listing.resultCount', {
              count: formatNumber(totalCount),
            })
          )
        }
      />

      <div className="grid gap-8 lg:grid-cols-[250px_minmax(0,1fr)]">
        <button
          type="button"
          aria-expanded={mobileFiltersOpen}
          aria-controls="book-category-filters"
          aria-label={
            mobileFiltersOpen
              ? t('book.listing.filterClose')
              : t('book.listing.filterOpen')
          }
          onClick={() => setMobileFiltersOpen((current) => !current)}
          className="flex h-12 w-full items-center justify-between rounded-2xl border border-border bg-card px-4 text-sm font-semibold text-foreground transition-colors hover:border-primary/40 hover:bg-primary/5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 lg:hidden"
        >
          <span className="flex items-center gap-2">
            <SlidersHorizontal className="size-4 text-primary" />
            {t('book.listing.filterToggle')}
          </span>
          <ChevronDown
            className={cn(
              'size-4 transition-transform',
              mobileFiltersOpen && 'rotate-180',
            )}
          />
        </button>

        <aside
          id="book-category-filters"
          className={cn(
            'lg:sticky lg:top-24 lg:block lg:self-start',
            mobileFiltersOpen ? 'block' : 'hidden',
          )}
        >
          <SurfaceCard className="overflow-hidden">
            <div className="border-b border-border/60 p-4">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                <input
                  type="text"
                  value={query}
                  onChange={handleQueryChange}
                  placeholder={t('book.listing.searchPlaceholder')}
                  aria-label={t('book.listing.searchAria')}
                  className="h-11 w-full rounded-2xl border border-border bg-background/80 pl-10 pr-4 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
                />
              </div>
            </div>

            <div className="p-4">
              <div className="mb-3 flex items-center justify-between gap-3">
                <h2 className="flex items-center gap-2 font-heading text-sm font-semibold">
                  <SlidersHorizontal className="size-4 text-primary" />
                  {t('book.listing.categoryTitle')}
                </h2>
                {isLoading ? (
                  <span className="h-4 w-16 animate-pulse rounded-full bg-muted" />
                ) : (
                  <span className="text-xs font-medium tabular-nums text-muted-foreground">
                    {t('book.listing.categoryCount', {
                      count: formatNumber(totalCategoryCount),
                    })}
                  </span>
                )}
              </div>

              <div className="rounded-2xl border border-border/60 bg-background/70 p-3">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                      {t('book.listing.selectedCategoryLabel')}
                    </p>
                    <p className="mt-1 truncate font-heading text-sm font-semibold text-foreground">
                      {selectedCategoryLabel}
                    </p>
                  </div>
                  {category !== allCategoriesValue ? (
                    <button
                      type="button"
                      onClick={() => handleCategorySelect(allCategoriesValue)}
                      className="inline-flex min-h-11 shrink-0 items-center gap-1 rounded-full border border-border/80 px-3 py-2 text-xs font-semibold text-muted-foreground transition-colors hover:border-primary/40 hover:bg-primary/5 hover:text-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50"
                    >
                      <X className="size-3.5" />
                      {t('book.listing.clearCategory')}
                    </button>
                  ) : null}
                </div>
              </div>

              <div className="mt-3">
                <Select value={category} onValueChange={handleCategorySelect}>
                  <SelectTrigger
                    aria-label={t('book.listing.categoryFilterAria')}
                    className="h-11 w-full rounded-2xl bg-background/80 px-3"
                  >
                    <SelectValue>
                      {selectedCategoryLabel}
                    </SelectValue>
                  </SelectTrigger>
                  <SelectContent className="max-h-80">
                    <SelectItem value={allCategoriesValue}>
                      {t('categories.all')}
                    </SelectItem>
                    {categoryOptions
                      .filter((option) => option !== allCategoriesValue)
                      .map((option) => (
                        <SelectItem key={option} value={option}>
                          {getCategoryLabel(option, t)}
                        </SelectItem>
                      ))}
                  </SelectContent>
                </Select>
              </div>

              {totalCategoryCount > 6 ? (
                <div className="mt-3">
                  <div className="relative">
                    <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                    <input
                      type="text"
                      value={categoryQuery}
                      onChange={(event) =>
                        setCategoryQuery(event.currentTarget.value)
                      }
                      placeholder={t(
                        'book.listing.categorySearchPlaceholder',
                      )}
                      aria-label={t('book.listing.categorySearchAria')}
                      className="h-11 w-full rounded-2xl border border-border bg-background/80 pl-10 pr-4 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
                    />
                  </div>
                  <p className="mt-2 text-xs text-muted-foreground">
                    {t('book.listing.categoryShowingCount', {
                      count: formatNumber(matchingCategoryCount),
                      total: formatNumber(totalCategoryCount),
                    })}
                  </p>
                </div>
              ) : null}

              <div className="mt-3 max-h-80 space-y-2 overflow-y-auto pr-1">
                {error ? null : isLoading ? (
                  <CategoryFilterSkeleton />
                ) : (
                  <>
                    <CategoryFilterButton
                      label={t('categories.all')}
                      isActive={category === allCategoriesValue}
                      onClick={() => handleCategorySelect(allCategoriesValue)}
                    />

                    {matchingCategories.length > 0 ? (
                      matchingCategories.map((option) => (
                        <CategoryFilterButton
                          key={option}
                          label={getCategoryLabel(option, t)}
                          isActive={category === option}
                          onClick={() => handleCategorySelect(option)}
                        />
                      ))
                    ) : (
                      <StatePanel
                        minHeightClassName="min-h-[150px]"
                        title={t('book.listing.categoryEmptyTitle')}
                        description={t('book.listing.categoryEmptyDescription')}
                        className="px-4 py-5"
                      />
                    )}
                  </>
                )}
              </div>
            </div>
          </SurfaceCard>
        </aside>

        <div>
          <div className="mb-4 flex items-center justify-end">
            <Select value={sort} onValueChange={handleSortChange}>
              <SelectTrigger
                aria-label={t('book.listing.sortAria')}
                className="h-11 w-full rounded-full sm:w-[220px]"
              >
                <SelectValue placeholder={t('book.listing.sortPlaceholder')}>
                  {selectedSortLabel}
                </SelectValue>
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="popular">
                  {t('book.listing.sortPopular')}
                </SelectItem>
                <SelectItem value="rating">
                  {t('book.listing.sortRating')}
                </SelectItem>
                <SelectItem value="price-asc">
                  {t('book.listing.sortPriceAsc')}
                </SelectItem>
                <SelectItem value="price-desc">
                  {t('book.listing.sortPriceDesc')}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>

          {isLoading ? (
            <BookGridSkeleton label={t('common.loading')} />
          ) : error ? (
            <StatePanel
              tone="error"
              title={t('book.listing.errorTitle')}
              description={error || t('book.listing.errorDescription')}
            />
          ) : filteredBooks.length > 0 ? (
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-4">
              {filteredBooks.map((book) => (
                <BookCard key={book.id} book={book} />
              ))}
            </div>
          ) : (
            <StatePanel
              title={t('book.listing.emptyTitle')}
              description={t('book.listing.emptyDescription')}
            />
          )}

          {!isLoading && !error && totalCount > 0 ? (
            <SurfaceCard tone="nested" className="mt-6 overflow-hidden">
              <PaginationControls
                page={page}
                size={pageSize}
                totalCount={totalCount}
                onPageChange={handlePageChange}
              />
            </SurfaceCard>
          ) : null}
        </div>
      </div>
    </div>
  )
}

function CategoryFilterButton({
  label,
  isActive,
  onClick,
}: {
  label: string
  isActive: boolean
  onClick: () => void
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-pressed={isActive}
      className={cn(
        'flex w-full items-center justify-between rounded-2xl border px-3.5 py-3 text-left text-sm font-medium transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 active:scale-[0.99]',
        isActive
          ? 'border-primary/40 bg-primary/10 text-primary shadow-sm'
          : 'border-border/70 bg-background/70 text-foreground hover:border-primary/30 hover:bg-primary/5',
      )}
    >
      <span className="pr-3">{label}</span>
      {isActive ? <Check className="size-4 shrink-0" /> : null}
    </button>
  )
}

function CategoryFilterSkeleton() {
  return (
    <div aria-hidden="true" className="space-y-2">
      {Array.from({ length: 6 }, (_, index) => (
        <div
          key={index}
          className="h-12 animate-pulse rounded-2xl border border-border/50 bg-muted/70"
        />
      ))}
    </div>
  )
}

function BookGridSkeleton({ label }: { label: string }) {
  return (
    <div role="status" aria-label={label}>
      <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-4">
        {Array.from({ length: 8 }, (_, index) => (
          <div
            key={index}
            aria-hidden="true"
            className="overflow-hidden rounded-2xl border border-border/60 bg-card"
          >
            <div className="aspect-[3/4] animate-pulse bg-muted" />
            <div className="space-y-3 p-4">
              <div className="h-4 w-3/4 animate-pulse rounded-full bg-muted" />
              <div className="h-4 w-1/2 animate-pulse rounded-full bg-muted" />
              <div className="h-5 w-2/5 animate-pulse rounded-full bg-muted" />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

function normalizeCategoryText(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .replace(/\u0111/g, 'd')
    .replace(/\u0110/g, 'D')
    .toLowerCase()
    .trim()
}
