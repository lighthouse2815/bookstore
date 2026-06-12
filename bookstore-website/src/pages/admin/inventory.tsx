import { useEffect, useMemo, useState } from 'react'
import { createPortal } from 'react-dom'
import {
  AlertTriangle,
  ArrowRightLeft,
  BookOpen,
  Boxes,
  Eye,
  Search,
  X,
} from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useLanguage } from '@/contexts/language-context'
import {
  getAdminBookStockMovements,
  getAdminStockMovements,
} from '@/services/admin-access-service'
import { getBookCatalog } from '@/services/book-service'
import type {
  AdminStockMovementResponse,
  AdminStockMovementType,
} from '@/types/admin-access'
import type { Book } from '@/types/book'
import { getErrorMessage } from '@/utils'

export default function AdminInventoryPage() {
  const { language, t, formatCurrency, formatDate, formatNumber } =
    useLanguage()
  const isVietnamese = language === 'vi'
  const [books, setBooks] = useState<Book[]>([])
  const [movements, setMovements] = useState<AdminStockMovementResponse[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedBook, setSelectedBook] = useState<Book | null>(null)
  const [bookMovements, setBookMovements] = useState<AdminStockMovementResponse[]>(
    [],
  )
  const [isHistoryLoading, setIsHistoryLoading] = useState(false)
  const [historyError, setHistoryError] = useState<string | null>(null)

  const labels = useMemo(
    () => ({
      title: isVietnamese ? 'Quan ly ton kho' : 'Inventory management',
      description: isVietnamese
        ? 'Theo doi ton hien tai va lich su bien dong kho cua tung dau sach.'
        : 'Track current stock and movement history for each book title.',
      totalBooks: isVietnamese ? '{count} dau sach' : '{count} books',
      search: isVietnamese
        ? 'Tim theo ten sach, tac gia, the loai...'
        : 'Search by title, author, or category...',
      empty: isVietnamese ? 'Khong co sach trong kho' : 'No inventory found',
      lowStock: isVietnamese ? 'Sap het' : 'Low stock',
      inStock: isVietnamese ? 'Con hang' : 'In stock',
      outOfStock: isVietnamese ? 'Het hang' : 'Out of stock',
      recentMovements: isVietnamese ? 'Bien dong gan day' : 'Recent movements',
      movementHistory: isVietnamese ? 'Lich su bien dong' : 'Movement history',
      latestMovement: isVietnamese ? 'Bien dong moi nhat' : 'Latest movement',
      book: isVietnamese ? 'Sach' : 'Book',
      price: isVietnamese ? 'Gia ban' : 'Price',
      stock: isVietnamese ? 'Ton kho' : 'Stock',
      loadError: isVietnamese
        ? 'Khong tai duoc du lieu ton kho'
        : 'Unable to load inventory',
      historyError: isVietnamese
        ? 'Khong tai duoc lich su ton kho'
        : 'Unable to load stock history',
      noMovement: isVietnamese ? 'Chua co bien dong kho' : 'No stock movement yet',
      reference: isVietnamese ? 'Lien ket nghiep vu' : 'Reference',
      beforeAfter: isVietnamese ? 'Truoc / Sau' : 'Before / After',
      quantity: isVietnamese ? 'So luong' : 'Quantity',
      unknownReference: isVietnamese ? 'Khong co tham chieu' : 'No reference',
    }),
    [isVietnamese],
  )

  const movementLookup = useMemo(() => {
    const grouped = new Map<string, AdminStockMovementResponse[]>()

    for (const movement of movements) {
      const currentList = grouped.get(movement.bookId) ?? []
      currentList.push(movement)
      grouped.set(movement.bookId, currentList)
    }

    for (const [bookId, currentList] of grouped.entries()) {
      grouped.set(
        bookId,
        [...currentList].sort(
          (leftMovement, rightMovement) =>
            new Date(rightMovement.createdAt).getTime() -
            new Date(leftMovement.createdAt).getTime(),
        ),
      )
    }

    return grouped
  }, [movements])

  const filteredBooks = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return books
    }

    return books.filter((book) =>
      [book.title, book.author, book.category, book.publisher]
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [books, searchTerm])

  const lowStockCount = books.filter(
    (book) => book.stockQuantity > 0 && book.stockQuantity <= 5,
  ).length
  const outOfStockCount = books.filter((book) => book.stockQuantity === 0).length
  const recentMovementCount = movements.filter((movement) => {
    const movementTime = new Date(movement.createdAt).getTime()
    return movementTime >= Date.now() - 7 * 24 * 60 * 60 * 1000
  }).length

  useEffect(() => {
    void loadInventory()
  }, [])

  useEffect(() => {
    if (!selectedBook) {
      return
    }

    const previousOverflow = document.body.style.overflow

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape' && !isHistoryLoading) {
        closeHistory()
      }
    }

    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [isHistoryLoading, selectedBook])

  async function loadInventory() {
    setIsLoading(true)

    try {
      const [bookResponse, movementResponse] = await Promise.all([
        getBookCatalog(),
        getAdminStockMovements(),
      ])

      setBooks(bookResponse.books)
      setMovements(movementResponse)
      setError(null)
    } catch (currentError) {
      setError(getErrorMessage(currentError, labels.loadError))
    } finally {
      setIsLoading(false)
    }
  }

  async function openHistory(book: Book) {
    setSelectedBook(book)
    setIsHistoryLoading(true)
    setHistoryError(null)

    try {
      const response = await getAdminBookStockMovements(book.id)
      setBookMovements(
        [...response].sort(
          (leftMovement, rightMovement) =>
            new Date(rightMovement.createdAt).getTime() -
            new Date(leftMovement.createdAt).getTime(),
        ),
      )
    } catch (currentError) {
      setHistoryError(getErrorMessage(currentError, labels.historyError))
      setBookMovements([])
    } finally {
      setIsHistoryLoading(false)
    }
  }

  function closeHistory() {
    if (isHistoryLoading) {
      return
    }

    setSelectedBook(null)
    setBookMovements([])
    setHistoryError(null)
  }

  const dialogMarkup = selectedBook ? (
    <div className="fixed inset-0 z-[160] flex items-center justify-center px-4 py-6">
      <button
        type="button"
        aria-label={t('common.close')}
        className="absolute inset-0 bg-background/72 backdrop-blur-sm"
        onClick={closeHistory}
        disabled={isHistoryLoading}
      />
      <div className="relative z-10 w-full max-w-4xl">
        <DialogShell
          title={`${labels.movementHistory}: ${selectedBook.title}`}
          onClose={closeHistory}
          canClose={!isHistoryLoading}
        >
          <div className="space-y-6">
            <div className="grid gap-4 md:grid-cols-3">
              <MetricCard
                label={labels.stock}
                value={formatNumber(selectedBook.stockQuantity)}
              />
              <MetricCard
                label={labels.latestMovement}
                value={
                  bookMovements[0]
                    ? getMovementTypeLabel(bookMovements[0].type, isVietnamese)
                    : labels.noMovement
                }
              />
              <MetricCard
                label={labels.recentMovements}
                value={formatNumber(bookMovements.length)}
              />
            </div>

            {historyError ? (
              <div className="rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                {historyError}
              </div>
            ) : null}

            {isHistoryLoading ? (
              <div className="rounded-[22px] border border-border/60 bg-background/55 px-6 py-10 text-center text-muted-foreground">
                {t('common.loading')}
              </div>
            ) : bookMovements.length === 0 ? (
              <div className="rounded-[22px] border border-dashed border-border/60 bg-background/35 px-6 py-10 text-center text-muted-foreground">
                {labels.noMovement}
              </div>
            ) : (
              <div className="space-y-3">
                {bookMovements.map((movement) => (
                  <article
                    key={movement.id}
                    className="grid gap-4 rounded-[22px] border border-border/60 bg-background/55 p-4 md:grid-cols-[12rem_10rem_1fr_10rem]"
                  >
                    <div>
                      <p className="text-sm text-muted-foreground">
                        {formatDate(movement.createdAt)}
                      </p>
                      <p className="mt-2">
                        <MovementBadge
                          type={movement.type}
                          isVietnamese={isVietnamese}
                        />
                      </p>
                    </div>

                    <div>
                      <p className="text-xs uppercase tracking-[0.12em] text-muted-foreground">
                        {labels.quantity}
                      </p>
                      <p className="mt-2 text-lg font-semibold text-foreground">
                        {formatSignedQuantity(movement.type, movement.quantity)}
                      </p>
                    </div>

                    <div>
                      <p className="text-xs uppercase tracking-[0.12em] text-muted-foreground">
                        {labels.beforeAfter}
                      </p>
                      <p className="mt-2 font-semibold text-foreground">
                        {formatNumber(movement.beforeQuantity)} →{' '}
                        {formatNumber(movement.afterQuantity)}
                      </p>
                      <p className="mt-2 break-all text-xs text-muted-foreground">
                        {labels.reference}:{' '}
                        {movement.referenceType && movement.referenceId
                          ? `${movement.referenceType} / ${movement.referenceId}`
                          : labels.unknownReference}
                      </p>
                    </div>

                    <div className="flex items-center justify-start md:justify-end">
                      <Badge variant="outline" className="rounded-2xl px-3 py-1.5">
                        {formatNumber(movement.afterQuantity)}
                      </Badge>
                    </div>
                  </article>
                ))}
              </div>
            )}
          </div>
        </DialogShell>
      </div>
    </div>
  ) : null

  return (
    <>
      <AdminLayout>
        <div className="relative overflow-hidden rounded-[32px] border border-border/60 bg-card/90 p-6 shadow-[0_28px_90px_rgba(2,6,23,0.35)] backdrop-blur xl:p-8">
          <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(129,140,248,0.18),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(16,185,129,0.14),transparent_32%)]" />

          <div className="relative">
            <div className="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
              <div>
                <div className="flex flex-wrap items-center gap-3">
                  <h1 className="font-heading text-3xl font-bold text-foreground sm:text-4xl">
                    {labels.title}
                  </h1>
                  <Badge
                    variant="outline"
                    className="rounded-2xl border-primary/20 bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary dark:border-primary/30"
                  >
                    <Boxes className="mr-2 h-4 w-4" />
                    {interpolateLabel(labels.totalBooks, {
                      count: formatNumber(books.length),
                    })}
                  </Badge>
                </div>
                <p className="mt-3 max-w-2xl text-base text-muted-foreground">
                  {labels.description}
                </p>
              </div>

              <div className="grid gap-3 sm:grid-cols-3">
                <MetricCard
                  label={labels.lowStock}
                  value={formatNumber(lowStockCount)}
                />
                <MetricCard
                  label={labels.outOfStock}
                  value={formatNumber(outOfStockCount)}
                />
                <MetricCard
                  label={labels.recentMovements}
                  value={formatNumber(recentMovementCount)}
                />
              </div>
            </div>

            <div className="mt-8 max-w-xl">
              <div className="relative">
                <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                <Input
                  value={searchTerm}
                  onChange={(event) => setSearchTerm(event.currentTarget.value)}
                  placeholder={labels.search}
                  className="h-14 rounded-2xl border-border/70 bg-background/55 pl-12 text-base"
                />
              </div>
            </div>

            {error && !isLoading ? (
              <div className="mt-8 rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                {error}
              </div>
            ) : null}

            <section className="mt-8 overflow-hidden rounded-[28px] border border-primary/30 bg-background/20 shadow-[0_24px_80px_rgba(15,23,42,0.24)] backdrop-blur">
              <div className="space-y-4 p-4">
                <div className="hidden rounded-[24px] border border-border/60 bg-background/55 text-sm font-semibold uppercase tracking-[0.08em] text-muted-foreground shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2.2fr)_11rem_1.2fr_12rem]">
                  <div className="px-8 py-6">{labels.book}</div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {labels.stock}
                  </div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {labels.latestMovement}
                  </div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {t('common.actions')}
                  </div>
                </div>

                {isLoading ? (
                  <div className="rounded-[24px] border border-border/50 bg-background/40 px-6 py-10 text-center text-muted-foreground">
                    {t('common.loading')}
                  </div>
                ) : filteredBooks.length === 0 ? (
                  <div className="rounded-[24px] border border-dashed border-border/60 bg-background/35 px-6 py-10 text-center text-muted-foreground">
                    {labels.empty}
                  </div>
                ) : (
                  filteredBooks.map((book) => {
                    const latestMovement = movementLookup.get(book.id)?.[0]

                    return (
                      <article
                        key={book.id}
                        className="flex flex-col gap-5 rounded-[24px] border border-border/60 bg-background/55 p-5 shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2.2fr)_11rem_1.2fr_12rem] xl:gap-0 xl:p-0"
                      >
                        <div className="flex min-w-0 items-center gap-5 xl:px-8 xl:py-6">
                          <div className="flex h-20 w-16 shrink-0 items-center justify-center overflow-hidden rounded-[16px] border border-border/60 bg-background/70">
                            {book.cover ? (
                              <img
                                src={book.cover}
                                alt={book.title}
                                className="size-full object-cover"
                              />
                            ) : (
                              <BookOpen className="h-7 w-7 text-primary" />
                            )}
                          </div>
                          <div className="min-w-0">
                            <p className="truncate text-xl font-semibold text-foreground">
                              {book.title}
                            </p>
                            <p className="mt-2 truncate text-sm text-muted-foreground">
                              {book.author} · {book.category}
                            </p>
                            <p className="mt-2 text-sm font-medium text-foreground">
                              {labels.price}: {formatCurrency(book.price)}
                            </p>
                          </div>
                        </div>

                        <div className="flex items-center justify-start border-border/40 xl:justify-center xl:border-l">
                          <StockBadge
                            count={book.stockQuantity}
                            isVietnamese={isVietnamese}
                            formatNumber={formatNumber}
                          />
                        </div>

                        <div className="flex items-center justify-start border-border/40 xl:justify-center xl:border-l">
                          {latestMovement ? (
                            <div className="text-left xl:text-center">
                              <MovementBadge
                                type={latestMovement.type}
                                isVietnamese={isVietnamese}
                              />
                              <p className="mt-2 text-xs text-muted-foreground">
                                {formatDate(latestMovement.createdAt)}
                              </p>
                            </div>
                          ) : (
                            <p className="text-sm text-muted-foreground">
                              {labels.noMovement}
                            </p>
                          )}
                        </div>

                        <div className="flex items-center justify-start border-border/40 xl:justify-center xl:border-l">
                          <Button
                            type="button"
                            variant="outline"
                            onClick={() => void openHistory(book)}
                            className="rounded-2xl"
                          >
                            <Eye className="mr-2 h-4 w-4" />
                            {labels.movementHistory}
                          </Button>
                        </div>
                      </article>
                    )
                  })
                )}
              </div>
            </section>
          </div>
        </div>
      </AdminLayout>

      {dialogMarkup && typeof document !== 'undefined'
        ? createPortal(dialogMarkup, document.body)
        : null}
    </>
  )
}

