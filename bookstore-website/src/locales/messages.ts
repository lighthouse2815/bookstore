export type AppLanguage = 'vi' | 'en'

export const messages = {
  vi: {
    language: {
      label: 'Ngôn ngữ',
      switcherAria: 'Chọn ngôn ngữ',
      vi: 'VI',
      en: 'EN',
    },
    common: {
      deployStartup: {
        badge: 'Đang kết nối',
        title: 'Nhà sách đang chuẩn bị',
        description:
          'Hệ thống cần thêm một chút thời gian để sẵn sàng. Trang sẽ tự tiếp tục ngay khi kết nối hoàn tất.',
        hint:
          'Bạn cứ giữ tab này mở, hệ thống đang tự thử kết nối lại để nạp dữ liệu nhà sách.',
        waitedLabel: 'Thời gian chờ',
        waitedSeconds: '{seconds} giây',
        retryLabel: 'Số lần thử',
        retryCount: '{count} lần',
        retryNow: 'Thử lại ngay',
        footer:
          'Nếu thời gian chờ lâu hơn dự kiến, bạn có thể thử kết nối lại ngay.',
        phases: {
          boot: 'Khởi động dịch vụ',
          warmup: 'Chuẩn bị kết nối',
          catalog: 'Nạp dữ liệu nhà sách',
        },
      },
      brand: 'SáchVui',
      or: 'hoặc',
      loading: 'Đang tải...',
      search: 'Tìm kiếm',
      subtotal: 'Tạm tính',
      shipping: 'Phí vận chuyển',
      total: 'Tổng cộng',
      free: 'Miễn phí',
      continueShopping: 'Tiếp tục mua sắm',
      proceedToCheckout: 'Tiến hành thanh toán',
      backHome: 'Về trang chủ',
      send: 'Gửi',
      quantity: 'Số lượng',
      actions: 'Thao tác',
      email: 'Email',
      phone: 'Số điện thoại',
      date: 'Ngày',
      category: 'Thể loại',
      price: 'Giá',
      processing: 'Đang xử lý...',
      viewAll: 'Xem tất cả',
      dashboard: 'Bảng điều khiển',
      save: 'Lưu',
      cancel: 'Hủy',
      close: 'Đóng',
      view: 'Xem',
      edit: 'Sửa',
      delete: 'Xóa',
      name: 'Tên',
      description: 'Mô tả',
      createdAt: 'Tạo lúc',
      updatedAt: 'Cập nhật lúc',
      pagination: {
        navigation: 'Phân trang',
        previous: 'Trang trước',
        next: 'Trang sau',
        page: 'Trang {page}/{total}',
        total: 'Tổng cộng {count} bản ghi',
        goToPage: 'Đến trang {page}',
        jumpLabel: 'Đến trang',
        jumpInput: 'Nhập số trang muốn đến',
        jumpAction: 'Đi',
      },
    },
    header: {
      searchAria: 'Tìm kiếm sách',
      cartAria: 'Giỏ hàng',
      login: 'Đăng nhập',
      logout: 'Đăng xuất',
      myProfile: 'Hồ sơ của tôi',
      myLibrary: 'Thư viện số',
      adminDashboard: 'Trang quản trị',
      profileMenu: 'Menu tài khoản',
      switchToLight: 'Chuyển sang giao diện sáng',
      switchToDark: 'Chuyển sang giao diện tối',
      nav: {
        home: 'Trang chủ',
        books: 'Sách',
        digitalLibrary: 'Thư viện số',
        lifeSkills: 'Kỹ năng',
        novel: 'Tiểu thuyết',
      },
    },
    footer: {
      description:
        'Nhà sách trực tuyến dành cho người trẻ. Hàng ngàn đầu sách hay, giao hàng nhanh, giá luôn tốt.',
      socialAria: 'Mạng xã hội',
      explore: 'Khám phá',
      support: 'Hỗ trợ',
      newsletterTitle: 'Đăng ký nhận tin',
      newsletterDescription: 'Nhận ưu đãi và gợi ý sách hay mỗi tuần.',
      newsletterPlaceholder: 'Email của bạn',
      newsletterSubmitting: 'Đang đăng ký',
      newsletterSuccess: 'Đăng ký nhận tin thành công.',
      newsletterError: 'Không thể đăng ký nhận tin. Vui lòng thử lại.',
      copyright: '© 2026 SáchVui. Bảo lưu mọi quyền.',
      tagline: 'Thiết kế dành cho người yêu sách.',
      links: {
        allBooks: 'Tất cả sách',
        bestsellers: 'Sách bán chạy',
        newBooks: 'Sách mới',
        promotions: 'Khuyến mãi',
        shippingPolicy: 'Chính sách giao hàng',
        returns: 'Đổi trả & hoàn tiền',
        faq: 'Câu hỏi thường gặp',
        contact: 'Liên hệ',
      },
    },
    home: {
      heroBadge: 'Giảm đến 40% sách mới mỗi tuần',
      heroTitlePrefix: 'Đọc nhiều hơn,',
      heroTitleAccent: 'sống vui hơn',
      heroDescription:
        'Khám phá những đầu sách hay từ tiểu thuyết, kỹ năng sống đến khoa học. Giao hàng nhanh, giá luôn tốt cho người trẻ.',
      shopNow: 'Mua sắm ngay',
      lifeSkillsBooks: 'Sách kỹ năng',
      stats: {
        books: 'Đầu sách',
        sales: 'Lượt bán',
        reviewsCount: '{count} đánh giá',
      },
      values: {
        fastDeliveryTitle: 'Giao hàng nhanh',
        fastDeliveryDesc: 'Nhận sách trong 24-48 giờ',
        greatPriceTitle: 'Giá tốt mỗi ngày',
        greatPriceDesc: 'Ưu đãi đến 40% cho thành viên',
        authenticTitle: 'Sách chính hãng',
        authenticDesc: '100% bản quyền từ NXB',
      },
      categoriesTitle: 'Khám phá theo thể loại',
      categoriesCount: '{count} thể loại để bạn khám phá',
      allCategories: 'Xem tất cả thể loại',
      catalogLoadingTitle: 'Đang tải kệ sách trang chủ',
      catalogLoadingDescription:
        'Phần khung trang vẫn sẵn sàng trong lúc dữ liệu sách đang được tải.',
      catalogBooksErrorTitle: 'Chưa tải được danh sách sách',
      catalogBooksErrorDescription:
        'Các mục khám phá vẫn hoạt động. Kệ sách sẽ tự hiển thị lại khi dữ liệu sẵn sàng.',
      catalogCategoriesErrorTitle: 'Chưa tải được thể loại',
      catalogCategoriesErrorDescription:
        'Danh sách thể loại sẽ xuất hiện lại khi dữ liệu sẵn sàng.',
      featuredTitle: 'Sách nổi bật',
      emptyTitle: 'Chưa có sách nào trong hệ thống',
      emptyDescription:
        'Các danh mục và gợi ý sẽ xuất hiện tại đây khi kho sách có dữ liệu.',
      promoTitle: 'Tuần lễ vàng - Mua 2 tặng 1',
      promoDescription:
        'Áp dụng cho toàn bộ sách kỹ năng sống và tiểu thuyết. Số lượng có hạn, nhanh tay sở hữu ngay hôm nay!',
      promoButton: 'Săn deal ngay',
      bestsellersTitle: 'Bán chạy nhất',
      bookMatch: {
        badge: 'BookMatch',
        title: 'Không biết đọc gì hôm nay?',
        description:
          'Trả lời 3 câu hỏi ngắn về cảm hứng, ngân sách và thời lượng đọc để tìm những cuốn sách hợp với bạn hôm nay.',
        button: 'Tìm sách hợp gu',
        stepCount: '3 câu hỏi ngắn',
      },
      couponGame: {
        badge: 'Mini game hôm nay',
        title: 'Săn mã giảm giá hôm nay',
        description:
          'Quay vòng may mắn để nhận một mã giảm giá còn hiệu lực, sau đó sao chép mã và dùng khi thanh toán.',
        button: 'Mở vòng quay',
        dailyLimit: '1 lượt mỗi ngày',
      },
    },
    recommendations: {
      title: 'Dành cho bạn',
      subtitle: 'Gợi ý dựa trên sở thích và hành trình đọc của bạn.',
      loading: 'Đang tải gợi ý dành cho bạn',
      reasons: {
        PURCHASE_HISTORY: 'Tương tự sách đã mua',
        FAVORITE_CATEGORY: 'Thể loại bạn thường đọc',
        FAVORITE_AUTHOR: 'Tác giả bạn yêu thích',
        WISHLIST_SIGNAL: 'Từ danh sách yêu thích',
        BOOKSHELF_SIGNAL: 'Từ kệ sách của bạn',
        HIGH_RATING_REVIEW: 'Dựa trên đánh giá cao của bạn',
        READING_JOURNAL_SIGNAL: 'Dựa trên nhật ký đọc',
        POPULAR_PICK: 'Được nhiều người chọn',
        HIGH_RATING: 'Được đánh giá cao',
        NEW_RELEASE: 'Sách mới phát hành',
        FALLBACK_POPULAR: 'Gợi ý phổ biến',
      },
    },
    couponGamePage: {
      badge: 'Vui nhẹ nhưng có quà',
      title: 'Vòng quay may mắn săn mã giảm giá',
      description:
        'Mỗi ngày bạn có một lượt quay để nhận ngẫu nhiên một mã giảm giá còn hiệu lực. Hãy sao chép mã và dùng cho đơn hàng phù hợp.',
      limitChip: 'Giới hạn',
      dailyLimit: '1 lần / ngày',
      poolChip: 'Mã đang có',
      poolCount: '{count} mã sẵn sàng',
      manualChip: 'Cách dùng',
      manualOnly: 'Sao chép và nhập mã',
      wheelBadge: 'Vòng quay may mắn',
      spinHint:
        'Bấm quay để nhận ngẫu nhiên một mã trong danh sách ưu đãi đang áp dụng.',
      spinButton: 'Quay ngay',
      spinLoading: 'Đang quay...',
      playedButton: 'Đã quay hôm nay',
      alreadyPlayedTitle: 'Bạn đã chơi hôm nay',
      alreadyPlayedDescription:
        'Kết quả hôm nay đã được giữ lại bên phải. Quay lại ngày mai để săn tiếp một mã khác.',
      resultTitle: 'Mã bạn có thể săn được',
      resultPlaceholder: 'Chưa mở quà',
      resultWaiting:
        'Sau khi quay, mã giảm giá, mức ưu đãi, hạn dùng và nút sao chép sẽ hiện ở đây.',
      todayResultBadge: 'Mã hôm nay',
      resultSaved: 'Kết quả giữ đến',
      copyButton: 'Sao chép mã',
      copySuccess: 'Đã sao chép mã giảm giá.',
      copyError:
        'Không thể sao chép tự động. Hãy sao chép mã phía trên theo cách thủ công.',
      openCart: 'Đi tới giỏ hàng',
      backHome: 'Về trang chủ',
      viewBooks: 'Xem thêm sách',
      manualApplyHint:
        'Mã chưa được áp tự động. Hãy dán mã vào ô giảm giá trong giỏ hàng hoặc khi thanh toán.',
      typeBook: 'Dùng cho sách',
      typeShipping: 'Dùng cho phí vận chuyển',
      minOrderLabel: 'Đơn tối thiểu',
      maxDiscountLabel: 'Giảm tối đa',
      expiresLabel: 'Hạn dùng',
      noExpiry: 'Không giới hạn thời gian',
      noDescription:
        'Mã này chưa có mô tả thêm. Bạn vẫn có thể sao chép và thử áp dụng cho đơn hàng phù hợp.',
      emptyTitle: 'Hôm nay chưa có mã giảm giá để quay',
      emptyDescription:
        'Vòng quay sẽ mở lại khi có mã giảm giá phù hợp. Bạn vui lòng quay lại sau nhé.',
      errorTitle: 'Tạm thời chưa tải được vòng quay',
      errorDescription:
        'Không thể tải danh sách mã giảm giá lúc này. Vui lòng thử lại sau.',
      discountPercent: 'Giảm {value}%',
      discountFixed: 'Giảm {amount}',
      wheelCenter: 'LUCKY',
    },
    auth: {
      login: {
        success: 'Đăng nhập thành công!',
        errorFallback: 'Đăng nhập thất bại',
        title: 'Chào mừng quay lại',
        description:
          'Đăng nhập bằng tài khoản của bạn để tiếp tục mua sách',
        cardTitle: 'Đăng nhập',
        username: 'Tên đăng nhập',
        password: 'Mật khẩu',
        forgotPassword: 'Quên mật khẩu?',
        passwordPlaceholder: 'Nhập mật khẩu',
        showPassword: 'Hiện mật khẩu',
        hidePassword: 'Ẩn mật khẩu',
        signupDescription:
          'Mặt sau dùng để giới thiệu nhanh phần đăng ký. Để tạo tài khoản đầy đủ, chuyển sang trang đăng ký riêng.',
        openRegisterPage: 'Mở trang đăng ký',
        submit: 'Đăng nhập',
        noAccount: 'Chưa có tài khoản?',
        registerNow: 'Đăng ký ngay',
        restrictions: {
          locked: {
            title: 'Tài khoản đã bị khóa',
            description:
              'Tài khoản này hiện không thể đăng nhập. Vui lòng liên hệ quản trị viên để được mở khóa.',
          },
          inactive: {
            title: 'Tài khoản chưa kích hoạt',
            description:
              'Tài khoản này cần xác thực OTP trước khi đăng nhập. Hãy hoàn tất bước kích hoạt rồi hệ thống sẽ đăng nhập lại cho bạn.',
          },
        },
        flow: {
          lockedActionLabel: 'Dùng tài khoản khác',
          inactiveActionLabel: 'Gửi lại OTP',
          inactiveOtpLead:
            'Nhập mã OTP kích hoạt gần nhất trong email của bạn. Nếu chưa nhận được hoặc mã đã hết hạn, bạn có thể gửi lại ngay từ đây.',
          inactiveOtpReadyHint:
            'Nhập đúng mã OTP 6 chữ số rồi hệ thống sẽ tự đăng nhập lại bằng tài khoản bạn vừa nhập.',
          inactiveBackLabel: 'Quay lại đăng nhập',
          inactiveVerifyLabel: 'Xác thực và đăng nhập',
          inactiveEmailRequiredMessage:
            'Tài khoản chưa kích hoạt cần đăng nhập bằng email để xác thực OTP.',
          inactiveRequestErrorFallback: 'Không thể gửi lại mã OTP kích hoạt',
        },
      },
      forgotPassword: {
        title: 'Quên mật khẩu',
        description:
          'Nhập email, xác thực mã bảo mật rồi đặt lại mật khẩu mới',
        requestTitle: 'Yêu cầu OTP',
        requestDescription:
          'Nhập email tài khoản để nhận mã đặt lại mật khẩu',
        requestHint:
          'Nếu email hợp lệ, mã xác thực sẽ được gửi đến hộp thư của bạn.',
        requestSubmit: 'Gửi OTP',
        requestSuccess: 'Nếu email tồn tại, OTP đã được gửi',
        requestErrorFallback: 'Không thể gửi OTP đặt lại mật khẩu',
        verifyTitle: 'Xác thực OTP',
        verifyDescription:
          'Nhập mã OTP 6 số đã được gửi tới {email}',
        otpSent: 'OTP đã được gửi tới email của bạn',
        verifyHint:
          'Sau khi mã được xác thực, bạn có thể tạo mật khẩu mới.',
        verifySubmit: 'Xác thực OTP',
        verifySuccess: 'OTP hợp lệ, hãy đặt mật khẩu mới',
        verifyErrorFallback: 'Xác thực OTP đặt lại mật khẩu thất bại',
        otpCode: 'Mã OTP',
        otpPlaceholder: '6 chữ số',
        otpInvalid: 'OTP phải gồm đúng 6 chữ số!',
        backStep: 'Quay lại',
        resetTitle: 'Đặt mật khẩu mới',
        resetDescription:
          'Tạo mật khẩu mới cho tài khoản {email}',
        resetHint:
          'Mật khẩu mới phải có ít nhất 8 ký tự. Các phiên đăng nhập cũ sẽ kết thúc sau khi đổi mật khẩu.',
        newPassword: 'Mật khẩu mới',
        newPasswordPlaceholder: 'Ít nhất 8 ký tự',
        confirmPassword: 'Xác nhận mật khẩu mới',
        resetSubmit: 'Đổi mật khẩu',
        resetSuccess: 'Đổi mật khẩu thành công!',
        resetErrorFallback: 'Đổi mật khẩu thất bại',
        startOver: 'Bắt đầu lại',
        backToLogin: 'Quay về đăng nhập',
      },
      register: {
        success: 'Đăng ký thành công!',
        errorFallback: 'Đăng ký thất bại',
        verifyErrorFallback: 'Xác thực OTP thất bại',
        passwordMismatch: 'Mật khẩu không khớp!',
        passwordTooShort: 'Mật khẩu phải có ít nhất 8 ký tự!',
        title: 'Tạo tài khoản',
        description:
          'Điền thông tin và xác thực email để hoàn tất đăng ký tài khoản',
        otpSent: 'Mã OTP đã được gửi tới email của bạn',
        verifyTitle: 'Xác thực OTP',
        verifyDescription:
          'Nhập mã OTP 6 số đã được gửi tới {email} để kích hoạt tài khoản',
        verifyHint:
          'Mã OTP được gửi ngay sau khi tạo tài khoản. Bạn có thể đăng nhập sau khi xác thực.',
        otpCode: 'Mã OTP',
        otpPlaceholder: '6 chữ số',
        otpHint: 'Kiểm tra hộp thư và nhập đúng mã gồm 6 chữ số.',
        otpInvalid: 'OTP phải gồm đúng 6 chữ số!',
        backToRegister: 'Quay lại đăng ký',
        verifySubmit: 'Xác thực OTP',
        phoneNumber: 'Số điện thoại',
        firstName: 'Tên',
        lastName: 'Họ',
        gender: 'Giới tính',
        dateOfBirth: 'Ngày sinh',
        avatarUrl: 'Liên kết ảnh đại diện (tuỳ chọn)',
        password: 'Mật khẩu',
        confirmPassword: 'Xác nhận mật khẩu',
        passwordPlaceholder: 'Ít nhất 8 ký tự',
        passwordWeak: 'Mật khẩu yếu',
        passwordMedium: 'Mật khẩu vừa',
        passwordStrong: 'Mật khẩu mạnh',
        passwordVeryStrong: 'Mật khẩu rất mạnh',
        passwordMatched: 'Mật khẩu khớp',
        passwordNotMatched: 'Mật khẩu không khớp',
        submit: 'Tạo tài khoản',
        haveAccount: 'Đã có tài khoản?',
        loginNow: 'Đăng nhập ngay',
        verification: {
          resendOtpLabel: 'Gửi lại mã OTP',
          requestOtpErrorFallback: 'Không thể gửi lại mã OTP kích hoạt',
        },
        terms: {
          badge: 'Điều khoản SáchVui',
          agreementLabel: 'Tôi đồng ý với',
          linkLabel: 'điều khoản sử dụng',
          requiredMessage: 'Bạn cần đồng ý với điều khoản trước khi đăng ký.',
          dialogTitle: 'Điều khoản sử dụng',
          closeHint:
            'Hãy cuộn xuống cuối nội dung để bật nút đóng ở góc phải.',
          closeReady:
            'Bạn đã đọc đến cuối. Có thể bấm dấu X để đóng cửa sổ.',
          intro:
            'Tài liệu này trình bày các điều khoản sử dụng tài khoản SáchVui. Bằng việc tiếp tục đăng ký, bạn xác nhận đã đọc, hiểu và đồng ý tuân thủ các nội dung dưới đây.',
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
                'Việc đánh dấu đồng ý thể hiện rằng bạn chấp nhận các điều khoản này. Nếu không đồng ý với bất kỳ nội dung nào, bạn nên dừng thao tác tạo tài khoản.',
                'Cảm ơn bạn đã dành thời gian đọc. Sự cẩn trọng này giúp quyền lợi và trách nhiệm của bạn khi sử dụng SáchVui được rõ ràng hơn.',
              ],
            },
          ],
        },
      },
      profile: {
        logout: 'Đăng xuất',
        accountTitle: 'Thông tin tài khoản',
        personalTitle: 'Thông tin cá nhân',
        saveAccount: 'Lưu tài khoản',
        saveProfile: 'Lưu hồ sơ',
        accountUpdated: 'Đã cập nhật thông tin tài khoản',
        profileUpdated: 'Đã cập nhật hồ sơ cá nhân',
        username: 'Tên đăng nhập',
        firstName: 'Tên',
        lastName: 'Họ',
        avatarUrl: 'Liên kết ảnh đại diện',
        gender: 'Giới tính',
        dateOfBirth: 'Ngày sinh',
        ordersTitle: 'Đơn hàng của bạn',
        emptyOrders: 'Bạn chưa có đơn hàng nào',
        orderTotal: 'Tổng cộng',
        loginPanelTitle: 'Thông tin đăng nhập',
        addressMenuTitle: 'Địa chỉ của tôi',
        passwordMenuTitle: 'Đổi mật khẩu',
        chooseImage: 'Chọn ảnh',
        imageHint: 'JPG, PNG',
        noOrdersDescription:
          'Khám phá sách hay và đặt đơn đầu tiên của bạn.',
        shopNow: 'Mua sách ngay',
        addressTitle: 'Sổ địa chỉ của bạn',
        addressDescription:
          'Các địa chỉ giao hàng đã lưu sẽ hiển thị ở đây.',
        noAddressesTitle: 'Bạn chưa có địa chỉ nào',
        noAddressesDescription:
          'Thêm địa chỉ khi thanh toán để lần sau chọn nhanh hơn.',
        passwordTitle: 'Bảo mật tài khoản',
        passwordDescription:
          'Đổi mật khẩu bằng luồng xác thực hiện có của hệ thống.',
        passwordAction: 'Đi tới đổi mật khẩu',
        defaultAddress: 'Mặc định',
        retry: 'Thử lại',
        goCheckout: 'Đi tới thanh toán',
        avatarLabel: 'Ảnh đại diện',
      },
    },
    book: {
      fallback: {
        author: 'Chưa có tác giả',
        category: 'Chưa có thể loại',
        publisher: 'Chưa có nhà xuất bản',
      },
      card: {
        coverAlt: 'Bìa sách {title}',
        bestseller: 'Bán chạy',
        addedToCart: 'Đã thêm "{title}" vào giỏ',
        addToCartAria: 'Thêm {title} vào giỏ',
      },
      addToCart: {
        quantity: 'Số lượng',
        decrease: 'Giảm số lượng',
        increase: 'Tăng số lượng',
        addedQtyToCart: 'Đã thêm {count} cuốn vào giỏ',
        addToCart: 'Thêm vào giỏ',
        buyNow: 'Mua ngay',
      },
      listing: {
        title: 'Tất cả sách',
        resultCount: 'Tìm thấy {count} cuốn sách phù hợp',
        searchPlaceholder: 'Tìm sách, tác giả...',
        categoryTitle: 'Thể loại',
        categoryCount: '{count} mục',
        selectedCategoryLabel: 'Đang lọc theo',
        clearCategory: 'Bỏ lọc',
        categorySearchPlaceholder: 'Tìm thể loại...',
        categoryShowingCount: 'Hiển thị {count}/{total} thể loại',
        categoryEmptyTitle: 'Không có thể loại phù hợp',
        categoryEmptyDescription: 'Thử từ khóa khác hoặc bỏ lọc để xem toàn bộ thể loại.',
        showMoreCategories: 'Xem thêm {count} mục',
        showFewerCategories: 'Thu gọn',
        sortPlaceholder: 'Sắp xếp',
        sortPopular: 'Phổ biến nhất',
        sortRating: 'Đánh giá cao',
        sortPriceAsc: 'Giá thấp đến cao',
        sortPriceDesc: 'Giá cao đến thấp',
        errorTitle: 'Không tải được danh sách sách',
        errorDescription:
          'Không thể nhận dữ liệu sách lúc này. Vui lòng thử lại sau.',
        emptyTitle: 'Không tìm thấy sách nào',
        emptyDescription: 'Thử thay đổi từ khóa hoặc bộ lọc của bạn.',
      },
      detail: {
        breadcrumbHome: 'Trang chủ',
        breadcrumbBooks: 'Sách',
        author: 'Tác giả',
        reviewsCount: '({count} đánh giá)',
        saveAmount: 'Tiết kiệm {amount}',
        availabilityLabel: 'Tình trạng',
        deliveryTitle: 'Giao hàng nhanh',
        deliveryTime: 'Nhận trong 24-48 giờ',
        freeShippingTitle: 'Miễn phí vận chuyển',
        freeShippingThreshold: 'Đơn từ {amount}',
        wishlistShort: 'Yêu thích',
        wishlistedShort: 'Đã lưu',
        shelfShort: 'Kệ sách',
        journalShort: 'Nhật ký',
        descriptionTitle: 'Giới thiệu sách',
        descriptionFallback: 'Mô tả sách đang được cập nhật.',
        detailsTitle: 'Mô tả chi tiết',
        detailsFallback: 'Đang cập nhật.',
        authorInfoTitle: 'Thông tin tác giả',
        authorBioFallback: 'Thông tin tác giả đang được cập nhật.',
        promotionsTitle: 'Ưu đãi dành cho bạn',
        promotionsEmpty: 'Ưu đãi đang được cập nhật.',
        promotionCodeLabel: 'Nhập mã',
        promotionMinOrder: 'Áp dụng cho đơn từ {amount}.',
        promotionNoMinOrder: 'Áp dụng cho mọi đơn hàng.',
        promotionMaxDiscount: 'Giảm tối đa {amount}.',
        commitmentsTitle: 'Cam kết từ {brand}',
        commitmentAuthentic: 'Sách chính hãng 100%',
        commitmentReturn: 'Đổi trả linh hoạt trong 7 ngày',
        commitmentShipping: 'Giao hàng nhanh 24-48h',
        commitmentSupport: 'Hỗ trợ khách hàng mỗi ngày',
        reviewTitle: 'Đánh giá của khách hàng',
        reviewEmpty: 'Chưa có đánh giá nào cho sách này.',
        reviewHelpful: 'Hữu ích ({count})',
        reviewVerifiedPurchase: 'Đã mua hàng',
        stockOut: 'Hết hàng',
        suggestionsTitle: 'Có thể bạn cũng thích',
        soldCountValue: 'Đã bán {count}',
        specStock: 'Tồn kho',
        specPublisher: 'Nhà xuất bản',
        specIsbn: 'ISBN',
        specUpdatedAt: 'Cập nhật',
        specPageCount: 'Số trang',
        specPublicationYear: 'Năm xuất bản',
        specLanguage: 'Ngôn ngữ',
        specCoverType: 'Loại bìa',
        specDimensions: 'Kích thước',
        specWeight: 'Trọng lượng',
        specTranslator: 'Người dịch',
        specEdition: 'Phiên bản',
        stockValue: '{count} cuốn',
        pageCountValue: '{count} trang',
        weightValue: '{count} g',
        digitalAssets: {
          title: 'Phi\u00ean b\u1ea3n s\u1ed1',
          description:
            '\u0110\u00e2y l\u00e0 c\u00e1c phi\u00ean b\u1ea3n s\u1ed1 \u0111ang \u0111\u01b0\u1ee3c ph\u00e1t h\u00e0nh c\u00f4ng khai cho cu\u1ed1n s\u00e1ch n\u00e0y.',
          addToCart: 'Th\u00eam ebook v\u00e0o gi\u1ecf',
          addToCartError:
            'Kh\u00f4ng th\u1ec3 th\u00eam phi\u00ean b\u1ea3n s\u1ed1 v\u00e0o gi\u1ecf h\u00e0ng.',
          addedToCart: '\u0110\u00e3 th\u00eam phi\u00ean b\u1ea3n s\u1ed1 v\u00e0o gi\u1ecf h\u00e0ng.',
          addingToCart: '\u0110ang th\u00eam...',
          downloadAllowed: 'Cho ph\u00e9p t\u1ea3i',
          downloadRestricted: 'Gi\u1edbi h\u1ea1n t\u1ea3i',
          noSample: 'Kh\u00f4ng c\u00f3 b\u1ea3n m\u1eabu',
          openSample: 'M\u1edf b\u1ea3n m\u1eabu',
          purchaseAvailable: 'C\u00f3 th\u1ec3 mua',
          purchaseDisabled: 'T\u1ea1m th\u1eddi kh\u00f4ng b\u00e1n',
          purchaseUnavailable: 'Kh\u00f4ng h\u1ed7 tr\u1ee3 mua',
          sampleAvailable: 'C\u00f3 b\u1ea3n m\u1eabu',
        },
      },
      match: {
        heroBadge: 'BookMatch',
        title: 'Hôm nay đọc gì?',
        description:
          'Chọn cảm hứng, ngân sách và thời lượng đọc để nhận danh sách sách phù hợp với bạn hôm nay.',
        progressTitle: 'Tiến độ lựa chọn',
        progressReady: 'Sẵn sàng gợi ý',
        progressStep: 'Bước {current}/3',
        sidebarTitle: 'Phiên gợi ý hôm nay',
        sidebarDescription:
          'Hoàn thành 3 lựa chọn để nhận khoảng {count} cuốn sách phù hợp.',
        submit: 'Gợi ý sách cho tôi',
        submitLoading: 'Đang ghép mood...',
        reset: 'Chọn lại',
        resultsTitle: 'Kết quả BookMatch',
        resultsDescription:
          'Danh sách ưu tiên cảm hứng đọc, ngân sách, độ dài, đánh giá và mức độ yêu thích của độc giả.',
        resultsCount: '{count} cuốn phù hợp hôm nay',
        errorTitle: 'Chưa gợi ý được sách',
        errorRetry: 'Thử lại',
        catalogEmptyTitle: 'Chưa có sách để gợi ý',
        catalogEmptyDescription:
          'Kho sách hiện chưa có dữ liệu phù hợp. Bạn vui lòng quay lại sau.',
        emptyTitle: 'Chưa tìm thấy cuốn phù hợp',
        emptyDescription:
          'Hãy thử đổi cảm hứng đọc hoặc mở rộng khoảng ngân sách một chút nhé.',
        summary: {
          mood: 'Cảm hứng',
          budget: 'Ngân sách',
          readingTime: 'Thời lượng',
          pending: 'Chưa chọn',
        },
        steps: {
          moodLabel: 'Cảm hứng',
          budgetLabel: 'Ngân sách',
          readingTimeLabel: 'Thời lượng',
          moodTitle: 'Bạn muốn đọc với cảm hứng nào?',
          moodDescription:
            'Lựa chọn này giúp ưu tiên thể loại và chủ đề gần với mong muốn của bạn.',
          budgetTitle: 'Hôm nay bạn muốn chi khoảng bao nhiêu?',
          budgetDescription:
            'Giá sách sẽ được ưu tiên theo đúng khoảng ngân sách bạn chọn.',
          readingTimeTitle: 'Bạn muốn đọc dày hay mỏng?',
          readingTimeDescription:
            'BookMatch dùng số trang hiện có để ưu tiên sách ngắn, vừa hoặc dài.',
        },
        moods: {
          RELAX: {
            label: 'RELAX · Muốn thư giãn',
            description:
              'Văn học, truyện ngắn, đời sống nhẹ nhàng để đọc chill.',
          },
          STUDY: {
            label: 'STUDY · Muốn học tập',
            description:
              'Kỹ năng, ngoại ngữ, kinh doanh và các cuốn giúp bạn lên level.',
          },
          ADVENTURE: {
            label: 'ADVENTURE · Muốn phiêu lưu',
            description:
              'Fantasy, hành trình, lịch sử và cảm giác muốn đi thật xa.',
          },
          MYSTERY: {
            label: 'MYSTERY · Muốn bí ẩn',
            description:
              'Trinh thám, kinh dị, bí mật và các cú twist giữ bạn đọc tiếp.',
          },
          HEALING: {
            label: 'HEALING · Muốn chữa lành',
            description:
              'Self-help, tâm lý, chữa lành và năng lượng dịu hơn một chút.',
          },
        },
        budgets: {
          UNDER_100: {
            label: 'Dưới 100.000đ',
            description:
              'Ưu tiên các cuốn giá mềm để chốt nhanh, ít đắn đo.',
          },
          FROM_100_TO_200: {
            label: 'Từ 100.000đ đến 200.000đ',
            description:
              'Khoảng ngân sách cân bằng nhất cho phần lớn đầu sách phổ biến.',
          },
          ABOVE_200: {
            label: 'Trên 200.000đ',
            description:
              'Mở rộng sang các cuốn dày, bản đẹp hoặc đầu sách premium hơn.',
          },
        },
        readingTimes: {
          SHORT: {
            label: 'SHORT · Đọc nhanh',
            description:
              'Ưu tiên sách khoảng 220 trang trở xuống cho một nhịp đọc nhẹ.',
          },
          MEDIUM: {
            label: 'MEDIUM · Vừa phải',
            description:
              'Ưu tiên sách khoảng 221-380 trang, đủ đã mà không quá dài.',
          },
          LONG: {
            label: 'LONG · Đọc lâu dài',
            description:
              'Ưu tiên sách từ 381 trang trở lên cho một hành trình đọc sâu.',
          },
        },
        weakDataHint:
          'Má»™t sá»‘ sĂ¡ch chÆ°a cĂ³ sá»‘ trang nĂªn káº¿t quáº£ cĂ³ thá»ƒ dá»±a nhiá»u hÆ¡n vĂ o Ä‘Ă¡nh giĂ¡ vĂ  Ä‘á»™ phá»• biáº¿n.',
        reasons: {
          MOOD: 'Đúng mood bạn chọn',
          BUDGET: 'Khớp ngân sách',
          READING_TIME: 'Vừa độ dày mong muốn',
          HIGH_RATING: 'Đánh giá tốt',
          POPULAR_PICK: 'Nhiều người chọn',
          FRESH_PICK: 'Sách mới lên kệ',
        },
      },
    },
    cart: {
      title: 'Giỏ hàng của bạn',
      itemCount: '{count} sách trong giỏ',
      emptyTitle: 'Giỏ hàng của bạn trống',
      summaryTitle: 'Tóm tắt đơn hàng',
      selectAll: 'Chọn tất cả ({selected}/{total})',
      selectAllAria: 'Chọn tất cả sách trong giỏ',
      selectItemAria: 'Chọn {title} để thanh toán',
      removeSelected: 'Xóa các mục đã chọn',
      orderTitle: 'Đơn hàng của bạn',
      selectedCount: 'Đã chọn {selected}/{total} sách',
      selectedSubtotal: 'Tạm tính ({count} sách)',
      totalPayment: 'Tổng thanh toán',
      checkoutSelected: 'Thanh toán sách đã chọn',
      secureCheckout: 'Thanh toán an toàn và bảo mật',
      inStock: 'Còn hàng',
      qualityTitle: 'Sách chính hãng 100%',
      qualityDescription: 'Cam kết chất lượng',
      returnTitle: 'Đổi trả dễ dàng',
      returnDescription: 'Trong 7 ngày',
      deliveryTitle: 'Giao hàng nhanh chóng',
      deliveryDescription: 'Toàn quốc',
      loginRequired: 'Vui lòng đăng nhập để sử dụng giỏ hàng',
      fetchError: 'Không tải được giỏ hàng',
      updateError: 'Không thể cập nhật giỏ hàng',
      itemTypes: {
        digitalAsset: {
          badge: 'T\u00e0i s\u1ea3n s\u1ed1',
          quantity: '1 quy\u1ec1n truy c\u1eadp',
        },
        physicalBook: {
          badge: 'S\u00e1ch gi\u1ea5y',
          quantity: 'S\u1ed1 l\u01b0\u1ee3ng v\u1eadt l\u00fd',
        },
      },
    },
    checkout: {
      title: 'Thanh toán',
      emptyTitle: 'Giỏ hàng trống',
      emptyDescription: 'Hãy thêm sách vào giỏ trước khi thanh toán',
      shippingInfoTitle: 'Thông tin giao hàng',
      fullName: 'Họ và tên',
      address: 'Địa chỉ',
      city: 'Tỉnh/Thành phố',
      district: 'Quận/Huyện',
      ward: 'Phường/Xã',
      newAddressTitle: 'Dùng địa chỉ mới',
      newAddressDescription: 'Nhập thông tin nhận hàng mới cho đơn này',
      couponCode: 'Mã giảm giá',
      couponPlaceholder: 'Nhập mã nếu có',
      paymentMethodTitle: 'Phương thức thanh toán',
      paymentMethodNotice: 'Chọn phương thức thanh toán phù hợp để hoàn tất đơn hàng.',
      missingInfo: 'Vui lòng điền đầy đủ thông tin',
      success: 'Đặt hàng thành công!',
      error: 'Có lỗi xảy ra. Vui lòng thử lại.',
      submit: 'Đặt hàng',
      orderSummary: 'Tóm tắt đơn hàng',
      quantityShort: 'SL: {count}',
      shippingAddressTitle: 'Địa chỉ giao hàng',
      changeAddress: 'Thay đổi',
      addAddress: 'Thêm địa chỉ',
      addNewAddress: 'Thêm địa chỉ mới',
      chooseSavedAddress: 'Chọn địa chỉ đã lưu',
      chooseAddressTitle: 'Chọn địa chỉ giao hàng',
      chooseAddressDescription: 'Chỉ địa chỉ được chọn sẽ dùng cho đơn hàng này.',
      useThisAddress: 'Dùng địa chỉ này',
      cancel: 'Hủy',
      close: 'Đóng',
      defaultAddress: 'Mặc định',
      noAddressTitle: 'Chưa có địa chỉ giao hàng',
      noAddressDescription: 'Thêm địa chỉ giao hàng trước khi đặt đơn.',
      newAddressHeading: 'Địa chỉ giao hàng mới',
      shippingMethodTitle: 'Phương thức vận chuyển',
      homeDelivery: 'Giao hàng tận nơi',
      deliveryDescription: 'Dự kiến giao trong 2-4 ngày làm việc',
      storePickup: 'Đến cửa hàng nhận',
      pickupDescription: 'Hiện chưa hỗ trợ',
      noteTitle: 'Lời nhắn cho cửa hàng',
      noteLabel: 'Ghi chú',
      notePlaceholder: 'Thêm ghi chú về thời gian nhận hoặc yêu cầu cho đơn',
      bankTransferQr: 'Chuyển khoản SePay',
      bankTransferQrDescription: 'Đặt đơn trước, sau đó chuyển khoản theo hướng dẫn để hoàn tất',
      cashOnDelivery: 'Thanh toán khi nhận hàng',
      cashOnDeliveryDescription: 'Bạn thanh toán tiền mặt khi nhận được đơn hàng',
      chooseCoupon: 'Chọn mã',
      selectedCouponPrefix: 'Mã đã chọn:',
      productTotal: 'Tổng tiền sản phẩm',
      shippingFeeTotal: 'Phí vận chuyển',
      shippingDiscount: 'Giảm phí vận chuyển',
      couponDiscount: 'Giảm giá',
      chooseCouponTitle: 'Chọn mã giảm giá',
      couponInputPlaceholder: 'Nhập mã giảm giá',
      applyCoupon: 'Áp dụng mã',
      useCoupon: 'Dùng mã',
      shippingCoupons: 'Mã ship',
      bookCoupons: 'Mã giảm tiền sách',
      couponLoading: 'Đang tải mã giảm giá...',
      noCoupons: 'Chưa có mã giảm giá phù hợp',
      couponMinOrder: 'Đơn tối thiểu {amount}',
      couponMaxDiscount: 'Giảm tối đa {amount}',
      couponUsage: 'Đã dùng {used}/{limit}',
      couponUsageNoLimit: 'Đã dùng {used}',
      couponUnavailable: 'Đơn hàng chưa đạt giá trị tối thiểu',
    },
    orderConfirmation: {
      title: 'Đặt hàng thành công!',
      description:
        'Cảm ơn bạn đã mua hàng. Đơn hàng của bạn đã được xác nhận và sẽ sớm được giao.',
      emailNotice:
        'Bạn sẽ nhận được email xác nhận với chi tiết đơn hàng trong vài phút.',
      orderId: 'Mã đơn hàng',
      receiver: 'Người nhận',
      paymentMethod: 'Thanh toán',
      paymentStatus: 'Tình trạng thanh toán',
      total: 'Tổng thanh toán',
    },
    orderConfirmationBankTransfer: {
      waitingTitle: 'Ch\u1edd thanh to\u00e1n chuy\u1ec3n kho\u1ea3n',
      waitingDescription:
        '\u0110\u01a1n h\u00e0ng \u0111\u00e3 \u0111\u01b0\u1ee3c t\u1ea1o. H\u00e3y chuy\u1ec3n kho\u1ea3n \u0111\u00fang n\u1ed9i dung b\u00ean d\u01b0\u1edbi \u0111\u1ec3 thanh to\u00e1n \u0111\u01b0\u1ee3c x\u00e1c nh\u1eadn t\u1ef1 \u0111\u1ed9ng.',
      paidTitle: 'Thanh to\u00e1n th\u00e0nh c\u00f4ng',
      paidDescription:
        'Thanh to\u00e1n c\u1ee7a \u0111\u01a1n h\u00e0ng n\u00e0y \u0111\u00e3 \u0111\u01b0\u1ee3c x\u00e1c nh\u1eadn.',
      failedTitle: 'Thanh to\u00e1n th\u1ea5t b\u1ea1i',
      failedDescription:
        'Giao d\u1ecbch ch\u01b0a th\u00e0nh c\u00f4ng. H\u00e3y ki\u1ec3m tra l\u1ea1i th\u00f4ng tin chuy\u1ec3n kho\u1ea3n.',
      cancelledTitle: 'Thanh to\u00e1n \u0111\u00e3 h\u1ee7y',
      cancelledDescription:
        'Giao d\u1ecbch \u0111\u00e3 b\u1ecb h\u1ee7y. H\u00e3y t\u1ea1o \u0111\u01a1n h\u00e0ng m\u1edbi n\u1ebfu b\u1ea1n v\u1eabn mu\u1ed1n mua s\u00e1ch.',
      pollingNotice:
        '\u0110ang ki\u1ec3m tra tr\u1ea1ng th\u00e1i thanh to\u00e1n m\u1ed7i 4 gi\u00e2y.',
      transferInstructionTitle: 'H\u01b0\u1edbng d\u1eabn chuy\u1ec3n kho\u1ea3n',
      transferInstructionDescription:
        'D\u00f9ng th\u00f4ng tin t\u00e0i kho\u1ea3n b\u00ean d\u01b0\u1edbi v\u00e0 gi\u1eef nguy\u00ean n\u1ed9i dung chuy\u1ec3n kho\u1ea3n.',
      bankNameLabel: 'Ng\u00e2n h\u00e0ng',
      accountNumberLabel: 'S\u1ed1 t\u00e0i kho\u1ea3n',
      accountNameLabel: 'Ch\u1ee7 t\u00e0i kho\u1ea3n',
      transferContentLabel: 'N\u1ed9i dung chuy\u1ec3n kho\u1ea3n',
      transferContentHint:
        'H\u00e3y copy \u0111\u00fang n\u1ed9i dung n\u00e0y \u0111\u1ec3 SePay \u0111\u1ed1i chi\u1ebfu ch\u00ednh x\u00e1c.',
      copyButton: 'Copy n\u1ed9i dung',
      copySuccess: '\u0110\u00e3 copy n\u1ed9i dung chuy\u1ec3n kho\u1ea3n.',
      copyError: 'Kh\u00f4ng th\u1ec3 copy n\u1ed9i dung chuy\u1ec3n kho\u1ea3n.',
      qrTitle: 'Khu v\u1ef1c QR thanh to\u00e1n',
      qrDescription:
        'QR \u0111\u01b0\u1ee3c t\u1ea1o \u0111\u1ed9ng theo s\u1ed1 ti\u1ec1n v\u00e0 n\u1ed9i dung chuy\u1ec3n kho\u1ea3n c\u1ee7a \u0111\u01a1n h\u00e0ng n\u1ebfu c\u1ea5u h\u00ecnh ng\u00e2n h\u00e0ng \u0111\u1ea7y \u0111\u1ee7.',
      qrUnavailableTitle: 'Ch\u01b0a t\u1ea1o \u0111\u01b0\u1ee3c QR \u0111\u1ed9ng',
      qrUnavailableDescription:
        'C\u1ea5u h\u00ecnh ng\u00e2n h\u00e0ng \u0111ang thi\u1ebfu, vui l\u00f2ng chuy\u1ec3n kho\u1ea3n th\u1ee7 c\u00f4ng d\u00f9ng th\u00f4ng tin hi\u1ec3n th\u1ecb \u1edf tr\u00ean.',
      qrImageErrorTitle: 'Kh\u00f4ng t\u1ea3i \u0111\u01b0\u1ee3c \u1ea3nh QR',
      qrImageErrorDescription:
        'D\u1ecbch v\u1ee5 QR t\u1ea1m th\u1eddi kh\u00f4ng t\u1ea3i \u0111\u01b0\u1ee3c. Vui l\u00f2ng chuy\u1ec3n kho\u1ea3n th\u1ee7 c\u00f4ng d\u00f9ng \u0111\u00fang th\u00f4ng tin hi\u1ec3n th\u1ecb \u1edf tr\u00ean.',
      qrFallbackNoticeTitle: '\u0110ang d\u00f9ng QR fallback',
      qrFallbackNoticeDescription:
        'H\u1ec7 th\u1ed1ng \u0111ang d\u00f9ng URL QR c\u0169 v\u00ec ch\u01b0a \u0111\u1ee7 c\u1ea5u h\u00ecnh \u0111\u1ec3 t\u1ea1o VietQR \u0111\u1ed9ng.',
      manualTransferTitle: 'H\u01b0\u1edbng d\u1eabn chuy\u1ec3n kho\u1ea3n th\u1ee7 c\u00f4ng',
      manualTransferDescription:
        'H\u00e3y d\u00f9ng \u0111\u00fang ng\u00e2n h\u00e0ng, s\u1ed1 t\u00e0i kho\u1ea3n, ch\u1ee7 t\u00e0i kho\u1ea3n, s\u1ed1 ti\u1ec1n v\u00e0 n\u1ed9i dung chuy\u1ec3n kho\u1ea3n hi\u1ec7n tr\u00ean trang n\u00e0y.',
      orderSummaryTitle: 'T\u00f3m t\u1eaft thanh to\u00e1n',
      summaryDescription:
        'Th\u00f4ng tin b\u00ean d\u01b0\u1edbi ph\u1ea3n \u00e1nh tr\u1ea1ng th\u00e1i thanh to\u00e1n m\u1edbi nh\u1ea5t c\u1ee7a \u0111\u01a1n h\u00e0ng.',
      orderIdLabel: 'M\u00e3 \u0111\u01a1n h\u00e0ng',
      orderCodeLabel: 'M\u00e3 giao d\u1ecbch',
      totalAmountLabel: 'T\u1ed5ng thanh to\u00e1n',
      paymentMethodLabel: 'Ph\u01b0\u01a1ng th\u1ee9c thanh to\u00e1n',
      paymentStatusLabel: 'Tr\u1ea1ng th\u00e1i thanh to\u00e1n',
      receiverInfoTitle: 'Th\u00f4ng tin ng\u01b0\u1eddi nh\u1eadn',
      receiverNameLabel: 'Ng\u01b0\u1eddi nh\u1eadn',
      receiverPhoneLabel: 'S\u1ed1 \u0111i\u1ec7n tho\u1ea1i',
      receiverAddressLabel: '\u0110\u1ecba ch\u1ec9',
      viewOrdersButton: 'Xem \u0111\u01a1n h\u00e0ng c\u1ee7a t\u00f4i',
      continueShoppingButton: 'Ti\u1ebfp t\u1ee5c mua s\u1eafm',
      loadingNotice: '\u0110ang t\u1ea3i tr\u1ea1ng th\u00e1i thanh to\u00e1n m\u1edbi nh\u1ea5t...',
      emptyValue: 'Kh\u00f4ng c\u00f3',
      bankFallback: 'C\u1eadp nh\u1eadt VITE_BANK_TRANSFER_BANK_NAME',
      accountNumberFallback:
        'C\u1eadp nh\u1eadt VITE_BANK_TRANSFER_ACCOUNT_NUMBER',
      accountNameFallback: 'C\u1eadp nh\u1eadt VITE_BANK_TRANSFER_ACCOUNT_NAME',
    },
    orderConfirmationCashOnDelivery: {
      waitingTitle:
        '\u0110\u01a1n h\u00e0ng \u0111\u00e3 t\u1ea1o v\u1edbi thanh to\u00e1n khi nh\u1eadn h\u00e0ng',
      waitingDescription:
        '\u0110\u01a1n h\u00e0ng c\u1ee7a b\u1ea1n \u0111\u00e3 \u0111\u01b0\u1ee3c t\u1ea1o th\u00e0nh c\u00f4ng. B\u1ea1n s\u1ebd thanh to\u00e1n khi nh\u1eadn h\u00e0ng, v\u00ec v\u1eady h\u1ec7 th\u1ed1ng kh\u00f4ng t\u1ea1o b\u01b0\u1edbc chuy\u1ec3n kho\u1ea3n SePay cho \u0111\u01a1n n\u00e0y.',
      instructionTitle: 'H\u01b0\u1edbng d\u1eabn thanh to\u00e1n khi nh\u1eadn h\u00e0ng',
      instructionDescription:
        'H\u00e3y gi\u1eef \u0111i\u1ec7n tho\u1ea1i s\u1eb5n s\u00e0ng v\u00ec c\u1eeda h\u00e0ng ho\u1eb7c \u0111\u01a1n v\u1ecb giao h\u00e0ng c\u00f3 th\u1ec3 li\u00ean h\u1ec7 tr\u01b0\u1edbc khi giao.',
      paymentLabel: 'Th\u1eddi \u0111i\u1ec3m thanh to\u00e1n',
      paymentValue: 'Thanh to\u00e1n khi nh\u1eadn \u0111\u01b0\u1ee3c ki\u1ec7n h\u00e0ng.',
      deliveryFeeLabel: 'Ch\u00ednh s\u00e1ch ph\u00ed giao h\u00e0ng',
      deliveryFeeValue:
        'Giao t\u1eadn n\u01a1i t\u00ednh ph\u00ed 30.000\u0111 v\u00e0 mi\u1ec5n ph\u00ed t\u1eeb 200.000\u0111. Nh\u1eadn t\u1ea1i c\u1eeda h\u00e0ng v\u1eabn mi\u1ec5n ph\u00ed.',
      nextStepLabel: 'C\u1eadp nh\u1eadt ti\u1ebfp theo',
      nextStepValue:
        'Theo d\u00f5i tr\u1ea1ng th\u00e1i x\u00e1c nh\u1eadn v\u00e0 giao h\u00e0ng trong l\u1ecbch s\u1eed \u0111\u01a1n h\u00e0ng.',
      noteTitle: 'Tr\u01b0\u1edbc khi nh\u1eadn h\u00e0ng',
      noteDescription:
        'N\u00ean chu\u1ea9n b\u1ecb tr\u01b0\u1edbc s\u1ed1 ti\u1ec1n c\u1ea7n thanh to\u00e1n n\u1ebfu c\u00f3 th\u1ec3 v\u00e0 ki\u1ec3m tra t\u00ecnh tr\u1ea1ng ki\u1ec7n h\u00e0ng tr\u01b0\u1edbc khi tr\u1ea3 ti\u1ec1n cho shipper.',
      summaryDescription:
        '\u0110\u01a1n h\u00e0ng n\u00e0y s\u1ebd \u0111\u01b0\u1ee3c thanh to\u00e1n b\u1eb1ng ti\u1ec1n m\u1eb7t khi giao \u0111\u1ebfn.',
    },
    library: {
      page: {
        acquiredAtLabel: 'Nh\u1eadn l\u00fac',
        allFormats: 'T\u1ea5t c\u1ea3 \u0111\u1ecbnh d\u1ea1ng',
        allStatuses: 'T\u1ea5t c\u1ea3 tr\u1ea1ng th\u00e1i',
        bookLabel: 'S\u00e1ch',
        countLabel: 'N\u1ed9i dung ph\u00f9 h\u1ee3p',
        description:
          'Xem l\u1ea1i ebook v\u00e0 s\u00e1ch n\u00f3i t\u1eeb c\u00e1c \u0111\u01a1n h\u00e0ng \u0111\u00e3 ho\u00e0n t\u1ea5t, r\u1ed3i ti\u1ebfp t\u1ee5c \u0111\u1ecdc ho\u1eb7c nghe ngay t\u1ea1i \u0111\u00e2y.',
        downloadAllowedLabel: 'C\u00f3 th\u1ec3 t\u1ea3i v\u1ec1',
        downloadReadyBadge: 'T\u1ea3i v\u1ec1 t\u1eeb trang chi ti\u1ebft',
        emptyDescription:
          'Kh\u00f4ng c\u00f3 ebook ho\u1eb7c s\u00e1ch n\u00f3i n\u00e0o kh\u1edbp v\u1edbi b\u1ed9 l\u1ecdc hi\u1ec7n t\u1ea1i.',
        emptyTitle: 'Ch\u01b0a t\u00ecm th\u1ea5y n\u1ed9i dung ph\u00f9 h\u1ee3p',
        expiresAtLabel: 'H\u1ebft h\u1ea1n l\u00fac',
        formatFilterLabel: 'L\u1ecdc theo \u0111\u1ecbnh d\u1ea1ng',
        loadMore: 'T\u1ea3i th\u00eam',
        loading: '\u0110ang t\u1ea3i th\u01b0 vi\u1ec7n s\u1ed1...',
        loadingMore: '\u0110ang t\u1ea3i th\u00eam...',
        noExpiry: 'Kh\u00f4ng gi\u1edbi h\u1ea1n',
        noProgress: 'Ch\u01b0a c\u00f3 ti\u1ebfn \u0111\u1ed9',
        openSampleLabel: 'M\u1edf b\u1ea3n m\u1eabu',
        openingSampleLabel: '\u0110ang m\u1edf b\u1ea3n m\u1eabu...',
        priceLabel: 'Gi\u00e1',
        progressLabel: 'Ti\u1ebfn \u0111\u1ed9 \u0111\u1ecdc',
        readNowLabel: '\u0110\u1ecdc ngay',
        sampleAvailableLabel: 'C\u00f3 b\u1ea3n m\u1eabu',
        sampleError: 'Kh\u00f4ng th\u1ec3 m\u1edf b\u1ea3n m\u1eabu.',
        searchPlaceholder: 'T\u00ecm theo t\u00ean s\u00e1ch ho\u1eb7c \u0111\u1ecbnh d\u1ea1ng...',
        statusFilterLabel: 'L\u1ecdc theo tr\u1ea1ng th\u00e1i \u0111\u1ecdc',
        title: 'Th\u01b0 vi\u1ec7n s\u1ed1 c\u1ee7a t\u00f4i',
        totalOwnedLabel: '{count} n\u1ed9i dung trong th\u01b0 vi\u1ec7n',
        viewDetailLabel: 'Xem chi ti\u1ebft',
      },
      detail: {
        accessDescription:
          'M\u1edf tr\u00ecnh \u0111\u1ecdc ri\u00eang ho\u1eb7c y\u00eau c\u1ea7u URL t\u1ea3i t\u1ea1m th\u1eddi khi t\u00e0i s\u1ea3n \u0111\u00e3 \u0111\u01b0\u1ee3c c\u1ea5p quy\u1ec1n truy c\u1eadp.',
        accessTitle: 'Truy c\u1eadp t\u00e0i s\u1ea3n',
        acquiredAtLabel: 'Nh\u1eadn l\u00fac',
        assetUpdatedAtLabel: 'T\u00e0i s\u1ea3n c\u1eadp nh\u1eadt l\u00fac',
        backLabel: 'Quay l\u1ea1i th\u01b0 vi\u1ec7n s\u1ed1',
        bookDescriptionLabel: 'M\u00f4 t\u1ea3 s\u00e1ch',
        bookLabel: 'S\u00e1ch',
        currentPageLabel: 'Trang hi\u1ec7n t\u1ea1i',
        downloadAllowed: 'Cho ph\u00e9p t\u1ea3i',
        downloadLabel: 'T\u1ea3i t\u00e0i s\u1ea3n',
        expiresAtLabel: 'H\u1ebft h\u1ea1n l\u00fac',
        fileNameLabel: 'T\u00ean t\u1ec7p',
        fileSizeLabel: 'Dung l\u01b0\u1ee3ng',
        loading: '\u0110ang t\u1ea3i chi ti\u1ebft t\u00e0i s\u1ea3n s\u1ed1...',
        mimeTypeLabel: 'MIME type',
        noDescription: 'M\u00f4 t\u1ea3 s\u00e1ch hi\u1ec7n ch\u01b0a c\u00f3 d\u1eef li\u1ec7u.',
        noExpiry: 'Kh\u00f4ng gi\u1edbi h\u1ea1n',
        noProgress: 'Ch\u01b0a c\u00f3 ti\u1ebfn \u0111\u1ed9',
        noSourceOrder: 'Kh\u00f4ng c\u00f3 \u0111\u01a1n ngu\u1ed3n',
        openReaderLabel: 'M\u1edf tr\u00ecnh \u0111\u1ecdc',
        openSampleLabel: 'M\u1edf b\u1ea3n m\u1eabu',
        openingDownloadLabel: '\u0110ang l\u1ea5y URL t\u1ea3i...',
        openingSampleLabel: '\u0110ang m\u1edf b\u1ea3n m\u1eabu...',
        positionDataLabel: 'Position data',
        positionDataPlaceholder:
          'JSON ho\u1eb7c chu\u1ed7i \u0111\u00e1nh d\u1ea5u v\u1ecb tr\u00ed n\u1ebfu reader c\u1ee7a b\u1ea1n c\u00f3 l\u01b0u tr\u1ea1ng th\u00e1i',
        priceLabel: 'Gi\u00e1',
        progressDescription:
          'B\u1ea1n v\u1eabn c\u00f3 th\u1ec3 \u0111\u1ed3ng b\u1ed9 ti\u1ebfn \u0111\u1ed9 \u0111\u1ecdc th\u1ee7 c\u00f4ng t\u1eeb m\u00e0n h\u00ecnh n\u00e0y n\u1ebfu tr\u00ecnh \u0111\u1ecdc ch\u01b0a t\u1ef1 g\u1eedi tr\u1ea1ng th\u00e1i.',
        progressPercentLabel: 'Ph\u1ea7n tr\u0103m ti\u1ebfn \u0111\u1ed9',
        progressTitle: 'Ti\u1ebfn \u0111\u1ed9 \u0111\u1ecdc',
        readOnlyBadge: 'Ch\u1ec9 cho ph\u00e9p \u0111\u1ecdc tr\u1ef1c tuy\u1ebfn',
        sampleAvailable: 'C\u00f3 b\u1ea3n m\u1eabu',
        saveProgressLabel: 'L\u01b0u ti\u1ebfn \u0111\u1ed9',
        savingLabel: '\u0110ang l\u01b0u...',
        sourceOrderIdLabel: 'M\u00e3 \u0111\u01a1n ngu\u1ed3n',
      },
      reader: {
        acquiredAtLabel: 'Nh\u1eadn l\u00fac',
        audioDescription:
          'D\u00f9ng tr\u00ecnh ph\u00e1t m\u1eb7c \u0111\u1ecbnh \u0111\u1ec3 nghe s\u00e1ch n\u00f3i.',
        audioFallback:
          'Tr\u00ecnh duy\u1ec7t c\u1ee7a b\u1ea1n ch\u01b0a h\u1ed7 tr\u1ee3 ph\u00e1t \u00e2m thanh.',
        audioTitle: 'Tr\u00ecnh ph\u00e1t \u00e2m thanh',
        backLabel: 'Quay l\u1ea1i chi ti\u1ebft t\u00e0i s\u1ea3n',
        bookLabel: 'S\u00e1ch',
        expiresAtLabel: 'H\u1ebft h\u1ea1n l\u00fac',
        fileNameLabel: 'T\u00ean t\u1ec7p',
        loading: '\u0110ang t\u1ea3i tr\u00ecnh \u0111\u1ecdc...',
        noExpiry: 'Kh\u00f4ng gi\u1edbi h\u1ea1n',
        notFound: 'Kh\u00f4ng th\u1ec3 m\u1edf t\u00e0i s\u1ea3n s\u1ed1 n\u00e0y.',
        openNewTab: 'M\u1edf tab m\u1edbi',
        updatedAtLabel: 'T\u00e0i s\u1ea3n c\u1eadp nh\u1eadt l\u00fac',
      },
      progress: {
        validation: {
          progressPercentRange:
            'Ti\u1ebfn \u0111\u1ed9 ph\u1ea3i n\u1eb1m trong kho\u1ea3ng t\u1eeb 0 \u0111\u1ebfn 100.',
          currentPageNonNegativeInteger:
            'Trang hi\u1ec7n t\u1ea1i ph\u1ea3i l\u00e0 s\u1ed1 nguy\u00ean kh\u00f4ng \u00e2m.',
        },
        updateSuccess: '\u0110\u00e3 c\u1eadp nh\u1eadt ti\u1ebfn \u0111\u1ed9 \u0111\u1ecdc.',
      },
    },
    orderDetail: {
      orderHistory: 'L\u1ecbch s\u1eed \u0111\u01a1n h\u00e0ng',
      orderSummary: 'T\u00f3m t\u1eaft \u0111\u01a1n h\u00e0ng',
      productTotal: 'T\u1ed5ng ti\u1ec1n h\u00e0ng',
      itemsTitle: 'S\u1ea3n ph\u1ea9m trong \u0111\u01a1n',
      digitalItemLabel: 'T\u00e0i s\u1ea3n s\u1ed1',
      physicalItemLabel: 'S\u00e1ch gi\u1ea5y',
      openLibraryAsset: 'M\u1edf trong th\u01b0 vi\u1ec7n',
    },
    orderHistoryPage: {
      completedStep: 'Ho\u00e0n t\u1ea5t',
      copyError: 'Kh\u00f4ng th\u1ec3 sao ch\u00e9p m\u00e3 \u0111\u01a1n h\u00e0ng.',
      copyOrderId: 'Sao ch\u00e9p m\u00e3 \u0111\u01a1n h\u00e0ng',
      copySuccess: '\u0110\u00e3 sao ch\u00e9p m\u00e3 \u0111\u01a1n h\u00e0ng.',
      discoverDescription:
        'H\u00e0ng ng\u00e0n t\u1ef1a s\u00e1ch ch\u1ea5t l\u01b0\u1ee3ng \u0111ang ch\u1edd b\u1ea1n kh\u00e1m ph\u00e1.',
      discoverTitle: 'Kh\u00e1m ph\u00e1 th\u00eam s\u00e1ch hay',
      exploreNow: 'Kh\u00e1m ph\u00e1 ngay',
      pendingStep: 'Ch\u1edd x\u00e1c nh\u1eadn',
      processingStep: '\u0110ang x\u1eed l\u00fd',
      shippingStep: '\u0110ang giao h\u00e0ng',
    },
    notifications: {
      emptyContent: 'Không có nội dung',
      realtimeConnected: 'Realtime đang bật',
      realtimeFallback: 'Đang dùng REST fallback',
      newNotificationFallback: 'Thông báo mới',
      errors: {
        fetch: 'Không tải được thông báo',
        update: 'Không cập nhật được thông báo',
        delete: 'Không xóa được thông báo',
      },
      bell: {
        title: 'Thông báo',
        empty: 'Chưa có thông báo nào',
        viewAll: 'Xem tất cả',
        delete: 'Xóa',
        loading: 'Đang tải thông báo...',
        openLabel: 'Mở thông báo',
        deleteSuccess: 'Đã xóa thông báo',
      },
      page: {
        title: 'Thông báo của bạn',
        description:
          'Theo dõi thông báo mới, cập nhật đơn hàng và giao hàng theo thời gian thực.',
        all: 'Tất cả',
        unread: 'Chưa đọc',
        empty: 'Không có thông báo nào phù hợp',
        markAll: 'Đánh dấu tất cả đã đọc',
        markRead: 'Đánh dấu đã đọc',
        loading: 'Đang tải thông báo...',
        loadMore: 'Tải thêm',
        delete: 'Xóa',
        open: 'Mở thông báo',
        deleteSuccess: 'Đã xóa thông báo',
      },
    },
    chat: {
      errors: {
        loadConversations: 'Không tải được cuộc trò chuyện hỗ trợ',
        loadMessages: 'Không tải được lịch sử tin nhắn',
      },
      customer: {
        title: 'Hỗ trợ khách hàng',
        chooseModeTitle: 'Bạn muốn được hỗ trợ theo cách nào?',
        chooseModeDescription:
          'Có thể đổi lựa chọn bất cứ lúc nào trong cuộc trò chuyện.',
        modeSwitcherLabel: 'Kênh hỗ trợ',
        aiMode: 'Chat với trợ lý AI',
        aiModeDescription:
          'Nhận phản hồi ngay về sách và cách sử dụng website.',
        humanMode: 'Chat với nhân viên',
        humanModeDescription:
          'Trao đổi trực tiếp với đội ngũ hỗ trợ qua kết nối realtime.',
        humanAgent: 'Nhân viên SáchVui',
        aiAssistant: 'Trợ lý AI SáchVui',
        aiReplying: 'Trợ lý AI đang soạn câu trả lời...',
        aiHandoff:
          'Trợ lý AI đang tạm nghỉ. Tin nhắn của bạn đã được chuyển cho nhân viên hỗ trợ.',
        aiLimitReached:
          'Bạn đã dùng hết lượt AI hôm nay. Nhân viên hỗ trợ sẽ tiếp tục phản hồi trong cuộc trò chuyện này.',
        defaultSubject: 'Hỗ trợ khách hàng',
        newConversation: 'Cuộc trò chuyện mới',
        subject: 'Chủ đề',
        subjectPlaceholder: 'Ví dụ: Hỏi về đơn hàng #1234',
        sendPlaceholder: 'Nhập nội dung cần hỗ trợ...',
        aiSendPlaceholder: 'Nhập câu hỏi cho trợ lý AI...',
        humanSendPlaceholder: 'Nhập nội dung cần nhân viên hỗ trợ...',
        send: 'Gửi tin nhắn',
        loadingMessages: 'Đang tải tin nhắn...',
        loadOlderMessages: 'Tải tin nhắn cũ hơn',
        emptyMessages:
          'Chưa có tin nhắn nào. Hãy mở đầu bằng vấn đề cần hỗ trợ.',
        emptyConversations: 'Bạn chưa có cuộc trò chuyện nào.',
        closedNotice:
          'Cuộc trò chuyện này đã đóng. Tạo cuộc trò chuyện mới nếu bạn cần hỗ trợ tiếp.',
        closeConversation: 'Đóng cuộc trò chuyện',
        realtimeConnected: 'Realtime đã kết nối',
        realtimeFallback: 'Realtime đang kết nối lại',
        refresh: 'Tải lại',
        openChat: 'Mở chat hỗ trợ',
        closeChat: 'Đóng chat hỗ trợ',
        incomingTitle: 'Tin nhắn hỗ trợ mới',
        viewAllNotifications: 'Xem thông báo',
      },
    },

    adminChat: {
      title: 'Chat hỗ trợ khách hàng',
      description:
        'Theo dõi hội thoại hỗ trợ theo thời gian thực, phân công nhân sự tự động hoặc mở lại cuộc trò chuyện từ một màn hình.',
      totalConversations: 'Tổng hội thoại',
      unreadCount: 'Chưa đọc',
      openCount: 'Đang mở',
      connected: 'Realtime đang bật',
      fallback: 'Đang dùng REST fallback',
      listTitle: 'Danh sách hội thoại',
      searchPlaceholder: 'Tìm theo khách hàng, email, nội dung...',
      statusLabel: 'Trạng thái',
      statusAll: 'Tất cả',
      statusOpen: 'Đang mở',
      statusPending: 'Đang chờ',
      statusClosed: 'Đã đóng',
      emptyConversations: 'Không có hội thoại phù hợp bộ lọc hiện tại.',
      loadMore: 'Tải thêm',
      loadingList: 'Đang tải hội thoại...',
      loadingMessages: 'Đang tải tin nhắn...',
      messageEmpty: 'Chưa có tin nhắn trong hội thoại này.',
      replyPlaceholder: 'Nhập nội dung phản hồi cho khách hàng...',
      send: 'Gửi phản hồi',
      closeConversation: 'Đóng hội thoại',
      reopenConversation: 'Mở lại hội thoại',
      customer: 'Khách hàng',
      assignee: 'Phụ trách',
      unassigned: 'Chưa phân công',
      assignToSelf: 'Nhận xử lý',
      assignButton: 'Phân công',
      staffPlaceholder: 'Chọn nhân viên',
      priority: 'Độ ưu tiên',
      target: 'Nguồn liên quan',
      createdAt: 'Tạo lúc',
      updatedAt: 'Cập nhật lúc',
      noConversationSelected: 'Chọn một hội thoại để bắt đầu xử lý.',
      closedNotice: 'Hội thoại đã đóng. Mở lại nếu cần tiếp tục hỗ trợ.',
      noMessagesYet: 'Khách hàng chưa gửi tin nhắn.',
      loadError: 'Không tải được danh sách chat hỗ trợ',
    },
    orders: {
      title: 'Lịch sử đơn hàng',
      totalCount: 'Tổng cộng {count} đơn hàng',
      emptyTitle: 'Bạn chưa có đơn hàng nào',
      emptyDescription: 'Sau khi đặt hàng, các đơn sẽ hiển thị ở đây.',
      detailTitle: 'Chi tiết đơn hàng',
      orderId: 'Mã đơn',
      createdAt: 'Ngày tạo',
      receiverName: 'Người nhận',
      receiverPhone: 'Số điện thoại nhận hàng',
      receiverAddress: 'Địa chỉ nhận hàng',
      itemsTitle: 'Sản phẩm trong đơn',
      subtotal: 'Tổng tiền hàng',
      discount: 'Giảm giá',
      shippingFee: 'Phí vận chuyển',
      finalAmount: 'Thanh toán cuối cùng',
      paymentMethod: 'Phương thức thanh toán',
      paymentStatus: 'Trạng thái thanh toán',
      status: 'Trạng thái đơn hàng',
      viewDetail: 'Xem chi tiết',
    },
    notFound: {
      title: 'Trang không tìm thấy',
      description: 'Xin lỗi, trang bạn tìm kiếm không tồn tại.',
    },
    admin: {
      sidebar: {
        title: 'Quản trị SáchVui',
        books: 'Quản lý sách',
        orders: 'Quản lý đơn hàng',
        categories: 'Quản lý danh mục',
        authors: 'Quản lý tác giả',
        publishers: 'Quản lý nhà xuất bản',
        users: 'Quản lý người dùng',
        roles: 'Quản lý vai trò',
        permissions: 'Quản lý quyền',
        promotions: 'Quản lý khuyến mãi',
        importReceipts: 'Quản lý nhập kho',
        inventory: 'Quản lý tồn kho',
        shipments: 'Quản lý giao hàng',
        reviews: 'Quản lý đánh giá',
        notifications: 'Quản lý thông báo',
        chat: 'Chat hỗ trợ',
        suppliers: 'Quản lý nhà cung cấp',
        customers: 'Quản lý khách hàng',
        staff: 'Quản lý nhân viên',
        settings: 'Cài đặt tài khoản',
        adminAccount: 'Tài khoản quản trị',
        references: 'Danh mục tham chiếu',
      },
      dashboard: {
        description: 'Tổng quan thống kê cửa hàng',
        recentOrders: 'Đơn hàng gần đây',
        emptyOrders: 'Chưa có đơn hàng nào để hiển thị',
        stats: {
          totalBooks: 'Tổng sách',
          ordersToday: 'Đơn hàng hôm nay',
          customers: 'Khách hàng',
          revenueMonth: 'Doanh thu tháng',
        },
        columns: {
          orderId: 'Mã đơn',
          customer: 'Khách hàng',
          total: 'Tổng tiền',
          status: 'Trạng thái',
          date: 'Ngày',
        },
      },
      books: {
        title: 'Quản lý sách',
        description: 'Quản lý các đầu sách hiển thị trên cửa hàng.',
        sectionLabel: 'S\u00e1ch',
        countLabel: '{count} cuốn sách',
        totalBooks: 'Tổng cộng {count} cuốn sách',
        addBook: 'Thêm sách mới',
        editBook: 'Chỉnh sửa sách',
        detailTitle: 'Chi ti\u1ebft s\u00e1ch',
        createSuccess: 'Đã tạo sách mới',
        updateSuccess: 'Đã cập nhật sách',
        deleteSuccess: 'Đã xóa sách',
        deleteTitle: 'X\u00e1c nh\u1eadn x\u00f3a s\u00e1ch',
        deleteDescription:
          'H\u00e0nh \u0111\u1ed9ng n\u00e0y s\u1ebd x\u00f3a s\u00e1ch kh\u1ecfi h\u1ec7 th\u1ed1ng qu\u1ea3n tr\u1ecb v\u00e0 kh\u00f4ng th\u1ec3 ho\u00e0n t\u00e1c.',
        manageReferenceData: 'Quản lý danh mục, tác giả, NXB',
        manageReferences: 'Quản lý danh mục',
        referencesMissing:
          'Cần có category, author và publisher trước khi tạo sách',
        referencesMissingDescription:
          'Hãy vào màn hình danh mục tham chiếu để tạo dữ liệu nền cho sách.',
        referencesSplitDescription:
          'Mở các trang danh mục, tác giả hoặc nhà xuất bản để tạo dữ liệu dùng cho form sách.',
        previewTitle: 'Xem nhanh hiển thị',
        imageGalleryTitle: 'Ảnh bìa và ảnh chi tiết',
        imageGalleryHelp:
          'Tải nhiều ảnh, chọn một ảnh làm bìa và sắp xếp thứ tự hiển thị trên trang chi tiết sách.',
        imageGalleryEmpty: 'Chưa có ảnh nào cho sách này.',
        addImages: 'Chọn một hoặc nhiều ảnh',
        uploadingImages: 'Đang tải ảnh lên...',
        imageCount: '{count} ảnh',
        setPrimaryImage: 'Đặt làm ảnh bìa',
        primaryImage: 'Ảnh bìa',
        moveImageLeft: 'Chuyển ảnh sang trước',
        moveImageRight: 'Chuyển ảnh ra sau',
        removeImage: 'Xóa ảnh khỏi sách',
        imageAltText: 'Mô tả ảnh',
        imageUploadPartial: 'Có {failed}/{total} ảnh tải lên không thành công.',
        emptyPreviewTitle: 'Tên sách sẽ hiển thị ở đây',
        emptyPreviewDescription:
          'Mô tả ngắn và hình ảnh sẽ được xem trước ngay trên form.',
        emptyDescription: 'S\u00e1ch n\u00e0y ch\u01b0a c\u00f3 m\u00f4 t\u1ea3.',
        confirmDelete: 'Xóa sách "{title}"?',
        formDescription: 'Điền đầy đủ thông tin để lưu thay đổi cho cuốn sách.',
        searchPlaceholder: 'Tìm theo tên sách hoặc tác giả...',
        filterPlaceholder: 'Lọc theo thể loại',
        allCategories: 'Tất cả thể loại',
        showingCount: 'Hi\u1ec3n th\u1ecb {count} tr\u00ean {total} cu\u1ed1n s\u00e1ch',
        stockUnit: 'cu\u1ed1n',
        inStock: 'C\u00f2n h\u00e0ng',
        outOfStock: 'H\u1ebft h\u00e0ng',
        fields: {
          title: 'Tên sách',
          description: 'Mô tả',
          price: 'Giá',
          stockQuantity: 'Tồn kho',
          imageUrl: 'Liên kết ảnh',
          category: 'Thể loại',
          author: 'Tác giả',
          publisher: 'Nhà xuất bản',
        },
        columns: {
          book: 'Sách',
          author: 'Tác giả',
          category: 'Thể loại',
          price: 'Giá',
          stock: 'Số lượng',
          actions: 'Thao tác',
        },
        empty: 'Không tìm thấy sách nào',
      },
      digitalAssets: {
        sectionLabel: 'T\u00e0i s\u1ea3n s\u1ed1',
        title: 'T\u00e0i s\u1ea3n s\u1ed1 c\u1ee7a cu\u1ed1n s\u00e1ch',
        description:
          'Qu\u1ea3n l\u00fd c\u00e1c phi\u00ean b\u1ea3n s\u1ed1 g\u1eafn v\u1edbi cu\u1ed1n s\u00e1ch n\u00e0y ngay trong trang qu\u1ea3n l\u00fd s\u00e1ch.',
        addAsset: 'Th\u00eam t\u00e0i s\u1ea3n',
        loading: '\u0110ang t\u1ea3i t\u00e0i s\u1ea3n s\u1ed1...',
        empty: 'Cu\u1ed1n s\u00e1ch n\u00e0y ch\u01b0a c\u00f3 t\u00e0i s\u1ea3n s\u1ed1 n\u00e0o.',
        published: '\u0110\u00e3 ph\u00e1t h\u00e0nh',
        unpublished: 'Nh\u00e1p',
        downloadAllowed: 'Cho ph\u00e9p t\u1ea3i',
        purchaseAllowed: 'Cho ph\u00e9p mua',
        editAsset: 'S\u1eeda t\u00e0i s\u1ea3n',
        deleteAsset: 'X\u00f3a t\u00e0i s\u1ea3n',
        priceLabel: 'Gi\u00e1',
        mimeTypeLabel: 'MIME type',
        fileSizeLabel: 'Dung l\u01b0\u1ee3ng',
        updatedAtLabel: 'C\u1eadp nh\u1eadt l\u00fac',
        createTitle: 'T\u1ea1o t\u00e0i s\u1ea3n s\u1ed1',
        editTitle: 'C\u1eadp nh\u1eadt t\u00e0i s\u1ea3n s\u1ed1',
        formatLabel: '\u0110\u1ecbnh d\u1ea1ng',
        titleLabel: 'Ti\u00eau \u0111\u1ec1',
        mainFileLabel: 'File ch\u00ednh',
        sampleFileLabel: 'File m\u1eabu',
        mainFileHelp:
          'T\u1ea3i file ch\u00ednh qua file service. Metadata s\u1ebd \u0111\u01b0\u1ee3c l\u1ea5y t\u1eeb file_assets.',
        sampleFileHelp:
          'T\u1ea3i file m\u1eabu n\u1ebfu c\u1ea7n preview. Kh\u00f4ng nh\u1eadp storage key th\u1ee7 c\u00f4ng.',
        fileAssetIdLabel: 'ID file asset',
        sampleFileAssetIdLabel: 'ID file asset m\u1eabu',
        fileNameLabel: 'T\u00ean t\u1ec7p',
        checksumLabel: 'Checksum',
        save: 'L\u01b0u t\u00e0i s\u1ea3n',
        deleteTitle: 'X\u00e1c nh\u1eadn x\u00f3a t\u00e0i s\u1ea3n s\u1ed1',
        confirmDelete: 'X\u00f3a t\u00e0i s\u1ea3n s\u1ed1 "{title}"?',
        notUploaded: 'Ch\u01b0a t\u1ea3i t\u1ec7p',
        noSample: 'Kh\u00f4ng c\u00f3 file m\u1eabu',
        validationError:
          'Vui l\u00f2ng ki\u1ec3m tra ti\u00eau \u0111\u1ec1, gi\u00e1 b\u00e1n v\u00e0 t\u1ec7p \u0111\u00e3 t\u1ea3i l\u00ean.',
        updatedSuccess: '\u0110\u00e3 c\u1eadp nh\u1eadt t\u00e0i s\u1ea3n s\u1ed1.',
        createdSuccess: '\u0110\u00e3 t\u1ea1o t\u00e0i s\u1ea3n s\u1ed1.',
        deletedSuccess: '\u0110\u00e3 x\u00f3a t\u00e0i s\u1ea3n s\u1ed1.',
      },
      orders: {
        title: 'Quản lý đơn hàng',
        totalOrders: 'Tổng cộng {count} đơn hàng',
        updateSuccess: 'Đã cập nhật trạng thái đơn hàng',
        searchPlaceholder:
          'Tìm theo mã đơn, tên khách hàng, số điện thoại...',
        filterLabel: 'Lọc theo trạng thái',
        allStatuses: 'Tất cả trạng thái',
        detailTitle: 'Chi tiết và cập nhật đơn hàng',
        columns: {
          orderId: 'Mã đơn',
          customer: 'Khách hàng',
          phone: 'Số điện thoại',
          products: 'Sản phẩm',
          total: 'Tổng tiền',
          status: 'Trạng thái',
          date: 'Ngày',
          actions: 'Thao tác',
        },
        productCount: '{count} sản phẩm',
        detail: {
          receiverAddress: 'Địa chỉ nhận hàng',
          paymentMethod: 'Thanh toán',
          paymentStatus: 'Tình trạng thanh toán',
          updateStatus: 'Cập nhật trạng thái',
        },
        shipmentAssignment: {
          title: 'Gán shipper',
          description:
            'Chỉ gán cho đơn CONFIRMED hoặc SHIPPING chưa có shipment active.',
          currentShipment: 'Shipment hiện tại',
          shipper: 'Shipper',
          assignedAt: 'Ngày gán',
          activeNotice:
            'Đơn này đã có shipment đang hoạt động. Theo dõi tiếp trong trang Quản lý giao hàng.',
          latestFailed: 'Shipment gần nhất thất bại',
          chooseShipper: 'Chọn shipper',
          noShippers: 'Không có tài khoản shipper',
          assigning: 'Đang gán shipper...',
          assign: 'Gán shipper',
          unavailable:
            'Chỉ hiển thị khu vực gán shipper khi đơn đang ở trạng thái CONFIRMED hoặc SHIPPING.',
          ineligible:
            'Đơn này chưa phù hợp để gán shipper hoặc đang có shipment active',
          chooseShipperError: 'Hãy chọn shipper trước khi gán',
          assignSuccess: 'Đã gán shipper cho đơn hàng',
          assignError: 'Không gán được shipper cho đơn hàng',
        },
      },
      shipmentsPage: {
        title: 'Quản lý giao hàng',
        totalShipments: '{count} shipment trong hệ thống',
        assignTitle: 'Gán shipper cho đơn hàng',
        assignDescription:
          'Chỉ hiển thị các đơn CONFIRMED hoặc SHIPPING chưa có shipment active.',
        ordersReady: '{count} đơn chờ gán',
        orderLabel: 'Đơn hàng',
        noEligibleOrders: 'Không còn đơn phù hợp để gán',
        shipperLabel: 'Shipper',
        noShippers: 'Không có tài khoản shipper',
        assigning: 'Đang gán...',
        assign: 'Gán shipper',
        filterLabel: 'Lọc theo trạng thái',
        allStatuses: 'Tất cả trạng thái',
        metrics: {
          delivering: 'Đang giao',
          delivered: 'Hoàn tất',
          failed: 'Thất bại',
        },
        loading: 'Đang tải shipment...',
        empty: 'Không có shipment phù hợp bộ lọc',
        detailLoading: 'Đang tải chi tiết...',
        detailTitle: 'Chi tiết đơn giao',
        deliveryInfoTitle: 'Thông tin giao hàng',
        timelineTitle: 'Mốc thời gian',
        confirmDelivered: 'Xác nhận đã giao',
        confirming: 'Đang xác nhận...',
        confirmHint:
          'Nút này chỉ bật khi đơn hàng đang ở trạng thái giao hàng.',
        loadError: 'Không tải được dữ liệu giao hàng',
        detailError: 'Không tải được chi tiết đơn giao',
        assignValidationError: 'Hãy chọn đơn hàng và shipper trước khi gán',
        assignSuccess: 'Đã gán shipper cho đơn hàng',
        assignError: 'Không gán được shipper',
        invalidConfirmState:
          'Chỉ có thể xác nhận giao thành công khi shipment đang DELIVERING',
        confirmSuccess: 'Admin đã xác nhận giao hàng thành công',
        confirmError: 'Không xác nhận được trạng thái đã giao',
        columns: {
          shipmentId: 'Mã shipment',
          order: 'Đơn hàng',
          receiver: 'Người nhận',
          shipper: 'Shipper',
          status: 'Trạng thái',
          amount: 'Giá trị',
          assignedAt: 'Ngày gán',
          actions: 'Thao tác',
        },
        detail: {
          orderCode: 'Mã đơn hàng',
          shipper: 'Shipper',
          payment: 'Thanh toán',
          totalAmount: 'Tổng thanh toán',
          receiver: 'Người nhận',
          phone: 'Số điện thoại',
          address: 'Địa chỉ',
          orderStatus: 'Trạng thái đơn hàng',
          failureReason: 'Lý do thất bại',
          assigned: 'Gán shipper',
          pickedUp: 'Lấy hàng',
          delivering: 'Đang giao',
          delivered: 'Hoàn tất',
          updatedAt: 'Cập nhật cuối',
        },
      },
      usersPage: {
        title: 'Quản lý người dùng',
        description:
          'Theo dõi tài khoản, vai trò và trạng thái khóa của người dùng trong hệ thống.',
        totalUsers: '{count} người dùng',
        searchPlaceholder: 'Tìm theo username, email, số điện thoại hoặc vai trò...',
        loadError: 'Không tải được danh sách người dùng',
        fallbackNotice:
          'Endpoint danh sách người dùng đang lỗi. Tạm hiển thị tài khoản hiện tại.',
        empty: 'Chưa có người dùng nào',
        active: 'Hoạt động',
        inactive: 'Ngưng hoạt động',
        locked: 'Đã khóa',
        unlocked: 'Đang mở',
        columns: {
          username: 'Tài khoản',
          contact: 'Liên hệ',
          roles: 'Vai trò',
          status: 'Trạng thái',
          locked: 'Khóa',
          updatedAt: 'Cập nhật',
        },
      },
      userManagement: {
        addEmployee: 'Thêm nhân viên',
        avatarLabel: 'Ảnh đại diện',
        createDialogDescription:
          'Tạo tài khoản nhân viên hoặc admin trực tiếp từ khu vực quản trị.',
        createError: 'Không tạo được nhân viên',
        createSuccess: 'Đã tạo nhân viên',
        deleteDescription:
          'Hành động này sẽ xóa tài khoản khỏi hệ thống quản trị và không thể hoàn tác.',
        deleteError: 'Không xóa được tài khoản',
        deleteSuccess: 'Đã xóa tài khoản',
        deleteTitle: 'Xác nhận xóa tài khoản',
        detailsCustomer: 'Chi tiết khách hàng',
        detailsStaff: 'Chi tiết nhân viên',
        editDialogDescription:
          'Chỉnh sửa những thông tin hiện được phép thay đổi với tài khoản nhân viên.',
        editError: 'Không cập nhật được nhân viên',
        editLockedHint: 'Tài khoản quản trị viên không thể chỉnh sửa tại đây.',
        editSuccess: 'Đã cập nhật nhân viên',
        editTitle: 'Sửa nhân viên',
        lockDescription: {
          lock:
            'Tài khoản này sẽ bị khóa và không thể đăng nhập cho đến khi được mở lại.',
          unlock:
            'Tài khoản này sẽ được mở lại để có thể đăng nhập và sử dụng hệ thống.',
        },
        lockError: 'Không cập nhật được trạng thái khóa',
        lockSuccess: {
          lock: 'Đã khóa tài khoản',
          unlock: 'Đã mở khóa tài khoản',
        },
        lockTitle: {
          lock: 'Khóa tài khoản',
          unlock: 'Mở khóa tài khoản',
        },
        lockAction: {
          lock: 'Khóa',
          unlock: 'Mở khóa',
        },
        role: 'Vai trò',
        selfManageBlocked:
          'Không thể tự khóa hoặc xóa chính tài khoản đang đăng nhập.',
        showingCount: 'Hiển thị {count} trên {total} tài khoản',
      },
      customersPage: {
        title: 'Quản lý khách hàng',
        description:
          'Xem danh sách khách hàng đang hoạt động trong hệ thống.',
        totalUsers: '{count} khách hàng',
        searchPlaceholder: 'Tìm theo username hoặc vai trò...',
        loadError: 'Không tải được danh sách khách hàng',
        empty: 'Chưa có khách hàng nào',
      },
      staffPage: {
        title: 'Quản lý nhân viên',
        description:
          'Xem chung các tài khoản staff và admin trong khu vực quản trị.',
        totalUsers: '{count} nhân viên',
        searchPlaceholder: 'Tìm theo username hoặc vai trò...',
        loadError: 'Không tải được danh sách nhân viên',
        empty: 'Chưa có nhân viên nào',
      },
      rolesPage: {
        title: 'Quản lý vai trò',
        description:
          'Xem các vai trò hệ thống và danh sách quyền gắn với từng vai trò.',
        totalRoles: '{count} vai trò',
        searchPlaceholder: 'Tìm theo tên vai trò, mô tả hoặc mã quyền...',
        loadError: 'Không tải được danh sách vai trò',
        empty: 'Chưa có vai trò nào',
        permissionCount: '{count} quyền',
        noDescription: 'Chưa có mô tả',
      },
      permissionsPage: {
        title: 'Quản lý quyền',
        description:
          'Xem danh sách quyền đang được sử dụng trong hệ thống quản trị.',
        totalPermissions: '{count} quyền',
        searchPlaceholder: 'Tìm theo mã quyền hoặc mô tả...',
        loadError: 'Không tải được danh sách quyền',
        empty: 'Chưa có quyền nào',
        noDescription: 'Chưa có mô tả',
        columns: {
          code: 'Mã quyền',
          description: 'Mô tả',
          updatedAt: 'Cập nhật',
        },
      },
      promotionsPage: {
        title: 'Quản lý khuyến mãi',
        description:
          'Theo dõi các chương trình giảm giá và tình trạng sử dụng hiện tại.',
        totalPromotions: '{count} khuyến mãi',
        searchPlaceholder: 'Tìm theo tên chương trình, mã hoặc mô tả...',
        loadError: 'Không tải được danh sách khuyến mãi',
        empty: 'Chưa có khuyến mãi nào',
        noDescription: 'Chưa có mô tả',
        percentType: 'Phần trăm',
        fixedType: 'Số tiền cố định',
        noEndDate: 'Không giới hạn',
        usageWithLimit: '{used} / {limit} lượt',
        usageNoLimit: '{used} lượt dùng',
        columns: {
          campaign: 'Chương trình',
          discount: 'Giảm giá',
          usage: 'Sử dụng',
          schedule: 'Thời gian',
          status: 'Trạng thái',
        },
        statuses: {
          active: 'Đang chạy',
          upcoming: 'Sắp diễn ra',
          expired: 'Đã hết hạn',
          inactive: 'Tạm tắt',
        },
      },
      references: {
        title: 'Quản lý danh mục tham chiếu',
        description:
          'Tạo và chỉnh sửa category, author, publisher để form sản phẩm dùng lại.',
        addCategory: 'Thêm category',
        addAuthor: 'Thêm author',
        addPublisher: 'Thêm publisher',
        saveSuccess: 'Đã lưu thay đổi',
        deleteSuccess: 'Đã xóa mục được chọn',
        deleteTitle: 'Xác nhận xóa',
        deleteDescription:
          'Hành động này sẽ xóa mục khỏi dữ liệu tham chiếu và không thể hoàn tác.',
        confirmDelete: 'Xóa "{name}"?',
        emptyCategories: 'Chưa có category nào',
        emptyAuthors: 'Chưa có author nào',
        emptyPublishers: 'Chưa có publisher nào',
        biography: 'Tiểu sử',
        categoryImage: 'Ảnh danh mục',
        publisherLogo: 'Logo nhà xuất bản',
        imageUploadHint: 'Chọn ảnh JPG, PNG hoặc WebP.',
        sections: {
          categories: 'Category',
          authors: 'Author',
          publishers: 'Publisher',
        },
      },
      referencePages: {
        categories: {
          title: 'Quản lý danh mục',
          description: 'Tạo và chỉnh sửa danh mục để gán cho sách.',
          section: 'Danh mục',
          countLabel: '{count} danh mục',
          add: 'Thêm danh mục',
          editTitle: 'Chỉnh sửa danh mục',
          detailTitle: 'Chi tiết danh mục',
          searchPlaceholder: 'Tìm kiếm danh mục...',
          empty: 'Chưa có danh mục nào',
          emptyDescription: 'Danh mục này chưa có mô tả.',
          code: 'Mã thể loại',
          vietnamese: 'Nội dung tiếng Việt',
          english: 'Nội dung tiếng Anh',
          localizedName: 'Tên hiển thị',
          localizedDescription: 'Mô tả',
        },
        authors: {
          title: 'Quản lý tác giả',
          description: 'Tạo và chỉnh sửa tác giả để gán cho sách.',
          section: 'Tác giả',
          countLabel: '{count} tác giả',
          add: 'Thêm tác giả',
          editTitle: 'Chỉnh sửa tác giả',
          detailTitle: 'Chi tiết tác giả',
          searchPlaceholder: 'Tìm kiếm tác giả...',
          empty: 'Chưa có tác giả nào',
          emptyDescription: 'Tác giả này chưa có tiểu sử.',
        },
        publishers: {
          title: 'Quản lý nhà xuất bản',
          description: 'Tạo và chỉnh sửa nhà xuất bản để gán cho sách.',
          section: 'Nhà xuất bản',
          countLabel: '{count} nhà xuất bản',
          add: 'Thêm nhà xuất bản',
          editTitle: 'Chỉnh sửa nhà xuất bản',
          detailTitle: 'Chi tiết nhà xuất bản',
          searchPlaceholder: 'Tìm kiếm nhà xuất bản...',
          empty: 'Chưa có nhà xuất bản nào',
          emptyDescription: 'Nhà xuất bản này chưa có mô tả.',
        },
      },
    },
    categories: {
      all: 'Tất cả',
    },
    orderStatus: {
      pending: 'Chờ xác nhận',
      processing: 'Đang xử lý',
      shipped: 'Đang giao',
      delivered: 'Đã giao',
      cancelled: 'Đã huỷ',
      PENDING: 'Chờ xác nhận',
      CONFIRMED: 'Đã xác nhận',
      SHIPPING: 'Đang giao',
      DELIVERED: 'Đã giao',
      CANCELLED: 'Đã hủy',
    },
    shipmentStatus: {
      ASSIGNED: 'Đã gán shipper',
      PICKED_UP: 'Đã lấy hàng',
      DELIVERING: 'Đang giao',
      DELIVERED: 'Đã giao',
      FAILED: 'Giao thất bại',
    },
    paymentMethods: {
      CASH: 'Tiền mặt',
      COD: 'Thanh toán khi nhận hàng',
      BANK_TRANSFER: 'Chuyển khoản ngân hàng',
      VNPAY: 'Ví VNPAY',
      MOMO: 'Ví MoMo',
    },
    paymentStatus: {
      UNPAID: 'Chưa thanh toán',
      PAID: 'Đã thanh toán',
      FAILED: 'Thanh toán thất bại',
      REFUNDED: 'Đã hoàn tiền',
    },
    roles: {
      ADMIN: 'Quản trị viên',
      STAFF: 'Nhân viên',
      SHIPPER: 'Nhân viên giao hàng',
      USER: 'Người dùng',
    },
    genders: {
      MALE: 'Nam',
      FEMALE: 'Nữ',
      OTHER: 'Khác',
    },
  },
  en: {
    language: {
      label: 'Language',
      switcherAria: 'Choose language',
      vi: 'VI',
      en: 'EN',
    },
    common: {
      deployStartup: {
        badge: 'Connecting',
        title: 'The bookstore is getting ready',
        description:
          'The service needs a little more time to get ready. This page will continue automatically once the connection is available.',
        hint:
          'Keep this tab open while the app retries in the background and prepares the bookstore data.',
        waitedLabel: 'Wait time',
        waitedSeconds: '{seconds}s',
        retryLabel: 'Retry attempts',
        retryCount: '{count}',
        retryNow: 'Retry now',
        footer:
          'If the wait takes longer than expected, you can retry the connection now.',
        phases: {
          boot: 'Starting the service',
          warmup: 'Preparing the connection',
          catalog: 'Loading bookstore data',
        },
      },
      brand: 'SachVui',
      or: 'or',
      loading: 'Loading...',
      search: 'Search',
      subtotal: 'Subtotal',
      shipping: 'Shipping',
      total: 'Total',
      free: 'Free',
      continueShopping: 'Continue shopping',
      proceedToCheckout: 'Proceed to checkout',
      backHome: 'Back to home',
      send: 'Send',
      quantity: 'Quantity',
      actions: 'Actions',
      email: 'Email',
      phone: 'Phone',
      date: 'Date',
      category: 'Category',
      price: 'Price',
      processing: 'Processing...',
      viewAll: 'View all',
      dashboard: 'Dashboard',
      save: 'Save',
      cancel: 'Cancel',
      close: 'Close',
      view: 'View',
      edit: 'Edit',
      delete: 'Delete',
      name: 'Name',
      description: 'Description',
      createdAt: 'Created at',
      updatedAt: 'Updated at',
      pagination: {
        navigation: 'Pagination',
        previous: 'Previous page',
        next: 'Next page',
        page: 'Page {page}/{total}',
        total: '{count} records in total',
        goToPage: 'Go to page {page}',
        jumpLabel: 'Go to page',
        jumpInput: 'Enter a page number',
        jumpAction: 'Go',
      },
    },
    header: {
      searchAria: 'Search books',
      cartAria: 'Cart',
      login: 'Login',
      logout: 'Logout',
      myProfile: 'My profile',
      myLibrary: 'Digital library',
      adminDashboard: 'Admin dashboard',
      profileMenu: 'Profile menu',
      switchToLight: 'Switch to light theme',
      switchToDark: 'Switch to dark theme',
      nav: {
        home: 'Home',
        books: 'Books',
        digitalLibrary: 'Digital library',
        lifeSkills: 'Life skills',
        novel: 'Novels',
      },
    },
    footer: {
      description:
        'An online bookstore for young readers. Thousands of great titles, fast delivery, and fair prices every day.',
      socialAria: 'Social links',
      explore: 'Explore',
      support: 'Support',
      newsletterTitle: 'Newsletter',
      newsletterDescription: 'Get weekly deals and curated book picks.',
      newsletterPlaceholder: 'Your email',
      newsletterSubmitting: 'Subscribing',
      newsletterSuccess: 'You are now subscribed to the newsletter.',
      newsletterError: 'Unable to subscribe. Please try again.',
      copyright: '© 2026 SachVui. All rights reserved.',
      tagline: 'Designed for book lovers.',
      links: {
        allBooks: 'All books',
        bestsellers: 'Bestsellers',
        newBooks: 'New arrivals',
        promotions: 'Promotions',
        shippingPolicy: 'Shipping policy',
        returns: 'Returns & refunds',
        faq: 'FAQ',
        contact: 'Contact',
      },
    },
    home: {
      heroBadge: 'Up to 40% off new books every week',
      heroTitlePrefix: 'Read more,',
      heroTitleAccent: 'live brighter',
      heroDescription:
        'Discover standout titles from novels and life skills to science. Fast delivery and great prices for young readers.',
      shopNow: 'Shop now',
      lifeSkillsBooks: 'Life skills',
      stats: {
        books: 'Titles',
        sales: 'Sales',
        reviewsCount: '{count} reviews',
      },
      values: {
        fastDeliveryTitle: 'Fast delivery',
        fastDeliveryDesc: 'Receive books within 24-48 hours',
        greatPriceTitle: 'Great price daily',
        greatPriceDesc: 'Up to 40% off for members',
        authenticTitle: 'Authentic books',
        authenticDesc: '100% licensed from publishers',
      },
      categoriesTitle: 'Browse by category',
      categoriesCount: '{count} categories to explore',
      allCategories: 'View all categories',
      catalogLoadingTitle: 'Loading the home catalog',
      catalogLoadingDescription:
        'The page shell stays available while the catalog data is loading.',
      catalogBooksErrorTitle: 'Could not load books right now',
      catalogBooksErrorDescription:
        'The hero and discovery blocks still work. Book shelves will reappear when the catalog responds again.',
      catalogCategoriesErrorTitle: 'Could not load categories right now',
      catalogCategoriesErrorDescription:
        'The category list will return when the catalog connection recovers.',
      featuredTitle: 'Featured books',
      emptyTitle: 'No books are available yet',
      emptyDescription:
        'Books and recommendations will appear here when the collection is available.',
      promoTitle: 'Golden week - Buy 2 get 1',
      promoDescription:
        'Applies to all life skills and novel titles. Limited quantity, grab the deal today.',
      promoButton: 'Grab the deal',
      bestsellersTitle: 'Bestsellers',
      bookMatch: {
        badge: 'BookMatch',
        title: 'Not sure what to read today?',
        description:
          'Answer 3 quick questions about your mood, budget, and reading time to find books that fit today.',
        button: 'Find my next read',
        stepCount: '3 quick steps',
      },
      couponGame: {
        badge: 'Today’s mini game',
        title: 'Hunt today’s coupon',
        description:
          'Spin the wheel to reveal one active public coupon. The code is shown for manual copy and use in the existing cart or checkout flow.',
        button: 'Open the wheel',
        dailyLimit: '1 spin per day',
      },
    },
    recommendations: {
      title: 'Picked for you',
      subtitle: 'Recommendations based on your preferences and reading journey.',
      loading: 'Loading personalized recommendations',
      reasons: {
        PURCHASE_HISTORY: 'Similar to books you bought',
        FAVORITE_CATEGORY: 'A category you often read',
        FAVORITE_AUTHOR: 'An author you enjoy',
        WISHLIST_SIGNAL: 'From your wishlist',
        BOOKSHELF_SIGNAL: 'From your bookshelf',
        HIGH_RATING_REVIEW: 'Based on your high ratings',
        READING_JOURNAL_SIGNAL: 'Based on your reading journal',
        POPULAR_PICK: 'Popular with readers',
        HIGH_RATING: 'Highly rated',
        NEW_RELEASE: 'New release',
        FALLBACK_POPULAR: 'Popular pick',
      },
    },
    couponGamePage: {
      badge: 'A light daily treat',
      title: 'Lucky wheel coupon hunt',
      description:
        'You get one spin per day to reveal a valid discount code. Copy the code and use it on an eligible order.',
      limitChip: 'Limit',
      dailyLimit: '1 time / day',
      poolChip: 'Available codes',
      poolCount: '{count} codes ready',
      manualChip: 'Usage',
      manualOnly: 'Copy and apply manually',
      wheelBadge: 'Lucky Spin',
      spinHint:
        'Tap spin to receive a random code from the available offers.',
      spinButton: 'Spin now',
      spinLoading: 'Spinning...',
      playedButton: 'Already played today',
      alreadyPlayedTitle: 'You already played today',
      alreadyPlayedDescription:
        'Today’s result is kept on the right. Come back tomorrow for another coupon hunt.',
      resultTitle: 'Coupon reveal',
      resultPlaceholder: 'Gift not opened yet',
      resultWaiting:
        'After the spin, the coupon code, discount summary, expiry, and copy button will appear here.',
      todayResultBadge: 'Today’s code',
      resultSaved: 'Saved until',
      copyButton: 'Copy code',
      copySuccess: 'Coupon code copied.',
      copyError:
        'Automatic copy failed. Please copy the coupon code manually from above.',
      openCart: 'Go to cart',
      backHome: 'Back to home',
      viewBooks: 'Browse books',
      manualApplyHint:
        'This code is not applied automatically. Paste it into the discount field in your cart or at checkout.',
      typeBook: 'For books',
      typeShipping: 'For shipping',
      minOrderLabel: 'Minimum order',
      maxDiscountLabel: 'Max discount',
      expiresLabel: 'Expires on',
      noExpiry: 'No time limit',
      noDescription:
        'This coupon does not have an extra description, but you can still copy the code and try it in the existing flow.',
      emptyTitle: 'No active coupon is available to spin today',
      emptyDescription:
        'The wheel will reopen when another discount code becomes available. Please check back later.',
      errorTitle: 'The wheel is unavailable right now',
      errorDescription:
        'The app could not load the active coupon list at the moment. Please try again later.',
      discountPercent: '{value}% off',
      discountFixed: '{amount} off',
      wheelCenter: 'LUCKY',
    },
    auth: {
      login: {
        success: 'Login successful!',
        errorFallback: 'Login failed',
        title: 'Welcome back',
        description: 'Sign in with your account to keep shopping',
        cardTitle: 'Login',
        username: 'Username',
        password: 'Password',
        forgotPassword: 'Forgot password?',
        passwordPlaceholder: 'Enter your password',
        showPassword: 'Show password',
        hidePassword: 'Hide password',
        signupDescription:
          'The back side is a quick registration preview. Use the dedicated register page to create a full account.',
        openRegisterPage: 'Open register page',
        submit: 'Login',
        noAccount: "Don't have an account?",
        registerNow: 'Register now',
        restrictions: {
          locked: {
            title: 'Account locked',
            description:
              'This account cannot sign in right now. Contact an administrator to unlock it.',
          },
          inactive: {
            title: 'Account not activated',
            description:
              'This account must be verified with an OTP before it can sign in. Complete the activation step and the app will sign you in again.',
          },
        },
        flow: {
          lockedActionLabel: 'Use another account',
          inactiveActionLabel: 'Resend OTP',
          inactiveOtpLead:
            'Enter the latest activation OTP from your email. If you did not receive one or it has expired, request a new code here.',
          inactiveOtpReadyHint:
            'Enter the correct 6-digit OTP and the app will sign you in again with the same account.',
          inactiveBackLabel: 'Back to login',
          inactiveVerifyLabel: 'Verify and sign in',
          inactiveEmailRequiredMessage:
            'Inactive accounts must sign in with an email address before OTP verification.',
          inactiveRequestErrorFallback: 'Unable to send a new activation OTP',
        },
      },
      forgotPassword: {
        title: 'Forgot password',
        description:
          'Enter your email, verify the security code, then set a new password',
        requestTitle: 'Request OTP',
        requestDescription:
          'Enter your account email to receive a password reset code',
        requestHint:
          'If the email is valid, a verification code will be sent to your inbox.',
        requestSubmit: 'Send OTP',
        requestSuccess: 'If the email exists, an OTP has been sent',
        requestErrorFallback: 'Unable to send password reset OTP',
        verifyTitle: 'Verify OTP',
        verifyDescription:
          'Enter the 6-digit OTP sent to {email}',
        otpSent: 'An OTP has been sent to your email',
        verifyHint:
          'Once the code is verified, you can create a new password.',
        verifySubmit: 'Verify OTP',
        verifySuccess: 'OTP verified, set your new password',
        verifyErrorFallback: 'Password reset OTP verification failed',
        otpCode: 'OTP code',
        otpPlaceholder: '6 digits',
        otpInvalid: 'OTP must contain exactly 6 digits!',
        backStep: 'Back',
        resetTitle: 'Set new password',
        resetDescription:
          'Create a new password for {email}',
        resetHint:
          'Your new password must be at least 8 characters. Previous sessions will end after the password is changed.',
        newPassword: 'New password',
        newPasswordPlaceholder: 'At least 8 characters',
        confirmPassword: 'Confirm new password',
        resetSubmit: 'Reset password',
        resetSuccess: 'Password reset successful!',
        resetErrorFallback: 'Password reset failed',
        startOver: 'Start over',
        backToLogin: 'Back to login',
      },
      register: {
        success: 'Registration successful!',
        errorFallback: 'Registration failed',
        verifyErrorFallback: 'OTP verification failed',
        passwordMismatch: 'Passwords do not match!',
        passwordTooShort: 'Password must be at least 8 characters!',
        title: 'Create account',
        description:
          'Enter your details and verify your email to finish creating your account',
        otpSent: 'An OTP has been sent to your email',
        verifyTitle: 'Verify OTP',
        verifyDescription:
          'Enter the 6-digit OTP sent to {email} to activate your account',
        verifyHint:
          'The verification code is sent after registration. You can sign in once the email is verified.',
        otpCode: 'OTP code',
        otpPlaceholder: '6 digits',
        otpHint: 'Check your inbox and enter the exact 6-digit code.',
        otpInvalid: 'OTP must contain exactly 6 digits!',
        backToRegister: 'Back to register',
        verifySubmit: 'Verify OTP',
        phoneNumber: 'Phone number',
        firstName: 'First name',
        lastName: 'Last name',
        gender: 'Gender',
        dateOfBirth: 'Date of birth',
        avatarUrl: 'Avatar URL (optional)',
        password: 'Password',
        confirmPassword: 'Confirm password',
        passwordPlaceholder: 'At least 8 characters',
        passwordWeak: 'Weak password',
        passwordMedium: 'Fair password',
        passwordStrong: 'Strong password',
        passwordVeryStrong: 'Very strong password',
        passwordMatched: 'Passwords match',
        passwordNotMatched: 'Passwords do not match',
        submit: 'Create account',
        haveAccount: 'Already have an account?',
        loginNow: 'Login now',
        verification: {
          resendOtpLabel: 'Resend OTP',
          requestOtpErrorFallback: 'Unable to send a new activation OTP',
        },
        terms: {
          badge: 'SachVui Terms',
          agreementLabel: 'I agree to the',
          linkLabel: 'terms of use',
          requiredMessage: 'You must agree to the terms before registering.',
          dialogTitle: 'Terms of Use',
          closeHint:
            'Scroll to the bottom to unlock the close button in the top-right corner.',
          closeReady:
            'You have reached the end. The X button can now be used to close this dialog.',
          intro:
            'This document explains the terms for using a SachVui account. By continuing to register, you confirm that you have read, understood, and accepted the terms below.',
          sections: [
            {
              title: '1. Account scope and access',
              paragraphs: [
                'An account is provided so you can buy books, track orders, manage reviews, and receive system notifications. You are responsible for using a working email address, safeguarding your credentials, and avoiding shared access with other people.',
                'If the platform detects impersonation, scraping, disruption attempts, or behavior that may mislead other customers, SachVui may temporarily restrict or suspend the account while an internal review is performed.',
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
                'By publishing content on the platform, you allow SachVui to display that content in product pages, review summaries, and internal moderation views for service quality and operational improvement purposes.',
              ],
            },
            {
              title: '4. Transaction rules',
              paragraphs: [
                'Pricing, promotions, stock levels, and delivery windows may change during normal operations. Adding a book to your cart does not automatically reserve inventory; an order is only confirmed once the checkout flow completes successfully.',
                'If stock conflicts, shipping policy changes, display-price issues, or delivery-area limitations occur, SachVui may contact you with a reasonable alternative such as replacement, refund, or order adjustment.',
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
                'SachVui may update the interface, features, terms, or account workflows when operational needs change. When a change materially affects the user experience, a visible in-product notice or another appropriate communication method may be used.',
                'You may stop using the service at any time. However, data tied to completed transactions, active support requests, or operational retention obligations may remain stored for as long as reasonably necessary.',
              ],
            },
            {
              title: '7. Final acknowledgement',
              paragraphs: [
                'Checking the agreement box means you accept these terms. If you disagree with any part, you should stop creating the account.',
                'Thank you for reading. Your attention helps keep your rights and responsibilities on SachVui clear.',
              ],
            },
          ],
        },
      },
      profile: {
        logout: 'Logout',
        accountTitle: 'Account information',
        personalTitle: 'Personal details',
        saveAccount: 'Save account',
        saveProfile: 'Save profile',
        accountUpdated: 'Account information updated',
        profileUpdated: 'Profile updated',
        username: 'Username',
        firstName: 'First name',
        lastName: 'Last name',
        avatarUrl: 'Avatar URL',
        gender: 'Gender',
        dateOfBirth: 'Date of birth',
        ordersTitle: 'Your orders',
        emptyOrders: 'You do not have any orders yet',
        orderTotal: 'Total',
        loginPanelTitle: 'Login information',
        addressMenuTitle: 'My addresses',
        passwordMenuTitle: 'Change password',
        chooseImage: 'Choose image',
        imageHint: 'JPG, PNG',
        noOrdersDescription: 'Explore the catalog and place your first order.',
        shopNow: 'Shop now',
        addressTitle: 'Your address book',
        addressDescription: 'Your saved delivery addresses appear here.',
        noAddressesTitle: 'You do not have any addresses yet',
        noAddressesDescription:
          'Add an address during checkout to use it faster next time.',
        passwordTitle: 'Account security',
        passwordDescription:
          'Reset your password using the current verification flow.',
        passwordAction: 'Go to password reset',
        defaultAddress: 'Default',
        retry: 'Retry',
        goCheckout: 'Go to checkout',
        avatarLabel: 'Avatar image',
      },
    },
    book: {
      fallback: {
        author: 'Unknown author',
        category: 'Uncategorized',
        publisher: 'Unknown publisher',
      },
      card: {
        coverAlt: 'Book cover {title}',
        bestseller: 'Bestseller',
        addedToCart: 'Added "{title}" to cart',
        addToCartAria: 'Add {title} to cart',
      },
      addToCart: {
        quantity: 'Quantity',
        decrease: 'Decrease quantity',
        increase: 'Increase quantity',
        addedQtyToCart: 'Added {count} item(s) to cart',
        addToCart: 'Add to cart',
        buyNow: 'Buy now',
      },
      listing: {
        title: 'All books',
        resultCount: 'Found {count} matching books',
        searchPlaceholder: 'Search books, authors...',
        categoryTitle: 'Category',
        categoryCount: '{count} items',
        selectedCategoryLabel: 'Filtering by',
        clearCategory: 'Clear',
        categorySearchPlaceholder: 'Search categories...',
        categoryShowingCount: 'Showing {count}/{total} categories',
        categoryEmptyTitle: 'No matching categories',
        categoryEmptyDescription: 'Try another keyword or clear the filter to browse all categories.',
        showMoreCategories: 'Show {count} more',
        showFewerCategories: 'Show less',
        sortPlaceholder: 'Sort by',
        sortPopular: 'Most popular',
        sortRating: 'Highest rated',
        sortPriceAsc: 'Price: low to high',
        sortPriceDesc: 'Price: high to low',
        errorTitle: 'Unable to load books',
        errorDescription:
          'Book information is unavailable right now. Please try again later.',
        emptyTitle: 'No books found',
        emptyDescription: 'Try changing your keywords or filters.',
      },
      detail: {
        breadcrumbHome: 'Home',
        breadcrumbBooks: 'Books',
        author: 'Author',
        reviewsCount: '({count} reviews)',
        saveAmount: 'Save {amount}',
        availabilityLabel: 'Availability',
        deliveryTitle: 'Fast delivery',
        deliveryTime: 'Arrives in 24-48 hours',
        freeShippingTitle: 'Free shipping',
        freeShippingThreshold: 'Orders from {amount}',
        wishlistShort: 'Wishlist',
        wishlistedShort: 'Saved',
        shelfShort: 'Bookshelf',
        journalShort: 'Journal',
        descriptionTitle: 'About this book',
        descriptionFallback: 'Book description is being updated.',
        detailsTitle: 'Detailed description',
        detailsFallback: 'Updating.',
        authorInfoTitle: 'Author information',
        authorBioFallback: 'Author information is being updated.',
        promotionsTitle: 'Offers for you',
        promotionsEmpty: 'Promotions are being updated.',
        promotionCodeLabel: 'Use code',
        promotionMinOrder: 'Valid for orders from {amount}.',
        promotionNoMinOrder: 'Valid for all orders.',
        promotionMaxDiscount: 'Max discount {amount}.',
        commitmentsTitle: '{brand} commitments',
        commitmentAuthentic: '100% authentic books',
        commitmentReturn: 'Flexible returns within 7 days',
        commitmentShipping: 'Fast delivery in 24-48h',
        commitmentSupport: 'Customer support every day',
        reviewTitle: 'Customer reviews',
        reviewEmpty: 'No reviews for this book yet.',
        reviewHelpful: 'Helpful ({count})',
        reviewVerifiedPurchase: 'Verified purchase',
        stockOut: 'Out of stock',
        suggestionsTitle: 'You may also like',
        soldCountValue: 'Sold {count}',
        specStock: 'Stock',
        specPublisher: 'Publisher',
        specIsbn: 'ISBN',
        specUpdatedAt: 'Updated at',
        specPageCount: 'Page count',
        specPublicationYear: 'Publication year',
        specLanguage: 'Language',
        specCoverType: 'Cover type',
        specDimensions: 'Dimensions',
        specWeight: 'Weight',
        specTranslator: 'Translator',
        specEdition: 'Edition',
        stockValue: '{count} copies',
        pageCountValue: '{count} pages',
        weightValue: '{count} g',
        digitalAssets: {
          title: 'Digital editions',
          description:
            'These are the published digital editions currently exposed publicly for this book.',
          addToCart: 'Add ebook to cart',
          addToCartError: 'Could not add the digital edition to the cart.',
          addedToCart: 'Digital edition added to cart.',
          addingToCart: 'Adding...',
          downloadAllowed: 'Download allowed',
          downloadRestricted: 'Download restricted',
          noSample: 'No sample',
          openSample: 'Open sample',
          purchaseAvailable: 'Can purchase',
          purchaseDisabled: 'Currently unavailable',
          purchaseUnavailable: 'Cannot purchase',
          sampleAvailable: 'Sample available',
        },
      },
      match: {
        heroBadge: 'BookMatch',
        title: '“What should I read today?” quiz',
        description:
          'Choose your mood, budget, and reading time to receive a list of books that fit today.',
        progressTitle: 'Quiz progress',
        progressReady: 'Ready to recommend',
        progressStep: 'Step {current}/3',
        sidebarTitle: 'Today’s recommendation run',
        sidebarDescription:
          'Complete all 3 choices to receive around {count} suitable books.',
        submit: 'Recommend books for me',
        submitLoading: 'Matching your mood...',
        reset: 'Start over',
        resultsTitle: 'BookMatch results',
        resultsDescription:
          'Suggestions are ordered by mood, price range, reading length, reader reviews, and popularity.',
        resultsCount: '{count} books fit today’s vibe',
        errorTitle: 'Unable to suggest books',
        errorRetry: 'Try again',
        catalogEmptyTitle: 'The shelf is taking a break',
        catalogEmptyDescription:
          'There are no books available for recommendations right now. Please check back later.',
        emptyTitle: 'Today’s shelf is being picky',
        emptyDescription:
          'This combination does not have enough good matches yet. Try another mood or loosen the budget a bit.',
        summary: {
          mood: 'Mood',
          budget: 'Budget',
          readingTime: 'Reading time',
          pending: 'Not selected',
        },
        steps: {
          moodLabel: 'Mood',
          budgetLabel: 'Budget',
          readingTimeLabel: 'Reading time',
          moodTitle: 'Which mood are you reading in?',
          moodDescription:
            'This helps BookMatch prefer the most relevant categories and keywords.',
          budgetTitle: 'How much do you want to spend today?',
          budgetDescription:
            'Prices are prioritized to stay inside the budget band you picked.',
          readingTimeTitle: 'How long do you want the read to feel?',
          readingTimeDescription:
            'BookMatch uses the current page-count data to prefer short, medium, or long reads.',
        },
        moods: {
          RELAX: {
            label: 'RELAX · Unwind',
            description:
              'Literary, slice-of-life, and shorter comforting reads for a chill session.',
          },
          STUDY: {
            label: 'STUDY · Learn something',
            description:
              'Skills, language, business, and books that help you level up.',
          },
          ADVENTURE: {
            label: 'ADVENTURE · Explore',
            description:
              'Fantasy, journeys, history, and books that feel like going far away.',
          },
          MYSTERY: {
            label: 'MYSTERY · Stay curious',
            description:
              'Detective, horror, secretive stories, and page-turning twists.',
          },
          HEALING: {
            label: 'HEALING · Feel softer',
            description:
              'Self-help, psychology, healing, and gentler emotional energy.',
          },
        },
        budgets: {
          UNDER_100: {
            label: 'Under 100,000 VND',
            description:
              'Soft-price picks that are easy to grab without overthinking.',
          },
          FROM_100_TO_200: {
            label: '100,000 VND to 200,000 VND',
            description:
              'The most balanced budget range for many popular titles.',
          },
          ABOVE_200: {
            label: 'Above 200,000 VND',
            description:
              'Broader picks for thicker books, nicer editions, or premium titles.',
          },
        },
        readingTimes: {
          SHORT: {
            label: 'SHORT · Quick read',
            description:
              'Prioritizes books with around 220 pages or fewer.',
          },
          MEDIUM: {
            label: 'MEDIUM · Balanced read',
            description:
              'Prioritizes books around 221-380 pages for a satisfying middle ground.',
          },
          LONG: {
            label: 'LONG · Deep dive',
            description:
              'Prioritizes books with 381 pages or more for a longer reading trip.',
          },
        },
        weakDataHint:
          'Some books do not have page-count data yet, so the ranking may lean more on ratings and popularity.',
        reasons: {
          MOOD: 'Matches your mood',
          BUDGET: 'Fits the budget',
          READING_TIME: 'Fits the reading length',
          HIGH_RATING: 'Strong rating',
          POPULAR_PICK: 'Popular choice',
          FRESH_PICK: 'Recently added',
        },
      },
    },
    cart: {
      title: 'Your cart',
      itemCount: '{count} books in cart',
      emptyTitle: 'Your cart is empty',
      summaryTitle: 'Order summary',
      selectAll: 'Select all ({selected}/{total})',
      selectAllAria: 'Select all cart books',
      selectItemAria: 'Select {title} for checkout',
      removeSelected: 'Remove selected items',
      orderTitle: 'Your order',
      selectedCount: 'Selected {selected}/{total} books',
      selectedSubtotal: 'Subtotal ({count} books)',
      totalPayment: 'Total payment',
      checkoutSelected: 'Checkout selected books',
      secureCheckout: 'Secure checkout',
      inStock: 'In stock',
      qualityTitle: '100% authentic books',
      qualityDescription: 'Quality guaranteed',
      returnTitle: 'Easy returns',
      returnDescription: 'Within 7 days',
      deliveryTitle: 'Fast delivery',
      deliveryDescription: 'Nationwide',
      loginRequired: 'Please sign in to use the cart',
      fetchError: 'Unable to load cart',
      updateError: 'Unable to update cart',
      itemTypes: {
        digitalAsset: {
          badge: 'Digital asset',
          quantity: '1 access right',
        },
        physicalBook: {
          badge: 'Physical book',
          quantity: 'Physical quantity',
        },
      },
    },
    checkout: {
      title: 'Checkout',
      emptyTitle: 'Cart is empty',
      emptyDescription: 'Add some books before proceeding to checkout',
      shippingInfoTitle: 'Shipping information',
      fullName: 'Full name',
      address: 'Address',
      city: 'City/Province',
      district: 'District',
      ward: 'Ward',
      newAddressTitle: 'Use a new address',
      newAddressDescription: 'Enter a new shipping address for this order',
      couponCode: 'Coupon code',
      couponPlaceholder: 'Enter a code if you have one',
      paymentMethodTitle: 'Payment method',
      paymentMethodNotice: 'Choose the payment method that works best for your order.',
      missingInfo: 'Please complete all required fields',
      success: 'Order placed successfully!',
      error: 'Something went wrong. Please try again.',
      submit: 'Place order',
      orderSummary: 'Order summary',
      quantityShort: 'Qty: {count}',
      shippingAddressTitle: 'Shipping address',
      changeAddress: 'Change',
      addAddress: 'Add address',
      addNewAddress: 'Add new address',
      chooseSavedAddress: 'Choose saved address',
      chooseAddressTitle: 'Choose shipping address',
      chooseAddressDescription: 'Only the selected address will be used for this order.',
      useThisAddress: 'Use this address',
      cancel: 'Cancel',
      close: 'Close',
      defaultAddress: 'Default',
      noAddressTitle: 'No shipping address yet',
      noAddressDescription: 'Add a shipping address before placing this order.',
      newAddressHeading: 'New shipping address',
      shippingMethodTitle: 'Shipping method',
      homeDelivery: 'Home delivery',
      deliveryDescription: 'Estimated delivery in 2-4 business days',
      storePickup: 'Store pickup',
      pickupDescription: 'Currently unavailable',
      noteTitle: 'Note for the shop',
      noteLabel: 'Message',
      notePlaceholder: 'Optional note about delivery time or order instructions',
      bankTransferQr: 'Bank transfer via SePay',
      bankTransferQrDescription: 'Place the order first, then transfer with the exact content from the waiting page',
      cashOnDelivery: 'Cash on delivery',
      cashOnDeliveryDescription: 'Pay in cash when you receive your order',
      chooseCoupon: 'Choose code',
      selectedCouponPrefix: 'Selected code:',
      productTotal: 'Product total',
      shippingFeeTotal: 'Shipping fee',
      shippingDiscount: 'Shipping discount',
      couponDiscount: 'Coupon discount',
      chooseCouponTitle: 'Choose coupon code',
      couponInputPlaceholder: 'Enter coupon code',
      applyCoupon: 'Apply code',
      useCoupon: 'Use code',
      shippingCoupons: 'Shipping codes',
      bookCoupons: 'Book discount codes',
      couponLoading: 'Loading coupon codes...',
      noCoupons: 'No coupon codes available',
      couponMinOrder: 'Min order {amount}',
      couponMaxDiscount: 'Max discount {amount}',
      couponUsage: 'Used {used}/{limit}',
      couponUsageNoLimit: 'Used {used}',
      couponUnavailable: 'Order has not reached the minimum value',
    },
    orderConfirmation: {
      title: 'Order placed successfully!',
      description:
        'Thank you for your purchase. Your order has been confirmed and will be delivered soon.',
      emailNotice:
        'You will receive a confirmation email with order details in a few minutes.',
      orderId: 'Order ID',
      receiver: 'Receiver',
      paymentMethod: 'Payment method',
      paymentStatus: 'Payment status',
      total: 'Total paid',
    },
    orderConfirmationBankTransfer: {
      waitingTitle: 'Waiting for your bank transfer',
      waitingDescription:
        'Your order has been created. Complete the transfer with the exact content below so the payment can be confirmed automatically.',
      paidTitle: 'Payment confirmed successfully',
      paidDescription:
        'Payment for this order has been confirmed.',
      failedTitle: 'Payment failed',
      failedDescription:
        'The payment was unsuccessful. Please review the transfer details and try again if needed.',
      cancelledTitle: 'Payment cancelled',
      cancelledDescription:
        'The payment was cancelled. Start a new order if you still want to purchase these books.',
      pollingNotice: 'Checking payment status every 4 seconds.',
      transferInstructionTitle: 'Transfer instructions',
      transferInstructionDescription:
        'Use the bank account below and keep the transfer content exactly the same.',
      bankNameLabel: 'Bank',
      accountNumberLabel: 'Account number',
      accountNameLabel: 'Account name',
      transferContentLabel: 'Transfer content',
      transferContentHint:
        'Copy this content exactly so SePay can reconcile the payment.',
      copyButton: 'Copy content',
      copySuccess: 'Transfer content copied.',
      copyError: 'Unable to copy transfer content.',
      qrTitle: 'QR payment area',
      qrDescription:
        'The QR code is generated from the order amount and transfer content whenever the bank config is available.',
      qrUnavailableTitle: 'Dynamic QR is unavailable',
      qrUnavailableDescription:
        'The bank config is incomplete, so please transfer manually with the exact account, amount, and transfer content shown above.',
      qrImageErrorTitle: 'Unable to load the QR image',
      qrImageErrorDescription:
        'The QR service could not be loaded right now. Please transfer manually with the exact details shown above.',
      qrFallbackNoticeTitle: 'Using fallback QR configuration',
      qrFallbackNoticeDescription:
        'A legacy QR URL is being used because dynamic VietQR config is incomplete.',
      manualTransferTitle: 'Manual transfer instructions',
      manualTransferDescription:
        'Use the bank, account number, account name, amount, and transfer content exactly as shown on this page.',
      orderSummaryTitle: 'Order summary',
      summaryDescription:
        'The information below reflects the latest payment status for this order.',
      orderIdLabel: 'Order ID',
      orderCodeLabel: 'Order code',
      totalAmountLabel: 'Total amount',
      paymentMethodLabel: 'Payment method',
      paymentStatusLabel: 'Payment status',
      receiverInfoTitle: 'Receiver information',
      receiverNameLabel: 'Receiver',
      receiverPhoneLabel: 'Phone',
      receiverAddressLabel: 'Address',
      viewOrdersButton: 'View my orders',
      continueShoppingButton: 'Continue shopping',
      loadingNotice: 'Loading the latest payment status...',
      emptyValue: 'N/A',
      bankFallback: 'Update VITE_BANK_TRANSFER_BANK_NAME',
      accountNumberFallback: 'Update VITE_BANK_TRANSFER_ACCOUNT_NUMBER',
      accountNameFallback: 'Update VITE_BANK_TRANSFER_ACCOUNT_NAME',
    },
    orderConfirmationCashOnDelivery: {
      waitingTitle: 'Order created with cash on delivery',
      waitingDescription:
        'Your order has been created successfully. Pay the carrier when the parcel arrives, so no SePay transfer flow is generated for this order.',
      instructionTitle: 'Cash on delivery instructions',
      instructionDescription:
        'Keep your phone available because the store or carrier may contact you before delivery.',
      paymentLabel: 'Payment timing',
      paymentValue: 'Pay when you receive the parcel.',
      deliveryFeeLabel: 'Delivery fee policy',
      deliveryFeeValue:
        'Home delivery costs 30,000 VND and becomes free from 200,000 VND. Store pickup remains free.',
      nextStepLabel: 'Next update',
      nextStepValue:
        'Track confirmation and shipping updates from your order history.',
      noteTitle: 'Before the parcel arrives',
      noteDescription:
        'Prepare the payment amount if possible and inspect the package before completing payment with the carrier.',
      summaryDescription: 'This order will be paid in cash when it is delivered.',
    },
    library: {
      page: {
        acquiredAtLabel: 'Acquired at',
        allFormats: 'All formats',
        allStatuses: 'All statuses',
        bookLabel: 'Book',
        countLabel: 'Matching titles',
        description:
          'Find the ebooks and audiobooks from your completed orders, then continue reading or listening here.',
        downloadAllowedLabel: 'Available to download',
        downloadReadyBadge: 'Download from the detail page',
        emptyDescription:
          'No ebook or audiobook matches the current filters.',
        emptyTitle: 'No matching content found',
        expiresAtLabel: 'Expires at',
        formatFilterLabel: 'Filter by format',
        loadMore: 'Load more',
        loading: 'Loading your digital library...',
        loadingMore: 'Loading more...',
        noExpiry: 'No expiry',
        noProgress: 'No progress yet',
        openSampleLabel: 'Open sample',
        openingSampleLabel: 'Opening sample...',
        priceLabel: 'Price',
        progressLabel: 'Reading progress',
        readNowLabel: 'Read now',
        sampleAvailableLabel: 'Sample available',
        sampleError: 'Could not open the sample file.',
        searchPlaceholder: 'Search by book title or format...',
        statusFilterLabel: 'Filter by reading status',
        title: 'My digital library',
        totalOwnedLabel: '{count} titles in your library',
        viewDetailLabel: 'View details',
      },
      detail: {
        accessDescription:
          'Open the private reader or request a temporary download URL only when access has already been granted.',
        accessTitle: 'Asset access',
        acquiredAtLabel: 'Acquired at',
        assetUpdatedAtLabel: 'Asset updated at',
        backLabel: 'Back to digital library',
        bookDescriptionLabel: 'Book description',
        bookLabel: 'Book',
        currentPageLabel: 'Current page',
        downloadAllowed: 'Download enabled',
        downloadLabel: 'Download asset',
        expiresAtLabel: 'Expires at',
        fileNameLabel: 'File name',
        fileSizeLabel: 'File size',
        loading: 'Loading digital asset details...',
        mimeTypeLabel: 'MIME type',
        noDescription: 'This book description is currently unavailable.',
        noExpiry: 'No expiry',
        noProgress: 'No progress yet',
        noSourceOrder: 'No source order',
        openReaderLabel: 'Open reader',
        openSampleLabel: 'Open sample',
        openingDownloadLabel: 'Resolving download...',
        openingSampleLabel: 'Opening sample...',
        positionDataLabel: 'Position data',
        positionDataPlaceholder:
          'Optional JSON or marker string returned by your reader state',
        priceLabel: 'Price',
        progressDescription:
          'You can still sync reading progress manually from this screen if the reader does not report it automatically.',
        progressPercentLabel: 'Progress percent',
        progressTitle: 'Reading progress',
        readOnlyBadge: 'Read-only access',
        sampleAvailable: 'Sample available',
        saveProgressLabel: 'Save progress',
        savingLabel: 'Saving...',
        sourceOrderIdLabel: 'Source order ID',
      },
      reader: {
        acquiredAtLabel: 'Acquired at',
        audioDescription: 'Use the native player for audiobook playback.',
        audioFallback: 'Your browser does not support audio playback.',
        audioTitle: 'Audio player',
        backLabel: 'Back to asset details',
        bookLabel: 'Book',
        expiresAtLabel: 'Expires at',
        fileNameLabel: 'File name',
        loading: 'Loading reader...',
        noExpiry: 'No expiry',
        notFound: 'The digital asset could not be opened.',
        openNewTab: 'Open in new tab',
        updatedAtLabel: 'Asset updated at',
      },
      progress: {
        validation: {
          progressPercentRange:
            'Progress percent must be between 0 and 100.',
          currentPageNonNegativeInteger:
            'Current page must be a non-negative integer.',
        },
        updateSuccess: 'Reading progress updated.',
      },
    },
    orderDetail: {
      orderHistory: 'Order history',
      orderSummary: 'Order summary',
      productTotal: 'Product total',
      itemsTitle: 'Items in this order',
      digitalItemLabel: 'Digital asset',
      physicalItemLabel: 'Physical book',
      openLibraryAsset: 'Open in library',
    },
    orderHistoryPage: {
      completedStep: 'Completed',
      copyError: 'Unable to copy the order ID.',
      copyOrderId: 'Copy order ID',
      copySuccess: 'Order ID copied.',
      discoverDescription: 'Thousands of curated titles are waiting for you.',
      discoverTitle: 'Discover more great books',
      exploreNow: 'Explore now',
      pendingStep: 'Pending confirmation',
      processingStep: 'Processing',
      shippingStep: 'Out for delivery',
    },
    notifications: {
      emptyContent: 'No content',
      realtimeConnected: 'Realtime connected',
      realtimeFallback: 'Using REST fallback',
      newNotificationFallback: 'New notification',
      errors: {
        fetch: 'Unable to load notifications',
        update: 'Unable to update the notification',
        delete: 'Unable to delete the notification',
      },
      bell: {
        title: 'Notifications',
        empty: 'No notifications yet',
        viewAll: 'View all',
        delete: 'Delete',
        loading: 'Loading notifications...',
        openLabel: 'Open notifications',
        deleteSuccess: 'Notification deleted',
      },
      page: {
        title: 'Your notifications',
        description:
          'Track new alerts, order updates, and shipment changes in real time.',
        all: 'All',
        unread: 'Unread',
        empty: 'No notifications match this filter',
        markAll: 'Mark all as read',
        markRead: 'Mark as read',
        loading: 'Loading notifications...',
        loadMore: 'Load more',
        delete: 'Delete',
        open: 'Open notification',
        deleteSuccess: 'Notification deleted',
      },
    },
    chat: {
      errors: {
        loadConversations: 'Unable to load support conversations',
        loadMessages: 'Unable to load chat history',
      },
      customer: {
        title: 'Customer support',
        chooseModeTitle: 'How would you like to get support?',
        chooseModeDescription:
          'You can switch channels at any time during the conversation.',
        modeSwitcherLabel: 'Support channel',
        aiMode: 'Chat with AI assistant',
        aiModeDescription:
          'Get an immediate answer about books and using the website.',
        humanMode: 'Chat with an agent',
        humanModeDescription:
          'Talk directly with the support team over a realtime connection.',
        humanAgent: 'SachVui support agent',
        aiAssistant: 'SachVui AI assistant',
        aiReplying: 'The AI assistant is preparing a reply...',
        aiHandoff:
          'The AI assistant is temporarily unavailable. Your message has been handed to the support team.',
        aiLimitReached:
          'You have used today’s AI allowance. The support team will continue in this conversation.',
        defaultSubject: 'Customer support',
        newConversation: 'New conversation',
        subject: 'Subject',
        subjectPlaceholder: 'Example: Question about order #1234',
        sendPlaceholder: 'Type your support message...',
        aiSendPlaceholder: 'Ask the AI assistant...',
        humanSendPlaceholder: 'Type a message for the support team...',
        send: 'Send message',
        loadingMessages: 'Loading messages...',
        loadOlderMessages: 'Load older messages',
        emptyMessages:
          'No messages yet. Start with the issue you need help with.',
        emptyConversations: 'You do not have a support conversation yet.',
        closedNotice:
          'This conversation is closed. Start a new one if you still need help.',
        closeConversation: 'Close conversation',
        realtimeConnected: 'Realtime connected',
        realtimeFallback: 'Realtime reconnecting',
        refresh: 'Refresh',
        openChat: 'Open support chat',
        closeChat: 'Close support chat',
        incomingTitle: 'New support reply',
        viewAllNotifications: 'View notifications',
      },
    },

    adminChat: {
      title: 'Customer support chat',
      description:
        'Handle support conversations in realtime, assign staff, and close or reopen threads from one workspace.',
      totalConversations: 'Total conversations',
      unreadCount: 'Unread',
      openCount: 'Open',
      connected: 'Realtime connected',
      fallback: 'Using REST fallback',
      listTitle: 'Conversation list',
      searchPlaceholder: 'Search by customer, email, or message...',
      statusLabel: 'Status',
      statusAll: 'All',
      statusOpen: 'Open',
      statusPending: 'Pending',
      statusClosed: 'Closed',
      emptyConversations: 'No conversations match the current filters.',
      loadMore: 'Load more',
      loadingList: 'Loading conversations...',
      loadingMessages: 'Loading messages...',
      messageEmpty: 'No messages in this conversation yet.',
      replyPlaceholder: 'Write a reply to the customer...',
      send: 'Send reply',
      closeConversation: 'Close conversation',
      reopenConversation: 'Reopen conversation',
      customer: 'Customer',
      assignee: 'Assignee',
      unassigned: 'Unassigned',
      assignToSelf: 'Assign to me',
      assignButton: 'Assign',
      staffPlaceholder: 'Choose staff',
      priority: 'Priority',
      target: 'Target',
      createdAt: 'Created at',
      updatedAt: 'Updated at',
      noConversationSelected: 'Select a conversation to start handling it.',
      closedNotice: 'This conversation is closed. Reopen it if support should continue.',
      noMessagesYet: 'The customer has not sent any messages yet.',
      loadError: 'Unable to load support conversations',
    },
orders: {
      title: 'Order history',
      totalCount: 'Total {count} orders',
      emptyTitle: 'You do not have any orders yet',
      emptyDescription: 'Your placed orders will appear here.',
      detailTitle: 'Order details',
      orderId: 'Order ID',
      createdAt: 'Created at',
      receiverName: 'Receiver name',
      receiverPhone: 'Receiver phone',
      receiverAddress: 'Receiver address',
      itemsTitle: 'Items',
      subtotal: 'Subtotal',
      discount: 'Discount',
      shippingFee: 'Shipping fee',
      finalAmount: 'Final amount',
      paymentMethod: 'Payment method',
      paymentStatus: 'Payment status',
      status: 'Order status',
      viewDetail: 'View details',
    },
    notFound: {
      title: 'Page not found',
      description: 'Sorry, the page you are looking for does not exist.',
    },
    admin: {
      sidebar: {
        title: 'SachVui Admin',
        books: 'Manage books',
        orders: 'Manage orders',
        categories: 'Manage categories',
        authors: 'Manage authors',
        publishers: 'Manage publishers',
        users: 'Manage users',
        roles: 'Manage roles',
        permissions: 'Manage permissions',
        promotions: 'Manage promotions',
        importReceipts: 'Import receipts',
        inventory: 'Inventory',
        shipments: 'Shipment management',
        reviews: 'Reviews',
        notifications: 'Notifications',
        chat: 'Support chat',
        suppliers: 'Manage suppliers',
        customers: 'Manage customers',
        staff: 'Manage staff',
        settings: 'Account settings',
        adminAccount: 'Admin account',
        references: 'Reference data',
      },
      dashboard: {
        description: 'Store performance overview',
        recentOrders: 'Recent orders',
        emptyOrders: 'No orders to display yet',
        stats: {
          totalBooks: 'Total books',
          ordersToday: 'Orders today',
          customers: 'Customers',
          revenueMonth: 'Monthly revenue',
        },
        columns: {
          orderId: 'Order ID',
          customer: 'Customer',
          total: 'Total',
          status: 'Status',
          date: 'Date',
        },
      },
      books: {
        title: 'Manage books',
        description: 'Manage the books available to customers.',
        sectionLabel: 'Book',
        countLabel: '{count} books',
        totalBooks: 'Total {count} books',
        addBook: 'Add new book',
        editBook: 'Edit book',
        detailTitle: 'Book details',
        createSuccess: 'Book created successfully',
        updateSuccess: 'Book updated successfully',
        deleteSuccess: 'Book deleted successfully',
        deleteTitle: 'Confirm book deletion',
        deleteDescription:
          'This action removes the book from the admin system and cannot be undone.',
        manageReferenceData: 'Manage categories, authors, publishers',
        manageReferences: 'Manage references',
        referencesMissing:
          'You need categories, authors, and publishers before creating books',
        referencesMissingDescription:
          'Open the reference data screen first to create the supporting options used by the book form.',
        referencesSplitDescription:
          'Open the categories, authors, or publishers pages to create the options used by the book form.',
        previewTitle: 'Live preview',
        imageGalleryTitle: 'Cover and detail images',
        imageGalleryHelp:
          'Upload multiple images, choose one cover, and arrange their display order on the book detail page.',
        imageGalleryEmpty: 'No images have been added to this book yet.',
        addImages: 'Choose one or more images',
        uploadingImages: 'Uploading images...',
        imageCount: '{count} images',
        setPrimaryImage: 'Set as cover',
        primaryImage: 'Cover image',
        moveImageLeft: 'Move image earlier',
        moveImageRight: 'Move image later',
        removeImage: 'Remove image from book',
        imageAltText: 'Image description',
        imageUploadPartial: '{failed} of {total} images failed to upload.',
        emptyPreviewTitle: 'The book title preview will appear here',
        emptyPreviewDescription:
          'The short description and image preview update immediately from the form.',
        emptyDescription: 'This book does not have a description yet.',
        confirmDelete: 'Delete "{title}"?',
        formDescription: 'Fill in the fields below to save the book changes.',
        searchPlaceholder: 'Search by title or author...',
        filterPlaceholder: 'Filter by category',
        allCategories: 'All categories',
        showingCount: 'Showing {count} of {total} books',
        stockUnit: 'units',
        inStock: 'In stock',
        outOfStock: 'Out of stock',
        fields: {
          title: 'Title',
          description: 'Description',
          price: 'Price',
          stockQuantity: 'Stock quantity',
          imageUrl: 'Image URL',
          category: 'Category',
          author: 'Author',
          publisher: 'Publisher',
        },
        columns: {
          book: 'Book',
          author: 'Author',
          category: 'Category',
          price: 'Price',
          stock: 'Stock',
          actions: 'Actions',
        },
        empty: 'No books found',
      },
      digitalAssets: {
        sectionLabel: 'Digital asset',
        title: 'Digital assets for this book',
        description:
          'Manage the digital editions attached to this book without leaving the book management page.',
        addAsset: 'Add asset',
        loading: 'Loading digital assets...',
        empty: 'No digital asset is attached to this book yet.',
        published: 'Published',
        unpublished: 'Draft',
        downloadAllowed: 'Download allowed',
        purchaseAllowed: 'Purchase allowed',
        editAsset: 'Edit asset',
        deleteAsset: 'Delete asset',
        priceLabel: 'Price',
        mimeTypeLabel: 'MIME type',
        fileSizeLabel: 'File size',
        updatedAtLabel: 'Updated at',
        createTitle: 'Create digital asset',
        editTitle: 'Edit digital asset',
        formatLabel: 'Format',
        titleLabel: 'Title',
        mainFileLabel: 'Main file',
        sampleFileLabel: 'Sample file',
        mainFileHelp:
          'Upload the main file through the file service. Metadata is derived from file_assets.',
        sampleFileHelp:
          'Upload an optional sample file for preview. Do not enter storage keys manually.',
        fileAssetIdLabel: 'File asset ID',
        sampleFileAssetIdLabel: 'Sample file asset ID',
        fileNameLabel: 'File name',
        checksumLabel: 'Checksum',
        save: 'Save asset',
        deleteTitle: 'Confirm digital asset deletion',
        confirmDelete: 'Delete digital asset "{title}"?',
        notUploaded: 'No uploaded file yet',
        noSample: 'No sample file',
        validationError: 'Please check the title, price, and uploaded files.',
        updatedSuccess: 'Digital asset updated.',
        createdSuccess: 'Digital asset created.',
        deletedSuccess: 'Digital asset deleted.',
      },
      orders: {
        title: 'Manage orders',
        totalOrders: 'Total {count} orders',
        updateSuccess: 'Order status updated',
        searchPlaceholder:
          'Search by order ID, customer name, phone number...',
        filterLabel: 'Filter by status',
        allStatuses: 'All statuses',
        detailTitle: 'Order details and status update',
        columns: {
          orderId: 'Order ID',
          customer: 'Customer',
          phone: 'Phone',
          products: 'Products',
          total: 'Total',
          status: 'Status',
          date: 'Date',
          actions: 'Actions',
        },
        productCount: '{count} product(s)',
        detail: {
          receiverAddress: 'Receiver address',
          paymentMethod: 'Payment method',
          paymentStatus: 'Payment status',
          updateStatus: 'Update status',
        },
        shipmentAssignment: {
          title: 'Assign shipper',
          description:
            'Only available for CONFIRMED or SHIPPING orders without an active shipment.',
          currentShipment: 'Current shipment',
          shipper: 'Shipper',
          assignedAt: 'Assigned at',
          activeNotice:
            'This order already has an active shipment. Track it from the shipment management page.',
          latestFailed: 'Latest shipment failed',
          chooseShipper: 'Choose shipper',
          noShippers: 'No shipper accounts found',
          assigning: 'Assigning shipper...',
          assign: 'Assign shipper',
          unavailable:
            'Shipment assignment is only available for CONFIRMED or SHIPPING orders.',
          ineligible:
            'This order is not eligible for shipment assignment right now',
          chooseShipperError: 'Choose a shipper before assigning',
          assignSuccess: 'Shipment assigned successfully',
          assignError: 'Unable to assign shipment',
        },
      },
      shipmentsPage: {
        title: 'Shipment management',
        totalShipments: '{count} shipments in the system',
        assignTitle: 'Assign shipper',
        assignDescription:
          'Only CONFIRMED or SHIPPING orders without an active shipment are shown.',
        ordersReady: '{count} orders ready',
        orderLabel: 'Order',
        noEligibleOrders: 'No eligible orders left',
        shipperLabel: 'Shipper',
        noShippers: 'No shipper accounts found',
        assigning: 'Assigning...',
        assign: 'Assign shipper',
        filterLabel: 'Filter by status',
        allStatuses: 'All statuses',
        metrics: {
          delivering: 'Delivering',
          delivered: 'Delivered',
          failed: 'Failed',
        },
        loading: 'Loading shipments...',
        empty: 'No shipments match the current filter',
        detailLoading: 'Loading detail...',
        detailTitle: 'Shipment detail',
        deliveryInfoTitle: 'Delivery info',
        timelineTitle: 'Timeline',
        confirmDelivered: 'Confirm delivered',
        confirming: 'Confirming...',
        confirmHint:
          'This action is only available while the shipment is DELIVERING.',
        loadError: 'Unable to load shipment data',
        detailError: 'Unable to load shipment detail',
        assignValidationError:
          'Choose an order and a shipper before assigning',
        assignSuccess: 'Shipment assigned successfully',
        assignError: 'Unable to assign shipment',
        invalidConfirmState:
          'Delivery can only be confirmed when the shipment is DELIVERING',
        confirmSuccess: 'Delivery confirmed successfully',
        confirmError: 'Unable to confirm delivery',
        columns: {
          shipmentId: 'Shipment ID',
          order: 'Order',
          receiver: 'Receiver',
          shipper: 'Shipper',
          status: 'Status',
          amount: 'Amount',
          assignedAt: 'Assigned at',
          actions: 'Actions',
        },
        detail: {
          orderCode: 'Order code',
          shipper: 'Shipper',
          payment: 'Payment',
          totalAmount: 'Total amount',
          receiver: 'Receiver',
          phone: 'Phone',
          address: 'Address',
          orderStatus: 'Order status',
          failureReason: 'Failure reason',
          assigned: 'Assigned',
          pickedUp: 'Picked up',
          delivering: 'Delivering',
          delivered: 'Delivered',
          updatedAt: 'Updated at',
        },
      },
      usersPage: {
        title: 'Manage users',
        description:
          'Review user accounts, assigned roles, and lock status from the admin area.',
        totalUsers: '{count} users',
        searchPlaceholder:
          'Search by username, email, phone number, or role...',
        loadError: 'Unable to load the user list',
        fallbackNotice:
          'The user list is temporarily unavailable. The current account is shown instead.',
        empty: 'No users found',
        active: 'Active',
        inactive: 'Inactive',
        locked: 'Locked',
        unlocked: 'Unlocked',
        columns: {
          username: 'Account',
          contact: 'Contact',
          roles: 'Roles',
          status: 'Status',
          locked: 'Lock',
          updatedAt: 'Updated',
        },
      },
      userManagement: {
        addEmployee: 'Add employee',
        avatarLabel: 'Avatar image',
        createDialogDescription:
          'Create a staff or admin account directly from the admin area.',
        createError: 'Unable to create employee',
        createSuccess: 'Employee created successfully',
        deleteDescription:
          'This action removes the account from the admin system and cannot be undone.',
        deleteError: 'Unable to delete account',
        deleteSuccess: 'Account deleted successfully',
        deleteTitle: 'Confirm account deletion',
        detailsCustomer: 'Customer details',
        detailsStaff: 'Employee details',
        editDialogDescription:
          'Edit the information available for staff accounts.',
        editError: 'Unable to update employee',
        editLockedHint:
          'Administrator accounts cannot be edited here.',
        editSuccess: 'Employee updated successfully',
        editTitle: 'Edit employee',
        lockDescription: {
          lock:
            'This account will be locked and cannot sign in until it is unlocked again.',
          unlock:
            'This account will be unlocked so it can sign in and use the system again.',
        },
        lockError: 'Unable to update lock status',
        lockSuccess: {
          lock: 'Account locked successfully',
          unlock: 'Account unlocked successfully',
        },
        lockTitle: {
          lock: 'Lock account',
          unlock: 'Unlock account',
        },
        lockAction: {
          lock: 'Lock',
          unlock: 'Unlock',
        },
        role: 'Role',
        selfManageBlocked:
          'You cannot lock or delete the currently signed-in account.',
        showingCount: 'Showing {count} of {total} accounts',
      },
      customersPage: {
        title: 'Manage customers',
        description:
          'Review active customer accounts in the system.',
        totalUsers: '{count} customers',
        searchPlaceholder: 'Search by username or role...',
        loadError: 'Unable to load the customer list',
        empty: 'No customers found',
      },
      staffPage: {
        title: 'Manage staff',
        description:
          'Review staff and administrator accounts in the system.',
        totalUsers: '{count} staff members',
        searchPlaceholder: 'Search by username or role...',
        loadError: 'Unable to load the staff list',
        empty: 'No staff members found',
      },
      rolesPage: {
        title: 'Manage roles',
        description:
          'Review system roles and the permission codes attached to each role.',
        totalRoles: '{count} roles',
        searchPlaceholder:
          'Search by role name, description, or permission code...',
        loadError: 'Unable to load the role list',
        empty: 'No roles found',
        permissionCount: '{count} permissions',
        noDescription: 'No description',
      },
      permissionsPage: {
        title: 'Manage permissions',
        description:
          'Review the permission codes used by the administration system.',
        totalPermissions: '{count} permissions',
        searchPlaceholder: 'Search by permission code or description...',
        loadError: 'Unable to load the permission list',
        empty: 'No permissions found',
        noDescription: 'No description',
        columns: {
          code: 'Permission code',
          description: 'Description',
          updatedAt: 'Updated',
        },
      },
      promotionsPage: {
        title: 'Manage promotions',
        description:
          'Review discount campaigns and their current usage state.',
        totalPromotions: '{count} promotions',
        searchPlaceholder: 'Search by campaign name, code, or description...',
        loadError: 'Unable to load the promotion list',
        empty: 'No promotions found',
        noDescription: 'No description',
        percentType: 'Percentage',
        fixedType: 'Fixed amount',
        noEndDate: 'No limit',
        usageWithLimit: '{used} / {limit} uses',
        usageNoLimit: '{used} uses',
        columns: {
          campaign: 'Campaign',
          discount: 'Discount',
          usage: 'Usage',
          schedule: 'Schedule',
          status: 'Status',
        },
        statuses: {
          active: 'Active',
          upcoming: 'Upcoming',
          expired: 'Expired',
          inactive: 'Inactive',
        },
      },
      references: {
        title: 'Manage reference data',
        description:
          'Create and edit categories, authors, and publishers for the book form.',
        addCategory: 'Add category',
        addAuthor: 'Add author',
        addPublisher: 'Add publisher',
        saveSuccess: 'Changes saved successfully',
        deleteSuccess: 'Selected item deleted',
        deleteTitle: 'Confirm deletion',
        deleteDescription:
          'This action removes the item from reference data and cannot be undone.',
        confirmDelete: 'Delete "{name}"?',
        emptyCategories: 'No categories yet',
        emptyAuthors: 'No authors yet',
        emptyPublishers: 'No publishers yet',
        biography: 'Biography',
        categoryImage: 'Category image',
        publisherLogo: 'Publisher logo',
        imageUploadHint: 'Choose a JPG, PNG, or WebP image.',
        sections: {
          categories: 'Categories',
          authors: 'Authors',
          publishers: 'Publishers',
        },
      },
      referencePages: {
        categories: {
          title: 'Manage categories',
          description: 'Create and edit categories used for books.',
          section: 'Categories',
          countLabel: '{count} categories',
          add: 'Add category',
          editTitle: 'Edit category',
          detailTitle: 'Category details',
          searchPlaceholder: 'Search categories...',
          empty: 'No categories yet',
          emptyDescription: 'This category does not have a description yet.',
          code: 'Category code',
          vietnamese: 'Vietnamese content',
          english: 'English content',
          localizedName: 'Display name',
          localizedDescription: 'Description',
        },
        authors: {
          title: 'Manage authors',
          description: 'Create and edit authors used for books.',
          section: 'Authors',
          countLabel: '{count} authors',
          add: 'Add author',
          editTitle: 'Edit author',
          detailTitle: 'Author details',
          searchPlaceholder: 'Search authors...',
          empty: 'No authors yet',
          emptyDescription: 'This author does not have a biography yet.',
        },
        publishers: {
          title: 'Manage publishers',
          description: 'Create and edit publishers used for books.',
          section: 'Publishers',
          countLabel: '{count} publishers',
          add: 'Add publisher',
          editTitle: 'Edit publisher',
          detailTitle: 'Publisher details',
          searchPlaceholder: 'Search publishers...',
          empty: 'No publishers yet',
          emptyDescription: 'This publisher does not have a description yet.',
        },
      },
    },
    categories: {
      all: 'All',
    },
    orderStatus: {
      pending: 'Pending confirmation',
      processing: 'Processing',
      shipped: 'Shipped',
      delivered: 'Delivered',
      cancelled: 'Cancelled',
      PENDING: 'Pending confirmation',
      CONFIRMED: 'Confirmed',
      SHIPPING: 'Shipping',
      DELIVERED: 'Delivered',
      CANCELLED: 'Cancelled',
    },
    shipmentStatus: {
      ASSIGNED: 'Assigned',
      PICKED_UP: 'Picked up',
      DELIVERING: 'Delivering',
      DELIVERED: 'Delivered',
      FAILED: 'Failed',
    },
    paymentMethods: {
      CASH: 'Cash',
      COD: 'Cash on delivery',
      BANK_TRANSFER: 'Bank transfer',
      VNPAY: 'VNPAY wallet',
      MOMO: 'MoMo wallet',
    },
    paymentStatus: {
      UNPAID: 'Unpaid',
      PAID: 'Paid',
      FAILED: 'Failed',
      REFUNDED: 'Refunded',
    },
    roles: {
      ADMIN: 'Administrator',
      STAFF: 'Staff',
      SHIPPER: 'Shipper',
      USER: 'User',
    },
    genders: {
      MALE: 'Male',
      FEMALE: 'Female',
      OTHER: 'Other',
    },
  },
} as const

