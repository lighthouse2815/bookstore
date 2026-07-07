export type LoginRestrictionKind = 'locked' | 'inactive'

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
