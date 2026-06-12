import { useEffect, useState, type FormEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { useBrandWordmark } from '@/hooks/use-brand-wordmark'
import { useLanguage } from '@/contexts/language-context'
import { useRegisterEntry } from '@/hooks/use-register-entry'
import { useRegistrationVerification } from '@/hooks/use-registration-verification'

export function useRegisterPage() {
  const navigate = useNavigate()
  const { language, t } = useLanguage()
  const verification = useRegistrationVerification()
  const registerEntry = useRegisterEntry({
    onSuccess: ({ email }) => {
      verification.openVerifyStep(email)
    },
    onGoogleSuccess: ({ reset }) => {
      reset()
      navigate('/')
    },
  })
  const { registerForm, termsConsent } = registerEntry
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const { brandPrefix, brandSuffix } = useBrandWordmark()

  useEffect(() => {
    if (verification.verificationEmail) {
      registerForm.setEmail(verification.verificationEmail)
    }
  }, [verification.verificationEmail])

  async function handleVerifySubmit(event: FormEvent) {
    event.preventDefault()
    await verification.submit({
      onSuccess: () => {
        registerEntry.reset()
      },
    })
  }

  function togglePasswordVisibility() {
    setShowPassword((current) => !current)
  }

  function toggleConfirmPasswordVisibility() {
    setShowConfirmPassword((current) => !current)
  }

  const pageTitle =
    verification.currentStep === 'verify'
      ? t('auth.register.verifyTitle')
      : t('auth.register.title')

  const pageDescription =
    verification.currentStep === 'verify'
      ? t('auth.register.verifyDescription', {
          email: verification.verificationEmail || registerForm.formData.email,
        })
      : t('auth.register.description')

  return {
    language,
    t,
    registerForm,
    termsConsent,
    verification,
    brandPrefix,
    brandSuffix,
    showPassword,
    showConfirmPassword,
    pageTitle,
    pageDescription,
    handleGoogleRegister: registerEntry.handleGoogleRegister,
    handleSubmit: registerEntry.handleRegisterSubmit,
    handleVerifySubmit,
    togglePasswordVisibility,
    toggleConfirmPasswordVisibility,
  }
}
