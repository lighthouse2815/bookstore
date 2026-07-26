export type CatalogSearchState<SortKey extends string> = {
  query: string
  category: string
  sort: SortKey
  page: number
}

export type CatalogSearchDefaults<SortKey extends string> = {
  allCategoriesValue: string
  defaultSort: SortKey
  allowedSorts: readonly SortKey[]
}

export type CatalogSearchUpdate<SortKey extends string> = Partial<
  CatalogSearchState<SortKey>
>

export function readCatalogSearchState<SortKey extends string>(
  searchParams: URLSearchParams,
  defaults: CatalogSearchDefaults<SortKey>,
): CatalogSearchState<SortKey> {
  const requestedSort = searchParams.get('sort')

  return {
    query: searchParams.get('q') ?? '',
    category:
      searchParams.get('category')?.trim() || defaults.allCategoriesValue,
    sort: defaults.allowedSorts.includes(requestedSort as SortKey)
      ? (requestedSort as SortKey)
      : defaults.defaultSort,
    page: parseCatalogPage(searchParams.get('page')),
  }
}

export function createCatalogSearchParams<SortKey extends string>(
  currentParams: URLSearchParams,
  update: CatalogSearchUpdate<SortKey>,
  defaults: CatalogSearchDefaults<SortKey>,
) {
  const state = {
    ...readCatalogSearchState(currentParams, defaults),
    ...update,
  }
  const nextParams = new URLSearchParams()

  if (state.query.trim()) {
    nextParams.set('q', state.query)
  }

  if (
    state.category &&
    state.category !== defaults.allCategoriesValue
  ) {
    nextParams.set('category', state.category)
  }

  if (state.sort !== defaults.defaultSort) {
    nextParams.set('sort', state.sort)
  }

  if (state.page > 0) {
    nextParams.set('page', String(state.page + 1))
  }

  return nextParams
}

function parseCatalogPage(value: string | null) {
  if (!value || !/^[1-9]\d*$/.test(value)) {
    return 0
  }

  return Number.parseInt(value, 10) - 1
}
