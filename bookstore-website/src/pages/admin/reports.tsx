import { ReportExportCenter } from '@/components/admin/report-export-center'
import { PageHeader } from '@/components/common/page-shell'
import { useLanguage } from '@/contexts/language-context'
import { AdminLayout } from '@/components/layout/admin-layout'

export default function AdminReportsPage() {
  const { t } = useLanguage()

  return (
    <AdminLayout>
      <div className="space-y-8">
        <PageHeader
          title={t('admin.reportsPage.title')}
          description={t('admin.reportsPage.description')}
        />
        <ReportExportCenter />
      </div>
    </AdminLayout>
  )
}
