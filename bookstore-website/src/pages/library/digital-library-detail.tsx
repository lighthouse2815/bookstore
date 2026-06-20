import { type ReactNode } from 'react'
import { Link, useParams } from 'react-router-dom'
import {
  ArrowLeft,
  Download,
  ExternalLink,
  Save,
} from 'lucide-react'
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
import {
  formatDigitalFileSize,
  resolveDigitalAssetUrl,
} from '@/utils/digital-asset'

export default function DigitalLibraryDetailPage() {
  const { digitalAssetId } = useParams<{ digitalAssetId: string }>()
  const { formatCurrency, formatDate, language } = useLanguage()
  const {
    asset,
    isLoading,
    error,
    notFound,
    isSavingProgress,
    progressForm,
    handleProgressFieldChange,
    submitProgress,
  } = useDigitalLibraryDetailPage(digitalAssetId)
  const copy = getDigitalLibraryDetailCopy(language)

  if (isLoading) {
    return (
      <PageShell>
        <StatePanel>{copy.loading}</StatePanel>
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

  const assetUrl = resolveDigitalAssetUrl(asset.storageKey)
  const sampleUrl = resolveDigitalAssetUrl(asset.sampleStorageKey)

  return (
    <PageShell>
      <div className="mx-auto flex w-full max-w-[1320px] flex-col gap-6 px-4 sm:px-6 lg:px-8">
        <Link
          to="/library"
          className="inline-flex items-center gap-2 text-sm font-semibold text-primary"
        >
          <ArrowLeft className="h-4 w-4" />
          {copy.backLabel}
        </Link>

        <section className="overflow-hidden rounded-[34px] border border-primary/10 bg-white/90 p-6 shadow-[0_24px_80px_rgba(109,76,255,0.1)] backdrop-blur xl:p-8">
          <div className="grid gap-6 xl:grid-cols-[280px_minmax(0,1fr)]">
            <div className="overflow-hidden rounded-[28px] border border-primary/10 bg-slate-50">
              <img
                src={getBookCoverUrl(asset.bookImageUrl)}
                alt={asset.bookTitle}
                className="aspect-[3/4] w-full object-cover"
              />
            </div>

            <div className="space-y-5">
              <div className="flex flex-wrap items-start justify-between gap-4">
                <div className="min-w-0">
                  <p className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">
                    {copy.bookLabel}
                  </p>
                  <h1 className="mt-2 font-heading text-4xl font-bold tracking-tight text-slate-950">
                    {asset.bookTitle}
                  </h1>
                  <p className="mt-3 text-lg font-medium text-slate-600">
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
                {asset.downloadAllowed ? <Pill>{copy.downloadAllowed}</Pill> : null}
              </div>

              <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
                <MetaCard
                  label={copy.priceLabel}
                  value={formatCurrency(asset.price)}
                />
                <MetaCard
                  label={copy.fileNameLabel}
                  value={asset.fileName}
                />
                <MetaCard
                  label={copy.mimeTypeLabel}
                  value={asset.mimeType}
                />
                <MetaCard
                  label={copy.fileSizeLabel}
                  value={formatDigitalFileSize(asset.fileSize)}
                />
                <MetaCard
                  label={copy.acquiredAtLabel}
                  value={formatDate(asset.acquiredAt)}
                />
                <MetaCard
                  label={copy.assetUpdatedAtLabel}
                  value={formatDate(asset.assetUpdatedAt)}
                />
                <MetaCard
                  label={copy.expiresAtLabel}
                  value={asset.expiresAt ? formatDate(asset.expiresAt) : copy.noExpiry}
                />
                <MetaCard
                  label={copy.sourceOrderIdLabel}
                  value={asset.sourceOrderId ?? copy.noSourceOrder}
                />
                <MetaCard
                  label={copy.checksumLabel}
                  value={asset.checksum ?? copy.noChecksum}
                />
              </div>

              <div className="rounded-[24px] border border-primary/10 bg-primary/4 p-5">
                <p className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">
                  {copy.bookDescriptionLabel}
                </p>
                <p className="mt-3 whitespace-pre-wrap leading-7 text-slate-600">
                  {asset.bookDescription?.trim() || copy.noDescription}
                </p>
              </div>

              <div className="rounded-[24px] border border-primary/10 bg-white p-5">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <div>
                    <h2 className="font-heading text-2xl font-bold text-slate-950">
                      {copy.accessTitle}
                    </h2>
                    <p className="mt-2 text-sm text-slate-500">
                      {copy.accessDescription}
                    </p>
                  </div>
                  <div className="flex flex-wrap gap-3">
                    {sampleUrl ? (
                      <a href={sampleUrl} target="_blank" rel="noreferrer">
                        <Button type="button" variant="outline" className="rounded-2xl">
                          <ExternalLink className="mr-2 h-4 w-4" />
                          {copy.openSampleLabel}
                        </Button>
                      </a>
                    ) : asset.sampleStorageKey ? (
                      <Badge
                        variant="outline"
                        className="rounded-2xl border-primary/10 bg-primary/5 px-3 py-2 text-slate-500"
                      >
                        {copy.sampleMetadataOnly}
                      </Badge>
                    ) : null}

                    {asset.downloadAllowed && assetUrl ? (
                      <a href={assetUrl} target="_blank" rel="noreferrer">
                        <Button type="button" className="rounded-2xl">
                          <Download className="mr-2 h-4 w-4" />
                          {copy.openAssetLabel}
                        </Button>
                      </a>
                    ) : asset.downloadAllowed ? (
                      <Badge
                        variant="outline"
                        className="rounded-2xl border-primary/10 bg-primary/5 px-3 py-2 text-slate-500"
                      >
                        {copy.fileInfrastructureNotice}
                      </Badge>
                    ) : null}
                  </div>
                </div>

                <dl className="mt-5 grid gap-4 lg:grid-cols-2">
                  <MetaCard
                    label={copy.storageKeyLabel}
                    value={asset.storageKey}
                  />
                  <MetaCard
                    label={copy.sampleStorageKeyLabel}
                    value={asset.sampleStorageKey ?? copy.noSampleStorageKey}
                  />
                </dl>
              </div>
            </div>
          </div>
        </section>

        <section className="rounded-[30px] border border-primary/10 bg-white/90 p-6 shadow-[0_18px_50px_rgba(109,76,255,0.08)] backdrop-blur">
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h2 className="font-heading text-2xl font-bold text-slate-950">
                {copy.progressTitle}
              </h2>
              <p className="mt-2 text-sm text-slate-500">
                {copy.progressDescription}
              </p>
            </div>
            <Badge
              variant="outline"
              className="w-fit rounded-full border-primary/10 bg-primary/5 px-3 py-1 text-primary"
            >
              {asset.progress ? `${asset.progress.progressPercent}%` : copy.noProgress}
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
              <Label htmlFor="currentPage">{copy.currentPageLabel}</Label>
              <Input
                id="currentPage"
                type="number"
                min="0"
                step="1"
                value={progressForm.currentPage}
                onChange={(event) =>
                  handleProgressFieldChange('currentPage', event.currentTarget.value)
                }
                className="mt-2 h-11 rounded-2xl border-primary/10 bg-white/80"
              />
            </div>

            <div>
              <Label htmlFor="progressPercent">{copy.progressPercentLabel}</Label>
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
                className="mt-2 h-11 rounded-2xl border-primary/10 bg-white/80"
              />
            </div>

            <div>
              <Label htmlFor="positionData">{copy.positionDataLabel}</Label>
              <Textarea
                id="positionData"
                value={progressForm.positionData}
                onChange={(event) =>
                  handleProgressFieldChange('positionData', event.currentTarget.value)
                }
                rows={4}
                className="mt-2 rounded-2xl border-primary/10 bg-white/80"
                placeholder={copy.positionDataPlaceholder}
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
              {isSavingProgress ? copy.savingLabel : copy.saveProgressLabel}
            </Button>
          </div>
        </section>
      </div>
    </PageShell>
  )
}

function PageShell({ children }: { children: ReactNode }) {
  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(249,247,255,1)_0%,rgba(244,239,255,0.94)_46%,rgba(255,255,255,1)_100%)]">
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
            ? 'border-primary/10 bg-white/88 text-slate-600'
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
      <dt className="text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">
        {label}
      </dt>
      <dd className="mt-2 break-words text-sm font-semibold text-slate-900">
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
      return 'border-emerald-200 bg-emerald-50 text-emerald-700'
    case 'EXPIRED':
      return 'border-amber-200 bg-amber-50 text-amber-700'
    case 'REVOKED':
      return 'border-rose-200 bg-rose-50 text-rose-700'
    default:
      return 'border-primary/10 bg-primary/5 text-primary'
  }
}

