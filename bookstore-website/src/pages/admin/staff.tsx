import { User } from 'lucide-react'
import { AdminUserManagementPage } from '@/components/admin/admin-user-management-page'
import { useLanguage } from '@/contexts/language-context'
import { getAdminEmployees } from '@/services/admin-access-service'

export default function AdminStaffPage() {
  const { language } = useLanguage()
  const isVietnamese = language === 'vi'

  return (
    <AdminUserManagementPage
      countIcon={User}
      title={isVietnamese ? 'Quản lý nhân viên' : 'Manage staff'}
      description={
        isVietnamese
          ? 'Xem chung các tài khoản staff và admin trong khu vực quản trị.'
          : 'Review both staff and admin accounts from the backend.'
      }
      totalUsersLabel={(countLabel) =>
        isVietnamese ? `${countLabel} nhân viên` : `${countLabel} staff members`
      }
      searchPlaceholder={
        isVietnamese
          ? 'Tìm theo username hoặc vai trò...'
          : 'Search by username or role...'
      }
      loadErrorLabel={
        isVietnamese
          ? 'Không tải được danh sách nhân viên'
          : 'Unable to load the staff list'
      }
      mode="staff"
      emptyLabel={isVietnamese ? 'Chưa có nhân viên nào' : 'No staff members found'}
      fetchUsers={getAdminEmployees}
    />
  )
}
