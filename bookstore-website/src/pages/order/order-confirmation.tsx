import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  AlertTriangle,
  CheckCircle2,
  Clock3,
  Copy,
  Landmark,
  PackageCheck,
  QrCode,
  RefreshCcw,
  XCircle,
  type LucideIcon,
} from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/common/button'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useOrderConfirmationPage } from '@/hooks/use-order-confirmation-page'
import type { PaymentStatus } from '@/types/order'
import { cn } from '@/utils'
import {
  getPaymentMethodLabel,
  getPaymentStatusLabel,
} from '@/utils/i18n'

export default function OrderConfirmationPage() {
  const { t, formatCurrency } = useLanguage()
  const transferT = (
    key: string,
    params?: Record<string, number | string>,
  ) => t(`orderConfirmationBankTransfer.${key}`, params)
  const cashOnDeliveryT = (key: string) => t(`orderConfirmationCashOnDelivery.${key}`)
  const {
    order,
    orderId,
    orderCode,
    transferContent,
    totalAmount,
    paymentMethod,
    paymentStatus,
    paymentExpiresAt,
    isLoading,
    isPolling,
    error,
  } = useOrderConfirmationPage()
  const isBankTransferOrder = paymentMethod === 'BANK_TRANSFER_QR'
  const remainingPaymentSeconds = usePaymentExpiryCountdown(
    paymentExpiresAt,
    paymentStatus,
  )
  const [hasQrImageError, setHasQrImageError] = useState(false)
  const bankInfo = useMemo(() => {
    const configuredFallbackQrUrl = readConfiguredValue(
      import.meta.env.VITE_BANK_TRANSFER_QR_URL,
    )
    const fallbackQrConfig = readVietQrConfig(configuredFallbackQrUrl)
    const bankName = readConfiguredValue(
      import.meta.env.VITE_BANK_TRANSFER_BANK_NAME,
    )
    const bankCode =
      readConfiguredValue(import.meta.env.VITE_BANK_TRANSFER_BANK_CODE) ??
      fallbackQrConfig?.bankCode ??
      null
    const accountNumber =
      readConfiguredValue(import.meta.env.VITE_BANK_TRANSFER_ACCOUNT_NUMBER) ??
      fallbackQrConfig?.accountNumber ??
      null
    const accountName =
      readConfiguredValue(import.meta.env.VITE_BANK_TRANSFER_ACCOUNT_NAME) ??
      fallbackQrConfig?.accountName ??
      null
    const paymentReference = transferContent.trim() || orderCode.trim()

    return {
      bankName: bankName || bankCode || transferT('bankFallback'),
      accountNumber: accountNumber || transferT('accountNumberFallback'),
      accountName: accountName || transferT('accountNameFallback'),
      dynamicQrUrl: buildVietQrUrl(
        bankCode,
        accountNumber,
        accountName,
        totalAmount,
        paymentReference,
      ),
      fallbackQrUrl: buildFallbackQrUrl(
        configuredFallbackQrUrl,
        totalAmount,
        paymentReference,
        accountName,
      ),
    }
  }, [
    transferT('accountNameFallback'),
    transferT('accountNumberFallback'),
    transferT('bankFallback'),
    orderCode,
    totalAmount,
    transferContent,
  ])
  const qrDisplay = useMemo(() => {
    if (bankInfo.dynamicQrUrl) {
      return {
        kind: 'dynamic' as const,
        url: bankInfo.dynamicQrUrl,
      }
    }

    if (bankInfo.fallbackQrUrl) {
      return {
        kind: 'fallback' as const,
        url: bankInfo.fallbackQrUrl,
      }
    }

    return {
      kind: 'none' as const,
      url: null,
    }
  }, [bankInfo.dynamicQrUrl, bankInfo.fallbackQrUrl])
  const statusMeta = getStatusMeta(paymentStatus, paymentMethod, transferT, cashOnDeliveryT)

  useEffect(() => {
    setHasQrImageError(false)
  }, [qrDisplay.url])

  async function handleCopyTransferContent() {
    try {
      await navigator.clipboard.writeText(transferContent)
      toast.success(transferT('copySuccess'))
    } catch {
      toast.error(transferT('copyError'))
    }
  }

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="flex-1 bg-gradient-to-b from-background via-background to-primary/5">
        <div className="mx-auto w-full max-w-[1240px] px-4 py-10 sm:px-6 lg:px-8">
          <section
            className={cn(
              'rounded-3xl border p-6 shadow-sm sm:p-8',
              statusMeta.containerClassName,
            )}
          >
            <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
              <div className="flex gap-4">
                <div
                  className={cn(
                    'flex size-16 shrink-0 items-center justify-center rounded-2xl',
                    statusMeta.iconContainerClassName,
                  )}
                >
                  <statusMeta.icon className={cn('size-8', statusMeta.iconClassName)} />
                </div>
                <div>
                  <p
                    className={cn(
                      'inline-flex rounded-full px-3 py-1 text-xs font-semibold uppercase tracking-wide',
                      statusMeta.badgeClassName,
                    )}
                  >
                    {getPaymentStatusLabel(paymentStatus, t)}
                  </p>
                  <h1 className="mt-4 font-heading text-3xl font-bold sm:text-4xl">
                    {statusMeta.title}
                  </h1>
                  <p className="mt-3 max-w-2xl text-sm leading-7 text-muted-foreground sm:text-base">
                    {statusMeta.description}
                  </p>
                  {isBankTransferOrder && isPolling && paymentStatus === 'PENDING' && (
                    <p className="mt-4 inline-flex items-center gap-2 text-sm font-medium text-amber-800 dark:text-amber-300">
                      <RefreshCcw className="size-4 animate-spin" />
                      {transferT('pollingNotice')}
                    </p>
                  )}
                  {isBankTransferOrder &&
                  paymentStatus === 'PENDING' &&
                  remainingPaymentSeconds != null ? (
                    <p className="mt-3 text-sm font-semibold text-rose-700 dark:text-rose-300">
                      {transferT('remainingTime', {
                        time: formatRemainingTime(remainingPaymentSeconds),
                      })}
                    </p>
                  ) : null}
                </div>
              </div>

              <div className="min-w-0 rounded-2xl border border-border/70 bg-background/90 p-4 sm:min-w-[280px]">
                <SummaryRow label={transferT('orderIdLabel')} value={orderId || transferT('emptyValue')} />
                <SummaryRow label={transferT('orderCodeLabel')} value={orderCode || transferT('emptyValue')} />
                <SummaryRow
                  label={transferT('totalAmountLabel')}
                  value={formatCurrency(totalAmount)}
                />
                <SummaryRow
                  label={transferT('paymentMethodLabel')}
                  value={getPaymentMethodLabel(paymentMethod, t)}
                />
              </div>
            </div>
          </section>

          <div className="mt-8 grid gap-8 lg:grid-cols-[minmax(0,1fr)_360px]">
            <section className="space-y-6">
              {isBankTransferOrder ? (
                <>
                  <div className="rounded-3xl border border-border bg-card p-6 shadow-sm">
                    <div className="flex flex-wrap items-start justify-between gap-4">
                      <div>
                        <h2 className="font-heading text-2xl font-bold">
                          {transferT('transferInstructionTitle')}
                        </h2>
                        <p className="mt-2 text-sm leading-6 text-muted-foreground">
                          {transferT('transferInstructionDescription')}
                        </p>
                      </div>
                      <Button
                        type="button"
                        variant="outline"
                        onClick={handleCopyTransferContent}
                      >
                        <Copy className="size-4" />
                        {transferT('copyButton')}
                      </Button>
                    </div>

                    <div className="mt-6 grid gap-4 md:grid-cols-3">
                      <DetailCard
                        label={transferT('bankNameLabel')}
                        value={bankInfo.bankName}
                      />
                      <DetailCard
                        label={transferT('accountNumberLabel')}
                        value={bankInfo.accountNumber}
                      />
                      <DetailCard
                        label={transferT('accountNameLabel')}
                        value={bankInfo.accountName}
                      />
                    </div>

                    <div className="mt-6 rounded-2xl border border-primary/20 bg-primary/5 p-5">
                      <p className="text-sm font-medium text-muted-foreground">
                        {transferT('transferContentLabel')}
                      </p>
                      <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                        <code className="rounded-xl bg-background px-4 py-3 font-mono text-base font-semibold text-primary">
                          {transferContent || transferT('emptyValue')}
                        </code>
                        <p className="text-sm text-muted-foreground">
                          {transferT('transferContentHint')}
                        </p>
                      </div>
                    </div>
                  </div>

                  <div className="rounded-3xl border border-border bg-card p-6 shadow-sm">
                    <div className="flex items-center gap-3">
                      <span className="flex size-11 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                        <QrCode className="size-5" />
                      </span>
                      <div>
                        <h2 className="font-heading text-xl font-bold">
                          {transferT('qrTitle')}
                        </h2>
                        <p className="text-sm text-muted-foreground">
                          {transferT('qrDescription')}
                        </p>
                      </div>
                    </div>

                    <div className="mt-5 overflow-hidden rounded-3xl border border-dashed border-border bg-white">
                      {qrDisplay.url && !hasQrImageError ? (
                        <img
                          key={qrDisplay.url}
                          src={qrDisplay.url}
                          alt={transferT('qrTitle')}
                          onError={() => setHasQrImageError(true)}
                          className="h-[320px] w-full object-contain p-6"
                        />
                      ) : (
                        <div className="flex h-[320px] flex-col items-center justify-center gap-3 px-6 text-center">
                          <AlertTriangle className="size-14 text-amber-600" />
                          <p className="font-semibold">
                            {hasQrImageError
                              ? transferT('qrImageErrorTitle')
                              : transferT('qrUnavailableTitle')}
                          </p>
                          <p className="max-w-md text-sm leading-6 text-muted-foreground">
                            {hasQrImageError
                              ? transferT('qrImageErrorDescription')
                              : transferT('qrUnavailableDescription')}
                          </p>
                        </div>
                      )}
                    </div>

                    {qrDisplay.kind === 'fallback' && !hasQrImageError && (
                      <div className="mt-4 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-4 text-sm text-amber-900 dark:border-amber-400/25 dark:bg-amber-400/10 dark:text-amber-200">
                        <p className="font-semibold">
                          {transferT('qrFallbackNoticeTitle')}
                        </p>
                        <p className="mt-1 leading-6">
                          {transferT('qrFallbackNoticeDescription')}
                        </p>
                      </div>
                    )}

                    {(qrDisplay.kind === 'none' || hasQrImageError) && (
                      <div className="mt-4 rounded-2xl border border-border bg-background px-4 py-4">
                        <p className="font-semibold">
                          {transferT('manualTransferTitle')}
                        </p>
                        <p className="mt-1 text-sm leading-6 text-muted-foreground">
                          {transferT('manualTransferDescription')}
                        </p>
                      </div>
                    )}
                  </div>
                </>
              ) : (
                <div className="rounded-3xl border border-border bg-card p-6 shadow-sm">
                  <div className="flex items-center gap-3">
                    <span className="flex size-11 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                      <PackageCheck className="size-5" />
                    </span>
                    <div>
                      <h2 className="font-heading text-2xl font-bold">
                        {cashOnDeliveryT('instructionTitle')}
                      </h2>
                      <p className="mt-2 text-sm leading-6 text-muted-foreground">
                        {cashOnDeliveryT('instructionDescription')}
                      </p>
                    </div>
                  </div>

                  <div className="mt-6 grid gap-4 md:grid-cols-3">
                    <DetailCard
                      label={cashOnDeliveryT('paymentLabel')}
                      value={cashOnDeliveryT('paymentValue')}
                    />
                    <DetailCard
                      label={cashOnDeliveryT('deliveryFeeLabel')}
                      value={cashOnDeliveryT('deliveryFeeValue')}
                    />
                    <DetailCard
                      label={cashOnDeliveryT('nextStepLabel')}
                      value={cashOnDeliveryT('nextStepValue')}
                    />
                  </div>

                  <div className="mt-6 rounded-2xl border border-amber-200 bg-amber-50 px-5 py-4 text-sm text-amber-900 dark:border-amber-400/25 dark:bg-amber-400/10 dark:text-amber-200">
                    <p className="font-semibold">
                      {cashOnDeliveryT('noteTitle')}
                    </p>
                    <p className="mt-2 leading-6">
                      {cashOnDeliveryT('noteDescription')}
                    </p>
                  </div>
                </div>
              )}

              {error && (
                <div className="rounded-2xl border border-destructive/30 bg-destructive/5 px-5 py-4 text-sm text-destructive">
                  {error}
                </div>
              )}
            </section>

            <aside className="space-y-6">
              <div className="rounded-3xl border border-border bg-card p-6 shadow-sm">
                <div className="flex items-center gap-3">
                  <span className="flex size-11 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                    <Landmark className="size-5" />
                  </span>
                  <div>
                    <h2 className="font-heading text-xl font-bold">{transferT('orderSummaryTitle')}</h2>
                    <p className="text-sm text-muted-foreground">
                      {isBankTransferOrder
                        ? transferT('summaryDescription')
                        : cashOnDeliveryT('summaryDescription')}
                    </p>
                  </div>
                </div>

                <div className="mt-5 space-y-3">
                  <SummaryRow label={transferT('orderIdLabel')} value={orderId || transferT('emptyValue')} />
                  <SummaryRow label={transferT('orderCodeLabel')} value={orderCode || transferT('emptyValue')} />
                  {isBankTransferOrder ? (
                    <SummaryRow
                      label={transferT('transferContentLabel')}
                      value={transferContent || transferT('emptyValue')}
                    />
                  ) : null}
                  <SummaryRow
                    label={transferT('paymentStatusLabel')}
                    value={getPaymentStatusLabel(paymentStatus, t)}
                  />
                  <SummaryRow
                    label={transferT('totalAmountLabel')}
                    value={formatCurrency(totalAmount)}
                    emphasized
                  />
                </div>
              </div>

              {order && (
                <div className="rounded-3xl border border-border bg-card p-6 shadow-sm">
                  <h2 className="font-heading text-xl font-bold">
                    {transferT('receiverInfoTitle')}
                  </h2>
                  <div className="mt-5 space-y-3">
                    <SummaryRow label={transferT('receiverNameLabel')} value={order.receiverName} />
                    <SummaryRow label={transferT('receiverPhoneLabel')} value={order.receiverPhone} />
                    <SummaryRow
                      label={transferT('receiverAddressLabel')}
                      value={order.receiverAddress}
                    />
                  </div>
                </div>
              )}

              <div className="space-y-3">
                <Link to="/orders" className="block">
                  <Button className="h-11 w-full">{transferT('viewOrdersButton')}</Button>
                </Link>
                <Link to="/books" className="block">
                  <Button variant="outline" className="h-11 w-full">
                    {transferT('continueShoppingButton')}
                  </Button>
                </Link>
              </div>

              {isLoading && !order && (
                <p className="text-center text-sm text-muted-foreground">
                  {transferT('loadingNotice')}
                </p>
              )}
            </aside>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  )
}

function DetailCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-2xl bg-muted/40 p-4">
      <p className="text-xs uppercase tracking-wide text-muted-foreground">
        {label}
      </p>
      <p className="mt-2 break-words font-semibold">{value}</p>
    </div>
  )
}

function SummaryRow({
  label,
  value,
  emphasized = false,
}: {
  label: string
  value: string
  emphasized?: boolean
}) {
  return (
    <div className="flex items-start justify-between gap-4 border-b border-border py-3 last:border-b-0 last:pb-0">
      <span className="text-sm text-muted-foreground">{label}</span>
      <span
        className={cn(
          'max-w-[62%] text-right text-sm font-medium break-words',
          emphasized && 'font-heading text-base font-bold text-primary',
        )}
      >
        {value}
      </span>
    </div>
  )
}

function getStatusMeta(
  paymentStatus: PaymentStatus,
  paymentMethod: 'BANK_TRANSFER_QR' | 'COD',
  transferT: (key: string) => string,
  cashOnDeliveryT: (key: string) => string,
) {
  const config: Record<
    PaymentStatus,
    {
      title: string
      description: string
      icon: LucideIcon
      containerClassName: string
      iconContainerClassName: string
      iconClassName: string
      badgeClassName: string
    }
  > = {
    PENDING: {
      title:
        paymentMethod === 'COD'
          ? cashOnDeliveryT('waitingTitle')
          : transferT('waitingTitle'),
      description:
        paymentMethod === 'COD'
          ? cashOnDeliveryT('waitingDescription')
          : transferT('waitingDescription'),
      icon: Clock3,
      containerClassName:
        'border-amber-200 bg-amber-50/80 dark:border-amber-400/25 dark:bg-amber-400/10',
      iconContainerClassName: 'bg-amber-100 dark:bg-amber-400/15',
      iconClassName: 'text-amber-700 dark:text-amber-300',
      badgeClassName:
        'bg-amber-100 text-amber-800 dark:bg-amber-400/15 dark:text-amber-200',
    },
    PAID: {
      title: transferT('paidTitle'),
      description: transferT('paidDescription'),
      icon: CheckCircle2,
      containerClassName:
        'border-emerald-200 bg-emerald-50/80 dark:border-emerald-400/25 dark:bg-emerald-400/10',
      iconContainerClassName: 'bg-emerald-100 dark:bg-emerald-400/15',
      iconClassName: 'text-emerald-700 dark:text-emerald-300',
      badgeClassName:
        'bg-emerald-100 text-emerald-800 dark:bg-emerald-400/15 dark:text-emerald-200',
    },
    FAILED: {
      title: transferT('failedTitle'),
      description: transferT('failedDescription'),
      icon: AlertTriangle,
      containerClassName:
        'border-rose-200 bg-rose-50/80 dark:border-rose-400/25 dark:bg-rose-400/10',
      iconContainerClassName: 'bg-rose-100 dark:bg-rose-400/15',
      iconClassName: 'text-rose-700 dark:text-rose-300',
      badgeClassName:
        'bg-rose-100 text-rose-800 dark:bg-rose-400/15 dark:text-rose-200',
    },
    CANCELLED: {
      title: transferT('cancelledTitle'),
      description: transferT('cancelledDescription'),
      icon: XCircle,
      containerClassName:
        'border-slate-200 bg-slate-50/80 dark:border-slate-400/25 dark:bg-slate-400/10',
      iconContainerClassName: 'bg-slate-200 dark:bg-slate-400/15',
      iconClassName: 'text-slate-700 dark:text-slate-300',
      badgeClassName:
        'bg-slate-200 text-slate-800 dark:bg-slate-400/15 dark:text-slate-200',
    },
    EXPIRED: {
      title: 'Đơn hàng đã hết hạn thanh toán',
      description: 'Đơn hàng đã được hủy để hoàn lại tồn kho và ưu đãi đã giữ.',
      icon: XCircle,
      containerClassName:
        'border-slate-200 bg-slate-50/80 dark:border-slate-400/25 dark:bg-slate-400/10',
      iconContainerClassName: 'bg-slate-200 dark:bg-slate-400/15',
      iconClassName: 'text-slate-700 dark:text-slate-300',
      badgeClassName:
        'bg-slate-200 text-slate-800 dark:bg-slate-400/15 dark:text-slate-200',
    },
  }

  return config[paymentStatus]
}

