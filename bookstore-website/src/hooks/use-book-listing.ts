import {
  useEffect,
  useMemo,
  useState,
  type ChangeEvent,
} from 'react'
import { useSearchParams } from 'react-router-dom'
import { useLanguage } from '@/contexts/language-context'
import { useBookCatalogPage } from '@/hooks/use-book-catalog'
import type { CategoryResponse } from '@/types/book'
import {
  createCatalogSearchParams,
  readCatalogSearchState,
  type CatalogSearchUpdate,
} from '@/utils/catalog-search-params'

type SortKey = 'popular' | 'price-asc' | 'price-desc' | 'rating'

const ALL_CATEGORIES = '__all__'
const PAGE_SIZE = 12
const BOOK_SORT_KEYS: readonly SortKey[] = [
  'popular',
  'price-asc',
  'price-desc',
  'rating',
]
const BOOK_SEARCH_DEFAULTS = {
  allCategoriesValue: ALL_CATEGORIES,
  defaultSort: 'popular' as const,
  allowedSorts: BOOK_SORT_KEYS,
}
const CATEGORY_PRESETS = {
  '__life-skills__': 'PERSONAL_DEVELOPMENT',
  '__novel__': 'LITERATURE',
} as const

export function useBookListing() {
  const [searchParams, setSearchParams] = useSearchParams()
  const searchState = readCatalogSearchState(
    searchParams,
    BOOK_SEARCH_DEFAULTS,
  )
  const requestedCategory = searchState.category
  const { t, language, formatNumber } = useLanguage()
  const [category, setCategory] = useState(requestedCategory)
  const [categoryIds, setCategoryIds] = useState<Record<string, string>>({})
  const { query, sort, page } = searchState
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
    if (requestedCategory === ALL_CATEGORIES) {
      setCategory(ALL_CATEGORIES)
      return
    }

    if (categories.length === 0) {
      return
    }

    setCategory(resolveRequestedCategory(requestedCategory, categories))
  }, [categories, requestedCategory])

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

  const categoryOptions = categories

  function handleQueryChange(event: ChangeEvent<HTMLInputElement>) {
    updateSearchParams(
      { query: event.currentTarget.value, page: 0 },
      true,
    )
  }

  function handleCategorySelect(nextCategory: string | null) {
    if (!nextCategory) {
      return
    }

    setCategory(nextCategory)
    updateSearchParams({ category: nextCategory, page: 0 })
  }

  function handleSortChange(nextSort: string | null) {
    if (!nextSort) {
      return
    }

    updateSearchParams({ sort: nextSort as SortKey, page: 0 })
  }

  function handlePageChange(nextPage: number) {
    updateSearchParams({ page: Math.max(0, nextPage) })
  }

  function updateSearchParams(
    update: CatalogSearchUpdate<SortKey>,
    replace = false,
  ) {
    setSearchParams(
      (currentParams) =>
        createCatalogSearchParams(
          currentParams,
          update,
          BOOK_SEARCH_DEFAULTS,
        ),
      { replace },
    )
  }

  return {
    t,
    language,
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
    handlePageChange,
  }
}

function resolveRequestedCategory(
  requestedCategory: string,
  categories: CategoryResponse[],
) {
  if (requestedCategory === ALL_CATEGORIES) {
    return ALL_CATEGORIES
  }

  const presetCode =
    CATEGORY_PRESETS[requestedCategory as keyof typeof CATEGORY_PRESETS]

  if (presetCode) {
    return categories.some((category) => category.code === presetCode)
      ? presetCode
      : ALL_CATEGORIES
  }

  return (
    categories.find(
      (category) =>
        category.code === requestedCategory ||
        category.name === requestedCategory ||
        Object.values(category.translations).some(
          (translation) => translation?.name === requestedCategory,
        ),
    )?.code ?? ALL_CATEGORIES
  )
}
