import { useState, type ReactNode } from 'react'
import { Link, useParams } from 'react-router-dom'
import { toast } from 'sonner'
import {
  ArrowLeft,
  BookOpenText,
  ChevronRight,
  ExternalLink,
  Headphones,
  ShieldCheck,
  Sparkles,
  Star,
} from 'lucide-react'
import { Button } from '@/components/common/button'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useCart } from '@/contexts/cart-context'
import { useLanguage } from '@/contexts/language-context'
import { useBookDetail } from '@/hooks/use-book-detail'
import NotFoundPage from '@/pages/home/not-found'
import { getPublishedDigitalAssetSampleUrl } from '@/services/digital-library-service'
import type { Book, BookRatingSummary } from '@/types/book'
import { getErrorMessage } from '@/utils'
import { getBookCoverUrl, setBookCoverFallback } from '@/utils/book-cover'
import { getCategoryLabel } from '@/utils/i18n'

type TranslateFunction = (
  key: string,
  params?: Record<string, number | string>,
) => string

export default function BookEbookPage() {
  const { id } = useParams<{ id: string }>()
  const { t, language, formatCurrency, formatNumber } = useLanguage()
  const { addDigitalItem } = useCart()
  const {
    book,
    categoryTrail,
    ratingSummary,
    digitalAssets,
    isLoading,
    error,
    notFound,
  } = useBookDetail(id)
  const [openingSampleAssetId, setOpeningSampleAssetId] = useState<string | null>(
    null,
  )
  const [addingDigitalAssetId, setAddingDigitalAssetId] = useState<string | null>(
    null,
  )

  async function handleOpenSample(digitalAssetId: string) {
    if (!book || typeof window === 'undefined') {
      return
    }

    const pendingWindow = window.open('', '_blank')
    setOpeningSampleAssetId(digitalAssetId)

    try {
      const signedUrl = await getPublishedDigitalAssetSampleUrl(book.id, digitalAssetId)

      if (pendingWindow) {
        pendingWindow.location.href = signedUrl.url
      } else {
        window.location.assign(signedUrl.url)
      }
    } catch (currentError) {
      pendingWindow?.close()
      toast.error(getErrorMessage(currentError, t('checkout.error')))
    } finally {
      setOpeningSampleAssetId(null)
    }
  }

  async function handleAddDigitalAsset(digitalAssetId: string) {
    setAddingDigitalAssetId(digitalAssetId)

    try {
      await addDigitalItem(digitalAssetId)
      toast.success(t('book.detail.digitalAssets.addedToCart'))
    } catch (currentError) {
      toast.error(
        getErrorMessage(currentError, t('book.detail.digitalAssets.addToCartError')),
      )
    } finally {
      setAddingDigitalAssetId(null)
    }
  }

  if (isLoading) {
    return (
      <div className="flex min-h-screen flex-col">
        <Header />
        <main className="mx-auto flex w-full max-w-7xl flex-1 items-center justify-center px-4 py-6 sm:px-6 lg:px-8">
          <div className="rounded-2xl border border-dashed border-border px-8 py-12 text-center">
            <p className="font-heading text-lg font-semibold">{t('common.loading')}</p>
          </div>
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
          <div className="rounded-2xl border border-dashed border-border px-8 py-12 text-center">
            <p className="font-heading text-lg font-semibold">
              {t('book.listing.errorTitle')}
            </p>
            <p className="mt-2 max-w-xl text-sm text-muted-foreground">{error}</p>
          </div>
        </main>
        <Footer />
      </div>
    )
  }

  if (notFound || !book) {
    return <NotFoundPage />
  }

  const resolvedRatingSummary = getResolvedRatingSummary(book, ratingSummary)
  const averageRating = resolvedRatingSummary.averageRating
  const reviewCount = resolvedRatingSummary.reviewCount
  const coverSrc = getBookCoverUrl(book.cover)
  const purchasableDigitalAssets = digitalAssets.filter((asset) => asset.purchaseAllowed)
  const digitalAssetFormats = Array.from(
    new Set(digitalAssets.map((asset) => asset.format)),
  )
  const digitalAssetFormatsLabel =
    digitalAssetFormats.length > 0 ? digitalAssetFormats.join(' / ') : '--'
  const purchasableDigitalAssetCount = purchasableDigitalAssets.length
  const digitalStartingPrice = purchasableDigitalAssets.reduce<number | null>(
    (currentPrice, asset) =>
      currentPrice === null ? asset.price : Math.min(currentPrice, asset.price),
    null,
  )
  const digitalStartingPriceLabel =
    digitalStartingPrice === null
      ? t('book.detail.digitalAssets.purchaseDisabled')
      : formatCurrency(digitalStartingPrice)
  const sampleDigitalAssetCount = digitalAssets.filter(
    (asset) => asset.sampleAvailable,
  ).length

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
              <span>{getCategoryLabel(category, language)}</span>
            </div>
          ))}
          <ChevronRight className="size-4" />
          <Link to={`/books/${book.id}`} className="hover:text-primary">
            {book.title}
          </Link>
          <ChevronRight className="size-4" />
          <span className="text-foreground">{t('book.detail.digitalAssets.title')}</span>
        </nav>

        <section className="grid gap-6 xl:grid-cols-[320px_minmax(0,1fr)_300px]">
          <aside className="space-y-4">
            <div className="overflow-hidden rounded-[2rem] border border-border bg-card shadow-[0_36px_90px_-48px_hsl(var(--foreground)/0.45)]">
              <div className="relative aspect-[3/4]">
                <img
                  src={coverSrc}
                  alt={t('book.card.coverAlt', { title: book.title })}
                  onError={(event) => setBookCoverFallback(event.currentTarget)}
                  className="absolute inset-0 size-full object-cover"
                />
              </div>
            </div>

            <div className="rounded-[2rem] border border-border bg-card p-5 shadow-sm">
              <span className="text-sm font-semibold uppercase tracking-[0.18em] text-primary">
                {getCategoryLabel(book.categoryInfo ?? book.category, language)}
              </span>
              <h1 className="mt-3 font-heading text-2xl font-bold tracking-tight text-balance text-foreground">
                {book.title}
              </h1>
              <div className="mt-3 flex flex-wrap items-center gap-x-3 gap-y-2 text-sm text-muted-foreground">
                <span>
                  {t('book.detail.author')}{' '}
                  <span className="font-semibold text-foreground">{book.author}</span>
                </span>
                <span className="hidden h-4 w-px bg-border sm:block" />
                <span>
                  {t('book.detail.specPublisher')}{' '}
                  <span className="font-semibold text-foreground">{book.publisher}</span>
                </span>
              </div>

              <div className="mt-5 flex flex-wrap items-center gap-x-4 gap-y-2 border-y border-border py-4 text-sm">
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
              </div>

              <div className="mt-5 grid gap-3">
                <Link
                  to={`/books/${book.id}`}
                  className="inline-flex items-center gap-2 rounded-2xl border border-border bg-background px-4 py-3 text-sm font-semibold transition-colors hover:bg-muted"
                >
                  <ArrowLeft className="size-4" />
                  {t('book.detail.digitalAssets.viewPhysicalBook')}
                </Link>
                <Link
                  to="/ebooks"
                  className="inline-flex items-center justify-center rounded-2xl border border-primary/20 bg-primary/8 px-4 py-3 text-sm font-semibold text-primary transition-colors hover:bg-primary/12"
                >
                  {t('book.detail.digitalAssets.browseCatalog')}
                </Link>
              </div>
            </div>
          </aside>

          <section className="space-y-6">
            <div className="overflow-hidden rounded-[2rem] border border-border/70 bg-[radial-gradient(circle_at_top_left,hsl(var(--primary)/0.14),transparent_42%),linear-gradient(135deg,hsl(var(--background)),hsl(var(--card)))] p-6 shadow-[0_24px_70px_rgba(15,23,42,0.08)] lg:p-8">
              <span className="inline-flex items-center gap-2 rounded-full border border-primary/15 bg-primary/8 px-3 py-1.5 text-xs font-semibold uppercase tracking-[0.18em] text-primary">
                <Sparkles className="size-3.5" />
                {t('book.detail.digitalAssets.heroBadge')}
              </span>

              <div className="mt-5 max-w-4xl">
                <h2 className="font-heading text-3xl font-bold tracking-tight text-balance text-foreground lg:text-4xl">
                  {book.title}
                </h2>
                <p className="mt-3 text-sm leading-7 text-muted-foreground">
                  {t('book.detail.digitalAssets.calloutDescription', {
                    formats: digitalAssetFormatsLabel,
                    price: digitalStartingPriceLabel,
                  })}
                </p>
                {book.description ? (
                  <p className="mt-4 text-sm leading-7 text-muted-foreground">
                    {book.description}
                  </p>
                ) : null}
              </div>

              <div className="mt-5 flex flex-wrap gap-2">
                <InfoPill>
                  {t('book.detail.digitalAssets.formatSummary', {
                    formats: digitalAssetFormatsLabel,
                  })}
                </InfoPill>
                <InfoPill>
                  {t('book.detail.digitalAssets.priceSummary', {
                    price: digitalStartingPriceLabel,
                  })}
                </InfoPill>
                <InfoPill>
                  {t('book.detail.digitalAssets.editionCountSummary', {
                    count: formatNumber(digitalAssets.length),
                  })}
                </InfoPill>
                {sampleDigitalAssetCount > 0 ? (
                  <InfoPill>
                    {t('book.detail.digitalAssets.sampleCallout', {
                      count: formatNumber(sampleDigitalAssetCount),
                    })}
                  </InfoPill>
                ) : null}
              </div>

              <div className="mt-6 grid gap-4 md:grid-cols-3">
                <StatCard
                  title={t('book.detail.digitalAssets.priceLabel')}
                  value={digitalStartingPriceLabel}
                  description={t('book.detail.digitalAssets.summaryDescription')}
                />
                <StatCard
                  title={t('book.detail.digitalAssets.editionCountLabel')}
                  value={formatNumber(purchasableDigitalAssetCount)}
                  description={t('book.detail.digitalAssets.editionCountSummary', {
                    count: formatNumber(purchasableDigitalAssetCount),
                  })}
                />
                <StatCard
                  title={t('book.detail.digitalAssets.sampleCountLabel')}
                  value={formatNumber(sampleDigitalAssetCount)}
                  description={
                    sampleDigitalAssetCount > 0
                      ? t('book.detail.digitalAssets.sampleCallout', {
                          count: formatNumber(sampleDigitalAssetCount),
                        })
                      : t('book.detail.digitalAssets.noSample')
                  }
                />
              </div>
            </div>

            {digitalAssets.length > 0 ? (
              <section id="ebook-options" className="space-y-4">
                <div className="rounded-[1.75rem] border border-border bg-card p-5 shadow-sm">
                  <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                    <div className="max-w-3xl">
                      <h2 className="font-heading text-2xl font-bold tracking-tight text-foreground">
                        {t('book.detail.digitalAssets.availableFormatsTitle')}
                      </h2>
                      <p className="mt-2 text-sm leading-6 text-muted-foreground">
                        {t('book.detail.digitalAssets.availableFormatsDescription')}
                      </p>
                    </div>

                    <div className="rounded-[1.5rem] border border-primary/15 bg-primary/5 p-4 lg:max-w-sm">
                      <div className="flex gap-3">
                        <ShieldCheck className="mt-0.5 h-5 w-5 shrink-0 text-primary" />
                        <div>
                          <p className="text-sm font-semibold text-foreground">
                            {t('book.detail.digitalAssets.accessNoteTitle')}
                          </p>
                          <p className="mt-1 text-sm leading-6 text-muted-foreground">
                            {t('book.detail.digitalAssets.accessNoteDescription')}
                          </p>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>

                {digitalAssets.map((asset) => {
                  const isOpeningSample = openingSampleAssetId === asset.id
                  const isAddingToCart = addingDigitalAssetId === asset.id

                  return (
                    <article
                      key={asset.id}
                      className="rounded-[1.75rem] border border-border bg-card p-5 shadow-sm"
                    >
                      <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_240px]">
                        <div className="min-w-0">
                          <div className="flex items-start gap-3">
                            <span className="mt-0.5 flex size-11 shrink-0 items-center justify-center rounded-2xl bg-primary/12 text-primary">
                              {asset.format === 'AUDIO' ? (
                                <Headphones className="size-5" />
                              ) : (
                                <BookOpenText className="size-5" />
                              )}
                            </span>

                            <div className="min-w-0 flex-1">
                              <div className="flex flex-wrap items-center gap-2">
                                <h3 className="font-heading text-xl font-bold tracking-tight text-foreground">
                                  {asset.title}
                                </h3>
                                <span className="inline-flex items-center rounded-full border border-primary/20 bg-primary/10 px-3 py-1 text-xs font-semibold uppercase tracking-[0.12em] text-primary">
                                  {asset.format}
                                </span>
                              </div>
                              <p className="mt-1 break-all text-sm text-muted-foreground">
                                {asset.fileName}
                              </p>
                            </div>
                          </div>

                          <div className="mt-4 flex flex-wrap gap-2">
                            <InfoPill>
                              {asset.purchaseAllowed
                                ? t('book.detail.digitalAssets.purchaseAvailable')
                                : t('book.detail.digitalAssets.purchaseDisabled')}
                            </InfoPill>
                            <InfoPill>
                              {asset.sampleAvailable
                                ? t('book.detail.digitalAssets.sampleAvailable')
                                : t('book.detail.digitalAssets.noSample')}
                            </InfoPill>
                            <InfoPill>
                              {asset.downloadAllowed
                                ? t('book.detail.digitalAssets.downloadAllowed')
                                : t('book.detail.digitalAssets.downloadRestricted')}
                            </InfoPill>
                          </div>

                          <div className="mt-5 grid gap-3 sm:grid-cols-2">
                            <DigitalMetaItem
                              label={t('book.detail.digitalAssets.formatLabel')}
                              value={asset.format}
                            />
                            <DigitalMetaItem
                              label={t('book.detail.digitalAssets.downloadLabel')}
                              value={
                                asset.downloadAllowed
                                  ? t('book.detail.digitalAssets.downloadAllowed')
                                  : t(
                                      'book.detail.digitalAssets.downloadRestricted',
                                    )
                              }
                            />
                            <DigitalMetaItem
                              label={t('book.detail.digitalAssets.sampleLabel')}
                              value={
                                asset.sampleAvailable
                                  ? t('book.detail.digitalAssets.sampleAvailable')
                                  : t('book.detail.digitalAssets.noSample')
                              }
                            />
                            <DigitalMetaItem
                              label={t('book.detail.digitalAssets.purchaseLabel')}
                              value={
                                asset.purchaseAllowed
                                  ? t('book.detail.digitalAssets.purchaseAvailable')
                                  : t(
                                      'book.detail.digitalAssets.purchaseUnavailable',
                                    )
                              }
                            />
                          </div>
                        </div>

                        <div className="flex flex-col justify-between gap-4 rounded-[1.5rem] border border-border/70 bg-background/85 p-4">
                          <div>
                            <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
                              {t('book.detail.digitalAssets.priceLabel')}
                            </p>
                            <p className="mt-2 font-heading text-3xl font-bold tabular-nums text-primary">
                              {formatCurrency(asset.price)}
                            </p>
                            <p className="mt-2 text-sm leading-6 text-muted-foreground">
                              {asset.purchaseAllowed
                                ? t('book.detail.digitalAssets.purchaseAvailable')
                                : t('book.detail.digitalAssets.purchaseDisabled')}
                            </p>
                          </div>

                          <div className="flex flex-col gap-3">
                            {asset.sampleAvailable ? (
                              <Button
                                type="button"
                                variant="outline"
                                className="w-full rounded-2xl"
                                disabled={isOpeningSample}
                                onClick={() => void handleOpenSample(asset.id)}
                              >
                                <ExternalLink className="mr-2 h-4 w-4" />
                                {t('book.detail.digitalAssets.openSample')}
                              </Button>
                            ) : null}

                            <Button
                              type="button"
                              className="w-full rounded-2xl"
                              disabled={!asset.purchaseAllowed || isAddingToCart}
                              onClick={() => void handleAddDigitalAsset(asset.id)}
                            >
                              {isAddingToCart
                                ? t('book.detail.digitalAssets.addingToCart')
                                : asset.purchaseAllowed
                                  ? t('book.detail.digitalAssets.addToCart')
                                  : t('book.detail.digitalAssets.purchaseDisabled')}
                            </Button>
                          </div>
                        </div>
                      </div>
                    </article>
                  )
                })}
              </section>
            ) : (
              <div className="rounded-[2rem] border border-dashed border-border bg-card px-6 py-12 text-center shadow-sm">
                <p className="font-heading text-lg font-semibold">
                  {t('home.emptyTitle')}
                </p>
                <p className="mt-2 text-sm text-muted-foreground">
                  {t('book.detail.digitalAssets.teaserDescription')}
                </p>
              </div>
            )}
          </section>

          <aside className="xl:sticky xl:top-24 xl:self-start">
            <div className="rounded-[2rem] border border-border/70 bg-card p-5 shadow-[0_22px_60px_rgba(15,23,42,0.08)]">
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-primary">
                {t('book.detail.digitalAssets.summaryTitle')}
              </p>
              <p className="mt-3 font-heading text-4xl font-bold tracking-tight text-primary">
                {digitalStartingPriceLabel}
              </p>
              <p className="mt-3 text-sm leading-6 text-muted-foreground">
                {t('book.detail.digitalAssets.summaryDescription')}
              </p>

              <div className="mt-5 space-y-3 rounded-[1.5rem] border border-border/70 bg-background/80 p-4">
                <SummaryRow
                  label={t('book.detail.digitalAssets.formatLabel')}
                  value={digitalAssetFormatsLabel}
                />
                <SummaryRow
                  label={t('book.detail.digitalAssets.editionCountLabel')}
                  value={formatNumber(purchasableDigitalAssetCount)}
                />
                <SummaryRow
                  label={t('book.detail.digitalAssets.sampleCountLabel')}
                  value={formatNumber(sampleDigitalAssetCount)}
                />
              </div>

              <div className="mt-5 rounded-[1.5rem] border border-primary/15 bg-primary/5 p-4">
                <div className="flex gap-3">
                  <ShieldCheck className="mt-0.5 h-5 w-5 shrink-0 text-primary" />
                  <div>
                    <p className="text-sm font-semibold text-foreground">
                      {t('book.detail.digitalAssets.accessNoteTitle')}
                    </p>
                    <p className="mt-1 text-sm leading-6 text-muted-foreground">
                      {t('book.detail.digitalAssets.accessNoteDescription')}
                    </p>
                  </div>
                </div>
              </div>

              <div className="mt-5 flex flex-col gap-3">
                <a
                  href="#ebook-options"
                  className="inline-flex items-center justify-center rounded-2xl bg-primary px-4 py-3 text-sm font-semibold text-primary-foreground transition-opacity hover:opacity-90"
                >
                  {t('book.detail.digitalAssets.viewOptions')}
                </a>
                <Link
                  to="/ebooks"
                  className="inline-flex items-center justify-center rounded-2xl border border-border bg-background px-4 py-3 text-sm font-semibold transition-colors hover:bg-muted"
                >
                  {t('book.detail.digitalAssets.browseCatalog')}
                </Link>
              </div>
            </div>
          </aside>
        </section>
      </main>
      <Footer />
    </div>
  )
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