function usePaymentExpiryCountdown(
  paymentExpiresAt: string | null,
  paymentStatus: PaymentStatus,
) {
  const [now, setNow] = useState(() => Date.now())

  useEffect(() => {
    if (!paymentExpiresAt || paymentStatus !== 'PENDING') {
      return
    }

    setNow(Date.now())
    const intervalId = window.setInterval(() => setNow(Date.now()), 1000)
    return () => window.clearInterval(intervalId)
  }, [paymentExpiresAt, paymentStatus])

  if (!paymentExpiresAt || paymentStatus !== 'PENDING') {
    return null
  }

  const expiry = Date.parse(paymentExpiresAt)
  return Number.isFinite(expiry) ? Math.max(0, Math.ceil((expiry - now) / 1000)) : null
}

function formatRemainingTime(seconds: number) {
  const minutes = Math.floor(seconds / 60)
  const remainderSeconds = seconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(remainderSeconds).padStart(2, '0')}`
}

function readConfiguredValue(value: string | null | undefined) {
  const normalizedValue = value?.trim() || ''

  if (normalizedValue === '' || isPlaceholderValue(normalizedValue)) {
    return null
  }

  return normalizedValue
}

function isPlaceholderValue(value: string) {
  const normalizedValue = value.toLowerCase()

  return (
    normalizedValue.includes('your-demo') ||
    normalizedValue.includes('your_account') ||
    normalizedValue.includes('your-account') ||
    normalizedValue.includes('placeholder') ||
    normalizedValue.includes('change-me') ||
    normalizedValue.includes('changeme') ||
    normalizedValue.includes('<') ||
    normalizedValue.includes('>')
  )
}

function readVietQrConfig(qrUrl: string | null) {
  if (!qrUrl) {
    return null
  }

  try {
    const url = new URL(qrUrl)

    if (url.hostname.toLowerCase() !== 'img.vietqr.io') {
      return null
    }

    const fileName = decodeURIComponent(url.pathname.split('/').pop() || '')
    const accountPath = fileName.replace(
      /-(?:compact2?|print)\.(?:png|jpe?g)$/i,
      '',
    )
    const separatorIndex = accountPath.indexOf('-')

    if (separatorIndex <= 0 || separatorIndex === accountPath.length - 1) {
      return null
    }

    const bankCode = readConfiguredValue(accountPath.slice(0, separatorIndex))
    const accountNumber = readConfiguredValue(
      accountPath.slice(separatorIndex + 1),
    )
    const accountName = readConfiguredValue(
      url.searchParams.get('accountName') ?? undefined,
    )

    if (!bankCode || !accountNumber) {
      return null
    }

    return {
      bankCode,
      accountNumber,
      accountName,
    }
  } catch {
    return null
  }
}

function buildVietQrUrl(
  bankCode: string | null,
  accountNumber: string | null,
  accountName: string | null,
  amount: number,
  content: string | null,
) {
  if (!bankCode || !accountNumber || !accountName) {
    return null
  }

  const roundedAmount = Math.max(0, Math.round(amount))
  const query = new URLSearchParams()

  query.set('amount', String(roundedAmount))

  if (content && content.trim() !== '') {
    query.set('addInfo', content.trim())
  }

  query.set('accountName', accountName)

  return `https://img.vietqr.io/image/${bankCode}-${accountNumber}-compact2.png?${query.toString()}`
}

function buildFallbackQrUrl(
  fallbackUrl: string | null | undefined,
  amount: number,
  content: string | null,
  accountName: string | null,
) {
  const normalizedFallbackUrl = readConfiguredValue(fallbackUrl)

  if (!normalizedFallbackUrl) {
    return null
  }

  try {
    const url = new URL(normalizedFallbackUrl)
    const roundedAmount = Math.max(0, Math.round(amount))

    url.searchParams.set('amount', String(roundedAmount))

    if (content && content.trim() !== '') {
      url.searchParams.set('addInfo', content.trim())
    }

    if (accountName) {
      url.searchParams.set('accountName', accountName)
    }

    return url.toString()
  } catch {
    return normalizedFallbackUrl
  }
}
