import { MessageSquareMore, Wifi, WifiOff } from 'lucide-react'
import { AdminConversationList } from '@/components/admin/chat/admin-conversation-list'
import { AdminChatDetailPanel } from '@/components/admin/chat/admin-chat-detail-panel'
import { AdminChatWindow } from '@/components/admin/chat/admin-chat-window'
import { Badge } from '@/components/common/badge'
import { AdminLayout } from '@/components/layout/admin-layout'
import { useAdminChatPage } from '@/hooks/use-admin-chat-page'
import { useState } from 'react'

export default function AdminChatPage() {
  const {
    user,
    locale,
    formatNumber,
    labels,
    conversations,
    activeConversation,
    activeConversationId,
    keyword,
    statusFilter,
    totalCount,
    unreadConversationCount,
    openConversationCount,
    hasNext,
    isLoading,
    isLoadingMore,
    isSending,
    isAssigning,
    isUpdatingStatus,
    isRealtimeConnected,
    error,
    canAssignEmployees,
    employeeOptions,
    pendingAssigneeId,
    setPendingAssigneeId,
    handleKeywordChange,
    handleStatusFilterChange,
    loadMoreConversations,
    selectConversation,
    loadMessages,
    sendMessage,
    assignConversation,
    closeConversation,
    reopenConversation,
    getConversationMessages,
    getMessagePageState,
  } = useAdminChatPage()
  const [draft, setDraft] = useState('')

  async function handleSendMessage() {
    if (draft.trim() === '') {
      return
    }

    await sendMessage(draft)
    setDraft('')
  }

  return (
    <AdminLayout>
      <div className="space-y-6">
        <section className="relative overflow-hidden rounded-[32px] border border-border/60 bg-card/90 p-6 shadow-[0_28px_90px_rgba(2,6,23,0.35)] backdrop-blur xl:p-8">
          <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_top_left,rgba(59,130,246,0.18),transparent_34%),radial-gradient(circle_at_bottom_right,rgba(16,185,129,0.14),transparent_32%)]" />

          <div className="relative">
            <div className="flex flex-col gap-5 xl:flex-row xl:items-start xl:justify-between">
              <div>
                <div className="flex flex-wrap items-center gap-3">
                  <h1 className="font-heading text-3xl font-bold text-foreground sm:text-4xl">
                    {labels.title}
                  </h1>
                  <Badge
                    variant="outline"
                    className="rounded-2xl border-primary/20 bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary"
                  >
                    <MessageSquareMore className="mr-2 h-4 w-4" />
                    {formatNumber(totalCount)}
                  </Badge>
                </div>
                <p className="mt-3 max-w-3xl text-base text-muted-foreground">
                  {labels.description}
                </p>
              </div>

              <div className="rounded-[24px] border border-border/60 bg-background/60 px-4 py-3 text-sm text-muted-foreground">
                <div className="flex items-center gap-2">
                  {isRealtimeConnected ? (
                    <Wifi className="h-4 w-4 text-emerald-600" />
                  ) : (
                    <WifiOff className="h-4 w-4 text-amber-600" />
                  )}
                  <span>
                    {isRealtimeConnected ? labels.connected : labels.fallback}
                  </span>
                </div>
              </div>
            </div>

            <div className="mt-6 grid gap-3 md:grid-cols-3">
              <MetricCard
                label={labels.totalConversations}
                value={formatNumber(totalCount)}
              />
              <MetricCard
                label={labels.unreadCount}
                value={formatNumber(unreadConversationCount)}
              />
              <MetricCard
                label={labels.openCount}
                value={formatNumber(openConversationCount)}
              />
            </div>
          </div>
        </section>

        {error && !isLoading ? (
          <div className="rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
            {error}
          </div>
        ) : null}

        <div className="grid items-start gap-6 xl:grid-cols-[22rem_minmax(0,1fr)_22rem]">
          <AdminConversationList
            labels={labels}
            conversations={conversations}
            activeConversationId={activeConversationId}
            keyword={keyword}
            statusFilter={statusFilter}
            locale={locale}
            isLoading={isLoading}
            isLoadingMore={isLoadingMore}
            hasNext={hasNext}
            onKeywordChange={handleKeywordChange}
            onStatusFilterChange={handleStatusFilterChange}
            onSelectConversation={selectConversation}
            onLoadMore={loadMoreConversations}
          />

          <AdminChatWindow
            labels={labels}
            conversation={activeConversation}
            messages={getConversationMessages(activeConversationId)}
            messagePageState={getMessagePageState(activeConversationId)}
            currentUserId={user?.id ?? ''}
            locale={locale}
            draft={draft}
            onDraftChange={setDraft}
            onLoadMore={() =>
              activeConversationId
                ? loadMessages(activeConversationId, { loadMore: true })
                : Promise.resolve()
            }
            onSend={handleSendMessage}
            onCloseConversation={closeConversation}
            onReopenConversation={reopenConversation}
            isSending={isSending}
            isUpdatingStatus={isUpdatingStatus}
          />

          <AdminChatDetailPanel
            labels={labels}
            conversation={activeConversation}
            employeeOptions={employeeOptions}
            pendingAssigneeId={pendingAssigneeId}
            currentUserId={user?.id ?? ''}
            locale={locale}
            canAssignEmployees={canAssignEmployees}
            isAssigning={isAssigning}
            onPendingAssigneeIdChange={setPendingAssigneeId}
            onAssign={() =>
              pendingAssigneeId.trim() === ''
                ? Promise.resolve()
                : assignConversation(pendingAssigneeId)
            }
            onAssignToSelf={() =>
              user ? assignConversation(user.id) : Promise.resolve()
            }
          />
        </div>
      </div>
    </AdminLayout>
  )
}

function MetricCard({
  label,
  value,
}: {
  label: string
  value: string
}) {
  return (
    <div className="rounded-2xl border border-border/60 bg-background/55 px-5 py-4">
      <p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">
        {label}
      </p>
      <p className="mt-2 text-2xl font-bold text-foreground">{value}</p>
    </div>
  )
}
