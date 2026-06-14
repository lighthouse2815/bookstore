import { useMemo } from 'react'
import { Link } from 'react-router-dom'
import {
  AlertTriangle,
  CheckCircle2,
  Clock3,
  Copy,
  Landmark,
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
  const { t, language, formatCurrency } = useLanguage()
  const labels = getPaymentWaitingLabels(language)
  const {
    order,
    orderId,
    orderCode,
    transferContent,
    totalAmount,
    paymentMethod,
    paymentStatus,
    isLoading,
    isPolling,
    error,
  } = useOrderConfirmationPage()
  const bankInfo = useMemo(
    () => ({
      bankName:
        import.meta.env.VITE_BANK_TRANSFER_BANK_NAME?.trim() || labels.bankFallback,
      accountNumber:
        import.meta.env.VITE_BANK_TRANSFER_ACCOUNT_NUMBER?.trim() ||
        labels.accountNumberFallback,
      accountName:
        import.meta.env.VITE_BANK_TRANSFER_ACCOUNT_NAME?.trim() ||
        labels.accountNameFallback,
      qrUrl: import.meta.env.VITE_BANK_TRANSFER_QR_URL?.trim() || null,
    }),
    [
      labels.accountNameFallback,
      labels.accountNumberFallback,
      labels.bankFallback,
    ],
  )
  const statusMeta = getStatusMeta(paymentStatus, labels)

  async function handleCopyTransferContent() {
    try {
      await navigator.clipboard.writeText(transferContent)
      toast.success(labels.copySuccess)
    } catch {
      toast.error(labels.copyError)
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
                  {isPolling && paymentStatus === 'PENDING' && (
                    <p className="mt-4 inline-flex items-center gap-2 text-sm font-medium text-amber-800">
                      <RefreshCcw className="size-4 animate-spin" />
                      {labels.pollingNotice}
                    </p>
                  )}
                </div>
              </div>

              <div className="min-w-0 rounded-2xl border border-border/70 bg-background/90 p-4 sm:min-w-[280px]">
                <SummaryRow label={labels.orderIdLabel} value={orderId || labels.emptyValue} />
                <SummaryRow label={labels.orderCodeLabel} value={orderCode || labels.emptyValue} />
                <SummaryRow
                  label={labels.totalAmountLabel}
                  value={formatCurrency(totalAmount)}
                />
                <SummaryRow
                  label={labels.paymentMethodLabel}
                  value={getPaymentMethodLabel(paymentMethod, t)}
                />
              </div>
            </div>
          </section>

          <div className="mt-8 grid gap-8 lg:grid-cols-[minmax(0,1fr)_360px]">
            <section className="space-y-6">
              <div className="rounded-3xl border border-border bg-card p-6 shadow-sm">
                <div className="flex flex-wrap items-start justify-between gap-4">
                  <div>
                    <h2 className="font-heading text-2xl font-bold">
                      {labels.transferInstructionTitle}
                    </h2>
                    <p className="mt-2 text-sm leading-6 text-muted-foreground">
                      {labels.transferInstructionDescription}
                    </p>
                  </div>
                  <Button type="button" variant="outline" onClick={handleCopyTransferContent}>
                    <Copy className="size-4" />
                    {labels.copyButton}
                  </Button>
                </div>

                <div className="mt-6 grid gap-4 md:grid-cols-3">
                  <DetailCard
                    label={labels.bankNameLabel}
                    value={bankInfo.bankName}
                  />
                  <DetailCard
                    label={labels.accountNumberLabel}
                    value={bankInfo.accountNumber}
                  />
                  <DetailCard
                    label={labels.accountNameLabel}
                    value={bankInfo.accountName}
                  />
                </div>

                <div className="mt-6 rounded-2xl border border-primary/20 bg-primary/5 p-5">
                  <p className="text-sm font-medium text-muted-foreground">
                    {labels.transferContentLabel}
                  </p>
                  <div className="mt-3 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <code className="rounded-xl bg-background px-4 py-3 font-mono text-base font-semibold text-primary">
                      {transferContent || labels.emptyValue}
                    </code>
                    <p className="text-sm text-muted-foreground">
                      {labels.transferContentHint}
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
                    <h2 className="font-heading text-xl font-bold">{labels.qrTitle}</h2>
                    <p className="text-sm text-muted-foreground">
                      {labels.qrDescription}
                    </p>
                  </div>
                </div>

                <div className="mt-5 overflow-hidden rounded-3xl border border-dashed border-border bg-muted/30">
                  {bankInfo.qrUrl ? (
                    <img
                      src={bankInfo.qrUrl}
                      alt={labels.qrTitle}
                      className="h-[320px] w-full object-contain p-6"
                    />
                  ) : (
                    <div className="flex h-[320px] flex-col items-center justify-center gap-3 px-6 text-center">
                      <QrCode className="size-14 text-muted-foreground" />
                      <p className="font-semibold">{labels.qrPlaceholderTitle}</p>
                      <p className="max-w-md text-sm leading-6 text-muted-foreground">
                        {labels.qrPlaceholderDescription}
                      </p>
                    </div>
                  )}
                </div>
              </div>

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
                    <h2 className="font-heading text-xl font-bold">{labels.orderSummaryTitle}</h2>
                    <p className="text-sm text-muted-foreground">
                      {labels.summaryDescription}
                    </p>
                  </div>
                </div>

                <div className="mt-5 space-y-3">
                  <SummaryRow label={labels.orderIdLabel} value={orderId || labels.emptyValue} />
                  <SummaryRow label={labels.orderCodeLabel} value={orderCode || labels.emptyValue} />
                  <SummaryRow
                    label={labels.transferContentLabel}
                    value={transferContent || labels.emptyValue}
                  />
                  <SummaryRow
                    label={labels.paymentStatusLabel}
                    value={getPaymentStatusLabel(paymentStatus, t)}
                  />
                  <SummaryRow
                    label={labels.totalAmountLabel}
                    value={formatCurrency(totalAmount)}
                    emphasized
                  />
                </div>
              </div>

              {order && (
                <div className="rounded-3xl border border-border bg-card p-6 shadow-sm">
                  <h2 className="font-heading text-xl font-bold">
                    {labels.receiverInfoTitle}
                  </h2>
                  <div className="mt-5 space-y-3">
                    <SummaryRow label={labels.receiverNameLabel} value={order.receiverName} />
                    <SummaryRow label={labels.receiverPhoneLabel} value={order.receiverPhone} />
                    <SummaryRow
                      label={labels.receiverAddressLabel}
                      value={order.receiverAddress}
                    />
                  </div>
                </div>
              )}

              <div className="space-y-3">
                <Link to="/orders" className="block">
                  <Button className="h-11 w-full">{labels.viewOrdersButton}</Button>
                </Link>
                <Link to="/books" className="block">
                  <Button variant="outline" className="h-11 w-full">
                    {labels.continueShoppingButton}
                  </Button>
                </Link>
              </div>

              {isLoading && !order && (
                <p className="text-center text-sm text-muted-foreground">
                  {labels.loadingNotice}
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
  labels: ReturnType<typeof getPaymentWaitingLabels>,
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
      title: labels.waitingTitle,
      description: labels.waitingDescription,
      icon: Clock3,
      containerClassName: 'border-amber-200 bg-amber-50/80',
      iconContainerClassName: 'bg-amber-100',
      iconClassName: 'text-amber-700',
      badgeClassName: 'bg-amber-100 text-amber-800',
    },
    PAID: {
      title: labels.paidTitle,
      description: labels.paidDescription,
      icon: CheckCircle2,
      containerClassName: 'border-emerald-200 bg-emerald-50/80',
      iconContainerClassName: 'bg-emerald-100',
      iconClassName: 'text-emerald-700',
      badgeClassName: 'bg-emerald-100 text-emerald-800',
    },
    FAILED: {
      title: labels.failedTitle,
      description: labels.failedDescription,
      icon: AlertTriangle,
      containerClassName: 'border-rose-200 bg-rose-50/80',
      iconContainerClassName: 'bg-rose-100',
      iconClassName: 'text-rose-700',
      badgeClassName: 'bg-rose-100 text-rose-800',
    },
    CANCELLED: {
      title: labels.cancelledTitle,
      description: labels.cancelledDescription,
      icon: XCircle,
      containerClassName: 'border-slate-200 bg-slate-50/80',
      iconContainerClassName: 'bg-slate-200',
      iconClassName: 'text-slate-700',
      badgeClassName: 'bg-slate-200 text-slate-800',
    },
  }

  return config[paymentStatus]
}

function getPaymentWaitingLabels(language: 'vi' | 'en') {
  if (language === 'en') {
    return {
      waitingTitle: 'Waiting for your bank transfer',
      waitingDescription:
        'Your order has been created. Complete the transfer with the exact content below so the backend can match the payment automatically.',
      paidTitle: 'Payment confirmed successfully',
      paidDescription:
        'The backend has marked this order as paid. No client-side status was forced.',
      failedTitle: 'Payment failed',
      failedDescription:
        'The backend marked this payment as failed. Please review the transfer and try again if needed.',
      cancelledTitle: 'Payment cancelled',
      cancelledDescription:
        'The backend marked this payment as cancelled. Start a new checkout if you still want to place the order.',
      pollingNotice: 'Checking payment status every 4 seconds.',
      transferInstructionTitle: 'Transfer instructions',
      transferInstructionDescription:
        'Use the bank account below and keep the transfer content exactly the same.',
      bankNameLabel: 'Bank',
      accountNumberLabel: 'Account number',
      accountNameLabel: 'Account name',
      transferContentLabel: 'Transfer content',
      transferContentHint:
        'Copy this content exactly so SePay can reconcile the payment.',
      copyButton: 'Copy content',
      copySuccess: 'Transfer content copied.',
      copyError: 'Unable to copy transfer content.',
      qrTitle: 'QR payment area',
      qrDescription: 'Use a configured static QR image or transfer manually.',
      qrPlaceholderTitle: 'Static QR placeholder',
      qrPlaceholderDescription:
        'Add VITE_BANK_TRANSFER_QR_URL to show a static QR image here.',
      orderSummaryTitle: 'Order summary',
      summaryDescription: 'This screen only reflects payment data from the backend.',
      orderIdLabel: 'Order ID',
      orderCodeLabel: 'Order code',
      totalAmountLabel: 'Total amount',
      paymentMethodLabel: 'Payment method',
      paymentStatusLabel: 'Payment status',
      receiverInfoTitle: 'Receiver information',
      receiverNameLabel: 'Receiver',
      receiverPhoneLabel: 'Phone',
      receiverAddressLabel: 'Address',
      viewOrdersButton: 'View my orders',
      continueShoppingButton: 'Continue shopping',
      loadingNotice: 'Loading the latest payment status...',
      emptyValue: 'N/A',
      bankFallback: 'Update VITE_BANK_TRANSFER_BANK_NAME',
      accountNumberFallback: 'Update VITE_BANK_TRANSFER_ACCOUNT_NUMBER',
      accountNameFallback: 'Update VITE_BANK_TRANSFER_ACCOUNT_NAME',
    }
  }

  return {
    waitingTitle: 'Cho thanh toan chuyen khoan',
    waitingDescription:
      'Don hang da duoc tao. Hay chuyen khoan dung noi dung ben duoi de backend tu doi chieu thanh toan.',
    paidTitle: 'Thanh toan thanh cong',
    paidDescription:
      'Backend da cap nhat don hang nay la da thanh toan. Frontend khong tu set PAID.',
    failedTitle: 'Thanh toan that bai',
    failedDescription:
      'Backend da danh dau giao dich that bai. Hay kiem tra lai chuyen khoan neu can.',
    cancelledTitle: 'Thanh toan da huy',
    cancelledDescription:
      'Backend da danh dau giao dich bi huy. Hay tao checkout moi neu ban van muon dat hang.',
    pollingNotice: 'Dang kiem tra trang thai thanh toan moi 4 giay.',
    transferInstructionTitle: 'Huong dan chuyen khoan',
    transferInstructionDescription:
      'Dung thong tin tai khoan ben duoi va giu nguyen noi dung chuyen khoan.',
    bankNameLabel: 'Ngan hang',
    accountNumberLabel: 'So tai khoan',
    accountNameLabel: 'Chu tai khoan',
    transferContentLabel: 'Noi dung chuyen khoan',
    transferContentHint:
      'Hay copy dung noi dung nay de SePay doi chieu chinh xac.',
    copyButton: 'Copy noi dung',
    copySuccess: 'Da copy noi dung chuyen khoan.',
    copyError: 'Khong the copy noi dung chuyen khoan.',
    qrTitle: 'Khu vuc QR thanh toan',
    qrDescription: 'Dung QR tinh neu da cau hinh, hoac chuyen khoan thu cong.',
    qrPlaceholderTitle: 'Placeholder QR tinh',
    qrPlaceholderDescription:
      'Them VITE_BANK_TRANSFER_QR_URL neu muon hien QR tinh o day.',
    orderSummaryTitle: 'Tom tat thanh toan',
    summaryDescription:
      'Man hinh nay chi doc trang thai va du lieu thanh toan tu backend.',
    orderIdLabel: 'Ma don hang',
    orderCodeLabel: 'Ma giao dich',
    totalAmountLabel: 'Tong thanh toan',
    paymentMethodLabel: 'Phuong thuc thanh toan',
    paymentStatusLabel: 'Trang thai thanh toan',
    receiverInfoTitle: 'Thong tin nguoi nhan',
    receiverNameLabel: 'Nguoi nhan',
    receiverPhoneLabel: 'So dien thoai',
    receiverAddressLabel: 'Dia chi',
    viewOrdersButton: 'Xem don hang cua toi',
    continueShoppingButton: 'Tiep tuc mua sam',
    loadingNotice: 'Dang tai trang thai thanh toan moi nhat...',
    emptyValue: 'Khong co',
    bankFallback: 'Cap nhat VITE_BANK_TRANSFER_BANK_NAME',
    accountNumberFallback: 'Cap nhat VITE_BANK_TRANSFER_ACCOUNT_NUMBER',
    accountNameFallback: 'Cap nhat VITE_BANK_TRANSFER_ACCOUNT_NAME',
  }
}
