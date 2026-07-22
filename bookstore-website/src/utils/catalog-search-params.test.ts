import { describe, expect, it } from 'vitest'
import {
  createCatalogSearchParams,
  readCatalogSearchState,
} from '@/utils/catalog-search-params'

const defaults = {
  allCategoriesValue: '__all__',
  defaultSort: 'popular',
  allowedSorts: ['popular', 'rating', 'price-asc', 'price-desc'],
} as const

describe('catalog search params', () => {
  it('reads q, category, sort and a one-based page from the URL', () => {
    const state = readCatalogSearchState(
      new URLSearchParams(
        'q=deep+work&category=K%E1%BB%B9+n%C4%83ng+s%E1%BB%91ng&sort=rating&page=3',
      ),
      defaults,
    )

    expect(state).toEqual({
      query: 'deep work',
      category: 'Kỹ năng sống',
      sort: 'rating',
      page: 2,
    })
  })

  it('falls back safely for unsupported sort and invalid page values', () => {
    const state = readCatalogSearchState(
      new URLSearchParams('sort=unknown&page=2x'),
      defaults,
    )

    expect(state).toEqual({
      query: '',
      category: '__all__',
      sort: 'popular',
      page: 0,
    })
  })

  it('writes only the canonical catalog keys and omits default values', () => {
    const params = createCatalogSearchParams(
      new URLSearchParams('legacy=true&q=old'),
      {
        query: 'atomic habits',
        category: 'Tâm lý',
        sort: 'price-desc',
        page: 1,
      },
      defaults,
    )

    expect(params.toString()).toBe(
      'q=atomic+habits&category=T%C3%A2m+l%C3%BD&sort=price-desc&page=2',
    )
  })

  it('removes q, category, sort and page when filters return to defaults', () => {
    const params = createCatalogSearchParams(
      new URLSearchParams(
        'q=old&category=Novel&sort=rating&page=4',
      ),
      {
        query: '',
        category: '__all__',
        sort: 'popular',
        page: 0,
      },
      defaults,
    )

    expect(params.toString()).toBe('')
  })
})
