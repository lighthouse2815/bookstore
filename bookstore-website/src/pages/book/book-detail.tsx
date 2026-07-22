import {
  useEffect,
  useState,
  type KeyboardEvent,
  type ReactNode,
} from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  BookPlus,
  BookOpenText,
  ChevronRight,
  Heart,
  Headphones,
  PackageCheck,
  ShieldCheck,
  Star,
  TicketPercent,
  Truck,
  Undo2,
  UserRound,
  PencilLine,
} from 'lucide-react'
import { toast } from 'sonner'
import { AddToCart } from '@/components/book/add-to-cart'
import { BookCard } from '@/components/book/book-card'
import { StatePanel, SurfaceCard } from '@/components/common/page-shell'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import { useWishlist } from '@/contexts/wishlist-context'
import { useBookDetail } from '@/hooks/use-book-detail'
import NotFoundPage from '@/pages/home/not-found'
import type {
  AuthorResponse,
  Book,
  BookCardData,
  BookPromotion,
  BookRatingSummary,
} from '@/types/book'
import { getBookCoverUrl, setBookCoverFallback } from '@/utils/book-cover'
import { getCategoryLabel } from '@/utils/i18n'
import {
  getRecentlyViewedBooks,
  pushRecentlyViewedBook,
} from '@/utils/recently-viewed'

type TranslateFunction = (
  key: string,
  params?: Record<string, number | string>,
) => string

type GalleryImage = {
  id: string
  src: string
  alt: string
}

type DetailItem = {
  label: string
  value: string
}

const BOOK_DETAIL_TAB_IDS = ['details', 'description', 'reviews'] as const

type BookDetailTab = (typeof BOOK_DETAIL_TAB_IDS)[number]