function getMutableMessageSection(
  root: unknown,
  ...path: string[]
): Record<string, unknown> {
  let current = root

  for (const key of path) {
    if (typeof current !== 'object' || current === null) {
      throw new Error(`Missing message section: ${path.join('.')}`)
    }
    current = (current as Record<string, unknown>)[key]
  }

  if (typeof current !== 'object' || current === null) {
    throw new Error(`Missing message section: ${path.join('.')}`)
  }

  return current as Record<string, unknown>
}

Object.assign(messages.vi.admin.permissionsPage as Record<string, unknown>, {
  showingCount: 'Hiển thị {count} trên {total} quyền',
  details: {
    title: 'Chi tiết quyền',
    code: 'Mã quyền',
    description: 'Mô tả',
  },
})

Object.assign(messages.en.admin.permissionsPage as Record<string, unknown>, {
  showingCount: 'Showing {count} of {total} permissions',
  details: {
    title: 'Permission details',
    code: 'Permission code',
    description: 'Description',
  },
})

Object.assign(messages.vi.admin.rolesPage as Record<string, unknown>, {
  addRole: 'Thêm vai trò',
  detailTitle: 'Chi tiết vai trò',
  editTitle: 'Sửa vai trò',
  deleteTitle: 'Xác nhận xóa vai trò',
  deleteDescription:
    'Hành động này sẽ xóa vai trò khỏi hệ thống và không thể hoàn tác.',
  createSuccess: 'Đã tạo vai trò',
  updateSuccess: 'Đã cập nhật vai trò',
  deleteSuccess: 'Đã xóa vai trò',
  saveError: 'Không lưu được vai trò',
  deleteError: 'Không xóa được vai trò',
  permissionList: 'Danh sách quyền',
  noPermissions: 'Chưa có quyền nào',
  showingCount: 'Hiển thị {count} trên {total} vai trò',
  roleName: 'Tên vai trò',
  choosePermissions: 'Chọn quyền',
  roleDescription: 'Mô tả',
})

