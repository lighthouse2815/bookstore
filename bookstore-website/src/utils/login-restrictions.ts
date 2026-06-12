import type { AppLanguage } from '@/locales/messages'

export type LoginRestrictionKind = 'locked' | 'inactive'

type LoginRestrictionCopy = {
  title: string
  description: string
}

const LOGIN_RESTRICTION_PREFIX: Record<LoginRestrictionKind, string> = {
  locked: '[ACCOUNT_LOCKED]',
  inactive: '[ACCOUNT_INACTIVE]',
}

const LOGIN_RESTRICTION_PATTERNS: Record<LoginRestrictionKind, RegExp[]> = {
  locked: [/\blocked\b/, /\blocked\b/, /\block\b/, /\bkhoa\b/],
  inactive: [
    /\binactive\b/,
    /\bnot active\b/,
    /\bnot activated\b/,
    /\bnot verified\b/,
    /\bunverified\b/,
    /\bdeactivated\b/,
    /\bchua kich hoat\b/,
    /\bkhong hoat dong\b/,
    /\bchua xac thuc\b/,
  ],
}

function normalizeText(value: string) {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
}

export function createLoginRestrictionMessage(
  kind: LoginRestrictionKind,
  message: string,
) {
  return `${LOGIN_RESTRICTION_PREFIX[kind]} ${message}`.trim()
}

export function parseLoginRestrictionMessage(message: string) {
  const trimmedMessage = message.trim()

  for (const [kind, prefix] of Object.entries(LOGIN_RESTRICTION_PREFIX) as Array<
    [LoginRestrictionKind, string]
  >) {
    if (trimmedMessage.startsWith(prefix)) {
      return { kind }
    }
  }

  const normalizedMessage = normalizeText(trimmedMessage)

  for (const [kind, patterns] of Object.entries(
    LOGIN_RESTRICTION_PATTERNS,
  ) as Array<[LoginRestrictionKind, RegExp[]]>) {
    if (patterns.some((pattern) => pattern.test(normalizedMessage))) {
      return { kind }
    }
  }

  return null
}

export function getLoginRestrictionCopy(
  kind: LoginRestrictionKind,
  language: AppLanguage,
): LoginRestrictionCopy {
  if (language === 'vi') {
    if (kind === 'locked') {
      return {
        title: 'Tài khoản đã bị khóa',
        description:
          'Tài khoản này hiện không thể đăng nhập. Vui lòng liên hệ quản trị viên để được mở khóa.',
      }
    }

    return {
      title: 'Tài khoản chưa kích hoạt',
      description:
        'Tài khoản này cần xác thực OTP trước khi đăng nhập. Hãy hoàn tất bước kích hoạt rồi hệ thống sẽ đăng nhập lại cho bạn.',
    }
  }

  if (kind === 'locked') {
    return {
      title: 'Account locked',
      description:
        'This account cannot sign in right now. Contact an administrator to unlock it.',
    }
  }

  return {
    title: 'Account not activated',
    description:
      'This account must be verified with an OTP before it can sign in. Complete the activation step and the app will sign you in again.',
  }
}