function DialogShell({
  canClose = true,
  children,
  onClose,
  title,
}: {
  canClose?: boolean
  children: React.ReactNode
  onClose: () => void
  title: string
}) {
  return (
    <div className="overflow-hidden rounded-[28px] border border-border/70 bg-card/95 shadow-[0_30px_120px_rgba(2,6,23,0.5)] backdrop-blur">
      <div className="flex items-start justify-between gap-4 border-b border-border/60 px-6 py-5">
        <h2 className="text-2xl font-semibold text-foreground">{title}</h2>
        <Button
          type="button"
          variant="ghost"
          size="icon-sm"
          onClick={onClose}
          className="rounded-2xl"
          disabled={!canClose}
        >
          <X className="h-4 w-4" />
        </Button>
      </div>
      <div className="max-h-[78vh] overflow-y-auto px-6 py-6">{children}</div>
    </div>
  )
}

function MetricCard({
  label,
  value,
}: {
  label: string
  value: string
}) {
  return (
    <div className="rounded-2xl border border-border/60 bg-background/55 px-5 py-4">
      <p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">
        {label}
      </p>
      <p className="mt-2 text-2xl font-bold text-foreground">{value}</p>
    </div>
  )
}

function StockBadge({
  count,
  formatNumber,
  isVietnamese,
}: {
  count: number
  formatNumber: (value: number) => string
  isVietnamese: boolean
}) {
  if (count === 0) {
    return (
      <Badge variant="destructive" className="rounded-2xl px-3 py-1.5">
        <AlertTriangle className="mr-1.5 h-3.5 w-3.5" />
        {isVietnamese ? 'Het hang' : 'Out of stock'}
      </Badge>
    )
  }

  if (count <= 5) {
    return (
      <Badge variant="outline" className="rounded-2xl px-3 py-1.5 text-amber-500">
        {formatNumber(count)} · {isVietnamese ? 'Sap het' : 'Low stock'}
      </Badge>
    )
  }

  return (
    <Badge variant="outline" className="rounded-2xl px-3 py-1.5 text-emerald-500">
      {formatNumber(count)} · {isVietnamese ? 'Con hang' : 'In stock'}
    </Badge>
  )
}

