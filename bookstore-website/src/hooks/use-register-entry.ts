import { type FormEvent } from 'react'
import { useRegisterForm } from '@/hooks/use-register-form'
import { useRegisterTermsConsent } from '@/hooks/use-register-terms-consent'

type RegisterEntrySuccessContext = {
  email: string
  reset: () => void
}

type RegisterEntryGoogleSuccessContext = {
  reset: () => void
}

type UseRegisterEntryOptions = {
  onSuccess?: (context: RegisterEntrySuccessContext) => void
  onGoogleSuccess?: (context: RegisterEntryGoogleSuccessContext) => void
}

export function useRegisterEntry(options: UseRegisterEntryOptions = {}) {
  const registerForm = useRegisterForm()
  const termsConsent = useRegisterTermsConsent()

  function reset() {
    registerForm.resetForm()
    termsConsent.resetTermsAcceptance()
  }

  async function handleRegisterSubmit(event: FormEvent) {
    event.preventDefault()

    await registerForm.submit({
      hasAcceptedTerms: termsConsent.hasAcceptedTerms,
      requiredTermsMessage: termsConsent.termsCopy.requiredMessage,
      onSuccess: (email) => {
        options.onSuccess?.({
          email,
          reset,
        })
      },
    })
  }

  async function handleGoogleRegister(idToken: string) {
    await registerForm.submitWithGoogle({
      hasAcceptedTerms: termsConsent.hasAcceptedTerms,
      idToken,
      requiredTermsMessage: termsConsent.termsCopy.requiredMessage,
      onSuccess: () => {
        options.onGoogleSuccess?.({
          reset,
        })
      },
    })
  }

  return {
    handleGoogleRegister,
    registerForm,
    termsConsent,
    handleRegisterSubmit,
    reset,
  }
}
