import type { CouponResponse } from '@/types/coupon'
import type {
  CouponGameCouponSummary,
  CouponGameResult,
  CouponGameStorageLike,
} from '@/types/coupon-game'

export const COUPON_GAME_STORAGE_KEY = 'bookstore.coupon-game.daily-result'

export function getCouponGameDateKey(date = new Date()) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

export function isCouponGameCandidate(
  coupon: CouponResponse,
  now = new Date(),
) {
  if (!coupon.active) {
    return false
  }

  const startsAt = parseTimestamp(coupon.startsAt)
  const expiresAt = parseTimestamp(coupon.expiresAt)
  if (startsAt === null || expiresAt === null) {
    return false
  }

  if (startsAt > now.getTime() || expiresAt <= now.getTime()) {
    return false
  }

  if (
    coupon.maxUsageCount !== null &&
    Number.isFinite(coupon.maxUsageCount) &&
    coupon.usedCount >= coupon.maxUsageCount
  ) {
    return false
  }

  return true
}

export function filterCouponGameCandidates(
  coupons: CouponResponse[],
  now = new Date(),
) {
  return coupons.filter((coupon) => isCouponGameCandidate(coupon, now))
}

export function getCouponGameWeight(coupon: CouponResponse) {
  const discountStrength =
    coupon.discountType === 'PERCENTAGE'
      ? coupon.discountValue
      : coupon.discountValue / 10_000
  const maxDiscountBonus =
    coupon.maxDiscountAmount === null
      ? 1.5
      : Math.min(coupon.maxDiscountAmount / 50_000, 3)
  const minOrderPenalty = Math.min(coupon.minOrderAmount / 300_000, 2)

  return Math.max(1, discountStrength + maxDiscountBonus - minOrderPenalty)
}

export function selectCouponGameWinner(
  coupons: CouponResponse[],
  options?: {
    now?: Date
    random?: () => number
  },
) {
  const candidates = filterCouponGameCandidates(coupons, options?.now)
  if (candidates.length === 0) {
    return null
  }

  const totalWeight = candidates.reduce(
    (sum, coupon) => sum + getCouponGameWeight(coupon),
    0,
  )
  if (totalWeight <= 0) {
    return candidates[0] ?? null
  }

  const randomValue = (options?.random ?? Math.random)() * totalWeight
  let cumulativeWeight = 0

  for (const coupon of candidates) {
    cumulativeWeight += getCouponGameWeight(coupon)
    if (randomValue < cumulativeWeight) {
      return coupon
    }
  }

  return candidates.at(-1) ?? null
}

export function createCouponGameResult(
  coupon: CouponResponse,
  playedAt = new Date(),
): CouponGameResult {
  return {
    date: getCouponGameDateKey(playedAt),
    playedAt: playedAt.toISOString(),
    couponCode: coupon.code,
    couponSummary: {
      code: coupon.code,
      description: coupon.description,
      couponType: coupon.couponType,
      discountType: coupon.discountType,
      discountValue: coupon.discountValue,
      minOrderAmount: coupon.minOrderAmount,
      maxDiscountAmount: coupon.maxDiscountAmount,
      expiresAt: coupon.expiresAt,
    },
  }
}

export function parseStoredCouponGameResult(
  rawValue: string | null,
  now = new Date(),
) {
  if (!rawValue) {
    return null
  }

  try {
    const parsedValue = JSON.parse(rawValue)
    if (!isRecord(parsedValue)) {
      return null
    }

    if (
      typeof parsedValue.date !== 'string' ||
      parsedValue.date !== getCouponGameDateKey(now)
    ) {
      return null
    }

    if (
      typeof parsedValue.playedAt !== 'string' ||
      typeof parsedValue.couponCode !== 'string' ||
      !isCouponGameCouponSummary(parsedValue.couponSummary)
    ) {
      return null
    }

    return {
      date: parsedValue.date,
      playedAt: parsedValue.playedAt,
      couponCode: parsedValue.couponCode,
      couponSummary: parsedValue.couponSummary,
    } satisfies CouponGameResult
  } catch {
    return null
  }
}

export function loadCouponGameResult(
  storage: CouponGameStorageLike,
  now = new Date(),
  storageKey = COUPON_GAME_STORAGE_KEY,
) {
  const rawValue = storage.getItem(storageKey)
  const parsedResult = parseStoredCouponGameResult(rawValue, now)

  if (!parsedResult && rawValue) {
    try {
      storage.removeItem(storageKey)
    } catch {
      // Ignore storage cleanup failures and keep the page usable.
    }
  }

  return parsedResult
}

export function saveCouponGameResult(
  storage: CouponGameStorageLike,
  result: CouponGameResult,
  storageKey = COUPON_GAME_STORAGE_KEY,
) {
  storage.setItem(storageKey, JSON.stringify(result))
}

function parseTimestamp(value: string | null | undefined) {
  if (!value) {
    return null
  }

  const timestamp = Date.parse(value)
  return Number.isNaN(timestamp) ? null : timestamp
}

function isCouponGameCouponSummary(
  value: unknown,
): value is CouponGameCouponSummary {
  if (!isRecord(value)) {
    return false
  }

  const discountType =
    value.discountType === 'PERCENTAGE' || value.discountType === 'FIXED_AMOUNT'
  const couponType = value.couponType === 'BOOK' || value.couponType === 'SHIPPING'

  return (
    typeof value.code === 'string' &&
    (typeof value.description === 'string' || value.description === null) &&
    discountType &&
    couponType &&
    typeof value.discountValue === 'number' &&
    typeof value.minOrderAmount === 'number' &&
    (typeof value.maxDiscountAmount === 'number' ||
      value.maxDiscountAmount === null) &&
    (typeof value.expiresAt === 'string' || value.expiresAt === null)
  )
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === 'object' && value !== null
}