function MovementBadge({
  type,
  isVietnamese,
}: {
  type: AdminStockMovementType
  isVietnamese: boolean
}) {
  const tone =
    type === 'IMPORT'
      ? 'text-emerald-500'
      : type === 'SALE'
        ? 'text-amber-500'
        : type === 'CANCEL_ORDER'
          ? 'text-sky-500'
          : 'text-primary'

  return (
    <Badge variant="outline" className={`rounded-2xl px-3 py-1.5 ${tone}`}>
      <ArrowRightLeft className="mr-1.5 h-3.5 w-3.5" />
      {getMovementTypeLabel(type, isVietnamese)}
    </Badge>
  )
}

function getMovementTypeLabel(
  type: AdminStockMovementType,
  isVietnamese: boolean,
) {
  switch (type) {
    case 'IMPORT':
      return isVietnamese ? 'Nhap kho' : 'Import'
    case 'SALE':
      return isVietnamese ? 'Ban hang' : 'Sale'
    case 'CANCEL_ORDER':
      return isVietnamese ? 'Huy don' : 'Cancel order'
    case 'ADJUSTMENT':
      return isVietnamese ? 'Dieu chinh' : 'Adjustment'
  }
}

function formatSignedQuantity(
  type: AdminStockMovementType,
  quantity: number,
) {
  if (type === 'SALE') {
    return `-${quantity}`
  }

  return `+${quantity}`
}

function interpolateLabel(
  template: string,
  params: Record<string, string | number>,
) {
  return template.replace(/\{(\w+)\}/g, (_, key: string) =>
    String(params[key] ?? `{${key}}`),
  )
}
