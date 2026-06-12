import { useEffect, useRef, useState } from 'react'
import { X } from 'lucide-react'
import { useLanguage } from '@/contexts/language-context'
import { getRegisterTermsCopy } from '@/utils/register-terms'

type RegisterTermsDialogProps = {
  open: boolean
  onClose: () => void
}

const CLOSE_UNLOCK_OFFSET = 24

export function RegisterTermsDialog({
  open,
  onClose,
}: RegisterTermsDialogProps) {
  const { language, t } = useLanguage()
  const scrollRef = useRef<HTMLDivElement | null>(null)
  const [hasReachedBottom, setHasReachedBottom] = useState(false)
  const termsCopy = getRegisterTermsCopy(language)

  useEffect(() => {
    if (!open) {
      return
    }

    const previousOverflow = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    setHasReachedBottom(false)

    requestAnimationFrame(() => {
      if (!scrollRef.current) {
        return
      }

      scrollRef.current.scrollTop = 0
      updateScrollState(scrollRef.current)
    })

    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [open])

  function updateScrollState(element: HTMLDivElement) {
    const reachedBottom =
      element.scrollTop + element.clientHeight >=
      element.scrollHeight - CLOSE_UNLOCK_OFFSET

    if (reachedBottom) {
      setHasReachedBottom(true)
    }
  }

  function handleScroll(event: React.UIEvent<HTMLDivElement>) {
    updateScrollState(event.currentTarget)
  }

  if (!open) {
    return null
  }

  return (
    <div className="fixed inset-0 z-[120] bg-slate-950/65 px-4 py-6 backdrop-blur-sm">
      <div
        className="mx-auto flex h-full w-full max-w-3xl items-center justify-center"
        role="dialog"
        aria-modal="true"
        aria-labelledby="register-terms-title"
      >
        <div className="flex max-h-full w-full flex-col overflow-hidden rounded-[28px] border border-border bg-background shadow-[0_30px_90px_rgba(15,23,42,0.28)]">
          <div className="flex items-start justify-between gap-4 border-b border-border px-6 py-5">
            <div className="space-y-2">
              <p className="text-xs font-semibold uppercase tracking-[0.25em] text-primary/80">
                SáchVui Terms
              </p>
              <h3
                id="register-terms-title"
                className="text-2xl font-semibold text-foreground"
              >
                {termsCopy.dialogTitle}
              </h3>
              <p className="max-w-2xl text-sm leading-6 text-muted-foreground">
                {hasReachedBottom
                  ? termsCopy.closeReady
                  : termsCopy.closeHint}
              </p>
            </div>

            <button
              type="button"
              aria-label={t('common.close')}
              onClick={onClose}
              disabled={!hasReachedBottom}
              className="inline-flex h-11 w-11 flex-shrink-0 items-center justify-center rounded-full border border-border bg-card text-foreground transition hover:border-primary/30 hover:text-primary disabled:cursor-not-allowed disabled:opacity-35"
            >
              <X className="h-5 w-5" />
            </button>
          </div>

          <div
            ref={scrollRef}
            onScroll={handleScroll}
            className="space-y-6 overflow-y-auto px-6 py-6 text-sm leading-7 text-foreground"
          >
            <p className="rounded-2xl border border-primary/15 bg-primary/5 px-4 py-4 text-muted-foreground">
              {termsCopy.intro}
            </p>

            {termsCopy.sections.map((section) => (
              <section
                key={section.title}
                className="rounded-2xl border border-border/80 bg-card/70 px-5 py-5"
              >
                <h4 className="mb-3 text-base font-semibold text-foreground">
                  {section.title}
                </h4>
                <div className="space-y-3 text-muted-foreground">
                  {section.paragraphs.map((paragraph) => (
                    <p key={paragraph}>{paragraph}</p>
                  ))}
                </div>
              </section>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
