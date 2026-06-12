import {
  useEffect,
  useMemo,
  useState,
  type ChangeEvent,
} from 'react'
import { useSearchParams } from 'react-router-dom'
import { useLanguage } from '@/contexts/language-context'
import { useBookCatalog } from '@/hooks/use-book-catalog'
import { getCategoryLabel } from '@/utils/i18n'

type SortKey = 'popular' | 'price-asc' | 'price-desc' | 'rating'

const ALL_CATEGORIES = '__all__'
const CATEGORY_PRESETS = {
  '__life-skills__': 'categories.lifeSkills',
  '__novel__': 'categories.novel',
} as const

export function useBookListing() {
  const [searchParams] = useSearchParams()
  const requestedCategory = searchParams.get('category') ?? ALL_CATEGORIES
  const { t, formatNumber } = useLanguage()
  const { books, categories, isLoading, error } = useBookCatalog()
  const [category, setCategory] = useState(requestedCategory)
  const [query, setQuery] = useState('')
  const [sort, setSort] = useState<SortKey>('popular')

  useEffect(() => {
    setCategory(resolveRequestedCategory(requestedCategory, categories, t))
  }, [categories, requestedCategory, t])

  const filteredBooks = useMemo(() => {
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

  function handleQueryChange(event: ChangeEvent<HTMLInputElement>) {
    setQuery(event.currentTarget.value)
  }

  function handleCategorySelect(nextCategory: string) {
    setCategory(nextCategory)
  }

  function handleSortChange(nextSort: string) {
    setSort(nextSort as SortKey)
  }

  return {
    t,
    formatNumber,
    isLoading,
    error,
    filteredBooks,
    category,
    query,
    sort,
    allCategoriesValue: ALL_CATEGORIES,
    categoryOptions,
    handleQueryChange,
    handleCategorySelect,
    handleSortChange,
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
