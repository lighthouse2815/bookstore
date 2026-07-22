import {
  useEffect,
  useMemo,
  useState,
  type ChangeEvent,
} from 'react'
import { useSearchParams } from 'react-router-dom'
import { useLanguage } from '@/contexts/language-context'
import { useEbookCatalogPage } from '@/hooks/use-ebook-catalog'
import { getCategoryLabel } from '@/utils/i18n'
import {
  createCatalogSearchParams,
  readCatalogSearchState,
  type CatalogSearchUpdate,
} from '@/utils/catalog-search-params'

type SortKey = 'featured' | 'price-asc' | 'price-desc' | 'format'

const ALL_CATEGORIES = '__all__'
const PAGE_SIZE = 12
const EBOOK_SORT_KEYS: readonly SortKey[] = [
  'featured',
  'price-asc',
  'price-desc',
  'format',
]
const EBOOK_SEARCH_DEFAULTS = {
  allCategoriesValue: ALL_CATEGORIES,
  defaultSort: 'featured' as const,
  allowedSorts: EBOOK_SORT_KEYS,
}
const CATEGORY_PRESETS = {
  '__life-skills__': 'categories.lifeSkills',
  '__novel__': 'categories.novel',
} as const
const formatOrder = {
  PDF: 0,
  EPUB: 1,
  AUDIO: 2,
} as const

export function useEbookListing() {
  const [searchParams, setSearchParams] = useSearchParams()
  const searchState = readCatalogSearchState(
    searchParams,
    EBOOK_SEARCH_DEFAULTS,
  )
  const requestedCategory = searchState.category
  const { t, formatNumber } = useLanguage()
  const [category, setCategory] = useState(requestedCategory)
  const [categoryIds, setCategoryIds] = useState<Record<string, string>>({})
  const { query, sort, page } = searchState
  const selectedCategoryId =
    category === ALL_CATEGORIES ? undefined : categoryIds[category]
  const catalog = useEbookCatalogPage({
    page,
    size: PAGE_SIZE,
    keyword: query,
    categoryId: selectedCategoryId,
  })
  const { ebooks, categories, isLoading, error, totalCount } = catalog

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

    setCategory(resolveRequestedCategory(requestedCategory, categories, t))
  }, [categories, requestedCategory, t])

  const filteredEbooks = useMemo(() => {
    if (sort === 'featured') {
      return ebooks
    }

    return [...ebooks].sort((firstItem, secondItem) => {
      switch (sort) {
        case 'price-asc':
          return firstItem.price - secondItem.price
        case 'price-desc':
          return secondItem.price - firstItem.price
        case 'format':
          return (
            formatOrder[firstItem.format] - formatOrder[secondItem.format] ||
            firstItem.bookTitle.localeCompare(secondItem.bookTitle, 'vi')
          )
        default:
          return 0
      }
    })
  }, [ebooks, sort])

  const categoryOptions = [ALL_CATEGORIES, ...categories]

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
          EBOOK_SEARCH_DEFAULTS,
        ),
      { replace },
    )
  }

  return {
    t,
    formatNumber,
    isLoading,
    error,
    filteredEbooks,
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
