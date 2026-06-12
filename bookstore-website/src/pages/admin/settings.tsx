import {
  CalendarDays,
  LogOut,
  Mail,
  Phone,
  ShieldCheck,
  Sparkles,
  UserRound,
} from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { ThemeSwitch } from '@/components/common/theme-switch'
import { AdminLayout } from '@/components/layout/admin-layout'
import { LanguageSwitcher } from '@/components/common/language-switcher'
import {
  adminSettingsGenderOptions,
  useAdminSettingsPage,
} from '@/hooks/use-admin-settings-page'
import type { ProfileResponse } from '@/types/profile'
import { getGenderLabel, getUserRoleLabel } from '@/utils/i18n'

export default function AdminSettingsPage() {
  const {
    user,
    profile,
    theme,
    t,
    formatDate,
    labels,
    isLoading,
    isSavingAccount,
    isSavingProfile,
    accountForm,
    profileForm,
    toggleTheme,
    handleAccountChange,
    handleProfileChange,
    handleProfileGenderChange,
    handleLogout,
    handleSaveAccount,
    handleSaveProfile,
  } = useAdminSettingsPage()

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
                void handleLogout()
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
                      handleAccountChange('username', event.currentTarget.value)
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
                      handleAccountChange('email', event.currentTarget.value)
                    }
                    className="mt-2 h-11 rounded-2xl"
                    required
                  />
                </FieldShell>

                <FieldShell label={t('common.phone')}>
                  <Input
                    value={accountForm.phoneNumber}
                    onChange={(event) =>
                      handleAccountChange(
                        'phoneNumber',
                        event.currentTarget.value,
                      )
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
                        handleProfileChange('lastName', event.currentTarget.value)
                      }
                      className="mt-2 h-11 rounded-2xl"
                      required
                    />
                  </FieldShell>

                  <FieldShell label={t('auth.profile.firstName')}>
                    <Input
                      value={profileForm.firstName}
                      onChange={(event) =>
                        handleProfileChange(
                          'firstName',
                          event.currentTarget.value,
                        )
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
                      handleProfileChange('avatarUrl', event.currentTarget.value)
                    }
                    className="mt-2 h-11 rounded-2xl"
                  />
                </FieldShell>

                <div className="grid gap-4 md:grid-cols-2">
                  <FieldShell label={t('auth.profile.gender')}>
                    <select
                      value={profileForm.gender}
                      onChange={(event) =>
                        handleProfileGenderChange(
                          event.currentTarget.value as ProfileResponse['gender'],
                        )
                      }
                      className="mt-2 h-11 w-full rounded-2xl border border-input bg-background px-3 text-sm"
                    >
                      {adminSettingsGenderOptions.map((gender) => (
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
                        handleProfileChange(
                          'dateOfBirth',
                          event.currentTarget.value,
                        )
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
