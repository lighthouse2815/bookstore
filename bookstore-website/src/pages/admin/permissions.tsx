import { useEffect, useMemo, useState } from 'react'
import { Badge } from '@/components/common/badge'
import { Input } from '@/components/common/input'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useLanguage } from '@/contexts/language-context'
import { getAdminPermissions } from '@/services/admin-access-service'
import type { AdminPermissionResponse } from '@/types/admin-access'
import { getErrorMessage } from '@/utils'

export default function AdminPermissionsPage() {
  const { t, formatDate, formatNumber } = useLanguage()
  const [permissions, setPermissions] = useState<AdminPermissionResponse[]>([])
  const [searchTerm, setSearchTerm] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

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
            getErrorMessage(currentError, t('admin.permissionsPage.loadError'))
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

  return (
    <AdminLayout>
      <div>
        <div>
          <h1 className="font-heading text-3xl font-bold text-foreground">
            {t('admin.permissionsPage.title')}
          </h1>
          <p className="mt-2 text-muted-foreground">
            {t('admin.permissionsPage.description')}
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
              {t('admin.permissionsPage.totalPermissions', {
                count: formatNumber(filteredPermissions.length),
              })}
            </p>
            <div className="w-full lg:max-w-sm">
              <Input
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.currentTarget.value)}
                placeholder={t('admin.permissionsPage.searchPlaceholder')}
              />
            </div>
          </div>

          <div className="mt-6 overflow-x-auto">
            {isLoading ? (
              <div className="py-8 text-center text-muted-foreground">
                {t('common.loading')}
              </div>
            ) : filteredPermissions.length === 0 ? (
              <div className="py-8 text-center text-muted-foreground">
                {t('admin.permissionsPage.empty')}
              </div>
            ) : (
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="px-4 py-3 text-left text-sm font-semibold text-foreground">
                      {t('admin.permissionsPage.columns.code')}
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-foreground">
                      {t('admin.permissionsPage.columns.description')}
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-foreground">
                      {t('admin.permissionsPage.columns.updatedAt')}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {filteredPermissions.map((permission) => (
                    <tr key={permission.id} className="border-b border-border">
                      <td className="px-4 py-4 text-sm">
                        <Badge variant="outline">{permission.code}</Badge>
                      </td>
                      <td className="px-4 py-4 text-sm text-foreground">
                        {permission.description ||
                          t('admin.permissionsPage.noDescription')}
                      </td>
                      <td className="px-4 py-4 text-sm text-muted-foreground">
                        {formatDate(permission.updatedAt)}
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
