import {
  startTransition,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import {
  BookOpenText,
  LoaderCircle,
  RefreshCw,
  Sparkles,
  Store,
} from 'lucide-react'
import { Button } from '@/components/common/button'
import { useLanguage } from '@/contexts/language-context'
import {
  probeBackendReady,
  rememberBackendReadyProbe,
  shouldUseDeployStartupGate,
} from '@/services/api'

const INITIAL_PROBE_GRACE_MS = 900
const RETRY_INTERVAL_MS = 3500

export function DeployStartupGate({ children }: { children: ReactNode }) {
  const { t } = useLanguage()
  const gateEnabled = shouldUseDeployStartupGate()
  const [isReady, setIsReady] = useState(() => !gateEnabled)
  const [isGateVisible, setIsGateVisible] = useState(false)
  const [attemptCount, setAttemptCount] = useState(0)
  const [elapsedSeconds, setElapsedSeconds] = useState(0)
  const [retryToken, setRetryToken] = useState(0)

  const phases = useMemo(
    () => [
      {
        icon: LoaderCircle,
        label: t('common.deployStartup.phases.boot'),
      },
      {
        icon: Sparkles,
        label: t('common.deployStartup.phases.warmup'),
      },
      {
        icon: BookOpenText,
        label: t('common.deployStartup.phases.catalog'),
      },
    ],
    [t],
  )

  const activePhaseIndex = Math.floor(elapsedSeconds / 4) % phases.length

  useEffect(() => {
    if (!gateEnabled) {
      setIsReady(true)
      setIsGateVisible(false)
      return
    }

    if (isReady) {
      setIsGateVisible(false)
      return
    }

    let cancelled = false
    let gateVisibilityTimer: number | undefined
    let retryTimer: number | undefined
    let elapsedTimer: number | undefined
    let controller: AbortController | null = null

    gateVisibilityTimer = window.setTimeout(() => {
      setIsGateVisible(true)
    }, INITIAL_PROBE_GRACE_MS)

    const runProbe = async () => {
      controller?.abort()
      controller = new AbortController()
      setAttemptCount((currentValue) => currentValue + 1)

      const ready = await probeBackendReady(controller.signal)
      if (cancelled) {
        return
      }

      if (ready) {
        rememberBackendReadyProbe()

        if (gateVisibilityTimer !== undefined) {
          window.clearTimeout(gateVisibilityTimer)
        }

        startTransition(() => {
          setIsGateVisible(false)
          setIsReady(true)
        })
        return
      }

      setIsGateVisible(true)

      retryTimer = window.setTimeout(() => {
        void runProbe()
      }, RETRY_INTERVAL_MS)
    }

    elapsedTimer = window.setInterval(() => {
      setElapsedSeconds((currentValue) => currentValue + 1)
    }, 1000)

    void runProbe()

    return () => {
      cancelled = true
      controller?.abort()

      if (gateVisibilityTimer !== undefined) {
        window.clearTimeout(gateVisibilityTimer)
      }

      if (retryTimer !== undefined) {
        window.clearTimeout(retryTimer)
      }

      if (elapsedTimer !== undefined) {
        window.clearInterval(elapsedTimer)
      }
    }
  }, [gateEnabled, isReady, retryToken])

  if (isReady) {
    return <>{children}</>
  }

  if (!isGateVisible) {
    return <div aria-hidden="true" className="min-h-screen bg-background" />
  }

  return (
    <div className="relative min-h-screen overflow-hidden bg-background text-foreground">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(124,58,237,0.18),_transparent_40%),radial-gradient(circle_at_bottom_right,_rgba(14,165,233,0.16),_transparent_38%)]" />
      <div className="pointer-events-none absolute -left-16 top-14 h-40 w-40 rounded-full bg-primary/12 blur-3xl animate-render-orb-drift" />
      <div className="pointer-events-none absolute right-0 top-1/3 h-56 w-56 rounded-full bg-secondary/15 blur-3xl animate-render-orb-drift [animation-delay:-1.8s]" />
      <div className="pointer-events-none absolute bottom-0 left-1/3 h-48 w-48 rounded-full bg-accent/12 blur-3xl animate-render-orb-drift [animation-delay:-3.1s]" />

      <div className="relative flex min-h-screen items-center justify-center px-4 py-10">
        <div className="w-full max-w-3xl rounded-[2rem] border border-white/40 bg-background/82 p-6 shadow-[0_30px_120px_-40px_rgba(91,33,182,0.45)] backdrop-blur-xl dark:border-white/10 dark:bg-card/88 sm:p-8">
          <div className="flex flex-col gap-8 lg:flex-row lg:items-center lg:gap-10">
            <div className="flex-1">
              <div className="inline-flex items-center gap-2 rounded-full border border-primary/15 bg-primary/8 px-3 py-1 text-xs font-medium text-primary">
                <Store className="size-3.5" />
                {t('common.deployStartup.badge')}
              </div>

              <div className="mt-5 space-y-4">
                <div className="space-y-2">
                  <h1 className="font-heading text-3xl font-semibold tracking-tight sm:text-4xl">
                    {t('common.deployStartup.title')}
                  </h1>
                  <p className="max-w-xl text-sm leading-6 text-muted-foreground sm:text-base">
                    {t('common.deployStartup.description')}
                  </p>
                </div>

                <p className="max-w-lg text-sm leading-6 text-muted-foreground">
                  {t('common.deployStartup.hint')}
                </p>
              </div>

              <div className="mt-6 flex flex-wrap gap-3">
                <div className="rounded-2xl border border-border/70 bg-card/75 px-4 py-3">
                  <p className="text-[0.68rem] font-semibold uppercase tracking-[0.22em] text-muted-foreground">
                    {t('common.deployStartup.waitedLabel')}
                  </p>
                  <p className="mt-1 text-lg font-semibold">
                    {t('common.deployStartup.waitedSeconds', {
                      seconds: elapsedSeconds,
                    })}
                  </p>
                </div>

                <div className="rounded-2xl border border-border/70 bg-card/75 px-4 py-3">
                  <p className="text-[0.68rem] font-semibold uppercase tracking-[0.22em] text-muted-foreground">
                    {t('common.deployStartup.retryLabel')}
                  </p>
                  <p className="mt-1 text-lg font-semibold">
                    {t('common.deployStartup.retryCount', {
                      count: attemptCount,
                    })}
                  </p>
                </div>
              </div>

              <div className="mt-6 flex flex-wrap items-center gap-3">
                <Button
                  size="lg"
                  variant="secondary"
                  onClick={() => setRetryToken((currentValue) => currentValue + 1)}
                >
                  <RefreshCw className="size-4" />
                  {t('common.deployStartup.retryNow')}
                </Button>
                <p className="text-xs text-muted-foreground sm:text-sm">
                  {t('common.deployStartup.footer')}
                </p>
              </div>
            </div>

            <div className="relative flex flex-1 flex-col gap-5">
              <div className="relative overflow-hidden rounded-[1.75rem] border border-border/70 bg-card/85 p-6 shadow-[inset_0_1px_0_rgba(255,255,255,0.6)] dark:shadow-none">
                <div className="absolute inset-x-6 top-0 h-px bg-gradient-to-r from-transparent via-primary/35 to-transparent" />

                <div className="flex items-end gap-3">
                  <div className="animate-render-book-bob rounded-2xl bg-primary px-4 py-8 text-primary-foreground shadow-lg shadow-primary/20">
                    <BookOpenText className="size-7" />
                  </div>
                  <div className="animate-render-book-bob rounded-2xl bg-secondary px-4 py-6 text-secondary-foreground shadow-lg shadow-secondary/20 [animation-delay:-1.3s]">
                    <Sparkles className="size-6" />
                  </div>
                  <div className="animate-render-book-bob rounded-2xl bg-accent px-4 py-10 text-accent-foreground shadow-lg shadow-accent/20 [animation-delay:-2.6s]">
                    <LoaderCircle className="size-7 animate-spin" />
                  </div>
                </div>

                <div className="mt-6 space-y-3" aria-live="polite">
                  {phases.map((phase, index) => {
                    const PhaseIcon = phase.icon
                    const isActive = activePhaseIndex === index

                    return (
                      <div
                        key={phase.label}
                        className={`flex items-center gap-3 rounded-2xl border px-4 py-3 transition-all ${
                          isActive
                            ? 'border-primary/30 bg-primary/10 text-foreground shadow-sm'
                            : 'border-border/70 bg-background/70 text-muted-foreground'
                        }`}
                      >
                        <div
                          className={`flex size-9 items-center justify-center rounded-full ${
                            isActive
                              ? 'bg-primary text-primary-foreground'
                              : 'bg-muted text-muted-foreground'
                          }`}
                        >
                          <PhaseIcon className={`size-4 ${isActive ? 'animate-pulse-soft' : ''}`} />
                        </div>
                        <div className="flex-1">
                          <p className="text-sm font-medium">{phase.label}</p>
                        </div>
                        <div className="flex items-center gap-1">
                          <span className="size-2 rounded-full bg-primary/85 animate-render-dot" />
                          <span className="size-2 rounded-full bg-primary/55 animate-render-dot [animation-delay:-0.25s]" />
                          <span className="size-2 rounded-full bg-primary/30 animate-render-dot [animation-delay:-0.5s]" />
                        </div>
                      </div>
                    )
                  })}
                </div>

                <div className="mt-6 overflow-hidden rounded-full bg-muted">
                  <div className="h-2 w-1/3 rounded-full bg-gradient-to-r from-primary via-secondary to-accent animate-render-progress" />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
