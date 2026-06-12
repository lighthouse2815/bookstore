import { Badge } from '@/components/common/badge'
import { Input } from '@/components/common/input'
import { useAdminPromotionsPage } from '@/hooks/use-admin-promotions-page'
import { AdminLayout } from '@/components/layout/admin-layout'
import type { AdminPromotionResponse } from '@/types/admin-access'

export default function AdminPromotionsPage() {
  const {
    t,
    formatCurrency,
    formatDate,
    formatNumber,
    filteredPromotions,
    searchTerm,
    isLoading,
    error,
    handleSearchTermChange,
  } = useAdminPromotionsPage()

  return (
    <AdminLayout>
      <div>
        <div>
          <h1 className="font-heading text-3xl font-bold text-foreground">
            {t('admin.promotionsPage.title')}
          </h1>
          <p className="mt-2 text-muted-foreground">
            {t('admin.promotionsPage.description')}
          </p>
        </div>

        {error && !isLoading && (
          <div className="mt-8 rounded-2xl border border-amber-400/30 bg-amber-50/70 p-4 text-sm text-amber-900 dark:bg-amber-950/20 dark:text-amber-200">
            <p className="font-semibold">{t('admin.promotionsPage.loadError')}</p>
            <p className="mt-2">{error}</p>
          </div>
        )}

        <div className="mt-8 rounded-2xl border border-border bg-card p-6">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <p className="text-sm text-muted-foreground">
              {t('admin.promotionsPage.totalPromotions', {
                count: formatNumber(filteredPromotions.length),
              })}
            </p>
            <div className="w-full lg:max-w-sm">
              <Input
                value={searchTerm}
                onChange={handleSearchTermChange}
                placeholder={t('admin.promotionsPage.searchPlaceholder')}
              />
            </div>
          </div>

          <div className="mt-6 overflow-x-auto">
            {isLoading ? (
              <div className="py-8 text-center text-muted-foreground">
                {t('common.loading')}
              </div>
            ) : filteredPromotions.length === 0 ? (
              <div className="py-8 text-center text-muted-foreground">
                {t('admin.promotionsPage.empty')}
              </div>
            ) : (
              <table className="w-full">
                <thead>
                  <tr className="border-b border-border">
                    <th className="px-4 py-3 text-left text-sm font-semibold text-foreground">
                      {t('admin.promotionsPage.columns.campaign')}
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-foreground">
                      {t('admin.promotionsPage.columns.discount')}
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-foreground">
                      {t('admin.promotionsPage.columns.usage')}
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-foreground">
                      {t('admin.promotionsPage.columns.schedule')}
                    </th>
                    <th className="px-4 py-3 text-left text-sm font-semibold text-foreground">
                      {t('admin.promotionsPage.columns.status')}
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {filteredPromotions.map((promotion) => (
                    <tr key={promotion.id} className="border-b border-border">
                      <td className="px-4 py-4 text-sm">
                        <div className="font-medium text-foreground">
                          {promotion.name}
                        </div>
                        <div className="mt-1 text-xs text-muted-foreground">
                          {promotion.code}
                        </div>
                        <div className="mt-2 text-xs text-muted-foreground">
                          {promotion.description ||
                            t('admin.promotionsPage.noDescription')}
                        </div>
                      </td>
                      <td className="px-4 py-4 text-sm">
                        <Badge variant="outline">
                          {getPromotionTypeLabel(promotion.discountType, t)}
                        </Badge>
                        <div className="mt-2 font-medium text-foreground">
                          {formatPromotionValue(
                            promotion.discountType,
                            promotion.discountValue,
                            formatCurrency,
                          )}
                        </div>
                      </td>
                      <td className="px-4 py-4 text-sm text-foreground">
                        {promotion.usageLimit
                          ? t('admin.promotionsPage.usageWithLimit', {
                              used: formatNumber(promotion.usedCount),
                              limit: formatNumber(promotion.usageLimit),
                            })
                          : t('admin.promotionsPage.usageNoLimit', {
                              used: formatNumber(promotion.usedCount),
                            })}
                      </td>
                      <td className="px-4 py-4 text-sm text-muted-foreground">
                        <div>{formatOptionalDate(promotion.startAt, formatDate, t)}</div>
                        <div className="mt-1">
                          {formatOptionalDate(promotion.endAt, formatDate, t)}
                        </div>
                      </td>
                      <td className="px-4 py-4 text-sm">
                        <Badge variant={getPromotionStatusVariant(promotion)}>
                          {getPromotionStatusLabel(promotion, t)}
                        </Badge>
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

function getPromotionTypeLabel(
  discountType: string,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  return discountType.toUpperCase().includes('PERCENT')
    ? t('admin.promotionsPage.percentType')
    : t('admin.promotionsPage.fixedType')
}

function formatPromotionValue(
  discountType: string,
  discountValue: number,
  formatCurrency: (value: number) => string,
) {
  return discountType.toUpperCase().includes('PERCENT')
    ? `${discountValue}%`
    : formatCurrency(discountValue)
}

function formatOptionalDate(
  value: string | null,
  formatDate: (value: string | number | Date) => string,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  return value ? formatDate(value) : t('admin.promotionsPage.noEndDate')
}

function getPromotionStatusVariant(promotion: AdminPromotionResponse) {
  const currentTime = Date.now()
  const startAt = promotion.startAt ? new Date(promotion.startAt).getTime() : null
  const endAt = promotion.endAt ? new Date(promotion.endAt).getTime() : null

  if (!promotion.active) {
    return 'secondary' as const
  }

  if (startAt && startAt > currentTime) {
    return 'outline' as const
  }

  if (endAt && endAt < currentTime) {
    return 'destructive' as const
  }

  return 'default' as const
}

function getPromotionStatusLabel(
  promotion: AdminPromotionResponse,
  t: (key: string, params?: Record<string, number | string>) => string,
) {
  const currentTime = Date.now()
  const startAt = promotion.startAt ? new Date(promotion.startAt).getTime() : null
  const endAt = promotion.endAt ? new Date(promotion.endAt).getTime() : null

  if (!promotion.active) {
    return t('admin.promotionsPage.statuses.inactive')
  }

  if (startAt && startAt > currentTime) {
    return t('admin.promotionsPage.statuses.upcoming')
  }

  if (endAt && endAt < currentTime) {
    return t('admin.promotionsPage.statuses.expired')
  }

  return t('admin.promotionsPage.statuses.active')
}
