import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import {
  ArrowRight,
  BookOpenText,
  Download,
  Eye,
  FileText,
  Headphones,
} from 'lucide-react'
import { buttonVariants } from '@/components/common/button'
import { useLanguage } from '@/contexts/language-context'
import type {
  DigitalAssetFormat,
  PublishedDigitalAssetCatalogItem,
} from '@/types/digital-library'
import { cn } from '@/utils'
import { getBookCoverUrl } from '@/utils/book-cover'
import { getCategoryLabel } from '@/utils/i18n'

const formatAccentMap: Record<DigitalAssetFormat, string> = {
  PDF: 'border-emerald-500/30 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300',
  EPUB: 'border-sky-500/30 bg-sky-500/10 text-sky-700 dark:text-sky-300',
  AUDIO:
    'border-amber-500/30 bg-amber-500/10 text-amber-700 dark:text-amber-300',
}

export function EbookCard({
  ebook,
}: {
  ebook: PublishedDigitalAssetCatalogItem
}) {
  const { t, formatCurrency } = useLanguage()
  const FormatIcon = getFormatIcon(ebook.format)

  return (
    <article className="group flex h-full flex-col overflow-hidden rounded-[1.75rem] border border-border/70 bg-card shadow-[0_18px_50px_rgba(15,23,42,0.06)] transition-all duration-300 hover:-translate-y-1 hover:shadow-[0_26px_60px_rgba(15,23,42,0.14)]">
      <Link
        to={`/books/${ebook.bookId}/ebook`}
        className="relative block aspect-[4/5] overflow-hidden bg-muted"
      >
        <img
          src={getBookCoverUrl(ebook.bookImageUrl)}
          alt={t('book.card.coverAlt', { title: ebook.bookTitle })}
          className="absolute inset-0 size-full object-cover transition-transform duration-500 group-hover:scale-105"
        />
        <div className="absolute inset-x-4 top-4 flex items-start justify-between gap-3">
          <span
            className={cn(
              'inline-flex items-center gap-2 rounded-full border px-3 py-1.5 text-xs font-semibold backdrop-blur',
              formatAccentMap[ebook.format],
            )}
          >
            <FormatIcon className="size-3.5" />
            {ebook.format}
          </span>
          {!ebook.purchaseAllowed ? (
            <span className="rounded-full bg-background/85 px-3 py-1.5 text-[11px] font-semibold uppercase tracking-[0.14em] text-muted-foreground backdrop-blur">
              {t('book.detail.digitalAssets.purchaseDisabled')}
            </span>
          ) : null}
        </div>
      </Link>

      <div className="flex flex-1 flex-col p-4">
        <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
          <span className="rounded-full bg-primary/8 px-2.5 py-1 font-medium text-primary">
            {getCategoryLabel(ebook.categoryName, t)}
          </span>
          <span>{ebook.authorName || t('book.fallback.author')}</span>
        </div>

        <Link to={`/books/${ebook.bookId}/ebook`} className="mt-3">
          <h3 className="line-clamp-2 font-heading text-lg font-semibold leading-snug text-balance text-foreground transition-colors group-hover:text-primary">
            {ebook.bookTitle}
          </h3>
        </Link>

        <p className="mt-2 line-clamp-1 text-sm font-medium text-foreground/80">
          {ebook.title}
        </p>

        <p className="mt-2 line-clamp-2 text-sm leading-6 text-muted-foreground">
          {ebook.bookDescription ||
            t('ebookCatalog.publisherLine', { publisher: ebook.publisherName })}
        </p>

        <div className="mt-4 flex flex-wrap gap-2">
          <FeaturePill
            icon={<Eye className="size-3.5" />}
            label={
              ebook.sampleAvailable
                ? t('ebookCatalog.sampleAvailable')
                : t('book.detail.digitalAssets.noSample')
            }
            emphasized={ebook.sampleAvailable}
          />
          <FeaturePill
            icon={<Download className="size-3.5" />}
            label={
              ebook.downloadAllowed
                ? t('ebookCatalog.downloadAllowed')
                : t('book.detail.digitalAssets.downloadRestricted')
            }
            emphasized={ebook.downloadAllowed}
          />
        </div>

        <div className="mt-auto flex items-end justify-between gap-3 pt-5">
          <div>
            <p className="text-xs font-medium uppercase tracking-[0.14em] text-muted-foreground">
              {t('book.detail.digitalAssets.priceLabel')}
            </p>
            <p className="mt-1 font-heading text-2xl font-bold text-primary">
              {formatCurrency(ebook.price)}
            </p>
          </div>

          <Link
            to={`/books/${ebook.bookId}/ebook`}
            className={cn(
              buttonVariants({ variant: 'outline', size: 'lg' }),
              'h-11 rounded-2xl px-4',
            )}
          >
            {t('ebookCatalog.openDetail')}
            <ArrowRight className="size-4" />
          </Link>
        </div>
      </div>
    </article>
  )
}

function FeaturePill({
  icon,
  label,
  emphasized = false,
}: {
  icon: ReactNode
  label: string
  emphasized?: boolean
}) {
  return (
    <span
      className={cn(
        'inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-medium',
        emphasized
          ? 'border-primary/20 bg-primary/8 text-primary'
          : 'border-border bg-background text-muted-foreground',
      )}
    >
      {icon}
      {label}
    </span>
  )
}

function getFormatIcon(format: DigitalAssetFormat) {
  switch (format) {
    case 'AUDIO':
      return Headphones
    case 'EPUB':
      return BookOpenText
    default:
      return FileText
  }
}
