import { Clock3, PackageCheck, ShieldCheck, Truck } from 'lucide-react'
import { useLanguage } from '@/contexts/language-context'
import { SupportPageShell, SupportSection } from './support-page-shell'

export default function ShippingPolicyPage() {
  const { language } = useLanguage()

  const content =
    language === 'en'
      ? {
          badge: 'Shipping policy',
          title: 'Delivery policy for every order',
          description:
            'We process bookstore orders quickly, package books carefully, and keep shipping expectations clear before checkout.',
          highlights: [
            { label: 'Processing', value: 'Within 24 business hours' },
            { label: 'Delivery time', value: '2-5 business days' },
            { label: 'Coverage', value: 'Nationwide shipping' },
          ],
          sections: [
            {
              title: 'Order processing',
              description:
                'Orders confirmed before 4 PM are prioritized on the same working day. Orders placed on weekends or holidays are processed on the next business day.',
              items: [
                'We verify stock and contact details before handoff.',
                'Books are packed with corner protection and moisture resistance.',
                'You receive an update once the parcel leaves the warehouse.',
              ],
            },
            {
              title: 'Delivery timeline',
              description:
                'Actual timing depends on destination, carrier workload, and weather conditions.',
              items: [
                'Inner-city areas: usually 1-2 business days.',
                'Provincial cities: usually 2-4 business days.',
                'Remote districts: usually 3-5 business days.',
              ],
            },
            {
              title: 'Shipping fees and inspection',
              description:
                'The final shipping fee is shown during checkout. Please inspect the parcel condition when receiving the order.',
              items: [
                'Free shipping may apply during selected campaigns.',
                'If the package shows heavy damage, report it immediately to the carrier.',
                'Keep the invoice and packaging if you need support after delivery.',
              ],
            },
          ],
        }
      : {
          badge: 'Chính sách giao hàng',
          title: 'Chính sách giao hàng cho mọi đơn sách',
          description:
            'Chúng tôi xử lý đơn nhanh, đóng gói sách cẩn thận và hiển thị rõ thời gian giao dự kiến trước khi bạn thanh toán.',
          highlights: [
            { label: 'Xử lý đơn', value: 'Trong 24 giờ làm việc' },
            { label: 'Thời gian giao', value: '2-5 ngày làm việc' },
            { label: 'Phạm vi', value: 'Giao hàng toàn quốc' },
          ],
          sections: [
            {
              title: 'Quy trình xử lý đơn',
              description:
                'Các đơn được xác nhận trước 16:00 sẽ được ưu tiên xử lý trong ngày làm việc. Đơn đặt cuối tuần hoặc ngày lễ sẽ chuyển sang ngày làm việc kế tiếp.',
              items: [
                'Kiểm tra tồn kho và thông tin nhận hàng trước khi bàn giao đơn vị vận chuyển.',
                'Sách được bọc chống ẩm, chèn góc và đóng gói để hạn chế cong gãy.',
                'Bạn sẽ nhận thông báo khi kiện hàng rời kho.',
              ],
            },
            {
              title: 'Thời gian giao dự kiến',
              description:
                'Thời gian thực tế có thể thay đổi theo khu vực, tình trạng vận hành của hãng vận chuyển và thời tiết.',
              items: [
                'Nội thành: thường 1-2 ngày làm việc.',
                'Thành phố, tỉnh lân cận: thường 2-4 ngày làm việc.',
                'Khu vực xa trung tâm: thường 3-5 ngày làm việc.',
              ],
            },
            {
              title: 'Phí vận chuyển và kiểm tra khi nhận',
              description:
                'Phí giao hàng cuối cùng sẽ hiển thị ở bước thanh toán. Vui lòng kiểm tra tình trạng kiện hàng khi nhận.',
              items: [
                'Một số chương trình khuyến mãi có thể áp dụng miễn phí vận chuyển.',
                'Nếu thùng hàng móp méo nghiêm trọng, hãy báo ngay cho đơn vị giao nhận.',
                'Giữ lại hóa đơn và bao bì nếu cần hỗ trợ sau giao hàng.',
              ],
            },
          ],
        }

  return (
    <SupportPageShell
      icon={Truck}
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
              const Icon = [PackageCheck, Clock3, ShieldCheck][index] ?? PackageCheck

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
