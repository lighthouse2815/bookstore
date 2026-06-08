import { useNavigate } from 'react-router-dom'
import { LogOut, Mail, Package, User } from 'lucide-react'
import { Button } from '@/components/common/button'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import { getOrderStatusLabel, getUserRoleLabel } from '@/utils/i18n'

const MOCK_ORDERS = [
  {
    id: 'ORD-001',
    date: '2024-06-01',
    items: [{ title: 'ĐẮC NHÂN TÂM', qty: 1, price: 189000 }],
    total: 219000,
    status: 'delivered',
  },
  {
    id: 'ORD-002',
    date: '2024-05-28',
    items: [
      { title: 'NHÀ GIẢ KIM', qty: 2, price: 99000 },
      { title: 'ATOMIC HABITS', qty: 1, price: 165000 },
    ],
    total: 419000,
    status: 'delivered',
  },
]

const STATUS_COLORS: Record<string, string> = {
  pending: 'bg-yellow-100 text-yellow-800',
  processing: 'bg-blue-100 text-blue-800',
  shipped: 'bg-purple-100 text-purple-800',
  delivered: 'bg-green-100 text-green-800',
  cancelled: 'bg-red-100 text-red-800',
}

export default function ProfilePage() {
  const { user, logout } = useAuth()
  const { t, formatCurrency, formatDate } = useLanguage()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/')
  }

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />

      <main className="flex-1 py-12">
        <div className="mx-auto max-w-4xl px-4">
          <div className="mb-12 rounded-lg border border-border bg-card p-8">
            <div className="flex items-start justify-between">
              <div className="flex items-center gap-6">
                <div className="flex h-20 w-20 items-center justify-center rounded-full bg-primary/10 text-4xl">
                  {user?.avatar}
                </div>
                <div>
                  <h1 className="font-heading text-3xl font-bold text-foreground">
                    {user?.name}
                  </h1>
                  <p className="mt-1 flex items-center gap-2 text-muted-foreground">
                    <Mail className="h-4 w-4" />
                    {user?.email}
                  </p>
                  <p className="mt-2 flex items-center gap-2">
                    <User className="h-4 w-4" />
                    <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">
                      {user ? getUserRoleLabel(user.role, t) : ''}
                    </span>
                  </p>
                </div>
              </div>
              <Button onClick={handleLogout} variant="outline" size="sm">
                <LogOut className="mr-2 h-4 w-4" />
                {t('auth.profile.logout')}
              </Button>
            </div>
          </div>

          <div>
            <h2 className="mb-6 flex items-center gap-2 font-heading text-2xl font-bold text-foreground">
              <Package className="h-6 w-6" />
              {t('auth.profile.ordersTitle')}
            </h2>

            {MOCK_ORDERS.length === 0 ? (
              <div className="rounded-lg border border-border bg-card p-8 text-center">
                <p className="text-muted-foreground">
                  {t('auth.profile.emptyOrders')}
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {MOCK_ORDERS.map((order) => (
                  <div
                    key={order.id}
                    className="rounded-lg border border-border bg-card p-6"
                  >
                    <div className="mb-4 flex items-center justify-between">
                      <div>
                        <h3 className="font-semibold text-foreground">
                          {order.id}
                        </h3>
                        <p className="text-sm text-muted-foreground">
                          {formatDate(order.date)}
                        </p>
                      </div>
                      <span
                        className={`rounded-full px-3 py-1 text-xs font-semibold ${STATUS_COLORS[order.status]}`}
                      >
                        {getOrderStatusLabel(order.status, t)}
                      </span>
                    </div>

                    <div className="mb-4 space-y-2 border-t border-border pt-4">
                      {order.items.map((item, index) => (
                        <div
                          key={`${order.id}-${index}`}
                          className="flex justify-between text-sm"
                        >
                          <span className="text-foreground">
                            {item.title} x{item.qty}
                          </span>
                          <span className="text-muted-foreground">
                            {formatCurrency(item.price * item.qty)}
                          </span>
                        </div>
                      ))}
                    </div>

                    <div className="flex justify-end border-t border-border pt-4">
                      <div className="text-right">
                        <p className="text-xs text-muted-foreground">
                          {t('auth.profile.orderTotal')}
                        </p>
                        <p className="font-bold text-primary">
                          {formatCurrency(order.total)}
                        </p>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>

      <Footer />
    </div>
  )
}
