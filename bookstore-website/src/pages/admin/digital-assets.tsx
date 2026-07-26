import { Link } from 'react-router-dom'
import {
  ArrowRight,
  BookOpen,
  CheckCircle2,
  FileText,
  ShieldCheck,
  ShoppingCart,
  Upload,
  type LucideIcon,
} from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useLanguage } from '@/contexts/language-context'

type GuideCard = {
  icon: LucideIcon
  title: string
  description: string
}

export default function AdminDigitalAssetsPage() {
  const { t } = useLanguage()
  const steps: GuideCard[] = [
    {
      icon: BookOpen,
      title: t('admin.digitalAssetsGuide.steps.pickBookTitle'),
      description: t('admin.digitalAssetsGuide.steps.pickBookDescription'),
    },
    {
      icon: Upload,
      title: t('admin.digitalAssetsGuide.steps.uploadTitle'),
      description: t('admin.digitalAssetsGuide.steps.uploadDescription'),
    },
    {
      icon: ShoppingCart,
      title: t('admin.digitalAssetsGuide.steps.sellTitle'),
      description: t('admin.digitalAssetsGuide.steps.sellDescription'),
    },
  ]
  const checklist = [
    t('admin.digitalAssetsGuide.checklist.mainFile'),
    t('admin.digitalAssetsGuide.checklist.published'),
    t('admin.digitalAssetsGuide.checklist.purchaseAllowed'),
    t('admin.digitalAssetsGuide.checklist.downloadAllowed'),
  ]

  return (
    <AdminLayout>
      <div className="relative overflow-hidden rounded-[32px] border border-border/60 bg-card/90 p-6 shadow-[0_28px_90px_rgba(2,6,23,0.35)] backdrop-blur xl:p-8">
        <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(59,130,246,0.18),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(16,185,129,0.13),transparent_32%)]" />

        <div className="relative space-y-7">
          <section className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_360px] xl:items-stretch">
            <div className="rounded-[28px] border border-primary/20 bg-background/55 p-6 shadow-[0_20px_60px_rgba(2,6,23,0.18)]">
              <Badge
                variant="outline"
                className="rounded-2xl border-primary/20 bg-primary/10 px-4 py-1.5 text-primary"
              >
                <FileText className="mr-2 h-4 w-4" />
                {t('admin.digitalAssetsGuide.badge')}
              </Badge>

              <h1 className="mt-5 font-heading text-4xl font-bold tracking-tight text-foreground">
                {t('admin.digitalAssetsGuide.title')}
              </h1>
              <p className="mt-4 max-w-3xl text-base leading-7 text-muted-foreground">
                {t('admin.digitalAssetsGuide.description')}
              </p>

              <div className="mt-6 flex flex-wrap gap-3">
                <Link to="/admin/books">
                  <Button className="h-12 rounded-2xl px-5 text-base">
                    {t('admin.digitalAssetsGuide.openBooks')}
                    <ArrowRight className="ml-2 h-4 w-4" />
                  </Button>
                </Link>
                <Link to="/library">
                  <Button
                    type="button"
                    variant="outline"
                    className="h-12 rounded-2xl px-5 text-base"
                  >
                    {t('admin.digitalAssetsGuide.previewLibrary')}
                  </Button>
                </Link>
              </div>
            </div>

            <div className="rounded-[28px] border border-emerald-400/20 bg-emerald-400/8 p-6">
              <div className="flex items-center gap-3">
                <span className="flex size-12 items-center justify-center rounded-2xl bg-emerald-400/12 text-emerald-500">
                  <ShieldCheck className="h-5 w-5" />
                </span>
                <div>
                  <p className="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                    {t('admin.digitalAssetsGuide.accessLabel')}
                  </p>
                  <h2 className="font-heading text-2xl font-bold text-foreground">
                    {t('admin.digitalAssetsGuide.accessTitle')}
                  </h2>
                </div>
              </div>
              <p className="mt-4 text-sm leading-6 text-muted-foreground">
                {t('admin.digitalAssetsGuide.accessDescription')}
              </p>
            </div>
          </section>

          <section className="grid gap-4 lg:grid-cols-3">
            {steps.map((step, index) => {
              const Icon = step.icon

              return (
                <article
                  key={step.title}
                  className="rounded-[26px] border border-border/60 bg-background/55 p-5 shadow-[0_18px_40px_rgba(2,6,23,0.16)]"
                >
                  <div className="flex items-start gap-4">
                    <span className="flex size-12 shrink-0 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                      <Icon className="h-5 w-5" />
                    </span>
                    <div>
                      <p className="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                        {t('admin.digitalAssetsGuide.stepLabel', {
                          count: index + 1,
                        })}
                      </p>
                      <h3 className="mt-2 font-heading text-xl font-bold text-foreground">
                        {step.title}
                      </h3>
                      <p className="mt-2 text-sm leading-6 text-muted-foreground">
                        {step.description}
                      </p>
                    </div>
                  </div>
                </article>
              )
            })}
          </section>

          <section className="rounded-[28px] border border-border/60 bg-background/55 p-6">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
              <div>
                <p className="text-sm font-semibold text-primary">
                  {t('admin.digitalAssetsGuide.checklistLabel')}
                </p>
                <h2 className="mt-1 font-heading text-2xl font-bold text-foreground">
                  {t('admin.digitalAssetsGuide.checklistTitle')}
                </h2>
              </div>
              <p className="max-w-xl text-sm leading-6 text-muted-foreground">
                {t('admin.digitalAssetsGuide.checklistDescription')}
              </p>
            </div>

            <div className="mt-5 grid gap-3 md:grid-cols-2">
              {checklist.map((item) => (
                <div
                  key={item}
                  className="flex items-start gap-3 rounded-2xl border border-border/50 bg-card/60 p-4 text-sm text-muted-foreground"
                >
                  <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-emerald-500" />
                  <span>{item}</span>
                </div>
              ))}
            </div>
          </section>
        </div>
      </div>
    </AdminLayout>
  )
}
