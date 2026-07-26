import { useEffect, useRef, useState } from 'react'
import { toast } from 'sonner'
import { useLanguage } from '@/contexts/language-context'
import { cn } from '@/utils'

const GOOGLE_SCRIPT_SRC = 'https://accounts.google.com/gsi/client'

let googleScriptPromise: Promise<void> | null = null
let initializedClientId: string | null = null
let activeCredentialHandler: ((credential: string) => void) | null = null

type GoogleAuthButtonText = 'continue_with' | 'signin_with' | 'signup_with'

type GoogleAuthButtonProps = {
  className?: string
  disabled?: boolean
  disabledMessage?: string
  isLoading?: boolean
  locale?: 'en' | 'vi'
  onCredential: (credential: string) => Promise<void> | void
  text?: GoogleAuthButtonText
}

export function GoogleAuthButton({
  className,
  disabled = false,
  disabledMessage,
  isLoading = false,
  locale = 'vi',
  onCredential,
  text = 'continue_with',
}: GoogleAuthButtonProps) {
  const { t } = useLanguage()
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID?.trim()
  const containerRef = useRef<HTMLDivElement | null>(null)
  const onCredentialRef = useRef(onCredential)
  const [isReady, setIsReady] = useState(false)
  const [loadError, setLoadError] = useState(false)

  useEffect(() => {
    onCredentialRef.current = onCredential
  }, [onCredential])

  useEffect(() => {
    if (!clientId) {
      setIsReady(false)
      setLoadError(false)
      return
    }

    const resolvedClientId = clientId
    let isMounted = true

    async function initializeGoogleButton() {
      try {
        await loadGoogleScript()

        if (!isMounted) {
          return
        }

        const googleId = window.google?.accounts.id
        const container = containerRef.current

        if (!googleId || !container) {
          throw new Error('Google Identity Services is unavailable')
        }

        container.replaceChildren()

        activeCredentialHandler = (credential: string) => {
          void onCredentialRef.current(credential)
        }

        if (initializedClientId !== resolvedClientId) {
          googleId.initialize({
          callback: (response) => {
            const credential = response.credential?.trim()

            if (!credential) {
              toast.error(t('auth.google.loadError'))
              return
            }

            activeCredentialHandler?.(credential)
          },
          client_id: resolvedClientId,
          })
          initializedClientId = resolvedClientId
        }

        googleId.renderButton(container, {
          locale,
          logo_alignment: 'left',
          shape: 'pill',
          size: 'large',
          text,
          theme: 'outline',
          width: Math.min(
            Math.floor(container.parentElement?.clientWidth ?? 360),
            360,
          ),
        })

        setIsReady(true)
        setLoadError(false)
      } catch {
        if (!isMounted) {
          return
        }

        setIsReady(false)
        setLoadError(true)
      }
    }

    void initializeGoogleButton()

    return () => {
      isMounted = false
    }
  }, [clientId, locale, t, text])

  if (!clientId) {
    return (
      <div
        className={cn(
          'rounded-2xl border border-dashed border-border/80 bg-muted/35 px-4 py-3 text-center text-xs leading-5 text-muted-foreground',
          className,
        )}
      >
        {t('auth.google.unavailable')}
      </div>
    )
  }

  if (loadError) {
    return (
      <div
        className={cn(
          'rounded-2xl border border-dashed border-destructive/30 bg-destructive/5 px-4 py-3 text-center text-xs leading-5 text-muted-foreground',
          className,
        )}
      >
        {t('auth.google.loadError')}
      </div>
    )
  }

  return (
    <div className={cn('relative w-full', className)}>
      <div
        ref={containerRef}
        className={cn(
          'flex min-h-11 w-full items-center justify-center overflow-hidden rounded-full transition-opacity',
          !isReady && 'opacity-70',
          (disabled || isLoading) && 'opacity-60',
        )}
      />

      {!isReady ? (
        <div className="absolute inset-0 flex items-center justify-center rounded-full bg-background/45">
          <span className="h-4 w-4 animate-spin rounded-full border-2 border-primary border-t-transparent" />
        </div>
      ) : null}

      {disabled ? (
        <button
          type="button"
          aria-label={disabledMessage ?? t('auth.google.disabled')}
          onClick={() => {
            if (disabledMessage) {
              toast.error(disabledMessage)
            }
          }}
          className="absolute inset-0 h-full w-full cursor-not-allowed rounded-full bg-background/30 text-transparent hover:bg-background/30"
        >
          {t('auth.google.blocked')}
        </button>
      ) : null}

      {isLoading ? (
        <div className="absolute inset-0 flex items-center justify-center rounded-full bg-background/55">
          <span className="h-4 w-4 animate-spin rounded-full border-2 border-primary border-t-transparent" />
        </div>
      ) : null}
    </div>
  )
}

function loadGoogleScript() {
  if (window.google?.accounts.id) {
    return Promise.resolve()
  }

  if (googleScriptPromise) {
    return googleScriptPromise
  }

  googleScriptPromise = new Promise<void>((resolve, reject) => {
    const existingScript = document.querySelector<HTMLScriptElement>(
      `script[src="${GOOGLE_SCRIPT_SRC}"]`,
    )

    if (existingScript) {
      existingScript.addEventListener('load', () => resolve(), { once: true })
      existingScript.addEventListener(
        'error',
        () => {
          googleScriptPromise = null
          reject()
        },
        { once: true },
      )
      return
    }

    const script = document.createElement('script')
    script.src = GOOGLE_SCRIPT_SRC
    script.async = true
    script.defer = true
    script.onload = () => resolve()
    script.onerror = () => {
      googleScriptPromise = null
      reject()
    }

    document.head.appendChild(script)
  })

  return googleScriptPromise
}
