export const OTP_LENGTH = 6
const MIN_PASSWORD_LENGTH = 8

export function sanitizeOtpCode(value: string, maxLength = OTP_LENGTH) {
  return value.replace(/\D/g, '').slice(0, maxLength)
}

export function getPasswordValidationError(
  password: string,
  confirmation: string,
  t: (key: string, variables?: Record<string, number | string>) => string,
) {
  if (password !== confirmation) {
    return t('auth.register.passwordMismatch')
  }

  if (password.length < MIN_PASSWORD_LENGTH) {
    return t('auth.register.passwordTooShort')
  }

  return null
}
