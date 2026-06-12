import { useEffect, useState, type ChangeEvent } from 'react'
import { toast } from 'sonner'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/auth-context'
import { useLanguage } from '@/contexts/language-context'
import { OTP_LENGTH, sanitizeOtpCode } from '@/utils/auth-flow'
import {
  getLoginRestrictionCopy,
  parseLoginRestrictionMessage,
} from '@/utils/login-restrictions'

type LoginFormState = {
  username: string
  password: string
}

type LoginRestrictionState =
  | { kind: 'locked' }
  | { kind: 'inactive'; email: string }

type LoginFlowCopy = {
  inactiveActionLabel: string
  inactiveBackLabel: string
  inactiveEmailRequiredMessage: string
  inactiveOtpLead: string
  inactiveOtpReadyHint: string
  inactiveRequestErrorFallback: string
  inactiveVerifyLabel: string
  lockedActionLabel: string
}

const initialFormData: LoginFormState = {
  username: '',
  password: '',
}

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function useLoginForm() {
  const location = useLocation()
  const navigate = useNavigate()
  const { login, requestRegistrationOtp, verifyRegistrationOtp } = useAuth()
  const { language, t } = useLanguage()
  const [formData, setFormData] = useState(initialFormData)
  const [isLoading, setIsLoading] = useState(false)
  const [prefilledUsername, setPrefilledUsername] = useState('')
  const [loginRestriction, setLoginRestriction] =
    useState<LoginRestrictionState | null>(null)
  const [activationOtpCode, setActivationOtpCode] = useState('')
  const [isActivationRequestLoading, setIsActivationRequestLoading] =
    useState(false)
  const [isActivationLoading, setIsActivationLoading] = useState(false)
  const [pendingActivationCredentials, setPendingActivationCredentials] =
    useState<LoginFormState | null>(null)

  const restrictionCopy = loginRestriction
    ? getLoginRestrictionCopy(loginRestriction.kind, language)
    : null
  const flowCopy = getLoginFlowCopy(language)

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search)
    const username = searchParams.get('username')?.trim() ?? ''

    setPrefilledUsername(username)

    if (!username) {
      return
    }

    setFormData((currentFormData) => ({
      ...currentFormData,
      username,
      password: '',
    }))
  }, [location.search])

  function resetActivationState() {
    setActivationOtpCode('')
    setIsActivationRequestLoading(false)
    setPendingActivationCredentials(null)
  }

  function clearLoginRestriction() {
    setLoginRestriction(null)
    resetActivationState()
  }

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.target
    clearLoginRestriction()
    setFormData((previousValue) => ({ ...previousValue, [name]: value }))
  }

  function handleActivationOtpChange(event: ChangeEvent<HTMLInputElement>) {
    setActivationOtpCode(sanitizeOtpCode(event.target.value, OTP_LENGTH))
  }

  function openLockedState() {
    setLoginRestriction({ kind: 'locked' })
    resetActivationState()
  }

  function openInactiveState(credentials: LoginFormState) {
    const email = credentials.username.trim()

    if (!EMAIL_PATTERN.test(email)) {
      toast.error(flowCopy.inactiveEmailRequiredMessage)
      return true
    }

    setLoginRestriction({
      kind: 'inactive',
      email,
    })
    setActivationOtpCode('')
    setPendingActivationCredentials({
      username: email,
      password: credentials.password,
    })
    return true
  }

  function handleLoginFailure(
    errorMessage: string,
    credentials: LoginFormState,
  ) {
    const restriction = parseLoginRestrictionMessage(errorMessage)

    if (!restriction) {
      return false
    }

    if (restriction.kind === 'locked') {
      openLockedState()
      return true
    }

    return openInactiveState(credentials)
  }

  async function submit() {
    clearLoginRestriction()
    setIsLoading(true)

    try {
      await login(formData.username, formData.password)
      toast.success(t('auth.login.success'))
      navigate('/')
    } catch (error) {
      const errorMessage =
        error instanceof Error ? error.message : t('auth.login.errorFallback')

      if (!handleLoginFailure(errorMessage, formData)) {
        toast.error(errorMessage)
      }
    } finally {
      setIsLoading(false)
    }
  }

  async function requestActivationOtp() {
    if (!loginRestriction || loginRestriction.kind !== 'inactive') {
      return
    }

    setIsActivationRequestLoading(true)

    try {
      await requestRegistrationOtp({ email: loginRestriction.email })
      toast.success(t('auth.register.otpSent'))
    } catch (error) {
      toast.error(
        error instanceof Error
          ? error.message
          : flowCopy.inactiveRequestErrorFallback,
      )
    } finally {
      setIsActivationRequestLoading(false)
    }
  }

  async function submitActivationOtp() {
    if (
      !loginRestriction ||
      loginRestriction.kind !== 'inactive' ||
      !pendingActivationCredentials
    ) {
      return
    }

    if (!new RegExp(`^\\d{${OTP_LENGTH}}$`).test(activationOtpCode)) {
      toast.error(t('auth.register.otpInvalid'))
      return
    }

    const credentials = pendingActivationCredentials

    setIsActivationLoading(true)

    try {
      await verifyRegistrationOtp({
        email: loginRestriction.email,
        otpCode: activationOtpCode,
      })

      setLoginRestriction(null)
      setActivationOtpCode('')
      setPendingActivationCredentials(null)

      await login(credentials.username, credentials.password)
      toast.success(t('auth.login.success'))
      navigate('/')
    } catch (error) {
      const errorMessage =
        error instanceof Error
          ? error.message
          : t('auth.register.verifyErrorFallback')

      if (!handleLoginFailure(errorMessage, credentials)) {
        toast.error(errorMessage)
      }
    } finally {
      setIsActivationLoading(false)
    }
  }

  const activationEmail =
    loginRestriction?.kind === 'inactive' ? loginRestriction.email : ''

  return {
    activationEmail,
    activationOtpCode,
    flowCopy,
    formData,
    isActivationLoading,
    isActivationRequestLoading,
    isInactiveRestriction: loginRestriction?.kind === 'inactive',
    isLoading,
    isLockedRestriction: loginRestriction?.kind === 'locked',
    prefilledUsername,
    restrictionCopy,
    clearLoginRestriction,
    handleActivationOtpChange,
    handleChange,
    requestActivationOtp,
    submit,
    submitActivationOtp,
  }
}

