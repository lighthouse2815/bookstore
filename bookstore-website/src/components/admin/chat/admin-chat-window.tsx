import { Button } from '@/components/common/button'
import { ChatInput } from '@/components/chat/chat-input'
import { ChatMessageList } from '@/components/chat/chat-message-list'
import type {
  AdminChatLabels,
  AdminMessagePageState,
} from '@/hooks/use-admin-chat-page'
import type { ChatMessageResponse, ConversationResponse } from '@/types/chat'

type AdminChatWindowProps = {
  labels: AdminChatLabels
  conversation: ConversationResponse | null
  messages: ChatMessageResponse[]
  messagePageState: AdminMessagePageState
  currentUserId: string
  locale: string
  draft: string
  onDraftChange: (value: string) => void
  onLoadMore: () => Promise<void> | void
  onSend: () => Promise<void> | void
  onCloseConversation: () => Promise<void> | void
  onReopenConversation: () => Promise<void> | void
  isSending: boolean
  isUpdatingStatus: boolean
}

export function AdminChatWindow({
  labels,
  conversation,
  messages,
  messagePageState,
  currentUserId,
  locale,
  draft,
  onDraftChange,
  onLoadMore,
  onSend,
  onCloseConversation,
  onReopenConversation,
  isSending,
  isUpdatingStatus,
}: AdminChatWindowProps) {
  if (!conversation) {
    return (
      <section className="flex h-[clamp(32rem,78dvh,42rem)] min-h-0 items-center justify-center rounded-[30px] border border-dashed border-border/70 bg-card/70 p-8 text-center text-muted-foreground">
        <p>{labels.noConversationSelected}</p>
      </section>
    )
  }

  return (
    <section className="flex h-[clamp(32rem,78dvh,42rem)] min-h-0 flex-col overflow-hidden rounded-[30px] border border-border/60 bg-card/90 shadow-[0_24px_80px_rgba(2,6,23,0.18)]">
      <div className="border-b border-border/60 px-5 py-5">
        <div className="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p className="text-lg font-semibold text-foreground">
              {conversation.subject || labels.noMessagesYet}
            </p>
            <p className="mt-1 text-sm text-muted-foreground">
              {conversation.customerName || conversation.customerEmail || conversation.customerId}
            </p>
          </div>
          <div className="flex flex-wrap gap-2">
            {conversation.status === 'CLOSED' ? (
              <Button
                type="button"
                variant="outline"
                className="rounded-2xl"
                disabled={isUpdatingStatus}
                onClick={() => void onReopenConversation()}
              >
                {labels.reopenConversation}
              </Button>
            ) : (
              <Button
                type="button"
                variant="outline"
                className="rounded-2xl"
                disabled={isUpdatingStatus}
                onClick={() => void onCloseConversation()}
              >
                {labels.closeConversation}
              </Button>
            )}
          </div>
        </div>
      </div>

      <div className="flex min-h-0 flex-1 flex-col gap-4 overflow-hidden px-5 py-5">
        <ChatMessageList
          messages={messages}
          currentUserId={currentUserId}
          locale={locale}
          emptyLabel={labels.messageEmpty}
          loadingLabel={labels.loadingMessages}
          loadMoreLabel={labels.loadMore}
          hasNext={messagePageState.hasNext}
          isLoading={messagePageState.isLoading}
          isLoadingMore={messagePageState.isLoadingMore}
          onLoadMore={onLoadMore}
        />

        {conversation.status === 'CLOSED' ? (
          <div className="shrink-0 rounded-[24px] border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-sm text-amber-700 dark:text-amber-300">
            {labels.closedNotice}
          </div>
        ) : (
          <div className="shrink-0">
            <ChatInput
              value={draft}
              onChange={onDraftChange}
              onSubmit={onSend}
              placeholder={labels.replyPlaceholder}
              submitLabel={labels.send}
              disabled={isSending}
              isSubmitting={isSending}
            />
          </div>
        )}
      </div>
    </section>
  )
}
