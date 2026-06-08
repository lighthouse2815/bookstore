import { useEffect, useMemo, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { Search, SlidersHorizontal } from 'lucide-react'
import { BookCard } from '@/components/book/book-card'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/common/select'
import { useLanguage } from '@/contexts/language-context'
import { useBookCatalog } from '@/hooks/use-book-catalog'
import { cn } from '@/utils'
import { getCategoryLabel } from '@/utils/i18n'

type SortKey = 'popular' | 'price-asc' | 'price-desc' | 'rating'
const ALL_CATEGORIES = '__all__'
const CATEGORY_PRESETS = {
  '__life-skills__': 'categories.lifeSkills',
  '__novel__': 'categories.novel',
} as const

export function BookListing() {
  const [searchParams] = useSearchParams()
  const requestedCategory = searchParams.get('category') ?? ALL_CATEGORIES
  const { t, formatNumber } = useLanguage()
  const { books, categories, isLoading, error } = useBookCatalog()

  const [category, setCategory] = useState(requestedCategory)
  const [query, setQuery] = useState('')
  const [sort, setSort] = useState<SortKey>('popular')

  useEffect(() => {
    setCategory(
      resolveRequestedCategory(requestedCategory, categories, t),
    )
  }, [categories, requestedCategory, t])

  const filtered = useMemo(() => {
    let result = books.filter((book) => {
      const matchCategory = category === ALL_CATEGORIES || book.category === category
      const matchQuery =
        query.trim() === '' ||
        book.title.toLowerCase().includes(query.toLowerCase()) ||
        book.author.toLowerCase().includes(query.toLowerCase())

      return matchCategory && matchQuery
    })

    result = [...result].sort((firstBook, secondBook) => {
      switch (sort) {
        case 'price-asc':
          return firstBook.price - secondBook.price
        case 'price-desc':
          return secondBook.price - firstBook.price
        case 'rating':
          return (secondBook.rating ?? 0) - (firstBook.rating ?? 0)
        default:
          return (
            new Date(secondBook.updatedAt).getTime() -
            new Date(firstBook.updatedAt).getTime()
          )
      }
    })

    return result
  }, [books, category, query, sort])

  const categoryOptions = [ALL_CATEGORIES, ...categories]

  return (
    <div>
      <div className="mb-6">
        <h1 className="font-heading text-3xl font-bold tracking-tight">
          {t('book.listing.title')}
        </h1>
        <p className="mt-1 text-muted-foreground">
          {t('book.listing.resultCount', {
            count: formatNumber(filtered.length),
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
              onChange={(event) => setQuery(event.target.value)}
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
                  onClick={() => setCategory(nextCategory)}
                  className={cn(
                    'rounded-full px-4 py-2 text-sm font-medium transition-colors lg:w-full lg:text-left',
                    category === nextCategory
                      ? 'bg-primary text-primary-foreground'
                      : 'bg-muted text-foreground hover:bg-muted/70',
                  )}
                >
                  {nextCategory === ALL_CATEGORIES
                    ? t('categories.all')
                    : getCategoryLabel(nextCategory, t)}
                </button>
              ))}
            </div>
          </div>
        </aside>

        <div>
          <div className="mb-4 flex items-center justify-end">
            <Select value={sort} onValueChange={(value) => setSort(value as SortKey)}>
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
          ) : filtered.length > 0 ? (
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-4">
              {filtered.map((book) => (
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

function resolveRequestedCategory(
  requestedCategory: string,
  categories: string[],
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  if (requestedCategory === ALL_CATEGORIES) {
    return ALL_CATEGORIES
  }

  const presetKey =
    CATEGORY_PRESETS[requestedCategory as keyof typeof CATEGORY_PRESETS]

  if (presetKey) {
    return (
      categories.find(
        (category) => getCategoryLabel(category, t) === t(presetKey),
      ) ?? ALL_CATEGORIES
    )
  }

  return categories.includes(requestedCategory) ? requestedCategory : ALL_CATEGORIES
}