export default function BookDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const { isAuthenticated } = useAuth()
  const { isWishlisted, toggleBook } = useWishlist()
  const { t, formatCurrency, formatDate, formatNumber, formatYear } = useLanguage()
  const {
    book,
    suggestions,
    author,
    categoryTrail,
    promotions,
    ratingSummary,
    reviews,
    digitalAssets,
    isLoading,
    error,
    notFound,
  } = useBookDetail(id)
  const [selectedImageIndex, setSelectedImageIndex] = useState(0)
  const [activeDetailTab, setActiveDetailTab] =
    useState<BookDetailTab>('details')
  const [recentlyViewedBooks, setRecentlyViewedBooks] = useState<BookCardData[]>(
    [],
  )

  useEffect(() => {
    setSelectedImageIndex(0)
    setActiveDetailTab('details')
  }, [id])

  useEffect(() => {
    if (!book) {
      setRecentlyViewedBooks([])
      return
    }

    pushRecentlyViewedBook(book)
    setRecentlyViewedBooks(
      getRecentlyViewedBooks().filter((item) => item.id !== book.id).slice(0, 4),
    )
  }, [book])

  async function handleToggleWishlist() {
    if (!book) {
      return
    }

    if (!isAuthenticated) {
      toast.error(t('wishlist.loginRequired'))
      navigate('/login')
      return
    }

    try {
      const added = await toggleBook(toBookCardData(book))
      toast.success(
        added
          ? t('wishlist.added', { title: book.title })
          : t('wishlist.removed', { title: book.title }),
      )
    } catch (error) {
      toast.error(
        error instanceof Error ? error.message : t('wishlist.updateError'),
      )
    }
  }

  function handleAddToShelf() {
    if (!book) {
      return
    }

    if (!isAuthenticated) {
      toast.error(t('shelves.loginRequired'))
      navigate('/login')
      return
    }

    navigate(`/shelves?addBook=${book.id}`)
  }

  function handleAddJournalEntry() {
    if (!book) {
      return
    }

    if (!isAuthenticated) {
      toast.error(t('readingJournal.loginRequired'))
      navigate('/login')
      return
    }

    navigate(`/reading-journal?bookId=${book.id}`)
  }

  function handleDetailTabKeyDown(
    event: KeyboardEvent<HTMLButtonElement>,
    currentIndex: number,
  ) {
    let nextIndex: number | null = null

    if (event.key === 'ArrowRight') {
      nextIndex = (currentIndex + 1) % BOOK_DETAIL_TAB_IDS.length
    } else if (event.key === 'ArrowLeft') {
      nextIndex =
        (currentIndex - 1 + BOOK_DETAIL_TAB_IDS.length) %
        BOOK_DETAIL_TAB_IDS.length
    } else if (event.key === 'Home') {
      nextIndex = 0
    } else if (event.key === 'End') {
      nextIndex = BOOK_DETAIL_TAB_IDS.length - 1
    }

    if (nextIndex === null) {
      return
    }

    event.preventDefault()
    const nextTab = BOOK_DETAIL_TAB_IDS[nextIndex]
    setActiveDetailTab(nextTab)
    document.getElementById(`book-detail-tab-${nextTab}`)?.focus()
  }

  if (isLoading) {
    return (
      <div className="flex min-h-screen flex-col">
        <Header />
        <main className="mx-auto flex w-full max-w-7xl flex-1 items-center justify-center px-4 py-6 sm:px-6 lg:px-8">
          <StatePanel title={t('common.loading')} />
        </main>
        <Footer />
      </div>
    )
  }

  if (error) {
    return (
      <div className="flex min-h-screen flex-col">
        <Header />
        <main className="mx-auto flex w-full max-w-7xl flex-1 items-center justify-center px-4 py-6 sm:px-6 lg:px-8">
          <StatePanel
            tone="error"
            title={t('book.listing.errorTitle')}
            description={error}
          />
        </main>
        <Footer />
      </div>
    )
  }

  if (notFound || !book) {
    return <NotFoundPage />
  }

  const detailFallback = t('book.detail.detailsFallback')
  const discount =
    typeof book.oldPrice === 'number' && book.oldPrice > book.price
      ? Math.round((1 - book.price / book.oldPrice) * 100)
      : 0
  const galleryImages = getBookGalleryImages(book, t)
  const activeImageIndex = galleryImages[selectedImageIndex]
    ? selectedImageIndex
    : 0
  const activeImage = galleryImages[activeImageIndex]
  const resolvedRatingSummary = getResolvedRatingSummary(book, ratingSummary)
  const averageRating = resolvedRatingSummary.averageRating
  const reviewCount = resolvedRatingSummary.reviewCount
  const reviewItems = reviews.slice(0, 3)
  const brandName = t('common.brand')
  const isFavorite = isWishlisted(book.id)
  const detailTabs = [
    {
      id: 'details' as const,
      label: t('book.detail.detailsTitle'),
      icon: BookOpenText,
    },
    {
      id: 'description' as const,
      label: t('book.detail.descriptionTitle'),
      icon: PencilLine,
    },
    {
      id: 'reviews' as const,
      label: `${t('book.detail.reviewTitle')} (${formatNumber(reviewCount)})`,
      icon: Star,
    },
  ]

  const detailItems = [
    createTextDetailItem(t('book.detail.specIsbn'), book.isbn, detailFallback),
    createNumberDetailItem(
      t('book.detail.specPageCount'),
      book.detail?.pageCount,
      (value) =>
        t('book.detail.pageCountValue', { count: formatNumber(value) }),
      detailFallback,
    ),
    createTextDetailItem(
      t('book.detail.specLanguage'),
      book.detail?.language,
      detailFallback,
    ),
    createTextDetailItem(
      t('book.detail.specCoverType'),
      book.detail?.coverType,
      detailFallback,
    ),
    createTextDetailItem(
      t('book.detail.specDimensions'),
      book.detail?.dimensions,
      detailFallback,
    ),
    createTextDetailItem(
      t('book.detail.specPublisher'),
      book.publisher || null,
      detailFallback,
    ),
    createNumberDetailItem(
      t('book.detail.specPublicationYear'),
      book.detail?.publicationYear,
      (value) => formatYear(value),
      detailFallback,
    ),
    createNumberDetailItem(
      t('book.detail.specWeight'),
      book.detail?.weight,
      (value) => t('book.detail.weightValue', { count: formatNumber(value) }),
      detailFallback,
    ),
    createTextDetailItem(
      t('book.detail.specTranslator'),
      book.detail?.translator,
      detailFallback,
    ),
    createTextDetailItem(
      t('book.detail.specEdition'),
      book.detail?.edition,
      detailFallback,
    ),
  ]

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="mx-auto w-full max-w-[1380px] flex-1 px-4 py-6 sm:px-6 lg:px-8">
        <nav className="mb-6 flex flex-wrap items-center gap-1 text-sm text-muted-foreground">
          <Link to="/" className="hover:text-primary">
            {t('book.detail.breadcrumbHome')}
          </Link>
          <ChevronRight className="size-4" />
          <Link to="/books" className="hover:text-primary">
            {t('book.detail.breadcrumbBooks')}
          </Link>
          {categoryTrail.map((category) => (
            <div key={category.id} className="flex items-center gap-1">
              <ChevronRight className="size-4" />
              <span>{getCategoryLabel(category.name, t)}</span>
            </div>
          ))}
          <ChevronRight className="size-4" />
          <span className="line-clamp-1 text-foreground">{book.title}</span>
        </nav>

        <section className="grid gap-6 xl:grid-cols-[88px_minmax(340px,430px)_minmax(0,1fr)_320px] 2xl:grid-cols-[96px_440px_minmax(0,1fr)_340px]">
          <div className="order-2 flex gap-3 overflow-x-auto pb-2 xl:order-1 xl:flex-col xl:overflow-visible xl:pb-0 xl:pt-1">
            {galleryImages.map((image, index) => (
              <button
                key={image.id}
                type="button"
                onClick={() => setSelectedImageIndex(index)}
                aria-label={image.alt}
                aria-pressed={index === activeImageIndex}
                className={`relative aspect-[3/4] w-16 shrink-0 overflow-hidden rounded-[1.25rem] border bg-card transition-all xl:w-full ${
                  index === activeImageIndex
                    ? 'border-primary shadow-[0_12px_30px_-18px_hsl(var(--primary)/0.8)] ring-2 ring-primary/15'
                    : 'border-border hover:border-primary/40'
                }`}
              >
                <img
                  src={image.src}
                  alt={image.alt}
                  onError={(event) => setBookCoverFallback(event.currentTarget)}
                  className="absolute inset-0 size-full object-cover"
                />
              </button>
            ))}
          </div>

          <div className="order-1 xl:order-2">
            <div className="mx-auto w-full max-w-[440px] xl:mx-0">
              <div className="relative aspect-[3/4] overflow-hidden rounded-[2rem] border border-border bg-card shadow-[0_36px_90px_-48px_hsl(var(--foreground)/0.45)]">
                <img
                  src={activeImage.src}
                  alt={activeImage.alt}
                  onError={(event) => setBookCoverFallback(event.currentTarget)}
                  className="absolute inset-0 size-full object-cover"
                />
                {discount > 0 && (
                  <span className="absolute right-4 top-4 rounded-full bg-primary px-3 py-1 text-sm font-bold text-primary-foreground">
                    -{discount}%
                  </span>
                )}
              </div>
            </div>
          </div>

          <section className="order-3 min-w-0 space-y-6 xl:pt-6">
            <div className="space-y-4">
              <span className="text-sm font-semibold uppercase tracking-[0.18em] text-primary">
                {getCategoryLabel(book.category, t)}
              </span>
              <div className="space-y-3">
                <h1 className="font-heading text-4xl font-bold tracking-tight text-balance text-foreground lg:text-5xl">
                  {book.title}
                </h1>
                <div className="flex flex-wrap items-center gap-x-3 gap-y-2 text-sm text-muted-foreground">
                  <span>
                    {t('book.detail.author')}{' '}
                    <span className="font-semibold text-foreground">
                      {book.author || t('book.fallback.author')}
                    </span>
                  </span>
                  <span className="hidden h-4 w-px bg-border sm:block" />
                  <span>
                    {t('book.detail.specPublisher')}{' '}
                    <span className="font-semibold text-foreground">
                      {book.publisher || t('book.fallback.publisher')}
                    </span>
                  </span>
                  <span className="hidden h-4 w-px bg-border sm:block" />
                  <span>
                    {t('book.detail.specPublicationYear')}{' '}
                    <span className="font-semibold text-foreground">
                      {book.detail?.publicationYear
                        ? formatYear(book.detail.publicationYear)
                        : detailFallback}
                    </span>
                  </span>
                </div>
              </div>

              <div className="flex flex-wrap items-center gap-x-4 gap-y-2 border-y border-border py-4 text-sm">
                <div className="flex items-center gap-2">
                  <div className="flex items-center gap-0.5">
                    {Array.from({ length: 5 }).map((_, index) => (
                      <Star
                        key={index}
                        className={
                          index < Math.round(averageRating)
                            ? 'size-4 fill-chart-3 text-chart-3'
                            : 'size-4 text-border'
                        }
                      />
                    ))}
                  </div>
                  <span className="font-semibold text-foreground">
                    {averageRating > 0 ? averageRating.toFixed(1) : '0.0'}
                  </span>
                </div>
                <span className="text-muted-foreground">
                  {t('book.detail.reviewsCount', {
                    count: formatNumber(reviewCount),
                  })}
                </span>
                <span className="hidden h-4 w-px bg-border sm:block" />
                <span className="text-muted-foreground">
                  {t('book.detail.soldCountValue', {
                    count: formatNumber(book.soldCount),
                  })}
                </span>
              </div>
            </div>

            <div className="flex flex-wrap items-end gap-3">
              <span className="font-heading text-4xl font-bold text-primary sm:text-5xl">
                {formatCurrency(book.price)}
              </span>
              {book.oldPrice && book.oldPrice > book.price && (
                <span className="pb-1 text-xl text-muted-foreground line-through">
                  {formatCurrency(book.oldPrice)}
                </span>
              )}
              {book.oldPrice && book.oldPrice > book.price && (
                <span className="mb-1 rounded-full bg-primary/10 px-2.5 py-1 text-sm font-semibold text-primary">
                  {t('book.detail.saveAmount', {
                    amount: formatCurrency(book.oldPrice - book.price),
                  })}
                </span>
              )}
            </div>

            {digitalAssets.length > 0 ? (
              <div className="rounded-[1.75rem] border border-primary/20 bg-[linear-gradient(135deg,hsl(var(--primary)/0.1),hsl(var(--background))_62%)] p-4 shadow-[0_18px_45px_rgba(99,102,241,0.12)]">
                <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
                  <div className="flex gap-3">
                    <span className="flex size-12 shrink-0 items-center justify-center rounded-2xl bg-primary/12 text-primary">
                      <BookOpenText className="h-5 w-5" />
                    </span>
                    <div>
                      <p className="font-heading text-lg font-bold text-foreground">
                        {t('book.detail.digitalAssets.calloutTitle', {
                          count: formatNumber(digitalAssets.length),
                        })}
                      </p>
                      <p className="mt-1 text-sm leading-6 text-muted-foreground">
                        {t('book.detail.digitalAssets.teaserDescription')}
                      </p>
                    </div>
                  </div>

                  <div className="flex flex-wrap gap-2 sm:justify-end">
                    <Link
                      to={`/books/${book.id}/ebook`}
                      className="inline-flex items-center rounded-2xl border border-border bg-background/70 px-4 py-2.5 text-sm font-semibold transition-colors hover:bg-muted"
                    >
                      {t('book.detail.digitalAssets.viewOptions')}
                    </Link>
                  </div>
                </div>
              </div>
            ) : null}

          </section>

          <aside className="order-4 space-y-4 xl:pt-1">
            <SurfaceCard className="p-5">
              <div className="mb-4 flex items-center gap-2">
                <TicketPercent className="size-5 text-primary" />
                <h2 className="font-heading text-xl font-bold">
                  {t('book.detail.promotionsTitle')}
                </h2>
              </div>
              <div className="space-y-3">
                {promotions.length > 0 ? (
                  promotions.slice(0, 3).map((promotion) => (
                    <div
                      key={promotion.id}
                      className="rounded-2xl border border-border bg-muted/30 p-3"
                    >
                      <p className="font-semibold text-foreground">
                        {getPromotionDiscountLabel(
                          promotion,
                          formatCurrency,
                          formatNumber,
                        )}
                      </p>
                      <p className="mt-1 text-sm leading-6 text-muted-foreground">
                        {promotion.description?.trim() ||
                          getPromotionMinOrderLabel(
                            promotion,
                            t,
                            formatCurrency,
                          )}
                      </p>
                      <div className="mt-3 flex flex-wrap items-center gap-2 text-sm">
                        <span className="text-muted-foreground">
                          {t('book.detail.promotionCodeLabel')}:
                        </span>
                        <span className="rounded-full border border-primary/20 bg-primary/10 px-2.5 py-1 font-semibold text-primary">
                          {promotion.code}
                        </span>
                      </div>
                      {promotion.maxDiscountAmount && (
                        <p className="mt-2 text-sm text-muted-foreground">
                          {t('book.detail.promotionMaxDiscount', {
                            amount: formatCurrency(promotion.maxDiscountAmount),
                          })}
                        </p>
                      )}
                    </div>
                  ))
                ) : (
                  <p className="text-sm text-muted-foreground">
                    {t('book.detail.promotionsEmpty')}
                  </p>
                )}
              </div>
            </SurfaceCard>

            <SurfaceCard className="p-5">
              <h2 className="mb-4 font-heading text-xl font-bold">
                {t('book.detail.commitmentsTitle', { brand: brandName })}
              </h2>
              <div className="space-y-3 text-sm text-muted-foreground">
                <CommitmentRow
                  icon={<ShieldCheck className="size-4 text-primary" />}
                  text={t('book.detail.commitmentAuthentic')}
                />
                <CommitmentRow
                  icon={<Undo2 className="size-4 text-primary" />}
                  text={t('book.detail.commitmentReturn')}
                />
                <CommitmentRow
                  icon={<Truck className="size-4 text-primary" />}
                  text={t('book.detail.commitmentShipping')}
                />
                <CommitmentRow
                  icon={<Headphones className="size-4 text-primary" />}
                  text={t('book.detail.commitmentSupport')}
                />
              </div>
            </SurfaceCard>
          </aside>
        </section>

        <section className="mt-6 overflow-hidden rounded-[1.75rem] border border-primary/15 bg-card/55 shadow-[0_24px_70px_-44px_hsl(var(--primary)/0.5)]">
          <div className="flex items-center justify-between gap-4 border-b border-border/70 px-5 py-4 sm:px-6">
            <h2 className="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
              {t('book.detail.availabilityLabel')}
            </h2>
            <span
              className={`inline-flex items-center gap-2 text-sm font-semibold tabular-nums ${
                book.stockQuantity > 0
                  ? 'text-emerald-600 dark:text-emerald-400'
                  : 'text-destructive'
              }`}
            >
              <span
                className={`size-2 rounded-full ${
                  book.stockQuantity > 0 ? 'bg-emerald-500' : 'bg-destructive'
                }`}
              />
              {book.stockQuantity > 0
                ? t('book.detail.stockValue', {
                    count: formatNumber(book.stockQuantity),
                  })
                : t('book.detail.stockOut')}
            </span>
          </div>

          <div className="grid gap-3 p-4 sm:grid-cols-2 sm:p-5 lg:grid-cols-5">
            <div className="flex min-h-24 min-w-0 items-center gap-3 rounded-2xl border border-border/80 bg-background/55 px-4 py-4">
              <span className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary">
                <Truck className="size-[1.125rem]" />
              </span>
              <div className="min-w-0">
                <p className="text-xs text-muted-foreground">
                  {t('book.detail.deliveryTitle')}
                </p>
                <p className="mt-1 text-sm font-semibold leading-5 text-foreground">
                  {t('book.detail.deliveryTime')}
                </p>
              </div>
            </div>

            <div className="flex min-h-24 min-w-0 items-center gap-3 rounded-2xl border border-border/80 bg-background/55 px-4 py-4">
              <span className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary">
                <PackageCheck className="size-[1.125rem]" />
              </span>
              <div className="min-w-0">
                <p className="text-xs text-muted-foreground">
                  {t('book.detail.freeShippingTitle')}
                </p>
                <p className="mt-1 text-sm font-semibold leading-5 text-foreground">
                  {t('book.detail.freeShippingThreshold', {
                    amount: formatCurrency(200000),
                  })}
                </p>
              </div>
            </div>

            <button
              type="button"
              onClick={() => {
                void handleToggleWishlist()
              }}
              className={`group flex min-h-24 items-center justify-center gap-3 rounded-2xl border px-4 py-4 text-sm font-semibold transition-all duration-200 hover:-translate-y-0.5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 active:translate-y-0 ${
                isFavorite
                  ? 'border-primary/35 bg-primary/10 text-primary'
                  : 'border-border/80 bg-background/55 text-foreground hover:border-primary/35 hover:bg-primary/5'
              }`}
              aria-label={
                isFavorite
                  ? t('book.detail.removeFromWishlist')
                  : t('book.detail.addToWishlist')
              }
              aria-pressed={isFavorite}
            >
              <span className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary transition-transform duration-200 group-hover:scale-105">
                <Heart
                  className={`size-[1.125rem] ${isFavorite ? 'fill-current' : ''}`}
                />
              </span>
              {isFavorite
                ? t('book.detail.wishlistedShort')
                : t('book.detail.wishlistShort')}
            </button>

            <button
              type="button"
              onClick={handleAddToShelf}
              className="group flex min-h-24 items-center justify-center gap-3 rounded-2xl border border-border/80 bg-background/55 px-4 py-4 text-sm font-semibold text-foreground transition-all duration-200 hover:-translate-y-0.5 hover:border-primary/35 hover:bg-primary/5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 active:translate-y-0"
              aria-label={t('shelves.addToShelfAction')}
            >
              <span className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary transition-transform duration-200 group-hover:scale-105">
                <BookPlus className="size-[1.125rem]" />
              </span>
              {t('book.detail.shelfShort')}
            </button>

            <button
              type="button"
              onClick={handleAddJournalEntry}
              className="group flex min-h-24 items-center justify-center gap-3 rounded-2xl border border-border/80 bg-background/55 px-4 py-4 text-sm font-semibold text-foreground transition-all duration-200 hover:-translate-y-0.5 hover:border-primary/35 hover:bg-primary/5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 active:translate-y-0 sm:col-span-2 lg:col-span-1"
              aria-label={t('readingJournal.openFromBookDetail')}
            >
              <span className="flex size-10 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary transition-transform duration-200 group-hover:scale-105">
                <PencilLine className="size-[1.125rem]" />
              </span>
              {t('book.detail.journalShort')}
            </button>
          </div>
        </section>

        <section
          aria-label={t('book.addToCart.addToCart')}
          className="mt-4 rounded-[1.75rem] border border-primary/15 bg-card/55 p-4 shadow-[0_22px_60px_-46px_hsl(var(--primary)/0.45)] sm:p-5"
        >
          <AddToCart book={book} />
        </section>

        <div className="mt-12 overflow-x-auto pb-1">
          <div
            role="tablist"
            aria-label={`${book.title} - ${t('book.detail.detailsTitle')}`}
            className="inline-flex min-w-full gap-1 rounded-2xl border border-border/70 bg-muted/30 p-1.5 sm:grid sm:grid-cols-3"
          >
            {detailTabs.map((tab, index) => {
              const TabIcon = tab.icon
              const isActive = activeDetailTab === tab.id

              return (
                <button
                  key={tab.id}
                  id={`book-detail-tab-${tab.id}`}
                  type="button"
                  role="tab"
                  aria-selected={isActive}
                  aria-controls={`book-detail-panel-${tab.id}`}
                  tabIndex={isActive ? 0 : -1}
                  onClick={() => setActiveDetailTab(tab.id)}
                  onKeyDown={(event) => handleDetailTabKeyDown(event, index)}
                  className={`inline-flex min-h-12 min-w-[12rem] items-center justify-center gap-2 rounded-xl px-4 py-3 text-sm font-semibold transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 sm:min-w-0 ${
                    isActive
                      ? 'bg-card text-foreground shadow-[0_8px_24px_rgba(15,23,42,0.08)] ring-1 ring-border/70 dark:shadow-[0_8px_24px_rgba(0,0,0,0.24)]'
                      : 'text-muted-foreground hover:bg-background/70 hover:text-foreground active:scale-[0.99]'
                  }`}
                >
                  <TabIcon
                    className={`size-4 ${isActive ? 'text-primary' : ''}`}
                    strokeWidth={1.8}
                  />
                  <span>{tab.label}</span>
                </button>
              )
            })}
          </div>
        </div>

        {activeDetailTab === 'details' ? (
          <section
            id="book-detail-panel-details"
            role="tabpanel"
            aria-labelledby="book-detail-tab-details"
            tabIndex={0}
            className="mt-6 grid animate-fade-in gap-6 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 xl:grid-cols-[minmax(0,1.5fr)_360px]"
          >
          <div className="rounded-3xl border border-border bg-card p-6 shadow-sm">
            <div className="mb-5 flex items-center gap-2">
              <BookOpenText className="size-5 text-primary" />
              <h2 className="font-heading text-2xl font-bold">
                {t('book.detail.detailsTitle')}
              </h2>
            </div>
            <div className="grid gap-4 md:grid-cols-2">
              {detailItems.map((item) => (
                <div
                  key={item.label}
                  className="rounded-2xl border border-border bg-muted/20 px-4 py-3"
                >
                  <p className="text-xs font-semibold uppercase tracking-[0.14em] text-muted-foreground">
                    {item.label}
                  </p>
                  <p className="mt-2 text-sm font-semibold leading-6 text-foreground">
                    {item.value}
                  </p>
                </div>
              ))}
            </div>
          </div>

          <section className="rounded-3xl border border-border bg-card p-6 shadow-sm">
            <h2 className="mb-5 font-heading text-2xl font-bold">
              {t('book.detail.authorInfoTitle')}
            </h2>
            {author ? (
              <div className="space-y-4">
                <div className="flex items-center gap-4">
                  {author.avatarUrl ? (
                    <img
                      src={author.avatarUrl}
                      alt={author.name}
                      className="size-16 rounded-full object-cover"
                    />
                  ) : (
                    <div className="flex size-16 items-center justify-center rounded-full bg-muted text-muted-foreground">
                      <UserRound className="size-8" />
                    </div>
                  )}
                  <div>
                    <p className="font-heading text-xl font-bold text-foreground">
                      {author.name}
                    </p>
                    {getAuthorLifeLabel(author) && (
                      <p className="text-sm text-muted-foreground">
                        {getAuthorLifeLabel(author)}
                      </p>
                    )}
                  </div>
                </div>
                <p className="leading-7 text-muted-foreground">
                  {author.biography?.trim() || t('book.detail.authorBioFallback')}
                </p>
              </div>
            ) : (
              <p className="text-sm text-muted-foreground">
                {t('book.detail.authorBioFallback')}
              </p>
            )}
          </section>
          </section>
        ) : null}

        {activeDetailTab === 'description' ? (
          <section
            id="book-detail-panel-description"
            role="tabpanel"
            aria-labelledby="book-detail-tab-description"
            tabIndex={0}
            className="mt-6 animate-fade-in rounded-3xl border border-border bg-card p-6 shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 sm:p-8"
          >
          <h2 className="font-heading text-2xl font-bold">
            {t('book.detail.descriptionTitle')}
          </h2>
          <p className="mt-4 max-w-4xl leading-8 text-muted-foreground">
            {book.description || t('book.detail.descriptionFallback')}
          </p>
          </section>
        ) : null}

        {activeDetailTab === 'reviews' ? (
          <section
            id="book-detail-panel-reviews"
            role="tabpanel"
            aria-labelledby="book-detail-tab-reviews"
            tabIndex={0}
            className="mt-6 animate-fade-in rounded-3xl border border-border bg-card p-6 shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 sm:p-8"
          >
          <div className="mb-6 flex items-center justify-between gap-4">
            <div>
              <h2 className="font-heading text-2xl font-bold">
                {t('book.detail.reviewTitle')}
              </h2>
              <p className="mt-1 text-sm text-muted-foreground">
                {t('book.detail.reviewsCount', {
                  count: formatNumber(reviewCount),
                })}
              </p>
            </div>
          </div>

          <div className="grid gap-6 xl:grid-cols-[280px_minmax(0,1fr)]">
            <div className="rounded-3xl border border-border bg-muted/20 p-5">
              <div className="flex items-end gap-2">
                <span className="font-heading text-5xl font-bold text-foreground">
                  {averageRating > 0 ? averageRating.toFixed(1) : '0.0'}
                </span>
                <span className="pb-2 text-lg text-muted-foreground">/5</span>
              </div>
              <div className="mt-3 flex items-center gap-1">
                {Array.from({ length: 5 }).map((_, index) => (
                  <Star
                    key={index}
                    className={
                      index < Math.round(averageRating)
                        ? 'size-4 fill-chart-3 text-chart-3'
                        : 'size-4 text-border'
                    }
                  />
                ))}
              </div>
              <div className="mt-6 space-y-3">
                {[5, 4, 3, 2, 1].map((star) => {
                  const count = resolvedRatingSummary.starBreakdown[star] ?? 0
                  const percentage =
                    reviewCount > 0 ? (count / reviewCount) * 100 : 0

                  return (
                    <div key={star} className="grid grid-cols-[44px_1fr_44px] items-center gap-3 text-sm">
                      <span className="text-muted-foreground">{star}★</span>
                      <div className="h-2 overflow-hidden rounded-full bg-muted">
                        <div
                          className="h-full rounded-full bg-primary"
                          style={{ width: `${percentage}%` }}
                        />
                      </div>
                      <span className="text-right text-muted-foreground">
                        {formatNumber(count)}
                      </span>
                    </div>
                  )
                })}
              </div>
            </div>

            <div className="space-y-4">
              {reviewItems.length > 0 ? (
                reviewItems.map((review) => (
                  <article
                    key={review.reviewId}
                    className="rounded-3xl border border-border bg-muted/10 p-5"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex items-start gap-3">
                        {review.reviewerAvatarUrl ? (
                          <img
                            src={review.reviewerAvatarUrl}
                            alt={review.reviewerName}
                            className="size-12 rounded-full object-cover"
                          />
                        ) : (
                          <div className="flex size-12 items-center justify-center rounded-full bg-muted text-muted-foreground">
                            <UserRound className="size-6" />
                          </div>
                        )}
                        <div>
                          <p className="font-semibold text-foreground">
                            {review.reviewerName}
                          </p>
                          <div className="mt-1 flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                            <div className="flex items-center gap-0.5">
                              {Array.from({ length: 5 }).map((_, index) => (
                                <Star
                                  key={index}
                                  className={
                                    index < review.rating
                                      ? 'size-3.5 fill-chart-3 text-chart-3'
                                      : 'size-3.5 text-border'
                                  }
                                />
                              ))}
                            </div>
                            {review.verifiedPurchase && (
                              <span className="inline-flex items-center gap-1 text-emerald-600">
                                <ShieldCheck className="size-3.5" />
                                {t('book.detail.reviewVerifiedPurchase')}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                      <span className="text-xs text-muted-foreground">
                        {formatDate(review.createdAt)}
                      </span>
                    </div>

                    <p className="mt-4 leading-7 text-muted-foreground">
                      {review.comment?.trim() || detailFallback}
                    </p>

                    {review.reviewImages.length > 0 && (
                      <div className="mt-4 flex flex-wrap gap-3">
                        {review.reviewImages.map((image, index) => (
                          <img
                            key={`${review.reviewId}-${index}`}
                            src={image}
                            alt={`${review.reviewerName}-${index + 1}`}
                            className="size-20 rounded-2xl object-cover"
                          />
                        ))}
                      </div>
                    )}

                    <div className="mt-4 flex items-center gap-2 text-sm text-muted-foreground">
                      <span>{t('book.detail.reviewHelpful', { count: formatNumber(review.helpfulCount) })}</span>
                    </div>
                  </article>
                ))
              ) : (
                <StatePanel
                  minHeightClassName="min-h-[160px]"
                  title={t('book.detail.reviewEmpty')}
                />
              )}
            </div>
          </div>
          </section>
        ) : null}

        {suggestions.length > 0 && (
          <section className="mt-16">
            <h2 className="mb-6 font-heading text-2xl font-bold tracking-tight">
              {t('book.detail.suggestionsTitle')}
            </h2>
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
              {suggestions.map((suggestedBook) => (
                <BookCard key={suggestedBook.id} book={suggestedBook} />
              ))}
            </div>
          </section>
        )}

        {recentlyViewedBooks.length > 0 && (
          <section className="mt-10">
            <h2 className="mb-6 font-heading text-2xl font-bold tracking-tight">
              {t('book.detail.recentlyViewedTitle')}
            </h2>
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 lg:grid-cols-4">
              {recentlyViewedBooks.map((recentBook) => (
                <BookCard key={recentBook.id} book={recentBook} />
              ))}
            </div>
          </section>
        )}
      </main>
      <Footer />
    </div>
  )
}

