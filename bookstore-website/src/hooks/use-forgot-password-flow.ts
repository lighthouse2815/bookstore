import { useState, type ChangeEvent } from 'react'
import { KeyRound, Mail, ShieldCheck } from 'lucide-react'
import { toast } from 'sonner'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import {
  getPasswordValidationError,
  OTP_LENGTH,
  sanitizeOtpCode,
} from '@/utils/auth-flow'

export type ForgotPasswordStep = 'request' | 'verify' | 'reset'

export function useForgotPasswordFlow() {
  const navigate = useNavigate()
  const { requestPasswordResetOtp, verifyPasswordResetOtp, resetPassword } =
    useAuth()
  const { t } = useLanguage()
  const [currentStep, setCurrentStep] = useState<ForgotPasswordStep>('request')
  const [email, setEmail] = useState('')
  const [otpCode, setOtpCode] = useState('')
  const [resetToken, setResetToken] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [showNewPassword, setShowNewPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [isRequestLoading, setIsRequestLoading] = useState(false)
  const [isVerifyLoading, setIsVerifyLoading] = useState(false)
  const [isResetLoading, setIsResetLoading] = useState(false)

  const pageTitle =
    currentStep === 'request'
      ? t('auth.forgotPassword.requestTitle')
      : currentStep === 'verify'
        ? t('auth.forgotPassword.verifyTitle')
        : t('auth.forgotPassword.resetTitle')

  const pageDescription =
    currentStep === 'request'
      ? t('auth.forgotPassword.requestDescription')
      : currentStep === 'verify'
        ? t('auth.forgotPassword.verifyDescription', { email })
        : t('auth.forgotPassword.resetDescription', { email })

  const noticeTitle =
    currentStep === 'request'
      ? t('auth.forgotPassword.requestTitle')
      : currentStep === 'verify'
        ? t('auth.forgotPassword.otpSent')
        : t('auth.forgotPassword.resetTitle')

  const noticeText =
    currentStep === 'request'
      ? t('auth.forgotPassword.requestHint')
      : currentStep === 'verify'
        ? t('auth.forgotPassword.verifyHint')
        : t('auth.forgotPassword.resetHint')

  const NoticeIcon =
    currentStep === 'request'
      ? Mail
      : currentStep === 'verify'
        ? ShieldCheck
        : KeyRound

  function handleEmailChange(event: ChangeEvent<HTMLInputElement>) {
    setEmail(event.target.value)
  }

  function handleOtpChange(event: ChangeEvent<HTMLInputElement>) {
    setOtpCode(sanitizeOtpCode(event.target.value, OTP_LENGTH))
  }

  function handleNewPasswordChange(event: ChangeEvent<HTMLInputElement>) {
    setNewPassword(event.target.value)
  }

  function handleConfirmPasswordChange(event: ChangeEvent<HTMLInputElement>) {
    setConfirmPassword(event.target.value)
  }

  function toggleNewPasswordVisibility() {
    setShowNewPassword((currentValue) => !currentValue)
  }

  function toggleConfirmPasswordVisibility() {
    setShowConfirmPassword((currentValue) => !currentValue)
  }

  function goBackToRequest() {
    setCurrentStep('request')
    setOtpCode('')
    setResetToken('')
    setNewPassword('')
    setConfirmPassword('')
    setShowNewPassword(false)
    setShowConfirmPassword(false)
  }

  function goBackToVerify() {
    setCurrentStep('verify')
    setResetToken('')
    setNewPassword('')
    setConfirmPassword('')
    setShowNewPassword(false)
    setShowConfirmPassword(false)
  }

  async function submitRequest() {
    const nextEmail = email.trim()
    setIsRequestLoading(true)

    try {
      await requestPasswordResetOtp({ email: nextEmail })
      setEmail(nextEmail)
      setOtpCode('')
      setResetToken('')
      toast.success(t('auth.forgotPassword.requestSuccess'))
      setCurrentStep('verify')
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : t('auth.forgotPassword.requestErrorFallback'),
      )
    } finally {
      setIsRequestLoading(false)
    }
  }

  async function submitVerification() {
    const nextEmail = email.trim()

    if (!new RegExp(`^\\d{${OTP_LENGTH}}$`).test(otpCode)) {
      toast.error(t('auth.forgotPassword.otpInvalid'))
      return
    }

    setIsVerifyLoading(true)

    try {
      const result = await verifyPasswordResetOtp({
        email: nextEmail,
        otpCode,
      })
      setResetToken(result.resetToken)
      toast.success(t('auth.forgotPassword.verifySuccess'))
      setCurrentStep('reset')
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : t('auth.forgotPassword.verifyErrorFallback'),
      )
    } finally {
      setIsVerifyLoading(false)
    }
  }

  async function submitReset() {
    const passwordValidationError = getPasswordValidationError(
      newPassword,
      confirmPassword,
      t,
    )

    if (passwordValidationError) {
      toast.error(passwordValidationError)
      return
    }

    if (resetToken.trim() === '') {
      toast.error(t('auth.forgotPassword.resetErrorFallback'))
      return
    }

    setIsResetLoading(true)

    try {
      await resetPassword({
        resetToken,
        newPassword,
      })
      toast.success(t('auth.forgotPassword.resetSuccess'))
      navigate(`/login?username=${encodeURIComponent(email.trim())}`, {
        replace: true,
      })
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : t('auth.forgotPassword.resetErrorFallback'),
      )
    } finally {
      setIsResetLoading(false)
    }
  }

  return {
    currentStep,
    email,
    otpCode,
    newPassword,
    confirmPassword,
    showNewPassword,
    showConfirmPassword,
    isRequestLoading,
    isVerifyLoading,
    isResetLoading,
    pageTitle,
    pageDescription,
    noticeTitle,
    noticeText,
    NoticeIcon,
    handleEmailChange,
    handleOtpChange,
    handleNewPasswordChange,
    handleConfirmPasswordChange,
    toggleNewPasswordVisibility,
    toggleConfirmPasswordVisibility,
    goBackToRequest,
    goBackToVerify,
    submitRequest,
    submitVerification,
    submitReset,
  }
}
