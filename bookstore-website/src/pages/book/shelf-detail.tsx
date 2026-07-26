import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import {
  ArrowDown,
  ArrowLeft,
  ArrowUp,
  ExternalLink,
  LibraryBig,
  PencilLine,
  Trash2,
  X,
} from 'lucide-react'
import { toast } from 'sonner'
import { Button, buttonVariants } from '@/components/common/button'
import { Input } from '@/components/common/input'
import {
  StatePanel,
  SurfaceCard,
  primaryButtonClassName,
} from '@/components/common/page-shell'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { getCategoryLabel } from '@/utils/i18n'
import {
  addBookToShelf,
  deleteBookshelf,
  getMyBookshelf,
  removeBookFromShelf,
  reorderShelfItems,
  updateBookshelf,
} from '@/services/bookshelf-service'
import type { Bookshelf, BookshelfItem } from '@/types/bookshelf'
import { cn, getErrorMessage } from '@/utils'
import { getBookCoverUrl, setBookCoverFallback } from '@/utils/book-cover'
import {
  getReorderedShelfItemIds,
  type ShelfMoveDirection,
} from '@/utils/bookshelf'

export default function ShelfDetailPage() {
  const { shelfId } = useParams<{ shelfId: string }>()
  const { t, formatCurrency, formatDate } = useLanguage()
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const addBookId = searchParams.get('addBook')
  const [bookshelf, setBookshelf] = useState<Bookshelf | null>(null)
  const [renameValue, setRenameValue] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    if (!shelfId) {
      setError(t('shelves.loadError'))
      setIsLoading(false)
      return
    }
    const resolvedShelfId = shelfId

    let isCancelled = false

    async function loadBookshelf() {
      setIsLoading(true)
      setError(null)

      try {
        const nextBookshelf = await getMyBookshelf(resolvedShelfId)
        if (isCancelled) {
          return
        }

        setBookshelf(nextBookshelf)
        setRenameValue(nextBookshelf.name)
      } catch (nextError) {
        if (isCancelled) {
          return
        }

        setError(getErrorMessage(nextError, t('shelves.loadError')))
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadBookshelf()

    return () => {
      isCancelled = true
    }
  }, [shelfId, t])

  useEffect(() => {
    if (!shelfId || !addBookId) {
      return
    }
    const resolvedShelfId = shelfId
    const resolvedBookId = addBookId

    let isCancelled = false

    async function attachSelectedBook() {
      setIsSaving(true)

      try {
        const updatedShelf = await addBookToShelf(resolvedShelfId, resolvedBookId)
        if (isCancelled) {
          return
        }

        setBookshelf(updatedShelf)
        setRenameValue(updatedShelf.name)
        toast.success(
          t('shelves.addedToShelfSuccess', { name: updatedShelf.name }),
        )

        const nextParams = new URLSearchParams(searchParams)
        nextParams.delete('addBook')
        setSearchParams(nextParams, { replace: true })
      } catch (nextError) {
        if (isCancelled) {
          return
        }

        toast.error(getErrorMessage(nextError, t('shelves.addToShelfError')))
      } finally {
        if (!isCancelled) {
          setIsSaving(false)
        }
      }
    }

    void attachSelectedBook()

    return () => {
      isCancelled = true
    }
  }, [addBookId, searchParams, setSearchParams, shelfId, t])

  async function handleRename(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!shelfId || !bookshelf) {
      return
    }

    const nextName = renameValue.trim()
    if (!nextName) {
      return
    }

    try {
      setIsSaving(true)
      const updatedShelf = await updateBookshelf(shelfId, nextName)
      setBookshelf(updatedShelf)
      setRenameValue(updatedShelf.name)
      toast.success(t('shelves.renameSuccess', { name: updatedShelf.name }))
    } catch (nextError) {
      toast.error(getErrorMessage(nextError, t('shelves.renameError')))
    } finally {
      setIsSaving(false)
    }
  }

  async function handleDeleteShelf() {
    if (!shelfId || !bookshelf) {
      return
    }

    if (!window.confirm(t('shelves.deleteConfirm', { name: bookshelf.name }))) {
      return
    }

    try {
      setIsSaving(true)
      await deleteBookshelf(shelfId)
      toast.success(t('shelves.deleteSuccess', { name: bookshelf.name }))
      navigate('/shelves')
    } catch (nextError) {
      toast.error(getErrorMessage(nextError, t('shelves.deleteError')))
    } finally {
      setIsSaving(false)
    }
  }

  async function handleRemoveBook(bookId: string) {
    if (!shelfId) {
      return
    }

    try {
      setIsSaving(true)
      const updatedShelf = await removeBookFromShelf(shelfId, bookId)
      setBookshelf(updatedShelf)
      toast.success(t('shelves.removeBookSuccess'))
    } catch (nextError) {
      toast.error(getErrorMessage(nextError, t('shelves.removeBookError')))
    } finally {
      setIsSaving(false)
    }
  }

  async function handleMoveItem(itemId: string, direction: ShelfMoveDirection) {
    if (!shelfId || !bookshelf) {
      return
    }

    const nextItemIds = getReorderedShelfItemIds(
      bookshelf.items,
      itemId,
      direction,
    )
    const currentItemIds = bookshelf.items.map((item) => item.id)
    if (nextItemIds.join(',') === currentItemIds.join(',')) {
      return
    }

    try {
      setIsSaving(true)
      const updatedShelf = await reorderShelfItems(shelfId, nextItemIds)
      setBookshelf(updatedShelf)
    } catch (nextError) {
      toast.error(getErrorMessage(nextError, t('shelves.reorderError')))
    } finally {
      setIsSaving(false)
    }
  }

  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(249,245,255,1)_0%,rgba(255,255,255,1)_46%,rgba(246,249,255,1)_100%)] dark:bg-[linear-gradient(180deg,rgba(18,15,29,1)_0%,rgba(23,20,34,1)_46%,rgba(18,15,29,1)_100%)]">
      <Header />
      <main className="flex-1 pb-16 pt-8">
        <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-3">
            <Link
              to="/shelves"
              className={cn(
                buttonVariants({ variant: 'outline' }),
                'rounded-2xl',
              )}
            >
              <ArrowLeft className="mr-2 size-4" />
              {t('shelves.backToShelves')}
            </Link>
            <Link
              to="/books"
              className={cn(
                buttonVariants({ variant: 'outline' }),
                'rounded-2xl',
              )}
            >
              {t('shelves.browseMoreBooks')}
            </Link>
          </div>

          {isLoading ? (
            <StatePanel title={t('common.loading')} />
          ) : error || !bookshelf ? (
            <StatePanel
              tone="error"
              icon={<LibraryBig className="size-12" />}
              title={t('shelves.loadError')}
              description={error ?? t('shelves.loadError')}
            />
          ) : (
            <>
              <SurfaceCard className="p-6">
                <div className="flex flex-col gap-6 xl:flex-row xl:items-end xl:justify-between">
                  <div className="space-y-4">
                    <span className="inline-flex items-center gap-2 rounded-full bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary dark:bg-primary/18">
                      <LibraryBig className="size-4" />
                      {t('shelves.detailBadge')}
                    </span>
                    <div>
                      <h1 className="font-heading text-3xl font-bold tracking-tight text-slate-950 dark:text-foreground">
                        {bookshelf.name}
                      </h1>
                      <p className="mt-2 text-sm text-slate-600 dark:text-muted-foreground">
                        {t('shelves.detailMeta', {
                          count: bookshelf.bookCount,
                          date: formatDate(bookshelf.updatedAt),
                        })}
                      </p>
                    </div>
                  </div>

                  <Button
                    type="button"
                    variant="outline"
                    className="rounded-2xl border-rose-200 text-rose-500 hover:bg-rose-50 hover:text-rose-600 dark:border-rose-400/25 dark:text-rose-300 dark:hover:bg-rose-400/10"
                    disabled={isSaving}
                    onClick={() => {
                      void handleDeleteShelf()
                    }}
                  >
                    <Trash2 className="mr-2 size-4" />
                    {t('common.delete')}
                  </Button>
                </div>

                <form
                  className="mt-6 flex flex-col gap-3 md:flex-row"
                  onSubmit={(event) => {
                    void handleRename(event)
                  }}
                >
                  <Input
                    value={renameValue}
                    onChange={(event) => setRenameValue(event.currentTarget.value)}
                    disabled={isSaving}
                    className="h-12 rounded-2xl"
                  />
                  <Button
                    type="submit"
                    disabled={isSaving || !renameValue.trim()}
                    className={`${primaryButtonClassName} h-12`}
                  >
                    <PencilLine className="mr-2 size-4" />
                    {t('shelves.renameAction')}
                  </Button>
                </form>
              </SurfaceCard>

              {bookshelf.items.length === 0 ? (
                <StatePanel
                  icon={<LibraryBig className="size-12 text-primary" />}
                  title={t('shelves.emptyShelfTitle')}
                  description={t('shelves.emptyShelfDescription')}
                />
              ) : (
                <section className="grid gap-5 lg:grid-cols-2">
                  {bookshelf.items.map((item, index) => (
                    <ShelfBookCard
                      key={item.id}
                      item={item}
                      isSaving={isSaving}
                      canMoveUp={index > 0}
                      canMoveDown={index < bookshelf.items.length - 1}
                      formatCurrency={formatCurrency}
                      formatDate={formatDate}
                      labels={{
                        order: t('shelves.positionLabel', { position: index + 1 }),
                        inStock: t('shelves.inStock', {
                          count: item.book.stockQuantity,
                        }),
                        outOfStock: t('shelves.outOfStock'),
                        openBook: t('shelves.openBookDetail'),
                        removeBook: t('shelves.removeBookAction'),
                      }}
                      onMove={(direction) => {
                        void handleMoveItem(item.id, direction)
                      }}
                      onRemove={() => {
                        void handleRemoveBook(item.book.id)
                      }}
                    />
                  ))}
                </section>
              )}
            </>
          )}
        </div>
      </main>
      <Footer />
    </div>
  )
}

