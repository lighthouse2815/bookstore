import { Users } from 'lucide-react'
import { AdminUserManagementPage } from '@/components/admin/admin-user-management-page'
import { useLanguage } from '@/contexts/language-context'
import { getAdminCustomersPage } from '@/services/admin-access-service'

export default function AdminCustomersPage() {
  const { t } = useLanguage()

  return (
    <AdminUserManagementPage
      countIcon={Users}
      title={t('admin.customersPage.title')}
      description={t('admin.customersPage.description')}
      totalUsersLabel={(countLabel) =>
        t('admin.customersPage.totalUsers', { count: countLabel })
      }
      searchPlaceholder={t('admin.customersPage.searchPlaceholder')}
      loadErrorLabel={t('admin.customersPage.loadError')}
      mode="customer"
      emptyLabel={t('admin.customersPage.empty')}
      fetchUsers={getAdminCustomersPage}
    />
  )
}
