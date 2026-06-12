import { useId, useState } from 'react'

export function useAuthFlipCard(
  checked?: boolean,
  onCheckedChange?: (checked: boolean) => void,
) {
  const toggleId = useId().replace(/:/g, '')
  const [internalChecked, setInternalChecked] = useState(false)
  const isControlled = checked !== undefined
  const isChecked = isControlled ? checked : internalChecked

  function handleCheckedChange(nextChecked: boolean) {
    if (!isControlled) {
      setInternalChecked(nextChecked)
    }

    onCheckedChange?.(nextChecked)
  }

  return {
    toggleId,
    isChecked,
    handleCheckedChange,
  }
}
