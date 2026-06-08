import { useLanguage } from '@/contexts/language-context'
import { cn } from '@/utils'

type LanguageSwitcherProps = {
  className?: string
}

export function LanguageSwitcher({ className }: LanguageSwitcherProps) {
  const { language, setLanguage, t } = useLanguage()

  return (
    <div
      className={cn(
        'inline-flex items-center rounded-full border border-border bg-background p-1',
        className,
      )}
      aria-label={t('language.switcherAria')}
      role="group"
    >
      {(['vi', 'en'] as const).map((nextLanguage) => (
        <button
          key={nextLanguage}
          type="button"
          onClick={() => setLanguage(nextLanguage)}
          className={cn(
            'rounded-full px-3 py-1 text-xs font-semibold transition-colors',
            language === nextLanguage
              ? 'bg-primary text-primary-foreground'
              : 'text-muted-foreground hover:text-foreground',
          )}
        >
          {t(`language.${nextLanguage}`)}
        </button>
      ))}
    </div>
  )
}
