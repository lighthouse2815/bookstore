import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useBrandWordmark } from '@/hooks/use-brand-wordmark'
import { useLoginForm } from '@/hooks/use-login-form'
import { useRegisterEntry } from '@/hooks/use-register-entry'
import { useLanguage } from '@/contexts/language-context'

export function useLoginPage() {
  const navigate = useNavigate()
  const { language, t } = useLanguage()
  const loginForm = useLoginForm()
  const { brandPrefix, brandSuffix } = useBrandWordmark()
  const [isRegisterFace, setIsRegisterFace] = useState(false)
  const registerEntry = useRegisterEntry({
    onSuccess: ({ email, reset }) => {
      reset()
      setIsRegisterFace(false)
      navigate(`/register?step=verify&email=${encodeURIComponent(email)}`)
    },
    onGoogleSuccess: ({ reset }) => {
      reset()
      setIsRegisterFace(false)
      navigate('/')
    },
  })

  useEffect(() => {
    if (loginForm.prefilledUsername) {
      setIsRegisterFace(false)
    }
  }, [loginForm.prefilledUsername])

  async function handleLoginSubmit(event: FormEvent) {
    event.preventDefault()
    await loginForm.submit()
  }

  async function handleActivationSubmit(event: FormEvent) {
    event.preventDefault()
    await loginForm.submitActivationOtp()
  }

  function handleRegisterFaceChange(checked: boolean) {
    if (checked) {
      loginForm.clearLoginRestriction()
    }

    setIsRegisterFace(checked)
  }

  return {
    language,
    t,
    loginForm,
    registerForm: registerEntry.registerForm,
    termsConsent: registerEntry.termsConsent,
    brandPrefix,
    brandSuffix,
    isRegisterFace,
    handleRegisterFaceChange,
    handleActivationSubmit,
    handleGoogleRegister: registerEntry.handleGoogleRegister,
    handleLoginSubmit,
    handleRegisterSubmit: registerEntry.handleRegisterSubmit,
  }
}
