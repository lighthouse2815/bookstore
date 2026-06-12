import type { AppLanguage } from '@/locales/messages'

type TermsSection = {
  title: string
  paragraphs: string[]
}

export type RegisterTermsCopy = {
  agreementLabel: string
  linkLabel: string
  requiredMessage: string
  dialogTitle: string
  closeHint: string
  closeReady: string
  intro: string
  sections: TermsSection[]
}

const REGISTER_TERMS_COPY: Record<AppLanguage, RegisterTermsCopy> = {
  vi: {
    agreementLabel: 'Tôi đồng ý với',
    linkLabel: 'điều khoản sử dụng',
    requiredMessage: 'Bạn cần đồng ý với điều khoản trước khi đăng ký.',
    dialogTitle: 'Điều khoản sử dụng',
    closeHint:
      'Hãy cuộn xuống cuối nội dung để bật nút đóng ở góc phải.',
    closeReady: 'Bạn đã đọc đến cuối. Có thể bấm dấu X để đóng cửa sổ.',
    intro:
      'Tài liệu này là nội dung minh hoạ cho trải nghiệm đăng ký tài khoản trên SáchVui. Bằng việc tiếp tục tạo tài khoản, bạn xác nhận đã đọc, hiểu và sẵn sàng tuân thủ các nguyên tắc vận hành dưới đây.',
    sections: [
      {
        title: '1. Tài khoản và phạm vi sử dụng',
        paragraphs: [
          'Tài khoản được tạo ra để hỗ trợ mua sách, theo dõi đơn hàng, lưu lịch sử đánh giá và nhận thông báo từ hệ thống. Bạn có trách nhiệm cung cấp email còn hoạt động, bảo quản thông tin đăng nhập và không chia sẻ tài khoản cho người khác sử dụng chung.',
          'Nếu hệ thống phát hiện tài khoản bị dùng cho mục đích giả mạo, thu thập dữ liệu, phá hoại trải nghiệm mua sắm hoặc gây nhầm lẫn cho người khác, SáchVui có thể tạm khóa tài khoản để kiểm tra mà không cần báo trước trong từng trường hợp riêng lẻ.',
        ],
      },
      {
        title: '2. Dữ liệu bạn cung cấp',
        paragraphs: [
          'Bạn đồng ý rằng email dùng để đăng ký có thể được sử dụng để gửi xác nhận tài khoản, hóa đơn điện tử, cập nhật đơn hàng và các thông báo vận hành quan trọng. Chúng tôi không khuyến khích dùng email dùng chung nếu bạn muốn giữ lịch sử mua sắm tách biệt.',
          'Các thông tin được lưu trong hồ sơ cá nhân cần phản ánh đúng nhu cầu sử dụng thực tế. Khi phát hiện thông tin có dấu hiệu sai lệch nghiêm trọng hoặc được nhập tự động hàng loạt, hệ thống có quyền yêu cầu xác minh bổ sung trước khi mở các tính năng liên quan.',
        ],
      },
      {
        title: '3. Nội dung đánh giá và tương tác',
        paragraphs: [
          'Mọi đánh giá sách, bình luận hoặc phản hồi gửi qua tài khoản cần được trình bày văn minh, không chứa nội dung xúc phạm, kích động, bịa đặt hoặc quảng bá dịch vụ không liên quan. Những nội dung vi phạm có thể bị ẩn hoặc xóa để giữ môi trường đọc lành mạnh.',
          'Khi bạn đăng nội dung lên hệ thống, bạn cho phép SáchVui hiển thị lại nội dung đó trong giao diện sản phẩm, trang tổng hợp đánh giá hoặc báo cáo quản trị nội bộ nhằm phục vụ cải thiện chất lượng dịch vụ và trải nghiệm người dùng.',
        ],
      },
      {
        title: '4. Quy tắc giao dịch',
        paragraphs: [
          'Giá bán, khuyến mãi, tồn kho và thời gian giao hàng có thể thay đổi theo từng thời điểm vận hành. Việc thêm sách vào giỏ không đồng nghĩa với việc giữ hàng tự động; đơn chỉ được xác nhận khi hệ thống ghi nhận thành công bước đặt hàng theo đúng quy trình.',
          'Trong các tình huống phát sinh như lệch tồn kho, thay đổi chính sách vận chuyển, lỗi hiển thị giá hoặc giới hạn khu vực giao hàng, SáchVui sẽ chủ động liên hệ để đề xuất phương án thay thế, hoàn tiền hoặc điều chỉnh đơn theo cách hợp lý nhất.',
        ],
      },
      {
        title: '5. Bảo mật và ổn định hệ thống',
        paragraphs: [
          'Bạn không được thực hiện các hành vi dò quét, gửi yêu cầu quá mức, thử nghiệm trái phép, tự động hóa bất thường hoặc can thiệp vào luồng xử lý nhằm vượt qua giới hạn của website. Những hành vi này có thể ảnh hưởng trực tiếp đến độ ổn định của toàn hệ thống.',
          'Nếu phát hiện lỗ hổng hoặc bất thường, cách xử lý phù hợp là báo lại cho đội ngũ vận hành thay vì khai thác tiếp. Việc hợp tác có trách nhiệm sẽ giúp hạn chế rủi ro cho cộng đồng người dùng và bảo vệ dữ liệu liên quan đến giao dịch.',
        ],
      },
      {
        title: '6. Chỉnh sửa, tạm ngừng và chấm dứt dịch vụ',
        paragraphs: [
          'SáchVui có thể cập nhật giao diện, tính năng, điều khoản vận hành hoặc luồng xử lý tài khoản theo nhu cầu thực tế. Với những thay đổi ảnh hưởng đáng kể đến cách sử dụng, thông tin sẽ được công bố trong giao diện hoặc gửi kèm theo thông báo phù hợp.',
          'Bạn có thể ngừng sử dụng dịch vụ bất kỳ lúc nào. Tuy nhiên, các dữ liệu phát sinh từ giao dịch đã hoàn tất, yêu cầu hỗ trợ đang xử lý hoặc nghĩa vụ lưu trữ theo vận hành vẫn có thể được giữ lại trong phạm vi cần thiết để đối soát và chăm sóc khách hàng.',
        ],
      },
      {
        title: '7. Đồng thuận cuối cùng',
        paragraphs: [
          'Việc đánh dấu đồng ý thể hiện rằng bạn chấp nhận bộ điều khoản minh hoạ này như một phần của trải nghiệm đăng ký. Nếu bạn không đồng ý với bất kỳ nội dung nào, lựa chọn an toàn nhất là dừng thao tác tạo tài khoản cho đến khi bạn cảm thấy phù hợp hơn.',
          'Cảm ơn bạn đã dành thời gian đọc đến cuối. Sự cẩn trọng của bạn giúp quá trình dùng thử giao diện đăng ký trở nên rõ ràng, có chủ đích và sát hơn với cách một website thương mại điện tử nên trao đổi trách nhiệm với người dùng.',
        ],
      },
    ],
  },
  en: {
    agreementLabel: 'I agree to the',
    linkLabel: 'terms of use',
    requiredMessage: 'You must agree to the terms before registering.',
    dialogTitle: 'Terms of Use',
    closeHint:
      'Scroll to the bottom to unlock the close button in the top-right corner.',
    closeReady:
      'You have reached the end. The X button can now be used to close this dialog.',
    intro:
      'This document is a demo terms-of-use sheet for the SáchVui registration flow. By continuing to create an account, you confirm that you have read, understood, and are willing to follow the operating principles below.',
    sections: [
      {
        title: '1. Account scope and access',
        paragraphs: [
          'An account is provided so you can buy books, track orders, manage reviews, and receive system notifications. You are responsible for using a working email address, safeguarding your credentials, and avoiding shared access with other people.',
          'If the platform detects impersonation, scraping, disruption attempts, or behavior that may mislead other customers, SáchVui may temporarily restrict or suspend the account while an internal review is performed.',
        ],
      },
      {
        title: '2. Information you provide',
        paragraphs: [
          'You agree that the email used for registration may be used for account notices, transaction receipts, order updates, and other operational messages that are necessary to keep the service functioning correctly.',
          'Profile information should reflect a legitimate usage intent. When obviously false, bulk-generated, or abusive information is detected, the system may request additional verification before related features are fully available.',
        ],
      },
      {
        title: '3. Reviews and community conduct',
        paragraphs: [
          'Any review, rating, or feedback posted through your account must remain respectful and relevant. Content containing harassment, fabrication, spam, or unrelated advertising may be hidden or removed to preserve a healthy reading community.',
          'By publishing content on the platform, you allow SáchVui to display that content in product pages, review summaries, and internal moderation views for service quality and operational improvement purposes.',
        ],
      },
      {
        title: '4. Transaction rules',
        paragraphs: [
          'Pricing, promotions, stock levels, and delivery windows may change during normal operations. Adding a book to your cart does not automatically reserve inventory; an order is only confirmed once the checkout flow completes successfully.',
          'If stock conflicts, shipping policy changes, display-price issues, or delivery-area limitations occur, SáchVui may contact you with a reasonable alternative such as replacement, refund, or order adjustment.',
        ],
      },
      {
        title: '5. Security and system stability',
        paragraphs: [
          'You must not perform scanning, abusive automation, unauthorized testing, or attempts to bypass technical limits on the website. These actions can directly affect service reliability for everyone else.',
          'If you discover a vulnerability or unusual behavior, the responsible path is to report it to the team rather than continue exploiting it. Responsible handling helps protect the wider customer base and the data associated with transactions.',
        ],
      },
      {
        title: '6. Changes, interruption, and retention',
        paragraphs: [
          'SáchVui may update the interface, features, terms, or account workflows when operational needs change. When a change materially affects the user experience, a visible in-product notice or another appropriate communication method may be used.',
          'You may stop using the service at any time. However, data tied to completed transactions, active support requests, or operational retention obligations may remain stored for as long as reasonably necessary.',
        ],
      },
      {
        title: '7. Final acknowledgement',
        paragraphs: [
          'Checking the agreement box means you accept these demo terms as part of the registration experience. If you disagree with any part of the text, the safer option is to stop creating the account until you are comfortable proceeding.',
          'Thank you for reading to the end. Your attention makes the signup flow feel intentional, transparent, and closer to how a production e-commerce experience should communicate responsibilities.',
        ],
      },
    ],
  },
}

export function getRegisterTermsCopy(language: AppLanguage) {
  return REGISTER_TERMS_COPY[language]
}
