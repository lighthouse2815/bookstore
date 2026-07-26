export type PageRequest = {
  page?: number
  size?: number
}

export type PageResult<T> = {
  items: T[]
  totalCount: number
  page: number
  size: number
  hasNext: boolean
  totalPages: number
}
