import { Users } from 'lucide-react'
import { AdminUserManagementPage } from '@/components/admin/admin-user-management-page'
import { useLanguage } from '@/contexts/language-context'
import { getAdminCustomers } from '@/services/admin-access-service'

export default function AdminCustomersPage() {
  const { language } = useLanguage()
  const isVietnamese = language === 'vi'

  return (
    <AdminUserManagementPage
      countIcon={Users}
      title={isVietnamese ? 'Quản lý khách hàng' : 'Manage customers'}
      description={
        isVietnamese
          ? 'Xem danh sách khách hàng đang hoạt động trong hệ thống.'
          : 'Review active customer accounts from the backend.'
      }
      totalUsersLabel={(countLabel) =>
        isVietnamese ? `${countLabel} khách hàng` : `${countLabel} customers`
      }
      searchPlaceholder={
        isVietnamese
          ? 'Tìm theo username hoặc vai trò...'
          : 'Search by username or role...'
      }
      loadErrorLabel={
        isVietnamese
          ? 'Không tải được danh sách khách hàng'
          : 'Unable to load the customer list'
      }
      mode="customer"
      emptyLabel={
        isVietnamese ? 'Chưa có khách hàng nào' : 'No customers found'
      }
      fetchUsers={getAdminCustomers}
    />
  )
}
