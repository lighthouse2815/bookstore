import {
  useEffect,
  useMemo,
  useRef,
  useState,
  type ChangeEvent,
} from 'react'
import { useSearchParams } from 'react-router-dom'
import { useLanguage } from '@/contexts/language-context'
import { useBookCatalogPage } from '@/hooks/use-book-catalog'
import { getCategoryLabel } from '@/utils/i18n'

type SortKey = 'popular' | 'price-asc' | 'price-desc' | 'rating'

const ALL_CATEGORIES = '__all__'
const PAGE_SIZE = 12
const CATEGORY_PRESETS = {
  '__life-skills__': 'categories.lifeSkills',
  '__novel__': 'categories.novel',
} as const

export function useBookListing() {
  const [searchParams] = useSearchParams()
  const requestedCategory = searchParams.get('category') ?? ALL_CATEGORIES
  const requestedQuery = searchParams.get('q')?.trim() ?? ''
  const { t, formatNumber } = useLanguage()
  const [category, setCategory] = useState(requestedCategory)
  const [query, setQuery] = useState(requestedQuery)
  const [sort, setSort] = useState<SortKey>('popular')
  const [page, setPage] = useState(0)
  const [categoryIds, setCategoryIds] = useState<Record<string, string>>({})
  const resolvedCategoryRequest = useRef<string | null>(null)
  const selectedCategoryId =
    category === ALL_CATEGORIES ? undefined : categoryIds[category]
  const catalog = useBookCatalogPage({
    page,
    size: PAGE_SIZE,
    keyword: query,
    categoryId: selectedCategoryId,
  })
  const { books, categories, isLoading, error, totalCount } = catalog

  useEffect(() => {
    setCategoryIds(catalog.categoryIds)
  }, [catalog.categoryIds])

  useEffect(() => {
    if (
      categories.length === 0 ||
      resolvedCategoryRequest.current === requestedCategory
    ) {
      return
    }

    setCategory(resolveRequestedCategory(requestedCategory, categories, t))
    setPage(0)
    resolvedCategoryRequest.current = requestedCategory
  }, [categories, requestedCategory, t])

  useEffect(() => {
    setQuery(requestedQuery)
  }, [requestedQuery])

  const filteredBooks = useMemo(() => {
    return [...books].sort((firstBook, secondBook) => {
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

  }, [books, sort])

  const categoryOptions = [ALL_CATEGORIES, ...categories]

  function handleQueryChange(event: ChangeEvent<HTMLInputElement>) {
    setQuery(event.currentTarget.value)
    setPage(0)
  }

  function handleCategorySelect(nextCategory: string | null) {
    if (!nextCategory) {
      return
    }

    setCategory(nextCategory)
    setPage(0)
  }

  function handleSortChange(nextSort: string | null) {
    if (!nextSort) {
      return
    }

    setSort(nextSort as SortKey)
  }

  return {
    t,
    formatNumber,
    isLoading,
    error,
    filteredBooks,
    totalCount,
    page,
    pageSize: PAGE_SIZE,
    category,
    query,
    sort,
    allCategoriesValue: ALL_CATEGORIES,
    categoryOptions,
    handleQueryChange,
    handleCategorySelect,
    handleSortChange,
    handlePageChange: setPage,
  }
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
