import type { RecommendationReasonCode } from '@/types/book-recommendation'

const DISPLAYED_REASON_COUNT = 2

export function getRecommendationReasonKey(reasonCode: RecommendationReasonCode) {
  return `recommendations.reasons.${reasonCode}`
}

export function getDisplayReasonCodes(reasonCodes: RecommendationReasonCode[]) {
  return reasonCodes.slice(0, DISPLAYED_REASON_COUNT)
}
