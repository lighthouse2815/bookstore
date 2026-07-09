import { type ReactNode } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ArrowLeft, Download, ExternalLink, PencilLine, Save } from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { Textarea } from '@/components/common/textarea'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useDigitalLibraryDetailPage } from '@/hooks/use-digital-library-detail-page'
import NotFoundPage from '@/pages/home/not-found'
import { cn } from '@/utils'
import { getBookCoverUrl } from '@/utils/book-cover'
import { formatDigitalFileSize } from '@/utils/digital-asset'

export default function DigitalLibraryDetailPage() {
  const { digitalAssetId } = useParams<{ digitalAssetId: string }>()
  const { formatCurrency, formatDate, t } = useLanguage()
  const {
    asset,
    isLoading,
    error,
    notFound,
    isSavingProgress,
    isResolvingDownloadUrl,
    isResolvingSampleUrl,
    progressForm,
    handleProgressFieldChange,
    submitProgress,
    openSampleAsset,
    downloadAsset,
  } = useDigitalLibraryDetailPage(digitalAssetId)
  const sampleButtonLabel = isResolvingSampleUrl
    ? t('library.detail.openingSampleLabel')
    : t('library.detail.openSampleLabel')
  const downloadButtonLabel = isResolvingDownloadUrl
    ? t('library.detail.openingDownloadLabel')
    : t('library.detail.downloadLabel')

  if (isLoading) {
    return (
      <PageShell>
        <StatePanel>{t('library.detail.loading')}</StatePanel>
      </PageShell>
    )
  }

  if (error) {
    return (
      <PageShell>
        <StatePanel tone="error">{error}</StatePanel>
      </PageShell>
    )
  }

  if (notFound || !asset) {
    return <NotFoundPage />
  }

  return (
    <PageShell>
      <div className="mx-auto flex w-full max-w-[1320px] flex-col gap-6 px-4 sm:px-6 lg:px-8">
        <Link
          to="/library"
          className="inline-flex items-center gap-2 text-sm font-semibold text-primary"
        >
          <ArrowLeft className="h-4 w-4" />
          {t('library.detail.backLabel')}
        </Link>

        <section className="overflow-hidden rounded-[34px] border border-primary/10 bg-card/90 p-6 text-card-foreground shadow-[0_24px_80px_rgba(109,76,255,0.1)] backdrop-blur dark:border-white/10 dark:bg-card/88 dark:shadow-[0_24px_80px_rgba(0,0,0,0.35)] xl:p-8">
          <div className="grid gap-6 xl:grid-cols-[280px_minmax(0,1fr)]">
            <div className="overflow-hidden rounded-[28px] border border-primary/10 bg-muted/50 dark:border-white/10">
              <img
                src={getBookCoverUrl(asset.bookImageUrl)}
                alt={asset.bookTitle}
                className="aspect-[3/4] w-full object-cover"
              />
            </div>

            <div className="space-y-5">
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div className="min-w-0">
                  <p className="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                    {t('library.detail.bookLabel')}
                  </p>
                  <h1 className="mt-2 font-heading text-4xl font-bold tracking-tight text-foreground">
                    {asset.bookTitle}
                  </h1>
                  <p className="mt-3 text-lg font-medium text-muted-foreground">
                    {asset.assetTitle}
                  </p>
                </div>

                <Badge
                  variant="outline"
                  className={cn(
                    'rounded-full px-3 py-1 text-xs font-semibold',
                    getStatusBadgeClassName(asset.accessStatus),
                  )}
                >
                  {asset.accessStatus}
                </Badge>
              </div>

              <div className="flex flex-wrap gap-2">
                <Pill>{asset.format}</Pill>
                <Pill>{asset.accessType}</Pill>
                {asset.downloadAllowed ? <Pill>{t('library.detail.downloadAllowed')}</Pill> : null}
                {asset.sampleAvailable ? <Pill>{t('library.detail.sampleAvailable')}</Pill> : null}
              </div>

              <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
                <MetaCard label={t('library.detail.priceLabel')} value={formatCurrency(asset.price)} />
                <MetaCard label={t('library.detail.fileNameLabel')} value={asset.fileName} />
                <MetaCard label={t('library.detail.mimeTypeLabel')} value={asset.mimeType} />
                <MetaCard
                  label={t('library.detail.fileSizeLabel')}
                  value={formatDigitalFileSize(asset.fileSize)}
                />
                <MetaCard
                  label={t('library.detail.acquiredAtLabel')}
                  value={formatDate(asset.acquiredAt)}
                />
                <MetaCard
                  label={t('library.detail.assetUpdatedAtLabel')}
                  value={formatDate(asset.assetUpdatedAt)}
                />
                <MetaCard
                  label={t('library.detail.expiresAtLabel')}
                  value={asset.expiresAt ? formatDate(asset.expiresAt) : t('library.detail.noExpiry')}
                />
                <MetaCard
                  label={t('library.detail.sourceOrderIdLabel')}
                  value={asset.sourceOrderId ?? t('library.detail.noSourceOrder')}
                />
              </div>

              <div className="rounded-[24px] border border-primary/10 bg-primary/5 p-5 dark:border-primary/20 dark:bg-primary/10">
                <p className="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                  {t('library.detail.bookDescriptionLabel')}
                </p>
                <p className="mt-3 whitespace-pre-wrap leading-7 text-muted-foreground">
                  {asset.bookDescription?.trim() || t('library.detail.noDescription')}
                </p>
              </div>

              <div className="rounded-[24px] border border-primary/10 bg-background/70 p-5 dark:border-white/10 dark:bg-background/35">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <h2 className="font-heading text-2xl font-bold text-foreground">
                      {t('library.detail.accessTitle')}
                    </h2>
                    <p className="mt-2 text-sm text-muted-foreground">
                      {t('library.detail.accessDescription')}
                    </p>
                  </div>
                  <div className="flex flex-wrap gap-3">
                    <Link to={`/library/${asset.digitalAssetId}/read`}>
                      <Button type="button" className="rounded-2xl">
                        {t('library.detail.openReaderLabel')}
                      </Button>
                    </Link>

                    <Link
                      to={`/reading-journal?bookId=${asset.bookId}&currentPage=${
                        asset.progress?.currentPage ?? ''
                      }&progressPercent=${asset.progress?.progressPercent ?? ''}`}
                    >
                      <Button type="button" variant="outline" className="rounded-2xl">
                        <PencilLine className="mr-2 h-4 w-4" />
                        {t('readingJournal.openFromLibraryDetail')}
                      </Button>
                    </Link>

                    {asset.sampleAvailable ? (
                      <Button
                        type="button"
                        variant="outline"
                        className="rounded-2xl"
                        disabled={isResolvingSampleUrl}
                        onClick={() => void openSampleAsset()}
                      >
                        <ExternalLink className="mr-2 h-4 w-4" />
                        {sampleButtonLabel}
                      </Button>
                    ) : null}

                    {asset.downloadAllowed ? (
                      <Button
                        type="button"
                        onClick={() => void downloadAsset()}
                        disabled={isResolvingDownloadUrl}
                        className="rounded-2xl"
                      >
                        <Download className="mr-2 h-4 w-4" />
                        {downloadButtonLabel}
                      </Button>
                    ) : (
                      <Badge
                        variant="outline"
                        className="rounded-2xl border-primary/10 bg-primary/5 px-3 py-2 text-muted-foreground dark:border-primary/20 dark:bg-primary/10"
                      >
                        {t('library.detail.readOnlyBadge')}
                      </Badge>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="rounded-[30px] border border-primary/10 bg-card/90 p-6 text-card-foreground shadow-[0_18px_50px_rgba(109,76,255,0.08)] backdrop-blur dark:border-white/10 dark:bg-card/88 dark:shadow-[0_18px_50px_rgba(0,0,0,0.28)]">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="font-heading text-2xl font-bold text-foreground">
                {t('library.detail.progressTitle')}
              </h2>
              <p className="mt-2 text-sm text-muted-foreground">
                {t('library.detail.progressDescription')}
              </p>
            </div>
            <Badge
              variant="outline"
              className="w-fit rounded-full border-primary/10 bg-primary/5 px-3 py-1 text-primary"
            >
              {asset.progress ? `${asset.progress.progressPercent}%` : t('library.detail.noProgress')}
            </Badge>
          </div>

          <div className="mt-5 h-2 overflow-hidden rounded-full bg-primary/8">
            <div
              className="h-full rounded-full bg-primary"
              style={{
                width: `${Math.max(
                  0,
                  Math.min(asset.progress?.progressPercent ?? 0, 100),
                )}%`,
              }}
            />
          </div>

          <div className="mt-6 grid gap-5 lg:grid-cols-[180px_180px_minmax(0,1fr)]">
            <div>
              <Label htmlFor="currentPage">{t('library.detail.currentPageLabel')}</Label>
              <Input
                id="currentPage"
                type="number"
                min="0"
                step="1"
                value={progressForm.currentPage}
                onChange={(event) =>
                  handleProgressFieldChange('currentPage', event.currentTarget.value)
                }
                className="mt-2 h-11 rounded-2xl border-primary/10 bg-background/80 dark:bg-input/40"
              />
            </div>

            <div>
              <Label htmlFor="progressPercent">{t('library.detail.progressPercentLabel')}</Label>
              <Input
                id="progressPercent"
                type="number"
                min="0"
                max="100"
                step="0.01"
                value={progressForm.progressPercent}
                onChange={(event) =>
                  handleProgressFieldChange(
                    'progressPercent',
                    event.currentTarget.value,
                  )
                }
                className="mt-2 h-11 rounded-2xl border-primary/10 bg-background/80 dark:bg-input/40"
              />
            </div>

            <div>
              <Label htmlFor="positionData">{t('library.detail.positionDataLabel')}</Label>
              <Textarea
                id="positionData"
                value={progressForm.positionData}
                onChange={(event) =>
                  handleProgressFieldChange('positionData', event.currentTarget.value)
                }
                rows={4}
                className="mt-2 rounded-2xl border-primary/10 bg-background/80 dark:bg-input/40"
                placeholder={t('library.detail.positionDataPlaceholder')}
              />
            </div>
          </div>

          <div className="mt-6 flex justify-end">
            <Button
              type="button"
              onClick={() => void submitProgress()}
              disabled={isSavingProgress}
              className="rounded-2xl"
            >
              <Save className="mr-2 h-4 w-4" />
              {isSavingProgress ? t('library.detail.savingLabel') : t('library.detail.saveProgressLabel')}
            </Button>
          </div>
        </section>
      </div>
    </PageShell>
  )
}

function PageShell({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(249,247,255,1)_0%,rgba(244,239,255,0.94)_46%,rgba(255,255,255,1)_100%)] dark:bg-[linear-gradient(180deg,rgba(24,20,38,1)_0%,rgba(18,16,29,0.98)_46%,rgba(13,12,21,1)_100%)]">
      <Header />
      <main className="flex-1 pb-16 pt-6 sm:pb-20 sm:pt-8">{children}</main>
      <Footer />
    </div>
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
    <div className="mx-auto w-full max-w-[1320px] px-4 sm:px-6 lg:px-8">
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
    </div>
  )
}

function MetaCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-[20px] border border-primary/8 bg-primary/4 px-4 py-3">
      <dt className="text-xs font-semibold uppercase tracking-[0.16em] text-muted-foreground">
        {label}
      </dt>
      <dd className="mt-2 break-words text-sm font-semibold text-foreground">
        {value}
      </dd>
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

function getStatusBadgeClassName(status: string) {
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