Object.assign(messages.en.admin.rolesPage as Record<string, unknown>, {
  addRole: 'Add role',
  detailTitle: 'Role details',
  editTitle: 'Edit role',
  deleteTitle: 'Confirm role deletion',
  deleteDescription:
    'This action removes the role from the system and cannot be undone.',
  createSuccess: 'Role created successfully',
  updateSuccess: 'Role updated successfully',
  deleteSuccess: 'Role deleted successfully',
  saveError: 'Unable to save role',
  deleteError: 'Unable to delete role',
  permissionList: 'Permission list',
  noPermissions: 'No permissions assigned',
  showingCount: 'Showing {count} of {total} roles',
  roleName: 'Role name',
  choosePermissions: 'Choose permissions',
  roleDescription: 'Description',
})

Object.assign(messages.vi.admin.promotionsPage as Record<string, unknown>, {
  addPromotion: 'Tạo coupon',
  detailTitle: 'Chi tiết coupon',
  editTitle: 'Sửa coupon',
  deleteTitle: 'Xác nhận xóa coupon',
  deleteDescription:
    'Coupon này sẽ bị xóa khỏi hệ thống và không thể hoàn tác.',
  createSuccess: 'Đã tạo coupon',
  updateSuccess: 'Đã cập nhật coupon',
  deleteSuccess: 'Đã xóa coupon',
  saveError: 'Không lưu được coupon',
  deleteError: 'Không xóa được coupon',
  showingCount: 'Hiển thị {count} trên {total} coupon',
  codeLabel: 'Mã coupon',
  descriptionLabel: 'Mô tả',
  couponTypeLabel: 'Loại coupon',
  discountTypeLabel: 'Kiểu giảm giá',
  discountValueLabel: 'Giá trị giảm',
  minOrderAmountLabel: 'Đơn tối thiểu',
  maxDiscountAmountLabel: 'Giảm tối đa',
  maxUsageCountLabel: 'Lượt dùng tối đa',
  startsAtLabel: 'Bắt đầu lúc',
  expiresAtLabel: 'Hết hạn lúc',
  activeLabel: 'Đang kích hoạt',
  noLimit: 'Không giới hạn',
  noMaxDiscount: 'Không giới hạn',
  codeHint: 'Tự động viết hoa và bỏ khoảng trắng.',
  percentageHint: 'Coupon phần trăm chỉ nhận giá trị từ 0 đến 100.',
  startsAtHint: 'Thời gian tính theo giờ máy hiện tại.',
  expiresAtHint: 'Phải sau thời điểm bắt đầu.',
  invalidForm: 'Vui lòng kiểm tra lại thông tin coupon.',
  codeRequired: 'Vui lòng nhập mã coupon.',
  discountValueRequired: 'Vui lòng nhập giá trị giảm.',
  discountValuePositive: 'Giá trị giảm phải lớn hơn 0.',
  discountValuePercentageMax:
    'Giá trị giảm theo phần trăm không được vượt quá 100.',
  minOrderAmountInvalid: 'Đơn tối thiểu phải từ 0 trở lên.',
  maxDiscountAmountInvalid: 'Giảm tối đa phải lớn hơn 0 nếu được nhập.',
  maxUsageCountInvalid: 'Lượt dùng tối đa phải là số nguyên dương.',
  startsAtInvalid: 'Vui lòng chọn thời điểm bắt đầu hợp lệ.',
  expiresAtInvalid: 'Vui lòng chọn thời điểm hết hạn hợp lệ.',
  expiresAtAfterStartsAt:
    'Thời điểm hết hạn phải sau thời điểm bắt đầu.',
  deleteBlockedShort: 'Đã phát sinh lượt dùng',
  deleteBlockedReason: 'Coupon đã có lượt sử dụng, không thể xóa.',
  formDescription: 'Cập nhật thông tin mã giảm giá và lịch áp dụng.',
})

