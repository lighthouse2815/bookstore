import { HelpCircle } from 'lucide-react'
import { useLanguage } from '@/contexts/language-context'
import { SupportPageShell, SupportSection } from './support-page-shell'

export default function FaqPage() {
  const { language } = useLanguage()

  const content =
    language === 'en'
      ? {
          badge: 'FAQ',
          title: 'Answers to common bookstore questions',
          description:
            'Here are the questions customers usually ask before ordering, paying, tracking shipments, or requesting support.',
          highlights: [
            { label: 'Order support', value: 'During business hours daily' },
            { label: 'Payment options', value: 'COD, transfer, e-wallets' },
            { label: 'Help channel', value: 'Email and hotline support' },
          ],
          groups: [
            {
              title: 'Orders and payment',
              items: [
                {
                  question: 'Can I place an order without an account?',
                  answer:
                    'At the moment, the checkout flow works best when you sign in so your cart, shipping information, and order history stay synchronized.',
                },
                {
                  question: 'Which payment methods are supported?',
                  answer:
                    'We currently support cash on delivery, bank transfer, and connected payment gateways depending on the checkout configuration.',
                },
              ],
            },
            {
              title: 'Shipping and tracking',
              items: [
                {
                  question: 'How do I know where my parcel is?',
                  answer:
                    'Once the order is confirmed and handed to the carrier, tracking updates are shared through the order status flow and support channels.',
                },
                {
                  question: 'What happens if I miss the first delivery attempt?',
                  answer:
                    'The carrier usually contacts you again or reschedules based on local policy. If the order is returned, our support team will guide the next step.',
                },
              ],
            },
            {
              title: 'Products and after-sales support',
              items: [
                {
                  question: 'Do you sell genuine books only?',
                  answer:
                    'Yes. The online bookstore is intended for official books with clear publisher references and carefully managed product information.',
                },
                {
                  question: 'How do I request an exchange or refund?',
                  answer:
                    'Please prepare your order code, issue details, and photos if needed, then contact support through the contact page or the channels listed there.',
                },
              ],
            },
          ],
        }
      : {
          badge: 'Câu hỏi thường gặp',
          title: 'Giải đáp những câu hỏi thường gặp khi mua sách',
          description:
            'Đây là các thắc mắc phổ biến trước khi đặt hàng, thanh toán, theo dõi vận chuyển hoặc cần hỗ trợ sau mua.',
          highlights: [
            { label: 'Hỗ trợ đơn hàng', value: 'Trong giờ làm việc mỗi ngày' },
            { label: 'Thanh toán', value: 'COD, chuyển khoản, ví điện tử' },
            { label: 'Kênh hỗ trợ', value: 'Email và hotline' },
          ],
          groups: [
            {
              title: 'Đặt hàng và thanh toán',
              items: [
                {
                  question: 'Tôi có thể đặt hàng mà không cần tài khoản không?',
                  answer:
                    'Hiện tại luồng thanh toán hoạt động ổn định nhất khi bạn đăng nhập để giỏ hàng, địa chỉ nhận và lịch sử đơn hàng được đồng bộ.',
                },
                {
                  question: 'Shop hỗ trợ những phương thức thanh toán nào?',
                  answer:
                    'Hệ thống hiện hỗ trợ thanh toán khi nhận hàng, chuyển khoản ngân hàng và các cổng thanh toán được bật ở bước checkout.',
                },
              ],
            },
            {
              title: 'Giao hàng và theo dõi đơn',
              items: [
                {
                  question: 'Làm sao để biết đơn hàng đang ở đâu?',
                  answer:
                    'Sau khi đơn được xác nhận và bàn giao vận chuyển, trạng thái theo dõi sẽ được cập nhật qua luồng đơn hàng và các kênh hỗ trợ.',
                },
                {
                  question: 'Nếu tôi lỡ cuộc gọi giao hàng đầu tiên thì sao?',
                  answer:
                    'Đơn vị giao nhận thường sẽ liên hệ lại hoặc hẹn giao lại theo chính sách khu vực. Nếu hàng bị hoàn, bộ phận hỗ trợ sẽ hướng dẫn bước tiếp theo.',
                },
              ],
            },
            {
              title: 'Sản phẩm và hậu mãi',
              items: [
                {
                  question: 'Website có bán sách chính hãng không?',
                  answer:
                    'Có. Cửa hàng được xây dựng cho các đầu sách có nguồn gốc rõ ràng, tham chiếu nhà xuất bản đầy đủ và dữ liệu danh mục được kiểm soát.',
                },
                {
                  question: 'Tôi cần làm gì để yêu cầu đổi trả?',
                  answer:
                    'Hãy chuẩn bị mã đơn, mô tả vấn đề và hình ảnh liên quan nếu có, sau đó liên hệ qua trang Liên hệ hoặc các kênh hỗ trợ được công khai ở đó.',
                },
              ],
            },
          ],
        }

  return (
    <SupportPageShell
      icon={HelpCircle}
      badge={content.badge}
      title={content.title}
      description={content.description}
      highlights={content.highlights}
    >
      {content.groups.map((group) => (
        <SupportSection key={group.title} title={group.title}>
          <div className="space-y-4">
            {group.items.map((item) => (
              <details
                key={item.question}
                className="group rounded-[22px] border border-border/60 bg-background/45 p-5"
              >
                <summary className="cursor-pointer list-none text-base font-semibold text-foreground">
                  {item.question}
                </summary>
                <p className="mt-4 text-sm leading-7 text-muted-foreground">
                  {item.answer}
                </p>
              </details>
            ))}
          </div>
        </SupportSection>
      ))}
    </SupportPageShell>
  )
}