function DigitalMetaItem({
  label,
  value,
}: {
  label: string
  value: string
}) {
  return (
    <div className="rounded-2xl border border-border/70 bg-background/80 px-4 py-3">
      <p className="text-[11px] font-semibold uppercase tracking-[0.14em] text-muted-foreground">
        {label}
      </p>
      <p className="mt-2 text-sm font-semibold leading-6 text-foreground">
        {value}
      </p>
    </div>
  )
}

function StatCard({
  title,
  value,
  description,
}: {
  title: string
  value: string
  description: string
}) {
  return (
    <div className="rounded-[1.5rem] border border-border/70 bg-card px-5 py-4 shadow-sm">
      <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-muted-foreground">
        {title}
      </p>
      <p className="mt-2 font-heading text-3xl font-bold text-foreground">
        {value}
      </p>
      <p className="mt-2 text-sm leading-6 text-muted-foreground">{description}</p>
    </div>
  )
}

function SummaryRow({
  label,
  value,
}: {
  label: string
  value: string
}) {
  return (
    <div className="flex items-center justify-between gap-3">
      <span className="text-sm text-muted-foreground">{label}</span>
      <span className="text-sm font-semibold text-foreground">{value}</span>
    </div>
  )
}

function InfoPill({
  children,
}: {
  children: ReactNode
}) {
  return (
    <span className="inline-flex items-center rounded-2xl border border-border bg-background px-3.5 py-2 text-xs font-semibold text-muted-foreground">
      {children}
    </span>
  )
}
