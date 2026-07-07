import { User } from 'lucide-react'
import { AdminUserManagementPage } from '@/components/admin/admin-user-management-page'
import { useLanguage } from '@/contexts/language-context'
import { getAdminManagedUsersPage } from '@/services/admin-access-service'

export default function AdminStaffPage() {
  const { t } = useLanguage()

  return (
    <AdminUserManagementPage
      countIcon={User}
      title={t('admin.staffPage.title')}
      description={t('admin.staffPage.description')}
      totalUsersLabel={(countLabel) =>
        t('admin.staffPage.totalUsers', { count: countLabel })
      }
      searchPlaceholder={t('admin.staffPage.searchPlaceholder')}
      loadErrorLabel={t('admin.staffPage.loadError')}
      mode="staff"
      emptyLabel={t('admin.staffPage.empty')}
      fetchUsers={getAdminManagedUsersPage}
    />
  )
}
