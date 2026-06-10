import { useEffect, useMemo, useState } from 'react'
import { Badge } from '@/components/common/badge'
import { Input } from '@/components/common/input'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useLanguage } from '@/contexts/language-context'
import { getAdminRoles } from '@/services/admin-access-service'
import type { AdminRoleResponse } from '@/types/admin-access'
import type { UserRole } from '@/types/auth'
import { getErrorMessage } from '@/utils'
import { getUserRoleLabel } from '@/utils/i18n'

const knownRoles: UserRole[] = ['ADMIN', 'STAFF', 'USER']
const roleVariants: Record<UserRole, 'default' | 'secondary' | 'outline'> = {
  ADMIN: 'default',
  STAFF: 'secondary',
  USER: 'outline',
}

export default function AdminRolesPage() {
  const { t, formatDate, formatNumber } = useLanguage()
  const [roles, setRoles] = useState<AdminRoleResponse[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let isCancelled = false

    async function loadRoles() {
      try {
        const response = await getAdminRoles()

        if (isCancelled) {
          return
        }

        setRoles(response)
        setError(null)
      } catch (currentError) {
        if (!isCancelled) {
          setError(getErrorMessage(currentError, t('admin.rolesPage.loadError')))
        }
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadRoles()

    return () => {
      isCancelled = true
    }
  }, [t])

  const filteredRoles = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return roles
    }

    return roles.filter((role) =>
      [role.name, role.description ?? '', ...role.permissionCodes]
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [roles, searchTerm])

  return (
    <AdminLayout>
      <div>
        <div>
          <h1 className="font-heading text-3xl font-bold text-foreground">
            {t('admin.rolesPage.title')}
          </h1>
          <p className="mt-2 text-muted-foreground">
            {t('admin.rolesPage.description')}
          </p>
        </div>

        {error && !isLoading && (
          <div className="mt-8 rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
            {error}
          </div>
        )}

        <div className="mt-8 rounded-2xl border border-border bg-card p-6">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <p className="text-sm text-muted-foreground">
              {t('admin.rolesPage.totalRoles', {
                count: formatNumber(filteredRoles.length),
              })}
            </p>
            <div className="w-full lg:max-w-sm">
              <Input
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.currentTarget.value)}
                placeholder={t('admin.rolesPage.searchPlaceholder')}
              />
            </div>
          </div>

          <div className="mt-6">
            {isLoading ? (
              <div className="py-8 text-center text-muted-foreground">
                {t('common.loading')}
              </div>
            ) : filteredRoles.length === 0 ? (
              <div className="py-8 text-center text-muted-foreground">
                {t('admin.rolesPage.empty')}
              </div>
            ) : (
              <div className="grid gap-5 xl:grid-cols-2">
                {filteredRoles.map((role) => (
                  <div
                    key={role.id}
                    className="rounded-xl border border-border bg-background p-5"
                  >
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div>
                        <Badge variant={getRoleVariant(role.name)}>
                          {getRoleLabel(role.name, t)}
                        </Badge>
                        <p className="mt-3 text-sm text-muted-foreground">
                          {role.description || t('admin.rolesPage.noDescription')}
                        </p>
                      </div>
                      <p className="text-xs text-muted-foreground">
                        {formatDate(role.updatedAt)}
                      </p>
                    </div>

                    <p className="mt-4 text-sm font-medium text-foreground">
                      {t('admin.rolesPage.permissionCount', {
                        count: formatNumber(role.permissionCodes.length),
                      })}
                    </p>

                    <div className="mt-3 flex flex-wrap gap-2">
                      {role.permissionCodes.map((permissionCode) => (
                        <Badge key={`${role.id}-${permissionCode}`} variant="outline">
                          {permissionCode}
                        </Badge>
                      ))}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>
    </AdminLayout>
  )
}

function getRoleVariant(roleName: string) {
  return knownRoles.includes(roleName as UserRole)
    ? roleVariants[roleName as UserRole]
    : 'outline'
}

function getRoleLabel(
  roleName: string,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  return knownRoles.includes(roleName as UserRole)
    ? getUserRoleLabel(roleName as UserRole, t)
    : roleName
}
