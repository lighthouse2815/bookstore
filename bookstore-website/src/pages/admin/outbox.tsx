import { useEffect, useState } from 'react'
import { Filter, RefreshCw, Send, X } from 'lucide-react'
import { toast } from 'sonner'
import { AdminLayout } from '@/components/layout/admin-layout'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { useLanguage } from '@/contexts/language-context'
import { getOutboxPage, retryOutboxEvent } from '@/services/outbox-service'
import type { OutboxEvent, OutboxStatus } from '@/types/outbox'
import { getErrorMessage } from '@/utils'

const statuses: OutboxStatus[] = ['PENDING', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'DEAD']

export default function OutboxPage() {
  const { formatDate } = useLanguage()
  const [items, setItems] = useState<OutboxEvent[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(0)
  const [status, setStatus] = useState<OutboxStatus | ''>('')
  const [selected, setSelected] = useState<OutboxEvent | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [retryingId, setRetryingId] = useState<string | null>(null)

  async function load(nextPage = page) {
    setIsLoading(true)
    try {
      const result = await getOutboxPage({ page: nextPage, size: 20, status: status || undefined })
      setItems(result.items); setTotal(result.totalCount); setPage(nextPage)
      if (selected) setSelected(result.items.find((item) => item.id === selected.id) ?? selected)
    } catch (error) { toast.error(getErrorMessage(error, 'Không thể tải transactional outbox.')) }
    finally { setIsLoading(false) }
  }
  useEffect(() => { void load(0) }, [])

  async function retry(item: OutboxEvent) {
    setRetryingId(item.id)
    try {
      const updated = await retryOutboxEvent(item.id)
      setItems((current) => current.map((entry) => entry.id === updated.id ? updated : entry))
      setSelected(updated); toast.success('Đã đưa event DEAD trở lại hàng đợi.')
    } catch (error) { toast.error(getErrorMessage(error, 'Không thể retry outbox event.')) }
    finally { setRetryingId(null) }
  }

  return <AdminLayout><div className="space-y-6">
    <section className="rounded-[28px] border border-border bg-card p-6 shadow-sm">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between"><div className="flex items-start gap-3"><span className="flex size-11 items-center justify-center rounded-2xl bg-primary/10 text-primary"><Send className="size-5" /></span><div><h1 className="font-heading text-3xl font-bold">Transactional outbox</h1><p className="mt-1 text-sm text-muted-foreground">Theo dõi hàng đợi side-effect, lỗi retry và dead-letter.</p></div></div><Badge variant="outline" className="rounded-full px-4 py-2 text-sm">{total} events</Badge></div>
      <div className="mt-6 flex flex-col gap-3 sm:flex-row"><select value={status} onChange={(event) => setStatus(event.currentTarget.value as OutboxStatus | '')} className="h-10 max-w-xs rounded-xl border border-input bg-background px-3 text-sm"><option value="">Tất cả trạng thái</option>{statuses.map((value) => <option key={value} value={value}>{label(value)}</option>)}</select><Button disabled={isLoading} onClick={() => void load(0)}><Filter className="mr-2 size-4" />Áp dụng bộ lọc</Button></div>
    </section>
    <section className="overflow-hidden rounded-[28px] border border-border bg-card shadow-sm"><div className="overflow-x-auto"><table className="min-w-[1040px] w-full text-left text-sm"><thead className="bg-muted/40 text-muted-foreground"><tr><th className="px-5 py-4 font-semibold">Event</th><th className="px-5 py-4 font-semibold">Aggregate</th><th className="px-5 py-4 font-semibold">Tạo lúc</th><th className="px-5 py-4 font-semibold">Retry</th><th className="px-5 py-4 font-semibold">Trạng thái</th><th className="px-5 py-4" /></tr></thead><tbody>{isLoading ? <tr><td colSpan={6} className="px-5 py-12 text-center text-muted-foreground">Đang tải…</td></tr> : items.length === 0 ? <tr><td colSpan={6} className="px-5 py-12 text-center text-muted-foreground">Không có outbox event phù hợp.</td></tr> : items.map((item) => <tr key={item.id} className="border-t border-border/70"><td className="px-5 py-4"><p className="font-semibold">{item.eventType}</p><p className="mt-1 font-mono text-xs text-muted-foreground">{item.id.slice(0, 8)}…</p></td><td className="px-5 py-4"><p>{item.aggregateType}</p><p className="mt-1 font-mono text-xs text-muted-foreground">{item.aggregateId.slice(0, 8)}…</p></td><td className="px-5 py-4 text-muted-foreground">{formatDate(item.createdAt)}</td><td className="px-5 py-4"><p>{item.attemptCount} lần</p><p className="mt-1 text-xs text-muted-foreground">{formatDate(item.nextAttemptAt)}</p></td><td className="px-5 py-4"><StatusBadge status={item.status} /></td><td className="px-5 py-4 text-right"><Button size="sm" variant="outline" onClick={() => setSelected(item)}>Chi tiết</Button></td></tr>)}</tbody></table></div><div className="flex items-center justify-end gap-3 border-t border-border px-5 py-4"><Button size="sm" variant="outline" disabled={page === 0 || isLoading} onClick={() => void load(page - 1)}>Trước</Button><span className="text-sm text-muted-foreground">Trang {page + 1}</span><Button size="sm" variant="outline" disabled={isLoading || items.length < 20} onClick={() => void load(page + 1)}>Sau</Button></div></section>
    {selected ? <aside className="fixed inset-y-0 right-0 z-50 flex w-full max-w-xl flex-col border-l border-border bg-background p-6 shadow-2xl"><div className="flex items-start justify-between gap-4"><div><h2 className="font-heading text-2xl font-bold">Outbox event</h2><p className="mt-1 break-all font-mono text-xs text-muted-foreground">{selected.id}</p></div><Button variant="ghost" size="icon" onClick={() => setSelected(null)}><X className="size-5" /></Button></div><div className="mt-6 space-y-4 overflow-y-auto text-sm"><Detail label="Event type" value={selected.eventType} /><Detail label="Aggregate" value={`${selected.aggregateType} · ${selected.aggregateId}`} /><Detail label="Trạng thái" value={label(selected.status)} /><Detail label="Lần thử" value={String(selected.attemptCount)} /><Detail label="Next attempt" value={formatDate(selected.nextAttemptAt)} /><Detail label="Locked by" value={selected.lockedBy || '—'} /><Detail label="Last error" value={selected.lastError || '—'} /><Detail label="Processed at" value={selected.processedAt ? formatDate(selected.processedAt) : '—'} />{selected.status === 'DEAD' ? <Button className="w-full" disabled={retryingId === selected.id} onClick={() => void retry(selected)}><RefreshCw className="mr-2 size-4" />{retryingId === selected.id ? 'Đang retry…' : 'Retry dead event'}</Button> : null}</div></aside> : null}
  </div></AdminLayout>
}

function StatusBadge({ status }: { status: OutboxStatus }) { const classes = status === 'SUCCEEDED' ? 'border-emerald-200 bg-emerald-50 text-emerald-800' : status === 'DEAD' ? 'border-rose-200 bg-rose-50 text-rose-800' : status === 'FAILED' ? 'border-amber-200 bg-amber-50 text-amber-800' : status === 'PROCESSING' ? 'border-primary/25 bg-primary/10 text-primary' : 'border-slate-200 bg-slate-50 text-slate-700'; return <Badge variant="outline" className={classes}>{label(status)}</Badge> }
function Detail({ label, value }: { label: string; value: string }) { return <div><p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">{label}</p><p className="mt-1 break-words leading-6">{value}</p></div> }
function label(status: OutboxStatus) { return ({ PENDING: 'Chờ xử lý', PROCESSING: 'Đang xử lý', SUCCEEDED: 'Thành công', FAILED: 'Sẽ retry', DEAD: 'Dead-letter' })[status] }
