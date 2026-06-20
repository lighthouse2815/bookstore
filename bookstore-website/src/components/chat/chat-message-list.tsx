import { useEffect, useMemo, useRef } from 'react'
import { Loader2, MessageSquareText } from 'lucide-react'
import type { ChatMessageResponse } from '@/types/chat'
import { cn } from '@/utils'

type ChatMessageListProps = {
  messages: ChatMessageResponse[]
  currentUserId: string
  locale: string
  emptyLabel: string
  loadingLabel: string
  loadMoreLabel?: string
  hasNext?: boolean
  isLoading?: boolean
  isLoadingMore?: boolean
  onLoadMore?: () => void
  className?: string
}

export function ChatMessageList({
  messages,
  currentUserId,
  locale,
  emptyLabel,
  loadingLabel,
  loadMoreLabel = 'Load older',
  hasNext = false,
  isLoading = false,
  isLoadingMore = false,
  onLoadMore,
  className,
}: ChatMessageListProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const lastMessageId = messages.at(-1)?.messageId

  useEffect(() => {
    if (!containerRef.current) {
      return
    }

    containerRef.current.scrollTo({
      top: containerRef.current.scrollHeight,
      behavior: 'smooth',
    })
  }, [lastMessageId])

  const formatter = useMemo(
    () =>
      new Intl.DateTimeFormat(locale, {
        dateStyle: 'medium',
        timeStyle: 'short',
      }),
    [locale],
  )

  return (
    <div
      className={cn(
        'flex min-h-0 flex-1 flex-col overflow-hidden rounded-[26px] border border-border/60 bg-background/60',
        className,
      )}
    >
      <div
        ref={containerRef}
        className="flex min-h-0 flex-1 flex-col gap-3 overflow-y-auto px-4 py-4"
      >
        {hasNext ? (
          <div className="flex justify-center pb-1">
            <button
              type="button"
              onClick={onLoadMore}
              disabled={isLoadingMore}
              className="rounded-full border border-border/70 px-4 py-2 text-xs font-semibold text-muted-foreground transition-colors hover:bg-muted disabled:cursor-not-allowed disabled:opacity-60"
            >
              {isLoadingMore ? loadingLabel : loadMoreLabel}
            </button>
          </div>
        ) : null}

        {isLoading && messages.length === 0 ? (
          <div className="flex min-h-52 items-center justify-center gap-2 text-sm text-muted-foreground">
            <Loader2 className="h-4 w-4 animate-spin" />
            {loadingLabel}
          </div>
        ) : messages.length === 0 ? (
          <div className="flex min-h-52 flex-col items-center justify-center gap-3 text-center text-sm text-muted-foreground">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 text-primary">
              <MessageSquareText className="h-5 w-5" />
            </div>
            <p>{emptyLabel}</p>
          </div>
        ) : (
          messages.map((message) => {
            const isMine = message.senderId === currentUserId

            return (
              <div
                key={message.messageId}
                className={cn(
                  'flex flex-col gap-1',
                  isMine ? 'items-end' : 'items-start',
                )}
              >
                <div
                  className={cn(
                    'max-w-[85%] rounded-[22px] px-4 py-3 shadow-sm',
                    isMine
                      ? 'bg-primary text-primary-foreground'
                      : 'border border-border/60 bg-card text-foreground',
                  )}
                >
                  <p className="whitespace-pre-wrap break-words text-sm leading-6">
                    {message.content}
                  </p>
                </div>
                <div
                  className={cn(
                    'px-1 text-[11px] text-muted-foreground',
                    isMine ? 'text-right' : 'text-left',
                  )}
                >
                  <span className="font-medium">{message.senderRole}</span>
                  <span className="mx-1.5">•</span>
                  <span>{formatter.format(new Date(message.createdAt))}</span>
                </div>
              </div>
            )
          })
        )}
      </div>
    </div>
  )
}
