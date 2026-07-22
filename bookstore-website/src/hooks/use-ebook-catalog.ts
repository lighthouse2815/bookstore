import { useEffect, useState } from 'react'
import { getBookReferences } from '@/services/book-service'
import { getPublishedDigitalAssetCatalog } from '@/services/digital-library-service'
import type { BookReferenceData, CategoryResponse } from '@/types/book'
import type { PublishedDigitalAssetCatalogItem } from '@/types/digital-library'
import type { PageRequest } from '@/types/pagination'
import { getErrorMessage } from '@/utils'

type UseEbookCatalogResult = {
  ebooks: PublishedDigitalAssetCatalogItem[]
  categories: string[]
  categoryIds: Record<string, string>
  isLoading: boolean
  error: string | null
}

type UseEbookCatalogPageOptions = PageRequest & {
  keyword?: string
  categoryId?: string
}

type UseEbookCatalogPageResult = UseEbookCatalogResult & {
  totalCount: number
  page: number
  size: number
}

const EMPTY_REFERENCES: BookReferenceData = {
  categories: [],
  authors: [],
  publishers: [],
}

const initialState: UseEbookCatalogPageResult = {
  ebooks: [],
  categories: [],
  categoryIds: {},
  isLoading: true,
  error: null,
  totalCount: 0,
  page: 0,
  size: 12,
}

export function useEbookCatalogPage({
  page = 0,
  size = 12,
  keyword,
  categoryId,
}: UseEbookCatalogPageOptions): UseEbookCatalogPageResult {
  const [state, setState] = useState<UseEbookCatalogPageResult>({
    ...initialState,
    page,
    size,
  })

  useEffect(() => {
    let isCancelled = false
    setState((currentState) => ({
      ...currentState,
      isLoading: true,
      error: null,
    }))

    async function loadEbookCatalogPage() {
      try {
        const [catalogPage, references] = await Promise.all([
          getPublishedDigitalAssetCatalog({
            page,
            size,
            keyword,
            categoryId,
          }),
          getBookReferences().catch(() => EMPTY_REFERENCES),
        ])

        if (isCancelled) {
          return
        }

        const authorMap = new Map(
          references.authors.map((author) => [author.id, author.name]),
        )
        const categoryMap = new Map(
          references.categories.map((category) => [category.id, category.name]),
        )
        const publisherMap = new Map(
          references.publishers.map((publisher) => [publisher.id, publisher.name]),
        )

        setState({
          ebooks: catalogPage.items.map((item) => ({
            ...item,
            categoryName: categoryMap.get(item.categoryId) ?? '',
            authorName: authorMap.get(item.authorId) ?? '',
            publisherName: publisherMap.get(item.publisherId) ?? '',
          })),
          categories: getCategoryNames(references.categories),
          categoryIds: getCategoryIds(references.categories),
          totalCount: catalogPage.totalCount,
          page: catalogPage.page,
          size: catalogPage.size,
          isLoading: false,
          error: null,
        })
      } catch (error) {
        if (isCancelled) {
          return
        }

        setState((currentState) => ({
          ...currentState,
          ebooks: [],
          totalCount: 0,
          isLoading: false,
          error: getErrorMessage(error),
        }))
      }
    }

    void loadEbookCatalogPage()

    return () => {
      isCancelled = true
    }
  }, [categoryId, keyword, page, size])

  return state
}

function getCategoryNames(categories: CategoryResponse[]) {
  return [...new Set(categories.map((category) => category.name))].sort(
    (firstCategory, secondCategory) =>
      firstCategory.localeCompare(secondCategory, 'vi'),
  )
}

function getCategoryIds(categories: CategoryResponse[]) {
  return Object.fromEntries(
    categories.map((category) => [category.name, category.id]),
  )
}
