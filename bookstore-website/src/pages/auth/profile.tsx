import { Link, useNavigate } from 'react-router-dom'
import { LogOut, Mail, Package } from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useProfilePage } from '@/hooks/use-profile-page'
import type { OrderResponse, OrderStatus } from '@/types/order'
import type { ProfileResponse } from '@/types/profile'
import { getGenderLabel, getOrderStatusLabel, getUserRoleLabel } from '@/utils/i18n'

const STATUS_VARIANTS: Record<
  OrderStatus,
  'default' | 'secondary' | 'outline' | 'destructive'
> = {
  PENDING: 'secondary',
  CONFIRMED: 'default',
  SHIPPING: 'outline',
  DELIVERED: 'default',
  CANCELLED: 'destructive',
}

export default function ProfilePage() {
  const { t, formatCurrency, formatDate } = useLanguage()
  const {
    user,
    orders,
    profile,
    isLoading,
    isSavingAccount,
    isSavingProfile,
    accountForm,
    profileForm,
    handleAccountChange,
    handleProfileInputChange,
    handleProfileGenderChange,
    handleLogout,
    handleSaveAccount,
    handleSaveProfile,
  } = useProfilePage()

  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Header />

      <main className="flex-1 py-12">
        <div className="mx-auto max-w-6xl px-4">
          <div className="mb-12 rounded-2xl border border-border bg-card p-8">
            <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
              <div className="flex items-center gap-6">
                <div className="flex h-20 w-20 items-center justify-center overflow-hidden rounded-full bg-primary/10 text-4xl">
                  {profile?.avatarUrl ? (
                    <img
                      src={profile.avatarUrl}
                      alt={user?.name}
                      className="size-full object-cover"
                    />
                  ) : (
                    user?.avatar
                  )}
                </div>
                <div>
                  <h1 className="font-heading text-3xl font-bold text-foreground">
                    {user?.name}
                  </h1>
                  <p className="mt-1 flex items-center gap-2 text-muted-foreground">
                    <Mail className="h-4 w-4" />
                    {user?.email}
                  </p>
                  <div className="mt-3 flex flex-wrap items-center gap-2">
                    <span className="rounded-full bg-primary/10 px-3 py-1 text-xs font-semibold text-primary">
                      {user ? getUserRoleLabel(user.role, t) : ''}
                    </span>
                    {profile && (
                      <span className="rounded-full bg-muted px-3 py-1 text-xs font-medium text-muted-foreground">
                        {getGenderLabel(profile.gender, t)}
                      </span>
                    )}
                  </div>
                </div>
              </div>
              <Button onClick={handleLogout} variant="outline" size="sm">
                <LogOut className="mr-2 h-4 w-4" />
                {t('auth.profile.logout')}
              </Button>
            </div>
          </div>

          <div className="grid gap-8 lg:grid-cols-2">
            <form
              onSubmit={handleSaveAccount}
              className="rounded-2xl border border-border bg-card p-6"
            >
              <h2 className="font-heading text-2xl font-bold">
                {t('auth.profile.accountTitle')}
              </h2>
              <div className="mt-6 space-y-4">
                <div>
                  <Label htmlFor="username">{t('auth.profile.username')}</Label>
                  <Input
                    id="username"
                    value={accountForm.username}
                    onChange={(event) =>
                      handleAccountChange('username', event.currentTarget.value)
                    }
                    className="mt-2"
                  />
                </div>
                <div>
                  <Label htmlFor="accountEmail">{t('common.email')}</Label>
                  <Input
                    id="accountEmail"
                    type="email"
                    value={accountForm.email}
                    onChange={(event) =>
                      handleAccountChange('email', event.currentTarget.value)
                    }
                    className="mt-2"
                  />
                </div>
                <div>
                  <Label htmlFor="accountPhone">{t('common.phone')}</Label>
                  <Input
                    id="accountPhone"
                    value={accountForm.phoneNumber}
                    onChange={(event) =>
                      handleAccountChange(
                        'phoneNumber',
                        event.currentTarget.value,
                      )
                    }
                    className="mt-2"
                  />
                </div>
              </div>
              <Button
                type="submit"
                className="mt-6"
                disabled={isSavingAccount || isLoading}
              >
                {isSavingAccount
                  ? t('common.processing')
                  : t('auth.profile.saveAccount')}
              </Button>
            </form>

            <form
              onSubmit={handleSaveProfile}
              className="rounded-2xl border border-border bg-card p-6"
            >
              <h2 className="font-heading text-2xl font-bold">
                {t('auth.profile.personalTitle')}
              </h2>
              <div className="mt-6 space-y-4">
                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <Label htmlFor="lastName">{t('auth.profile.lastName')}</Label>
                    <Input
                      id="lastName"
                      value={profileForm.lastName}
                      onChange={(event) =>
                        handleProfileInputChange(
                          'lastName',
                          event.currentTarget.value,
                        )
                      }
                      className="mt-2"
                    />
                  </div>
                  <div>
                    <Label htmlFor="firstName">{t('auth.profile.firstName')}</Label>
                    <Input
                      id="firstName"
                      value={profileForm.firstName}
                      onChange={(event) =>
                        handleProfileInputChange(
                          'firstName',
                          event.currentTarget.value,
                        )
                      }
                      className="mt-2"
                    />
                  </div>
                </div>
                <div>
                  <Label htmlFor="avatarUrl">{t('auth.profile.avatarUrl')}</Label>
                  <Input
                    id="avatarUrl"
                    value={profileForm.avatarUrl}
                    onChange={(event) =>
                      handleProfileInputChange(
                        'avatarUrl',
                        event.currentTarget.value,
                      )
                    }
                    className="mt-2"
                  />
                </div>
                <div className="grid gap-4 sm:grid-cols-2">
                  <div>
                    <Label htmlFor="gender">{t('auth.profile.gender')}</Label>
                    <select
                      id="gender"
                      value={profileForm.gender}
                      onChange={(event) =>
                        handleProfileGenderChange(
                          event.currentTarget.value as ProfileResponse['gender'],
                        )
                      }
                      className="mt-2 h-10 w-full rounded-md border border-border bg-background px-3 text-sm"
                    >
                      {(['MALE', 'FEMALE', 'OTHER'] as const).map((gender) => (
                        <option key={gender} value={gender}>
                          {getGenderLabel(gender, t)}
                        </option>
                      ))}
                    </select>
                  </div>
                  <div>
                    <Label htmlFor="dateOfBirth">
                      {t('auth.profile.dateOfBirth')}
                    </Label>
                    <Input
                      id="dateOfBirth"
                      type="date"
                      value={profileForm.dateOfBirth}
                      onChange={(event) =>
                        handleProfileInputChange(
                          'dateOfBirth',
                          event.currentTarget.value,
                        )
                      }
                      className="mt-2"
                    />
                  </div>
                </div>
              </div>
              <Button
                type="submit"
                className="mt-6"
                disabled={isSavingProfile || isLoading}
              >
                {isSavingProfile
                  ? t('common.processing')
                  : t('auth.profile.saveProfile')}
              </Button>
            </form>
          </div>

          <div className="mt-12">
            <div className="mb-6 flex items-center justify-between gap-4">
              <h2 className="flex items-center gap-2 font-heading text-2xl font-bold text-foreground">
                <Package className="h-6 w-6" />
                {t('auth.profile.ordersTitle')}
              </h2>
              <Link to="/orders">
                <Button variant="outline" size="sm">
                  {t('orders.title')}
                </Button>
              </Link>
            </div>

            {orders.length === 0 ? (
              <div className="rounded-lg border border-border bg-card p-8 text-center">
                <p className="text-muted-foreground">
                  {t('auth.profile.emptyOrders')}
                </p>
              </div>
            ) : (
              <div className="space-y-4">
                {orders.slice(0, 3).map((order) => (
                  <div
                    key={order.orderId}
                    className="rounded-lg border border-border bg-card p-6"
                  >
                    <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                      <div>
                        <h3 className="font-semibold text-foreground">
                          {order.orderId}
                        </h3>
                        <p className="text-sm text-muted-foreground">
                          {formatDate(order.createdAt)}
                        </p>
                      </div>
                      <Badge variant={STATUS_VARIANTS[order.status]}>
                        {getOrderStatusLabel(order.status, t)}
                      </Badge>
                    </div>

                    <div className="mb-4 space-y-2 border-t border-border pt-4">
                      {order.items.map((item) => (
                        <div
                          key={item.id}
                          className="flex justify-between text-sm"
                        >
                          <span className="text-foreground">
                            {item.bookTitle} x{item.quantity}
                          </span>
                          <span className="text-muted-foreground">
                            {formatCurrency(item.lineTotal)}
                          </span>
                        </div>
                      ))}
                    </div>

                    <div className="flex items-center justify-between border-t border-border pt-4">
                      <div className="text-right">
                        <p className="text-xs text-muted-foreground">
                          {t('auth.profile.orderTotal')}
                        </p>
                        <p className="font-bold text-primary">
                          {formatCurrency(order.finalAmount)}
                        </p>
                      </div>
                      <Link to={`/orders/${order.orderId}`}>
                        <Button size="sm">{t('orders.viewDetail')}</Button>
                      </Link>
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
