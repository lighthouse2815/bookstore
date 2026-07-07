import { useEffect, useRef, useState, type UIEvent } from 'react'
import { useLanguage } from '@/contexts/language-context'
import type { RegisterTermsCopy } from '@/utils/register-terms'

const CLOSE_UNLOCK_OFFSET = 24

export function useRegisterTermsDialog(open: boolean) {
  const { getMessage, t } = useLanguage()
  const scrollRef = useRef<HTMLDivElement | null>(null)
  const [hasReachedBottom, setHasReachedBottom] = useState(false)
  const termsCopy = getMessage<RegisterTermsCopy>('auth.register.terms')

  if (!termsCopy) {
    throw new Error('Missing register terms copy')
  }

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
      updateScrollState(scrollRef.current, setHasReachedBottom)
    })

    return () => {
      document.body.style.overflow = previousOverflow
    }
  }, [open])

  function handleScroll(event: UIEvent<HTMLDivElement>) {
    updateScrollState(event.currentTarget, setHasReachedBottom)
  }

  return {
    t,
    scrollRef,
    hasReachedBottom,
    termsCopy,
    handleScroll,
  }
}

function updateScrollState(
  element: HTMLDivElement,
  setHasReachedBottom: (value: boolean) => void,
) {
  const reachedBottom =
    element.scrollTop + element.clientHeight >=
    element.scrollHeight - CLOSE_UNLOCK_OFFSET

  if (reachedBottom) {
    setHasReachedBottom(true)
  }
}