Object.assign(messages.en.admin.promotionsPage as Record<string, unknown>, {
  addPromotion: 'Create coupon',
  detailTitle: 'Coupon details',
  editTitle: 'Edit coupon',
  deleteTitle: 'Confirm coupon deletion',
  deleteDescription:
    'This coupon will be removed from the system and cannot be undone.',
  createSuccess: 'Coupon created successfully',
  updateSuccess: 'Coupon updated successfully',
  deleteSuccess: 'Coupon deleted successfully',
  saveError: 'Unable to save coupon',
  deleteError: 'Unable to delete coupon',
  showingCount: 'Showing {count} of {total} coupons',
  codeLabel: 'Coupon code',
  descriptionLabel: 'Description',
  couponTypeLabel: 'Coupon type',
  discountTypeLabel: 'Discount type',
  discountValueLabel: 'Discount value',
  minOrderAmountLabel: 'Minimum order amount',
  maxDiscountAmountLabel: 'Maximum discount amount',
  maxUsageCountLabel: 'Maximum usage count',
  startsAtLabel: 'Starts at',
  expiresAtLabel: 'Expires at',
  activeLabel: 'Active',
  noLimit: 'No limit',
  noMaxDiscount: 'No cap',
  codeHint: 'Automatically uppercased with spaces removed.',
  percentageHint: 'Percentage coupons only accept values from 0 to 100.',
  startsAtHint: 'Time is based on the current device timezone.',
  expiresAtHint: 'Must be later than the start time.',
  invalidForm: 'Please review the coupon details.',
  codeRequired: 'Coupon code is required.',
  discountValueRequired: 'Discount value is required.',
  discountValuePositive: 'Discount value must be greater than 0.',
  discountValuePercentageMax: 'Percentage discount cannot exceed 100.',
  minOrderAmountInvalid: 'Minimum order amount must be 0 or greater.',
  maxDiscountAmountInvalid:
    'Maximum discount amount must be greater than 0 when provided.',
  maxUsageCountInvalid: 'Maximum usage count must be a positive integer.',
  startsAtInvalid: 'Please choose a valid start time.',
  expiresAtInvalid: 'Please choose a valid expiration time.',
  expiresAtAfterStartsAt:
    'Expiration time must be later than the start time.',
  deleteBlockedShort: 'Already used',
  deleteBlockedReason:
    'This coupon already has usage history and cannot be deleted.',
  formDescription: 'Update the coupon information and active schedule.',
})

