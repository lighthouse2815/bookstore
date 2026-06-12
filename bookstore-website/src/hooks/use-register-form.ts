import { useState, type ChangeEvent } from 'react'
import { toast } from 'sonner'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import type { RegisterRequest } from '@/types/auth'
import { getPasswordValidationError } from '@/utils/auth-flow'

export type RegisterFormState = RegisterRequest & {
  confirmPassword: string
}

type SubmitRegisterFormOptions = {
  hasAcceptedTerms: boolean
  requiredTermsMessage: string
  onSuccess?: (email: string) => void
}

type SubmitGoogleRegisterFormOptions = {
  hasAcceptedTerms: boolean
  idToken: string
  requiredTermsMessage: string
  onSuccess?: () => void
}

const initialFormData: RegisterFormState = {
  email: '',
  password: '',
  confirmPassword: '',
}

export function useRegisterForm() {
  const { loginWithGoogle, register } = useAuth()
  const { t } = useLanguage()
  const [formData, setFormData] = useState(initialFormData)
  const [isLoading, setIsLoading] = useState(false)
  const [isGoogleLoading, setIsGoogleLoading] = useState(false)

  const passwordStrength = getPasswordStrength(formData.password)
  const passwordStrengthLabel = getPasswordStrengthLabel(passwordStrength, t)
  const passwordsMatch = formData.password === formData.confirmPassword

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.target
    setFormData((previousValue) => ({ ...previousValue, [name]: value }))
  }

  async function submit({
    hasAcceptedTerms,
    requiredTermsMessage,
    onSuccess,
  }: SubmitRegisterFormOptions) {
    const passwordValidationError = getPasswordValidationError(
      formData.password,
      formData.confirmPassword,
      t,
    )

    if (passwordValidationError) {
      toast.error(passwordValidationError)
      return
    }

    if (!hasAcceptedTerms) {
      toast.error(requiredTermsMessage)
      return
    }

    setIsLoading(true)

    try {
      const email = formData.email.trim()

      await register({
        email,
        password: formData.password,
      })
      toast.success(t('auth.register.otpSent'))
      onSuccess?.(email)
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : t('auth.register.errorFallback'),
      )
    } finally {
      setIsLoading(false)
    }
  }

  async function submitWithGoogle({
    hasAcceptedTerms,
    idToken,
    requiredTermsMessage,
    onSuccess,
  }: SubmitGoogleRegisterFormOptions) {
    if (!hasAcceptedTerms) {
      toast.error(requiredTermsMessage)
      return
    }

    setIsGoogleLoading(true)

    try {
      await loginWithGoogle(idToken)
      toast.success(t('auth.login.success'))
      onSuccess?.()
    } catch (error) {
      toast.error(
        error instanceof Error ? error.message : t('auth.login.errorFallback'),
      )
    } finally {
      setIsGoogleLoading(false)
    }
  }

  function resetForm() {
    setFormData(initialFormData)
  }

  function setEmail(email: string) {
    setFormData((previousValue) =>
      previousValue.email === email
        ? previousValue
        : { ...previousValue, email },
    )
  }

  return {
    formData,
    isGoogleLoading,
    isLoading,
    passwordStrength,
    passwordStrengthLabel,
    passwordsMatch,
    handleChange,
    submit,
    submitWithGoogle,
    resetForm,
    setEmail,
  }
}

function getPasswordStrength(password: string) {
  let strength = 0

  if (password.length >= 8) {
    strength += 1
  }

  if (/[A-Z]/.test(password)) {
    strength += 1
  }

  if (/[0-9]/.test(password)) {
    strength += 1
  }

  if (/[^A-Za-z0-9]/.test(password)) {
    strength += 1
  }

  return strength
}

function getPasswordStrengthLabel(
  passwordStrength: number,
  t: (key: string, variables?: Record<string, string | number>) => string,
) {
  if (passwordStrength <= 1) {
    return t('auth.register.passwordWeak')
  }

  if (passwordStrength === 2) {
    return t('auth.register.passwordMedium')
  }

  if (passwordStrength === 3) {
    return t('auth.register.passwordStrong')
  }

  return t('auth.register.passwordVeryStrong')
}
