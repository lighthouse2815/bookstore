import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { CheckCircle2, CircleDollarSign, ExternalLink, Filter, X } from 'lucide-react'
import { toast } from 'sonner'
import { AdminLayout } from '@/components/layout/admin-layout'
import { Badge } from '@/components/common/badge'
import { Button } from '@/components/common/button'
import { Input } from '@/components/common/input'
import { Label } from '@/components/common/label'
import { Textarea } from '@/components/common/textarea'
import { useLanguage } from '@/contexts/language-context'
import {
  getPaymentReconciliationPage,
  resolvePaymentReconciliationIssue,
} from '@/services/payment-reconciliation-service'
import type {
  PaymentReconciliationIssue,
  PaymentReconciliationIssueType,
  PaymentReconciliationStatus,
} from '@/types/payment-reconciliation'
import { getErrorMessage } from '@/utils'

const ISSUE_TYPES: PaymentReconciliationIssueType[] = [
  'PAYMENT_AFTER_EXPIRY',
  'PAYMENT_AFTER_CANCELLATION',
  'AMOUNT_MISMATCH',
  'PAYMENT_WITH_INVALID_ORDER_STATE',
]

const STATUSES: PaymentReconciliationStatus[] = ['OPEN', 'RESOLVED', 'IGNORED']

