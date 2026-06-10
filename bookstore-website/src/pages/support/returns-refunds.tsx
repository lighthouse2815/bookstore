import { CheckCircle2, PackageSearch, ReceiptText, RotateCcw } from 'lucide-react'
import { useLanguage } from '@/contexts/language-context'
import { SupportPageShell, SupportSection } from './support-page-shell'

export default function ReturnsRefundsPage() {
  const { language } = useLanguage()

  const content =
    language === 'en'
      ? {
          badge: 'Returns & refunds',
          title: 'Clear rules for exchanges and refunds',
          description:
            'If a book arrives damaged, incorrect, or incomplete, we support fast resolution with a straightforward review flow.',
          highlights: [
            { label: 'Request window', value: 'Within 7 days' },
            { label: 'Response', value: 'Within 24 business hours' },
            { label: 'Refund method', value: 'Original payment method' },
          ],
          sections: [
            {
              title: 'Eligible cases',
              description:
                'We support exchanges or refunds when the issue is confirmed from packaging, picking, or transport.',
              items: [
                'Wrong title, wrong edition, or missing quantity.',
                'Books damaged during shipping or with major printing defects.',
                'Products significantly different from the confirmed order.',
              ],
            },
            {
              title: 'Request procedure',
              description:
                'Please contact support with enough evidence so the case can be reviewed quickly.',
              items: [
                'Provide order code, issue description, and photos of the parcel.',
                'Do not discard the invoice, seal, or original packaging too early.',
                'Our team confirms the solution and return direction after review.',
              ],
            },
            {
              title: 'Refund timeline',
              description:
                'After the request is approved, exchange or refund timing depends on carrier transit and payment channel.',
              items: [
                'Exchange shipment is prepared after the original parcel is verified.',
                'Refunds to bank or e-wallet accounts may take 3-7 business days.',
                'For COD orders, we confirm refund account details before transfer.',
              ],
            },
          ],
        }
      : {
          badge: 'Đổi trả & hoàn tiền',
          title: 'Quy định rõ ràng cho đổi trả và hoàn tiền',
          description:
            'Nếu sách giao sai, hư hỏng hoặc thiếu số lượng, chúng tôi hỗ trợ xử lý nhanh với quy trình rà soát minh bạch.',
          highlights: [
            { label: 'Thời hạn yêu cầu', value: 'Trong vòng 7 ngày' },
            { label: 'Phản hồi', value: 'Trong 24 giờ làm việc' },
            { label: 'Hoàn tiền', value: 'Về phương thức thanh toán ban đầu' },
          ],
          sections: [
            {
              title: 'Trường hợp được hỗ trợ',
              description:
                'Chúng tôi hỗ trợ đổi hoặc hoàn tiền khi lỗi được xác nhận từ khâu đóng gói, soạn đơn hoặc vận chuyển.',
              items: [
                'Giao sai tên sách, sai phiên bản hoặc thiếu số lượng.',
                'Sách hư hỏng trong quá trình vận chuyển hoặc lỗi in ấn nghiêm trọng.',
                'Sản phẩm khác biệt đáng kể so với đơn hàng đã xác nhận.',
              ],
            },
            {
              title: 'Quy trình gửi yêu cầu',
              description:
                'Vui lòng liên hệ bộ phận hỗ trợ kèm đủ bằng chứng để việc xử lý diễn ra nhanh hơn.',
              items: [
                'Cung cấp mã đơn, mô tả lỗi và hình ảnh kiện hàng/sản phẩm.',
                'Không vứt bỏ hóa đơn, tem niêm phong hoặc bao bì gốc quá sớm.',
                'Đội ngũ hỗ trợ sẽ xác nhận hướng xử lý sau khi rà soát.',
              ],
            },
            {
              title: 'Thời gian hoàn tiền',
              description:
                'Sau khi yêu cầu được duyệt, tốc độ đổi hàng hoặc hoàn tiền phụ thuộc đơn vị vận chuyển và kênh thanh toán.',
              items: [
                'Đơn đổi hàng được chuẩn bị sau khi kiện cũ được xác minh.',
                'Hoàn tiền qua ngân hàng hoặc ví điện tử có thể mất 3-7 ngày làm việc.',
                'Với đơn COD, chúng tôi xác nhận tài khoản nhận tiền trước khi chuyển khoản.',
              ],
            },
          ],
        }

  return (
    <SupportPageShell
      icon={RotateCcw}
      badge={content.badge}
      title={content.title}
      description={content.description}
      highlights={content.highlights}
    >
      {content.sections.map((section, index) => (
        <SupportSection
          key={section.title}
          title={section.title}
          description={section.description}
        >
          <div className="grid gap-4 md:grid-cols-3">
            {section.items.map((item) => {
              const Icon =
                [CheckCircle2, PackageSearch, ReceiptText][index] ?? CheckCircle2

              return (
                <div
                  key={item}
                  className="rounded-[22px] border border-border/60 bg-background/45 p-5"
                >
                  <Icon className="size-5 text-primary" />
                  <p className="mt-4 text-sm leading-7 text-muted-foreground">
                    {item}
                  </p>
                </div>
              )
            })}
          </div>
        </SupportSection>
      ))}
    </SupportPageShell>
  )
}
