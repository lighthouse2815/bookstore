import { Link } from 'react-router-dom'
import { CheckCircle } from 'lucide-react'
import { Button } from '@/components/common/button'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useOrderConfirmationPage } from '@/hooks/use-order-confirmation-page'
import {
  getPaymentMethodLabel,
  getPaymentStatusLabel,
} from '@/utils/i18n'

export default function OrderConfirmationPage() {
  const { t, formatCurrency } = useLanguage()
  const { order, error } = useOrderConfirmationPage()

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />
      <main className="container mx-auto flex-1 px-4 py-12">
        <div className="mx-auto max-w-md text-center">
          <div className="mb-6 flex justify-center">
            <CheckCircle className="size-20 text-green-600" />
          </div>
          <h1 className="mb-2 font-heading text-3xl font-bold">
            {t('orderConfirmation.title')}
          </h1>
          <p className="mb-6 text-muted-foreground">
            {t('orderConfirmation.description')}
          </p>
          <p className="mb-8 text-sm text-muted-foreground">
            {t('orderConfirmation.emailNotice')}
          </p>
          {order && (
            <div className="mb-8 rounded-2xl border border-border bg-card p-5 text-left">
              <SummaryRow
                label={t('orderConfirmation.orderId')}
                value={order.orderId}
              />
              <SummaryRow
                label={t('orderConfirmation.receiver')}
                value={order.receiverName}
              />
              <SummaryRow
                label={t('orderConfirmation.paymentMethod')}
                value={getPaymentMethodLabel(order.paymentMethod, t)}
              />
              <SummaryRow
                label={t('orderConfirmation.paymentStatus')}
                value={getPaymentStatusLabel(order.paymentStatus, t)}
              />
              <SummaryRow
                label={t('orderConfirmation.total')}
                value={formatCurrency(order.finalAmount)}
              />
            </div>
          )}
          {error && <p className="mb-8 text-sm text-destructive">{error}</p>}
          <div className="space-y-3">
            <Link to="/books" className="block">
              <Button className="w-full">
                {t('common.continueShopping')}
              </Button>
            </Link>
            <Link to="/orders" className="block">
              <Button variant="outline" className="w-full">
                {t('orders.title')}
              </Button>
            </Link>
            <Link to="/" className="block">
              <Button variant="outline" className="w-full">
                {t('common.backHome')}
              </Button>
            </Link>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  )
}

function SummaryRow({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-center justify-between gap-4 border-b border-border py-2 last:border-b-0">
      <span className="text-sm text-muted-foreground">{label}</span>
      <span className="text-right text-sm font-medium">{value}</span>
    </div>
  )
}
