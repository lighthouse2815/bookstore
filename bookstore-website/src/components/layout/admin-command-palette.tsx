import type { KeyboardEvent, ReactNode } from 'react'
import { ArrowRight, Search } from 'lucide-react'
import { Input } from '@/components/common/input'
import { useLanguage } from '@/contexts/language-context'
import { cn } from '@/utils'
import type { AdminCommandItem } from '@/types/admin-command'
import type { UseAdminCommandPaletteResult } from '@/hooks/use-admin-command-palette'

type AdminCommandPaletteProps = {
  palette: UseAdminCommandPaletteResult
}

export function AdminCommandPalette({ palette }: AdminCommandPaletteProps) {
  const { t } = useLanguage()
  const {
    isOpen,
    query,
    commands,
    highlightedIndex,
    close,
    setQuery,
    setHighlightedIndex,
    moveHighlight,
    executeCommand,
    executeHighlightedCommand,
  } = palette

  if (!isOpen) {
    return null
  }

  const navigationCommands = commands.filter(
    (command) => command.group === 'navigation',
  )
  const actionCommands = commands.filter((command) => command.group === 'action')

  function handleInputKeyDown(event: KeyboardEvent<HTMLInputElement>) {
    if (event.key === 'ArrowDown') {
      event.preventDefault()
      moveHighlight(1)
      return
    }

    if (event.key === 'ArrowUp') {
      event.preventDefault()
      moveHighlight(-1)
      return
    }

    if (event.key === 'Enter') {
      event.preventDefault()
      void executeHighlightedCommand()
      return
    }

    if (event.key === 'Escape') {
      event.preventDefault()
      close()
    }
  }

  return (
    <div
      className="fixed inset-0 z-[80] bg-slate-950/45 px-4 py-8 backdrop-blur-sm"
      onClick={close}
    >
      <div
        className="mx-auto flex max-h-[min(44rem,calc(100vh-4rem))] w-full max-w-3xl flex-col overflow-hidden rounded-[1.75rem] border border-border/70 bg-card shadow-[0_32px_100px_rgba(15,23,42,0.32)]"
        onClick={(event) => {
          event.stopPropagation()
        }}
      >
        <div className="border-b border-border/70 px-5 py-4 sm:px-6">
          <div className="flex items-start gap-3">
            <span className="mt-1 flex size-10 shrink-0 items-center justify-center rounded-2xl bg-primary/10 text-primary">
              <Search className="size-5" />
            </span>
            <div className="min-w-0 flex-1">
              <p className="font-heading text-xl font-semibold">
                {t('admin.commandPalette.title')}
              </p>
              <p className="mt-1 text-sm leading-6 text-muted-foreground">
                {t('admin.commandPalette.description')}
              </p>
            </div>
          </div>

          <div className="relative mt-4">
            <Search className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              autoFocus
              value={query}
              onChange={(event) => setQuery(event.target.value)}
              onKeyDown={handleInputKeyDown}
              placeholder={t('admin.commandPalette.placeholder')}
              className="h-11 rounded-2xl border-border/70 bg-background pl-10 text-sm"
            />
          </div>
        </div>

        <div className="min-h-0 flex-1 overflow-y-auto px-2 py-3">
          {commands.length === 0 ? (
            <div className="flex min-h-56 flex-col items-center justify-center rounded-[1.5rem] border border-dashed border-border/70 bg-muted/35 px-6 text-center">
              <p className="font-heading text-lg font-semibold">
                {t('admin.commandPalette.emptyTitle')}
              </p>
              <p className="mt-2 max-w-md text-sm leading-6 text-muted-foreground">
                {t('admin.commandPalette.emptyDescription')}
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              {navigationCommands.length > 0 ? (
                <CommandGroup title={t('admin.commandPalette.navigationGroup')}>
                  {navigationCommands.map((command, index) => (
                    <CommandRow
                      key={command.id}
                      command={command}
                      isHighlighted={highlightedIndex === index}
                      onHover={() => setHighlightedIndex(index)}
                      onSelect={() => {
                        void executeCommand(command)
                      }}
                    />
                  ))}
                </CommandGroup>
              ) : null}

              {actionCommands.length > 0 ? (
                <CommandGroup title={t('admin.commandPalette.actionsGroup')}>
                  {actionCommands.map((command, index) => (
                    <CommandRow
                      key={command.id}
                      command={command}
                      isHighlighted={
                        highlightedIndex === navigationCommands.length + index
                      }
                      onHover={() =>
                        setHighlightedIndex(navigationCommands.length + index)
                      }
                      onSelect={() => {
                        void executeCommand(command)
                      }}
                    />
                  ))}
                </CommandGroup>
              ) : null}
            </div>
          )}
        </div>

        <div className="flex flex-wrap items-center gap-2 border-t border-border/70 px-5 py-3 text-xs text-muted-foreground sm:px-6">
          <PaletteHint>{t('admin.commandPalette.shortcutOpen')}</PaletteHint>
          <PaletteHint>{t('admin.commandPalette.shortcutMove')}</PaletteHint>
          <PaletteHint>{t('admin.commandPalette.shortcutSelect')}</PaletteHint>
          <PaletteHint>{t('admin.commandPalette.shortcutClose')}</PaletteHint>
        </div>
      </div>
    </div>
  )
}

function CommandGroup({
  title,
  children,
}: {
  title: string
  children: ReactNode
}) {
  return (
    <section className="space-y-2">
      <p className="px-3 text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground">
        {title}
      </p>
      <div className="space-y-1">{children}</div>
    </section>
  )
}

function CommandRow({
  command,
  isHighlighted,
  onHover,
  onSelect,
}: {
  command: AdminCommandItem
  isHighlighted: boolean
  onHover: () => void
  onSelect: () => void
}) {
  const Icon = command.icon

  return (
    <button
      type="button"
      onMouseEnter={onHover}
      onFocus={onHover}
      onClick={onSelect}
      className={cn(
        'flex w-full items-center gap-3 rounded-2xl px-3 py-3 text-left transition-colors',
        isHighlighted
          ? 'bg-primary/8 text-foreground'
          : 'hover:bg-muted/65 text-foreground',
      )}
    >
      <span
        className={cn(
          'flex size-10 shrink-0 items-center justify-center rounded-2xl border',
          isHighlighted
            ? 'border-primary/25 bg-primary/10 text-primary'
            : 'border-border/70 bg-background text-muted-foreground',
        )}
      >
        <Icon className="size-4" />
      </span>

      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-2">
          <p className="truncate text-sm font-semibold">{command.label}</p>
        </div>
        <p className="mt-1 truncate text-sm text-muted-foreground">
          {command.subtitle}
        </p>
      </div>

      <ArrowRight className="size-4 shrink-0 text-muted-foreground" />
    </button>
  )
}

function PaletteHint({ children }: { children: ReactNode }) {
  return (
    <span className="rounded-full border border-border/70 bg-background px-2.5 py-1">
      {children}
    </span>
  )
}
