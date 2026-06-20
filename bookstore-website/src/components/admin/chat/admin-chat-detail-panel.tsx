import { Button } from '@/components/common/button'
import type { AdminChatLabels } from '@/hooks/use-admin-chat-page'
import type { AdminUserResponse } from '@/types/admin-access'
import type { ConversationResponse } from '@/types/chat'

type AdminChatDetailPanelProps = {
  labels: AdminChatLabels
  conversation: ConversationResponse | null
  employeeOptions: AdminUserResponse[]
  pendingAssigneeId: string
  currentUserId: string
  locale: string
  canAssignEmployees: boolean
  isAssigning: boolean
  onPendingAssigneeIdChange: (value: string) => void
  onAssign: () => Promise<void> | void
  onAssignToSelf: () => Promise<void> | void
}

export function AdminChatDetailPanel({
  labels,
  conversation,
  employeeOptions,
  pendingAssigneeId,
  currentUserId,
  locale,
  canAssignEmployees,
  isAssigning,
  onPendingAssigneeIdChange,
  onAssign,
  onAssignToSelf,
}: AdminChatDetailPanelProps) {
  const formatter = new Intl.DateTimeFormat(locale, {
    dateStyle: 'medium',
    timeStyle: 'short',
  })

  if (!conversation) {
    return (
      <section className="flex min-h-[44rem] items-center justify-center rounded-[30px] border border-dashed border-border/70 bg-card/70 p-8 text-center text-muted-foreground">
        <p>{labels.noConversationSelected}</p>
      </section>
    )
  }

  return (
    <section className="flex min-h-[44rem] flex-col overflow-hidden rounded-[30px] border border-border/60 bg-card/90 shadow-[0_24px_80px_rgba(2,6,23,0.18)]">
      <div className="border-b border-border/60 px-5 py-5">
        <h2 className="text-lg font-semibold text-foreground">{labels.assignee}</h2>
      </div>

      <div className="space-y-4 px-5 py-5">
        <DetailCard
          label={labels.customer}
          value={conversation.customerName || conversation.customerEmail || conversation.customerId}
          secondary={conversation.customerEmail || conversation.customerId}
        />
        <DetailCard
          label={labels.assignee}
          value={conversation.assignedStaffName || labels.unassigned}
          secondary={conversation.assignedStaffEmail || conversation.assignedStaffId || undefined}
        />
        <DetailCard label={labels.priority} value={conversation.priority} />
        <DetailCard
          label={labels.target}
          value={conversation.targetType}
          secondary={conversation.targetId || undefined}
        />
        <DetailCard
          label={labels.createdAt}
          value={formatter.format(new Date(conversation.createdAt))}
        />
        <DetailCard
          label={labels.updatedAt}
          value={formatter.format(new Date(conversation.updatedAt))}
        />

        {canAssignEmployees ? (
          <div className="rounded-[24px] border border-border/60 bg-background/60 p-4">
            <p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">
              {labels.assignee}
            </p>
            <select
              value={pendingAssigneeId}
              onChange={(event) => onPendingAssigneeIdChange(event.currentTarget.value)}
              className="mt-3 h-11 w-full rounded-2xl border border-input bg-background px-3 text-sm text-foreground"
            >
              <option value="">{labels.staffPlaceholder}</option>
              {employeeOptions.map((employee) => (
                <option key={employee.userId} value={employee.userId}>
                  {employee.username} - {employee.email}
                </option>
              ))}
            </select>
            <Button
              type="button"
              className="mt-3 w-full rounded-2xl"
              disabled={isAssigning || pendingAssigneeId.trim() === ''}
              onClick={() => void onAssign()}
            >
              {labels.assignButton}
            </Button>
          </div>
        ) : conversation.assignedStaffId !== currentUserId ? (
          <Button
            type="button"
            className="w-full rounded-2xl"
            disabled={isAssigning}
            onClick={() => void onAssignToSelf()}
          >
            {labels.assignToSelf}
          </Button>
        ) : null}
      </div>
    </section>
  )
}

function DetailCard({
  label,
  value,
  secondary,
}: {
  label: string
  value: string
  secondary?: string
}) {
  return (
    <div className="rounded-[24px] border border-border/60 bg-background/60 p-4">
      <p className="text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">
        {label}
      </p>
      <p className="mt-2 break-words text-base font-semibold text-foreground">
        {value}
      </p>
      {secondary ? (
        <p className="mt-1 break-all text-xs text-muted-foreground">{secondary}</p>
      ) : null}
    </div>
  )
}
