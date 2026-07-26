import type { PageRequest, PageResult } from '@/types/pagination'

export function toPageResult<T>(
  items: T[],
  headers: Record<string, unknown>,
  request: PageRequest,
): PageResult<T> {
  const page = parseNumber(headers['x-page'], request.page ?? 0)
  const size = parseNumber(headers['x-size'], request.size ?? Math.max(items.length, 1))
  const totalCount = parseNumber(headers['x-total-count'], items.length)
  const hasNext = parseBoolean(
    headers['x-has-next'],
    (page + 1) * size < totalCount,
  )

  return {
    items,
    totalCount,
    page,
    size,
    hasNext,
    totalPages: totalCount === 0 ? 0 : Math.ceil(totalCount / size),
  }
}

function parseNumber(value: unknown, fallback: number) {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : fallback
}

function parseBoolean(value: unknown, fallback: boolean) {
  if (typeof value === 'boolean') {
    return value
  }

  if (typeof value === 'string') {
    return value.toLowerCase() === 'true'
  }

  return fallback
}
