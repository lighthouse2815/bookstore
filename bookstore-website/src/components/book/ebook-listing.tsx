import {
  Check,
  ChevronDown,
  Search,
  SlidersHorizontal,
  Sparkles,
  X,
} from 'lucide-react'
import { type ReactNode, useMemo, useState } from 'react'
import { EbookCard } from '@/components/book/ebook-card'
import { PaginationControls } from '@/components/common/pagination-controls'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/common/select'
import { useEbookListing } from '@/hooks/use-ebook-listing'
import { cn } from '@/utils'
import { getCategoryLabel } from '@/utils/i18n'

export function EbookListing() {
  const [categoryQuery, setCategoryQuery] = useState('')
  const [mobileFiltersOpen, setMobileFiltersOpen] = useState(false)
  const {
    t,
    language,
    formatNumber,
    isLoading,
    error,
    filteredEbooks,
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
  } = useEbookListing()
  const totalCategoryCount = categoryOptions.length
  const selectedCategory = categoryOptions.find(
    (option) => option.code === category,
  )
  const selectedCategoryLabel =
    category === allCategoriesValue
      ? t('categories.all')
      : getCategoryLabel(selectedCategory, language, t('book.fallback.category'))
  const selectedSortLabel = {
    featured: t('ebookCatalog.sortFeatured'),
    format: t('ebookCatalog.sortFormat'),
    'price-asc': t('ebookCatalog.sortPriceAsc'),
    'price-desc': t('ebookCatalog.sortPriceDesc'),
  }[sort]

  const matchingCategories = useMemo(() => {
    const normalizedQuery = normalizeCategoryText(categoryQuery)

    if (!normalizedQuery) {
      return categoryOptions
    }

    const matchedCategories = categoryOptions.filter((option) =>
      normalizeCategoryText(getCategoryLabel(option, language)).includes(normalizedQuery),
    )

    if (
      category !== allCategoriesValue &&
      selectedCategory &&
      !matchedCategories.some((option) => option.code === category)
    ) {
      return [selectedCategory, ...matchedCategories]
    }

    return matchedCategories
  }, [allCategoriesValue, category, categoryOptions, categoryQuery, language, selectedCategory])

  const matchingCategoryCount = useMemo(() => {
    const normalizedQuery = normalizeCategoryText(categoryQuery)

    if (!normalizedQuery) {
      return totalCategoryCount
    }

    return categoryOptions.filter((option) =>
        normalizeCategoryText(getCategoryLabel(option, language)).includes(
          normalizedQuery,
        ),
      ).length
  }, [categoryOptions, categoryQuery, language, totalCategoryCount])

  return (
    <div>
      <div className="mb-6 overflow-hidden rounded-[2rem] border border-border/70 bg-[radial-gradient(circle_at_top_left,hsl(var(--primary)/0.16),transparent_42%),linear-gradient(135deg,hsl(var(--background)),hsl(var(--card)))] p-6 shadow-[0_24px_70px_rgba(15,23,42,0.08)]">
        <span className="inline-flex items-center gap-2 rounded-full border border-primary/15 bg-primary/8 px-3 py-1.5 text-xs font-semibold uppercase tracking-[0.18em] text-primary">
          <Sparkles className="size-3.5" />
          {t('ebookCatalog.eyebrow')}
        </span>
        <h1 className="mt-4 max-w-3xl font-heading text-3xl font-bold tracking-tight text-balance text-foreground">
          {t('ebookCatalog.title')}
        </h1>
        <p className="mt-3 max-w-3xl text-sm leading-7 text-muted-foreground">
          {t('ebookCatalog.description')}
        </p>
        <div className="mt-5 flex flex-wrap gap-2">
          {isLoading ? (
            <>
              <span className="h-8 w-32 animate-pulse rounded-full bg-muted" />
              <span className="h-8 w-28 animate-pulse rounded-full bg-muted" />
            </>
          ) : error ? null : (
            <>
              <InfoPill>
                {t('ebookCatalog.resultCount', {
                  count: formatNumber(totalCount),
                })}
              </InfoPill>
              <InfoPill>
                {t('ebookCatalog.categoryCount', {
                  count: formatNumber(totalCategoryCount),
                })}
              </InfoPill>
            </>
          )}
        </div>
      </div>

      <div className="grid gap-8 lg:grid-cols-[250px_minmax(0,1fr)]">
        <button
          type="button"
          aria-expanded={mobileFiltersOpen}
          aria-controls="ebook-category-filters"
          aria-label={
            mobileFiltersOpen
              ? t('ebookCatalog.filterClose')
              : t('ebookCatalog.filterOpen')
          }
          onClick={() => setMobileFiltersOpen((current) => !current)}
          className="flex h-12 w-full items-center justify-between rounded-2xl border border-border bg-card px-4 text-sm font-semibold text-foreground transition-colors hover:border-primary/40 hover:bg-primary/5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 lg:hidden"
        >
          <span className="flex items-center gap-2">
            <SlidersHorizontal className="size-4 text-primary" />
            {t('ebookCatalog.filterToggle')}
          </span>
          <ChevronDown
            className={cn(
              'size-4 transition-transform',
              mobileFiltersOpen && 'rotate-180',
            )}
          />
        </button>

        <aside
          id="ebook-category-filters"
          className={cn(
            'lg:sticky lg:top-24 lg:block lg:self-start',
            mobileFiltersOpen ? 'block' : 'hidden',
          )}
        >
          <div className="overflow-hidden rounded-3xl border border-border/70 bg-card/80 shadow-[0_18px_50px_rgba(15,23,42,0.08)] backdrop-blur">
            <div className="border-b border-border/60 p-4">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
                <input
                  type="text"
                  value={query}
                  onChange={handleQueryChange}
                  placeholder={t('ebookCatalog.searchPlaceholder')}
                  aria-label={t('ebookCatalog.searchAria')}
                  className="h-11 w-full rounded-2xl border border-border bg-background/80 pl-10 pr-4 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
                />
              </div>
            </div>

            <div className="p-4">
              <div className="mb-3 flex items-center justify-between gap-3">
                <h2 className="flex items-center gap-2 font-heading text-sm font-semibold">
                  <SlidersHorizontal className="size-4 text-primary" />
                  {t('ebookCatalog.categoryTitle')}
                </h2>
                {isLoading ? (
                  <span className="h-4 w-16 animate-pulse rounded-full bg-muted" />
                ) : (
                  <span className="text-xs font-medium tabular-nums text-muted-foreground">
                    {t('ebookCatalog.categoryCount', {
                      count: formatNumber(totalCategoryCount),
                    })}
                  </span>
                )}
              </div>

              <div className="rounded-2xl border border-border/60 bg-background/70 p-3">
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                      {t('ebookCatalog.selectedCategoryLabel')}
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
                      {t('ebookCatalog.clearCategory')}
                    </button>
                  ) : null}
                </div>
              </div>

              <div className="mt-3">
                <Select value={category} onValueChange={handleCategorySelect}>
                  <SelectTrigger
                    aria-label={t('ebookCatalog.categoryFilterAria')}
                    className="h-11 w-full rounded-2xl bg-background/80 px-3"
                  >
                    <SelectValue>{selectedCategoryLabel}</SelectValue>
                  </SelectTrigger>
                  <SelectContent className="max-h-80">
                    <SelectItem value={allCategoriesValue}>
                      {t('categories.all')}
                    </SelectItem>
                    {categoryOptions.map((option) => (
                        <SelectItem key={option.id} value={option.code}>
                          {getCategoryLabel(option, language)}
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
                      placeholder={t('ebookCatalog.categorySearchPlaceholder')}
                      aria-label={t('ebookCatalog.categorySearchAria')}
                      className="h-11 w-full rounded-2xl border border-border bg-background/80 pl-10 pr-4 text-sm outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
                    />
                  </div>
                  <p className="mt-2 text-xs text-muted-foreground">
                    {t('ebookCatalog.categoryShowingCount', {
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
                          key={option.id}
                          label={getCategoryLabel(option, language)}
                          isActive={category === option.code}
                          onClick={() => handleCategorySelect(option.code)}
                        />
                      ))
                    ) : (
                      <div className="rounded-2xl border border-dashed border-border px-4 py-5 text-center">
                        <p className="font-medium">
                          {t('ebookCatalog.categoryEmptyTitle')}
                        </p>
                        <p className="mt-1 text-sm text-muted-foreground">
                          {t('ebookCatalog.categoryEmptyDescription')}
                        </p>
                      </div>
                    )}
                  </>
                )}
              </div>
            </div>
          </div>
        </aside>

        <div>
          <div className="mb-4 flex items-center justify-end">
            <Select value={sort} onValueChange={handleSortChange}>
              <SelectTrigger
                aria-label={t('ebookCatalog.sortAria')}
                className="h-11 w-full rounded-full sm:w-[220px]"
              >
                <SelectValue placeholder={t('ebookCatalog.sortPlaceholder')}>
                  {selectedSortLabel}
                </SelectValue>
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="featured">
                  {t('ebookCatalog.sortFeatured')}
                </SelectItem>
                <SelectItem value="format">
                  {t('ebookCatalog.sortFormat')}
                </SelectItem>
                <SelectItem value="price-asc">
                  {t('ebookCatalog.sortPriceAsc')}
                </SelectItem>
                <SelectItem value="price-desc">
                  {t('ebookCatalog.sortPriceDesc')}
                </SelectItem>
              </SelectContent>
            </Select>
          </div>

          {isLoading ? (
            <EbookGridSkeleton label={t('common.loading')} />
          ) : error ? (
            <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border py-20 text-center">
              <p className="font-heading text-lg font-semibold">
                {t('ebookCatalog.errorTitle')}
              </p>
              <p className="mt-1 max-w-xl text-sm text-muted-foreground">
                {error || t('ebookCatalog.errorDescription')}
              </p>
            </div>
          ) : filteredEbooks.length > 0 ? (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
              {filteredEbooks.map((ebook) => (
                <EbookCard key={ebook.id} ebook={ebook} />
              ))}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border py-20 text-center">
              <p className="font-heading text-lg font-semibold">
                {t('ebookCatalog.emptyTitle')}
              </p>
              <p className="mt-1 text-sm text-muted-foreground">
                {t('ebookCatalog.emptyDescription')}
              </p>
            </div>
          )}

          {!isLoading && !error && totalCount > 0 ? (
            <div className="mt-6 overflow-hidden rounded-2xl border border-border/60 bg-card/70">
              <PaginationControls
                page={page}
                size={pageSize}
                totalCount={totalCount}
                onPageChange={handlePageChange}
              />
            </div>
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

function EbookGridSkeleton({ label }: { label: string }) {
  return (
    <div role="status" aria-label={label}>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {Array.from({ length: 6 }, (_, index) => (
          <div
            key={index}
            aria-hidden="true"
            className="flex gap-4 rounded-2xl border border-border/60 bg-card p-4"
          >
            <div className="h-36 w-24 shrink-0 animate-pulse rounded-xl bg-muted" />
            <div className="flex-1 space-y-3 py-2">
              <div className="h-4 w-3/4 animate-pulse rounded-full bg-muted" />
              <div className="h-4 w-1/2 animate-pulse rounded-full bg-muted" />
              <div className="h-4 w-2/3 animate-pulse rounded-full bg-muted" />
              <div className="h-5 w-2/5 animate-pulse rounded-full bg-muted" />
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}

function InfoPill({
  children,
}: {
  children: ReactNode
}) {
  return (
    <span className="inline-flex items-center rounded-2xl border border-border bg-background/85 px-3.5 py-2 text-xs font-semibold text-muted-foreground">
      {children}
    </span>
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
