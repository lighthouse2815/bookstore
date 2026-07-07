import { createPortal } from 'react-dom'
import type { ReactNode } from 'react'
import {
  BellRing,
  CalendarDays,
  Eye,
  Globe,
  Link2,
  MailPlus,
  Plus,
  Search,
  Send,
  Trash2,
  UserRound,
  X,
} from 'lucide-react'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { Textarea } from '@/components/common/textarea'
import { useAdminNotificationsPage } from '@/hooks/use-admin-notifications-page'
import { AdminLayout } from '@/components/layout/admin-layout'
import type { NotificationResponse } from '@/types/notification'

type UserLookup = {
  id: string
  name: string
  email: string
}

export default function AdminNotificationsPage() {
  const {
    t,
    formatDate,
    formatNumber,
    labels,
    recipients,
    searchTerm,
    isLoading,
    isLoadingMore,
    error,
    dialogMode,
    selectedNotification,
    form,
    totalCount,
    hasNext,
    isSubmitting,
    recipientLookup,
    filteredNotifications,
    unreadCount,
    readCount,
    handleSearchTermChange,
    handleFormChange,
    closeDialog,
    openCreateDialog,
    openBroadcastDialog,
    openViewDialog,
    handleLoadMore,
    handleDelete,
    handleSubmit,
  } = useAdminNotificationsPage()

  const isCreateDialog = dialogMode === 'create'
  const isBroadcastDialog = dialogMode === 'broadcast'

  const dialogMarkup = dialogMode ? (
    <div className="fixed inset-0 z-[160] flex items-center justify-center px-4 py-6">
      <button
        type="button"
        aria-label={t('common.close')}
        className="absolute inset-0 bg-background/72 backdrop-blur-sm"
        onClick={closeDialog}
        disabled={isSubmitting}
      />
      <div className="relative z-10 w-full max-w-4xl">
        {isCreateDialog || isBroadcastDialog ? (
          <DialogShell
            title={isBroadcastDialog ? labels.broadcast : labels.add}
            onClose={closeDialog}
            canClose={!isSubmitting}
          >
            <form className="space-y-6" onSubmit={(event) => void handleSubmit(event)}>
              <div className="grid gap-5 lg:grid-cols-[minmax(0,1.2fr)_minmax(0,0.8fr)]">
                <div className="space-y-5">
                  {isCreateDialog ? (
                    <div className="space-y-2">
                      <Label>{labels.recipient}</Label>
                      <select
                        value={form.userId}
                        onChange={(event) =>
                          handleFormChange('userId', event.currentTarget.value)
                        }
                        className="h-11 w-full rounded-2xl border border-input bg-background px-3 text-sm"
                        required
                      >
                        <option value="" disabled>
                          {labels.chooseRecipient}
                        </option>
                        {recipients.map((recipient) => (
                          <option key={recipient.id} value={recipient.id}>
                            {recipient.name} - {recipient.email}
                          </option>
                        ))}
                      </select>
                    </div>
                  ) : null}

                  <div className="space-y-2">
                    <Label>{labels.subject}</Label>
                    <Input
                      value={form.title}
                      onChange={(event) =>
                        handleFormChange('title', event.currentTarget.value)
                      }
                      className="h-11 rounded-2xl"
                      required
                    />
                  </div>

                  <div className="grid gap-4 md:grid-cols-2">
                    <div className="space-y-2">
                      <Label>
                        {labels.type}
                        <span className="ml-2 text-xs text-muted-foreground">
                          {labels.optional}
                        </span>
                      </Label>
                      <Input
                        value={form.type}
                        onChange={(event) =>
                          handleFormChange('type', event.currentTarget.value)
                        }
                        className="h-11 rounded-2xl"
                        placeholder="SYSTEM"
                      />
                    </div>

                    <div className="space-y-2">
                      <Label>
                        {labels.link}
                        <span className="ml-2 text-xs text-muted-foreground">
                          {labels.optional}
                        </span>
                      </Label>
                      <Input
                        value={form.link}
                        onChange={(event) =>
                          handleFormChange('link', event.currentTarget.value)
                        }
                        className="h-11 rounded-2xl"
                        placeholder="/orders/..."
                      />
                    </div>
                  </div>

                  <div className="space-y-2">
                    <Label>{labels.content}</Label>
                    <Textarea
                      value={form.content}
                      onChange={(event) =>
                        handleFormChange('content', event.currentTarget.value)
                      }
                      className="min-h-40 rounded-2xl"
                      required
                    />
                  </div>
                </div>

                <NotificationPreviewCard
                  labels={labels}
                  recipientLabel={
                    isBroadcastDialog
                      ? labels.allRecipients
                      : getRecipientPreview(recipientLookup[form.userId], form.userId)
                  }
                  title={form.title}
                  content={form.content}
                  type={form.type}
                  link={form.link}
                />
              </div>

              <div className="flex justify-end gap-3">
                <Button
                  type="button"
                  variant="outline"
                  onClick={closeDialog}
                  className="rounded-2xl"
                  disabled={isSubmitting}
                >
                  {t('common.cancel')}
                </Button>
                <Button
                  type="submit"
                  className="rounded-2xl"
                  disabled={isSubmitting || (!isBroadcastDialog && !form.userId)}
                >
                  <Send className="mr-2 h-4 w-4" />
                  {isSubmitting
                    ? t('common.processing')
                    : isBroadcastDialog
                      ? labels.broadcast
                      : labels.add}
                </Button>
              </div>
            </form>
          </DialogShell>
        ) : null}

        {dialogMode === 'view' && selectedNotification ? (
          <DialogShell title={labels.detailTitle} onClose={closeDialog}>
            <NotificationDetail
              formatDate={formatDate}
              labels={labels}
              notification={selectedNotification}
              recipient={recipientLookup[selectedNotification.userId]}
            />
          </DialogShell>
        ) : null}
      </div>
    </div>
  ) : null

  return (
    <>
      <AdminLayout>
        <div className="relative overflow-hidden rounded-[32px] border border-border/60 bg-card/90 p-6 shadow-[0_28px_90px_rgba(2,6,23,0.35)] backdrop-blur xl:p-8">
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
                    className="rounded-2xl border-primary/20 bg-primary/12 px-4 py-1.5 text-sm font-semibold text-primary dark:border-primary/30"
                  >
                    <BellRing className="mr-2 h-4 w-4" />
                    {t('admin.notificationsPage.total', {
                      count: formatNumber(totalCount),
                    })}
                  </Badge>
                </div>
                <p className="mt-3 max-w-2xl text-base text-muted-foreground">
                  {labels.description}
                </p>
              </div>

              <div className="flex flex-wrap items-center gap-3">
                <Button
                  size="lg"
                  variant="outline"
                  onClick={openBroadcastDialog}
                  className="h-14 rounded-2xl px-6 text-base"
                >
                  <Globe className="mr-2 h-5 w-5" />
                  {labels.broadcast}
                </Button>
                <Button
                  size="lg"
                  onClick={openCreateDialog}
                  className="h-14 rounded-2xl px-6 text-base shadow-[0_18px_40px_rgba(99,102,241,0.35)]"
                >
                  <Plus className="mr-2 h-5 w-5" />
                  {labels.add}
                </Button>
              </div>
            </div>

            <div className="mt-6 grid gap-3 sm:grid-cols-3">
              <MetricCard label={labels.unread} value={formatNumber(unreadCount)} />
              <MetricCard label={labels.read} value={formatNumber(readCount)} />
              <MetricCard
                label={labels.recipientCount}
                value={formatNumber(recipients.length)}
              />
            </div>

            <div className="mt-8 max-w-xl">
              <div className="relative">
                <Search className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-muted-foreground" />
                <Input
                  value={searchTerm}
                  onChange={handleSearchTermChange}
                  placeholder={labels.search}
                  className="h-14 rounded-2xl border-border/70 bg-background/55 pl-12 text-base"
                />
              </div>
            </div>

            {error && !isLoading ? (
              <div className="mt-8 rounded-2xl border border-destructive/30 bg-destructive/10 p-4 text-sm text-destructive">
                {error}
              </div>
            ) : null}

            <section className="mt-8 overflow-hidden rounded-[28px] border border-primary/30 bg-background/20 shadow-[0_24px_80px_rgba(15,23,42,0.24)] backdrop-blur">
              <div className="space-y-4 p-4">
                <div className="hidden rounded-[24px] border border-border/60 bg-background/55 text-sm font-semibold uppercase tracking-[0.08em] text-muted-foreground shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2fr)_1.2fr_10rem_14rem]">
                  <div className="px-8 py-6">{labels.subject}</div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {labels.recipient}
                  </div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {labels.status}
                  </div>
                  <div className="border-l border-border/40 px-6 py-6 text-center">
                    {labels.actions}
                  </div>
                </div>

                {isLoading ? (
                  <div className="rounded-[24px] border border-border/50 bg-background/40 px-6 py-10 text-center text-muted-foreground">
                    {t('common.loading')}
                  </div>
                ) : filteredNotifications.length === 0 ? (
                  <div className="rounded-[24px] border border-dashed border-border/60 bg-background/35 px-6 py-10 text-center text-muted-foreground">
                    {labels.empty}
                  </div>
                ) : (
                  filteredNotifications.map((notification) => {
                    const recipient = recipientLookup[notification.userId]

                    return (
                      <article
                        key={notification.notificationId}
                        className="flex flex-col gap-5 rounded-[24px] border border-border/60 bg-background/55 p-5 shadow-[0_18px_40px_rgba(2,6,23,0.16)] xl:grid xl:grid-cols-[minmax(0,2fr)_1.2fr_10rem_14rem] xl:gap-0 xl:p-0"
                      >
                        <div className="min-w-0 xl:px-8 xl:py-6">
                          <div className="flex flex-wrap items-center gap-2">
                            <p className="truncate text-lg font-semibold text-foreground">
                              {notification.title}
                            </p>
                            {notification.type ? (
                              <Badge
                                variant="outline"
                                className="rounded-full border-primary/20 bg-primary/10 text-xs font-semibold text-primary"
                              >
                                {notification.type}
                              </Badge>
                            ) : null}
                          </div>
                          <p className="mt-2 truncate text-sm text-muted-foreground">
                            {notification.content || labels.noContent}
                          </p>
                          <div className="mt-3 flex flex-wrap items-center gap-3 text-xs text-muted-foreground">
                            <span className="flex items-center gap-2">
                              <CalendarDays className="h-3.5 w-3.5" />
                              {formatDate(notification.createdAt)}
                            </span>
                            {notification.link ? (
                              <span className="flex items-center gap-1 text-primary">
                                <Link2 className="h-3.5 w-3.5" />
                                <span className="truncate">{notification.link}</span>
                              </span>
                            ) : null}
                          </div>
                        </div>

                        <div className="flex items-center justify-start border-border/40 text-sm font-medium text-foreground xl:justify-center xl:border-l">
                          <div className="min-w-0 text-left xl:text-center">
                            <p className="truncate">
                              {recipient?.name ?? labels.unknownUser}
                            </p>
                            <p className="mt-1 truncate text-xs text-muted-foreground">
                              {recipient?.email ?? notification.userId}
                            </p>
                          </div>
                        </div>

                        <div className="flex items-center justify-start border-border/40 xl:justify-center xl:border-l">
                          <ReadStatusBadge
                            isRead={notification.read}
                            readLabel={labels.read}
                            unreadLabel={labels.unread}
                          />
                        </div>

                        <div className="flex items-center justify-start gap-2 border-border/40 xl:justify-center xl:border-l">
                          <Button
                            type="button"
                            variant="outline"
                            onClick={() => openViewDialog(notification)}
                            className="rounded-2xl"
                          >
                            <Eye className="mr-2 h-4 w-4" />
                            {t('common.view')}
                          </Button>
                          <Button
                            type="button"
                            variant="outline"
                            onClick={() => void handleDelete(notification.notificationId)}
                            className="rounded-2xl text-destructive hover:text-destructive"
                          >
                            <Trash2 className="mr-2 h-4 w-4" />
                            {labels.delete}
                          </Button>
                        </div>
                      </article>
                    )
                  })
                )}

                {hasNext ? (
                  <div className="pt-2 text-center">
                    <Button
                      type="button"
                      variant="outline"
                      onClick={() => void handleLoadMore()}
                      className="rounded-2xl"
                      disabled={isLoadingMore}
                    >
                      {isLoadingMore ? t('common.processing') : labels.loadMore}
                    </Button>
                  </div>
                ) : null}
              </div>
            </section>
          </div>
        </div>
      </AdminLayout>

      {dialogMarkup && typeof document !== 'undefined'
        ? createPortal(dialogMarkup, document.body)
        : null}
    </>
  )
}

