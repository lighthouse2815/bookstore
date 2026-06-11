import { useEffect, useMemo, useState } from 'react'
import {
  CalendarDays,
  LogOut,
  Mail,
  Phone,
  ShieldCheck,
  Sparkles,
  UserRound,
} from 'lucide-react'
import { toast } from 'sonner'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { ThemeSwitch } from '@/components/common/theme-switch'
import { AdminLayout } from '@/components/layout/admin-layout'
import { LanguageSwitcher } from '@/components/common/language-switcher'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import { useTheme } from '@/contexts/theme-context'
import { updateCurrentUser } from '@/services/auth-service'
import {
  getCurrentProfile,
  updateCurrentProfile,
} from '@/services/profile-service'
import type { ProfileResponse } from '@/types/profile'
import { getErrorMessage } from '@/utils'
import { getGenderLabel, getUserRoleLabel } from '@/utils/i18n'

type AccountFormState = {
  username: string
  email: string
  phoneNumber: string
}

type ProfileFormState = {
  lastName: string
  firstName: string
  avatarUrl: string
  gender: ProfileResponse['gender']
  dateOfBirth: string
}

export default function AdminSettingsPage() {
  const { user, logout, refreshUser } = useAuth()
  const { language, t, formatDate } = useLanguage()
  const { theme, toggleTheme } = useTheme()
  const isVietnamese = language === 'vi'
  const [profile, setProfile] = useState<ProfileResponse | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [isSavingAccount, setIsSavingAccount] = useState(false)
  const [isSavingProfile, setIsSavingProfile] = useState(false)
  const [accountForm, setAccountForm] = useState<AccountFormState>({
    username: user?.username ?? '',
    email: user?.email ?? '',
    phoneNumber: user?.phoneNumber ?? '',
  })
  const [profileForm, setProfileForm] = useState<ProfileFormState>({
    lastName: '',
    firstName: '',
    avatarUrl: '',
    gender: 'OTHER',
    dateOfBirth: '',
  })

  const labels = useMemo(
    () => ({
      title: isVietnamese ? 'Cai dat tai khoan quan tri' : 'Admin account settings',
      description: isVietnamese
        ? 'Xem thong tin tai khoan, cap nhat profile va tuy chinh khong gian lam viec quan tri.'
        : 'Review account details, update your profile, and adjust your admin workspace preferences.',
      overview: isVietnamese ? 'Tong quan tai khoan' : 'Account overview',
      preferences: isVietnamese ? 'Tuy chon giao dien' : 'Workspace preferences',
      role: isVietnamese ? 'Vai tro' : 'Role',
      status: isVietnamese ? 'Trang thai' : 'Status',
      active: isVietnamese ? 'Dang hoat dong' : 'Active',
      inactive: isVietnamese ? 'Khong hoat dong' : 'Inactive',
      accountCreated: isVietnamese ? 'Ngay tao tai khoan' : 'Account created',
      accountUpdated: isVietnamese ? 'Cap nhat gan nhat' : 'Last updated',
      theme: isVietnamese ? 'Che do sang toi' : 'Light and dark mode',
      themeDescription: isVietnamese
        ? 'Chuyen giao dien admin ma khong can roi khoi bang dieu khien.'
        : 'Switch the admin interface theme without leaving the dashboard.',
      language: isVietnamese ? 'Ngon ngu hien thi' : 'Display language',
      languageDescription: isVietnamese
        ? 'Ap dung ngay cho toan bo giao dien quan tri.'
        : 'Applies immediately across the admin interface.',
      accountSaved: isVietnamese
        ? 'Da cap nhat thong tin tai khoan'
        : 'Account information updated',
      profileSaved: isVietnamese ? 'Da cap nhat profile' : 'Profile updated',
      profileLoadError: isVietnamese
        ? 'Khong tai duoc thong tin profile'
        : 'Unable to load profile information',
      logout: t('auth.profile.logout'),
    }),
    [isVietnamese, t],
  )

  useEffect(() => {
    if (!user) {
      return
    }

    setAccountForm({
      username: user.username,
      email: user.email,
      phoneNumber: user.phoneNumber,
    })
  }, [user])

  useEffect(() => {
    let isCancelled = false

    async function loadProfile() {
      try {
        const response = await getCurrentProfile()

        if (isCancelled) {
          return
        }

        setProfile(response)
        setProfileForm({
          lastName: response.lastName,
          firstName: response.firstName,
          avatarUrl: response.avatarUrl ?? '',
          gender: response.gender,
          dateOfBirth: response.dateOfBirth,
        })
      } catch (error) {
        if (!isCancelled) {
          toast.error(getErrorMessage(error, labels.profileLoadError))
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadProfile()

    return () => {
      isCancelled = true
    }
  }, [labels.profileLoadError])

  async function handleSaveAccount(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSavingAccount(true)

    try {
      await updateCurrentUser({
        username: accountForm.username.trim(),
        email: accountForm.email.trim(),
        phoneNumber: accountForm.phoneNumber.trim(),
      })
      await refreshUser()
      toast.success(labels.accountSaved)
    } catch (error) {
      toast.error(getErrorMessage(error, t('checkout.error')))
    } finally {
      setIsSavingAccount(false)
    }
  }

  async function handleSaveProfile(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsSavingProfile(true)

    try {
      const response = await updateCurrentProfile({
        lastName: profileForm.lastName.trim(),
        firstName: profileForm.firstName.trim(),
        avatarUrl: profileForm.avatarUrl.trim() || null,
        gender: profileForm.gender,
        dateOfBirth: profileForm.dateOfBirth,
      })

      setProfile(response)
      await refreshUser()
      toast.success(labels.profileSaved)
    } catch (error) {
      toast.error(getErrorMessage(error, t('checkout.error')))
    } finally {
      setIsSavingProfile(false)
    }
  }

  return (
    <AdminLayout>
      <div className="relative overflow-hidden rounded-[32px] border border-border/60 bg-card/90 p-6 shadow-[0_28px_90px_rgba(2,6,23,0.35)] backdrop-blur xl:p-8">
        <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(129,140,248,0.18),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(16,185,129,0.14),transparent_32%)]" />

        <div className="relative">
          <div className="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
            <div>
              <div className="flex flex-wrap items-center gap-3">
                <h1 className="font-heading text-3xl font-bold text-foreground sm:text-4xl">
                  {labels.title}
                </h1>
                <Badge
                  variant="outline"
                  className="rounded-2xl border-primary/20 bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary dark:border-primary/30"
                >
                  <Sparkles className="mr-2 h-4 w-4" />
                  {user ? getUserRoleLabel(user.role, t) : ''}
                </Badge>
              </div>
              <p className="mt-3 max-w-3xl text-base text-muted-foreground">
                {labels.description}
              </p>
            </div>

            <Button
              variant="outline"
              size="lg"
              onClick={() => {
                void logout()
              }}
              className="h-14 rounded-2xl px-6 text-base"
            >
              <LogOut className="mr-2 h-5 w-5" />
              {labels.logout}
            </Button>
          </div>

          <div className="mt-8 grid gap-6 xl:grid-cols-[1.15fr_1fr]">
            <section className="rounded-[28px] border border-border/60 bg-background/45 p-6 shadow-[0_18px_50px_rgba(2,6,23,0.14)]">
              <div className="flex flex-col gap-5 md:flex-row md:items-center">
                <div className="flex h-24 w-24 shrink-0 items-center justify-center overflow-hidden rounded-[28px] border border-border/60 bg-background/80 text-4xl font-semibold text-primary shadow-[0_16px_40px_rgba(2,6,23,0.16)]">
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

                <div className="min-w-0">
                  <p className="truncate text-2xl font-semibold text-foreground">
                    {user?.name}
                  </p>
                  <p className="mt-2 flex items-center gap-2 text-sm text-muted-foreground">
                    <Mail className="h-4 w-4" />
                    {user?.email}
                  </p>
                  <div className="mt-4 flex flex-wrap gap-2">
                    {user?.roles.map((role) => (
                      <Badge
                        key={role}
                        variant="outline"
                        className="rounded-2xl px-3 py-1 text-xs"
                      >
                        {getUserRoleLabel(role, t)}
                      </Badge>
                    ))}
                    {profile ? (
                      <Badge
                        variant="outline"
                        className="rounded-2xl px-3 py-1 text-xs"
                      >
                        {getGenderLabel(profile.gender, t)}
                      </Badge>
                    ) : null}
                  </div>
                </div>
              </div>

              <div className="mt-6 grid gap-4 md:grid-cols-2">
                <OverviewCard
                  icon={ShieldCheck}
                  label={labels.status}
                  value={user?.status === 'ACTIVE' ? labels.active : labels.inactive}
                />
                <OverviewCard
                  icon={CalendarDays}
                  label={labels.accountCreated}
                  value={user ? formatDate(user.createdAt) : '...'}
                />
                <OverviewCard
                  icon={UserRound}
                  label={labels.role}
                  value={user ? getUserRoleLabel(user.role, t) : '...'}
                />
                <OverviewCard
                  icon={CalendarDays}
                  label={labels.accountUpdated}
                  value={user ? formatDate(user.updatedAt) : '...'}
                />
                <OverviewCard
                  icon={Mail}
                  label={t('common.email')}
                  value={user?.email ?? '...'}
                />
                <OverviewCard
                  icon={Phone}
                  label={t('common.phone')}
                  value={user?.phoneNumber || '...'}
                />
              </div>
            </section>

            <section className="rounded-[28px] border border-border/60 bg-background/45 p-6 shadow-[0_18px_50px_rgba(2,6,23,0.14)]">
              <h2 className="text-2xl font-semibold text-foreground">
                {labels.preferences}
              </h2>
              <div className="mt-6 space-y-4">
                <div className="rounded-[22px] border border-border/60 bg-background/65 p-4">
                  <div className="flex items-center justify-between gap-4">
                    <div>
                      <p className="font-semibold text-foreground">
                        {labels.theme}
                      </p>
                      <p className="mt-1 text-sm text-muted-foreground">
                        {labels.themeDescription}
                      </p>
                    </div>
                    <ThemeSwitch
                      checked={theme === 'dark'}
                      onToggle={toggleTheme}
                      label={
                        theme === 'dark'
                          ? t('header.switchToLight')
                          : t('header.switchToDark')
                      }
                      className="animate-none"
                    />
                  </div>
                </div>

                <div className="rounded-[22px] border border-border/60 bg-background/65 p-4">
                  <p className="font-semibold text-foreground">
                    {labels.language}
                  </p>
                  <p className="mt-1 text-sm text-muted-foreground">
                    {labels.languageDescription}
                  </p>
                  <LanguageSwitcher className="mt-4" />
                </div>
              </div>
            </section>
          </div>

          <div className="mt-8 grid gap-6 xl:grid-cols-2">
            <form
              onSubmit={(event) => void handleSaveAccount(event)}
              className="rounded-[28px] border border-border/60 bg-background/45 p-6 shadow-[0_18px_50px_rgba(2,6,23,0.14)]"
            >
              <h2 className="text-2xl font-semibold text-foreground">
                {t('auth.profile.accountTitle')}
              </h2>

              <div className="mt-6 space-y-4">
                <FieldShell label={t('auth.profile.username')}>
                  <Input
                    value={accountForm.username}
                    onChange={(event) =>
                      setAccountForm((currentForm) => ({
                        ...currentForm,
                        username: event.currentTarget.value,
                      }))
                    }
                    className="mt-2 h-11 rounded-2xl"
                    required
                  />
                </FieldShell>

                <FieldShell label={t('common.email')}>
                  <Input
                    type="email"
                    value={accountForm.email}
                    onChange={(event) =>
                      setAccountForm((currentForm) => ({
                        ...currentForm,
                        email: event.currentTarget.value,
                      }))
                    }
                    className="mt-2 h-11 rounded-2xl"
                    required
                  />
                </FieldShell>

                <FieldShell label={t('common.phone')}>
                  <Input
                    value={accountForm.phoneNumber}
                    onChange={(event) =>
                      setAccountForm((currentForm) => ({
                        ...currentForm,
                        phoneNumber: event.currentTarget.value,
                      }))
                    }
                    className="mt-2 h-11 rounded-2xl"
                    required
                  />
                </FieldShell>
              </div>

              <Button
                type="submit"
                className="mt-6 rounded-2xl"
                disabled={isSavingAccount || isLoading}
              >
                {isSavingAccount ? t('common.processing') : t('auth.profile.saveAccount')}
              </Button>
            </form>

            <form
              onSubmit={(event) => void handleSaveProfile(event)}
              className="rounded-[28px] border border-border/60 bg-background/45 p-6 shadow-[0_18px_50px_rgba(2,6,23,0.14)]"
            >
              <h2 className="text-2xl font-semibold text-foreground">
                {t('auth.profile.personalTitle')}
              </h2>

              <div className="mt-6 space-y-4">
                <div className="grid gap-4 md:grid-cols-2">
                  <FieldShell label={t('auth.profile.lastName')}>
                    <Input
                      value={profileForm.lastName}
                      onChange={(event) =>
                        setProfileForm((currentForm) => ({
                          ...currentForm,
                          lastName: event.currentTarget.value,
                        }))
                      }
                      className="mt-2 h-11 rounded-2xl"
                      required
                    />
                  </FieldShell>

                  <FieldShell label={t('auth.profile.firstName')}>
                    <Input
                      value={profileForm.firstName}
                      onChange={(event) =>
                        setProfileForm((currentForm) => ({
                          ...currentForm,
                          firstName: event.currentTarget.value,
                        }))
                      }
                      className="mt-2 h-11 rounded-2xl"
                      required
                    />
                  </FieldShell>
                </div>

                <FieldShell label={t('auth.profile.avatarUrl')}>
                  <Input
                    value={profileForm.avatarUrl}
                    onChange={(event) =>
                      setProfileForm((currentForm) => ({
                        ...currentForm,
                        avatarUrl: event.currentTarget.value,
                      }))
                    }
                    className="mt-2 h-11 rounded-2xl"
                  />
                </FieldShell>

                <div className="grid gap-4 md:grid-cols-2">
                  <FieldShell label={t('auth.profile.gender')}>
                    <select
                      value={profileForm.gender}
                      onChange={(event) =>
                        setProfileForm((currentForm) => ({
                          ...currentForm,
                          gender: event.currentTarget.value as ProfileResponse['gender'],
                        }))
                      }
                      className="mt-2 h-11 w-full rounded-2xl border border-input bg-background px-3 text-sm"
                    >
                      {(['MALE', 'FEMALE', 'OTHER'] as const).map((gender) => (
                        <option key={gender} value={gender}>
                          {getGenderLabel(gender, t)}
                        </option>
                      ))}
                    </select>
                  </FieldShell>

                  <FieldShell label={t('auth.profile.dateOfBirth')}>
                    <Input
                      type="date"
                      value={profileForm.dateOfBirth}
                      onChange={(event) =>
                        setProfileForm((currentForm) => ({
                          ...currentForm,
                          dateOfBirth: event.currentTarget.value,
                        }))
                      }
                      className="mt-2 h-11 rounded-2xl"
                      required
                    />
                  </FieldShell>
                </div>
              </div>

              <Button
                type="submit"
                className="mt-6 rounded-2xl"
                disabled={isSavingProfile || isLoading}
              >
                {isSavingProfile ? t('common.processing') : t('auth.profile.saveProfile')}
              </Button>
            </form>
          </div>
        </div>
      </div>
    </AdminLayout>
  )
}

function FieldShell({
  children,
  label,
}: {
  children: React.ReactNode
  label: string
}) {
  return (
    <div>
      <Label>{label}</Label>
      {children}
    </div>
  )
}

function OverviewCard({
  icon: Icon,
  label,
  value,
}: {
  icon: typeof CalendarDays
  label: string
  value: string
}) {
  return (
    <div className="rounded-[22px] border border-border/60 bg-background/65 p-4">
      <div className="flex items-center gap-2 text-sm text-muted-foreground">
        <Icon className="h-4 w-4" />
        <span>{label}</span>
      </div>
      <p className="mt-3 text-base font-semibold text-foreground">{value}</p>
    </div>
  )
}
