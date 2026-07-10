import { useEffect, useState } from 'react'
import { getPersonalizedRecommendations } from '@/services/recommendation-service'
import type { PersonalizedRecommendation } from '@/types/book-recommendation'
import { getErrorMessage } from '@/utils'

type PersonalizedRecommendationState = {
  data: PersonalizedRecommendation | null
  error: string | null
  isLoading: boolean
}

const initialState: PersonalizedRecommendationState = {
  data: null,
  error: null,
  isLoading: false,
}

export function usePersonalizedRecommendations(enabled: boolean, limit = 12) {
  const [state, setState] = useState<PersonalizedRecommendationState>(initialState)

  useEffect(() => {
    let isCancelled = false

    if (!enabled) {
      setState(initialState)
      return () => {
        isCancelled = true
      }
    }

    setState({ data: null, error: null, isLoading: true })

    async function loadRecommendations() {
      try {
        const data = await getPersonalizedRecommendations(limit)
        if (!isCancelled) {
          setState({ data, error: null, isLoading: false })
        }
      } catch (error) {
        if (!isCancelled) {
          setState({ data: null, error: getErrorMessage(error), isLoading: false })
        }
      }
    }

    void loadRecommendations()

    return () => {
      isCancelled = true
    }
  }, [enabled, limit])

  return state
}