function NotificationPreviewCard({
  labels,
  recipientLabel,
  title,
  content,
  type,
  link,
}: {
  labels: {
    content: string
    link: string
    noContent: string
    noLink: string
    noType: string
    previewTitle: string
    recipient: string
    subject: string
    type: string
  }
  recipientLabel: string
  title: string
  content: string
  type: string
  link: string
}) {
  return (
    <div className="rounded-[24px] border border-border/60 bg-background/50 p-5">
      <p className="text-sm font-semibold uppercase tracking-[0.12em] text-muted-foreground">
        {labels.previewTitle}
      </p>

      <div className="mt-5 grid gap-4">
        <DetailCard
          icon={UserRound}
          label={labels.recipient}
          value={recipientLabel}
        />
        <DetailCard
          icon={BellRing}
          label={labels.subject}
          value={title.trim() || '...'}
        />
        <DetailCard
          icon={MailPlus}
          label={labels.content}
          value={content.trim() || labels.noContent}
        />
        <DetailCard
          icon={BellRing}
          label={labels.type}
          value={type.trim() || labels.noType}
        />
        <DetailCard
          icon={Link2}
          label={labels.link}
          value={link.trim() || labels.noLink}
        />
      </div>
    </div>
  )
}

function NotificationDetail({
  formatDate,
  labels,
  notification,
  recipient,
}: {
  formatDate: (value: string | number | Date) => string
  labels: {
    content: string
    createdAt: string
    link: string
    noContent: string
    noLink: string
    noReadAt: string
    noType: string
    read: string
    readAt: string
    recipient: string
    subject: string
    type: string
    unread: string
    unknownUser: string
  }
  notification: NotificationResponse
  recipient: UserLookup | undefined
}) {
  return (
    <div className="space-y-6">
      <div className="grid gap-4 md:grid-cols-2">
        <DetailCard
          icon={UserRound}
          label={labels.recipient}
          value={recipient?.name ?? labels.unknownUser}
          secondary={recipient?.email ?? notification.userId}
        />
        <DetailCard
          icon={BellRing}
          label={labels.subject}
          value={notification.title}
        />
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <DetailCard
          icon={CalendarDays}
          label={labels.createdAt}
          value={formatDate(notification.createdAt)}
        />
        <DetailCard
          icon={MailPlus}
          label={notification.read ? labels.readAt : labels.read}
          value={
            notification.readAt
              ? formatDate(notification.readAt)
              : notification.read
                ? labels.read
                : labels.noReadAt
          }
          secondary={notification.read ? labels.read : labels.unread}
        />
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <DetailCard
          icon={BellRing}
          label={labels.type}
          value={notification.type ?? labels.noType}
        />
        <DetailCard
          icon={Link2}
          label={labels.link}
          value={notification.link ?? labels.noLink}
        />
      </div>

      <div className="rounded-[22px] border border-border/60 bg-background/55 p-5">
        <p className="text-sm text-muted-foreground">{labels.content}</p>
        <p className="mt-3 whitespace-pre-wrap text-base font-medium text-foreground">
          {notification.content || labels.noContent}
        </p>
      </div>
    </div>
  )
}

