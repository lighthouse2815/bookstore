import {
  BadgeCheck,
  CircleOff,
  Clock3,
  PackageCheck,
  ReceiptText,
  RotateCcw,
  TicketPercent,
  Truck,
  type LucideIcon,
} from 'lucide-react'
import { useLanguage } from '@/contexts/language-context'
import type { OrderTimelineEventResponse } from '@/types/order'
import type { ShipmentStatus } from '@/types/shipment'
import { cn } from '@/utils'
import {
  getOrderStatusLabel,
  getShipmentStatusLabel,
  getUserRoleLabel,
} from '@/utils/i18n'

type OrderTimelineListProps = {
  emptyLabel: string
  events: OrderTimelineEventResponse[]
  showActor?: boolean
}

type EventTone = {
  icon: LucideIcon
  iconClassName: string
  surfaceClassName: string
}

type KnownShipmentStatus = ShipmentStatus

const DEFAULT_TONE: EventTone = {
  icon: ReceiptText,
  iconClassName: 'text-slate-600 dark:text-slate-300',
  surfaceClassName:
    'border-slate-200 bg-slate-50/80 dark:border-slate-400/20 dark:bg-slate-400/10',
}

const EVENT_TONES: Record<string, EventTone> = {
  ORDER_CREATED: {
    icon: ReceiptText,
    iconClassName: 'text-primary',
    surfaceClassName:
      'border-primary/15 bg-primary/6 dark:border-primary/25 dark:bg-primary/10',
  },
  COUPON_APPLIED: {
    icon: TicketPercent,
    iconClassName: 'text-fuchsia-600 dark:text-fuchsia-300',
    surfaceClassName:
      'border-fuchsia-200 bg-fuchsia-50/80 dark:border-fuchsia-400/20 dark:bg-fuchsia-400/10',
  },
  PAYMENT_PENDING: {
    icon: Clock3,
    iconClassName: 'text-amber-600 dark:text-amber-300',
    surfaceClassName:
      'border-amber-200 bg-amber-50/80 dark:border-amber-400/20 dark:bg-amber-400/10',
  },
  PAYMENT_PAID: {
    icon: BadgeCheck,
    iconClassName: 'text-emerald-600 dark:text-emerald-300',
    surfaceClassName:
      'border-emerald-200 bg-emerald-50/80 dark:border-emerald-400/20 dark:bg-emerald-400/10',
  },
  ORDER_STATUS_CHANGED: {
    icon: PackageCheck,
    iconClassName: 'text-sky-600 dark:text-sky-300',
    surfaceClassName:
      'border-sky-200 bg-sky-50/80 dark:border-sky-400/20 dark:bg-sky-400/10',
  },
  SHIPMENT_ASSIGNED: {
    icon: Truck,
    iconClassName: 'text-primary',
    surfaceClassName:
      'border-primary/15 bg-primary/6 dark:border-primary/25 dark:bg-primary/10',
  },
  SHIPMENT_STATUS_CHANGED: {
    icon: Truck,
    iconClassName: 'text-amber-700 dark:text-amber-300',
    surfaceClassName:
      'border-amber-200 bg-amber-50/80 dark:border-amber-400/20 dark:bg-amber-400/10',
  },
  ORDER_CANCELLED: {
    icon: CircleOff,
    iconClassName: 'text-rose-600 dark:text-rose-300',
    surfaceClassName:
      'border-rose-200 bg-rose-50/80 dark:border-rose-400/20 dark:bg-rose-400/10',
  },
  STOCK_ROLLED_BACK: {
    icon: RotateCcw,
    iconClassName: 'text-sky-700 dark:text-sky-300',
    surfaceClassName:
      'border-sky-200 bg-sky-50/80 dark:border-sky-400/20 dark:bg-sky-400/10',
  },
  COUPON_ROLLED_BACK: {
    icon: RotateCcw,
    iconClassName: 'text-fuchsia-600 dark:text-fuchsia-300',
    surfaceClassName:
      'border-fuchsia-200 bg-fuchsia-50/80 dark:border-fuchsia-400/20 dark:bg-fuchsia-400/10',
  },
  RETURN_REQUESTED: {
    icon: RotateCcw,
    iconClassName: 'text-amber-600 dark:text-amber-300',
    surfaceClassName:
      'border-amber-200 bg-amber-50/80 dark:border-amber-400/20 dark:bg-amber-400/10',
  },
  RETURN_APPROVED: {
    icon: BadgeCheck,
    iconClassName: 'text-emerald-600 dark:text-emerald-300',
    surfaceClassName:
      'border-emerald-200 bg-emerald-50/80 dark:border-emerald-400/20 dark:bg-emerald-400/10',
  },
  RETURN_REJECTED: {
    icon: CircleOff,
    iconClassName: 'text-rose-600 dark:text-rose-300',
    surfaceClassName:
      'border-rose-200 bg-rose-50/80 dark:border-rose-400/20 dark:bg-rose-400/10',
  },
  RETURN_CANCELLED: {
    icon: CircleOff,
    iconClassName: 'text-slate-600 dark:text-slate-300',
    surfaceClassName:
      'border-slate-200 bg-slate-50/80 dark:border-slate-400/20 dark:bg-slate-400/10',
  },
  REFUND_INTERNAL_APPROVED: {
    icon: BadgeCheck,
    iconClassName: 'text-primary',
    surfaceClassName:
      'border-primary/15 bg-primary/6 dark:border-primary/25 dark:bg-primary/10',
  },
  STOCK_RESTOCKED_FROM_RETURN: {
    icon: RotateCcw,
    iconClassName: 'text-sky-700 dark:text-sky-300',
    surfaceClassName:
      'border-sky-200 bg-sky-50/80 dark:border-sky-400/20 dark:bg-sky-400/10',
  },
}

