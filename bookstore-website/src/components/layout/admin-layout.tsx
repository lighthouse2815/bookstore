import { useState, type ReactNode } from 'react'
import { Menu } from 'lucide-react'
import { Button } from '@/components/common/button'
import { useLanguage } from '@/contexts/language-context'
import { useAdminCommandPalette } from '@/hooks/use-admin-command-palette'
import { AdminCommandPalette } from './admin-command-palette'
import { AdminSidebar } from './admin-sidebar'

type AdminLayoutProps = {
  children: ReactNode
}

export function AdminLayout({ children }: AdminLayoutProps) {
  const commandPalette = useAdminCommandPalette()
  const { t } = useLanguage()
  const [isSidebarOpen, setIsSidebarOpen] = useState(false)

  return (
    <div className="min-h-screen bg-background lg:flex">
      {isSidebarOpen ? (
        <button
          type="button"
          className="fixed inset-0 z-30 bg-slate-950/40 backdrop-blur-sm lg:hidden"
          aria-label={t('admin.sidebar.mobileClose')}
          onClick={() => setIsSidebarOpen(false)}
        />
      ) : null}

      <AdminSidebar
        isOpen={isSidebarOpen}
        onClose={() => setIsSidebarOpen(false)}
        onOpenCommandPalette={commandPalette.open}
      />

      <div className="min-w-0 flex-1">
        <div className="sticky top-0 z-20 flex items-center justify-between gap-3 border-b border-border/70 bg-background/95 px-4 py-3 backdrop-blur lg:hidden">
          <div>
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
              {t('common.dashboard')}
            </p>
            <p className="font-heading text-lg font-semibold text-foreground">
              {t('admin.sidebar.title')}
            </p>
          </div>
          <Button
            type="button"
            variant="outline"
            className="rounded-2xl"
            onClick={() => setIsSidebarOpen(true)}
          >
            <Menu className="h-4 w-4" />
            {t('admin.sidebar.mobileOpen')}
          </Button>
        </div>

        <main className="min-h-0 overflow-y-auto">
          <div className="mx-auto min-h-full w-full max-w-[1600px] p-4 sm:p-6 lg:p-8">
            {children}
          </div>
        </main>
      </div>

      <AdminCommandPalette palette={commandPalette} />
    </div>
  )
}