function DialogShell({
  canClose = true,
  children,
  onClose,
  title,
}: {
  canClose?: boolean
  children: ReactNode
  onClose: () => void
  title: string
}) {
  return (
    <div className="overflow-hidden rounded-[28px] border border-border/70 bg-card/95 shadow-[0_30px_120px_rgba(2,6,23,0.5)] backdrop-blur">
      <div className="flex items-start justify-between gap-4 border-b border-border/60 px-6 py-5">
        <h2 className="text-2xl font-semibold text-foreground">{title}</h2>
        <Button
          type="button"
          variant="ghost"
          size="icon-sm"
          onClick={onClose}
          className="rounded-2xl"
          disabled={!canClose}
        >
          <X className="h-4 w-4" />
        </Button>
      </div>
      <div className="max-h-[78vh] overflow-y-auto px-6 py-6">{children}</div>
    </div>
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

function DetailCard({
  icon: Icon,
  label,
  value,
  secondary,
}: {
  icon: typeof BellRing
  label: string
  value: string
  secondary?: string
}) {
  return (
    <div className="rounded-[22px] border border-border/60 bg-background/55 p-4">
      <div className="flex items-center gap-2 text-sm text-muted-foreground">
        <Icon className="h-4 w-4" />
        <span>{label}</span>
      </div>
      <p className="mt-3 break-words text-base font-semibold text-foreground">
        {value}
      </p>
      {secondary ? (
        <p className="mt-2 break-all text-xs text-muted-foreground">{secondary}</p>
      ) : null}
    </div>
  )
}

function ReadStatusBadge({
  isRead,
  readLabel,
  unreadLabel,
}: {
  isRead: boolean
  readLabel: string
  unreadLabel: string
}) {
  return (
    <Badge
      variant="outline"
      className={`rounded-2xl px-3 py-1.5 ${
        isRead ? 'text-emerald-500' : 'text-amber-500'
      }`}
    >
      {isRead ? readLabel : unreadLabel}
    </Badge>
  )
}

function getRecipientPreview(recipient: UserLookup | undefined, userId: string) {
  if (recipient) {
    return `${recipient.name} - ${recipient.email}`
  }

  if (userId.trim() !== '') {
    return userId
  }

  return '...'
}
