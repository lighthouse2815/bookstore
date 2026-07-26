import type { ChangeEvent } from 'react'
import { MessageSquareMore, Search } from 'lucide-react'
import { Input } from '@/components/common/input'
import type { AdminConversationFilter, AdminChatLabels } from '@/hooks/use-admin-chat-page'
import type { ConversationResponse } from '@/types/chat'
import { cn } from '@/utils'

type AdminConversationListProps = {
  labels: AdminChatLabels
  conversations: ConversationResponse[]
  activeConversationId: string | null
  keyword: string
  statusFilter: AdminConversationFilter
  locale: string
  isLoading: boolean
  isLoadingMore: boolean
  hasNext: boolean
  onKeywordChange: (event: ChangeEvent<HTMLInputElement>) => void
  onStatusFilterChange: (value: AdminConversationFilter) => void
  onSelectConversation: (conversationId: string) => void
  onLoadMore: () => Promise<void> | void
}

export function AdminConversationList({
  labels,
  conversations,
  activeConversationId,
  keyword,
  statusFilter,
  locale,
  isLoading,
  isLoadingMore,
  hasNext,
  onKeywordChange,
  onStatusFilterChange,
  onSelectConversation,
  onLoadMore,
}: AdminConversationListProps) {
  const formatter = new Intl.DateTimeFormat(locale, {
    dateStyle: 'medium',
    timeStyle: 'short',
  })

  return (
    <section className="flex h-[clamp(32rem,78dvh,42rem)] min-h-0 flex-col overflow-hidden rounded-[30px] border border-border/60 bg-card/90 shadow-[0_24px_80px_rgba(2,6,23,0.18)]">
      <div className="border-b border-border/60 px-5 py-5">
        <h2 className="text-lg font-semibold text-foreground">{labels.listTitle}</h2>

        <div className="mt-4 space-y-3">
          <div className="relative">
            <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              value={keyword}
              onChange={onKeywordChange}
              placeholder={labels.searchPlaceholder}
              className="h-12 rounded-2xl pl-11"
            />
          </div>

          <select
            value={statusFilter}
            onChange={(event) =>
              onStatusFilterChange(event.currentTarget.value as AdminConversationFilter)
            }
            className="h-12 w-full rounded-2xl border border-input bg-background px-3 text-sm text-foreground"
          >
            <option value="ALL">{labels.statusAll}</option>
            <option value="OPEN">{labels.statusOpen}</option>
            <option value="PENDING">{labels.statusPending}</option>
            <option value="CLOSED">{labels.statusClosed}</option>
          </select>
        </div>
      </div>

      <div className="flex min-h-0 flex-1 flex-col overflow-y-auto overscroll-contain px-3 py-3 [scrollbar-gutter:stable]">
        {isLoading ? (
          <div className="flex min-h-56 items-center justify-center text-sm text-muted-foreground">
            {labels.loadingList}
          </div>
        ) : conversations.length === 0 ? (
          <div className="flex min-h-56 flex-col items-center justify-center gap-3 px-6 text-center text-sm text-muted-foreground">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-primary/10 text-primary">
              <MessageSquareMore className="h-5 w-5" />
            </div>
            <p>{labels.emptyConversations}</p>
          </div>
        ) : (
          <div className="space-y-2">
            {conversations.map((conversation) => (
              <button
                key={conversation.conversationId}
                type="button"
                onClick={() => onSelectConversation(conversation.conversationId)}
                className={cn(
                  'w-full rounded-[24px] border px-4 py-4 text-left transition-colors',
                  conversation.conversationId === activeConversationId
                    ? 'border-primary bg-primary/8'
                    : 'border-border/60 bg-background/55 hover:bg-muted/70',
                )}
              >
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="truncate text-sm font-semibold text-foreground">
                      {conversation.customerName || conversation.customerEmail || conversation.customerId}
                    </p>
                    <p className="mt-1 truncate text-xs text-muted-foreground">
                      {conversation.customerEmail || conversation.customerId}
                    </p>
                  </div>
                  {conversation.myUnreadCount > 0 ? (
                    <span className="rounded-full bg-primary px-2 py-1 text-[11px] font-bold text-primary-foreground">
                      {conversation.myUnreadCount}
                    </span>
                  ) : null}
                </div>

                <p className="mt-3 line-clamp-1 text-sm font-medium text-foreground">
                  {conversation.subject || labels.noMessagesYet}
                </p>
                <p className="mt-1 line-clamp-2 text-sm text-muted-foreground">
                  {conversation.lastMessagePreview || labels.noMessagesYet}
                </p>

                <div className="mt-3 flex flex-wrap items-center gap-2 text-[11px] text-muted-foreground">
                  <span className="rounded-full border border-border/70 px-2 py-1">
                    {conversation.status}
                  </span>
                  <span className="rounded-full border border-border/70 px-2 py-1">
                    {conversation.priority}
                  </span>
                  <span>
                    {formatter.format(
                      new Date(
                        conversation.lastMessageAt ??
                          conversation.updatedAt ??
                          conversation.createdAt,
                      ),
                    )}
                  </span>
                </div>
              </button>
            ))}

            {hasNext ? (
              <div className="pt-2 text-center">
                <button
                  type="button"
                  onClick={() => void onLoadMore()}
                  disabled={isLoadingMore}
                  className="rounded-full border border-border/70 px-4 py-2 text-xs font-semibold text-muted-foreground transition-colors hover:bg-muted disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isLoadingMore ? labels.loadingList : labels.loadMore}
                </button>
              </div>
            ) : null}
          </div>
        )}
      </div>
    </section>
  )
}