function CommitmentRow({
  icon,
  text,
}: {
  icon: ReactNode
  text: string
}) {
  return (
    <div className="flex items-center gap-3">
      <span className="flex size-9 items-center justify-center rounded-full bg-primary/10 text-primary">
        {icon}
      </span>
      <span>{text}</span>
    </div>
  )
}

function getBookGalleryImages(book: Book, t: TranslateFunction): GalleryImage[] {
  const fallbackAlt = t('book.card.coverAlt', { title: book.title })
  const galleryImages = new Map<string, GalleryImage>()

  for (const [index, image] of book.images.entries()) {
    const src = getBookCoverUrl(image.imageUrl)
    if (galleryImages.has(src)) {
      continue
    }

    galleryImages.set(src, {
      id: image.id || `${book.id}-image-${index}`,
      src,
      alt: image.altText?.trim() || fallbackAlt,
    })
  }

  const fallbackCover = getBookCoverUrl(book.cover)
  if (!galleryImages.has(fallbackCover)) {
    galleryImages.set(fallbackCover, {
      id: `${book.id}-cover`,
      src: fallbackCover,
      alt: fallbackAlt,
    })
  }

  return Array.from(galleryImages.values())
}

function getResolvedRatingSummary(
  book: Book,
  ratingSummary: BookRatingSummary | null,
): BookRatingSummary {
  if (ratingSummary) {
    return ratingSummary
  }

  return {
    averageRating: typeof book.rating === 'number' ? book.rating : 0,
    reviewCount: book.reviews ?? 0,
    starBreakdown: book.starBreakdown,
  }
}

