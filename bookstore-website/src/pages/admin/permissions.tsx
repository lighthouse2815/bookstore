import { useEffect, useMemo, useState } from 'react'
import { createPortal } from 'react-dom'
import {
  CalendarDays,
  Eye,
  KeyRound,
  RefreshCw,
  Search,
  X,
} from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useLanguage } from '@/contexts/language-context'
import { getAdminPermissions } from '@/services/admin-access-service'
import type { AdminPermissionResponse } from '@/types/admin-access'
import { getErrorMessage } from '@/utils'

export default function AdminPermissionsPage() {
  const { language, t, formatDate, formatNumber } = useLanguage()
  const isVietnamese = language === 'vi'
  const [permissions, setPermissions] = useState<AdminPermissionResponse[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedPermission, setSelectedPermission] =
    useState<AdminPermissionResponse | null>(null)

  const labels = useMemo(
    () => ({
      detailTitle: isVietnamese ? 'Chi tiet quyen' : 'Permission details',
      permissionCode: isVietnamese ? 'Ma quyen' : 'Permission code',
      permissionDescription: isVietnamese ? 'Mo ta' : 'Description',
      showingCount: isVietnamese
        ? 'Hien thi {count} tren {total} quyen'
        : 'Showing {count} of {total} permissions',
    }),
    [isVietnamese],
  )

  useEffect(() => {
    let isCancelled = false

    async function loadPermissions() {
      try {
        const response = await getAdminPermissions()

        if (isCancelled) {
          return
        }

        setPermissions(response)
        setError(null)
      } catch (currentError) {
        if (!isCancelled) {
          setError(
            getErrorMessage(currentError, t('admin.permissionsPage.loadError')),
          )
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadPermissions()

    return () => {
      isCancelled = true
    }
  }, [t])

  useEffect(() => {
    if (!selectedPermission) {
      return
    }

    const previousOverflow = document.body.style.overflow

    function handleKeyDown(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        setSelectedPermission(null)
      }
    }

    document.body.style.overflow = 'hidden'
    window.addEventListener('keydown', handleKeyDown)

    return () => {
      document.body.style.overflow = previousOverflow
      window.removeEventListener('keydown', handleKeyDown)
    }
  }, [selectedPermission])

  const filteredPermissions = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return permissions
    }

    return permissions.filter((permission) =>
      [permission.code, permission.description ?? '']
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [permissions, searchTerm])

  const dialogMarkup = selectedPermission ? (
    <div className="fixed inset-0 z-[160] flex items-center justify-center px-4 py-6">
      <button
        type="button"
        aria-label={t('common.close')}
        className="absolute inset-0 bg-background/72 backdrop-blur-sm"
        onClick={() => setSelectedPermission(null)}
      />
      <div className="relative z-10 w-full max-w-3xl">
        <DialogShell
          title={labels.detailTitle}
          onClose={() => setSelectedPermission(null)}
        >
          <div className="space-y-6">
            <div className="rounded-[24px] border border-border/60 bg-background/55 p-5">
              <div className="flex flex-col gap-5 sm:flex-row sm:items-center">
                <div className="flex h-24 w-20 shrink-0 items-center justify-center rounded-[20px] border border-border/60 bg-background/70 shadow-[0_18px_40px_rgba(2,6,23,0.22)]">
                  <KeyRound className="h-9 w-9 text-primary" />
                </div>
                <div className="min-w-0">
                  <p className="truncate text-3xl font-semibold text-foreground">
                    {selectedPermission.code}
                  </p>
                </div>
              </div>
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <DetailCard
                icon={CalendarDays}
                label={t('common.createdAt')}
                value={formatDate(selectedPermission.createdAt)}
              />
              <DetailCard
                icon={RefreshCw}
                label={t('common.updatedAt')}
                value={formatDate(selectedPermission.updatedAt)}
              />
            </div>

            <div className="grid gap-4 md:grid-cols-2">
              <DetailCard
                icon={KeyRound}
                label={labels.permissionCode}
                value={selectedPermission.code}
              />
              <DetailCard
                icon={KeyRound}
                label={labels.permissionDescription}
                value={
                  selectedPermission.description ||
                  t('admin.permissionsPage.noDescription')
                }
              />
            </div>

            <div className="flex items-center justify-end gap-3">
              <Button
                type="button"
                variant="outline"
                onClick={() => setSelectedPermission(null)}
                className="rounded-2xl"
              >
                {t('common.close')}
              </Button>
            </div>
          </div>
        </DialogShell>
      </div>
    </div>
  ) : null

  return (
    <>
      <AdminLayout>
        <div className="relative overflow-hidden rounded-[32px] border border-border/60 bg-card/90 p-6 shadow-[0_28px_90px_rgba(2,6,23,0.35)] backdrop-blur xl:p-8">
          <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(129,140,248,0.18),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(59,130,246,0.12),transparent_32%)]" />

          <div className="relative">
            <div className="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
              <div>
                <div className="flex flex-wrap items-center gap-3">
                  <h1 className="font-heading text-3xl font-bold text-foreground sm:text-4xl">
                    {t('admin.permissionsPage.title')}
                  </h1>
                  <Badge
                    variant="outline"
                    className="rounded-2xl border-primary/20 bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary dark:border-primary/30"
                  >
                    <KeyRound className="mr-2 h-4 w-4" />
                    {t('admin.permissionsPage.totalPermissions', {
                      count: formatNumber(permissions.length),
                    })}
                  </Badge>
                </div>
                <p className="mt-3 max-w-2xl text-base text-muted-foreground">
                  {t('admin.permissionsPage.description')}
                </p>
              </div>
            </div>

            <div className="mt-8 flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
              <div className="w-full max-w-xl">
                <div className="relative">
                  <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                  <Input
                    value={searchTerm}
                    onChange={(event) => setSearchTerm(event.currentTarget.value)}
                    placeholder={t('admin.permissionsPage.searchPlaceholder')}
                    className="h-14 rounded-2xl border-border/70 bg-background/55 pl-12 text-base shadow-[inset_0_1px_0_rgba(255,255,255,0.04)]"
                  />
                </div>
              </div>
            </div>

            {error && !isLoading ? (
              <div className="mt-8 rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                {error}
              </div>
            ) : null}

            <section className="mt-8 overflow-hidden rounded-[28px] border border-primary/30 bg-background/20 shadow-[0_24px_80px_rgba(15,23,42,0.24)] backdrop-blur">
              <div className="space-y-4 p-4">
                <div className="hidden xl:block">
                  <div className="grid overflow-hidden rounded-[24px] border border-border/60 bg-background/55 text-sm font-semibold uppercase tracking-[0.08em] text-muted-foreground shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid-cols-[minmax(0,2.6fr)_16rem]">
                    <div className="px-8 py-6">
                      <p>{labels.permissionCode}</p>
                    </div>
                    <div className="flex items-center justify-center border-l border-border/40 px-6 py-6 text-center">
                      <p>{t('common.actions')}</p>
                    </div>
                  </div>
                </div>

                {isLoading ? (
                  <div className="rounded-[24px] border border-border/50 bg-background/40 px-6 py-10 text-center text-muted-foreground">
                    {t('common.loading')}
                  </div>
                ) : filteredPermissions.length === 0 ? (
                  <div className="rounded-[24px] border border-dashed border-border/60 bg-background/35 px-6 py-10 text-center">
                    <p className="text-base font-medium text-foreground">
                      {t('admin.permissionsPage.empty')}
                    </p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {filteredPermissions.map((permission) => (
                      <article
                        key={permission.id}
                        className="flex flex-col gap-5 rounded-[24px] border border-border/60 bg-background/55 p-5 shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2.6fr)_16rem] xl:gap-0 xl:p-0"
                      >
                        <div className="flex min-w-0 items-center gap-5 xl:px-8 xl:py-6">
                          <div className="flex h-20 w-16 shrink-0 items-center justify-center rounded-[20px] border border-border/60 bg-background/70 shadow-[0_18px_40px_rgba(2,6,23,0.22)]">
                            <KeyRound className="h-8 w-8 text-primary" />
                          </div>

                          <div className="min-w-0">
                            <p className="truncate text-2xl font-semibold text-foreground">
                              {permission.code}
                            </p>
                            <p className="mt-2 truncate text-sm text-muted-foreground">
                              {permission.description ||
                                t('admin.permissionsPage.noDescription')}
                            </p>
                          </div>
                        </div>

                        <div className="flex flex-wrap gap-3 xl:min-h-[128px] xl:flex-nowrap xl:items-center xl:justify-center xl:border-l xl:border-border/40 xl:px-6">
                          <Button
                            type="button"
                            variant="outline"
                            onClick={() => setSelectedPermission(permission)}
                            className="min-w-[96px] justify-center rounded-2xl bg-background/60"
                          >
                            <Eye className="mr-2 h-4 w-4" />
                            {t('common.view')}
                          </Button>
                        </div>
                      </article>
                    ))}
                  </div>
                )}
              </div>

              {!isLoading && !error && filteredPermissions.length > 0 ? (
                <div className="border-t border-border/60 px-6 py-5 text-sm text-muted-foreground">
                  {interpolateLabel(labels.showingCount, {
                    count: formatNumber(filteredPermissions.length),
                    total: formatNumber(permissions.length),
                  })}
                </div>
              ) : null}
            </section>
          </div>
        </div>
      </AdminLayout>

      {dialogMarkup && typeof document !== 'undefined'
        ? createPortal(dialogMarkup, document.body)
        : null}
    </>
  )
}

