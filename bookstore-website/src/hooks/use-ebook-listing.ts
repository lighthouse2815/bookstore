import {
  useEffect,
  useMemo,
  useRef,
  useState,
  type ChangeEvent,
} from 'react'
import { useSearchParams } from 'react-router-dom'
import { useLanguage } from '@/contexts/language-context'
import { useEbookCatalogPage } from '@/hooks/use-ebook-catalog'
import { getCategoryLabel } from '@/utils/i18n'

type SortKey = 'featured' | 'price-asc' | 'price-desc' | 'format'

const ALL_CATEGORIES = '__all__'
const PAGE_SIZE = 12
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
  const [searchParams] = useSearchParams()
  const requestedCategory = searchParams.get('category') ?? ALL_CATEGORIES
  const requestedQuery = searchParams.get('q')?.trim() ?? ''
  const { t, formatNumber } = useLanguage()
  const [category, setCategory] = useState(requestedCategory)
  const [query, setQuery] = useState(requestedQuery)
  const [sort, setSort] = useState<SortKey>('featured')
  const [page, setPage] = useState(0)
  const [categoryIds, setCategoryIds] = useState<Record<string, string>>({})
  const resolvedCategoryRequest = useRef<string | null>(null)
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
