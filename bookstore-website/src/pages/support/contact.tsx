import { Clock3, Mail, MapPin, PhoneCall } from 'lucide-react'
import { useLanguage } from '@/contexts/language-context'
import { SupportPageShell, SupportSection } from './support-page-shell'

export default function ContactPage() {
  const { language } = useLanguage()

  const content =
    language === 'en'
      ? {
          badge: 'Contact',
          title: 'Reach the bookstore support team',
          description:
            'If you need help with an order, delivery issue, account problem, or partnership request, contact us through the channels below.',
          highlights: [
            { label: 'Hotline', value: '1900 6868' },
            { label: 'Email', value: 'support@sachvui.vn' },
            { label: 'Working hours', value: '8:00 - 18:00 daily' },
          ],
          cards: [
            {
              icon: PhoneCall,
              title: 'Hotline',
              value: '1900 6868',
              description: 'Best for urgent support about orders and delivery.',
            },
            {
              icon: Mail,
              title: 'Email',
              value: 'support@sachvui.vn',
              description: 'Best for exchange, refund, and account review requests.',
            },
            {
              icon: MapPin,
              title: 'Office',
              value: '12 Nguyen Van Bao, Ho Chi Minh City',
              description: 'Administrative office and order support desk.',
            },
            {
              icon: Clock3,
              title: 'Working hours',
              value: '8:00 - 18:00',
              description: 'Support is available every day, including weekends.',
            },
          ],
          noteTitle: 'Before you contact support',
          noteDescription:
            'Prepare your order code, phone number, and issue summary so the support team can check the case faster.',
        }
      : {
          badge: 'Liên hệ',
          title: 'Liên hệ đội ngũ hỗ trợ của nhà sách',
          description:
            'Nếu bạn cần hỗ trợ đơn hàng, sự cố giao vận, tài khoản hoặc nhu cầu hợp tác, hãy liên hệ qua các kênh bên dưới.',
          highlights: [
            { label: 'Hotline', value: '1900 6868' },
            { label: 'Email', value: 'support@sachvui.vn' },
            { label: 'Giờ làm việc', value: '8:00 - 18:00 mỗi ngày' },
          ],
          cards: [
            {
              icon: PhoneCall,
              title: 'Hotline',
              value: '1900 6868',
              description: 'Phù hợp khi cần hỗ trợ gấp về đơn hàng và giao nhận.',
            },
            {
              icon: Mail,
              title: 'Email',
              value: 'support@sachvui.vn',
              description: 'Phù hợp cho các yêu cầu đổi trả, hoàn tiền và rà soát tài khoản.',
            },
            {
              icon: MapPin,
              title: 'Văn phòng',
              value: '12 Nguyễn Văn Bảo, TP. Hồ Chí Minh',
              description: 'Điểm làm việc hành chính và tiếp nhận hỗ trợ đơn hàng.',
            },
            {
              icon: Clock3,
              title: 'Giờ làm việc',
              value: '8:00 - 18:00',
              description: 'Hỗ trợ tất cả các ngày trong tuần, kể cả cuối tuần.',
            },
          ],
          noteTitle: 'Trước khi liên hệ',
          noteDescription:
            'Bạn nên chuẩn bị mã đơn, số điện thoại đặt hàng và mô tả ngắn vấn đề để đội ngũ hỗ trợ kiểm tra nhanh hơn.',
        }

  return (
    <SupportPageShell
      icon={Mail}
      badge={content.badge}
      title={content.title}
      description={content.description}
      highlights={content.highlights}
    >
      <SupportSection
        title={content.noteTitle}
        description={content.noteDescription}
      >
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {content.cards.map((card) => (
            <div
              key={card.title}
              className="rounded-[22px] border border-border/60 bg-background/45 p-5"
            >
              <card.icon className="size-5 text-primary" />
              <p className="mt-4 text-sm font-semibold uppercase tracking-[0.18em] text-muted-foreground">
                {card.title}
              </p>
              <p className="mt-3 text-lg font-semibold text-foreground">
                {card.value}
              </p>
              <p className="mt-3 text-sm leading-7 text-muted-foreground">
                {card.description}
              </p>
            </div>
          ))}
        </div>
      </SupportSection>
    </SupportPageShell>
  )
}
