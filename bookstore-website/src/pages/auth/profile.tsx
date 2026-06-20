import { useEffect, useState, type ChangeEventHandler, type ReactNode } from 'react'
import { Link } from 'react-router-dom'
import {
  ArrowRight,
  BookOpen,
  Camera,
  KeyRound,
  LogOut,
  Mail,
  MapPin,
  Package,
  Phone,
  Shield,
  User,
  type LucideIcon,
} from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { Footer } from '@/components/layout/footer'
import { Header } from '@/components/layout/header'
import { useLanguage } from '@/contexts/language-context'
import { useProfilePage } from '@/hooks/use-profile-page'
import { getMyAddresses } from '@/services/address-service'
import type { UserAddressResponse } from '@/types/address'
import type { OrderResponse, OrderStatus } from '@/types/order'
import type { ProfileResponse } from '@/types/profile'
import { cn, getErrorMessage } from '@/utils'
import { getGenderLabel, getOrderStatusLabel, getUserRoleLabel } from '@/utils/i18n'

type ProfileSection =
  | 'personal-panel'
  | 'account-panel'
  | 'address-panel'
  | 'orders-panel'
  | 'password-panel'

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
  const { t, formatCurrency, formatDate, language } = useLanguage()
  const isVietnamese = language === 'vi'
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
    handleProfileAvatarFileChange,
    handleProfileInputChange,
    handleProfileGenderChange,
    handleLogout,
    handleSaveAccount,
    handleSaveProfile,
    avatarLabel,
  } = useProfilePage()
  const [activeMenu, setActiveMenu] = useState<ProfileSection>('personal-panel')
  const [addresses, setAddresses] = useState<UserAddressResponse[]>([])
  const [isLoadingAddresses, setIsLoadingAddresses] = useState(false)
  const [addressError, setAddressError] = useState<string | null>(null)
  const [hasLoadedAddresses, setHasLoadedAddresses] = useState(false)

  useEffect(() => {
    if (activeMenu !== 'address-panel' || hasLoadedAddresses) {
      return
    }

    let isCancelled = false

    async function loadAddresses() {
      setIsLoadingAddresses(true)

      try {
        const response = await getMyAddresses()

        if (isCancelled) {
          return
        }

        setAddresses(response)
        setAddressError(null)
        setHasLoadedAddresses(true)
      } catch (error) {
        if (isCancelled) {
          return
        }

        setAddressError(getErrorMessage(error, t('checkout.error')))
      } finally {
        if (!isCancelled) {
          setIsLoadingAddresses(false)
        }
      }
    }

    void loadAddresses()

    return () => {
      isCancelled = true
    }
  }, [activeMenu, hasLoadedAddresses, t])

  const pageCopy = {
    personal: isVietnamese ? 'Hồ sơ cá nhân' : 'Personal profile',
    account: isVietnamese ? 'Thông tin đăng nhập' : 'Login information',
    address: isVietnamese ? 'Địa chỉ của tôi' : 'My addresses',
    orders: isVietnamese ? 'Đơn hàng của tôi' : 'My orders',
    password: isVietnamese ? 'Đổi mật khẩu' : 'Change password',
    chooseImage: isVietnamese ? 'Chọn ảnh' : 'Choose image',
    imageHint: isVietnamese ? 'JPG, PNG' : 'JPG, PNG',
    orderHistory: t('orders.title'),
    noOrdersTitle: isVietnamese
      ? 'Bạn chưa có đơn hàng nào'
      : 'You do not have any orders yet',
    noOrdersDescription: isVietnamese
      ? 'Khám phá sách hay và đặt đơn đầu tiên của bạn.'
      : 'Explore the catalog and place your first order.',
    shopNow: isVietnamese ? 'Mua sách ngay' : 'Shop now',
    addressTitle: isVietnamese ? 'Sổ địa chỉ của bạn' : 'Your address book',
    addressDescription: isVietnamese
      ? 'Các địa chỉ giao hàng đã lưu sẽ hiển thị ở đây.'
      : 'Your saved delivery addresses appear here.',
    noAddressesTitle: isVietnamese
      ? 'Bạn chưa có địa chỉ nào'
      : 'You do not have any addresses yet',
    noAddressesDescription: isVietnamese
      ? 'Thêm địa chỉ khi thanh toán để lần sau chọn nhanh hơn.'
      : 'Add an address during checkout to use it faster next time.',
    passwordTitle: isVietnamese ? 'Bảo mật tài khoản' : 'Account security',
    passwordDescription: isVietnamese
      ? 'Đổi mật khẩu bằng luồng xác thực hiện có của hệ thống.'
      : 'Reset your password using the current verification flow.',
    passwordAction: isVietnamese ? 'Đi tới đổi mật khẩu' : 'Go to password reset',
    defaultAddress: isVietnamese ? 'Mặc định' : 'Default',
    retry: isVietnamese ? 'Thử lại' : 'Retry',
    goCheckout: isVietnamese ? 'Đi tới thanh toán' : 'Go to checkout',
  }

  const displayName = getDisplayName(
    profileForm.lastName,
    profileForm.firstName,
    user?.name,
  )
  const avatarSource = profileForm.avatarUrl || profile?.avatarUrl || ''
  const avatarFallback = getAvatarFallback(displayName, user?.avatar)
  const profileGender = profileForm.gender || profile?.gender || 'OTHER'

  const menuItems: ProfileMenuItem[] = [
    {
      id: 'personal-panel',
      icon: User,
      label: pageCopy.personal,
    },
    {
      id: 'account-panel',
      icon: Shield,
      label: pageCopy.account,
    },
    {
      id: 'address-panel',
      icon: MapPin,
      label: pageCopy.address,
    },
    {
      id: 'orders-panel',
      icon: Package,
      label: pageCopy.orders,
    },
    {
      id: 'password-panel',
      icon: KeyRound,
      label: pageCopy.password,
    },
  ]

  return (
    <div className="flex min-h-screen flex-col bg-[linear-gradient(180deg,rgba(252,248,255,1)_0%,rgba(246,240,255,0.96)_54%,rgba(255,255,255,1)_100%)]">
      <Header />

      <main className="flex-1 pb-16 pt-6 sm:pb-20 sm:pt-8">
        <div className="mx-auto flex w-full max-w-7xl flex-col gap-6 px-4 sm:px-6 lg:px-8">
          <section className="relative overflow-hidden rounded-[34px] border border-primary/12 bg-white/88 p-6 shadow-[0_24px_80px_rgba(137,92,255,0.12)] backdrop-blur xl:p-9">
            <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(186,147,255,0.2),transparent_28%),radial-gradient(circle_at_bottom_right,rgba(150,121,255,0.16),transparent_26%)]" />
            <div className="pointer-events-none absolute -right-6 bottom-0 hidden h-52 w-72 rounded-full bg-[radial-gradient(circle,rgba(121,92,255,0.08),transparent_68%)] lg:block" />
            <BookOpen
              className="pointer-events-none absolute bottom-5 right-8 hidden h-36 w-36 text-primary/8 lg:block"
              strokeWidth={1.2}
            />

            <div className="relative flex flex-col gap-8 lg:flex-row lg:items-start lg:justify-between">
              <div className="flex min-w-0 flex-col gap-6 sm:flex-row sm:items-center">
                <div className="relative shrink-0">
                  <div className="flex size-28 items-center justify-center overflow-hidden rounded-full border-4 border-white bg-[linear-gradient(180deg,rgba(245,233,255,1)_0%,rgba(227,212,255,0.86)_100%)] text-[3.4rem] font-semibold text-primary shadow-[0_22px_44px_rgba(169,130,255,0.28)]">
                    {avatarSource ? (
                      <img
                        src={avatarSource}
                        alt={displayName}
                        className="size-full object-cover"
                      />
                    ) : (
                      avatarFallback
                    )}
                  </div>
                  <div className="absolute -bottom-1 right-1 flex size-11 items-center justify-center rounded-full border border-primary/12 bg-white text-primary shadow-[0_12px_26px_rgba(137,92,255,0.18)]">
                    <Camera className="h-4 w-4" />
                  </div>
                </div>

                <div className="min-w-0">
                  <h1 className="truncate font-heading text-3xl font-bold tracking-tight text-slate-950 sm:text-[2.35rem]">
                    {displayName}
                  </h1>
                  <div className="mt-3 flex flex-col gap-2 text-sm text-slate-500">
                    <p className="flex items-center gap-2">
                      <Mail className="h-4 w-4 text-primary/80" />
                      <span className="truncate">{accountForm.email}</span>
                    </p>
                    <div className="flex flex-wrap items-center gap-x-5 gap-y-2">
                      <span className="flex items-center gap-2">
                        <User className="h-4 w-4 text-primary/80" />
                        {accountForm.username}
                      </span>
                      <span className="flex items-center gap-2">
                        <Phone className="h-4 w-4 text-primary/80" />
                        {accountForm.phoneNumber || '...'}
                      </span>
                    </div>
                  </div>
                  <div className="mt-4 flex flex-wrap gap-2">
                    <span className="rounded-full bg-primary/12 px-3 py-1 text-xs font-semibold text-primary">
                      {user ? getUserRoleLabel(user.role, t) : ''}
                    </span>
                    <span className="rounded-full bg-sky-50 px-3 py-1 text-xs font-semibold text-sky-600">
                      {getGenderLabel(profileGender, t)}
                    </span>
                  </div>
                </div>
              </div>

              <Button
                onClick={() => void handleLogout()}
                variant="outline"
                className="h-11 rounded-2xl border-rose-200 px-5 text-rose-500 hover:border-rose-300 hover:bg-rose-50 hover:text-rose-600"
              >
                <LogOut className="mr-2 h-4 w-4" />
                {t('auth.profile.logout')}
              </Button>
            </div>
          </section>

          <section className="grid gap-6 xl:grid-cols-[240px_minmax(0,1fr)]">
            <aside className="xl:sticky xl:top-24 xl:self-start">
              <div className="rounded-[28px] border border-primary/10 bg-white/88 p-4 shadow-[0_18px_50px_rgba(137,92,255,0.08)] backdrop-blur">
                <nav className="space-y-2" aria-label={t('header.profileMenu')}>
                  {menuItems.map((item) => (
                    <ProfileMenuEntry
                      key={item.id}
                      item={item}
                      active={item.id === activeMenu}
                      onSelect={setActiveMenu}
                    />
                  ))}
                </nav>
              </div>
            </aside>

            <div>
              {activeMenu === 'personal-panel' ? (
                <SurfacePanel id="personal-panel">
                  <form onSubmit={handleSaveProfile}>
                    <PanelHeading icon={User} title={pageCopy.personal} />

                    <div className="mt-6 space-y-5">
                      <div className="grid gap-4 sm:grid-cols-2">
                        <StackField
                          id="lastName"
                          label={t('auth.profile.lastName')}
                          value={profileForm.lastName}
                          onChange={(event) =>
                            handleProfileInputChange(
                              'lastName',
                              event.currentTarget.value,
                            )
                          }
                        />
                        <StackField
                          id="firstName"
                          label={t('auth.profile.firstName')}
                          value={profileForm.firstName}
                          onChange={(event) =>
                            handleProfileInputChange(
                              'firstName',
                              event.currentTarget.value,
                            )
                          }
                        />
                      </div>

                      <div>
                        <Label className="text-sm font-medium text-slate-700">
                          {avatarLabel}
                        </Label>
                        <div className="mt-3 flex flex-col gap-4 sm:flex-row sm:items-center">
                          <div className="flex size-20 items-center justify-center overflow-hidden rounded-full border-2 border-primary/15 bg-[linear-gradient(180deg,rgba(245,233,255,1)_0%,rgba(227,212,255,0.82)_100%)] text-3xl font-semibold text-primary shadow-[0_12px_28px_rgba(137,92,255,0.14)]">
                            {avatarSource ? (
                              <img
                                src={avatarSource}
                                alt={displayName}
                                className="size-full object-cover"
                              />
                            ) : (
                              avatarFallback
                            )}
                          </div>

                          <div className="space-y-2">
                            <label
                              htmlFor="profile-avatar-upload"
                              className="inline-flex h-11 cursor-pointer items-center justify-center gap-2 rounded-2xl border border-primary/15 bg-primary/6 px-4 text-sm font-semibold text-primary transition hover:border-primary/25 hover:bg-primary/10"
                            >
                              <Camera className="h-4 w-4" />
                              {pageCopy.chooseImage}
                            </label>
                            <input
                              id="profile-avatar-upload"
                              type="file"
                              accept="image/*"
                              className="hidden"
                              onChange={(event) =>
                                void handleProfileAvatarFileChange(
                                  event.currentTarget.files?.[0] ?? null,
                                )
                              }
                            />
                            <p className="text-xs text-slate-500">
                              {pageCopy.imageHint}
                            </p>
                          </div>
                        </div>
                      </div>

                      <div className="grid gap-4 sm:grid-cols-2">
                        <div>
                          <Label
                            htmlFor="gender"
                            className="text-sm font-medium text-slate-700"
                          >
                            {t('auth.profile.gender')}
                          </Label>
                          <select
                            id="gender"
                            value={profileForm.gender}
                            onChange={(event) =>
                              handleProfileGenderChange(
                                event.currentTarget.value as ProfileResponse['gender'],
                              )
                            }
                            className="mt-2 h-11 w-full rounded-2xl border border-primary/10 bg-white/80 px-3 text-sm text-slate-700 shadow-[inset_0_1px_0_rgba(255,255,255,0.65)] outline-none transition focus:border-primary/30 focus:ring-4 focus:ring-primary/10"
                          >
                            {(['MALE', 'FEMALE', 'OTHER'] as const).map((gender) => (
                              <option key={gender} value={gender}>
                                {getGenderLabel(gender, t)}
                              </option>
                            ))}
                          </select>
                        </div>

                        <StackField
                          id="dateOfBirth"
                          label={t('auth.profile.dateOfBirth')}
                          type="date"
                          value={profileForm.dateOfBirth}
                          onChange={(event) =>
                            handleProfileInputChange(
                              'dateOfBirth',
                              event.currentTarget.value,
                            )
                          }
                        />
                      </div>
                    </div>

                    <Button
                      type="submit"
                      className="mt-6 h-11 rounded-2xl bg-[linear-gradient(135deg,rgba(124,92,255,1),rgba(101,72,248,0.96))] px-5 shadow-[0_18px_34px_rgba(109,76,255,0.28)] hover:opacity-95"
                      disabled={isSavingProfile || isLoading}
                    >
                      {isSavingProfile
                        ? t('common.processing')
                        : t('auth.profile.saveProfile')}
                    </Button>
                  </form>
                </SurfacePanel>
              ) : null}

              {activeMenu === 'account-panel' ? (
                <SurfacePanel id="account-panel">
                  <form onSubmit={handleSaveAccount}>
                    <PanelHeading icon={Shield} title={pageCopy.account} />

                    <div className="mt-6 space-y-4">
                      <FieldRow label={t('auth.profile.username')}>
                        <Input
                          id="username"
                          value={accountForm.username}
                          onChange={(event) =>
                            handleAccountChange('username', event.currentTarget.value)
                          }
                          className="h-11 rounded-2xl border-primary/10 bg-white/80 shadow-[inset_0_1px_0_rgba(255,255,255,0.65)]"
                        />
                      </FieldRow>
                      <FieldRow label={t('common.email')}>
                        <Input
                          id="accountEmail"
                          type="email"
                          value={accountForm.email}
                          onChange={(event) =>
                            handleAccountChange('email', event.currentTarget.value)
                          }
                          className="h-11 rounded-2xl border-primary/10 bg-white/80 shadow-[inset_0_1px_0_rgba(255,255,255,0.65)]"
                        />
                      </FieldRow>
                      <FieldRow label={t('common.phone')}>
                        <Input
                          id="accountPhone"
                          value={accountForm.phoneNumber}
                          onChange={(event) =>
                            handleAccountChange(
                              'phoneNumber',
                              event.currentTarget.value,
                            )
                          }
                          className="h-11 rounded-2xl border-primary/10 bg-white/80 shadow-[inset_0_1px_0_rgba(255,255,255,0.65)]"
                        />
                      </FieldRow>
                    </div>

                    <Button
                      type="submit"
                      className="mt-6 h-11 rounded-2xl bg-[linear-gradient(135deg,rgba(124,92,255,1),rgba(101,72,248,0.96))] px-5 shadow-[0_18px_34px_rgba(109,76,255,0.28)] hover:opacity-95"
                      disabled={isSavingAccount || isLoading}
                    >
                      {isSavingAccount
                        ? t('common.processing')
                        : t('auth.profile.saveAccount')}
                    </Button>
                  </form>
                </SurfacePanel>
              ) : null}

              {activeMenu === 'address-panel' ? (
                <SurfacePanel id="address-panel">
                  <PanelHeading icon={MapPin} title={pageCopy.addressTitle} />
                  <p className="mt-3 text-sm text-slate-500">
                    {pageCopy.addressDescription}
                  </p>

                  {isLoadingAddresses ? (
                    <div className="mt-6 rounded-[24px] border border-primary/10 bg-primary/4 p-6 text-sm text-slate-500">
                      {t('common.loading')}
                    </div>
                  ) : null}

                  {!isLoadingAddresses && addressError ? (
                    <div className="mt-6 rounded-[24px] border border-destructive/20 bg-destructive/8 p-6 text-sm text-destructive">
                      <p>{addressError}</p>
                      <Button
                        type="button"
                        variant="outline"
                        className="mt-4 h-10 rounded-2xl"
                        onClick={() => {
                          setHasLoadedAddresses(false)
                          setAddressError(null)
                        }}
                      >
                        {pageCopy.retry}
                      </Button>
                    </div>
                  ) : null}

                  {!isLoadingAddresses && !addressError && addresses.length === 0 ? (
                    <div className="mt-6 rounded-[24px] border border-primary/10 bg-[linear-gradient(180deg,rgba(255,255,255,0.96)_0%,rgba(248,244,255,0.92)_100%)] p-6">
                      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                        <div>
                          <h3 className="text-xl font-bold text-slate-950">
                            {pageCopy.noAddressesTitle}
                          </h3>
                          <p className="mt-2 text-sm leading-6 text-slate-500">
                            {pageCopy.noAddressesDescription}
                          </p>
                        </div>
                        <Link to="/checkout">
                          <Button className="h-11 rounded-2xl bg-[linear-gradient(135deg,rgba(124,92,255,1),rgba(101,72,248,0.96))] px-5 shadow-[0_18px_34px_rgba(109,76,255,0.24)] hover:opacity-95">
                            {pageCopy.goCheckout}
                          </Button>
                        </Link>
                      </div>
                    </div>
                  ) : null}

                  {!isLoadingAddresses && !addressError && addresses.length > 0 ? (
                    <div className="mt-6 grid gap-4">
                      {addresses.map((address) => (
                        <article
                          key={address.id}
                          className="rounded-[24px] border border-primary/10 bg-[linear-gradient(180deg,rgba(255,255,255,0.96)_0%,rgba(248,244,255,0.92)_100%)] p-5"
                        >
                          <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                            <div>
                              <div className="flex flex-wrap items-center gap-2">
                                <h3 className="text-lg font-bold text-slate-950">
                                  {address.receiverName}
                                </h3>
                                {address.defaultAddress ? (
                                  <Badge
                                    variant="outline"
                                    className="rounded-full border-primary/20 bg-primary/8 px-3 py-1 text-primary"
                                  >
                                    {pageCopy.defaultAddress}
                                  </Badge>
                                ) : null}
                              </div>
                              <p className="mt-2 text-sm text-slate-500">
                                {address.receiverPhone}
                              </p>
                            </div>
                            <p className="text-sm text-slate-400">
                              {formatDate(address.updatedAt)}
                            </p>
                          </div>
                          <p className="mt-4 text-sm leading-6 text-slate-600">
                            {address.receiverAddress}
                          </p>
                        </article>
                      ))}
                    </div>
                  ) : null}
                </SurfacePanel>
              ) : null}

              {activeMenu === 'orders-panel' ? (
                <SurfacePanel id="orders-panel">
                  <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                    <PanelHeading icon={Package} title={t('auth.profile.ordersTitle')} />
                    <Link to="/orders">
                      <Button
                        variant="outline"
                        className="h-10 rounded-2xl border-primary/10 px-4 text-primary hover:bg-primary/6"
                      >
                        {pageCopy.orderHistory}
                      </Button>
                    </Link>
                  </div>

                  {orders.length === 0 ? (
                    <div className="mt-6 rounded-[24px] border border-primary/8 bg-[linear-gradient(180deg,rgba(255,255,255,0.95)_0%,rgba(248,244,255,0.9)_100%)] p-6">
                      <div className="flex flex-col items-center gap-5 text-center md:flex-row md:text-left">
                        <div className="relative flex size-28 shrink-0 items-center justify-center">
                          <div className="absolute inset-4 rounded-[26px] bg-[linear-gradient(145deg,rgba(146,111,255,1),rgba(115,86,255,0.9))] shadow-[0_18px_38px_rgba(123,91,255,0.24)]" />
                          <div className="absolute right-3 top-4 h-7 w-7 rounded-xl bg-white/55" />
                          <div className="absolute bottom-5 left-4 h-4 w-4 rounded-lg bg-primary/15" />
                          <BookOpen className="relative h-12 w-12 text-white" />
                        </div>

                        <div className="max-w-xl">
                          <h3 className="text-2xl font-bold text-slate-950">
                            {pageCopy.noOrdersTitle}
                          </h3>
                          <p className="mt-2 text-sm leading-6 text-slate-500">
                            {pageCopy.noOrdersDescription}
                          </p>
                          <Link to="/books" className="inline-flex">
                            <Button className="mt-4 h-11 rounded-2xl bg-[linear-gradient(135deg,rgba(124,92,255,1),rgba(101,72,248,0.96))] px-5 shadow-[0_18px_34px_rgba(109,76,255,0.24)] hover:opacity-95">
                              {pageCopy.shopNow}
                              <ArrowRight className="ml-2 h-4 w-4" />
                            </Button>
                          </Link>
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className="mt-6 grid gap-4 lg:grid-cols-2">
                      {orders.slice(0, 3).map((order) => (
                        <OrderCard
                          key={order.orderId}
                          order={order}
                          formatCurrency={formatCurrency}
                          formatDate={formatDate}
                          t={t}
                        />
                      ))}
                    </div>
                  )}
                </SurfacePanel>
              ) : null}

              {activeMenu === 'password-panel' ? (
                <SurfacePanel id="password-panel">
                  <PanelHeading icon={KeyRound} title={pageCopy.passwordTitle} />
                  <div className="mt-6 rounded-[24px] border border-primary/10 bg-[linear-gradient(180deg,rgba(255,255,255,0.96)_0%,rgba(248,244,255,0.92)_100%)] p-6">
                    <p className="max-w-2xl text-sm leading-7 text-slate-500">
                      {pageCopy.passwordDescription}
                    </p>
                    <Link to="/forgot-password" className="inline-flex">
                      <Button className="mt-6 h-11 rounded-2xl bg-[linear-gradient(135deg,rgba(124,92,255,1),rgba(101,72,248,0.96))] px-5 shadow-[0_18px_34px_rgba(109,76,255,0.24)] hover:opacity-95">
                        {pageCopy.passwordAction}
                        <ArrowRight className="ml-2 h-4 w-4" />
                      </Button>
                    </Link>
                  </div>
                </SurfacePanel>
              ) : null}
            </div>
          </section>
        </div>
      </main>

      <Footer />
    </div>
  )
}

