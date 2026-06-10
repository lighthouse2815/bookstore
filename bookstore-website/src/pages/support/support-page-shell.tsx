import type { ReactNode } from 'react'
import { Link } from 'react-router-dom'
import type { LucideIcon } from 'lucide-react'
import { ChevronRight } from 'lucide-react'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'

type SupportPageShellProps = {
  icon: LucideIcon
  badge: string
  title: string
  description: string
  highlights: Array<{
    label: string
    value: string
  }>
  children: ReactNode
}

export function SupportPageShell({
  icon: Icon,
  badge,
  title,
  description,
  highlights,
  children,
}: SupportPageShellProps) {
  const { t } = useLanguage()

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="mx-auto flex w-full max-w-7xl flex-1 flex-col px-4 py-6 sm:px-6 lg:px-8">
        <nav className="mb-6 flex items-center gap-1 text-sm text-muted-foreground">
          <Link to="/" className="hover:text-primary">
            {t('header.nav.home')}
          </Link>
          <ChevronRight className="size-4" />
          <span className="line-clamp-1 text-foreground">{title}</span>
        </nav>

        <section className="relative overflow-hidden rounded-[32px] border border-border/60 bg-card/90 p-6 shadow-[0_28px_90px_rgba(2,6,23,0.18)] backdrop-blur xl:p-8">
          <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(129,140,248,0.16),transparent_32%),radial-gradient(circle_at_bottom_right,rgba(59,130,246,0.12),transparent_30%)]" />

          <div className="relative">
            <div className="inline-flex items-center gap-3 rounded-full border border-primary/20 bg-primary/10 px-4 py-2 text-sm font-semibold text-primary">
              <span className="flex size-8 items-center justify-center rounded-full bg-primary/12">
                <Icon className="size-4" />
              </span>
              {badge}
            </div>

            <h1 className="mt-5 max-w-3xl font-heading text-4xl font-bold tracking-tight text-foreground sm:text-5xl">
              {title}
            </h1>
            <p className="mt-4 max-w-3xl text-base leading-8 text-muted-foreground sm:text-lg">
              {description}
            </p>

            <div className="mt-8 grid gap-4 md:grid-cols-3">
              {highlights.map((highlight) => (
                <div
                  key={highlight.label}
                  className="rounded-[24px] border border-border/60 bg-background/55 p-5 shadow-[0_16px_40px_rgba(2,6,23,0.08)]"
                >
                  <p className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">
                    {highlight.label}
                  </p>
                  <p className="mt-3 text-xl font-semibold text-foreground">
                    {highlight.value}
                  </p>
                </div>
              ))}
            </div>
          </div>
        </section>

        <section className="mt-8 grid gap-6">{children}</section>
      </main>
      <Footer />
    </div>
  )
}

export function SupportSection({
  title,
  description,
  children,
}: {
  title: string
  description?: string
  children: ReactNode
}) {
  return (
    <div className="rounded-[28px] border border-border/60 bg-card p-6 shadow-[0_18px_40px_rgba(2,6,23,0.08)]">
      <h2 className="font-heading text-2xl font-bold text-foreground">
        {title}
      </h2>
      {description ? (
        <p className="mt-3 max-w-3xl text-sm leading-7 text-muted-foreground">
          {description}
        </p>
      ) : null}
      <div className="mt-6">{children}</div>
    </div>
  )
}
