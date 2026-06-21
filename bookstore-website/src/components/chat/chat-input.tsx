import type { FormEvent, KeyboardEvent } from 'react'
import { SendHorizonal } from 'lucide-react'
import { Button } from '@/components/common/button'
import { Textarea } from '@/components/common/textarea'

type ChatInputProps = {
  value: string
  onChange: (value: string) => void
  onSubmit: () => Promise<void> | void
  placeholder: string
  submitLabel: string
  disabled?: boolean
  isSubmitting?: boolean
}

export function ChatInput({
  value,
  onChange,
  onSubmit,
  placeholder,
  submitLabel,
  disabled = false,
  isSubmitting = false,
}: ChatInputProps) {
  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (disabled || isSubmitting || value.trim() === '') {
      return
    }

    await onSubmit()
  }

  function handleKeyDown(event: KeyboardEvent<HTMLTextAreaElement>) {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault()
      void onSubmit()
    }
  }

  return (
    <form onSubmit={(event) => void handleSubmit(event)} className="space-y-3">
      <Textarea
        value={value}
        onChange={(event) => onChange(event.currentTarget.value)}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        className="min-h-24 rounded-[22px]"
        disabled={disabled || isSubmitting}
      />
      <div className="flex justify-end">
        <Button
          type="submit"
          className="rounded-2xl"
          disabled={disabled || isSubmitting || value.trim() === ''}
        >
          <SendHorizonal className="mr-2 h-4 w-4" />
          {isSubmitting ? '...' : submitLabel}
        </Button>
      </div>
    </form>
  )
}