type ProfileMenuItem = {
  id: ProfileSection
  icon: LucideIcon
  label: string
}

function ProfileMenuEntry({
  item,
  active,
  onSelect,
}: {
  item: ProfileMenuItem
  active: boolean
  onSelect: (value: ProfileSection) => void
}) {
  return (
    <button
      type="button"
      className={cn(
        'flex w-full items-center gap-3 rounded-[20px] px-3 py-3 text-left text-sm font-semibold transition',
        active
          ? 'bg-[linear-gradient(135deg,rgba(124,92,255,0.12),rgba(124,92,255,0.06))] text-primary shadow-[inset_0_1px_0_rgba(255,255,255,0.65)]'
          : 'text-slate-600 hover:bg-primary/6 hover:text-slate-950',
      )}
      onClick={() => onSelect(item.id)}
    >
      <span
        className={cn(
          'flex size-9 items-center justify-center rounded-2xl transition',
          active ? 'bg-primary/12 text-primary' : 'bg-transparent text-slate-500',
        )}
      >
        <item.icon className="h-4 w-4" />
      </span>
      <span className="truncate">{item.label}</span>
    </button>
  )
}

function SurfacePanel({
  children,
  id,
}: {
  children: ReactNode
  id: string
}) {
  return (
    <section
      id={id}
      className="rounded-[30px] border border-primary/10 bg-white/88 p-6 shadow-[0_18px_50px_rgba(137,92,255,0.08)] backdrop-blur"
    >
      {children}
    </section>
  )
}

