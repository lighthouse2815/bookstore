import { type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import {
  BookOpen,
  Download,
  ExternalLink,
  Search,
} from 'lucide-react'
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
import type {
  DigitalAccessStatus,
  DigitalAssetFormat,
} from '@/types/digital-library'
import { cn } from '@/utils'
import { getBookCoverUrl } from '@/utils/book-cover'
import { resolveDigitalAssetUrl } from '@/utils/digital-asset'

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
  const { formatCurrency, formatDate, formatNumber, language } = useLanguage()
  const {
    filteredItems,
    isLoading,
    error,
    searchTerm,
    selectedFormat,
    selectedStatus,
    setSearchTerm,
    setSelectedFormat,
    setSelectedStatus,
  } = useDigitalLibraryPage()
  const copy = getDigitalLibraryPageCopy(language)

  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(249,247,255,1)_0%,rgba(244,239,255,0.94)_46%,rgba(255,255,255,1)_100%)]">
      <Header />

      <main className="flex-1 pb-16 pt-6 sm:pb-20 sm:pt-8">
        <div className="mx-auto flex w-full max-w-[1320px] flex-col gap-6 px-4 sm:px-6 lg:px-8">
          <section className="overflow-hidden rounded-[34px] border border-primary/10 bg-white/88 px-6 py-7 shadow-[0_24px_80px_rgba(109,76,255,0.1)] backdrop-blur sm:px-8 lg:px-10">
            <div className="flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between">
              <div className="flex min-w-0 items-start gap-4">
                <span className="flex size-[72px] shrink-0 items-center justify-center rounded-[24px] bg-primary/10 text-primary shadow-[0_18px_40px_rgba(109,76,255,0.12)]">
                  <BookOpen className="h-9 w-9" strokeWidth={1.8} />
                </span>
                <div className="min-w-0">
                  <h1 className="font-heading text-4xl font-bold tracking-tight text-slate-950">
                    {copy.title}
                  </h1>
                  <p className="mt-2 max-w-2xl text-[1rem] leading-7 text-slate-500">
                    {copy.description}
                  </p>
                </div>
              </div>

              <div className="rounded-[24px] border border-primary/10 bg-primary/4 px-5 py-4">
                <p className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">
                  {copy.countLabel}
                </p>
                <p className="mt-2 font-heading text-3xl font-bold text-primary">
                  {formatNumber(filteredItems.length)}
                </p>
              </div>
            </div>
          </section>

          <section className="rounded-[30px] border border-primary/10 bg-white/88 p-5 shadow-[0_18px_50px_rgba(109,76,255,0.08)] backdrop-blur">
            <div className="grid gap-4 lg:grid-cols-[minmax(0,1.4fr)_220px_220px]">
              <div className="relative">
                <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
                <Input
                  value={searchTerm}
                  onChange={(event) => setSearchTerm(event.currentTarget.value)}
                  placeholder={copy.searchPlaceholder}
                  className="h-12 rounded-2xl border-primary/10 bg-white/80 pl-12"
                />
              </div>

              <Select
                value={selectedFormat}
                onValueChange={(value) =>
                  setSelectedFormat((value as DigitalAssetFormat | 'all') ?? 'all')
                }
              >
                <SelectTrigger className="h-12 rounded-2xl border-primary/10 bg-white/80">
                  <SelectValue placeholder={copy.formatFilterLabel} />
                </SelectTrigger>
                <SelectContent>
                  {FORMAT_OPTIONS.map((format) => (
                    <SelectItem key={format} value={format}>
                      {format === 'all' ? copy.allFormats : format}
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
                <SelectTrigger className="h-12 rounded-2xl border-primary/10 bg-white/80">
                  <SelectValue placeholder={copy.statusFilterLabel} />
                </SelectTrigger>
                <SelectContent>
                  {STATUS_OPTIONS.map((status) => (
                    <SelectItem key={status} value={status}>
                      {status === 'all' ? copy.allStatuses : status}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </section>

          {isLoading ? (
            <StatePanel>{copy.loading}</StatePanel>
          ) : error ? (
            <StatePanel tone="error">{error}</StatePanel>
          ) : filteredItems.length === 0 ? (
            <StatePanel>
              <div>
                <p className="font-semibold text-slate-950">{copy.emptyTitle}</p>
                <p className="mt-2 text-sm text-slate-500">{copy.emptyDescription}</p>
              </div>
            </StatePanel>
          ) : (
            <div className="grid gap-5 lg:grid-cols-2">
              {filteredItems.map((item) => {
                const sampleUrl = resolveDigitalAssetUrl(item.sampleStorageKey)

                return (
                  <article
                    key={item.digitalAssetId}
                    className="overflow-hidden rounded-[30px] border border-primary/10 bg-white/92 shadow-[0_18px_50px_rgba(109,76,255,0.08)]"
                  >
                    <div className="grid gap-5 p-5 sm:grid-cols-[132px_minmax(0,1fr)]">
                      <div className="overflow-hidden rounded-[24px] border border-primary/10 bg-slate-50">
                        <img
                          src={getBookCoverUrl(item.bookImageUrl)}
                          alt={item.bookTitle}
                          className="aspect-[3/4] w-full object-cover"
                        />
                      </div>

                      <div className="min-w-0">
                        <div className="flex flex-wrap items-start justify-between gap-3">
                          <div className="min-w-0">
                            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">
                              {copy.bookLabel}
                            </p>
                            <h2 className="mt-2 line-clamp-2 font-heading text-2xl font-bold text-slate-950">
                              {item.bookTitle}
                            </h2>
                            <p className="mt-2 line-clamp-2 text-sm font-medium text-slate-600">
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
                            <Pill>{copy.downloadAllowedLabel}</Pill>
                          ) : null}
                          {item.sampleStorageKey ? (
                            <Pill>{copy.sampleAvailableLabel}</Pill>
                          ) : null}
                        </div>

                        <dl className="mt-5 grid gap-3 sm:grid-cols-2">
                          <MetaItem
                            label={copy.priceLabel}
                            value={formatCurrency(item.price)}
                          />
                          <MetaItem
                            label={copy.acquiredAtLabel}
                            value={formatDate(item.acquiredAt)}
                          />
                          <MetaItem
                            label={copy.progressLabel}
                            value={
                              item.progress
                                ? `${item.progress.progressPercent}%`
                                : copy.noProgress
                            }
                          />
                          <MetaItem
                            label={copy.expiresAtLabel}
                            value={
                              item.expiresAt ? formatDate(item.expiresAt) : copy.noExpiry
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
                          <Link to={`/library/${item.digitalAssetId}`}>
                            <Button className="rounded-2xl">
                              {copy.viewDetailLabel}
                            </Button>
                          </Link>

                          {sampleUrl ? (
                            <a
                              href={sampleUrl}
                              target="_blank"
                              rel="noreferrer"
                            >
                              <Button
                                type="button"
                                variant="outline"
                                className="rounded-2xl"
                              >
                                <ExternalLink className="mr-2 h-4 w-4" />
                                {copy.openSampleLabel}
                              </Button>
                            </a>
                          ) : null}

                          {item.downloadAllowed ? (
                            <Badge
                              variant="outline"
                              className="rounded-2xl border-primary/10 bg-primary/5 px-3 py-2 text-slate-500"
                            >
                              <Download className="mr-2 h-4 w-4" />
                              {copy.downloadReadyBadge}
                            </Badge>
                          ) : null}
                        </div>
                      </div>
                    </div>
                  </article>
                )
              })}
            </div>
          )}
        </div>
      </main>

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
  )
}

function MetaItem({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-[20px] border border-primary/8 bg-primary/4 px-4 py-3">
      <dt className="text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">
        {label}
      </dt>
      <dd className="mt-2 text-sm font-semibold text-slate-900">{value}</dd>
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
      return 'border-emerald-200 bg-emerald-50 text-emerald-700'
    case 'EXPIRED':
      return 'border-amber-200 bg-amber-50 text-amber-700'
    case 'REVOKED':
      return 'border-rose-200 bg-rose-50 text-rose-700'
    default:
      return 'border-primary/10 bg-primary/5 text-primary'
  }
}

function getDigitalLibraryPageCopy(language: 'vi' | 'en') {
  if (language === 'en') {
    return {
      acquiredAtLabel: 'Acquired at',
      allFormats: 'All formats',
      allStatuses: 'All statuses',
      bookLabel: 'Book',
      countLabel: 'Available items',
      description:
        'Review the digital assets already granted by your completed orders and keep your reading progress in sync with the backend.',
      downloadAllowedLabel: 'Download enabled',
      downloadReadyBadge: 'Asset access available in detail',
      emptyDescription:
        'No digital asset matches the current filters. Assets you own will appear here as soon as the backend grants access.',
      emptyTitle: 'No digital assets found',
      expiresAtLabel: 'Expires at',
      formatFilterLabel: 'Filter by format',
      loading: 'Loading your digital library...',
      noExpiry: 'No expiry',
      noProgress: 'No progress yet',
      openSampleLabel: 'Open sample',
      priceLabel: 'Price',
      progressLabel: 'Reading progress',
      sampleAvailableLabel: 'Sample available',
      searchPlaceholder: 'Search by book title, asset title, or format...',
      statusFilterLabel: 'Filter by access status',
      title: 'My digital library',
      viewDetailLabel: 'View asset details',
    }
  }

  return {
    acquiredAtLabel: 'Nhận lúc',
    allFormats: 'Tất cả định dạng',
    allStatuses: 'Tất cả trạng thái',
    bookLabel: 'Sách',
    countLabel: 'Tài sản hiện có',
    description:
      'Xem các tài sản số đã được cấp quyền từ đơn hàng của bạn và đồng bộ tiến độ đọc trực tiếp với backend.',
    downloadAllowedLabel: 'Cho phép tải',
    downloadReadyBadge: 'Có thể mở/tải trong trang chi tiết',
    emptyDescription:
      'Không có tài sản số nào khớp bộ lọc hiện tại. Khi backend cấp quyền truy cập, tài sản sẽ xuất hiện ở đây.',
    emptyTitle: 'Chưa tìm thấy tài sản số',
    expiresAtLabel: 'Hết hạn lúc',
    formatFilterLabel: 'Lọc theo định dạng',
    loading: 'Đang tải thư viện số...',
    noExpiry: 'Không giới hạn',
    noProgress: 'Chưa có tiến độ',
    openSampleLabel: 'Mở bản mẫu',
    priceLabel: 'Giá',
    progressLabel: 'Tiến độ đọc',
    sampleAvailableLabel: 'Có bản mẫu',
    searchPlaceholder: 'Tìm theo tên sách, tên tài sản hoặc định dạng...',
    statusFilterLabel: 'Lọc theo trạng thái',
    title: 'Thư viện số của tôi',
    viewDetailLabel: 'Xem chi tiết tài sản',
  }
}