function DialogShell({
  canClose = true,
  children,
  onClose,
  title,
}: {
  canClose?: boolean
  children: React.ReactNode
  onClose: () => void
  title: string
}) {
  return (
    <div className="overflow-hidden rounded-[28px] border border-border/70 bg-card/95 shadow-[0_30px_120px_rgba(2,6,23,0.5)] backdrop-blur">
      <div className="flex items-start justify-between gap-4 border-b border-border/60 px-6 py-5">
        <div>
          <h2 className="text-2xl font-semibold text-foreground">{title}</h2>
        </div>
        <Button
          type="button"
          variant="ghost"
          size="icon-sm"
          onClick={onClose}
          className="rounded-2xl"
          disabled={!canClose}
        >
          <X className="h-4 w-4" />
        </Button>
      </div>

      <div className="px-6 py-6">{children}</div>
    </div>
  )
}

function DetailCard({
  icon: Icon,
  label,
  value,
}: {
  icon: typeof KeyRound
  label: string
  value: string
}) {
  return (
    <div className="rounded-[22px] border border-border/60 bg-background/55 p-4">
      <div className="flex items-center gap-2 text-sm text-muted-foreground">
        <Icon className="h-4 w-4" />
        <span>{label}</span>
      </div>
      <p className="mt-3 text-base font-semibold text-foreground">{value}</p>
    </div>
  )
}

function interpolateLabel(
  template: string,
  params: Record<string, string | number>,
) {
  return template.replace(/\{(\w+)\}/g, (_, key: string) =>
    String(params[key] ?? `{${key}}`),
  )
}