function PanelHeading({
  icon: Icon,
  title,
}: {
  icon: LucideIcon
  title: string
}) {
  return (
    <div className="flex items-center gap-3">
      <span className="flex size-10 items-center justify-center rounded-2xl bg-primary/8 text-primary">
        <Icon className="h-5 w-5" />
      </span>
      <h2 className="font-heading text-2xl font-bold text-slate-950">{title}</h2>
    </div>
  )
}

function FieldRow({
  children,
  label,
}: {
  children: ReactNode
  label: string
}) {
  return (
    <div className="grid gap-2 sm:grid-cols-[108px_minmax(0,1fr)] sm:items-center sm:gap-4">
      <Label className="text-sm font-medium text-slate-700">{label}</Label>
      {children}
    </div>
  )
}

function StackField({
  id,
  label,
  onChange,
  type = 'text',
  value,
}: {
  id: string
  label: string
  onChange: ChangeEventHandler<HTMLInputElement>
  type?: string
  value: string
}) {
  return (
    <div>
      <Label htmlFor={id} className="text-sm font-medium text-slate-700">
        {label}
      </Label>
      <Input
        id={id}
        type={type}
        value={value}
        onChange={onChange}
        className="mt-2 h-11 rounded-2xl border-primary/10 bg-white/80 shadow-[inset_0_1px_0_rgba(255,255,255,0.65)]"
      />
    </div>
  )
}

