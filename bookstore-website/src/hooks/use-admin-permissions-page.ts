import { useEffect, useMemo, useState, type ChangeEvent } from 'react'
import { useLanguage } from '@/contexts/language-context'
import { getAdminPermissions } from '@/services/admin-access-service'
import type { AdminPermissionResponse } from '@/types/admin-access'
import { getErrorMessage } from '@/utils'

export function useAdminPermissionsPage() {
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

  function handleSearchTermChange(event: ChangeEvent<HTMLInputElement>) {
    setSearchTerm(event.currentTarget.value)
  }

  function openPermissionDetail(permission: AdminPermissionResponse) {
    setSelectedPermission(permission)
  }

  function closePermissionDetail() {
    setSelectedPermission(null)
  }

  return {
    t,
    formatDate,
    formatNumber,
    labels,
    permissions,
    searchTerm,
    isLoading,
    error,
    selectedPermission,
    filteredPermissions,
    handleSearchTermChange,
    openPermissionDetail,
    closePermissionDetail,
  }
}
