import { useEffect, useRef, useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import {
  MessageCircleMore,
  Plus,
  RefreshCw,
  Wifi,
  WifiOff,
  X,
} from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { ChatInput } from '@/components/chat/chat-input'
import { ChatMessageList } from '@/components/chat/chat-message-list'
import { useAuth } from '@/contexts/auth-context'
import { useChat } from '@/contexts/chat-context'
import { useLanguage } from '@/contexts/language-context'
import { cn, getErrorMessage } from '@/utils'

export function CustomerChatWidget() {
  const { pathname } = useLocation()
  const { user } = useAuth()
  const { locale, t } = useLanguage()
  const {
    conversations,
    activeConversation,
    activeConversationId,
    unreadCount,
    isLoading,
    isRealtimeConnected,
    error,
    lastIncomingMessage,
    refresh,
    setActiveConversation,
    loadMessages,
    sendMessage,
    markRead,
    closeConversation,
    getConversationMessages,
    getMessagePageState,
  } = useChat()
  const [open, setOpen] = useState(false)
  const [draft, setDraft] = useState('')
  const [draftSubject, setDraftSubject] = useState('')
  const [isSending, setIsSending] = useState(false)
  const [isNewConversationMode, setIsNewConversationMode] = useState(false)
  const lastToastMessageIdRef = useRef<string | null>(null)

  const defaultSubject = t('chat.customer.defaultSubject')

  const canRender =
    Boolean(user) &&
    user!.roles.includes('USER') &&
    !pathname.startsWith('/admin')

  const activeMessages = getConversationMessages(activeConversationId)
  const messagePageState = getMessagePageState(activeConversationId)

  useEffect(() => {
    if (!canRender) {
      setOpen(false)
      setIsNewConversationMode(false)
      return
    }

    if (draftSubject.trim() === '') {
      setDraftSubject(defaultSubject)
    }
  }, [canRender, defaultSubject, draftSubject])

  useEffect(() => {
    if (!open || !activeConversationId) {
      return
    }

    void loadMessages(activeConversationId).catch((currentError) => {
      toast.error(getErrorMessage(currentError))
    })
    void markRead(activeConversationId).catch(() => undefined)
  }, [activeConversationId, loadMessages, markRead, open])

  useEffect(() => {
    if (
      !lastIncomingMessage ||
      lastToastMessageIdRef.current === lastIncomingMessage.messageId
    ) {
      return
    }

    lastToastMessageIdRef.current = lastIncomingMessage.messageId

    if (open && activeConversationId === lastIncomingMessage.conversationId) {
      void markRead(lastIncomingMessage.conversationId).catch(() => undefined)
      return
    }

    toast.success(t('chat.customer.incomingTitle'), {
      description: lastIncomingMessage.content,
    })
  }, [activeConversationId, lastIncomingMessage, markRead, open, t])

  if (!canRender) {
    return null
  }

  async function handleSendMessage() {
    const trimmedDraft = draft.trim()

    if (trimmedDraft === '') {
      return
    }

    setIsSending(true)

    try {
      await sendMessage(
        {
          content: trimmedDraft,
          messageType: 'TEXT',
        },
        {
          conversationId:
            !isNewConversationMode && activeConversation?.status !== 'CLOSED'
              ? activeConversationId
              : null,
          createConversation:
            !isNewConversationMode && activeConversation?.status !== 'CLOSED'
              ? undefined
              : {
                  subject: draftSubject,
                  priority: 'NORMAL',
                  targetType: 'GENERAL',
                  targetId: null,
                },
        },
      )
      setDraft('')
      setIsNewConversationMode(false)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    } finally {
      setIsSending(false)
    }
  }

  async function handleCloseConversation() {
    if (!activeConversationId) {
      return
    }

    try {
      await closeConversation(activeConversationId)
    } catch (currentError) {
      toast.error(getErrorMessage(currentError))
    }
  }

  function handleStartNewConversation() {
    setIsNewConversationMode(true)
    setDraftSubject(defaultSubject)
    setDraft('')
    setActiveConversation(null)
  }

  const disableComposer =
    isSending ||
    (activeConversation?.status === 'CLOSED' && !isNewConversationMode)

  return (
    <div className="pointer-events-none fixed bottom-5 right-5 z-[140] flex flex-col items-end gap-3">
      {open ? (
        <div className="pointer-events-auto w-[min(26rem,calc(100vw-1.5rem))] overflow-hidden rounded-[30px] border border-border/70 bg-background/95 shadow-[0_32px_90px_rgba(2,6,23,0.35)] backdrop-blur">
          <div className="border-b border-border/60 px-5 py-4">
            <div className="flex items-start justify-between gap-4">
              <div>
                <div className="flex items-center gap-2">
                  <p className="text-lg font-semibold text-foreground">
                    {t('chat.customer.title')}
                  </p>
                  <span
                    className={cn(
                      'inline-flex items-center gap-1 rounded-full px-2.5 py-1 text-[11px] font-semibold',
                      isRealtimeConnected
                        ? 'bg-emerald-500/12 text-emerald-600'
                        : 'bg-amber-500/12 text-amber-600',
                    )}
                  >
                    {isRealtimeConnected ? (
                      <Wifi className="h-3 w-3" />
                    ) : (
                      <WifiOff className="h-3 w-3" />
                    )}
                    {isRealtimeConnected
                      ? t('chat.customer.realtimeConnected')
                      : t('chat.customer.realtimeFallback')}
                  </span>
                </div>
                <p className="mt-1 text-sm text-muted-foreground">
                  {t('chat.customer.subtitle')}
                </p>
              </div>
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  onClick={() => void refresh()}
                  className="flex h-9 w-9 items-center justify-center rounded-full text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
                  aria-label={t('chat.customer.refresh')}
                >
                  <RefreshCw className="h-4 w-4" />
                </button>
                <button
                  type="button"
                  onClick={() => setOpen(false)}
                  className="flex h-9 w-9 items-center justify-center rounded-full text-muted-foreground transition-colors hover:bg-muted hover:text-foreground"
                  aria-label={t('chat.customer.openChat')}
                >
                  <X className="h-4 w-4" />
                </button>
              </div>
            </div>

            <div className="mt-4 flex items-center gap-2 overflow-x-auto pb-1">
              <button
                type="button"
                onClick={handleStartNewConversation}
                className={cn(
                  'shrink-0 rounded-full border px-3 py-2 text-xs font-semibold transition-colors',
                  isNewConversationMode
                    ? 'border-primary bg-primary text-primary-foreground'
                    : 'border-border/70 text-muted-foreground hover:bg-muted',
                )}
              >
                <Plus className="mr-1 inline h-3.5 w-3.5" />
                {t('chat.customer.newConversation')}
              </button>

              {conversations.map((conversation) => (
                <button
                  key={conversation.conversationId}
                  type="button"
                  onClick={() => {
                    setIsNewConversationMode(false)
                    setActiveConversation(conversation.conversationId)
                  }}
                  className={cn(
                    'shrink-0 rounded-full border px-3 py-2 text-left text-xs font-semibold transition-colors',
                    conversation.conversationId === activeConversationId &&
                      !isNewConversationMode
                      ? 'border-primary bg-primary text-primary-foreground'
                      : 'border-border/70 text-muted-foreground hover:bg-muted',
                  )}
                >
                  <span className="line-clamp-1">
                    {conversation.subject || defaultSubject}
                  </span>
                  {conversation.myUnreadCount > 0 ? (
                    <span className="ml-2 rounded-full bg-background/20 px-1.5 py-0.5 text-[10px]">
                      {conversation.myUnreadCount}
                    </span>
                  ) : null}
                </button>
              ))}
            </div>
          </div>

          <div className="flex h-[34rem] flex-col gap-4 px-4 py-4">
            {error ? (
              <div className="rounded-2xl border border-destructive/30 bg-destructive/10 px-4 py-3 text-sm text-destructive">
                {error}
              </div>
            ) : null}

            {isNewConversationMode ? (
              <div className="rounded-[24px] border border-border/60 bg-card/70 p-4">
                <p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">
                  {t('chat.customer.subject')}
                </p>
                <Input
                  value={draftSubject}
                  onChange={(event) => setDraftSubject(event.currentTarget.value)}
                  placeholder={t('chat.customer.subjectPlaceholder')}
                  className="mt-3 h-11 rounded-2xl"
                />
              </div>
            ) : activeConversation ? (
              <div className="flex items-center justify-between gap-3 rounded-[24px] border border-border/60 bg-card/70 px-4 py-3">
                <div className="min-w-0">
                  <p className="truncate text-sm font-semibold text-foreground">
                    {activeConversation.subject || defaultSubject}
                  </p>
                  <p className="mt-1 text-xs text-muted-foreground">
                    {activeConversation.status}
                  </p>
                </div>
                {activeConversation.status !== 'CLOSED' ? (
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    className="rounded-2xl"
                    onClick={() => void handleCloseConversation()}
                  >
                    {t('chat.customer.closeConversation')}
                  </Button>
                ) : null}
              </div>
            ) : null}

            <ChatMessageList
              messages={activeMessages}
              currentUserId={user!.id}
              locale={locale}
              emptyLabel={
                isNewConversationMode || activeConversation
                  ? t('chat.customer.emptyMessages')
                  : t('chat.customer.emptyConversations')
              }
              loadingLabel={t('chat.customer.loadingMessages')}
              loadMoreLabel={t('chat.customer.loadOlderMessages')}
              hasNext={messagePageState.hasNext}
              isLoading={messagePageState.isLoading || isLoading}
              isLoadingMore={messagePageState.isLoadingMore}
              onLoadMore={
                activeConversationId
                  ? () => void loadMessages(activeConversationId, { loadMore: true })
                  : undefined
              }
            />

            {activeConversation?.status === 'CLOSED' && !isNewConversationMode ? (
              <div className="rounded-[24px] border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-sm text-amber-700 dark:text-amber-300">
                <p>{t('chat.customer.closedNotice')}</p>
                <div className="mt-3 flex gap-2">
                  <Button
                    type="button"
                    className="rounded-2xl"
                    onClick={handleStartNewConversation}
                  >
                    {t('chat.customer.newConversation')}
                  </Button>
                  <Link
                    to="/notifications"
                    className="inline-flex h-7 items-center justify-center rounded-2xl border border-border bg-background px-3 text-[0.8rem] font-medium text-foreground transition-colors hover:bg-muted"
                  >
                    {t('chat.customer.viewAllNotifications')}
                  </Link>
                </div>
              </div>
            ) : (
              <ChatInput
                value={draft}
                onChange={setDraft}
                onSubmit={handleSendMessage}
                placeholder={t('chat.customer.sendPlaceholder')}
                submitLabel={t('chat.customer.send')}
                disabled={disableComposer}
                isSubmitting={isSending}
              />
            )}
          </div>
        </div>
      ) : null}

      <button
        type="button"
        onClick={() => setOpen((currentOpen) => !currentOpen)}
        className="pointer-events-auto relative flex h-16 w-16 items-center justify-center rounded-full bg-primary text-primary-foreground shadow-[0_18px_40px_rgba(79,70,229,0.45)] transition-transform hover:scale-[1.02]"
        aria-label={t('chat.customer.openChat')}
      >
        <MessageCircleMore className="h-7 w-7" />
        {unreadCount > 0 ? (
          <span className="absolute -right-1 -top-1 flex min-w-6 items-center justify-center rounded-full bg-destructive px-1.5 py-1 text-[10px] font-bold text-destructive-foreground">
            {unreadCount > 99 ? '99+' : unreadCount}
          </span>
        ) : null}
      </button>
    </div>
  )
}