function getLoginFlowCopy(language: 'vi' | 'en'): LoginFlowCopy {
  if (language === 'vi') {
    return {
      lockedActionLabel: 'Dùng tài khoản khác',
      inactiveActionLabel: 'Gửi lại OTP',
      inactiveOtpLead:
        'Nhập mã OTP kích hoạt gần nhất trong email của bạn. Nếu chưa nhận được hoặc mã đã hết hạn, bạn có thể gửi lại ngay từ đây.',
      inactiveOtpReadyHint:
        'Nhập đúng mã OTP 6 chữ số rồi hệ thống sẽ tự đăng nhập lại bằng tài khoản bạn vừa nhập.',
      inactiveBackLabel: 'Quay lại đăng nhập',
      inactiveVerifyLabel: 'Xác thực và đăng nhập',
      inactiveEmailRequiredMessage:
        'Tài khoản chưa kích hoạt cần đăng nhập bằng email để xác thực OTP.',
      inactiveRequestErrorFallback: 'Không thể gửi lại mã OTP kích hoạt',
    }
  }

  return {
    lockedActionLabel: 'Use another account',
    inactiveActionLabel: 'Resend OTP',
    inactiveOtpLead:
      'Enter the latest activation OTP from your email. If you did not receive one or it has expired, request a new code here.',
    inactiveOtpReadyHint:
      'Enter the correct 6-digit OTP and the app will sign you in again with the same account.',
    inactiveBackLabel: 'Back to login',
    inactiveVerifyLabel: 'Verify and sign in',
    inactiveEmailRequiredMessage:
      'Inactive accounts must sign in with an email address before OTP verification.',
    inactiveRequestErrorFallback: 'Unable to send a new activation OTP',
  }
}
