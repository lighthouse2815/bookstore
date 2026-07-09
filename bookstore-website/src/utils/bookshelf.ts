import type { BookshelfItem } from '@/types/bookshelf'

export type ShelfMoveDirection = 'UP' | 'DOWN'

export function getReorderedShelfItemIds(
  items: BookshelfItem[],
  itemId: string,
  direction: ShelfMoveDirection,
) {
  const currentIndex = items.findIndex((item) => item.id === itemId)
  if (currentIndex < 0) {
    return items.map((item) => item.id)
  }

  const targetIndex =
    direction === 'UP' ? currentIndex - 1 : currentIndex + 1

  if (targetIndex < 0 || targetIndex >= items.length) {
    return items.map((item) => item.id)
  }

  const nextItems = [...items]
  const [movedItem] = nextItems.splice(currentIndex, 1)

  nextItems.splice(targetIndex, 0, movedItem)

  return nextItems.map((item) => item.id)
}