Object.assign(messages.vi.admin as Record<string, unknown>, {
  inventoryPage: {
    title: 'Quản lý tồn kho',
    description:
      'Theo dõi tồn hiện tại và lịch sử biến động kho của từng đầu sách.',
    totalBooks: '{count} đầu sách',
    search: 'Tìm theo tên sách, tác giả, thể loại...',
    empty: 'Không có sách trong kho',
    lowStock: 'Sắp hết',
    inStock: 'Còn hàng',
    outOfStock: 'Hết hàng',
    recentMovements: 'Biến động gần đây',
    movementHistory: 'Lịch sử biến động',
    latestMovement: 'Biến động mới nhất',
    book: 'Sách',
    price: 'Giá bán',
    stock: 'Tồn kho',
    loadError: 'Không tải được dữ liệu tồn kho',
    historyError: 'Không tải được lịch sử tồn kho',
    noMovement: 'Chưa có biến động kho',
    reference: 'Liên kết nghiệp vụ',
    beforeAfter: 'Trước / Sau',
    quantity: 'Số lượng',
    unknownReference: 'Không có tham chiếu',
    movementTypes: {
      IMPORT: 'Nhập kho',
      SALE: 'Bán hàng',
      CANCEL_ORDER: 'Hủy đơn',
      ADJUSTMENT: 'Điều chỉnh',
    },
  },
  reviewsPage: {
    title: 'Quản lý đánh giá',
    description:
      'Theo dõi đánh giá sách từ khách hàng, xem chi tiết và gỡ bỏ nội dung không phù hợp.',
    total: '{count} đánh giá',
    search: 'Tìm theo tên sách, người dùng, nhận xét...',
    empty: 'Chưa có đánh giá nào',
    loadError: 'Không tải được danh sách đánh giá',
    deleteError: 'Không xóa được đánh giá',
    deleteSuccess: 'Đã xóa đánh giá',
    reviewer: 'Người đánh giá',
    book: 'Sách',
    rating: 'Điểm số',
    comment: 'Nhận xét',
    noComment: 'Không có nhận xét',
    detailTitle: 'Chi tiết đánh giá',
    deleteTitle: 'Xác nhận xóa đánh giá',
    deleteDescription:
      'Đánh giá này sẽ bị xóa khỏi hệ thống và không thể khôi phục.',
    average: 'Điểm trung bình',
    withComment: 'Có nội dung',
    updatedAt: 'Cập nhật',
    unknownUser: 'Người dùng không xác định',
    unknownBook: 'Sách không xác định',
  },
  notificationsPage: {
    title: 'Quản lý thông báo',
    description:
      'Gửi thông báo cho từng tài khoản hoặc broadcast đến toàn bộ người dùng, vẫn giữ REST làm nguồn dữ liệu chính.',
    total: '{count} thông báo',
    search: 'Tìm theo tiêu đề, người nhận, nội dung...',
    empty: 'Chưa có thông báo nào',
    loadError: 'Không tải được danh sách thông báo',
    createError: 'Không gửi được thông báo',
    createSuccess: 'Đã gửi thông báo',
    broadcastSuccess: 'Đã broadcast thông báo cho {count} tài khoản',
    deleteError: 'Không xóa được thông báo',
    deleteSuccess: 'Đã xóa thông báo',
    add: 'Gửi cho 1 user',
    broadcast: 'Broadcast tất cả',
    detailTitle: 'Chi tiết thông báo',
    previewTitle: 'Xem trước',
    recipient: 'Người nhận',
    allRecipients: 'Tất cả người dùng',
    subject: 'Tiêu đề',
    content: 'Nội dung',
    type: 'Loại',
    link: 'Liên kết',
    chooseRecipient: 'Chọn người nhận',
    unread: 'Chưa đọc',
    read: 'Đã đọc',
    readAt: 'Đọc lúc',
    createdAt: 'Gửi lúc',
    noReadAt: 'Chưa đọc',
    noContent: 'Không có nội dung',
    noType: 'Mặc định',
    noLink: 'Không có',
    optional: 'Tùy chọn',
    loadMore: 'Tải thêm',
    delete: 'Xóa',
    recipientCount: 'Tài khoản có thể gửi',
    unknownUser: 'Người nhận không xác định',
    status: 'Trạng thái',
    actions: 'Thao tác',
  },
})

Object.assign(messages.en.admin as Record<string, unknown>, {
  inventoryPage: {
    title: 'Inventory management',
    description: 'Track current stock and movement history for each book title.',
    totalBooks: '{count} books',
    search: 'Search by title, author, or category...',
    empty: 'No inventory found',
    lowStock: 'Low stock',
    inStock: 'In stock',
    outOfStock: 'Out of stock',
    recentMovements: 'Recent movements',
    movementHistory: 'Movement history',
    latestMovement: 'Latest movement',
    book: 'Book',
    price: 'Price',
    stock: 'Stock',
    loadError: 'Unable to load inventory',
    historyError: 'Unable to load stock history',
    noMovement: 'No stock movement yet',
    reference: 'Reference',
    beforeAfter: 'Before / After',
    quantity: 'Quantity',
    unknownReference: 'No reference',
    movementTypes: {
      IMPORT: 'Import',
      SALE: 'Sale',
      CANCEL_ORDER: 'Cancel order',
      ADJUSTMENT: 'Adjustment',
    },
  },
  reviewsPage: {
    title: 'Review management',
    description:
      'Review customer book ratings, inspect details, and remove inappropriate feedback.',
    total: '{count} reviews',
    search: 'Search by book, user, or review text...',
    empty: 'No reviews found',
    loadError: 'Unable to load reviews',
    deleteError: 'Unable to delete the review',
    deleteSuccess: 'Review deleted',
    reviewer: 'Reviewer',
    book: 'Book',
    rating: 'Rating',
    comment: 'Comment',
    noComment: 'No comment',
    detailTitle: 'Review details',
    deleteTitle: 'Confirm review deletion',
    deleteDescription:
      'This review will be removed from the system and cannot be restored.',
    average: 'Average rating',
    withComment: 'With comment',
    updatedAt: 'Updated',
    unknownUser: 'Unknown user',
    unknownBook: 'Unknown book',
  },
  notificationsPage: {
    title: 'Notification management',
    description:
      'Send notifications to a single account or broadcast them to all users while keeping REST as the source of truth.',
    total: '{count} notifications',
    search: 'Search by title, recipient, or content...',
    empty: 'No notifications found',
    loadError: 'Unable to load notifications',
    createError: 'Unable to send the notification',
    createSuccess: 'Notification sent',
    broadcastSuccess: 'Broadcast notification sent to {count} accounts',
    deleteError: 'Unable to delete the notification',
    deleteSuccess: 'Notification deleted',
    add: 'Send to one user',
    broadcast: 'Broadcast all',
    detailTitle: 'Notification details',
    previewTitle: 'Preview',
    recipient: 'Recipient',
    allRecipients: 'All users',
    subject: 'Title',
    content: 'Content',
    type: 'Type',
    link: 'Link',
    chooseRecipient: 'Choose recipient',
    unread: 'Unread',
    read: 'Read',
    readAt: 'Read at',
    createdAt: 'Sent at',
    noReadAt: 'Not read yet',
    noContent: 'No content',
    noType: 'Default',
    noLink: 'None',
    optional: 'Optional',
    loadMore: 'Load more',
    delete: 'Delete',
    recipientCount: 'Available recipients',
    unknownUser: 'Unknown recipient',
    status: 'Status',
    actions: 'Actions',
  },
})

Object.assign(messages.vi.admin as Record<string, unknown>, {
  importReceiptsPage: {
    title: 'Quản lý nhập kho',
    description: 'Tạo và theo dõi các phiếu nhập sách vào kho.',
    total: '{count} phiếu nhập',
    add: 'Tạo phiếu nhập',
    search: 'Tìm theo nhà cung cấp, mã phiếu hoặc tên sách...',
    empty: 'Chưa có phiếu nhập nào',
    receipt: 'Phiếu nhập',
    supplier: 'Nhà cung cấp',
    totalAmount: 'Tổng tiền',
    items: 'Số dòng',
    createdAt: 'Ngày nhập',
    detailTitle: 'Chi tiết phiếu nhập',
    note: 'Ghi chú',
    noNote: 'Không có ghi chú',
    loadError: 'Không tải được danh sách phiếu nhập',
    saveError: 'Không tạo được phiếu nhập',
    saveSuccess: 'Đã tạo phiếu nhập',
    book: 'Sách',
    quantity: 'Số lượng',
    unitCost: 'Giá nhập',
    addLine: 'Thêm dòng sách',
    removeLine: 'Xóa dòng',
    chooseSupplier: 'Chọn nhà cung cấp',
    chooseBook: 'Chọn sách',
  },
  suppliersPage: {
    pageTitle: 'Quản lý nhà cung cấp',
    pageDescription:
      'Theo dõi và cập nhật danh sách nhà cung cấp dùng cho nhập hàng.',
    totalSuppliers: '{count} nhà cung cấp',
    addSupplier: 'Thêm nhà cung cấp',
    searchPlaceholder: 'Tìm theo tên, email hoặc số điện thoại...',
    loadError: 'Không tải được danh sách nhà cung cấp',
    empty: 'Chưa có nhà cung cấp nào',
    showingCount: 'Hiển thị {count} trên {total} nhà cung cấp',
    nameColumn: 'Nhà cung cấp',
    detailTitle: 'Chi tiết nhà cung cấp',
    editTitle: 'Sửa nhà cung cấp',
    deleteTitle: 'Xác nhận xóa nhà cung cấp',
    deleteDescription:
      'Hành động này sẽ xóa nhà cung cấp khỏi hệ thống và không thể hoàn tác.',
    createSuccess: 'Đã tạo nhà cung cấp',
    updateSuccess: 'Đã cập nhật nhà cung cấp',
    deleteSuccess: 'Đã xóa nhà cung cấp',
    saveError: 'Không lưu được nhà cung cấp',
    deleteError: 'Không xóa được nhà cung cấp',
    noPhone: 'Chưa có số điện thoại',
    noEmail: 'Chưa có email',
    noAddress: 'Chưa có địa chỉ',
    noNote: 'Chưa có ghi chú',
    phoneLabel: 'Số điện thoại',
    emailLabel: 'Email',
    addressLabel: 'Địa chỉ',
    noteLabel: 'Ghi chú',
  },
  settingsPage: {
    title: 'Cài đặt tài khoản quản trị',
    description:
      'Xem thông tin tài khoản, cập nhật profile và tùy chỉnh không gian làm việc quản trị.',
    overview: 'Tổng quan tài khoản',
    preferences: 'Tùy chọn giao diện',
    role: 'Vai trò',
    status: 'Trạng thái',
    active: 'Đang hoạt động',
    inactive: 'Không hoạt động',
    accountCreated: 'Ngày tạo tài khoản',
    accountUpdated: 'Cập nhật gần nhất',
    theme: 'Chế độ sáng tối',
    themeDescription:
      'Chuyển giao diện admin mà không cần rời khỏi bảng điều khiển.',
    language: 'Ngôn ngữ hiển thị',
    languageDescription: 'Áp dụng ngay cho toàn bộ giao diện quản trị.',
    avatarLabel: 'Ảnh đại diện',
    accountSaved: 'Đã cập nhật thông tin tài khoản',
    profileSaved: 'Đã cập nhật profile',
    profileLoadError: 'Không tải được thông tin profile',
  },
})

