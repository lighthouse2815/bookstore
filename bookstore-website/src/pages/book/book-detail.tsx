import { Link, useParams } from 'react-router-dom'
import {
  Building2,
  Calendar,
  ChevronRight,
  Package,
  Star,
  Truck,
} from 'lucide-react'
import { AddToCart } from '@/components/book/add-to-cart'
import { BookCard } from '@/components/book/book-card'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useBookDetail } from '@/hooks/use-book-detail'
import NotFoundPage from '@/pages/home/not-found'
import { getBookCoverUrl } from '@/utils/book-cover'
import { getCategoryLabel } from '@/utils/i18n'

export default function BookDetailPage() {
  const { id } = useParams<{ id: string }>()
  const { t, formatCurrency, formatNumber, formatDate } = useLanguage()
  const { book, suggestions, isLoading, error, notFound } = useBookDetail(id)

  if (isLoading) {
    return (
      <div className="flex min-h-screen flex-col">
        <Header />
        <main className="mx-auto flex w-full max-w-7xl flex-1 items-center justify-center px-4 py-6 sm:px-6 lg:px-8">
          <div className="rounded-2xl border border-dashed border-border px-8 py-12 text-center">
            <p className="font-heading text-lg font-semibold">
              {t('common.loading')}
            </p>
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
            <p className="mt-2 max-w-xl text-sm text-muted-foreground">
              {error}
            </p>
          </div>
        </main>
        <Footer />
      </div>
    )
  }

  if (notFound || !book) {
    return <NotFoundPage />
  }

  const hasRating = typeof book.rating === 'number' && book.rating > 0

  const discount = book.oldPrice
    ? Math.round((1 - book.price / book.oldPrice) * 100)
    : 0

  const specs = [
    {
      icon: Package,
      label: t('book.detail.specStock'),
      value: t('book.detail.stockValue', {
        count: formatNumber(book.stockQuantity),
      }),
    },
    {
      icon: Building2,
      label: t('book.detail.specPublisher'),
      value: book.publisher || t('book.fallback.publisher'),
    },
    {
      icon: Calendar,
      label: t('book.detail.specUpdatedAt'),
      value: formatDate(book.updatedAt),
    },
  ]

  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="mx-auto w-full max-w-7xl flex-1 px-4 py-6 sm:px-6 lg:px-8">
        <nav className="mb-6 flex items-center gap-1 text-sm text-muted-foreground">
          <Link to="/" className="hover:text-primary">
            {t('book.detail.breadcrumbHome')}
          </Link>
          <ChevronRight className="size-4" />
          <Link to="/books" className="hover:text-primary">
            {t('book.detail.breadcrumbBooks')}
          </Link>
          <ChevronRight className="size-4" />
          <span className="line-clamp-1 text-foreground">{book.title}</span>
        </nav>

        <div className="grid gap-8 lg:grid-cols-[2fr_3fr]">
          <div className="lg:sticky lg:top-24 lg:self-start">
            <div className="relative mx-auto aspect-[3/4] w-full max-w-sm overflow-hidden rounded-2xl border border-border bg-muted shadow-lg">
              <img
                src={getBookCoverUrl(book.cover)}
                alt={t('book.card.coverAlt', { title: book.title })}
                className="absolute inset-0 size-full object-cover"
              />
              {discount > 0 && (
                <span className="absolute left-3 top-3 rounded-full bg-primary px-3 py-1 text-sm font-bold text-primary-foreground">
                  -{discount}%
                </span>
              )}
            </div>
          </div>

          <div>
            <span className="text-sm font-semibold text-accent">
              {getCategoryLabel(book.category, t)}
            </span>
            <h1 className="mt-1 font-heading text-3xl font-bold tracking-tight text-balance">
              {book.title}
            </h1>
            <p className="mt-2 text-muted-foreground">
              {t('book.detail.author')}{' '}
              <span className="font-medium text-foreground">
                {book.author || t('book.fallback.author')}
              </span>
            </p>

            {hasRating && (
              <div className="mt-3 flex items-center gap-2">
                <div className="flex items-center gap-0.5">
                  {Array.from({ length: 5 }).map((_, index) => (
                    <Star
                      key={index}
                      className={
                        index < Math.round(book.rating ?? 0)
                          ? 'size-4 fill-chart-3 text-chart-3'
                          : 'size-4 text-border'
                      }
                    />
                  ))}
                </div>
                <span className="text-sm font-medium">{book.rating}</span>
                <span className="text-sm text-muted-foreground">
                  {t('book.detail.reviewsCount', {
                    count: formatNumber(book.reviews ?? 0),
                  })}
                </span>
              </div>
            )}

            <div className="mt-6 flex flex-wrap items-end gap-3 rounded-2xl bg-muted/50 p-5">
              <span className="font-heading text-3xl font-bold text-primary">
                {formatCurrency(book.price)}
              </span>
              {book.oldPrice && (
                <span className="pb-1 text-lg text-muted-foreground line-through">
                  {formatCurrency(book.oldPrice)}
                </span>
              )}
              {discount > 0 && (
                <span className="mb-1 rounded-full bg-primary/10 px-2 py-0.5 text-sm font-semibold text-primary">
                  {t('book.detail.saveAmount', {
                    amount: formatCurrency((book.oldPrice ?? 0) - book.price),
                  })}
                </span>
              )}
            </div>

            <div className="mt-6">
              <AddToCart book={book} />
            </div>

            <div className="mt-6 flex items-center gap-2 rounded-xl border border-dashed border-border p-4 text-sm">
              <Truck className="size-5 text-accent" />
              <span className="text-muted-foreground">
                {t('book.detail.shippingInfo', {
                  amount: formatCurrency(200000),
                })}
              </span>
            </div>

            <div className="mt-8">
              <h2 className="mb-3 font-heading text-lg font-bold">
                {t('book.detail.descriptionTitle')}
              </h2>
              <p className="leading-relaxed text-muted-foreground text-pretty">
                {book.description || t('book.detail.descriptionFallback')}
              </p>
            </div>

            <div className="mt-8">
              <h2 className="mb-3 font-heading text-lg font-bold">
                {t('book.detail.detailsTitle')}
              </h2>
              <div className="grid gap-3 sm:grid-cols-3">
                {specs.map((spec) => (
                  <div
                    key={spec.label}
                    className="flex items-center gap-3 rounded-xl border border-border p-3"
                  >
                    <span className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-primary/10 text-primary">
                      <spec.icon className="size-4" />
                    </span>
                    <div>
                      <p className="text-xs text-muted-foreground">
                        {spec.label}
                      </p>
                      <p className="text-sm font-semibold">{spec.value}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>

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
      </main>
      <Footer />
    </div>
  )
}