export default function PaymentReconciliationPage() {
  const { formatCurrency, formatDate } = useLanguage()
  const [issues, setIssues] = useState<PaymentReconciliationIssue[]>([])
  const [totalCount, setTotalCount] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [status, setStatus] = useState<PaymentReconciliationStatus | ''>('OPEN')
  const [issueType, setIssueType] = useState<PaymentReconciliationIssueType | ''>('')
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [selectedIssue, setSelectedIssue] = useState<PaymentReconciliationIssue | null>(null)
  const [resolutionNote, setResolutionNote] = useState('')
  const [isResolving, setIsResolving] = useState(false)

  async function load(nextPage = page) {
    setIsLoading(true)
    try {
      const result = await getPaymentReconciliationPage({
        page: nextPage,
        size: 20,
        status: status || undefined,
        issueType: issueType || undefined,
        from: from || undefined,
        to: to || undefined,
      })
      setIssues(result.items)
      setTotalCount(result.totalCount)
      setPage(nextPage)
    } catch (error) {
      toast.error(getErrorMessage(error, 'Không thể tải danh sách đối soát.'))
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void load(0)
  }, [])

  async function resolveSelectedIssue() {
    if (!selectedIssue || selectedIssue.status !== 'OPEN' || !resolutionNote.trim()) {
      return
    }
    setIsResolving(true)
    try {
      const resolved = await resolvePaymentReconciliationIssue(
        selectedIssue.id,
        resolutionNote,
      )
      setSelectedIssue(resolved)
      setIssues((current) => current.map((issue) => issue.id === resolved.id ? resolved : issue))
      setResolutionNote('')
      toast.success('Đã ghi nhận kết quả đối soát.')
    } catch (error) {
      toast.error(getErrorMessage(error, 'Không thể xử lý vấn đề đối soát.'))
    } finally {
      setIsResolving(false)
    }
  }

  return (
    <AdminLayout>
      <div className="space-y-6">
        <section className="rounded-[28px] border border-border bg-card p-6 shadow-sm">
          <div className="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <div className="flex items-center gap-3">
                <span className="flex size-11 items-center justify-center rounded-2xl bg-primary/10 text-primary">
                  <CircleDollarSign className="size-5" />
                </span>
                <div>
                  <h1 className="font-heading text-3xl font-bold">Đối soát thanh toán</h1>
                  <p className="mt-1 text-sm text-muted-foreground">
                    Theo dõi các giao dịch SePay cần xử lý thủ công, không tự động hoàn tiền.
                  </p>
                </div>
              </div>
            </div>
            <Badge variant="outline" className="w-fit rounded-full px-4 py-2 text-sm">
              {totalCount} vấn đề
            </Badge>
          </div>

          <div className="mt-6 grid gap-3 md:grid-cols-2 xl:grid-cols-5">
            <select
              value={status}
              onChange={(event) => setStatus(event.currentTarget.value as PaymentReconciliationStatus | '')}
              className="h-10 rounded-xl border border-input bg-background px-3 text-sm"
            >
              <option value="">Tất cả trạng thái</option>
              {STATUSES.map((value) => <option key={value} value={value}>{statusLabel(value)}</option>)}
            </select>
            <select
              value={issueType}
              onChange={(event) => setIssueType(event.currentTarget.value as PaymentReconciliationIssueType | '')}
              className="h-10 rounded-xl border border-input bg-background px-3 text-sm"
            >
              <option value="">Tất cả loại vấn đề</option>
              {ISSUE_TYPES.map((value) => <option key={value} value={value}>{issueTypeLabel(value)}</option>)}
            </select>
            <Input type="datetime-local" value={from} onChange={(event) => setFrom(event.currentTarget.value)} />
            <Input type="datetime-local" value={to} onChange={(event) => setTo(event.currentTarget.value)} />
            <Button type="button" onClick={() => void load(0)} disabled={isLoading}>
              <Filter className="mr-2 size-4" />Áp dụng bộ lọc
            </Button>
          </div>
        </section>

        <section className="overflow-hidden rounded-[28px] border border-border bg-card shadow-sm">
          <div className="overflow-x-auto">
            <table className="min-w-[1050px] w-full text-left text-sm">
              <thead className="bg-muted/40 text-muted-foreground">
                <tr>
                  <th className="px-5 py-4 font-semibold">Vấn đề</th>
                  <th className="px-5 py-4 font-semibold">Đơn hàng</th>
                  <th className="px-5 py-4 font-semibold">Số tiền</th>
                  <th className="px-5 py-4 font-semibold">Giao dịch ngoài</th>
                  <th className="px-5 py-4 font-semibold">Phát hiện</th>
                  <th className="px-5 py-4 font-semibold">Trạng thái</th>
                  <th className="px-5 py-4" />
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr><td colSpan={7} className="px-5 py-12 text-center text-muted-foreground">Đang tải dữ liệu…</td></tr>
                ) : issues.length === 0 ? (
                  <tr><td colSpan={7} className="px-5 py-12 text-center text-muted-foreground">Không có vấn đề phù hợp.</td></tr>
                ) : issues.map((issue) => (
                  <tr key={issue.id} className="border-t border-border/70">
                    <td className="px-5 py-4"><p className="font-semibold">{issueTypeLabel(issue.issueType)}</p><p className="mt-1 max-w-xs truncate text-xs text-muted-foreground">{issue.details || '—'}</p></td>
                    <td className="px-5 py-4"><Link to={`/admin/orders?orderId=${issue.orderId}`} className="inline-flex items-center gap-1 font-mono text-xs text-primary hover:underline">{issue.orderId.slice(0, 8)}… <ExternalLink className="size-3" /></Link></td>
                    <td className="px-5 py-4"><p>{formatCurrency(issue.receivedAmount)}</p><p className="text-xs text-muted-foreground">Kỳ vọng {formatCurrency(issue.expectedAmount)}</p></td>
                    <td className="px-5 py-4 font-mono text-xs">{issue.externalTransactionId || '—'}</td>
                    <td className="px-5 py-4 whitespace-nowrap text-muted-foreground">{formatDate(issue.detectedAt)}</td>
                    <td className="px-5 py-4"><StatusBadge status={issue.status} /></td>
                    <td className="px-5 py-4 text-right"><Button size="sm" variant="outline" onClick={() => setSelectedIssue(issue)}>Chi tiết</Button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="flex items-center justify-end gap-3 border-t border-border px-5 py-4">
            <Button variant="outline" size="sm" disabled={page === 0 || isLoading} onClick={() => void load(page - 1)}>Trước</Button>
            <span className="text-sm text-muted-foreground">Trang {page + 1}</span>
            <Button variant="outline" size="sm" disabled={isLoading || issues.length < 20} onClick={() => void load(page + 1)}>Sau</Button>
          </div>
        </section>
      </div>

      {selectedIssue ? (
        <aside className="fixed inset-y-0 right-0 z-50 flex w-full max-w-xl flex-col border-l border-border bg-background p-6 shadow-2xl">
          <div className="flex items-start justify-between gap-4">
            <div><h2 className="font-heading text-2xl font-bold">Chi tiết đối soát</h2><p className="mt-1 text-sm text-muted-foreground">{issueTypeLabel(selectedIssue.issueType)}</p></div>
            <Button variant="ghost" size="icon" onClick={() => setSelectedIssue(null)}><X className="size-5" /></Button>
          </div>
          <div className="mt-6 space-y-4 overflow-y-auto pr-1 text-sm">
            <DetailRow label="Order ID" value={selectedIssue.orderId} mono />
            <DetailRow label="Payment ID" value={selectedIssue.paymentId} mono />
            <DetailRow label="Mã giao dịch ngoài" value={selectedIssue.externalTransactionId || '—'} mono />
            <DetailRow label="Kỳ vọng" value={formatCurrency(selectedIssue.expectedAmount)} />
            <DetailRow label="Đã nhận" value={formatCurrency(selectedIssue.receivedAmount)} />
            <DetailRow label="Phát hiện" value={formatDate(selectedIssue.detectedAt)} />
            <DetailRow label="Nội dung" value={selectedIssue.details || '—'} />
            <div><p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">Trạng thái</p><div className="mt-2"><StatusBadge status={selectedIssue.status} /></div></div>
            {selectedIssue.resolutionNote ? <DetailRow label="Ghi chú xử lý" value={selectedIssue.resolutionNote} /> : null}
            {selectedIssue.status === 'OPEN' ? (
              <div className="rounded-2xl border border-primary/15 bg-primary/5 p-4">
                <Label htmlFor="reconciliation-note">Ghi chú xử lý</Label>
                <Textarea id="reconciliation-note" className="mt-2" value={resolutionNote} onChange={(event) => setResolutionNote(event.currentTarget.value)} maxLength={1000} placeholder="Mô tả cách xử lý giao dịch này" disabled={isResolving} />
                <Button className="mt-3 w-full" onClick={() => void resolveSelectedIssue()} disabled={!resolutionNote.trim() || isResolving}><CheckCircle2 className="mr-2 size-4" />{isResolving ? 'Đang lưu…' : 'Đánh dấu đã xử lý'}</Button>
              </div>
            ) : null}
          </div>
        </aside>
      ) : null}
    </AdminLayout>
  )
}

function DetailRow({ label, value, mono = false }: { label: string; value: string; mono?: boolean }) {
  return <div><p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">{label}</p><p className={mono ? 'mt-1 break-all font-mono text-xs' : 'mt-1 leading-6'}>{value}</p></div>
}

function StatusBadge({ status }: { status: PaymentReconciliationStatus }) {
  const classes = status === 'OPEN' ? 'border-amber-200 bg-amber-50 text-amber-800' : status === 'RESOLVED' ? 'border-emerald-200 bg-emerald-50 text-emerald-800' : 'border-slate-200 bg-slate-50 text-slate-700'
  return <Badge variant="outline" className={classes}>{statusLabel(status)}</Badge>
}

function statusLabel(status: PaymentReconciliationStatus) {
  return status === 'OPEN' ? 'Đang mở' : status === 'RESOLVED' ? 'Đã xử lý' : 'Đã bỏ qua'
}

function issueTypeLabel(type: PaymentReconciliationIssueType) {
  return {
    PAYMENT_AFTER_EXPIRY: 'Thanh toán sau khi hết hạn',
    PAYMENT_AFTER_CANCELLATION: 'Thanh toán sau khi hủy đơn',
    AMOUNT_MISMATCH: 'Số tiền không khớp',
    PAYMENT_WITH_INVALID_ORDER_STATE: 'Trạng thái đơn không hợp lệ',
  }[type]
}