function getDigitalLibraryDetailCopy(language: 'vi' | 'en') {
  if (language === 'en') {
    return {
      accessDescription:
        'Open the sample or the real file only when the backend already exposes a direct browser URL.',
      accessTitle: 'Asset access',
      acquiredAtLabel: 'Acquired at',
      assetUpdatedAtLabel: 'Asset updated at',
      backLabel: 'Back to digital library',
      bookDescriptionLabel: 'Book description',
      bookLabel: 'Book',
      checksumLabel: 'Checksum',
      currentPageLabel: 'Current page',
      downloadAllowed: 'Download enabled',
      expiresAtLabel: 'Expires at',
      fileInfrastructureNotice: 'Storage key is available, but file delivery still depends on storage URL infrastructure.',
      fileNameLabel: 'File name',
      fileSizeLabel: 'File size',
      loading: 'Loading digital asset details...',
      mimeTypeLabel: 'MIME type',
      noChecksum: 'No checksum',
      noDescription: 'This book description is currently unavailable.',
      noExpiry: 'No expiry',
      noProgress: 'No progress yet',
      noSampleStorageKey: 'No sample storage key',
      noSourceOrder: 'No source order',
      openAssetLabel: 'Open asset',
      openSampleLabel: 'Open sample',
      positionDataLabel: 'Position data',
      positionDataPlaceholder: 'Optional JSON or marker string returned by your reader state',
      priceLabel: 'Price',
      progressDescription:
        'The current project does not ship a dedicated reader yet, so progress can still be synced manually from this screen.',
      progressPercentLabel: 'Progress percent',
      progressTitle: 'Reading progress',
      sampleMetadataOnly: 'Sample key available, but not yet a direct URL',
      sampleStorageKeyLabel: 'Sample storage key',
      saveProgressLabel: 'Save progress',
      savingLabel: 'Saving...',
      sourceOrderIdLabel: 'Source order ID',
      storageKeyLabel: 'Storage key',
    }
  }

  return {
    accessDescription:
      'Chỉ mở bản mẫu hoặc tệp thật khi backend đã trả về URL/path có thể mở trực tiếp trên trình duyệt.',
    accessTitle: 'Truy cập tài sản',
    acquiredAtLabel: 'Nhận lúc',
    assetUpdatedAtLabel: 'Tài sản cập nhật lúc',
    backLabel: 'Quay lại thư viện số',
    bookDescriptionLabel: 'Mô tả sách',
    bookLabel: 'Sách',
    checksumLabel: 'Checksum',
    currentPageLabel: 'Trang hiện tại',
    downloadAllowed: 'Cho phép tải',
    expiresAtLabel: 'Hết hạn lúc',
    fileInfrastructureNotice:
      'Storage key đã có, nhưng việc mở/tải tệp còn phụ thuộc hạ tầng URL file thực tế.',
    fileNameLabel: 'Tên tệp',
    fileSizeLabel: 'Dung lượng',
    loading: 'Đang tải chi tiết tài sản số...',
    mimeTypeLabel: 'MIME type',
    noChecksum: 'Chưa có checksum',
    noDescription: 'Mô tả sách hiện chưa có dữ liệu.',
    noExpiry: 'Không giới hạn',
    noProgress: 'Chưa có tiến độ',
    noSampleStorageKey: 'Không có sample storage key',
    noSourceOrder: 'Không có source order',
    openAssetLabel: 'Mở tài sản',
    openSampleLabel: 'Mở bản mẫu',
    positionDataLabel: 'Position data',
    positionDataPlaceholder:
      'JSON hoặc chuỗi đánh dấu vị trí nếu reader của bạn có lưu trạng thái',
    priceLabel: 'Giá',
    progressDescription:
      'Project hiện chưa có reader riêng cho PDF/EPUB/AUDIO, nhưng luồng cập nhật tiến độ vẫn có thể dùng trực tiếp từ màn hình này.',
    progressPercentLabel: 'Phần trăm tiến độ',
    progressTitle: 'Tiến độ đọc',
    sampleMetadataOnly: 'Có sample key nhưng chưa phải URL mở trực tiếp',
    sampleStorageKeyLabel: 'Sample storage key',
    saveProgressLabel: 'Lưu tiến độ',
    savingLabel: 'Đang lưu...',
    sourceOrderIdLabel: 'Source order ID',
    storageKeyLabel: 'Storage key',
  }
}
