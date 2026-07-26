import { useEffect, useMemo, useRef, useState } from 'react'
import { getActiveCoupons } from '@/services/coupon-service'
import type { CouponResponse } from '@/types/coupon'
import type { CouponGameResult } from '@/types/coupon-game'
import { getErrorMessage } from '@/utils'
import {
  createCouponGameResult,
  filterCouponGameCandidates,
  loadCouponGameResult,
  saveCouponGameResult,
  selectCouponGameWinner,
} from '@/utils/coupon-game'

type UseCouponGameResult = {
  availableCoupons: CouponResponse[]
  result: CouponGameResult | null
  isLoading: boolean
  isSpinning: boolean
  playedToday: boolean
  spinRotation: number
  error: string | null
  play: () => void
}

const SPIN_DURATION_MS = 2200
const MIN_SPIN_ROTATION = 2_160
const EXTRA_SPIN_ROTATION = 720

export function useCouponGame(): UseCouponGameResult {
  const [allCoupons, setAllCoupons] = useState<CouponResponse[]>([])
  const [result, setResult] = useState<CouponGameResult | null>(
    readStoredCouponGameResult,
  )
  const [isLoading, setIsLoading] = useState(true)
  const [isSpinning, setIsSpinning] = useState(false)
  const [spinRotation, setSpinRotation] = useState(0)
  const [error, setError] = useState<string | null>(null)
  const spinTimeoutRef = useRef<number | null>(null)
  const availableCoupons = useMemo(
    () => filterCouponGameCandidates(allCoupons),
    [allCoupons],
  )
  const playedToday = result !== null

  useEffect(() => {
    let isMounted = true

    async function fetchCoupons() {
      try {
        const coupons = await getActiveCoupons()
        if (!isMounted) {
          return
        }

        setAllCoupons(coupons)
      } catch (nextError) {
        if (!isMounted) {
          return
        }

        setError(getErrorMessage(nextError))
      } finally {
        if (isMounted) {
          setIsLoading(false)
        }
      }
    }

    fetchCoupons()

    return () => {
      isMounted = false
      if (spinTimeoutRef.current !== null) {
        window.clearTimeout(spinTimeoutRef.current)
      }
    }
  }, [])

  function play() {
    if (playedToday || isSpinning || availableCoupons.length === 0) {
      return
    }

    const winningCoupon = selectCouponGameWinner(availableCoupons)
    if (!winningCoupon) {
      return
    }

    const playedAt = new Date()
    const nextResult = createCouponGameResult(winningCoupon, playedAt)

    setError(null)
    setIsSpinning(true)
    setSpinRotation((currentRotation) => {
      const extraRotation = Math.floor(Math.random() * EXTRA_SPIN_ROTATION)
      return currentRotation + MIN_SPIN_ROTATION + extraRotation
    })

    if (spinTimeoutRef.current !== null) {
      window.clearTimeout(spinTimeoutRef.current)
    }

    spinTimeoutRef.current = window.setTimeout(() => {
      setResult(nextResult)
      setIsSpinning(false)

      try {
        window.localStorage &&
          saveCouponGameResult(window.localStorage, nextResult)
      } catch {
        // Ignore storage failures and still show the result on screen.
      }
    }, SPIN_DURATION_MS)
  }

  return {
    availableCoupons,
    result,
    isLoading,
    isSpinning,
    playedToday,
    spinRotation,
    error,
    play,
  }
}

function readStoredCouponGameResult() {
  if (typeof window === 'undefined') {
    return null
  }

  try {
    return loadCouponGameResult(window.localStorage)
  } catch {
    return null
  }
}
