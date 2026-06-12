import { Search, SlidersHorizontal } from 'lucide-react'
import { BookCard } from '@/components/book/book-card'
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
  const {
    t,
    formatNumber,
    isLoading,
    error,
    filteredBooks,
    category,
    query,
    sort,
    allCategoriesValue,
    categoryOptions,
    handleQueryChange,
    handleCategorySelect,
    handleSortChange,
  } = useBookListing()

  return (
    <div>
      <div className="mb-6">
        <h1 className="font-heading text-3xl font-bold tracking-tight">
          {t('book.listing.title')}
        </h1>
        <p className="mt-1 text-muted-foreground">
          {t('book.listing.resultCount', {
            count: formatNumber(filteredBooks.length),
          })}
        </p>
      </div>

      <div className="grid gap-8 lg:grid-cols-[220px_1fr]">
        <aside className="space-y-6">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <input
              type="text"
              value={query}
              onChange={handleQueryChange}
              placeholder={t('book.listing.searchPlaceholder')}
              className="h-11 w-full rounded-full border border-border bg-background pl-10 pr-4 text-sm outline-none focus:border-primary"
            />
          </div>

          <div>
            <h2 className="mb-3 flex items-center gap-2 font-heading text-sm font-semibold">
              <SlidersHorizontal className="size-4" />
              {t('book.listing.categoryTitle')}
            </h2>
            <div className="flex flex-wrap gap-2 lg:flex-col lg:items-start">
              {categoryOptions.map((nextCategory) => (
                <button
                  key={nextCategory}
                  type="button"
                  onClick={() => handleCategorySelect(nextCategory)}
                  className={cn(
                    'rounded-full px-4 py-2 text-sm font-medium transition-colors lg:w-full lg:text-left',
                    category === nextCategory
                      ? 'bg-primary text-primary-foreground'
                      : 'bg-muted text-foreground hover:bg-muted/70',
                  )}
                >
                  {nextCategory === allCategoriesValue
                    ? t('categories.all')
                    : getCategoryLabel(nextCategory, t)}
                </button>
              ))}
            </div>
          </div>
        </aside>

        <div>
          <div className="mb-4 flex items-center justify-end">
            <Select value={sort} onValueChange={handleSortChange}>
              <SelectTrigger className="w-[220px] rounded-full">
                <SelectValue placeholder={t('book.listing.sortPlaceholder')} />
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
            <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border py-20 text-center">
              <p className="font-heading text-lg font-semibold">
                {t('common.loading')}
              </p>
            </div>
          ) : error ? (
            <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border py-20 text-center">
              <p className="font-heading text-lg font-semibold">
                {t('book.listing.errorTitle')}
              </p>
              <p className="mt-1 max-w-xl text-sm text-muted-foreground">
                {error || t('book.listing.errorDescription')}
              </p>
            </div>
          ) : filteredBooks.length > 0 ? (
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-4">
              {filteredBooks.map((book) => (
                <BookCard key={book.id} book={book} />
              ))}
            </div>
          ) : (
            <div className="flex flex-col items-center justify-center rounded-2xl border border-dashed border-border py-20 text-center">
              <p className="font-heading text-lg font-semibold">
                {t('book.listing.emptyTitle')}
              </p>
              <p className="mt-1 text-sm text-muted-foreground">
                {t('book.listing.emptyDescription')}
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
