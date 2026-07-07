import { useState, type ChangeEvent, type MouseEvent } from 'react'
import { useLanguage } from '@/contexts/language-context'
import type { RegisterTermsCopy } from '@/utils/register-terms'

export function useRegisterTermsConsent() {
  const { getMessage } = useLanguage()
  const [hasAcceptedTerms, setHasAcceptedTerms] = useState(false)
  const [hasReadTermsDialog, setHasReadTermsDialog] = useState(false)
  const [isTermsOpen, setIsTermsOpen] = useState(false)
  const [shouldAcceptTermsOnClose, setShouldAcceptTermsOnClose] =
    useState(false)

  const termsCopy = getMessage<RegisterTermsCopy>('auth.register.terms')

  if (!termsCopy) {
    throw new Error('Missing register terms copy')
  }

  function openTermsDialog(acceptTermsOnClose: boolean) {
    setShouldAcceptTermsOnClose(acceptTermsOnClose)
    setIsTermsOpen(true)
  }

  function handleTermsCheckboxChange(event: ChangeEvent<HTMLInputElement>) {
    if (event.currentTarget.checked) {
      if (hasReadTermsDialog) {
        setHasAcceptedTerms(true)
        return
      }

      setHasAcceptedTerms(false)
      openTermsDialog(true)
      return
    }

    setShouldAcceptTermsOnClose(false)
    setHasAcceptedTerms(false)
  }

  function handleTermsLinkClick(event: MouseEvent<HTMLButtonElement>) {
    event.preventDefault()
    event.stopPropagation()
    openTermsDialog(false)
  }

  function handleTermsDialogClose() {
    setIsTermsOpen(false)
    setHasReadTermsDialog(true)
    setShouldAcceptTermsOnClose(false)

    if (shouldAcceptTermsOnClose) {
      setHasAcceptedTerms(true)
    }
  }

  function resetTermsAcceptance() {
    setHasAcceptedTerms(false)
    setShouldAcceptTermsOnClose(false)
    setIsTermsOpen(false)
  }

  return {
    termsCopy,
    hasAcceptedTerms,
    isTermsOpen,
    handleTermsCheckboxChange,
    handleTermsLinkClick,
    handleTermsDialogClose,
    resetTermsAcceptance,
  }
}