Object.assign(messages.en.admin as Record<string, unknown>, {
  importReceiptsPage: {
    title: 'Import receipts',
    description: 'Create and review book inventory import receipts.',
    total: '{count} receipts',
    add: 'Create receipt',
    search: 'Search by supplier, receipt id, or book title...',
    empty: 'No import receipts found',
    receipt: 'Receipt',
    supplier: 'Supplier',
    totalAmount: 'Total',
    items: 'Items',
    createdAt: 'Created',
    detailTitle: 'Receipt details',
    note: 'Note',
    noNote: 'No note',
    loadError: 'Unable to load import receipts',
    saveError: 'Unable to create receipt',
    saveSuccess: 'Import receipt created',
    book: 'Book',
    quantity: 'Quantity',
    unitCost: 'Unit cost',
    addLine: 'Add book line',
    removeLine: 'Remove line',
    chooseSupplier: 'Choose supplier',
    chooseBook: 'Choose book',
  },
  suppliersPage: {
    pageTitle: 'Manage suppliers',
    pageDescription: 'Review and update suppliers used for inventory imports.',
    totalSuppliers: '{count} suppliers',
    addSupplier: 'Add supplier',
    searchPlaceholder: 'Search by name, email, or phone...',
    loadError: 'Unable to load the supplier list',
    empty: 'No suppliers found',
    showingCount: 'Showing {count} of {total} suppliers',
    nameColumn: 'Supplier',
    detailTitle: 'Supplier details',
    editTitle: 'Edit supplier',
    deleteTitle: 'Confirm supplier deletion',
    deleteDescription:
      'This action removes the supplier from the system and cannot be undone.',
    createSuccess: 'Supplier created successfully',
    updateSuccess: 'Supplier updated successfully',
    deleteSuccess: 'Supplier deleted successfully',
    saveError: 'Unable to save supplier',
    deleteError: 'Unable to delete supplier',
    noPhone: 'No phone number',
    noEmail: 'No email',
    noAddress: 'No address',
    noNote: 'No note',
    phoneLabel: 'Phone number',
    emailLabel: 'Email',
    addressLabel: 'Address',
    noteLabel: 'Note',
  },
  settingsPage: {
    title: 'Admin account settings',
    description:
      'Review account details, update your profile, and adjust your admin workspace preferences.',
    overview: 'Account overview',
    preferences: 'Workspace preferences',
    role: 'Role',
    status: 'Status',
    active: 'Active',
    inactive: 'Inactive',
    accountCreated: 'Account created',
    accountUpdated: 'Last updated',
    theme: 'Light and dark mode',
    themeDescription:
      'Switch the admin interface theme without leaving the dashboard.',
    language: 'Display language',
    languageDescription: 'Applies immediately across the admin interface.',
    avatarLabel: 'Avatar image',
    accountSaved: 'Account information updated',
    profileSaved: 'Profile updated',
    profileLoadError: 'Unable to load profile information',
  },
})

Object.assign(messages.vi.checkout as Record<string, unknown>, {
  digitalAccessTitle: 'Truy cập thư viện số',
  digitalAccessDescription:
    'Đơn hàng này chỉ gồm tài sản số. Hệ thống sẽ bỏ qua địa chỉ giao hàng và cấp quyền truy cập sau khi thanh toán hợp lệ.',
  pickupDescription:
    'Nhận tại cửa hàng, nhân viên sẽ liên hệ xác nhận sau khi đặt đơn.',
  cashOnDeliveryDigitalOnlyDescription:
    'Không áp dụng COD cho đơn chỉ gồm tài sản số.',
  digitalOnlyPaymentNotice:
    'Đơn thư viện số hiện chỉ hỗ trợ chuyển khoản SePay để cấp quyền truy cập nhanh sau thanh toán.',
  standardPaymentNotice:
    'Giao tận nơi tính phí 30.000đ và miễn phí từ 200.000đ. Chuyển khoản SePay sẽ có QR sau khi đặt đơn, COD thanh toán lúc nhận hàng.',
  itemTypes: {
    digitalAsset: 'Tài sản số',
    physicalBook: 'Sách giấy',
  },
})

Object.assign(messages.en.checkout as Record<string, unknown>, {
  digitalAccessTitle: 'Digital library access',
  digitalAccessDescription:
    'This order contains digital assets only. Shipping details are skipped and access will be granted after successful payment.',
  pickupDescription:
    'Pick up at the store. Staff will contact you to confirm after checkout.',
  cashOnDeliveryDigitalOnlyDescription:
    'COD is not available for digital-only orders.',
  digitalOnlyPaymentNotice:
    'Digital library orders currently support SePay transfer only so access can be granted right after payment.',
  standardPaymentNotice:
    'Delivery costs 30,000 VND and becomes free from 200,000 VND. SePay transfer shows a QR after checkout, while COD is paid on delivery.',
  itemTypes: {
    digitalAsset: 'Digital asset',
    physicalBook: 'Physical book',
  },
})

Object.assign(messages.vi.common as Record<string, unknown>, {
  avatarFileErrors: {
    invalidType: 'Vui lòng chọn một file ảnh hợp lệ.',
    decodeFailed: 'Không đọc được file ảnh đã chọn.',
    tooLarge: 'Ảnh này quá lớn để lưu. Hãy chọn ảnh nhỏ hơn.',
  },
})

Object.assign(messages.en.common as Record<string, unknown>, {
  avatarFileErrors: {
    invalidType: 'Please choose a valid image file.',
    decodeFailed: 'Could not read the selected image file.',
    tooLarge: 'This image is too large to save. Please choose a smaller one.',
  },
})

Object.assign(messages.vi.admin.sidebar as Record<string, string>, {
  digitalAssets: 'Tài sản số',
})

Object.assign(messages.en.admin.sidebar as Record<string, string>, {
  digitalAssets: 'Digital assets',
})

Object.assign(messages.vi.admin as Record<string, unknown>, {
  digitalAssetsGuide: {
    badge: 'Luồng quản trị tài sản số',
    title: 'Tài sản số',
    description:
      'Tài sản số được quản lý theo từng sách. Hãy mở một sách trong Quản lý sách, thêm file PDF/EPUB/AUDIO, bật trạng thái phát hành và khách sẽ mua như một sản phẩm số.',
    openBooks: 'Mở quản lý sách',
    previewLibrary: 'Xem thư viện khách hàng',
    accessLabel: 'Cấp quyền truy cập',
    accessTitle: 'Quyền đọc sinh sau thanh toán',
    accessDescription:
      'Sau khi đơn SePay được xác nhận đã thanh toán, hệ thống tự cấp quyền đọc vào Thư viện số. Nếu đơn COD có kèm hàng giấy, quyền số chỉ được cấp khi đơn giao thành công.',
    stepLabel: 'Bước {count}',
    steps: {
      pickBookTitle: 'Chọn sách cần gắn bản số',
      pickBookDescription:
        'Vào Quản lý sách, bấm Xem một sách rồi thao tác trong khối Tài sản số ở cuối dialog.',
      uploadTitle: 'Upload file và cấu hình bán',
      uploadDescription:
        'Chọn định dạng, upload file chính, thêm file mẫu nếu cần, nhập giá và bật published/purchaseAllowed.',
      sellTitle: 'Khách mua và đọc trong thư viện',
      sellDescription:
        'Khách thêm bản số vào giỏ, thanh toán, sau đó đọc ở /library bằng URL ký tạm thời.',
    },
    checklistLabel: 'Checklist trước khi publish',
    checklistTitle: 'Những điều kiện để khách dùng được',
    checklistDescription:
      'Nếu thiếu một trong các điều kiện này, khách có thể không thấy asset, không mua được hoặc không tải được file.',
    checklist: {
      mainFile: 'File chính phải upload xong, là file private với purpose EBOOK_FILE.',
      published: 'Bật Published để bản số hiện ở trang chi tiết sách và thư viện.',
      purchaseAllowed: 'Bật Purchase allowed nếu muốn khách thêm bản số vào giỏ.',
      downloadAllowed: 'Chỉ bật Download allowed khi muốn cho khách tải file gốc.',
    },
  },
})

Object.assign(messages.en.admin as Record<string, unknown>, {
  digitalAssetsGuide: {
    badge: 'Digital asset admin flow',
    title: 'Digital assets',
    description:
      'Digital assets are managed per book. Open a book in Books, attach a PDF/EPUB/AUDIO file, publish it, and customers can purchase it as a digital product.',
    openBooks: 'Open books',
    previewLibrary: 'Preview customer library',
    accessLabel: 'Access granting',
    accessTitle: 'Access is granted after payment',
    accessDescription:
      'After a SePay order is marked paid, the system grants reading access automatically. For COD orders that include physical items, digital access is granted only after delivery.',
    stepLabel: 'Step {count}',
    steps: {
      pickBookTitle: 'Choose the book',
      pickBookDescription:
        'Go to Books, open a book detail dialog, then manage its digital assets at the bottom.',
      uploadTitle: 'Upload and configure',
      uploadDescription:
        'Choose a format, upload the main file, add an optional sample, set a price, and enable published/purchaseAllowed.',
      sellTitle: 'Customer buys and reads',
      sellDescription:
        'Customers add the digital edition to cart, pay, then read it from /library through signed URLs.',
    },
    checklistLabel: 'Pre-publish checklist',
    checklistTitle: 'Conditions customers need',
    checklistDescription:
      'If one condition is missing, customers may not see the asset, may not be able to buy it, or may not be able to download it.',
    checklist: {
      mainFile: 'The main file must be uploaded, private, and use purpose EBOOK_FILE.',
      published: 'Enable Published so the asset appears on book detail and library pages.',
      purchaseAllowed: 'Enable Purchase allowed if customers should be able to add it to cart.',
      downloadAllowed: 'Enable Download allowed only when customers may download the original file.',
    },
  },
})

Object.assign(messages.vi.library.page as Record<string, unknown>, {
  filterEmptyTitle: 'Không tìm thấy nội dung phù hợp',
  filterEmptyDescription:
    'Thử đổi từ khóa, định dạng hoặc trạng thái để xem lại thư viện.',
  emptyOwnedTitle: 'Bạn chưa có ebook hoặc sách nói',
  emptyOwnedDescription:
    'Chọn một bản số, hoàn tất thanh toán rồi quay lại đây để đọc hoặc nghe.',
  emptyOwnedAction: 'Khám phá sách',
  emptyGuideLabel: 'Bắt đầu trong 3 bước',
  emptySteps: {
    findBook: 'Mở trang chi tiết sách và chọn bản ebook hoặc sách nói.',
    buyDigital: 'Thêm vào giỏ và hoàn tất thanh toán.',
    readHere: 'Nội dung sẽ xuất hiện tại đây sau khi thanh toán thành công.',
  },
})

Object.assign(messages.en.library.page as Record<string, unknown>, {
  filterEmptyTitle: 'No matching content found',
  filterEmptyDescription:
    'Try changing the search term, format, or reading status to review your library.',
  emptyOwnedTitle: 'Your ebook and audiobook library is empty',
  emptyOwnedDescription:
    'Choose a digital edition, complete payment, then return here to read or listen.',
  emptyOwnedAction: 'Browse books',
  emptyGuideLabel: 'Get started in 3 steps',
  emptySteps: {
    findBook: 'Open a book detail page and choose an ebook or audiobook edition.',
    buyDigital: 'Add it to your cart and complete payment.',
    readHere: 'Your book will appear here after payment succeeds.',
  },
})

Object.assign(messages.vi.book.detail.digitalAssets as Record<string, unknown>, {
  calloutTitle: 'Có {count} phiên bản số',
  calloutDescription:
    'Định dạng {formats}, giá từ {price}. Bạn có thể đọc thử hoặc mua bản số bên dưới.',
  teaserDescription:
    'Ebook của cuốn này được bán ở trang riêng. Bấm vào để xem bản mẫu, giá và tùy chọn mua.',
  formatSummary: 'Định dạng {formats}',
  priceSummary: 'Giá từ {price}',
  sampleCallout: '{count} bản đọc thử',
  viewOptions: 'Xem ebook',
  viewPhysicalBook: 'Xem sách giấy',
  accessNoteTitle: 'Quyền đọc được cấp sau thanh toán',
  accessNoteDescription:
    'Sau khi đơn SePay được xác nhận đã thanh toán, bản số sẽ xuất hiện trong Thư viện số của bạn. Nếu được phép tải, nút tải sẽ nằm trong trang chi tiết tài sản.',
  formatLabel: 'Định dạng',
  priceLabel: 'Giá bản số',
  editionCountLabel: 'Phiên bản mở bán',
  downloadLabel: 'Tải xuống',
  sampleLabel: 'Đọc thử',
  sampleCountLabel: 'Bản mẫu mở xem',
  purchaseLabel: 'Trạng thái mua',
})

Object.assign(messages.en.book.detail.digitalAssets as Record<string, unknown>, {
  calloutTitle: '{count} digital edition(s) available',
  calloutDescription:
    'Formats: {formats}. Starting from {price}. You can open a sample or buy a digital edition below.',
  teaserDescription:
    'This ebook is sold on a dedicated page. Open it to view the sample, pricing, and purchase options.',
  formatSummary: 'Formats: {formats}',
  priceSummary: 'Starting from {price}',
  sampleCallout: '{count} sample(s)',
  viewOptions: 'View ebook',
  viewPhysicalBook: 'View print book',
  accessNoteTitle: 'Reading access is granted after payment',
  accessNoteDescription:
    'After the SePay order is confirmed paid, the digital edition appears in your Digital library. If downloads are allowed, the download action is available on the asset detail page.',
  formatLabel: 'Format',
  priceLabel: 'Digital price',
  editionCountLabel: 'Editions on sale',
  downloadLabel: 'Download',
  sampleLabel: 'Sample',
  sampleCountLabel: 'Samples available',
  purchaseLabel: 'Purchase status',
})

Object.assign(messages.vi.paymentMethods as Record<string, string>, {
  BANK_TRANSFER_QR: 'Chuyển khoản SePay',
})

Object.assign(messages.vi.paymentStatus as Record<string, string>, {
  PENDING: 'Chờ thanh toán',
  CANCELLED: 'Đã hủy',
  EXPIRED: 'Đã hết hạn',
})

Object.assign(messages.en.paymentMethods as Record<string, string>, {
  BANK_TRANSFER_QR: 'SePay bank transfer',
})

Object.assign(messages.en.paymentStatus as Record<string, string>, {
  PENDING: 'Waiting for payment',
  CANCELLED: 'Cancelled',
  EXPIRED: 'Expired',
})

Object.assign(messages.vi.admin.sidebar as Record<string, string>, {
  paymentReconciliation: 'Đối soát thanh toán',
  refunds: 'Hoàn tiền',
  outbox: 'Transactional outbox',
})

Object.assign(messages.en.admin.sidebar as Record<string, string>, {
  paymentReconciliation: 'Payment reconciliation',
  refunds: 'Refunds',
  outbox: 'Transactional outbox',
})

Object.assign(messages.vi.header.nav as Record<string, string>, {
  ebooks: 'Ebook',
})

Object.assign(messages.en.header.nav as Record<string, string>, {
  ebooks: 'Ebooks',
})

Object.assign(messages.vi as Record<string, unknown>, {
  ebookCatalog: {
    eyebrow: 'Kho ebook',
    title: 'Danh mục ebook đang mở bán',
    description:
      'Tổng hợp các bản PDF, EPUB và AUDIO đang phát hành công khai. Tìm theo tên sách, tác giả hoặc thể loại rồi mở trang ebook riêng để xem bản mẫu và mua ngay.',
    resultCount: '{count} ebook đang hiển thị',
    categoryTitle: 'Thể loại ebook',
    categoryCount: '{count} thể loại',
    selectedCategoryLabel: 'Đang lọc',
    clearCategory: 'Bỏ lọc',
    categorySearchPlaceholder: 'Tìm thể loại ebook...',
    categoryShowingCount: 'Hiển thị {count}/{total} thể loại',
    categoryEmptyTitle: 'Không có thể loại phù hợp',
    categoryEmptyDescription: 'Thử đổi từ khóa để xem lại danh sách thể loại.',
    searchPlaceholder: 'Tìm ebook, tên sách hoặc tác giả...',
    sortPlaceholder: 'Sắp xếp',
    sortFeatured: 'Mới phát hành',
    sortFormat: 'Theo định dạng',
    sortPriceAsc: 'Giá tăng dần',
    sortPriceDesc: 'Giá giảm dần',
    emptyTitle: 'Chưa có ebook phù hợp',
    emptyDescription:
      'Thử đổi từ khóa hoặc thể loại để xem các ebook đang mở bán.',
    errorTitle: 'Không tải được danh mục ebook',
    errorDescription: 'Vui lòng thử lại sau ít phút.',
    sampleAvailable: 'Có bản mẫu',
    downloadAllowed: 'Cho tải xuống',
    openDetail: 'Xem trang ebook',
    publisherLine: 'Phát hành bởi {publisher}',
  },
})

Object.assign(messages.en as Record<string, unknown>, {
  ebookCatalog: {
    eyebrow: 'Ebook catalog',
    title: 'Digital editions now on sale',
    description:
      'Browse public PDF, EPUB, and AUDIO releases. Search by book title, author, or category, then open the dedicated ebook page to preview and purchase.',
    resultCount: '{count} ebooks showing',
    categoryTitle: 'Ebook categories',
    categoryCount: '{count} categories',
    selectedCategoryLabel: 'Current filter',
    clearCategory: 'Clear',
    categorySearchPlaceholder: 'Search ebook categories...',
    categoryShowingCount: 'Showing {count}/{total} categories',
    categoryEmptyTitle: 'No matching categories',
    categoryEmptyDescription: 'Try another keyword to review the category list.',
    searchPlaceholder: 'Search ebooks, book titles, or authors...',
    sortPlaceholder: 'Sort by',
    sortFeatured: 'Newest releases',
    sortFormat: 'By format',
    sortPriceAsc: 'Price: low to high',
    sortPriceDesc: 'Price: high to low',
    emptyTitle: 'No matching ebooks yet',
    emptyDescription:
      'Try changing the keyword or category to review ebooks that are currently on sale.',
    errorTitle: 'Could not load the ebook catalog',
    errorDescription: 'Please try again in a few minutes.',
    sampleAvailable: 'Sample available',
    downloadAllowed: 'Download enabled',
    openDetail: 'Open ebook page',
    publisherLine: 'Published by {publisher}',
  },
})

Object.assign(messages.vi.book.detail.digitalAssets as Record<string, unknown>, {
  heroBadge: 'Trang ebook chính thức',
  browseCatalog: 'Khám phá kho ebook',
  availableFormatsTitle: 'Các phiên bản đang mở bán',
  availableFormatsDescription:
    'Mỗi phiên bản có thể khác định dạng, khả năng tải xuống và bản mẫu. Chọn phiên bản phù hợp rồi thêm vào giỏ ngay tại đây.',
  summaryTitle: 'Mua phiên bản số',
  summaryDescription:
    'Chọn định dạng phù hợp và thanh toán để nhận quyền đọc ngay trong thư viện số của bạn.',
  editionCountSummary: '{count} phiên bản đang mở bán',
})

Object.assign(messages.en.book.detail.digitalAssets as Record<string, unknown>, {
  heroBadge: 'Official ebook page',
  browseCatalog: 'Browse ebook catalog',
  availableFormatsTitle: 'Digital editions on sale',
  availableFormatsDescription:
    'Each edition can differ by format, download access, and sample availability. Pick the version that fits you, then add it to cart here.',
  summaryTitle: 'Buy the digital edition',
  summaryDescription:
    'Choose the right format and complete payment to unlock reading access in your digital library right away.',
  editionCountSummary: '{count} editions on sale',
})

Object.assign(messages.vi.header as Record<string, unknown>, {
  myWishlist: 'Yêu thích của tôi',
})

Object.assign(messages.en.header as Record<string, unknown>, {
  myWishlist: 'My wishlist',
})

Object.assign(messages.vi.home as Record<string, unknown>, {
  recentlyViewedTitle: 'Đã xem gần đây',
  recentlyViewedDescription:
    'Những tựa sách bạn vừa mở sẽ được lưu tại đây để quay lại nhanh hơn.',
})

Object.assign(messages.en.home as Record<string, unknown>, {
  recentlyViewedTitle: 'Recently viewed',
  recentlyViewedDescription:
    'Books you opened recently are stored here for quick access.',
})

Object.assign(messages.vi.home as Record<string, unknown>, {
  funDiscovery: {
    title: 'Khám phá vui',
    subtitle: 'Một vài cách nhẹ nhàng để tìm sách và săn ưu đãi hôm nay.',
    bookMatchTitle: 'Hôm nay đọc gì?',
    bookMatchDescription: 'Trả lời vài câu hỏi để tìm sách hợp mood.',
    bookMatchCta: 'Mở quiz',
    couponGameTitle: 'Vòng quay săn mã',
    couponGameDescription: 'Quay mỗi ngày một lần để nhận mã giảm giá.',
    couponGameCta: 'Săn mã',
    recentTitle: 'Sách bạn vừa xem',
    recentDescription: 'Mở lại nhanh 3 tựa gần nhất và tiếp tục nơi bạn dừng.',
    recentItemLabel: 'Vừa xem',
    recentCta: 'Xem tiếp',
    recentEmptyTitle: 'Chưa có sách nào ở đây',
    recentEmptyDescription:
      'Khi bạn mở trang chi tiết sách, danh sách xem gần đây sẽ hiện ra.',
    recentEmptyCta: 'Khám phá sách',
    wishlistTitle: 'Danh sách yêu thích',
    wishlistDescription: 'Quay lại những cuốn bạn đã lưu.',
    wishlistCta: 'Mở wishlist',
  },
})

Object.assign(messages.en.home as Record<string, unknown>, {
  funDiscovery: {
    title: 'Fun discovery',
    subtitle: 'A few light ways to find books and hunt offers today.',
    bookMatchTitle: 'What should I read today?',
    bookMatchDescription: 'Answer a few quick questions to match the right mood.',
    bookMatchCta: 'Open quiz',
    couponGameTitle: 'Coupon spin',
    couponGameDescription: 'Spin once a day to reveal a discount code.',
    couponGameCta: 'Hunt deals',
    recentTitle: 'Books you just viewed',
    recentDescription: 'Jump back into the latest three titles you opened.',
    recentItemLabel: 'Just viewed',
    recentCta: 'Continue reading',
    recentEmptyTitle: 'Nothing here yet',
    recentEmptyDescription:
      'Open a book detail page and your recent list will appear here.',
    recentEmptyCta: 'Browse books',
    wishlistTitle: 'Wishlist',
    wishlistDescription: 'Return to the books you already saved.',
    wishlistCta: 'Open wishlist',
  },
})

Object.assign(getMutableMessageSection(messages.vi, 'home', 'funDiscovery'), {
  giftFinderBadge: '4 bước tặng quà',
  giftFinderTitle: 'Tìm sách làm quà',
  giftFinderDescription:
    'Chọn người nhận, dịp tặng, ngân sách và phong cách món quà để nhận 6-8 gợi ý sách còn hàng.',
  giftFinderCta: 'Tìm sách phù hợp',
  readingChallengeBadge: 'Reading Challenge',
  readingChallengeTitle: 'Giữ nhịp đọc mỗi ngày',
  readingChallengeDescription:
    'Tạo thử thách cá nhân, tăng tiến độ bằng từng cuốn đã đọc và giữ mọi thứ ngay trên trình duyệt của bạn.',
  readingChallengeCta: 'Tạo challenge',
  readingChallengeProgress: '{completed}/{target} cuốn',
  readingChallengeActiveDescription:
    'Trạng thái {status}, đã đi được {progress}. Mở lại để cập nhật tiến độ hoặc chỉnh deadline.',
  readingChallengeActiveCta: 'Tiếp tục thử thách',
  readingChallengeActiveHint: 'Có thể +1, -1, chỉnh sửa hoặc xóa challenge.',
})

Object.assign(getMutableMessageSection(messages.en, 'home', 'funDiscovery'), {
  giftFinderBadge: '4 quick gift picks',
  giftFinderTitle: 'Find a book gift',
  giftFinderDescription:
    'Choose the recipient, occasion, budget, and gift style to reveal 6-8 in-stock ideas.',
  giftFinderCta: 'Find a suitable book',
  readingChallengeBadge: 'Reading Challenge',
  readingChallengeTitle: 'Keep your reading streak alive',
  readingChallengeDescription:
    'Create a personal challenge, move the bar book by book, and keep everything in your browser only.',
  readingChallengeCta: 'Create challenge',
  readingChallengeProgress: '{completed}/{target} books',
  readingChallengeActiveDescription:
    'Status: {status}. You are already {progress} in. Open it again to update progress or change the deadline.',
  readingChallengeActiveCta: 'Continue challenge',
  readingChallengeActiveHint:
    'You can add +1, subtract -1, edit, or delete the challenge.',
})

Object.assign(messages.vi.book as Record<string, unknown>, {
  giftFinder: {
    heroBadge: 'Gift Finder',
    title: 'Tìm sách làm quà không cần đoán mò',
    description:
      'Chọn người nhận, dịp tặng, ngân sách và phong cách món quà để tìm những cuốn sách phù hợp nhất.',
    progressTitle: 'Tiến độ chọn quà',
    progressReady: 'Sẵn sàng gợi ý',
    progressStep: 'Bước {current}/4',
    sidebarTitle: 'Phiên chọn quà',
    sidebarDescription:
      'Hoàn thành đủ 4 bước để nhận danh sách 6-8 cuốn sách còn hàng, kèm lý do vì sao hợp để tặng.',
    submit: 'Tìm sách phù hợp',
    submitLoading: 'Đang chọn sách...',
    reset: 'Làm lại',
    resultsTitle: 'Gợi ý quà tặng',
    resultsDescription:
      'Các gợi ý được sắp xếp theo người nhận, dịp tặng, phong cách món quà, ngân sách và đánh giá của độc giả.',
    resultsCount: '{count} cuốn đang hợp nhất',
    errorTitle: 'Chưa tìm được gợi ý quà',
    errorRetry: 'Thử lại',
    catalogEmptyTitle: 'Chưa có sách để gợi ý',
    catalogEmptyDescription:
      'Kho sách hiện chưa có lựa chọn phù hợp để làm quà. Bạn vui lòng quay lại sau.',
    emptyTitle: 'Chưa có cuốn nào thật sự khớp',
    emptyDescription:
      'Thử đổi một lựa chọn hoặc nới ngân sách để hệ thống mở rộng danh sách gợi ý.',
    summary: {
      recipient: 'Người nhận',
      occasion: 'Dịp tặng',
      budget: 'Ngân sách',
      tone: 'Phong cách món quà',
      pending: 'Chưa chọn',
    },
    steps: {
      recipientLabel: 'Người nhận',
      occasionLabel: 'Dịp tặng',
      budgetLabel: 'Ngân sách',
      toneLabel: 'Phong cách món quà',
      recipientTitle: 'Bạn đang tìm quà cho ai?',
      recipientDescription:
        'Lựa chọn này giúp tìm những nhóm sách phù hợp với người bạn muốn tặng.',
      occasionTitle: 'Bạn muốn tặng vào dịp nào?',
      occasionDescription:
        'Mỗi dịp tặng sẽ kéo danh sách theo cảm xúc và mức độ trang trọng khác nhau.',
      budgetTitle: 'Khoảng ngân sách bạn muốn chi là bao nhiêu?',
      budgetDescription:
        'Giá sách sẽ được ưu tiên nằm gần đúng khoảng ngân sách bạn đã chọn.',
      toneTitle: 'Bạn muốn món quà mang cảm giác gì?',
      toneDescription:
        'Hãy chọn cảm giác ấm áp, truyền cảm hứng, thực tế hoặc cuốn hút mà bạn muốn gửi gắm.',
    },
    recipients: {
      BEST_FRIEND: {
        label: 'Bạn thân',
        description:
          'Nhẹ nhàng, hợp gu, đủ cá tính để tạo cảm giác “mình hiểu bạn”.',
      },
      PARTNER: {
        label: 'Người thương',
        description:
          'Ưu tiên những cuốn giàu cảm xúc, tinh tế và có thể khiến buổi tặng quà đáng nhớ hơn.',
      },
      PARENT: {
        label: 'Ba mẹ / người thân lớn',
        description:
          'Nghiêng về giá trị sống, chữa lành, kỹ năng và những cuốn dễ đọc, dễ tặng.',
      },
      COLLEAGUE: {
        label: 'Đồng nghiệp',
        description:
          'Gọn gàng, lịch sự, thực tế và phù hợp cho môi trường công việc.',
      },
      YOUNG_READER: {
        label: 'Bạn đọc trẻ',
        description:
          'Năng lượng mới, dễ hứng thú, thiên về khám phá và học hỏi.',
      },
    },
    occasions: {
      BIRTHDAY: {
        label: 'Sinh nhật',
        description:
          'Một món quà dễ tạo bất ngờ và mang cảm giác được chọn riêng.',
      },
      THANK_YOU: {
        label: 'Cảm ơn',
        description:
          'Ưu tiên những cuốn mềm mại, tinh tế và có sắc thái tri ân.',
      },
      CELEBRATION: {
        label: 'Chúc mừng',
        description:
          'Phù hợp với cột mốc mới, thành tựu mới hoặc một bước chuyển tích cực.',
      },
      ENCOURAGEMENT: {
        label: 'Động viên',
        description:
          'Nghiêng về chữa lành, bồi đắp tinh thần và tạo thêm động lực.',
      },
    },
    budgets: {
      UNDER_150: {
        label: 'Dưới 150.000đ',
        description:
          'Nhóm quà gọn nhẹ, dễ tặng nhanh mà vẫn đủ tinh tế.',
      },
      FROM_150_TO_300: {
        label: '150.000đ - 300.000đ',
        description:
          'Khoảng ngân sách phù hợp với nhiều tựa sách được độc giả yêu thích.',
      },
      ABOVE_300: {
        label: 'Trên 300.000đ',
        description:
          'Ưu tiên những cuốn dày dặn hơn hoặc có cảm giác quà tặng đậm hơn.',
      },
    },
    tones: {
      COZY: {
        label: 'Ấm áp',
        description:
          'Một món quà mềm, gần gũi, đọc vào thấy nhẹ đầu và dễ chịu.',
      },
      INSPIRING: {
        label: 'Truyền cảm hứng',
        description:
          'Thiên về động lực, cảm hứng sống và cảm giác được tiếp thêm năng lượng.',
      },
      PRACTICAL: {
        label: 'Thực tế',
        description:
          'Tập trung vào giá trị ứng dụng, kỹ năng và điều có thể dùng được ngay.',
      },
      ESCAPIST: {
        label: 'Thoát vai thường ngày',
        description:
          'Cho người muốn được cuốn vào một hành trình, thế giới hoặc câu chuyện khác.',
      },
    },
    actions: {
      viewDetail: 'Xem chi tiết',
      addToWishlist: 'Lưu wishlist',
      removeFromWishlist: 'Bỏ khỏi wishlist',
      addToCart: 'Thêm vào giỏ',
    },
    reasons: {
      RECIPIENT: 'Hợp người nhận',
      OCCASION: 'Hợp dịp tặng',
      BUDGET: 'Đúng tầm ngân sách',
      TONE: 'Đúng tone quà',
      HIGH_RATING: 'Được đánh giá tốt',
      POPULAR_PICK: 'Được chọn nhiều',
      GIFTABLE_PICK: 'Dễ tặng, độ dày vừa đẹp',
    },
  },
})

Object.assign(messages.en.book as Record<string, unknown>, {
  giftFinder: {
    heroBadge: 'Gift Finder',
    title: 'Find a book gift without guessing',
    description:
      'Choose the recipient, occasion, budget, and gift style to find the most suitable books.',
    progressTitle: 'Gift progress',
    progressReady: 'Ready to recommend',
    progressStep: 'Step {current}/4',
    sidebarTitle: 'Gift session',
    sidebarDescription:
      'Finish all 4 steps to reveal 6-8 in-stock books, plus clear reasons why each one fits.',
    submit: 'Find matching books',
    submitLoading: 'Picking books...',
    reset: 'Start over',
    resultsTitle: 'Gift suggestions',
    resultsDescription:
      'Suggestions are ordered by recipient, occasion, gift style, budget, and reader reviews.',
    resultsCount: '{count} books fit best right now',
    errorTitle: 'Unable to build gift suggestions',
    errorRetry: 'Try again',
    catalogEmptyTitle: 'The gift shelf is empty',
    catalogEmptyDescription:
      'There are no suitable gift books available right now. Please check back later.',
    emptyTitle: 'Nothing is a strong match yet',
    emptyDescription:
      'Try changing one answer or widening the budget to open up more gift options.',
    summary: {
      recipient: 'Recipient',
      occasion: 'Occasion',
      budget: 'Budget',
      tone: 'Gift tone',
      pending: 'Not selected',
    },
    steps: {
      recipientLabel: 'Recipient',
      occasionLabel: 'Occasion',
      budgetLabel: 'Budget',
      toneLabel: 'Gift tone',
      recipientTitle: 'Who are you buying for?',
      recipientDescription:
        'Gift Finder will prefer categories and keywords that fit this person best.',
      occasionTitle: 'What is the gifting moment?',
      occasionDescription:
        'Different occasions shift the shelf toward different emotional tones and levels of formality.',
      budgetTitle: 'How much do you want to spend?',
      budgetDescription:
        'Book prices are pushed toward the budget band you choose here.',
      toneTitle: 'What feeling should the gift carry?',
      toneDescription:
        'The tone helps the ranking lean warm, inspiring, practical, or more immersive.',
    },
    recipients: {
      BEST_FRIEND: {
        label: 'Best friend',
        description:
          'Warm, personal, and a little specific so the gift feels chosen on purpose.',
      },
      PARTNER: {
        label: 'Partner',
        description:
          'More emotional, intimate, and polished picks that feel memorable to give.',
      },
      PARENT: {
        label: 'Parent or elder family member',
        description:
          'Leans toward life value, healing, skills, and books that are easy to give and easy to read.',
      },
      COLLEAGUE: {
        label: 'Colleague',
        description:
          'Clean, polite, practical options that still feel thoughtful in a work setting.',
      },
      YOUNG_READER: {
        label: 'Young reader',
        description:
          'Fresh energy, easier curiosity hooks, and more room for discovery.',
      },
    },
    occasions: {
      BIRTHDAY: {
        label: 'Birthday',
        description:
          'A pick that can feel personal, fun, and easy to wrap into a small surprise.',
      },
      THANK_YOU: {
        label: 'Thank you',
        description:
          'Softer and more graceful picks with a clear sense of appreciation.',
      },
      CELEBRATION: {
        label: 'Celebration',
        description:
          'Built for milestones, wins, and positive turning points.',
      },
      ENCOURAGEMENT: {
        label: 'Encouragement',
        description:
          'More healing, restorative, and motivating choices.',
      },
    },
    budgets: {
      UNDER_150: {
        label: 'Under 150,000 VND',
        description:
          'Lightweight gift territory that still feels thoughtful and polished.',
      },
      FROM_150_TO_300: {
        label: '150,000 VND to 300,000 VND',
        description:
          'A balanced range with many popular and well-loved titles.',
      },
      ABOVE_300: {
        label: 'Above 300,000 VND',
        description:
          'Favors larger, weightier, or more premium-feeling gift picks.',
      },
    },
    tones: {
      COZY: {
        label: 'Cozy',
        description:
          'Soft, close, and comforting energy that feels easy to give.',
      },
      INSPIRING: {
        label: 'Inspiring',
        description:
          'Motivation, life energy, and books that leave the reader uplifted.',
      },
      PRACTICAL: {
        label: 'Practical',
        description:
          'Focused on immediate use, skill-building, and direct value.',
      },
      ESCAPIST: {
        label: 'Escapist',
        description:
          'For readers who want to disappear into a story or another world for a while.',
      },
    },
    actions: {
      viewDetail: 'View detail',
      addToWishlist: 'Save to wishlist',
      removeFromWishlist: 'Remove from wishlist',
      addToCart: 'Add to cart',
    },
    reasons: {
      RECIPIENT: 'Fits the recipient',
      OCCASION: 'Fits the occasion',
      BUDGET: 'Matches the budget',
      TONE: 'Matches the tone',
      HIGH_RATING: 'Strong rating',
      POPULAR_PICK: 'Popular pick',
      GIFTABLE_PICK: 'Nice giftable length',
    },
  },
})

