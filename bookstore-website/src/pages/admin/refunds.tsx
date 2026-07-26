import { useEffect, useState, type ReactNode } from 'react'
import { ExternalLink, FileCheck2, Filter, Landmark, Plus, RefreshCw, X } from 'lucide-react'
import { Link } from 'react-router-dom'
import { toast } from 'sonner'
import { AdminLayout } from '@/components/layout/admin-layout'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { Textarea } from '@/components/common/textarea'
import { useLanguage } from '@/contexts/language-context'
import { approveRefund, cancelRefund, createRefund, failRefund, getRefundPage, processRefund, succeedRefund } from '@/services/refund-service'
import type { Refund, RefundMethod, RefundStatus } from '@/types/refund'
import { getErrorMessage } from '@/utils'

const statuses: RefundStatus[] = ['REQUESTED', 'APPROVED', 'PROCESSING', 'SUCCEEDED', 'FAILED', 'CANCELLED']
const methods: RefundMethod[] = ['MANUAL_BANK_TRANSFER', 'ORIGINAL_PAYMENT_METHOD', 'CASH']
const createDefault = { orderId: '', returnRequestId: '', amount: '', reason: '', method: 'MANUAL_BANK_TRANSFER' as RefundMethod }

export default function RefundsPage() {
  const { formatCurrency, formatDate } = useLanguage()
  const [items, setItems] = useState<Refund[]>([])
  const [total, setTotal] = useState(0)
  const [page, setPage] = useState(0)
  const [status, setStatus] = useState<RefundStatus | ''>('')
  const [method, setMethod] = useState<RefundMethod | ''>('')
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [isLoading, setIsLoading] = useState(true)
  const [selected, setSelected] = useState<Refund | null>(null)
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [form, setForm] = useState(createDefault)
  const [isSaving, setIsSaving] = useState(false)
  const [reference, setReference] = useState('')
  const [evidenceUrl, setEvidenceUrl] = useState('')
  const [evidenceMetadata, setEvidenceMetadata] = useState('')
  const [failureReason, setFailureReason] = useState('')

  async function load(nextPage = page) {
    setIsLoading(true)
    try {
      const result = await getRefundPage({ page: nextPage, size: 20, status: status || undefined, method: method || undefined, from: from || undefined, to: to || undefined })
      setItems(result.items); setTotal(result.totalCount); setPage(nextPage)
      if (selected) setSelected(result.items.find((item) => item.id === selected.id) ?? selected)
    } catch (error) { toast.error(getErrorMessage(error, 'Không thể tải danh sách hoàn tiền.')) }
    finally { setIsLoading(false) }
  }
  useEffect(() => { void load(0) }, [])
  function update(item: Refund) { setItems((current) => current.map((entry) => entry.id === item.id ? item : entry)); setSelected(item) }
  async function execute(action: () => Promise<Refund>, success: string) {
    setIsSaving(true)
    try { update(await action()); toast.success(success) } catch (error) { toast.error(getErrorMessage(error, 'Không thể cập nhật hoàn tiền.')) } finally { setIsSaving(false) }
  }
  async function submitCreate() {
    const amount = Number(form.amount)
    if (!form.orderId.trim() || !form.reason.trim() || !Number.isFinite(amount) || amount <= 0) return
    setIsSaving(true)
    try {
      const refund = await createRefund(form.orderId.trim(), { returnRequestId: form.returnRequestId.trim() || undefined, amount, currency: 'VND', reason: form.reason, method: form.method })
      setItems((current) => [refund, ...current]); setTotal((value) => value + 1); setSelected(refund); setForm(createDefault); setIsCreateOpen(false); toast.success('Đã tạo yêu cầu hoàn tiền.')
    } catch (error) { toast.error(getErrorMessage(error, 'Không thể tạo yêu cầu hoàn tiền.')) } finally { setIsSaving(false) }
  }

  return <AdminLayout><div className="space-y-6">
    <section className="rounded-[28px] border border-border bg-card p-6 shadow-sm">
      <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between"><div className="flex items-start gap-3"><span className="flex size-11 items-center justify-center rounded-2xl bg-primary/10 text-primary"><Landmark className="size-5" /></span><div><h1 className="font-heading text-3xl font-bold">Sổ cái hoàn tiền</h1><p className="mt-1 text-sm text-muted-foreground">Phê duyệt, ghi nhận bằng chứng và đối soát hoàn tiền thủ công.</p></div></div><div className="flex gap-2"><Badge variant="outline" className="rounded-full px-4 py-2 text-sm">{total} giao dịch</Badge><Button onClick={() => setIsCreateOpen(true)}><Plus className="mr-2 size-4" />Tạo hoàn tiền</Button></div></div>
      <div className="mt-6 grid gap-3 md:grid-cols-2 xl:grid-cols-5"><select value={status} onChange={(event) => setStatus(event.currentTarget.value as RefundStatus | '')} className="h-10 rounded-xl border border-input bg-background px-3 text-sm"><option value="">Tất cả trạng thái</option>{statuses.map((value) => <option key={value} value={value}>{statusLabel(value)}</option>)}</select><select value={method} onChange={(event) => setMethod(event.currentTarget.value as RefundMethod | '')} className="h-10 rounded-xl border border-input bg-background px-3 text-sm"><option value="">Tất cả phương thức</option>{methods.map((value) => <option key={value} value={value}>{methodLabel(value)}</option>)}</select><Input type="datetime-local" value={from} onChange={(event) => setFrom(event.currentTarget.value)} /><Input type="datetime-local" value={to} onChange={(event) => setTo(event.currentTarget.value)} /><Button disabled={isLoading} onClick={() => void load(0)}><Filter className="mr-2 size-4" />Áp dụng bộ lọc</Button></div>
    </section>
    <section className="overflow-hidden rounded-[28px] border border-border bg-card shadow-sm"><div className="overflow-x-auto"><table className="min-w-[1080px] w-full text-left text-sm"><thead className="bg-muted/40 text-muted-foreground"><tr><th className="px-5 py-4 font-semibold">Hoàn tiền</th><th className="px-5 py-4 font-semibold">Đơn hàng</th><th className="px-5 py-4 font-semibold">Số tiền</th><th className="px-5 py-4 font-semibold">Phương thức</th><th className="px-5 py-4 font-semibold">Yêu cầu lúc</th><th className="px-5 py-4 font-semibold">Trạng thái</th><th className="px-5 py-4" /></tr></thead><tbody>{isLoading ? <tr><td colSpan={7} className="px-5 py-12 text-center text-muted-foreground">Đang tải dữ liệu…</td></tr> : items.length === 0 ? <tr><td colSpan={7} className="px-5 py-12 text-center text-muted-foreground">Không có hoàn tiền phù hợp.</td></tr> : items.map((item) => <tr key={item.id} className="border-t border-border/70"><td className="px-5 py-4"><p className="font-semibold">{item.id.slice(0, 8)}…</p><p className="mt-1 max-w-xs truncate text-xs text-muted-foreground">{item.reason}</p></td><td className="px-5 py-4"><Link to={`/admin/orders?orderId=${item.orderId}`} className="inline-flex items-center gap-1 font-mono text-xs text-primary hover:underline">{item.orderCode} <ExternalLink className="size-3" /></Link></td><td className="px-5 py-4"><p>{formatCurrency(item.amount)}</p><p className="text-xs text-muted-foreground">Đã thu {formatCurrency(item.paidAmount)}</p></td><td className="px-5 py-4">{methodLabel(item.method)}</td><td className="px-5 py-4 whitespace-nowrap text-muted-foreground">{formatDate(item.requestedAt)}</td><td className="px-5 py-4"><StatusBadge status={item.status} /></td><td className="px-5 py-4 text-right"><Button size="sm" variant="outline" onClick={() => setSelected(item)}>Chi tiết</Button></td></tr>)}</tbody></table></div><div className="flex items-center justify-end gap-3 border-t border-border px-5 py-4"><Button variant="outline" size="sm" disabled={page === 0 || isLoading} onClick={() => void load(page - 1)}>Trước</Button><span className="text-sm text-muted-foreground">Trang {page + 1}</span><Button variant="outline" size="sm" disabled={isLoading || items.length < 20} onClick={() => void load(page + 1)}>Sau</Button></div></section>
    {selected ? <aside className="fixed inset-y-0 right-0 z-50 flex w-full max-w-xl flex-col border-l border-border bg-background p-6 shadow-2xl"><div className="flex items-start justify-between gap-4"><div><h2 className="font-heading text-2xl font-bold">Chi tiết hoàn tiền</h2><p className="mt-1 font-mono text-xs text-muted-foreground">{selected.id}</p></div><Button variant="ghost" size="icon" onClick={() => setSelected(null)}><X className="size-5" /></Button></div><div className="mt-6 space-y-4 overflow-y-auto pr-1 text-sm"><Detail label="Đơn hàng" value={selected.orderCode} /><Detail label="Thanh toán" value={`${selected.paymentProvider} · ${selected.paymentStatus}`} /><Detail label="Số tiền" value={`${formatCurrency(selected.amount)} / ${formatCurrency(selected.paidAmount)}`} /><Detail label="Lý do" value={selected.reason} /><Detail label="Mã đối soát" value={selected.externalReference || '—'} mono /><Detail label="Evidence URL" value={selected.evidenceUrl || '—'} /><Detail label="Evidence metadata" value={selected.evidenceMetadata || '—'} /><Detail label="Lý do thất bại" value={selected.failureReason || '—'} /><div><p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">Trạng thái</p><div className="mt-2"><StatusBadge status={selected.status} /></div></div><RefundActions refund={selected} isSaving={isSaving} reference={reference} evidenceUrl={evidenceUrl} evidenceMetadata={evidenceMetadata} failureReason={failureReason} setReference={setReference} setEvidenceUrl={setEvidenceUrl} setEvidenceMetadata={setEvidenceMetadata} setFailureReason={setFailureReason} onApprove={() => void execute(() => approveRefund(selected.id), 'Đã phê duyệt hoàn tiền.')} onProcessing={() => void execute(() => processRefund(selected.id), 'Đã chuyển sang đang xử lý.')} onSucceed={() => void execute(() => succeedRefund(selected.id, { externalReference: reference, evidenceUrl: evidenceUrl || undefined, evidenceMetadata: evidenceMetadata || undefined }), 'Đã ghi nhận hoàn tiền thành công.')} onFail={() => void execute(() => failRefund(selected.id, failureReason), 'Đã ghi nhận hoàn tiền thất bại.')} onCancel={() => void execute(() => cancelRefund(selected.id), 'Đã hủy yêu cầu hoàn tiền.')} /></div></aside> : null}
    {isCreateOpen ? <aside className="fixed inset-y-0 right-0 z-50 flex w-full max-w-lg flex-col border-l border-border bg-background p-6 shadow-2xl"><div className="flex items-start justify-between"><div><h2 className="font-heading text-2xl font-bold">Tạo hoàn tiền</h2><p className="mt-1 text-sm text-muted-foreground">Đơn phải có thanh toán PAID.</p></div><Button variant="ghost" size="icon" onClick={() => setIsCreateOpen(false)}><X className="size-5" /></Button></div><div className="mt-6 space-y-4"><Field label="Order ID"><Input value={form.orderId} onChange={(event) => setForm({ ...form, orderId: event.currentTarget.value })} /></Field><Field label="Return request ID (nếu có)"><Input value={form.returnRequestId} onChange={(event) => setForm({ ...form, returnRequestId: event.currentTarget.value })} /></Field><Field label="Số tiền VND"><Input type="number" min="1" value={form.amount} onChange={(event) => setForm({ ...form, amount: event.currentTarget.value })} /></Field><Field label="Phương thức"><select value={form.method} onChange={(event) => setForm({ ...form, method: event.currentTarget.value as RefundMethod })} className="h-10 w-full rounded-xl border border-input bg-background px-3 text-sm">{methods.map((value) => <option key={value} value={value}>{methodLabel(value)}</option>)}</select></Field><Field label="Lý do"><Textarea value={form.reason} maxLength={1000} onChange={(event) => setForm({ ...form, reason: event.currentTarget.value })} /></Field><Button className="w-full" disabled={isSaving} onClick={() => void submitCreate()}><FileCheck2 className="mr-2 size-4" />{isSaving ? 'Đang lưu…' : 'Tạo yêu cầu hoàn tiền'}</Button></div></aside> : null}
  </div></AdminLayout>
}

function RefundActions(props: { refund: Refund; isSaving: boolean; reference: string; evidenceUrl: string; evidenceMetadata: string; failureReason: string; setReference: (value: string) => void; setEvidenceUrl: (value: string) => void; setEvidenceMetadata: (value: string) => void; setFailureReason: (value: string) => void; onApprove: () => void; onProcessing: () => void; onSucceed: () => void; onFail: () => void; onCancel: () => void }) {
  const { refund } = props
  if (refund.status === 'REQUESTED') return <div className="flex gap-2"><Button className="flex-1" disabled={props.isSaving} onClick={props.onApprove}>Phê duyệt</Button><Button className="flex-1" variant="outline" disabled={props.isSaving} onClick={props.onCancel}>Hủy</Button></div>
  if (refund.status === 'APPROVED' || refund.status === 'FAILED') return <div className="space-y-3">{refund.status === 'FAILED' ? <p className="rounded-xl bg-amber-50 p-3 text-xs text-amber-800">Retry sẽ chuyển lại sang trạng thái đang xử lý.</p> : null}<Button className="w-full" disabled={props.isSaving} onClick={props.onProcessing}><RefreshCw className="mr-2 size-4" />Bắt đầu xử lý</Button><Button className="w-full" variant="outline" disabled={props.isSaving} onClick={props.onCancel}>Hủy yêu cầu</Button></div>
  if (refund.status !== 'PROCESSING') return null
  return <div className="space-y-3 rounded-2xl border border-primary/15 bg-primary/5 p-4"><Field label="Mã đối soát"><Input value={props.reference} onChange={(event) => props.setReference(event.currentTarget.value)} /></Field><Field label="Evidence URL"><Input value={props.evidenceUrl} onChange={(event) => props.setEvidenceUrl(event.currentTarget.value)} /></Field><Field label="Evidence metadata"><Textarea value={props.evidenceMetadata} onChange={(event) => props.setEvidenceMetadata(event.currentTarget.value)} /></Field><Button className="w-full" disabled={props.isSaving || !props.reference.trim() || (!props.evidenceUrl.trim() && !props.evidenceMetadata.trim())} onClick={props.onSucceed}>Ghi nhận thành công</Button><Field label="Lý do thất bại"><Textarea value={props.failureReason} onChange={(event) => props.setFailureReason(event.currentTarget.value)} /></Field><Button className="w-full" variant="outline" disabled={props.isSaving || !props.failureReason.trim()} onClick={props.onFail}>Ghi nhận thất bại</Button></div>
}
function Field({ label, children }: { label: string; children: ReactNode }) { return <div><Label>{label}</Label><div className="mt-2">{children}</div></div> }
function Detail({ label, value, mono = false }: { label: string; value: string; mono?: boolean }) { return <div><p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">{label}</p><p className={mono ? 'mt-1 break-all font-mono text-xs' : 'mt-1 break-words leading-6'}>{value}</p></div> }
function StatusBadge({ status }: { status: RefundStatus }) { const classes = status === 'SUCCEEDED' ? 'border-emerald-200 bg-emerald-50 text-emerald-800' : status === 'FAILED' ? 'border-rose-200 bg-rose-50 text-rose-800' : status === 'CANCELLED' ? 'border-slate-200 bg-slate-50 text-slate-700' : status === 'PROCESSING' ? 'border-primary/25 bg-primary/10 text-primary' : 'border-amber-200 bg-amber-50 text-amber-800'; return <Badge variant="outline" className={classes}>{statusLabel(status)}</Badge> }
function statusLabel(value: RefundStatus) { return ({ REQUESTED: 'Chờ duyệt', APPROVED: 'Đã duyệt', PROCESSING: 'Đang xử lý', SUCCEEDED: 'Thành công', FAILED: 'Thất bại', CANCELLED: 'Đã hủy' })[value] }
function methodLabel(value: RefundMethod) { return ({ MANUAL_BANK_TRANSFER: 'Chuyển khoản thủ công', ORIGINAL_PAYMENT_METHOD: 'Phương thức gốc', CASH: 'Tiền mặt' })[value] }