function OrderCard({
  order,
  formatCurrency,
  formatDate,
  t,
}: {
  order: OrderResponse
  formatCurrency: (value: number) => string
  formatDate: (value: Date | number | string) => string
  t: (key: string, params?: Record<string, number | string>) => string
}) {
  return (
    <article className="rounded-[24px] border border-primary/8 bg-[linear-gradient(180deg,rgba(255,255,255,0.96)_0%,rgba(248,244,255,0.92)_100%)] p-5">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div className="min-w-0">
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">
            {t('orders.orderId')}
          </p>
          <h3 className="mt-2 truncate text-lg font-bold text-slate-950">
            {order.orderId}
          </h3>
          <p className="mt-1 text-sm text-slate-500">{formatDate(order.createdAt)}</p>
        </div>
        <Badge
          variant={STATUS_VARIANTS[order.status]}
          className="rounded-full px-3 py-1"
        >
          {getOrderStatusLabel(order.status, t)}
        </Badge>
      </div>

      <div className="mt-5 space-y-3 border-t border-primary/8 pt-4">
        {order.items.slice(0, 3).map((item) => (
          <div
            key={item.id}
            className="flex items-start justify-between gap-3 text-sm"
          >
            <span className="line-clamp-2 text-slate-700">
              {item.bookTitle} x{item.quantity}
            </span>
            <span className="shrink-0 font-semibold text-slate-900">
              {formatCurrency(item.lineTotal)}
            </span>
          </div>
        ))}
      </div>

      <div className="mt-5 flex items-center justify-between border-t border-primary/8 pt-4">
        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-400">
            {t('auth.profile.orderTotal')}
          </p>
          <p className="mt-1 text-lg font-bold text-primary">
            {formatCurrency(order.finalAmount)}
          </p>
        </div>
        <Link to={`/orders/${order.orderId}`}>
          <Button
            variant="outline"
            className="h-10 rounded-2xl border-primary/12 px-4 text-primary hover:bg-primary/6"
          >
            {t('orders.viewDetail')}
          </Button>
        </Link>
      </div>
    </article>
  )
}

function getDisplayName(
  lastName: string,
  firstName: string,
  fallbackName?: string,
) {
  const fullName = `${lastName} ${firstName}`.trim()
  if (fullName !== '') {
    return fullName
  }

  return fallbackName || '...'
}

function getAvatarFallback(displayName: string, fallbackAvatar?: string) {
  const trimmedName = displayName.trim()
  if (trimmedName !== '') {
    return trimmedName.charAt(0).toUpperCase()
  }

  return fallbackAvatar || 'U'
}
