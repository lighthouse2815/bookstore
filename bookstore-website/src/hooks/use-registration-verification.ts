import { useEffect, useState, type ChangeEvent } from 'react'
import { toast } from 'sonner'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import { OTP_LENGTH, sanitizeOtpCode } from '@/utils/auth-flow'

export type RegisterStep = 'register' | 'verify'

type SubmitRegistrationVerificationOptions = {
  onSuccess?: () => void
}

type RegistrationVerificationCopy = {
  requestOtpErrorFallback: string
  resendOtpLabel: string
}

export function useRegistrationVerification() {
  const location = useLocation()
  const navigate = useNavigate()
  const { requestRegistrationOtp, verifyRegistrationOtp } = useAuth()
  const { t } = useLanguage()
  const [currentStep, setCurrentStep] = useState<RegisterStep>('register')
  const [verificationEmail, setVerificationEmail] = useState('')
  const [otpCode, setOtpCode] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isRequestingOtp, setIsRequestingOtp] = useState(false)
  const copy: RegistrationVerificationCopy = {
    resendOtpLabel: t('auth.register.verification.resendOtpLabel'),
    requestOtpErrorFallback: t(
      'auth.register.verification.requestOtpErrorFallback',
    ),
  }

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search)
    const step = searchParams.get('step')
    const email = searchParams.get('email')?.trim() ?? ''

    if (step === 'verify' && email !== '') {
      setCurrentStep('verify')
      setVerificationEmail(email)
      setOtpCode('')
      return
    }

    setCurrentStep('register')
    setVerificationEmail('')
    setOtpCode('')
  }, [location.search])

  function handleOtpChange(event: ChangeEvent<HTMLInputElement>) {
    setOtpCode(sanitizeOtpCode(event.target.value, OTP_LENGTH))
  }

  function openVerifyStep(email: string) {
    const nextEmail = email.trim()
    setCurrentStep('verify')
    setVerificationEmail(nextEmail)
    setOtpCode('')
    navigate(`/register?step=verify&email=${encodeURIComponent(nextEmail)}`, {
      replace: true,
    })
  }

  function goBackToRegister() {
    setCurrentStep('register')
    setVerificationEmail('')
    setOtpCode('')
    navigate('/register', { replace: true })
  }

  async function resendOtp() {
    const email = verificationEmail.trim()

    if (!email) {
      toast.error(copy.requestOtpErrorFallback)
      return
    }

    setIsRequestingOtp(true)

    try {
      await requestRegistrationOtp({ email })
      toast.success(t('auth.register.otpSent'))
    } catch (error) {
      toast.error(
        error instanceof Error ? error.message : copy.requestOtpErrorFallback,
      )
    } finally {
      setIsRequestingOtp(false)
    }
  }

  async function submit({
    onSuccess,
  }: SubmitRegistrationVerificationOptions = {}) {
    const email = verificationEmail.trim()

    if (!email) {
      toast.error(t('auth.register.verifyErrorFallback'))
      return
    }

    if (!new RegExp(`^\\d{${OTP_LENGTH}}$`).test(otpCode)) {
      toast.error(t('auth.register.otpInvalid'))
      return
    }

    setIsLoading(true)

    try {
      await verifyRegistrationOtp({
        email,
        otpCode,
      })
      toast.success(t('auth.register.success'))
      onSuccess?.()
      navigate(`/login?username=${encodeURIComponent(email)}`, {
        replace: true,
      })
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : t('auth.register.verifyErrorFallback'),
      )
    } finally {
      setIsLoading(false)
    }
  }

  return {
    currentStep,
    verificationEmail,
    otpCode,
    copy,
    isLoading,
    isRequestingOtp,
    handleOtpChange,
    openVerifyStep,
    goBackToRegister,
    resendOtp,
    submit,
  }
}