function getPromotionDiscountLabel(
  promotion: BookPromotion,
  formatCurrency: (value: number) => string,
  formatNumber: (value: number) => string,
) {
  if (promotion.discountType.toUpperCase().includes('PERCENT')) {
    return `${formatNumber(promotion.discountValue)}%`
  }

  return formatCurrency(promotion.discountValue)
}

function getPromotionMinOrderLabel(
  promotion: BookPromotion,
  t: TranslateFunction,
  formatCurrency: (value: number) => string,
) {
  if (typeof promotion.minOrderAmount === 'number') {
    return t('book.detail.promotionMinOrder', {
      amount: formatCurrency(promotion.minOrderAmount),
    })
  }

  return t('book.detail.promotionNoMinOrder')
}

function getAuthorLifeLabel(author: AuthorResponse) {
  const birthYear = author.birthYear?.toString() ?? ''
  const deathYear = author.deathYear?.toString() ?? ''

  if (!birthYear && !deathYear) {
    return ''
  }

  return `(${birthYear || '?'}-${deathYear || '?'})`
}

function createTextDetailItem(
  label: string,
  value: string | null | undefined,
  fallbackValue: string,
): DetailItem {
  const normalizedValue = value?.trim()
  return {
    label,
    value: normalizedValue || fallbackValue,
  }
}

function createNumberDetailItem(
  label: string,
  value: number | null | undefined,
  formatValue: (value: number) => string,
  fallbackValue: string,
): DetailItem {
  const resolvedValue =
    typeof value === 'number' ? formatValue(value) : fallbackValue

  return {
    label,
    value: resolvedValue,
  }
}

function toBookCardData(book: Book): BookCardData {
  return {
    id: book.id,
    title: book.title,
    author: book.author,
    category: book.category,
    price: book.price,
    cover: book.cover,
    oldPrice: book.oldPrice,
    rating: book.rating,
    reviews: book.reviews,
    bestseller: book.bestseller,
  }
}