function ShelfBookCard({
  item,
  isSaving,
  canMoveUp,
  canMoveDown,
  formatCurrency,
  formatDate,
  labels,
  onMove,
  onRemove,
}: {
  item: BookshelfItem
  isSaving: boolean
  canMoveUp: boolean
  canMoveDown: boolean
  formatCurrency: (value: number) => string
  formatDate: (value: string) => string
  labels: {
    order: string
    inStock: string
    outOfStock: string
    openBook: string
    removeBook: string
  }
  onMove: (direction: ShelfMoveDirection) => void
  onRemove: () => void
}) {
  const { language } = useLanguage()
  const hasRating =
    typeof item.book.rating === 'number' && !Number.isNaN(item.book.rating)

  return (
    <SurfaceCard as="article" className="overflow-hidden p-0">
      <div className="grid gap-0 sm:grid-cols-[180px_minmax(0,1fr)]">
        <Link
          to={`/books/${item.book.id}`}
          className="relative block min-h-[220px] overflow-hidden bg-muted"
        >
          <img
            src={getBookCoverUrl(item.book.cover)}
            alt={item.book.title}
            onError={(event) => setBookCoverFallback(event.currentTarget)}
            className="absolute inset-0 size-full object-cover"
          />
        </Link>

        <div className="flex flex-col gap-5 p-5">
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-primary/80">
                {labels.order}
              </p>
              <Link
                to={`/books/${item.book.id}`}
                className="mt-2 block font-heading text-2xl font-bold leading-tight text-slate-950 hover:text-primary dark:text-foreground"
              >
                {item.book.title}
              </Link>
              <p className="mt-2 text-sm text-muted-foreground">
                {item.book.author}
              </p>
            </div>
            <div className="flex gap-2">
              <Button
                type="button"
                variant="outline"
                size="icon"
                className="rounded-2xl"
                disabled={!canMoveUp || isSaving}
                onClick={() => onMove('UP')}
              >
                <ArrowUp className="size-4" />
              </Button>
              <Button
                type="button"
                variant="outline"
                size="icon"
                className="rounded-2xl"
                disabled={!canMoveDown || isSaving}
                onClick={() => onMove('DOWN')}
              >
                <ArrowDown className="size-4" />
              </Button>
            </div>
          </div>

          <div className="flex flex-wrap gap-2">
            <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary dark:bg-primary/18">
              {getCategoryLabel(item.book.categoryInfo ?? item.book.category, language)}
            </span>
            <span
              className={cn(
                'rounded-full px-3 py-1 text-xs font-semibold',
                item.book.stockQuantity > 0
                  ? 'bg-emerald-50 text-emerald-700 dark:bg-emerald-400/10 dark:text-emerald-200'
                  : 'bg-rose-50 text-rose-600 dark:bg-rose-400/10 dark:text-rose-200',
              )}
            >
              {item.book.stockQuantity > 0 ? labels.inStock : labels.outOfStock}
            </span>
          </div>

          <div className="space-y-2 text-sm text-muted-foreground">
            <p className="font-heading text-2xl font-bold text-primary">
              {formatCurrency(item.book.price)}
            </p>
            {hasRating ? (
              <p>
                {item.book.rating?.toFixed(1)} • {item.book.reviews ?? 0}
              </p>
            ) : null}
            <p>{formatDate(item.updatedAt)}</p>
          </div>

          <div className="mt-auto flex flex-wrap gap-2">
            <Link
              to={`/books/${item.book.id}`}
              className={cn(
                buttonVariants({ variant: 'outline' }),
                'rounded-2xl',
              )}
            >
              <ExternalLink className="mr-2 size-4" />
              {labels.openBook}
            </Link>
            <Button
              type="button"
              variant="outline"
              className="rounded-2xl border-rose-200 text-rose-500 hover:bg-rose-50 hover:text-rose-600 dark:border-rose-400/25 dark:text-rose-300 dark:hover:bg-rose-400/10"
              disabled={isSaving}
              onClick={onRemove}
            >
              <X className="mr-2 size-4" />
              {labels.removeBook}
            </Button>
          </div>
        </div>
      </div>
    </SurfaceCard>
  )
}