const userRoleSet = new Set(['ADMIN', 'STAFF', 'SHIPPER', 'USER'])
type KnownTimelineActorRole = 'ADMIN' | 'STAFF' | 'SHIPPER' | 'USER'

export function OrderTimelineList({
  emptyLabel,
  events,
  showActor = false,
}: OrderTimelineListProps) {
  const { locale, t } = useLanguage()

  if (events.length === 0) {
    return (
      <div className="rounded-2xl border border-dashed border-border/80 bg-muted/20 px-5 py-8 text-center text-sm text-muted-foreground">
        {emptyLabel}
      </div>
    )
  }

  const formatter = new Intl.DateTimeFormat(locale, {
    dateStyle: 'medium',
    timeStyle: 'short',
  })

  return (
    <ol className="space-y-4">
      {events.map((event, index) => {
        const tone = EVENT_TONES[event.eventType] ?? DEFAULT_TONE
        const Icon = tone.icon
        const actorLabel = getActorLabel(event, showActor, t)

        return (
          <li key={event.id} className="relative pl-16">
            {index < events.length - 1 ? (
              <span className="absolute left-[23px] top-12 h-[calc(100%-0.25rem)] w-px bg-border/70" />
            ) : null}

            <span
              className={cn(
                'absolute left-0 top-1 flex size-12 items-center justify-center rounded-2xl border shadow-sm',
                tone.surfaceClassName,
              )}
            >
              <Icon className={cn('h-5 w-5', tone.iconClassName)} />
            </span>

            <div className="rounded-2xl border border-border/70 bg-background/80 px-4 py-4 shadow-sm dark:bg-background/45">
              <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                <div className="space-y-1">
                  <p className="text-sm font-semibold text-foreground">{event.title}</p>
                  <p className="text-xs text-muted-foreground">
                    {formatter.format(new Date(event.createdAt))}
                  </p>
                </div>

                {renderStatusTransition(event, t)}
              </div>

              {event.description ? (
                <p className="mt-3 text-sm leading-6 text-muted-foreground">
                  {event.description}
                </p>
              ) : null}

              {actorLabel ? (
                <p className="mt-3 text-xs font-medium text-muted-foreground">
                  {t('orderTimeline.actorLabel')}: {actorLabel}
                </p>
              ) : null}
            </div>
          </li>
        )
      })}
    </ol>
  )
}

function renderStatusTransition(
  event: OrderTimelineEventResponse,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  if (!event.oldStatus && !event.newStatus) {
    return null
  }

  const toLabel = (status: string | null) => {
    if (!status) {
      return null
    }

    if (event.eventType === 'SHIPMENT_ASSIGNED' || event.eventType === 'SHIPMENT_STATUS_CHANGED') {
      return getShipmentStatusLabel(status as KnownShipmentStatus, t)
    }

    return getOrderStatusLabel(status, t)
  }

  const oldStatusLabel = toLabel(event.oldStatus)
  const newStatusLabel = toLabel(event.newStatus)

  return (
    <div className="flex flex-wrap items-center gap-2 text-xs font-semibold">
      {oldStatusLabel ? (
        <span className="rounded-full border border-border bg-muted/40 px-3 py-1 text-muted-foreground">
          {oldStatusLabel}
        </span>
      ) : null}
      {oldStatusLabel && newStatusLabel ? (
        <span className="text-muted-foreground">{'->'}</span>
      ) : null}
      {newStatusLabel ? (
        <span className="rounded-full border border-primary/15 bg-primary/6 px-3 py-1 text-primary">
          {newStatusLabel}
        </span>
      ) : null}
    </div>
  )
}

function getActorLabel(
  event: OrderTimelineEventResponse,
  showActor: boolean,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  if (!showActor && !event.actorName && !event.actorRole) {
    return null
  }

  const roleLabel =
    event.actorRole && userRoleSet.has(event.actorRole)
      ? getUserRoleLabel(event.actorRole as KnownTimelineActorRole, t)
      : event.actorRole

  if (!event.actorName && !roleLabel) {
    return t('orderTimeline.systemActor')
  }

  if (event.actorName && roleLabel) {
    return `${event.actorName} - ${roleLabel}`
  }

  return event.actorName ?? roleLabel ?? t('orderTimeline.systemActor')
}
