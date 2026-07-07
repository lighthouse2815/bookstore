import { useState, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import {
  BookOpen,
  CheckCircle2,
  Download,
  ExternalLink,
  Eye,
  Search,
  ShoppingBag,
} from 'lucide-react'
import { toast } from 'sonner'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/common/select'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useDigitalLibraryPage } from '@/hooks/use-digital-library-page'
import { getPublishedDigitalAssetSampleUrl } from '@/services/digital-library-service'
import type {
  DigitalAccessStatus,
  DigitalAssetFormat,
} from '@/types/digital-library'
import { cn, getErrorMessage } from '@/utils'
import { getBookCoverUrl } from '@/utils/book-cover'

const FORMAT_OPTIONS: Array<DigitalAssetFormat | 'all'> = [
  'all',
  'PDF',
  'EPUB',
  'AUDIO',
]

const STATUS_OPTIONS: Array<DigitalAccessStatus | 'all'> = [
  'all',
  'ACTIVE',
  'EXPIRED',
  'REVOKED',
]

export default function DigitalLibraryPage() {
  const { formatCurrency, formatDate, formatNumber, t } = useLanguage()
  const {
    items,
    filteredItems,
    isLoading,
    isLoadingMore,
    error,
    totalCount,
    hasNext,
    searchTerm,
    selectedFormat,
    selectedStatus,
    setSearchTerm,
    setSelectedFormat,
    setSelectedStatus,
    loadMore,
  } = useDigitalLibraryPage()
  const [openingSampleAssetId, setOpeningSampleAssetId] = useState<string | null>(
    null,
  )

  async function handleOpenSample(bookId: string, digitalAssetId: string) {
    if (typeof window === 'undefined') {
      return
    }

    const pendingWindow = window.open('', '_blank')
    setOpeningSampleAssetId(digitalAssetId)

    try {
      const signedUrl = await getPublishedDigitalAssetSampleUrl(bookId, digitalAssetId)

      if (pendingWindow) {
        pendingWindow.location.href = signedUrl.url
      } else {
        window.location.assign(signedUrl.url)
      }
    } catch (currentError) {
      pendingWindow?.close()
      toast.error(getErrorMessage(currentError, t('library.page.sampleError')))
    } finally {
      setOpeningSampleAssetId(null)
    }
  }

  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(249,247,255,1)_0%,rgba(244,239,255,0.94)_46%,rgba(255,255,255,1)_100%)] dark:bg-[linear-gradient(180deg,rgba(24,20,38,1)_0%,rgba(18,16,29,0.98)_46%,rgba(13,12,21,1)_100%)]">
      <Header />

      <main className="flex-1 pb-16 pt-6 sm:pb-20 sm:pt-8">
        <div className="mx-auto flex w-full max-w-[1320px] flex-col gap-6 px-4 sm:px-6 lg:px-8">
          <section className="overflow-hidden rounded-[34px] border border-primary/10 bg-card/90 px-6 py-7 text-card-foreground shadow-[0_24px_80px_rgba(109,76,255,0.1)] backdrop-blur dark:border-white/10 dark:bg-card/88 dark:shadow-[0_24px_80px_rgba(0,0,0,0.35)] sm:px-8 lg:px-10">
            <div className="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
              <div className="flex min-w-0 items-start gap-4">
                <span className="flex size-[72px] shrink-0 items-center justify-center rounded-[24px] bg-primary/10 text-primary shadow-[0_18px_40px_rgba(109,76,255,0.12)]">
                  <BookOpen className="h-9 w-9" strokeWidth={1.8} />
                </span>
                <div className="min-w-0">
                  <h1 className="font-heading text-4xl font-bold tracking-tight text-foreground">
                    {t('library.page.title')}
                  </h1>
                  <p className="mt-2 max-w-2xl text-[1rem] leading-7 text-muted-foreground">
                    {t('library.page.description')}
                  </p>
                </div>
              </div>

              <div className="rounded-[24px] border border-primary/10 bg-primary/5 px-5 py-4 dark:border-primary/20 dark:bg-primary/10">
                <p className="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                  {t('library.page.countLabel')}
                </p>
                <p className="mt-2 font-heading text-3xl font-bold text-primary">
                  {formatNumber(filteredItems.length)}
                </p>
                <p className="mt-1 text-xs text-muted-foreground">
                  {t('library.page.totalOwnedLabel', { count: formatNumber(totalCount) })}
                </p>
              </div>
            </div>
          </section>

          <section className="rounded-[30px] border border-primary/10 bg-card/90 p-5 shadow-[0_18px_50px_rgba(109,76,255,0.08)] backdrop-blur dark:border-white/10 dark:bg-card/88 dark:shadow-[0_18px_50px_rgba(0,0,0,0.28)]">
            <div className="grid gap-4 lg:grid-cols-[minmax(0,1.4fr)_220px_220px]">
              <div className="relative">
                <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                <Input
                  value={searchTerm}
                  onChange={(event) => setSearchTerm(event.currentTarget.value)}
                  placeholder={t('library.page.searchPlaceholder')}
                  className="h-12 rounded-2xl border-primary/10 bg-background/80 pl-12 dark:bg-input/40"
                />
              </div>

              <Select
                value={selectedFormat}
                onValueChange={(value) =>
                  setSelectedFormat((value as DigitalAssetFormat | 'all') ?? 'all')
                }
              >
                <SelectTrigger className="h-12 rounded-2xl border-primary/10 bg-background/80 dark:bg-input/40">
                  <SelectValue placeholder={t('library.page.formatFilterLabel')}>
                    {selectedFormat === 'all'
                      ? t('library.page.allFormats')
                      : selectedFormat}
                  </SelectValue>
                </SelectTrigger>
                <SelectContent>
                  {FORMAT_OPTIONS.map((format) => (
                    <SelectItem key={format} value={format}>
                      {format === 'all' ? t('library.page.allFormats') : format}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>

              <Select
                value={selectedStatus}
                onValueChange={(value) =>
                  setSelectedStatus((value as DigitalAccessStatus | 'all') ?? 'all')
                }
              >
                <SelectTrigger className="h-12 rounded-2xl border-primary/10 bg-background/80 dark:bg-input/40">
                  <SelectValue placeholder={t('library.page.statusFilterLabel')}>
                    {selectedStatus === 'all'
                      ? t('library.page.allStatuses')
                      : selectedStatus}
                  </SelectValue>
                </SelectTrigger>
                <SelectContent>
                  {STATUS_OPTIONS.map((status) => (
                    <SelectItem key={status} value={status}>
                      {status === 'all' ? t('library.page.allStatuses') : status}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </section>

          {isLoading ? (
            <StatePanel>{t('library.page.loading')}</StatePanel>
          ) : error ? (
            <StatePanel tone="error">{error}</StatePanel>
          ) : filteredItems.length === 0 ? (
            items.length === 0 ? (
              <EmptyLibraryGuide t={t} />
            ) : (
              <StatePanel>
                <div>
                  <p className="font-semibold text-foreground">{t('library.page.filterEmptyTitle')}</p>
                  <p className="mt-2 text-sm text-muted-foreground">{t('library.page.filterEmptyDescription')}</p>
                </div>
              </StatePanel>
            )
          ) : (
            <>
              <div className="grid gap-5 lg:grid-cols-2">
                {filteredItems.map((item) => {
                  const isOpeningSample = openingSampleAssetId === item.digitalAssetId

                  return (
                    <article
                      key={item.digitalAssetId}
                      className="overflow-hidden rounded-[30px] border border-primary/10 bg-card/92 text-card-foreground shadow-[0_18px_50px_rgba(109,76,255,0.08)] dark:border-white/10 dark:bg-card/88 dark:shadow-[0_18px_50px_rgba(0,0,0,0.28)]"
                    >
                      <div className="grid gap-5 p-5 sm:grid-cols-[132px_minmax(0,1fr)]">
                        <div className="overflow-hidden rounded-[24px] border border-primary/10 bg-muted/50 dark:border-white/10">
                          <img
                            src={getBookCoverUrl(item.bookImageUrl)}
                            alt={item.bookTitle}
                            className="aspect-[3/4] w-full object-cover"
                          />
                        </div>

                        <div className="min-w-0">
                          <div className="flex flex-wrap items-start justify-between gap-3">
                            <div className="min-w-0">
                              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                                {t('library.page.bookLabel')}
                              </p>
                              <h2 className="mt-2 line-clamp-2 font-heading text-2xl font-bold text-foreground">
                                {item.bookTitle}
                              </h2>
                              <p className="mt-2 line-clamp-2 text-sm font-medium text-muted-foreground">
                                {item.assetTitle}
                              </p>
                            </div>

                            <Badge
                              variant="outline"
                              className={cn(
                                'rounded-full px-3 py-1 text-xs font-semibold',
                                getAccessStatusClassName(item.accessStatus),
                              )}
                            >
                              {item.accessStatus}
                            </Badge>
                          </div>

                          <div className="mt-4 flex flex-wrap gap-2">
                            <Pill>{item.format}</Pill>
                            <Pill>{item.accessType}</Pill>
                            {item.downloadAllowed ? (
                              <Pill>{t('library.page.downloadAllowedLabel')}</Pill>
                            ) : null}
                            {item.sampleAvailable ? (
                              <Pill>{t('library.page.sampleAvailableLabel')}</Pill>
                            ) : null}
                          </div>

                          <dl className="mt-5 grid gap-3 sm:grid-cols-2">
                            <MetaItem
                              label={t('library.page.priceLabel')}
                              value={formatCurrency(item.price)}
                            />
                            <MetaItem
                              label={t('library.page.acquiredAtLabel')}
                              value={formatDate(item.acquiredAt)}
                            />
                            <MetaItem
                              label={t('library.page.progressLabel')}
                              value={
                                item.progress
                                  ? `${item.progress.progressPercent}%`
                                  : t('library.page.noProgress')
                              }
                            />
                            <MetaItem
                              label={t('library.page.expiresAtLabel')}
                              value={
                                item.expiresAt ? formatDate(item.expiresAt) : t('library.page.noExpiry')
                              }
                            />
                          </dl>

                          <div className="mt-5">
                            <div className="h-2 overflow-hidden rounded-full bg-primary/8">
                              <div
                                className="h-full rounded-full bg-primary"
                                style={{
                                  width: `${Math.max(
                                    0,
                                    Math.min(item.progress?.progressPercent ?? 0, 100),
                                  )}%`,
                                }}
                              />
                            </div>
                          </div>

                          <div className="mt-5 flex flex-wrap gap-3">
                            <Link to={`/library/${item.digitalAssetId}/read`}>
                              <Button className="rounded-2xl">
                                <Eye className="mr-2 h-4 w-4" />
                                {t('library.page.readNowLabel')}
                              </Button>
                            </Link>

                            <Link to={`/library/${item.digitalAssetId}`}>
                              <Button variant="outline" className="rounded-2xl">
                                {t('library.page.viewDetailLabel')}
                              </Button>
                            </Link>

                            {item.sampleAvailable ? (
                              <Button
                                type="button"
                                variant="outline"
                                className="rounded-2xl"
                                disabled={isOpeningSample}
                                onClick={() =>
                                  void handleOpenSample(item.bookId, item.digitalAssetId)
                                }
                              >
                                <ExternalLink className="mr-2 h-4 w-4" />
                                {isOpeningSample
                                  ? t('library.page.openingSampleLabel')
                                  : t('library.page.openSampleLabel')}
                              </Button>
                            ) : null}

                            {item.downloadAllowed ? (
                              <Badge
                                variant="outline"
                                className="rounded-2xl border-primary/10 bg-primary/5 px-3 py-2 text-muted-foreground dark:border-primary/20 dark:bg-primary/10"
                              >
                                <Download className="mr-2 h-4 w-4" />
                                {t('library.page.downloadReadyBadge')}
                              </Badge>
                            ) : null}
                          </div>
                        </div>
                      </div>
                    </article>
                  )
                })}
              </div>

              {hasNext ? (
                <div className="flex justify-center">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => void loadMore()}
                    disabled={isLoadingMore}
                    className="rounded-2xl"
                  >
                    {isLoadingMore ? t('library.page.loading') : t('library.page.loadMore')}
                  </Button>
                </div>
              ) : null}
            </>
          )}
        </div>
      </main>

      <Footer />
    </div>
  )
}

type LibraryTranslate = (
  key: string,
  params?: Record<string, number | string>,
) => string

function EmptyLibraryGuide({ t }: { t: LibraryTranslate }) {
  const steps = [
    t('library.page.emptySteps.findBook'),
    t('library.page.emptySteps.buyDigital'),
    t('library.page.emptySteps.readHere'),
  ]

  return (
    <section className="overflow-hidden rounded-[34px] border border-primary/10 bg-card/90 p-6 text-left text-card-foreground shadow-[0_24px_80px_rgba(109,76,255,0.1)] backdrop-blur dark:border-white/10 dark:bg-card/88 dark:shadow-[0_24px_80px_rgba(0,0,0,0.35)]">
      <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_340px] lg:items-center">
        <div>
          <span className="inline-flex size-14 items-center justify-center rounded-[22px] bg-primary/10 text-primary">
            <ShoppingBag className="h-6 w-6" />
          </span>
          <h2 className="mt-5 font-heading text-3xl font-bold text-foreground">
            {t('library.page.emptyOwnedTitle')}
          </h2>
          <p className="mt-3 max-w-2xl text-sm leading-7 text-muted-foreground">
            {t('library.page.emptyOwnedDescription')}
          </p>
          <div className="mt-5">
            <Link to="/books">
              <Button className="rounded-2xl">
                {t('library.page.emptyOwnedAction')}
              </Button>
            </Link>
          </div>
        </div>

        <div className="rounded-[26px] border border-primary/10 bg-primary/5 p-5 dark:border-primary/20 dark:bg-primary/10">
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
            {t('library.page.emptyGuideLabel')}
          </p>
          <div className="mt-4 space-y-3">
            {steps.map((step, index) => (
              <div
                key={step}
                className="flex gap-3 rounded-2xl border border-primary/8 bg-background/70 p-3 text-sm leading-6 text-muted-foreground dark:border-white/10 dark:bg-background/35"
              >
                <CheckCircle2 className="mt-1 h-4 w-4 shrink-0 text-emerald-500" />
                <span>
                  <span className="font-semibold text-foreground">
                    {index + 1}.{' '}
                  </span>
                  {step}
                </span>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  )
}

function StatePanel({
  children,
  tone = 'default',
}: {
  children: ReactNode
  tone?: 'default' | 'error'
}) {
  return (
    <section
      className={cn(
        'rounded-[30px] border p-8 text-center shadow-[0_18px_50px_rgba(109,76,255,0.08)]',
        tone === 'default'
          ? 'border-primary/10 bg-card/90 text-muted-foreground dark:border-white/10 dark:bg-card/88'
          : 'border-destructive/20 bg-destructive/5 text-destructive',
      )}
    >
      {children}
    </section>
  )
}

function MetaItem({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-[20px] border border-primary/8 bg-primary/4 px-4 py-3">
      <dt className="text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
        {label}
      </dt>
      <dd className="mt-2 text-sm font-semibold text-foreground">{value}</dd>
    </div>
  )
}

function Pill({ children }: { children: ReactNode }) {
  return (
    <span className="inline-flex items-center rounded-full border border-primary/10 bg-primary/5 px-3 py-1 text-xs font-semibold text-primary">
      {children}
    </span>
  )
}

function getAccessStatusClassName(status: DigitalAccessStatus) {
  switch (status) {
    case 'ACTIVE':
      return 'border-emerald-500/25 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300'
    case 'EXPIRED':
      return 'border-amber-500/25 bg-amber-500/10 text-amber-700 dark:text-amber-300'
    case 'REVOKED':
      return 'border-rose-500/25 bg-rose-500/10 text-rose-700 dark:text-rose-300'
    default:
      return 'border-primary/10 bg-primary/5 text-primary'
  }
}
