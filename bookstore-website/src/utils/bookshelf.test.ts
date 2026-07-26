import { describe, expect, it } from 'vitest'
import type { BookshelfItem } from '@/types/bookshelf'
import { getReorderedShelfItemIds } from './bookshelf'

describe('bookshelf utils', () => {
  it('moves an item up when a previous slot exists', () => {
    const items = createItems(['item-1', 'item-2', 'item-3'])

    expect(getReorderedShelfItemIds(items, 'item-2', 'UP')).toEqual([
      'item-2',
      'item-1',
      'item-3',
    ])
  })

  it('moves an item down when a next slot exists', () => {
    const items = createItems(['item-1', 'item-2', 'item-3'])

    expect(getReorderedShelfItemIds(items, 'item-2', 'DOWN')).toEqual([
      'item-1',
      'item-3',
      'item-2',
    ])
  })

  it('returns the original order when the move would exceed boundaries', () => {
    const items = createItems(['item-1', 'item-2', 'item-3'])

    expect(getReorderedShelfItemIds(items, 'item-1', 'UP')).toEqual([
      'item-1',
      'item-2',
      'item-3',
    ])
    expect(getReorderedShelfItemIds(items, 'item-3', 'DOWN')).toEqual([
      'item-1',
      'item-2',
      'item-3',
    ])
  })
})

function createItems(ids: string[]): BookshelfItem[] {
  return ids.map((id, index) => ({
    id,
    sortOrder: index,
    createdAt: '2025-01-01T00:00:00.000Z',
    updatedAt: '2025-01-01T00:00:00.000Z',
    book: {
      id: `book-${id}`,
      title: `Book ${id}`,
      author: 'Author',
      category: 'Van hoc',
      price: 100_000,
      cover: null,
      stockQuantity: 5,
    },
  }))
}
