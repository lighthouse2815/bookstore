import type { ComponentPropsWithoutRef, ElementType, ReactNode } from 'react'
import type { LucideIcon } from 'lucide-react'
import { cn } from '@/utils'

export const primaryLinkButtonClassName =
  'inline-flex items-center justify-center gap-2 rounded-2xl bg-primary px-5 py-3 text-sm font-semibold text-primary-foreground transition-all duration-200 hover:-translate-y-0.5 hover:opacity-95 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/40'

export const secondaryLinkButtonClassName =
  'inline-flex items-center justify-center gap-2 rounded-2xl border border-border/70 bg-background/85 px-5 py-3 text-sm font-semibold text-foreground transition-colors hover:bg-muted focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/30'

export const primaryButtonClassName =
  'h-11 rounded-2xl px-5 shadow-[0_18px_34px_rgba(109,76,255,0.18)]'

export const secondaryButtonClassName =
  'h-11 rounded-2xl border-border/70 bg-background/85 px-5'

export const destructiveOutlineButtonClassName =
  'h-11 rounded-2xl border-rose-200 text-rose-500 hover:border-rose-300 hover:bg-rose-50 hover:text-rose-600 dark:border-rose-400/25 dark:text-rose-300 dark:hover:border-rose-300/35 dark:hover:bg-rose-400/10'

type SurfaceTone = 'default' | 'muted' | 'nested'

type SurfaceCardProps<T extends ElementType> = {
  as?: T
  children: ReactNode
  className?: string
  tone?: SurfaceTone
}

const surfaceToneClassNames: Record<SurfaceTone, string> = {
  default:
    'rounded-[30px] border border-border/70 bg-card/92 shadow-[0_18px_50px_rgba(15,23,42,0.07)] backdrop-blur dark:shadow-none',
  muted:
    'rounded-[30px] border border-border/60 bg-background/55 shadow-[0_16px_40px_rgba(15,23,42,0.05)] backdrop-blur dark:shadow-none',
  nested:
    'rounded-[24px] border border-border/60 bg-background/80 shadow-sm',
}

export function SurfaceCard<T extends ElementType = 'section'>({
  as,
  children,
  className,
  tone = 'default',
  ...props
}: SurfaceCardProps<T> &
  Omit<ComponentPropsWithoutRef<T>, keyof SurfaceCardProps<T>>) {
  const Component = as ?? 'section'

  return (
    <Component
      className={cn(surfaceToneClassNames[tone], className)}
      {...props}
    >
      {children}
    </Component>
  )
}

type StateTone = 'default' | 'warning' | 'error' | 'success'

const stateToneClassNames: Record<StateTone, string> = {
  default:
    'border-dashed border-border/70 bg-card/80 text-foreground dark:bg-card/88',
  warning:
    'border-amber-200/70 bg-amber-50/85 text-amber-950 dark:border-amber-300/20 dark:bg-amber-300/10 dark:text-amber-100',
  error:
    'border-rose-200/70 bg-rose-50/90 text-rose-900 dark:border-rose-400/20 dark:bg-rose-400/10 dark:text-rose-100',
  success:
    'border-emerald-200/70 bg-emerald-50/90 text-emerald-900 dark:border-emerald-400/20 dark:bg-emerald-400/10 dark:text-emerald-100',
}

export function StatePanel({
  icon,
  title,
  description,
  detail,
  action,
  children,
  className,
  minHeightClassName = 'min-h-[220px]',
  tone = 'default',
}: {
  icon?: ReactNode
  title?: string
  description?: string
  detail?: string | null
  action?: ReactNode
  children?: ReactNode
  className?: string
  minHeightClassName?: string
  tone?: StateTone
}) {
  return (
    <div
      className={cn(
        'flex flex-col items-center justify-center rounded-[28px] border px-6 py-10 text-center shadow-sm',
        minHeightClassName,
        stateToneClassNames[tone],
        className,
      )}
    >
      {icon ? <div className="mb-4 flex justify-center">{icon}</div> : null}
      {title ? (
        <p className="font-heading text-xl font-semibold text-inherit">{title}</p>
      ) : null}
      {description ? (
        <p className="mt-2 max-w-2xl text-sm leading-7 text-inherit/80">
          {description}
        </p>
      ) : null}
      {detail ? (
        <p className="mt-2 max-w-2xl text-xs leading-6 text-inherit/70">
          {detail}
        </p>
      ) : null}
      {children ? <div className="mt-5 w-full">{children}</div> : null}
      {action ? <div className="mt-5">{action}</div> : null}
    </div>
  )
}

export function StatPill({
  label,
  value,
  className,
}: {
  label: string
  value: string
  className?: string
}) {
  return (
    <div
      className={cn(
        'rounded-2xl border border-white/70 bg-white/80 px-4 py-3 shadow-sm dark:border-white/10 dark:bg-background/40',
        className,
      )}
    >
      <p className="text-[11px] font-semibold uppercase tracking-[0.22em] text-muted-foreground">
        {label}
      </p>
      <p className="mt-2 text-sm font-semibold text-foreground">{value}</p>
    </div>
  )
}

export function SummaryField({
  label,
  value,
  className,
}: {
  label: string
  value: string
  className?: string
}) {
  return (
    <div
      className={cn(
        'rounded-2xl border border-border/70 bg-background/80 px-4 py-3',
        className,
      )}
    >
      <p className="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
        {label}
      </p>
      <p className="mt-1 text-sm font-medium text-foreground">{value}</p>
    </div>
  )
}

export function StepCard({
  stepNumber,
  title,
  description,
  children,
  className,
}: {
  stepNumber: number
  title: string
  description: string
  children: ReactNode
  className?: string
}) {
  return (
    <SurfaceCard className={cn('p-5', className)}>
      <div className="mb-5 flex items-start gap-4">
        <span className="inline-flex size-11 shrink-0 items-center justify-center rounded-2xl bg-primary/10 font-heading text-lg font-bold text-primary">
          {stepNumber}
        </span>
        <div>
          <h2 className="font-heading text-xl font-semibold text-foreground">
            {title}
          </h2>
          <p className="mt-1 text-sm leading-6 text-muted-foreground">
            {description}
          </p>
        </div>
      </div>
      {children}
    </SurfaceCard>
  )
}

export function ChoiceCard({
  icon: Icon,
  title,
  description,
  accentClassName,
  isSelected,
  onClick,
  className,
}: {
  icon: LucideIcon
  title: string
  description: string
  accentClassName: string
  isSelected: boolean
  onClick: () => void
  className?: string
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        'group flex h-full min-h-34 flex-col items-start gap-4 rounded-[24px] border px-4 py-4 text-left transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary/50 active:scale-[0.99]',
        isSelected
          ? 'border-primary/50 bg-primary/6 shadow-[0_16px_35px_rgba(15,23,42,0.08)]'
          : 'border-border/70 bg-background/70 hover:-translate-y-0.5 hover:border-primary/30 hover:bg-primary/5',
        className,
      )}
    >
      <span
        className={cn(
          'flex size-11 items-center justify-center rounded-2xl',
          accentClassName,
        )}
      >
        <Icon className="size-5" />
      </span>
      <div>
        <p className="font-heading text-base font-semibold text-foreground">
          {title}
        </p>
        <p className="mt-1 text-sm leading-6 text-muted-foreground">
          {description}
        </p>
      </div>
    </button>
  )
}
