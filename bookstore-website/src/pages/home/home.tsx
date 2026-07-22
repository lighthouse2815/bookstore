import { Link } from 'react-router-dom'
import {
  ArrowRight,
  ArrowUpRight,
  BookMarked,
  BookOpen,
  Brain,
  Briefcase,
  Cpu,
  GraduationCap,
  Landmark,
  Palette,
  Rocket,
  Sparkles,
  Truck,
  Wallet,
} from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { BookCard } from '@/components/book/book-card'
import {
  StatePanel,
  primaryLinkButtonClassName,
  secondaryLinkButtonClassName,
} from '@/components/common/page-shell'
import { FunDiscoverySection } from '@/components/home/fun-discovery-section'
import { PersonalizedRecommendations } from '@/components/home/personalized-recommendations'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import { useBookCatalog } from '@/hooks/use-book-catalog'
import { getBookCoverUrl, setBookCoverFallback } from '@/utils/book-cover'
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

function getCategoryIcon(categoryCode: string): LucideIcon {
  return {
    SCIENCE_FICTION: Rocket,
    EDUCATION: GraduationCap,
    SCIENCE_TECHNOLOGY: Cpu,
    BUSINESS_MANAGEMENT: Briefcase,
    PERSONAL_DEVELOPMENT: Brain,
    HISTORY_MEMOIR: Landmark,
    ART_CREATIVITY: Palette,
    FANTASY: Sparkles,
  }[categoryCode] ?? BookOpen
}

export default function HomePage() {
  const { t, language, formatNumber } = useLanguage()
  const { isAuthenticated, isLoading: isAuthLoading } = useAuth()
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
                            onError={(event) => setBookCoverFallback(event.currentTarget)}
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

        <section className="mx-auto max-w-7xl px-4 py-14 sm:px-6 lg:px-8">
          <div className="overflow-hidden rounded-[2rem] border border-border/60 bg-muted/30 p-5 shadow-[0_24px_70px_rgba(15,23,42,0.06)] sm:p-8 lg:p-10 dark:shadow-[0_24px_70px_rgba(0,0,0,0.22)]">
            <div className="mb-8 flex flex-col gap-5 sm:flex-row sm:items-end sm:justify-between">
              <div className="max-w-xl">
                <div className="mb-3 flex items-center gap-2 text-sm font-medium text-muted-foreground">
                  <BookOpen className="size-4 text-primary" strokeWidth={1.8} />
                  <span>
                    {isLoading
                      ? t('home.catalogLoadingTitle')
                      : hasCategoryError
                        ? t('home.catalogCategoriesErrorTitle')
                        : t('home.categoriesCount', { count: categories.length })}
                  </span>
                </div>
                <h2 className="font-heading text-3xl font-bold tracking-[-0.03em] text-balance sm:text-4xl">
                  {t('home.categoriesTitle')}
                </h2>
              </div>
              <Link
                to="/books"
                className="group inline-flex min-h-11 w-fit items-center gap-2 rounded-full border border-border/70 bg-background px-5 py-2.5 text-sm font-semibold text-foreground shadow-sm transition-all duration-200 hover:-translate-y-0.5 hover:border-primary/40 hover:text-primary hover:shadow-md focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 active:translate-y-0"
              >
                {t('home.allCategories')}
                <ArrowRight className="size-4 transition-transform group-hover:translate-x-0.5" />
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
              <div className="grid gap-px overflow-hidden rounded-[1.5rem] border border-border/60 bg-border/60 sm:grid-cols-2 lg:grid-cols-4">
                {featuredCategories.map((category) => {
                  const CategoryIcon = getCategoryIcon(category.code)

                  return (
                    <Link
                      key={category.id}
                      to={`/books?category=${encodeURIComponent(category.code)}`}
                      className="group flex min-h-32 flex-col justify-between gap-6 bg-card p-5 transition-all duration-300 hover:bg-primary/5 focus-visible:relative focus-visible:z-10 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-primary/60 active:bg-primary/10"
                    >
                      <span className="flex items-start justify-between gap-4">
                        <span className="flex size-11 items-center justify-center rounded-2xl bg-primary/10 text-primary transition-all duration-300 group-hover:scale-105 group-hover:bg-primary group-hover:text-primary-foreground">
                          <CategoryIcon className="size-5" strokeWidth={1.8} />
                        </span>
                        <span className="flex size-9 items-center justify-center rounded-full border border-border/70 text-muted-foreground transition-all duration-300 group-hover:-translate-y-0.5 group-hover:translate-x-0.5 group-hover:border-primary group-hover:bg-primary group-hover:text-primary-foreground">
                          <ArrowUpRight className="size-4" />
                        </span>
                      </span>
                      <span className="max-w-[15rem] text-base font-semibold leading-snug text-foreground text-pretty transition-colors group-hover:text-primary">
                        {getCategoryLabel(category, language)}
                      </span>
                    </Link>
                  )
                })}
              </div>
            ) : (
              <CatalogStateCard
                title={t('home.emptyTitle')}
                description={t('home.emptyDescription')}
              />
            )}
          </div>
        </section>

        <FunDiscoverySection />

        <PersonalizedRecommendations
          enabled={isAuthenticated && !isAuthLoading}
        />

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
