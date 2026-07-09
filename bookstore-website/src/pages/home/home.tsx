import { Link } from 'react-router-dom'
import {
  ArrowRight,
  BookMarked,
  Sparkles,
  Truck,
  Wallet,
} from 'lucide-react'
import { BookCard } from '@/components/book/book-card'
import {
  StatePanel,
  primaryLinkButtonClassName,
  secondaryLinkButtonClassName,
} from '@/components/common/page-shell'
import { FunDiscoverySection } from '@/components/home/fun-discovery-section'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useBookCatalog } from '@/hooks/use-book-catalog'
import { getBookCoverUrl } from '@/utils/book-cover'
import { getCategoryLabel } from '@/utils/i18n'

const CATEGORY_PREVIEW_LIMIT = 8

type CatalogStateCardProps = {
  title: string
  description: string
  detail?: string | null
  className?: string
}

function CatalogStateCard({
  title,
  description,
  detail,
  className = '',
}: CatalogStateCardProps) {
  return (
    <StatePanel
      title={title}
      description={description}
      detail={detail}
      minHeightClassName="min-h-[220px]"
      className={className}
    />
  )
}

export default function HomePage() {
  const { t, formatNumber } = useLanguage()
  const { books, categories, isLoading, bookError, categoryError } =
    useBookCatalog()
  const hasBookError = Boolean(bookError)
  const hasCategoryError = Boolean(categoryError)
  const heroBooks = books.slice(0, 4)
  const featured = books.slice(0, 4)
  const featuredCategories = categories.slice(0, CATEGORY_PREVIEW_LIMIT)
  const bestsellers =
    books.length > 4 ? books.slice(4, 8) : books.slice(0, Math.min(4, books.length))
  const totalSoldCount = books.reduce((sum, book) => sum + book.soldCount, 0)
  const totalReviewCount = books.reduce((sum, book) => sum + (book.reviews ?? 0), 0)
  const weightedRatingTotal = books.reduce(
    (sum, book) => sum + (book.rating ?? 0) * (book.reviews ?? 0),
    0,
  )
  const averageRating =
    totalReviewCount > 0 ? weightedRatingTotal / totalReviewCount : 0
  const heroStats = [
    {
      value: isLoading ? '...' : hasBookError ? '--' : formatNumber(books.length),
      label: t('home.stats.books'),
    },
    {
      value: isLoading ? '...' : hasBookError ? '--' : formatNumber(totalSoldCount),
      label: t('home.stats.sales'),
    },
    {
      value: isLoading ? '...' : hasBookError ? '--' : `${averageRating.toFixed(1)}/5`,
      label: t('home.stats.reviewsCount', {
        count: formatNumber(totalReviewCount),
      }),
    },
  ]

  const valueProps = [
    {
      icon: Truck,
      title: t('home.values.fastDeliveryTitle'),
      description: t('home.values.fastDeliveryDesc'),
    },
    {
      icon: Wallet,
      title: t('home.values.greatPriceTitle'),
      description: t('home.values.greatPriceDesc'),
    },
    {
      icon: BookMarked,
      title: t('home.values.authenticTitle'),
      description: t('home.values.authenticDesc'),
    },
  ]

  return (
    <div className="flex min-h-screen flex-col">
      <Header />
      <main className="flex-1">
        <section className="relative overflow-hidden border-b border-border bg-muted/40">
          <div className="mx-auto grid max-w-7xl items-center gap-10 px-4 py-12 sm:px-6 lg:grid-cols-2 lg:gap-6 lg:px-8 lg:py-20">
            <div className="flex flex-col items-start gap-6">
              <span className="inline-flex items-center gap-2 rounded-full bg-primary/10 px-4 py-1.5 text-sm font-semibold text-primary">
                <Sparkles className="size-4" />
                {t('home.heroBadge')}
              </span>
              <h1 className="font-heading text-4xl font-bold leading-tight tracking-tight text-balance sm:text-5xl lg:text-6xl">
                {t('home.heroTitlePrefix')}{' '}
                <span className="text-primary">
                  {t('home.heroTitleAccent')}
                </span>
              </h1>
              <p className="max-w-md text-lg leading-relaxed text-muted-foreground text-pretty">
                {t('home.heroDescription')}
              </p>
              <div className="flex flex-wrap items-center gap-3">
                <Link
                  to="/books"
                  className={primaryLinkButtonClassName}
                >
                  {t('home.shopNow')}
                  <ArrowRight className="size-4" />
                </Link>
                <Link
                  to="/books?category=__life-skills__"
                  className={secondaryLinkButtonClassName}
                >
                  {t('home.lifeSkillsBooks')}
                </Link>
              </div>
              <div className="flex flex-wrap gap-6 pt-2">
                {heroStats.map((item) => (
                  <div key={item.label}>
                    <p className="font-heading text-2xl font-bold">{item.value}</p>
                    <p className="text-sm text-muted-foreground">{item.label}</p>
                  </div>
                ))}
              </div>
            </div>

            <div className="relative mx-auto w-full max-w-lg">
              {isLoading ? (
                <CatalogStateCard
                  title={t('home.catalogLoadingTitle')}
                  description={t('home.catalogLoadingDescription')}
                  className="px-6 py-12 shadow-sm"
                />
              ) : hasBookError ? (
                <CatalogStateCard
                  title={t('home.catalogBooksErrorTitle')}
                  description={t('home.catalogBooksErrorDescription')}
                  detail={bookError}
                  className="px-6 py-12 shadow-sm"
                />
              ) : heroBooks.length > 0 ? (
                <>
                  <div className="absolute -right-4 -top-4 hidden size-24 rounded-full bg-accent/20 lg:block" />
                  <div className="absolute -bottom-6 -left-6 hidden size-32 rounded-full bg-primary/10 lg:block" />
                  <div className="relative grid grid-cols-2 gap-4">
                    {heroBooks.map((book) => (
                      <Link
                        key={book.id}
                        to={`/books/${book.id}`}
                        className="overflow-hidden rounded-2xl border border-border bg-card shadow-lg"
                      >
                        <div className="relative aspect-[4/5]">
                          <img
                            src={getBookCoverUrl(book.cover)}
                            alt={book.title}
                            className="absolute inset-0 size-full object-cover"
                          />
                        </div>
                      </Link>
                    ))}
                  </div>
                </>
              ) : (
                <CatalogStateCard
                  title={t('home.emptyTitle')}
                  description={t('home.emptyDescription')}
                  className="px-6 py-12 shadow-sm"
                />
              )}
            </div>
          </div>
        </section>

        <section className="border-b border-border">
          <div className="mx-auto grid max-w-7xl gap-4 px-4 py-8 sm:grid-cols-3 sm:px-6 lg:px-8">
            {valueProps.map((item) => (
              <div key={item.title} className="flex items-center gap-4">
                <span className="flex size-12 shrink-0 items-center justify-center rounded-xl bg-primary/10 text-primary">
                  <item.icon className="size-6" />
                </span>
                <div>
                  <p className="font-heading font-semibold">{item.title}</p>
                  <p className="text-sm text-muted-foreground">
                    {item.description}
                  </p>
                </div>
              </div>
            ))}
          </div>
        </section>

        <section className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
          <div className="mb-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <h2 className="font-heading text-2xl font-bold tracking-tight">
                {t('home.categoriesTitle')}
              </h2>
              <p className="mt-1 text-sm text-muted-foreground">
                {isLoading
                  ? t('home.catalogLoadingTitle')
                  : hasCategoryError
                    ? t('home.catalogCategoriesErrorTitle')
                    : t('home.categoriesCount', { count: categories.length })}
              </p>
            </div>
            <Link
              to="/books"
              className="inline-flex items-center gap-1.5 text-sm font-semibold text-primary transition-colors hover:text-primary/80"
            >
              {t('home.allCategories')}
              <ArrowRight className="size-4" />
            </Link>
          </div>
          {isLoading ? (
            <CatalogStateCard
              title={t('home.catalogLoadingTitle')}
              description={t('home.catalogLoadingDescription')}
            />
          ) : hasCategoryError ? (
            <CatalogStateCard
              title={t('home.catalogCategoriesErrorTitle')}
              description={t('home.catalogCategoriesErrorDescription')}
              detail={categoryError}
            />
          ) : categories.length > 0 ? (
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
              {featuredCategories.map((category, index) => (
                <Link
                  key={category}
                  to={`/books?category=${encodeURIComponent(category)}`}
                  className="group flex min-h-24 items-end justify-between gap-4 overflow-hidden rounded-2xl border border-border/70 bg-card px-4 py-4 transition-all duration-200 hover:-translate-y-0.5 hover:border-primary/40 hover:bg-primary/5 hover:shadow-[0_16px_36px_rgba(15,23,42,0.08)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50"
                >
                  <span className="min-w-0">
                    <span className="block text-xs font-semibold tabular-nums text-primary/70">
                      {String(index + 1).padStart(2, '0')}
                    </span>
                    <span className="mt-2 block truncate text-sm font-semibold text-foreground group-hover:text-primary">
                      {getCategoryLabel(category, t)}
                    </span>
                  </span>
                  <ArrowRight className="size-4 shrink-0 text-muted-foreground transition-transform group-hover:translate-x-1 group-hover:text-primary" />
                </Link>
              ))}
            </div>
          ) : (
            <CatalogStateCard
              title={t('home.emptyTitle')}
              description={t('home.emptyDescription')}
            />
          )}
        </section>

        <FunDiscoverySection />

        <section className="mx-auto max-w-7xl px-4 pb-4 sm:px-6 lg:px-8">
          <div className="mb-6 flex items-end justify-between">
            <h2 className="font-heading text-2xl font-bold tracking-tight">
              {t('home.featuredTitle')}
            </h2>
            <Link
              to="/books"
              className="inline-flex items-center gap-1 text-sm font-semibold text-primary hover:underline"
            >
              {t('common.viewAll')}
              <ArrowRight className="size-4" />
            </Link>
          </div>
          <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
            {isLoading ? (
              <div className="col-span-full">
                <CatalogStateCard
                  title={t('home.catalogLoadingTitle')}
                  description={t('home.catalogLoadingDescription')}
                />
              </div>
            ) : hasBookError ? (
              <div className="col-span-full">
                <CatalogStateCard
                  title={t('home.catalogBooksErrorTitle')}
                  description={t('home.catalogBooksErrorDescription')}
                  detail={bookError}
                />
              </div>
            ) : featured.length > 0 ? (
              featured.map((book) => <BookCard key={book.id} book={book} />)
            ) : (
              <div className="col-span-full">
                <CatalogStateCard
                  title={t('home.emptyTitle')}
                  description={t('home.emptyDescription')}
                />
              </div>
            )}
          </div>
        </section>

        <section className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
          <div className="flex flex-col items-center gap-4 rounded-3xl bg-primary px-6 py-12 text-center text-primary-foreground sm:px-12">
            <Sparkles className="size-8" />
            <h2 className="font-heading text-2xl font-bold text-balance sm:text-3xl">
              {t('home.promoTitle')}
            </h2>
            <p className="max-w-lg text-pretty opacity-90">
              {t('home.promoDescription')}
            </p>
            <Link
              to="/books"
              className="mt-2 inline-flex items-center gap-2 rounded-full bg-background px-6 py-3 text-sm font-semibold text-foreground transition-opacity hover:opacity-90"
            >
              {t('home.promoButton')}
              <ArrowRight className="size-4" />
            </Link>
          </div>
        </section>

        <section className="mx-auto max-w-7xl px-4 pb-16 sm:px-6 lg:px-8">
          <div className="mb-6 flex items-end justify-between">
            <h2 className="font-heading text-2xl font-bold tracking-tight">
              {t('home.bestsellersTitle')}
            </h2>
            <Link
              to="/books"
              className="inline-flex items-center gap-1 text-sm font-semibold text-primary hover:underline"
            >
              {t('common.viewAll')}
              <ArrowRight className="size-4" />
            </Link>
          </div>
          <div className="grid grid-cols-2 gap-4 md:grid-cols-4">
            {isLoading ? (
              <div className="col-span-full">
                <CatalogStateCard
                  title={t('home.catalogLoadingTitle')}
                  description={t('home.catalogLoadingDescription')}
                />
              </div>
            ) : hasBookError ? (
              <div className="col-span-full">
                <CatalogStateCard
                  title={t('home.catalogBooksErrorTitle')}
                  description={t('home.catalogBooksErrorDescription')}
                  detail={bookError}
                />
              </div>
            ) : bestsellers.length > 0 ? (
              bestsellers.map((book) => <BookCard key={book.id} book={book} />)
            ) : (
              <div className="col-span-full">
                <CatalogStateCard
                  title={t('home.emptyTitle')}
                  description={t('home.emptyDescription')}
                />
              </div>
            )}
          </div>
        </section>
      </main>
      <Footer />
    </div>
  )
}