Object.assign(messages.vi.admin as Record<string, unknown>, {
  commandPalette: {
    openButton: 'Lệnh nhanh',
    shortcutBadge: 'Ctrl/Cmd K',
    title: 'Quick Command Palette',
    recentGroup: 'Trang gần đây',
    recentRouteSubtitle: 'Mở lại màn hình admin gần đây',
    description:
      'Tìm nhanh màn hình admin hoặc thao tác shell mà không cần rời bàn phím.',
    placeholder: 'Tìm màn hình, thao tác hoặc từ khóa...',
    emptyTitle: 'Không có lệnh phù hợp',
    emptyDescription:
      'Thử từ khóa khác hoặc mở rộng truy vấn để thấy thêm route và thao tác.',
    navigationGroup: 'Điều hướng',
    actionsGroup: 'Tác vụ nhanh',
    routeSubtitle: 'Mở màn hình admin',
    shortcutOpen: 'Ctrl/Cmd + K mở palette',
    shortcutMove: '↑ ↓ di chuyển',
    shortcutSelect: 'Enter thực thi',
    shortcutClose: 'Esc đóng',
    actions: {
      switchToDark: 'Chuyển sang giao diện tối',
      switchToLight: 'Chuyển sang giao diện sáng',
      openChat: 'Mở chat hỗ trợ',
      goStorefront: 'Về trang khách hàng',
      logout: 'Đăng xuất',
    },
    subtitles: {
      toggleTheme: 'Đổi theme của workspace admin hiện tại',
      openChat: 'Đi thẳng tới khu vực chat hỗ trợ',
      goStorefront: 'Rời khu vực quản trị và quay về trang khách hàng',
      logout: 'Đóng phiên hiện tại và quay về màn hình đăng nhập',
    },
  },
})

Object.assign(messages.en.admin as Record<string, unknown>, {
  commandPalette: {
    openButton: 'Quick commands',
    shortcutBadge: 'Ctrl/Cmd K',
    title: 'Quick Command Palette',
    recentGroup: 'Recent pages',
    recentRouteSubtitle: 'Reopen a recently visited admin screen',
    description:
      'Jump across admin routes or trigger shell actions without leaving the keyboard.',
    placeholder: 'Search routes, actions, or keywords...',
    emptyTitle: 'No command matches',
    emptyDescription:
      'Try another keyword or broaden the query to reveal more routes and actions.',
    navigationGroup: 'Navigation',
    actionsGroup: 'Quick actions',
    routeSubtitle: 'Open admin screen',
    shortcutOpen: 'Ctrl/Cmd + K opens the palette',
    shortcutMove: '↑ ↓ moves selection',
    shortcutSelect: 'Enter runs command',
    shortcutClose: 'Esc closes palette',
    actions: {
      switchToDark: 'Switch to dark theme',
      switchToLight: 'Switch to light theme',
      openChat: 'Open support chat',
      goStorefront: 'Go to customer site',
      logout: 'Log out',
    },
    subtitles: {
      toggleTheme: 'Change the current admin workspace theme',
      openChat: 'Jump straight to the support chat workspace',
      goStorefront: 'Leave admin and return to the customer site',
      logout: 'End the current session and go back to login',
    },
  },
})

Object.assign(messages.vi as Record<string, unknown>, {
  readingChallengePage: {
    badge: 'Thử thách đọc sách cá nhân',
    title: 'Reading Challenge của riêng bạn',
    description:
      'Đặt mục tiêu đọc, theo dõi tiến độ và tự thưởng cho mình khi hoàn thành. Tiến độ được lưu riêng trong trình duyệt này.',
    localStorageLabel: 'Nơi lưu tiến độ',
    localStorageValue: 'Trình duyệt này',
    scopeLabel: 'Cách hoạt động',
    scopeValue: 'Theo dõi riêng trên thiết bị này',
    goalLabel: 'Mục tiêu',
    goalValue: '{target} cuốn',
    goalEmptyValue: 'Chưa tạo thử thách',
    storageErrorTitle: 'Không thể lưu ổn định vào trình duyệt',
    storageErrorDescription:
      'Thao tác vẫn được phản ánh trên màn hình hiện tại, nhưng dữ liệu có thể không giữ lại sau khi reload.',
    storageErrorDismiss: 'Ẩn cảnh báo',
    progressOfGoal: 'Đã đọc {completed}/{target} cuốn',
    progressDescription:
      'Mỗi lần tăng hoặc giảm số sách đã đọc, thanh tiến độ sẽ cập nhật ngay và luôn bám sát mục tiêu hiện tại.',
    progressPercentLabel: 'Tiến độ hiện tại',
    progressBarLabel: 'Tiến độ thử thách',
    completedLabel: 'Đã đọc',
    booksLeftValue: 'Còn {count} cuốn nữa để chạm đích',
    targetLabelCard: 'Mục tiêu',
    startedAtValue: 'Bắt đầu từ {date}',
    deadlineLabel: 'Deadline',
    endsAtValue: 'Kết thúc vào {date}',
    daysRemaining: 'Còn {count} ngày',
    daysDueToday: 'Hết hạn hôm nay',
    daysOverdue: 'Quá hạn {count} ngày',
    completedTitle: 'Hoàn thành rồi!',
    completedDescription:
      'Bạn đã chạm mục tiêu đọc sách. Thử tăng target mới hoặc tạo thử thách kế tiếp để giữ đà đọc.',
    urgentTitle: 'Sắp hết hạn',
    urgentDescription:
      'Deadline đang ở rất gần nhưng challenge vẫn chưa hoàn tất. Đây là lúc tăng tốc cho vài trang cuối.',
    overdueTitle: 'Challenge đã quá hạn',
    overdueDescription:
      'Bạn vẫn có thể chỉnh deadline hoặc reset tiến độ để bắt đầu lại một vòng đọc mới.',
    incrementButton: '+1 sách đã đọc',
    decrementButton: '-1',
    resetProgressButton: 'Đặt lại tiến độ',
    deleteButton: 'Xóa challenge',
    deleteConfirm: 'Bạn có chắc muốn xóa Reading Challenge hiện tại không?',
    emptyTitle: 'Chưa có challenge nào được tạo',
    emptyDescription:
      'Bắt đầu bằng một mục tiêu nhỏ như 5 cuốn trong tháng này. Khi tạo xong, progress card, badge trạng thái và deadline sẽ hiện ngay ở đây.',
    emptyBrowseBooks: 'Khám phá sách',
    emptyCreateButton: 'Tạo challenge ngay',
    formCreateBadge: 'Bắt đầu mới',
    formEditBadge: 'Chỉnh sửa',
    formCreateTitle: 'Tạo thử thách đọc mới',
    formEditTitle: 'Chỉnh sửa challenge hiện tại',
    formCreateDescription:
      'Điền tên, mục tiêu và khoảng thời gian bạn muốn chinh phục.',
    formEditDescription:
      'Bạn có thể đổi tên, đổi target hoặc kéo dài deadline mà không cần rời khỏi trang.',
    localOnlyChip: 'Lưu trên trình duyệt',
    titleLabel: 'Tên thử thách',
    titlePlaceholder: 'Ví dụ: Đọc 5 cuốn tháng này',
    targetLabel: 'Số sách mục tiêu',
    targetPlaceholder: 'Nhập số cuốn muốn hoàn thành',
    durationLabel: 'Thời hạn',
    presetWeek: '1 tuần',
    presetWeekDescription: 'Phù hợp với một mini sprint đọc nhanh.',
    presetMonth: '1 tháng',
    presetMonthDescription: 'Đủ thoải mái để giữ nhịp đọc đều mỗi tuần.',
    presetYear: '1 năm',
    presetYearDescription: 'Dành cho mục tiêu dài hơi và bền bỉ.',
    presetCustom: 'Chọn ngày kết thúc',
    presetCustomDescription:
      'Tự đặt deadline chính xác theo lịch cá nhân của bạn.',
    customEndDateLabel: 'Ngày kết thúc',
    previewLabel: 'Deadline dự kiến',
    previewValue: 'Challenge sẽ kết thúc vào {date}',
    previewFallback: 'Chọn thêm deadline để xem trước.',
    createButton: 'Tạo challenge',
    updateButton: 'Lưu thay đổi',
    formHintTitle: 'Gợi ý nhỏ',
    formHintCreate:
      'Hãy đặt một target vừa sức để bạn dễ hoàn thành lần đầu, rồi nâng dần ở các challenge sau.',
    formHintEdit:
      'Nếu giảm target xuống thấp hơn completedBooks hiện tại, hệ thống sẽ tự clamp lại để dữ liệu luôn hợp lệ.',
    errors: {
      titleRequired: 'Hãy nhập tên challenge.',
      targetInvalid: 'Số sách mục tiêu phải lớn hơn 0.',
      endDateRequired: 'Hãy chọn ngày kết thúc cho challenge.',
      endDateInvalid: 'Ngày kết thúc chưa hợp lệ.',
      endDateBeforeStart:
        'Ngày kết thúc không thể sớm hơn ngày bắt đầu challenge.',
      unknown: 'Không thể lưu challenge lúc này. Vui lòng thử lại.',
    },
    status: {
      notStarted: 'Mới bắt đầu',
      inProgress: 'Đang tiến triển',
      nearCompletion: 'Sắp hoàn thành',
      completed: 'Hoàn thành',
      overdue: 'Quá hạn',
    },
  },
})

Object.assign(messages.en as Record<string, unknown>, {
  readingChallengePage: {
    badge: 'Personal reading challenge',
    title: 'Your own Reading Challenge',
    description:
      'Set a reading goal, track your progress, and celebrate the finish line. Progress is saved privately in this browser.',
    localStorageLabel: 'Progress saved in',
    localStorageValue: 'This browser',
    scopeLabel: 'How it works',
    scopeValue: 'Private to this device',
    goalLabel: 'Goal',
    goalValue: '{target} books',
    goalEmptyValue: 'No challenge yet',
    storageErrorTitle: 'Browser storage is not fully available',
    storageErrorDescription:
      'The current screen still updates, but the data may not survive after a reload.',
    storageErrorDismiss: 'Dismiss',
    progressOfGoal: '{completed}/{target} books read',
    progressDescription:
      'Each +1 or -1 updates the bar immediately. If you lower the target, the completed count is clamped automatically.',
    progressPercentLabel: 'Current progress',
    progressBarLabel: 'Challenge progress',
    completedLabel: 'Completed',
    booksLeftValue: '{count} books left to finish',
    targetLabelCard: 'Target',
    startedAtValue: 'Started on {date}',
    deadlineLabel: 'Deadline',
    endsAtValue: 'Ends on {date}',
    daysRemaining: '{count} days left',
    daysDueToday: 'Due today',
    daysOverdue: '{count} days overdue',
    completedTitle: 'You made it!',
    completedDescription:
      'You reached the goal. Raise the target or start another challenge to keep the rhythm going.',
    urgentTitle: 'Deadline is getting close',
    urgentDescription:
      'The challenge is still open and time is tight. A small push now can get you over the line.',
    overdueTitle: 'The challenge is overdue',
    overdueDescription:
      'You can still extend the deadline or reset the progress to begin a fresh round.',
    incrementButton: '+1 book read',
    decrementButton: '-1',
    resetProgressButton: 'Reset progress',
    deleteButton: 'Delete challenge',
    deleteConfirm: 'Are you sure you want to delete the current Reading Challenge?',
    emptyTitle: 'No challenge has been created yet',
    emptyDescription:
      'Start with a small goal such as 5 books this month. As soon as you create it, the progress card, status badge, and deadline appear here.',
    emptyBrowseBooks: 'Browse books',
    emptyCreateButton: 'Create a challenge',
    formCreateBadge: 'Fresh start',
    formEditBadge: 'Edit',
    formCreateTitle: 'Create a new reading challenge',
    formEditTitle: 'Edit the current challenge',
    formCreateDescription:
      'Give it a name, choose the target, and decide how long you want to chase it.',
    formEditDescription:
      'You can update the title, target, or deadline without leaving the page.',
    localOnlyChip: 'Saved in this browser',
    titleLabel: 'Challenge title',
    titlePlaceholder: 'Example: Read 5 books this month',
    targetLabel: 'Target number of books',
    targetPlaceholder: 'How many books do you want to finish?',
    durationLabel: 'Timeframe',
    presetWeek: '1 week',
    presetWeekDescription: 'Best for a quick reading sprint.',
    presetMonth: '1 month',
    presetMonthDescription: 'A balanced pace for steady weekly reading.',
    presetYear: '1 year',
    presetYearDescription: 'Great for a longer, slower commitment.',
    presetCustom: 'Pick an end date',
    presetCustomDescription: 'Choose the exact deadline that fits your calendar.',
    customEndDateLabel: 'End date',
    previewLabel: 'Expected deadline',
    previewValue: 'This challenge will end on {date}',
    previewFallback: 'Pick a deadline to preview it.',
    createButton: 'Create challenge',
    updateButton: 'Save changes',
    formHintTitle: 'Small tip',
    formHintCreate:
      'Start with an achievable target for your first run, then scale it up in later challenges.',
    formHintEdit:
      'If the new target is lower than the completed count, the app clamps the count automatically to stay valid.',
    errors: {
      titleRequired: 'Please enter a challenge title.',
      targetInvalid: 'The target number of books must be greater than 0.',
      endDateRequired: 'Please choose an end date for the challenge.',
      endDateInvalid: 'The end date is not valid.',
      endDateBeforeStart:
        'The end date cannot be earlier than the challenge start date.',
      unknown: 'Could not save the challenge right now. Please try again.',
    },
    status: {
      notStarted: 'Just started',
      inProgress: 'In progress',
      nearCompletion: 'Almost there',
      completed: 'Completed',
      overdue: 'Overdue',
    },
  },
})

Object.assign(messages.vi as Record<string, unknown>, {
  wishlist: {
    badge: 'Yêu thích',
    title: 'Sách yêu thích',
    description: 'Lưu lại những tựa sách bạn muốn quay lại xem hoặc mua sau.',
    count: '{count} sách trong wishlist',
    emptyTitle: 'Wishlist của bạn đang trống',
    emptyDescription:
      'Bấm trái tim ở book card hoặc trang chi tiết để lưu sách vào đây.',
    browseBooks: 'Khám phá sách',
    loginRequired: 'Vui lòng đăng nhập để dùng wishlist',
    fetchError: 'Không thể tải wishlist',
    updateError: 'Không thể cập nhật wishlist',
    added: 'Đã thêm "{title}" vào wishlist',
    removed: 'Đã xóa "{title}" khỏi wishlist',
  },
})

Object.assign(messages.en as Record<string, unknown>, {
  wishlist: {
    badge: 'Wishlist',
    title: 'My wishlist',
    description: 'Save books you want to revisit or purchase later.',
    count: '{count} books in your wishlist',
    emptyTitle: 'Your wishlist is empty',
    emptyDescription:
      'Tap the heart button on a book card or detail page to save books here.',
    browseBooks: 'Browse books',
    loginRequired: 'Please sign in to use wishlist',
    fetchError: 'Could not load wishlist',
    updateError: 'Could not update wishlist',
    added: 'Added "{title}" to wishlist',
    removed: 'Removed "{title}" from wishlist',
  },
})

Object.assign(messages.vi.book.card as Record<string, unknown>, {
  wishlistAria: 'Lưu "{title}" vào wishlist',
})

Object.assign(messages.en.book.card as Record<string, unknown>, {
  wishlistAria: 'Save "{title}" to wishlist',
})

Object.assign(messages.vi.book.detail as Record<string, unknown>, {
  addToWishlist: 'Thêm vào wishlist',
  removeFromWishlist: 'Bỏ khỏi wishlist',
  recentlyViewedTitle: 'Bạn vừa xem',
})

Object.assign(messages.en.book.detail as Record<string, unknown>, {
  addToWishlist: 'Add to wishlist',
  removeFromWishlist: 'Remove from wishlist',
  recentlyViewedTitle: 'Recently viewed',
})

Object.assign(messages.vi.cart as Record<string, unknown>, {
  bestCouponTitle: 'Mã giảm giá tốt nhất cho bạn',
  bestCouponDescription:
    'Hệ thống gợi ý mã giảm giá tối ưu cho những sản phẩm đang được chọn.',
  bestCouponRecommended: 'Gợi ý từ hệ thống',
  bestCouponApply: 'Áp dụng',
  bestCouponApplied: 'Đã áp dụng mã {code}',
  bestCouponAppliedBadge: 'Đã áp dụng',
  bestCouponAppliedButton: 'Đã áp dụng',
  bestCouponUnavailable: 'Chưa có mã giảm giá phù hợp cho lựa chọn hiện tại.',
  bestCouponDiscount: 'Giảm được {amount}',
  bestCouponEstimate: 'Tổng dự kiến sau giảm: {amount}',
  bestCouponLine: 'Giảm từ coupon',
})

Object.assign(messages.en.cart as Record<string, unknown>, {
  bestCouponTitle: 'Best coupon for you',
  bestCouponDescription:
    'The system suggests the best discount code for the items you selected.',
  bestCouponRecommended: 'Recommended by the system',
  bestCouponApply: 'Apply',
  bestCouponApplied: 'Applied code {code}',
  bestCouponAppliedBadge: 'Applied',
  bestCouponAppliedButton: 'Applied',
  bestCouponUnavailable: 'No suitable coupon is available for this selection.',
  bestCouponDiscount: 'You save {amount}',
  bestCouponEstimate: 'Estimated total after discount: {amount}',
  bestCouponLine: 'Coupon discount',
})

Object.assign(messages.vi.checkout as Record<string, unknown>, {
  bestCouponTitle: 'Gợi ý mã giảm giá',
  bestCouponDescription:
    'Chọn mã phù hợp bên dưới để tiết kiệm hơn cho đơn hàng này.',
  bestCouponRecommended: 'Gợi ý từ hệ thống',
  bestCouponApply: 'Áp dụng mã này',
  bestCouponApplied: 'Đã áp dụng',
  bestCouponUnavailable: 'Chưa có mã giảm giá phù hợp cho đơn hàng này.',
  bestCouponDiscount: 'Giảm được {amount}',
  bestCouponEstimate: 'Tổng dự kiến sau giảm: {amount}',
})

Object.assign(messages.en.checkout as Record<string, unknown>, {
  bestCouponTitle: 'Suggested coupon',
  bestCouponDescription:
    'Choose a suitable code below to save more on this order.',
  bestCouponRecommended: 'Recommended by the system',
  bestCouponApply: 'Apply this code',
  bestCouponApplied: 'Applied',
  bestCouponUnavailable: 'No suitable coupon is available for this order.',
  bestCouponDiscount: 'You save {amount}',
  bestCouponEstimate: 'Estimated total after discount: {amount}',
})

Object.assign(messages.vi.admin.sidebar as Record<string, string>, {
  auditLogs: 'Nhật ký hệ thống',
  reports: 'Báo cáo',
})

Object.assign(messages.en.admin.sidebar as Record<string, string>, {
  auditLogs: 'Audit logs',
  reports: 'Reports',
})

Object.assign(messages.vi.admin as Record<string, unknown>, {
  reportsPage: {
    title: 'Báo cáo',
    description:
      'Tải báo cáo CSV theo nhu cầu vận hành mà không làm quá tải trang tổng quan.',
    dashboardTitle: 'Báo cáo vận hành',
    dashboardDescription: 'Mở trung tâm báo cáo để chọn bộ lọc và tải CSV.',
    dashboardHint:
      'Đơn hàng, doanh thu, tồn kho thấp và review đều có bộ lọc riêng trong Trung tâm báo cáo.',
    openCenter: 'Mở trung tâm báo cáo',
  },
})

Object.assign(messages.en.admin as Record<string, unknown>, {
  reportsPage: {
    title: 'Reports',
    description:
      'Download operational CSV reports without crowding the main dashboard.',
    dashboardTitle: 'Operational reports',
    dashboardDescription: 'Open the report center to choose filters and download CSV files.',
    dashboardHint:
      'Orders, revenue, low stock, and reviews each have focused filters in the Report Center.',
    openCenter: 'Open report center',
  },
})

Object.assign(messages.vi.admin as Record<string, unknown>, {
  auditLogsPage: {
    title: 'Nhật ký hệ thống',
    description:
      'Theo dõi thao tác quan trọng của admin và staff, xem trạng thái trước/sau thay đổi và tra cứu nhanh theo hành động hoặc đối tượng.',
    total: '{count} bản ghi',
    actorSearch: 'Tìm theo người thao tác hoặc mô tả trong trang hiện tại',
    action: 'Hành động',
    targetType: 'Đối tượng',
    from: 'Từ ngày',
    to: 'Đến ngày',
    detailTitle: 'Chi tiết audit log',
    empty: 'Chưa có bản ghi phù hợp với bộ lọc hiện tại.',
    systemActor: 'Hệ thống',
    beforeValue: 'Dữ liệu trước thay đổi',
    afterValue: 'Dữ liệu sau thay đổi',
    noBeforeValue: 'Không có dữ liệu trước thay đổi.',
    noAfterValue: 'Không có dữ liệu sau thay đổi.',
    ipAddress: 'IP',
    userAgent: 'User agent',
    columns: {
      createdAt: 'Thời gian',
      actor: 'Người thao tác',
      action: 'Hành động',
      target: 'Đối tượng',
      description: 'Mô tả',
    },
  },
})

Object.assign(messages.en.admin as Record<string, unknown>, {
  auditLogsPage: {
    title: 'Audit logs',
    description:
      'Review important admin and staff actions, inspect before/after values, and filter by action or target type.',
    total: '{count} records',
    actorSearch: 'Search current page by actor or description',
    action: 'Action',
    targetType: 'Target type',
    from: 'From',
    to: 'To',
    detailTitle: 'Audit log details',
    empty: 'No audit logs match the current filters.',
    systemActor: 'System',
    beforeValue: 'Before value',
    afterValue: 'After value',
    noBeforeValue: 'No previous value.',
    noAfterValue: 'No next value.',
    ipAddress: 'IP',
    userAgent: 'User agent',
    columns: {
      createdAt: 'Created at',
      actor: 'Actor',
      action: 'Action',
      target: 'Target',
      description: 'Description',
    },
  },
})

Object.assign(messages.vi as Record<string, unknown>, {
  orderTimeline: {
    title: 'Tiến trình đơn hàng',
    description:
      'Theo dõi các mốc quan trọng của đơn hàng, thanh toán và giao hàng theo thứ tự thời gian.',
    empty: 'Đơn hàng này chưa có sự kiện timeline nào.',
    actorLabel: 'Người thực hiện',
    systemActor: 'Hệ thống',
  },
})

Object.assign(messages.en as Record<string, unknown>, {
  orderTimeline: {
    title: 'Order timeline',
    description:
      'Follow important order, payment, and delivery milestones in chronological order.',
    empty: 'This order does not have any timeline events yet.',
    actorLabel: 'Actor',
    systemActor: 'System',
  },
})

Object.assign(messages.vi as Record<string, unknown>, {
  returnRequests: {
    eyebrow: 'Đổi trả đơn hàng',
    title: 'Yêu cầu trả hàng / hoàn tiền',
    description:
      'Theo dõi các yêu cầu đổi trả của bạn, xem trạng thái xử lý và số tiền hoàn nội bộ nếu có.',
    sectionTitle: 'Trả hàng / hoàn tiền',
    sectionDescription:
      'Khi đơn đã giao thành công, bạn có thể gửi yêu cầu trả hàng hoặc hoàn tiền để admin xem xét thủ công.',
    createAction: 'Tạo yêu cầu',
    cancelAction: 'Hủy yêu cầu',
    latestRequestTitle: 'Yêu cầu gần nhất',
    emptyForOrder: 'Đơn hàng này chưa có yêu cầu trả hàng nào.',
    dialogTitle: 'Tạo yêu cầu trả hàng',
    dialogDescription:
      'Mô tả rõ vấn đề bạn gặp phải để admin có đủ thông tin xử lý nhanh hơn.',
    reasonLabel: 'Lý do yêu cầu',
    reasonPlaceholder:
      'Ví dụ: sách bị rách, giao sai sản phẩm, muốn hoàn một phần tiền...',
    submitAction: 'Gửi yêu cầu',
    filterLabel: 'Lọc theo trạng thái',
    allStatuses: 'Tất cả trạng thái',
    totalCount: '{count} yêu cầu',
    backToOrders: 'Quay lại đơn hàng',
    empty: 'Bạn chưa có yêu cầu trả hàng nào phù hợp với bộ lọc hiện tại.',
    viewOrder: 'Xem đơn hàng',
    createdAt: 'Tạo lúc',
    requestedAmount: 'Số tiền yêu cầu hoàn',
    approvedAmount: 'Số tiền được duyệt',
    orderAmount: 'Giá trị đơn hàng',
    receiverName: 'Người nhận',
    processedAt: 'Xử lý lúc',
    processedBy: 'Người xử lý',
    adminNote: 'Ghi chú từ admin',
    notProvided: 'Chưa cung cấp',
    notProcessed: 'Chưa xử lý',
    unknown: 'Không xác định',
    paymentSummary:
      'Thanh toán: {paymentMethod} · Trạng thái thanh toán: {paymentStatus}',
    createSuccess: 'Đã gửi yêu cầu trả hàng thành công.',
    cancelSuccess: 'Đã hủy yêu cầu trả hàng.',
    errors: {
      load: 'Không thể tải danh sách yêu cầu trả hàng',
      create: 'Không thể tạo yêu cầu trả hàng',
      cancel: 'Không thể hủy yêu cầu trả hàng',
    },
    status: {
      PENDING: 'Chờ xử lý',
      APPROVED: 'Đã duyệt',
      REJECTED: 'Đã từ chối',
      CANCELLED: 'Đã hủy',
    },
  },
})

Object.assign(messages.en as Record<string, unknown>, {
  returnRequests: {
    eyebrow: 'Order return requests',
    title: 'Return / refund requests',
    description:
      'Track your return requests, review processing status, and see any internally approved refund amount.',
    sectionTitle: 'Return / refund',
    sectionDescription:
      'Once an order is delivered, you can submit a return or refund request for manual admin review.',
    createAction: 'Create request',
    cancelAction: 'Cancel request',
    latestRequestTitle: 'Latest request',
    emptyForOrder: 'This order does not have a return request yet.',
    dialogTitle: 'Create return request',
    dialogDescription:
      'Describe the issue clearly so the admin has enough context to review it faster.',
    reasonLabel: 'Reason',
    reasonPlaceholder:
      'Example: damaged book, wrong item delivered, requesting a partial refund...',
    submitAction: 'Submit request',
    filterLabel: 'Filter by status',
    allStatuses: 'All statuses',
    totalCount: '{count} requests',
    backToOrders: 'Back to orders',
    empty: 'No return requests match the current filter.',
    viewOrder: 'View order',
    createdAt: 'Created at',
    requestedAmount: 'Requested refund',
    approvedAmount: 'Approved refund',
    orderAmount: 'Order amount',
    receiverName: 'Receiver',
    processedAt: 'Processed at',
    processedBy: 'Processed by',
    adminNote: 'Admin note',
    notProvided: 'Not provided',
    notProcessed: 'Not processed',
    unknown: 'Unknown',
    paymentSummary:
      'Payment: {paymentMethod} · Payment status: {paymentStatus}',
    createSuccess: 'Return request submitted successfully.',
    cancelSuccess: 'Return request cancelled successfully.',
    errors: {
      load: 'Could not load return requests',
      create: 'Could not create the return request',
      cancel: 'Could not cancel the return request',
    },
    status: {
      PENDING: 'Pending',
      APPROVED: 'Approved',
      REJECTED: 'Rejected',
      CANCELLED: 'Cancelled',
    },
  },
})

Object.assign(messages.vi.admin.sidebar as Record<string, string>, {
  returnRequests: 'Yêu cầu trả hàng',
})

Object.assign(messages.en.admin.sidebar as Record<string, string>, {
  returnRequests: 'Return requests',
})

Object.assign(messages.vi.admin as Record<string, unknown>, {
  returnRequestsPage: {
    title: 'Yêu cầu trả hàng / hoàn tiền',
    total: '{count} yêu cầu',
    searchPlaceholder: 'Tìm theo mã đơn, tên khách, email hoặc người nhận',
    filterLabel: 'Trạng thái',
    allStatuses: 'Tất cả trạng thái',
    empty: 'Chưa có yêu cầu trả hàng nào phù hợp.',
    detailTitle: 'Chi tiết yêu cầu',
    reasonTitle: 'Lý do khách hàng gửi',
    adminNoteTitle: 'Ghi chú xử lý',
    approveAction: 'Duyệt yêu cầu',
    rejectAction: 'Từ chối',
    approveDialogTitle: 'Duyệt yêu cầu trả hàng',
    rejectDialogTitle: 'Từ chối yêu cầu trả hàng',
    restockLabel:
      'Hoàn lại tồn kho cho các sách vật lý trong đơn sau khi duyệt yêu cầu',
    approveSuccess: 'Đã duyệt yêu cầu trả hàng.',
    approveError: 'Không thể duyệt yêu cầu trả hàng',
    rejectSuccess: 'Đã từ chối yêu cầu trả hàng.',
    rejectError: 'Không thể từ chối yêu cầu trả hàng',
    loadError: 'Không thể tải danh sách yêu cầu trả hàng',
    notProvided: 'Chưa cung cấp',
    notProcessed: 'Chưa xử lý',
    columns: {
      orderCode: 'Mã đơn',
      customer: 'Khách hàng',
      requestedAmount: 'Tiền yêu cầu',
      status: 'Trạng thái',
      createdAt: 'Ngày tạo',
      actions: 'Thao tác',
    },
    fields: {
      customer: 'Khách hàng',
      email: 'Email',
      orderAmount: 'Giá trị đơn',
      createdAt: 'Tạo lúc',
      requestedAmount: 'Tiền yêu cầu',
      approvedAmount: 'Tiền duyệt',
      processedBy: 'Người xử lý',
      processedAt: 'Xử lý lúc',
      adminNote: 'Ghi chú admin',
    },
  },
})

Object.assign(messages.en.admin as Record<string, unknown>, {
  returnRequestsPage: {
    title: 'Return / refund requests',
    total: '{count} requests',
    searchPlaceholder: 'Search by order code, customer, email, or receiver',
    filterLabel: 'Status',
    allStatuses: 'All statuses',
    empty: 'No return requests match the current filters.',
    detailTitle: 'Request details',
    reasonTitle: 'Customer reason',
    adminNoteTitle: 'Processing note',
    approveAction: 'Approve request',
    rejectAction: 'Reject request',
    approveDialogTitle: 'Approve return request',
    rejectDialogTitle: 'Reject return request',
    restockLabel:
      'Restock physical books from this order after approving the request',
    approveSuccess: 'Return request approved successfully.',
    approveError: 'Could not approve the return request',
    rejectSuccess: 'Return request rejected successfully.',
    rejectError: 'Could not reject the return request',
    loadError: 'Could not load return requests',
    notProvided: 'Not provided',
    notProcessed: 'Not processed',
    columns: {
      orderCode: 'Order code',
      customer: 'Customer',
      requestedAmount: 'Requested amount',
      status: 'Status',
      createdAt: 'Created at',
      actions: 'Actions',
    },
    fields: {
      customer: 'Customer',
      email: 'Email',
      orderAmount: 'Order amount',
      createdAt: 'Created at',
      requestedAmount: 'Requested amount',
      approvedAmount: 'Approved amount',
      processedBy: 'Processed by',
      processedAt: 'Processed at',
      adminNote: 'Admin note',
    },
  },
})

Object.assign(messages.vi as Record<string, unknown>, {
  shelves: {
    badge: 'Kệ sách',
    title: 'Kệ sách của bạn',
    description:
      'Tạo nhiều kệ sách riêng để lưu danh sách đọc, sách muốn mua sau, hoặc những tựa sách muốn gợi ý lại nhanh.',
    hint: 'Mỗi kệ sách giúp bạn gom sách theo đúng mục đích đọc.',
    countLabel: '{count} kệ sách đang lưu',
    addBookBannerTitle: 'Đang chọn kệ sách cho cuốn này',
    addBookBannerDescription:
      'Chọn một kệ sách có sẵn hoặc tạo kệ mới để lưu sách ngay.',
    browseMoreBooks: 'Khám phá thêm sách',
    createPlaceholder: 'Nhập tên kệ sách mới',
    createAction: 'Tạo kệ sách',
    createAndSave: 'Tạo kệ và lưu sách',
    createSuccess: 'Đã tạo kệ "{name}"',
    createError: 'Không thể tạo kệ sách',
    renameAction: 'Đổi tên',
    renameSuccess: 'Đã cập nhật kệ "{name}"',
    renameError: 'Không thể cập nhật tên kệ sách',
    deleteConfirm: 'Bạn có chắc muốn xóa kệ "{name}" không?',
    deleteSuccess: 'Đã xóa kệ "{name}"',
    deleteError: 'Không thể xóa kệ sách',
    openAction: 'Mở kệ sách',
    saveSelectedBook: 'Lưu sách vào kệ này',
    addToShelfError: 'Không thể thêm sách vào kệ',
    addedToShelfSuccess: 'Đã lưu sách vào kệ "{name}"',
    emptyTitle: 'Bạn chưa có kệ sách nào',
    emptyDescription:
      'Tạo kệ đầu tiên để bắt đầu lưu những tựa sách bạn muốn quay lại.',
    emptyWithBookDescription:
      'Tạo kệ đầu tiên để lưu ngay cuốn sách bạn vừa chọn.',
    cardMeta: '{count} sách • Cập nhật {date}',
    booksCountBadge: '{count} sách',
    detailBadge: 'Chi tiết kệ sách',
    detailMeta: '{count} sách • Cập nhật {date}',
    emptyShelfTitle: 'Kệ sách này đang trống',
    emptyShelfDescription:
      'Thêm sách từ book card, trang chi tiết, hoặc tiếp tục khám phá catalog.',
    positionLabel: 'Vị trí #{position}',
    inStock: 'Còn {count} cuốn',
    outOfStock: 'Tạm hết hàng',
    openBookDetail: 'Xem chi tiết sách',
    removeBookAction: 'Bỏ khỏi kệ',
    removeBookSuccess: 'Đã bỏ sách khỏi kệ',
    removeBookError: 'Không thể bỏ sách khỏi kệ',
    reorderError: 'Không thể sắp xếp lại kệ sách',
    backToShelves: 'Quay lại danh sách kệ',
    loadError: 'Không thể tải dữ liệu kệ sách',
    loginRequired: 'Vui lòng đăng nhập để dùng kệ sách',
    addToShelfAction: 'Thêm vào kệ sách',
    addToShelfAria: 'Thêm "{title}" vào kệ sách',
    profileShortcut: 'Kệ sách của tôi',
  },
})

