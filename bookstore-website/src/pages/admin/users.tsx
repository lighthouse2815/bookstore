import { useEffect, useMemo, useState } from 'react'
import { Badge } from '@/components/common/badge'
import { Input } from '@/components/common/input'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import { getAdminUsers } from '@/services/admin-access-service'
import type { AdminUserResponse } from '@/types/admin-access'
import type { User, UserRole, UserStatus } from '@/types/auth'
import { getErrorMessage } from '@/utils'
import { getUserRoleLabel } from '@/utils/i18n'

const statusVariants: Record<
  UserStatus,
  'default' | 'secondary' | 'outline' | 'destructive'
> = {
  ACTIVE: 'default',
  INACTIVE: 'secondary',
}

const knownRoles: UserRole[] = ['ADMIN', 'STAFF', 'USER']

export default function AdminUsersPage() {
  const { user } = useAuth()
  const { t, formatDate, formatNumber } = useLanguage()
  const [users, setUsers] = useState<AdminUserResponse[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [isUsingFallbackData, setIsUsingFallbackData] = useState(false)

  useEffect(() => {
    let isCancelled = false

    async function loadUsers() {
      try {
        const adminUsers = await getAdminUsers()

        if (isCancelled) {
          return
        }

        setUsers(adminUsers)
        setError(null)
        setIsUsingFallbackData(false)
      } catch (currentError) {
        if (isCancelled) {
          return
        }

        const fallbackUsers = user ? [mapUserToAdminUser(user)] : []
        setUsers(fallbackUsers)
        setError(getErrorMessage(currentError, t('admin.usersPage.loadError')))
        setIsUsingFallbackData(fallbackUsers.length > 0)
      } finally {
        if (!isCancelled) {
          setIsLoading(false)
        }
      }
    }

    void loadUsers()

    return () => {
      isCancelled = true
    }
  }, [t, user])

  const filteredUsers = useMemo(() => {
    const keyword = searchTerm.trim().toLowerCase()

    if (keyword === '') {
      return users
    }

    return users.filter((currentUser) =>
      [
        currentUser.username,
        currentUser.email,
        currentUser.phoneNumber,
        currentUser.status,
        ...currentUser.roles,
      ]
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    )
  }, [searchTerm, users])

  return (
    <AdminLayout>
      <div>
        <div>
          <h1 className="font-heading text-3xl font-bold text-foreground">
            {t('admin.usersPage.title')}
          </h1>
          <p className="mt-2 text-muted-foreground">
            {t('admin.usersPage.description')}
          </p>
        </div>

        {error && !isLoading && (
          <div className="mt-8 rounded-2xl border border-amber-400/30 bg-amber-50/70 p-4 text-sm text-amber-900 dark:bg-amber-950/20 dark:text-amber-200">
            <p className="font-semibold">
              {isUsingFallbackData
                ? t('admin.usersPage.fallbackNotice')
                : t('admin.usersPage.loadError')}
            </p>
            <p className="mt-2">{error}</p>
          </div>
        )}

        <div className="mt-8 rounded-2xl border border-border bg-card p-6">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <p className="text-sm text-muted-foreground">
                {t('admin.usersPage.totalUsers', {
                  count: formatNumber(filteredUsers.length),
                })}
              </p>
            </div>
            <div className="w-full lg:max-w-sm">
              <Input
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.currentTarget.value)}
                placeholder={t('admin.usersPage.searchPlaceholder')}
              />
            </div>
          </div>

          <div className="mt-6 overflow-x-auto">
            {isLoading ? (
              <div className="py-8 text-center text-muted-foreground">
                {t('common.loading')}
              </div>
            ) : filteredUsers.length === 0 ? (
              <div className="py-8 text-center text-muted-foreground">
                {t('admin.usersPage.empty')}
              </div>
            ) : (
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="px-4 py-3 text-left text-sm font-semibold text-foreground">
                      {t('admin.usersPage.columns.username')}
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-foreground">
                      {t('admin.usersPage.columns.contact')}
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-foreground">
                      {t('admin.usersPage.columns.roles')}
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-foreground">
                      {t('admin.usersPage.columns.status')}
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-foreground">
                      {t('admin.usersPage.columns.locked')}
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-foreground">
                      {t('admin.usersPage.columns.updatedAt')}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {filteredUsers.map((currentUser) => (
                    <tr key={currentUser.userId} className="border-b border-border">
                      <td className="px-4 py-4 text-sm">
                        <div className="font-medium text-foreground">
                          {currentUser.username}
                        </div>
                        <div className="mt-1 text-xs text-muted-foreground">
                          {currentUser.userId}
                        </div>
                      </td>
                      <td className="px-4 py-4 text-sm text-foreground">
                        <div>{currentUser.email}</div>
                        <div className="mt-1 text-xs text-muted-foreground">
                          {currentUser.phoneNumber || '—'}
                        </div>
                      </td>
                      <td className="px-4 py-4 text-sm">
                        <div className="flex flex-wrap gap-2">
                          {currentUser.roles.map((role) => (
                            <Badge key={`${currentUser.userId}-${role}`} variant="outline">
                              {getRoleLabel(role, t)}
                            </Badge>
                          ))}
                        </div>
                      </td>
                      <td className="px-4 py-4 text-sm">
                        <Badge variant={statusVariants[currentUser.status]}>
                          {getStatusLabel(currentUser.status, t)}
                        </Badge>
                      </td>
                      <td className="px-4 py-4 text-sm">
                        <Badge
                          variant={currentUser.locked ? 'destructive' : 'secondary'}
                        >
                          {currentUser.locked
                            ? t('admin.usersPage.locked')
                            : t('admin.usersPage.unlocked')}
                        </Badge>
                      </td>
                      <td className="px-4 py-4 text-sm text-muted-foreground">
                        {formatDate(currentUser.updatedAt)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </div>
      </div>
    </AdminLayout>
  )
}

function mapUserToAdminUser(user: User): AdminUserResponse {
  return {
    userId: user.id,
    username: user.username,
    email: user.email,
    phoneNumber: user.phoneNumber,
    status: user.status,
    locked: user.locked,
    roles: user.roles,
    createdAt: user.createdAt,
    updatedAt: user.updatedAt,
  }
}

function getRoleLabel(
  role: string,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  return knownRoles.includes(role as UserRole)
    ? getUserRoleLabel(role as UserRole, t)
    : role
}

function getStatusLabel(
  status: UserStatus,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  return status === 'ACTIVE'
    ? t('admin.usersPage.active')
    : t('admin.usersPage.inactive')
}