Object.assign(messages.en as Record<string, unknown>, {
  shelves: {
    badge: 'Shelves',
    title: 'Your shelves',
    description:
      'Create multiple shelves for reading lists, future purchases, and quick personal collections you want to revisit.',
    hint: 'Each shelf lets you group books around one clear intent.',
    countLabel: '{count} saved shelves',
    addBookBannerTitle: 'Choose a shelf for this book',
    addBookBannerDescription:
      'Pick an existing shelf or create a new one to save the selected book immediately.',
    browseMoreBooks: 'Browse more books',
    createPlaceholder: 'Enter a shelf name',
    createAction: 'Create shelf',
    createAndSave: 'Create shelf and save book',
    createSuccess: 'Created shelf "{name}"',
    createError: 'Could not create the shelf',
    renameAction: 'Rename shelf',
    renameSuccess: 'Updated shelf "{name}"',
    renameError: 'Could not rename the shelf',
    deleteConfirm: 'Delete the shelf "{name}"?',
    deleteSuccess: 'Deleted shelf "{name}"',
    deleteError: 'Could not delete the shelf',
    openAction: 'Open shelf',
    saveSelectedBook: 'Save selected book here',
    addToShelfError: 'Could not add the book to this shelf',
    addedToShelfSuccess: 'Saved the book to "{name}"',
    emptyTitle: 'You do not have any shelves yet',
    emptyDescription:
      'Create your first shelf to start saving books you want to revisit.',
    emptyWithBookDescription:
      'Create your first shelf to save the selected book right away.',
    cardMeta: '{count} books • Updated {date}',
    booksCountBadge: '{count} books',
    detailBadge: 'Shelf detail',
    detailMeta: '{count} books • Updated {date}',
    emptyShelfTitle: 'This shelf is empty',
    emptyShelfDescription:
      'Add books from book cards, the detail page, or keep browsing the catalog.',
    positionLabel: 'Position #{position}',
    inStock: '{count} copies left',
    outOfStock: 'Out of stock',
    openBookDetail: 'Open book detail',
    removeBookAction: 'Remove from shelf',
    removeBookSuccess: 'Removed the book from the shelf',
    removeBookError: 'Could not remove the book from the shelf',
    reorderError: 'Could not reorder the shelf',
    backToShelves: 'Back to shelves',
    loadError: 'Could not load the shelf',
    loginRequired: 'Please sign in to use shelves',
    addToShelfAction: 'Add to shelf',
    addToShelfAria: 'Add "{title}" to a shelf',
    profileShortcut: 'My shelves',
  },
})

Object.assign(messages.vi as Record<string, unknown>, {
  readingJournal: {
    badge: 'Reading Journal',
    title: 'Nhật ký đọc của bạn',
    description:
      'Ghi lại từng buổi đọc theo ngày, lưu vài dòng cảm nhận, và giữ streak đọc bằng những check-in ngắn gọn nhưng bền vững.',
    backToProfile: 'Quay lại hồ sơ',
    openLibrary: 'Mở thư viện số',
    booksLoadError: 'Không thể tải danh sách sách cho journal',
    loadError: 'Không thể tải nhật ký đọc',
    streakLoadError: 'Không thể tải streak đọc',
    createSuccess: 'Đã lưu entry mới vào nhật ký đọc',
    updateSuccess: 'Đã cập nhật entry nhật ký đọc',
    deleteSuccess: 'Đã xóa entry nhật ký đọc',
    checkInSuccess: 'Đã check-in reading streak hôm nay',
    createError: 'Không thể lưu entry nhật ký đọc',
    updateError: 'Không thể cập nhật entry nhật ký đọc',
    deleteError: 'Không thể xóa entry nhật ký đọc',
    checkInError: 'Không thể check-in hôm nay',
    deleteConfirm: 'Bạn có chắc muốn xóa entry này không?',
    profileShortcut: 'Nhật ký đọc của tôi',
    openFromBookDetail: 'Viết nhật ký',
    openFromLibraryDetail: 'Ghi vào journal',
    loginRequired: 'Vui lòng đăng nhập để dùng reading journal',
    stats: {
      entries: 'Tổng entry',
      entriesHint: 'Tất cả ghi chú đã lưu trong journal.',
      current: 'Current streak',
      currentHint: 'Số ngày đang giữ nhịp đọc hiện tại.',
      longest: 'Longest streak',
      longestHint: 'Chuỗi ngày dài nhất bạn từng giữ được.',
    },
    streak: {
      title: 'Book Streak / Daily Check-in',
      description: 'Journal là nguồn dữ liệu gốc cho streak của bạn.',
      current: 'Hiện tại',
      longest: 'Dài nhất',
      status: 'Trạng thái hôm nay',
      checkedIn: 'Đã check-in',
      notCheckedIn: 'Chưa check-in',
      lastActivity: 'Ngày hoạt động gần nhất',
      noActivity: 'Chưa có hoạt động nào',
    },
    composer: {
      title: 'Tạo entry mới',
      description: 'Chọn sách, ngày đọc, rồi lưu vài con số và ghi chú ngắn.',
      newBadge: 'New',
      editTitle: 'Chỉnh sửa entry',
      editDescription: 'Bạn đang cập nhật một entry đã lưu trước đó.',
      editBadge: 'Edit',
    },
    fields: {
      book: 'Sách',
      entryDate: 'Ngày đọc',
      currentPage: 'Trang hiện tại',
      progressPercent: 'Tiến độ (%)',
      note: 'Ghi chú',
    },
    placeholders: {
      book: 'Chọn một cuốn sách',
      currentPage: 'Ví dụ 42',
      progressPercent: 'Ví dụ 35',
      note: 'Hôm nay bạn đọc tới đâu, thấy gì đáng nhớ, hoặc muốn quay lại điều gì?',
    },
    actions: {
      saveEntry: 'Lưu entry',
      updateEntry: 'Cập nhật entry',
      checkInToday: 'Check-in hôm nay',
      openBook: 'Mở trang sách',
      editEntry: 'Sửa entry',
      deleteEntry: 'Xóa entry',
    },
    filters: {
      title: 'Bộ lọc timeline',
      description: 'Lọc lại journal theo sách hoặc theo khoảng ngày.',
      reset: 'Reset',
      book: 'Theo sách',
      allBooks: 'Tất cả sách',
      from: 'Từ ngày',
      to: 'Đến ngày',
    },
    timeline: {
      title: 'Timeline journal',
      description: 'Mỗi entry được nhóm theo ngày để bạn đọc lại hành trình rõ hơn.',
      countLabel: '{count} entry trong journal',
      dayCount: '{count} entry trong ngày này',
      noteFallback: 'Không có ghi chú chi tiết cho entry này.',
      currentPage: 'Trang',
      progressPercent: 'Tiến độ',
      updatedAt: 'Cập nhật',
    },
    pagination: {
      summary: 'Trang {page}/{totalPages}',
    },
    validation: {
      bookRequired: 'Vui lòng chọn một cuốn sách',
      entryDateRequired: 'Vui lòng chọn ngày đọc',
      currentPage: 'Trang hiện tại phải là số nguyên không âm',
      progressPercent: 'Tiến độ phải nằm trong khoảng 0 đến 100',
    },
    emptyTitle: 'Journal của bạn vẫn còn trống',
    emptyDescription:
      'Tạo entry đầu tiên hoặc check-in hôm nay để bắt đầu gom lại hành trình đọc của bạn.',
  },
})

Object.assign(messages.en as Record<string, unknown>, {
  readingJournal: {
    badge: 'Reading Journal',
    title: 'Your reading journal',
    description:
      'Capture each reading session by date, keep short notes, and maintain your streak with lightweight daily check-ins.',
    backToProfile: 'Back to profile',
    openLibrary: 'Open digital library',
    booksLoadError: 'Could not load the journal book list',
    loadError: 'Could not load the reading journal',
    streakLoadError: 'Could not load the reading streak',
    createSuccess: 'Saved a new journal entry',
    updateSuccess: 'Updated the journal entry',
    deleteSuccess: 'Deleted the journal entry',
    checkInSuccess: 'Checked in for today',
    createError: 'Could not save the journal entry',
    updateError: 'Could not update the journal entry',
    deleteError: 'Could not delete the journal entry',
    checkInError: 'Could not complete today check-in',
    deleteConfirm: 'Delete this journal entry?',
    profileShortcut: 'My reading journal',
    openFromBookDetail: 'Write journal entry',
    openFromLibraryDetail: 'Add to journal',
    loginRequired: 'Please sign in to use the reading journal',
    stats: {
      entries: 'Entries',
      entriesHint: 'Every saved note across your journal.',
      current: 'Current streak',
      currentHint: 'How many consecutive active days you are holding now.',
      longest: 'Longest streak',
      longestHint: 'The best reading streak you have reached so far.',
    },
    streak: {
      title: 'Book Streak / Daily Check-in',
      description: 'The journal is the source of truth for your streak.',
      current: 'Current',
      longest: 'Longest',
      status: 'Today status',
      checkedIn: 'Checked in',
      notCheckedIn: 'Not checked in',
      lastActivity: 'Last activity date',
      noActivity: 'No activity yet',
    },
    composer: {
      title: 'Create a new entry',
      description: 'Pick a book, choose the date, and save a short note with progress.',
      newBadge: 'New',
      editTitle: 'Edit entry',
      editDescription: 'You are updating an entry that is already in the journal.',
      editBadge: 'Edit',
    },
    fields: {
      book: 'Book',
      entryDate: 'Entry date',
      currentPage: 'Current page',
      progressPercent: 'Progress (%)',
      note: 'Note',
    },
    placeholders: {
      book: 'Choose a book',
      currentPage: 'For example 42',
      progressPercent: 'For example 35',
      note: 'What did you read today, what stood out, or what do you want to revisit later?',
    },
    actions: {
      saveEntry: 'Save entry',
      updateEntry: 'Update entry',
      checkInToday: 'Check in today',
      openBook: 'Open book page',
      editEntry: 'Edit entry',
      deleteEntry: 'Delete entry',
    },
    filters: {
      title: 'Timeline filters',
      description: 'Refine the journal by book or by date range.',
      reset: 'Reset',
      book: 'By book',
      allBooks: 'All books',
      from: 'From',
      to: 'To',
    },
    timeline: {
      title: 'Journal timeline',
      description: 'Entries are grouped by day so the reading journey stays easy to scan.',
      countLabel: '{count} entries in the journal',
      dayCount: '{count} entries on this day',
      noteFallback: 'No detailed note was saved for this entry.',
      currentPage: 'Page',
      progressPercent: 'Progress',
      updatedAt: 'Updated',
    },
    pagination: {
      summary: 'Page {page}/{totalPages}',
    },
    validation: {
      bookRequired: 'Please choose a book',
      entryDateRequired: 'Please choose an entry date',
      currentPage: 'Current page must be a non-negative integer',
      progressPercent: 'Progress must stay between 0 and 100',
    },
    emptyTitle: 'Your journal is still empty',
    emptyDescription:
      'Create the first entry or check in today to start building a readable record of your sessions.',
  },
})

Object.assign(messages.vi.book.card as Record<string, unknown>, {
  addToShelfAria: 'Thêm "{title}" vào kệ sách',
})

Object.assign(messages.en.book.card as Record<string, unknown>, {
  addToShelfAria: 'Add "{title}" to a shelf',
})

Object.assign(messages.vi.book.detail as Record<string, unknown>, {
  addToShelf: 'Thêm vào kệ sách',
})

Object.assign(messages.en.book.detail as Record<string, unknown>, {
  addToShelf: 'Add to shelf',
})

Object.assign(messages.vi.common as Record<string, unknown>, {
  retry: 'Th\u1eed l\u1ea1i',
})

Object.assign(messages.en.common as Record<string, unknown>, {
  retry: 'Retry',
})

Object.assign(messages.vi.admin.sidebar as Record<string, unknown>, {
  mobileOpen: 'M\u1edf menu qu\u1ea3n tr\u1ecb',
  mobileClose: '\u0110\u00f3ng menu qu\u1ea3n tr\u1ecb',
  groups: {
    overview: 'T\u1ed5ng quan',
    catalog: 'S\u00e1ch v\u00e0 danh m\u1ee5c',
    operations: 'V\u1eadn h\u00e0nh',
    engagement: 'T\u01b0\u01a1ng t\u00e1c',
    access: 'Ng\u01b0\u1eddi d\u00f9ng v\u00e0 ph\u00e2n quy\u1ec1n',
    system: 'Thi\u1ebft l\u1eadp',
  },
})

Object.assign(messages.en.admin.sidebar as Record<string, unknown>, {
  mobileOpen: 'Open admin menu',
  mobileClose: 'Close admin menu',
  groups: {
    overview: 'Overview',
    catalog: 'Catalog',
    operations: 'Operations',
    engagement: 'Engagement',
    access: 'Users and access',
    system: 'Settings',
  },
})

Object.assign(messages.vi.header as Record<string, unknown>, {
  mobileMenuOpen: 'Mở menu điều hướng',
  mobileMenuClose: 'Đóng menu điều hướng',
  appearanceLabel: 'Giao diện',
  primaryNavigation: 'Điều hướng chính',
})

Object.assign(messages.en.header as Record<string, unknown>, {
  mobileMenuOpen: 'Open navigation menu',
  mobileMenuClose: 'Close navigation menu',
  appearanceLabel: 'Appearance',
  primaryNavigation: 'Primary navigation',
})

Object.assign(messages.vi.footer as Record<string, unknown>, {
  facebookAria: 'Theo dõi SáchVui trên Facebook',
  instagramAria: 'Theo dõi SáchVui trên Instagram',
  zaloAria: 'Liên hệ SáchVui qua Zalo',
})

Object.assign(messages.en.footer as Record<string, unknown>, {
  facebookAria: 'Follow SachVui on Facebook',
  instagramAria: 'Follow SachVui on Instagram',
  zaloAria: 'Contact SachVui on Zalo',
})

Object.assign(messages.vi.book.listing as Record<string, unknown>, {
  filterToggle: 'Bộ lọc sách',
  filterOpen: 'Mở bộ lọc sách',
  filterClose: 'Đóng bộ lọc sách',
  categoryFilterAria: 'Lọc sách theo thể loại',
  sortAria: 'Sắp xếp danh sách sách',
  searchAria: 'Tìm trong danh sách sách',
  categorySearchAria: 'Tìm thể loại sách',
})

Object.assign(messages.en.book.listing as Record<string, unknown>, {
  filterToggle: 'Book filters',
  filterOpen: 'Open book filters',
  filterClose: 'Close book filters',
  categoryFilterAria: 'Filter books by category',
  sortAria: 'Sort book list',
  searchAria: 'Search within books',
  categorySearchAria: 'Search book categories',
})

Object.assign(getMutableMessageSection(messages.vi, 'ebookCatalog'), {
  filterToggle: 'Bộ lọc sách điện tử',
  filterOpen: 'Mở bộ lọc sách điện tử',
  filterClose: 'Đóng bộ lọc sách điện tử',
  categoryFilterAria: 'Lọc sách điện tử theo thể loại',
  sortAria: 'Sắp xếp danh sách sách điện tử',
  searchAria: 'Tìm trong danh sách sách điện tử',
  categorySearchAria: 'Tìm thể loại sách điện tử',
})

Object.assign(getMutableMessageSection(messages.en, 'ebookCatalog'), {
  filterToggle: 'Ebook filters',
  filterOpen: 'Open ebook filters',
  filterClose: 'Close ebook filters',
  categoryFilterAria: 'Filter ebooks by category',
  sortAria: 'Sort ebook list',
  searchAria: 'Search within ebooks',
  categorySearchAria: 'Search ebook categories',
})

Object.assign(messages.vi.library as Record<string, unknown>, {
  accessStatus: {
    ACTIVE: 'Đang sử dụng',
    EXPIRED: 'Đã hết hạn',
    REVOKED: 'Đã thu hồi',
  },
  accessType: {
    PURCHASED: 'Đã mua',
    BORROWED: 'Đang mượn',
    SUBSCRIPTION: 'Theo gói thành viên',
  },
  format: {
    PDF: 'PDF',
    EPUB: 'EPUB',
    AUDIO: 'Sách nói',
  },
})

Object.assign(messages.en.library as Record<string, unknown>, {
  accessStatus: {
    ACTIVE: 'Active',
    EXPIRED: 'Expired',
    REVOKED: 'Revoked',
  },
  accessType: {
    PURCHASED: 'Purchased',
    BORROWED: 'Borrowed',
    SUBSCRIPTION: 'Subscription',
  },
  format: {
    PDF: 'PDF',
    EPUB: 'EPUB',
    AUDIO: 'Audiobook',
  },
})

Object.assign(messages.vi.library.page as Record<string, unknown>, {
  searchAria: 'Tìm trong thư viện số',
  formatFilterAria: 'Lọc thư viện theo định dạng',
  statusFilterAria: 'Lọc thư viện theo trạng thái truy cập',
})

Object.assign(messages.en.library.page as Record<string, unknown>, {
  searchAria: 'Search the digital library',
  formatFilterAria: 'Filter library by format',
  statusFilterAria: 'Filter library by access status',
})

Object.assign(messages.vi.auth.profile as Record<string, unknown>, {
  securityTitle: 'Bảo mật và thiết bị',
  securityDescription:
    'Quản lý các thiết bị đang có quyền truy cập tài khoản của bạn.',
  logoutAllDevices: 'Đăng xuất khỏi mọi thiết bị',
  logoutAllConfirm: 'Đăng xuất khỏi tất cả thiết bị?',
  sessionsLoading: 'Đang tải các phiên đăng nhập…',
  sessionsErrorTitle: 'Không tải được các phiên đăng nhập',
  sessionsEmptyTitle: 'Không có phiên đang hoạt động',
  sessionsEmptyDescription:
    'Các phiên mới sẽ xuất hiện ở đây sau khi bạn đăng nhập.',
  unknownDevice: 'Thiết bị không xác định',
  currentDevice: 'Thiết bị này',
  unknownIp: 'IP không rõ',
  lastUsedAt: 'Dùng gần nhất {date}',
  revokeSessionConfirm: 'Thu hồi phiên trên thiết bị này?',
  revokeSession: 'Thu hồi',
})

Object.assign(messages.en.auth.profile as Record<string, unknown>, {
  securityTitle: 'Security and devices',
  securityDescription: 'Manage devices that can access your account.',
  logoutAllDevices: 'Sign out on all devices',
  logoutAllConfirm: 'Sign out on every device?',
  sessionsLoading: 'Loading sign-in sessions…',
  sessionsErrorTitle: 'Unable to load sign-in sessions',
  sessionsEmptyTitle: 'No active sessions',
  sessionsEmptyDescription:
    'New sessions will appear here after you sign in.',
  unknownDevice: 'Unknown device',
  currentDevice: 'This device',
  unknownIp: 'Unknown IP',
  lastUsedAt: 'Last used {date}',
  revokeSessionConfirm: 'Revoke this device session?',
  revokeSession: 'Revoke',
})

Object.assign(messages.vi.orderDetail as Record<string, unknown>, {
  cancelTitle: 'Hủy đơn hàng',
  cancelDescription:
    'Đơn đang chờ thanh toán. Số lượng tồn và ưu đãi sẽ được hoàn lại sau khi hủy.',
  cancelAction: 'Hủy đơn',
  cancelDialogTitle: 'Xác nhận hủy đơn',
  cancelDialogDescription:
    'Vui lòng cho biết lý do. Hành động này không thể khôi phục đơn hàng.',
  cancelReasonLabel: 'Lý do hủy đơn',
  cancelReasonPlaceholder: 'Ví dụ: Tôi không còn nhu cầu mua sách',
  cancelReasonCount: '{count}/500 ký tự',
  cancelBack: 'Quay lại',
  cancelling: 'Đang hủy…',
  cancelConfirm: 'Xác nhận hủy đơn',
})

Object.assign(messages.en.orderDetail as Record<string, unknown>, {
  cancelTitle: 'Cancel order',
  cancelDescription:
    'This order is awaiting payment. Stock and promotions will be restored after cancellation.',
  cancelAction: 'Cancel order',
  cancelDialogTitle: 'Confirm order cancellation',
  cancelDialogDescription:
    'Please tell us why. This action cannot restore the order afterward.',
  cancelReasonLabel: 'Cancellation reason',
  cancelReasonPlaceholder: 'For example: I no longer need these books',
  cancelReasonCount: '{count}/500 characters',
  cancelBack: 'Go back',
  cancelling: 'Cancelling…',
  cancelConfirm: 'Confirm cancellation',
})

Object.assign(
  messages.vi.orderConfirmationBankTransfer as Record<string, unknown>,
  {
    remainingTime: 'Thời gian thanh toán còn lại: {time}',
    bankFallback: 'Thông tin ngân hàng chưa được cập nhật',
    accountNumberFallback: 'Số tài khoản chưa được cập nhật',
    accountNameFallback: 'Tên tài khoản chưa được cập nhật',
  },
)

Object.assign(
  messages.en.orderConfirmationBankTransfer as Record<string, unknown>,
  {
    remainingTime: 'Payment time remaining: {time}',
    bankFallback: 'Bank information is not available yet',
    accountNumberFallback: 'Account number is not available yet',
    accountNameFallback: 'Account name is not available yet',
  },
)

Object.assign(messages.vi.auth as Record<string, unknown>, {
  google: {
    unavailable: 'Đăng nhập bằng Google hiện chưa khả dụng.',
    loadError: 'Không thể kết nối với Google. Vui lòng thử lại sau.',
    disabled: 'Đăng nhập bằng Google đang tạm khóa',
    blocked: 'Không khả dụng',
  },
})

Object.assign(messages.en.auth as Record<string, unknown>, {
  google: {
    unavailable: 'Google sign-in is currently unavailable.',
    loadError: 'Unable to connect to Google. Please try again later.',
    disabled: 'Google sign-in is temporarily disabled',
    blocked: 'Unavailable',
  },
})

Object.assign(messages.vi.orderConfirmationBankTransfer as Record<string, unknown>, {
  qrFallbackNoticeTitle: 'Đang dùng mã QR dự phòng',
  qrFallbackNoticeDescription:
    'Mã QR dự phòng đang được hiển thị. Hãy kiểm tra kỹ số tiền và nội dung chuyển khoản trước khi xác nhận.',
  copyButton: 'Sao chép nội dung',
  copySuccess: 'Đã sao chép nội dung chuyển khoản.',
  copyError: 'Không thể sao chép nội dung chuyển khoản.',
})

Object.assign(messages.en.orderConfirmationBankTransfer as Record<string, unknown>, {
  qrFallbackNoticeTitle: 'Using a backup QR code',
  qrFallbackNoticeDescription:
    'A backup QR code is shown. Check the amount and transfer content carefully before confirming.',
})

Object.assign(messages.vi.library.detail as Record<string, unknown>, {
  accessDescription:
    'Mở trình đọc riêng hoặc tải sách khi quyền truy cập của bạn còn hiệu lực.',
  mimeTypeLabel: 'Loại tệp',
  openingDownloadLabel: 'Đang chuẩn bị tệp tải xuống…',
})

Object.assign(messages.en.library.detail as Record<string, unknown>, {
  accessDescription:
    'Open the private reader or download the book while your access remains active.',
  openingDownloadLabel: 'Preparing your download…',
})

Object.assign(messages.vi.notifications as Record<string, unknown>, {
  realtimeConnected: 'Đã kết nối',
  realtimeFallback: 'Đang kết nối lại',
})

Object.assign(messages.en.notifications as Record<string, unknown>, {
  realtimeConnected: 'Connected',
  realtimeFallback: 'Reconnecting',
})

Object.assign(messages.vi.chat.customer as Record<string, unknown>, {
  humanModeDescription: 'Trao đổi trực tiếp với đội ngũ hỗ trợ.',
  realtimeConnected: 'Đã kết nối',
  realtimeFallback: 'Đang kết nối lại',
})

Object.assign(messages.en.chat.customer as Record<string, unknown>, {
  humanModeDescription: 'Talk directly with the support team.',
  realtimeConnected: 'Connected',
  realtimeFallback: 'Reconnecting',
})

Object.assign(getMutableMessageSection(messages.vi, 'orderTimeline'), {
  empty: 'Đơn hàng này chưa có mốc cập nhật nào.',
})

Object.assign(getMutableMessageSection(messages.vi, 'readingJournal'), {
  badge: 'Nhật ký đọc sách',
  description:
    'Ghi lại từng buổi đọc, lưu cảm nhận và duy trì thói quen đọc mỗi ngày.',
  booksLoadError: 'Không thể tải danh sách sách cho nhật ký',
  loadError: 'Không thể tải nhật ký đọc sách',
  streakLoadError: 'Không thể tải chuỗi ngày đọc',
  createSuccess: 'Đã lưu ghi chép mới',
  updateSuccess: 'Đã cập nhật ghi chép',
  deleteSuccess: 'Đã xóa ghi chép',
  checkInSuccess: 'Đã ghi nhận hoạt động đọc hôm nay',
  createError: 'Không thể lưu ghi chép',
  updateError: 'Không thể cập nhật ghi chép',
  deleteError: 'Không thể xóa ghi chép',
  checkInError: 'Không thể ghi nhận hoạt động hôm nay',
  deleteConfirm: 'Bạn có chắc muốn xóa ghi chép này không?',
  profileShortcut: 'Nhật ký đọc của tôi',
  openFromBookDetail: 'Viết nhật ký',
  openFromLibraryDetail: 'Ghi vào nhật ký',
  loginRequired: 'Vui lòng đăng nhập để dùng nhật ký đọc sách',
  emptyTitle: 'Nhật ký của bạn vẫn còn trống',
  emptyDescription:
    'Tạo ghi chép đầu tiên hoặc đánh dấu hoạt động hôm nay để bắt đầu lưu lại hành trình đọc.',
})

Object.assign(getMutableMessageSection(messages.vi, 'readingJournal', 'stats'), {
  entries: 'Tổng ghi chép',
  entriesHint: 'Tất cả ghi chú đã lưu trong nhật ký.',
  current: 'Chuỗi ngày hiện tại',
  currentHint: 'Số ngày đọc liên tiếp bạn đang duy trì.',
  longest: 'Chuỗi ngày dài nhất',
  longestHint: 'Chuỗi ngày đọc dài nhất bạn từng đạt được.',
})

Object.assign(getMutableMessageSection(messages.vi, 'readingJournal', 'streak'), {
  title: 'Thói quen đọc mỗi ngày',
  description: 'Các ghi chép giúp tính chuỗi ngày đọc của bạn.',
  checkedIn: 'Đã ghi nhận',
  notCheckedIn: 'Chưa ghi nhận',
})

Object.assign(getMutableMessageSection(messages.vi, 'readingJournal', 'composer'), {
  title: 'Tạo ghi chép mới',
  description: 'Chọn sách, ngày đọc, tiến độ và lưu một vài cảm nhận.',
  newBadge: 'Mới',
  editTitle: 'Chỉnh sửa ghi chép',
  editDescription: 'Bạn đang cập nhật một ghi chép đã lưu.',
  editBadge: 'Chỉnh sửa',
})

Object.assign(getMutableMessageSection(messages.vi, 'readingJournal', 'actions'), {
  saveEntry: 'Lưu ghi chép',
  updateEntry: 'Cập nhật ghi chép',
  checkInToday: 'Ghi nhận hôm nay',
  editEntry: 'Sửa ghi chép',
  deleteEntry: 'Xóa ghi chép',
})

Object.assign(getMutableMessageSection(messages.vi, 'readingJournal', 'filters'), {
  title: 'Bộ lọc nhật ký',
  description: 'Lọc nhật ký theo sách hoặc khoảng ngày.',
  reset: 'Đặt lại',
})

Object.assign(getMutableMessageSection(messages.vi, 'readingJournal', 'timeline'), {
  title: 'Hành trình đọc',
  description: 'Các ghi chép được nhóm theo ngày để bạn dễ xem lại.',
  countLabel: '{count} ghi chép trong nhật ký',
  dayCount: '{count} ghi chép trong ngày này',
  noteFallback: 'Ghi chép này chưa có nội dung chi tiết.',
})

Object.assign(messages.vi.auth.login as Record<string, unknown>, {
  lockedImageAlt: 'Tài khoản đang bị hạn chế đăng nhập',
})

Object.assign(getMutableMessageSection(messages.vi, 'home', 'funDiscovery'), {
  bookMatchDescription: 'Trả lời vài câu hỏi để tìm sách hợp cảm hứng hôm nay.',
  bookMatchCta: 'Bắt đầu chọn sách',
  wishlistCta: 'Mở danh sách yêu thích',
  readingChallengeBadge: 'Thử thách đọc sách',
  readingChallengeDescription:
    'Tạo mục tiêu cá nhân và cập nhật tiến độ theo từng cuốn đã đọc.',
  readingChallengeCta: 'Tạo thử thách',
  readingChallengeActiveDescription:
    'Trạng thái {status}, đã hoàn thành {progress}. Mở lại để cập nhật tiến độ hoặc đổi thời hạn.',
  readingChallengeActiveHint:
    'Bạn có thể tăng, giảm, chỉnh sửa hoặc xóa thử thách.',
})

Object.assign(messages.vi.book.match as Record<string, unknown>, {
  heroBadge: 'Gợi ý sách theo sở thích',
  submitLoading: 'Đang tìm sách phù hợp…',
  resultsTitle: 'Sách dành cho bạn',
})

Object.assign(messages.vi.book.match.reasons as Record<string, unknown>, {
  MOOD: 'Hợp cảm hứng bạn chọn',
})

Object.assign(messages.vi.home.bookMatch as Record<string, unknown>, {
  badge: 'Gợi ý sách theo sở thích',
})

Object.assign(getMutableMessageSection(messages.vi, 'book', 'giftFinder'), {
  heroBadge: 'Gợi ý quà tặng',
})

Object.assign(getMutableMessageSection(messages.vi, 'book', 'giftFinder', 'actions'), {
  addToWishlist: 'Lưu vào danh sách yêu thích',
  removeFromWishlist: 'Bỏ khỏi danh sách yêu thích',
})

Object.assign(messages.vi as Record<string, unknown>, {
  wishlist: {
    badge: 'Yêu thích',
    title: 'Sách yêu thích',
    description: 'Lưu lại những tựa sách bạn muốn quay lại xem hoặc mua sau.',
    count: '{count} sách yêu thích',
    emptyTitle: 'Danh sách yêu thích đang trống',
    emptyDescription:
      'Bấm nút hình trái tim trên thẻ sách hoặc trang chi tiết để lưu sách.',
    browseBooks: 'Khám phá sách',
    loginRequired: 'Vui lòng đăng nhập để dùng danh sách yêu thích',
    fetchError: 'Không thể tải danh sách yêu thích',
    updateError: 'Không thể cập nhật danh sách yêu thích',
    added: 'Đã thêm "{title}" vào danh sách yêu thích',
    removed: 'Đã xóa "{title}" khỏi danh sách yêu thích',
  },
})

Object.assign(messages.vi.book.card as Record<string, unknown>, {
  wishlistAria: 'Lưu "{title}" vào danh sách yêu thích',
})

Object.assign(messages.vi.book.detail as Record<string, unknown>, {
  addToWishlist: 'Thêm vào danh sách yêu thích',
  removeFromWishlist: 'Bỏ khỏi danh sách yêu thích',
})

Object.assign(getMutableMessageSection(messages.vi, 'readingChallengePage'), {
  title: 'Thử thách đọc sách của bạn',
  description:
    'Đặt mục tiêu đọc, theo dõi tiến độ và tự thưởng khi hoàn thành. Tiến độ được lưu trên thiết bị này.',
  localStorageLabel: 'Nơi lưu tiến độ',
  localStorageValue: 'Thiết bị này',
  storageErrorTitle: 'Chưa thể lưu tiến độ',
  storageErrorDescription:
    'Thay đổi vẫn hiển thị lúc này nhưng có thể mất khi bạn tải lại trang.',
  deadlineLabel: 'Thời hạn',
  completedDescription:
    'Bạn đã chạm mục tiêu đọc sách. Hãy tăng mục tiêu hoặc tạo thử thách mới để giữ nhịp.',
  urgentDescription:
    'Thời hạn đang đến gần nhưng mục tiêu chưa hoàn tất. Mỗi trang đọc thêm đều đáng giá.',
  overdueTitle: 'Thử thách đã quá hạn',
  overdueDescription:
    'Bạn vẫn có thể đổi thời hạn hoặc đặt lại tiến độ để bắt đầu một vòng đọc mới.',
  deleteButton: 'Xóa thử thách',
  deleteConfirm: 'Bạn có chắc muốn xóa thử thách đọc hiện tại không?',
  emptyTitle: 'Chưa có thử thách nào',
  emptyDescription:
    'Bắt đầu bằng một mục tiêu nhỏ như 5 cuốn trong tháng này. Sau khi tạo, tiến độ và thời hạn sẽ hiện tại đây.',
  emptyCreateButton: 'Tạo thử thách ngay',
  formEditTitle: 'Chỉnh sửa thử thách',
  formEditDescription:
    'Bạn có thể đổi tên, mục tiêu hoặc kéo dài thời hạn mà không cần rời trang.',
  targetLabel: 'Số sách mục tiêu',
  presetCustomDescription: 'Tự đặt thời hạn theo lịch cá nhân của bạn.',
  previewLabel: 'Thời hạn dự kiến',
  previewValue: 'Thử thách sẽ kết thúc vào {date}',
  previewFallback: 'Chọn thời hạn để xem trước.',
  createButton: 'Tạo thử thách',
  formHintCreate:
    'Hãy đặt mục tiêu vừa sức cho lần đầu, rồi tăng dần ở các thử thách sau.',
  formHintEdit:
    'Nếu giảm mục tiêu thấp hơn số sách đã đọc, tiến độ sẽ tự điều chỉnh cho phù hợp.',
})

Object.assign(
  getMutableMessageSection(messages.vi, 'readingChallengePage', 'errors'),
  {
    titleRequired: 'Hãy nhập tên thử thách.',
    endDateRequired: 'Hãy chọn ngày kết thúc.',
    endDateBeforeStart:
      'Ngày kết thúc không thể sớm hơn ngày bắt đầu thử thách.',
    unknown: 'Không thể lưu thử thách lúc này. Vui lòng thử lại.',
  },
)
