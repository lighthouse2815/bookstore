package com.bookstore.bookstore.application.exception;

public enum ApplicationErrorCode {

    AUTH_INVALID_CREDENTIALS("AUTH_INVALID_CREDENTIALS", "Thong tin dang nhap khong hop le"),
    AUTH_RATE_LIMITED("AUTH_RATE_LIMITED", "Ban da thu qua nhieu lan. Vui long thu lai sau."),
    AUTH_SESSION_EXPIRED("AUTH_SESSION_EXPIRED", "Phien dang nhap da het han"),
    AUTH_SESSION_REVOKED("AUTH_SESSION_REVOKED", "Phien dang nhap khong con hieu luc"),
    AUTH_REFRESH_REUSE_DETECTED("AUTH_REFRESH_REUSE_DETECTED", "Phien dang nhap khong con hieu luc"),
    AUTH_CSRF_INVALID("AUTH_CSRF_INVALID", "Yeu cau bao mat khong hop le"),
    OTP_LOCKED("AUTH_OTP_LOCKED", "Ma xac thuc khong hop le"),

    INVALID_ARGUMENT("APPLICATION_001", "%s không được null"),

    INVALID_AUTH_PASSWORD("APPLICATION_AUTH_001", "password không được null"),
    AUTH_USER_NOT_FOUND("APPLICATION_AUTH_003", "không tìm thấy user"),
    AUTH_INVALID_PASSWORD("APPLICATION_AUTH_006", "mật khẩu không đúng"),
    AUTH_INVALID_REFRESH_TOKEN("APPLICATION_AUTH_007", "refresh token không hợp lệ"),
    AUTH_REFRESH_TOKEN_EXPIRED("APPLICATION_AUTH_008", "refresh token đã hết hạn"),
    AUTH_INVALID_PASSWORD_RESET_TOKEN("APPLICATION_AUTH_009", "reset token không hợp lệ"),
    AUTH_PASSWORD_RESET_TOKEN_EXPIRED("APPLICATION_AUTH_010", "reset token đã hết hạn"),
    AUTH_PASSWORD_LOGIN_NOT_AVAILABLE("APPLICATION_AUTH_011", "tài khoản này không hỗ trợ đăng nhập bằng mật khẩu"),
    AUTH_GOOGLE_INVALID_ID_TOKEN("APPLICATION_AUTH_012", "Google ID token không hợp lệ"),
    AUTH_GOOGLE_EMAIL_NOT_VERIFIED("APPLICATION_AUTH_013", "email Google chưa được xác thực"),
    AUTH_GOOGLE_ACCOUNT_ALREADY_LINKED("APPLICATION_AUTH_014", "tài khoản này đã liên kết với một tài khoản Google khác"),

    OTP_INVALID("APPLICATION_OTP_001", "otp không hợp lệ"),
    OTP_EXPIRED("APPLICATION_OTP_002", "otp đã hết hạn"),
    OTP_EMAIL_NOT_CONFIGURED("APPLICATION_OTP_003", "cấu hình gửi email chưa đầy đủ"),
    OTP_EMAIL_SEND_FAILED("APPLICATION_OTP_004", "gửi otp thất bại"),
    OTP_NOT_VERIFIED("APPLICATION_OTP_005", "otp chưa được xác thực"),
    OTP_RATE_LIMITED("APPLICATION_OTP_006", "vui lòng thử lại sau %s giây"),

    USER_ALREADY_EXISTS("APPLICATION_USER_001", "user đã tồn tại"),
    USER_NOT_FOUND("APPLICATION_USER_002", "không tìm thấy user"),
    USER_USERNAME_ALREADY_EXISTS("APPLICATION_USER_003", "username đã tồn tại"),
    USER_PHONE_ALREADY_EXISTS("APPLICATION_USER_004", "phoneNumber đã tồn tại"),
    USER_EMAIL_ALREADY_EXISTS("APPLICATION_USER_005", "email đã tồn tại"),
    USER_NOT_ADMIN("APPLICATION_USER_006", "không có quyền xóa user này"),
    USER_ROLE_NOT_ALLOWED("APPLICATION_USER_007", "role chỉ được la STAFF, ADMIN hoặc SHIPPER"),
    STAFF_NOT_FOUND("APPLICATION_USER_008", "không tìm thấy nhân viên"),
    USER_SELF_MANAGEMENT_NOT_ALLOWED("APPLICATION_USER_009", "không thể thao tác tài khoản của chính mình"),
    SHIPPER_NOT_FOUND("APPLICATION_USER_010", "không tìm thấy shipper"),

    ROLE_ALREADY_EXISTS("APPLICATION_ROLE_001", "role đã tồn tại"),
    ROLE_NOT_FOUND("APPLICATION_ROLE_002", "không tìm thấy role"),
    ROLE_NAME_ALREADY_EXISTS("APPLICATION_ROLE_003", "role đã tồn tại"),

    CATEGORY_NOT_FOUND("APPLICATION_CATEGORY_001", "không tìm thấy category"),
    CATEGORY_NAME_ALREADY_EXISTS("APPLICATION_CATEGORY_002", "category đã tồn tại"),
    CATEGORY_CODE_ALREADY_EXISTS("APPLICATION_CATEGORY_003", "mã category đã tồn tại"),

    AUTHOR_NOT_FOUND("APPLICATION_AUTHOR_001", "không tìm thấy author"),
    AUTHOR_NAME_ALREADY_EXISTS("APPLICATION_AUTHOR_002", "author đã tồn tại"),

    PUBLISHER_NOT_FOUND("APPLICATION_PUBLISHER_001", "không tìm thấy publisher"),
    PUBLISHER_NAME_ALREADY_EXISTS("APPLICATION_PUBLISHER_002", "publisher đã tồn tại"),

    SUPPLIER_NOT_FOUND("APPLICATION_SUPPLIER_001", "không tìm thấy supplier"),
    SUPPLIER_NAME_ALREADY_EXISTS("APPLICATION_SUPPLIER_002", "supplier đã tồn tại"),

    IMPORT_RECEIPT_NOT_FOUND("APPLICATION_IMPORT_RECEIPT_001", "không tìm thấy phiếu nhập"),
    AUDIT_LOG_NOT_FOUND("APPLICATION_AUDIT_LOG_001", "không tìm thấy nhật ký hệ thống"),

    COUPON_NOT_FOUND("APPLICATION_COUPON_001", "không tìm thấy coupon"),
    COUPON_CODE_ALREADY_EXISTS("APPLICATION_COUPON_002", "coupon đã tồn tại"),
    COUPON_TYPE_NOT_MATCH("APPLICATION_COUPON_003", "coupon không đúng loại áp dụng"),

    BOOK_NOT_FOUND("APPLICATION_BOOK_001", "không tìm thấy book"),
    BOOKSHELF_NOT_FOUND("APPLICATION_BOOKSHELF_001", "khong tim thay ke sach"),
    BOOKSHELF_NAME_ALREADY_EXISTS("APPLICATION_BOOKSHELF_002", "ten ke sach da ton tai"),
    BOOKSHELF_REORDER_INVALID("APPLICATION_BOOKSHELF_003", "thu tu sap xep ke sach khong hop le"),
    READING_JOURNAL_ENTRY_NOT_FOUND("APPLICATION_READING_JOURNAL_001", "khong tim thay nhat ky doc"),
    READING_JOURNAL_ENTRY_ALREADY_EXISTS("APPLICATION_READING_JOURNAL_002", "nhat ky doc da ton tai"),

    DIGITAL_ASSET_NOT_FOUND("APPLICATION_DIGITAL_ASSET_001", "không tìm thấy tài nguyên số"),
    DIGITAL_ASSET_PURCHASE_NOT_ALLOWED("APPLICATION_DIGITAL_ASSET_002", "tài nguyên số này không cho phép mua"),
    FILE_ASSET_NOT_FOUND("APPLICATION_FILE_001", "không tìm thấy file"),
    FILE_ASSET_UPLOAD_NOT_COMPLETED("APPLICATION_FILE_002", "file upload chưa hoàn tất"),
    FILE_ASSET_INVALID_PURPOSE("APPLICATION_FILE_003", "file không đúng mục đích nghiệp vụ"),
    FILE_ASSET_INVALID_VISIBILITY("APPLICATION_FILE_004", "file không đúng phạm vi truy cập"),
    FILE_ASSET_CONTENT_TYPE_NOT_ALLOWED("APPLICATION_FILE_005", "contentType không được hỗ trợ"),
    FILE_ASSET_SIZE_EXCEEDED("APPLICATION_FILE_006", "kích thước file vượt giới hạn"),
    FILE_ASSET_DOWNLOAD_NOT_ALLOWED("APPLICATION_FILE_007", "tải tệp không được phép"),
    FILE_ASSET_OBJECT_NOT_FOUND("APPLICATION_FILE_008", "tệp trên storage không tồn tại"),
    FILE_STORAGE_NOT_CONFIGURED("APPLICATION_FILE_009", "cấu hình file storage chưa đầy đủ"),
    FILE_ASSET_ACCESS_DENIED("APPLICATION_FILE_010", "không có quyền truy cập file"),
    FILE_ASSET_IN_USE("APPLICATION_FILE_011", "file đang được sử dụng bởi: %s"),
    FILE_ASSET_OBJECT_METADATA_MISMATCH("APPLICATION_FILE_012", "metadata tệp trên storage không khớp"),
    FILE_STORAGE_SAFETY_LIMIT_REACHED(
            "APPLICATION_FILE_013",
            "Hệ thống đã tạm dừng upload để không vượt hạn mức lưu trữ miễn phí"
    ),

    CART_NOT_FOUND("APPLICATION_CART_001", "không tìm thấy giỏ hàng"),
    CART_EMPTY("APPLICATION_CART_002", "giỏ hàng đang trống"),
    CART_ITEM_NOT_FOUND("APPLICATION_CART_003", "không tìm thấy sản phẩm trong giỏ hàng"),

    ORDER_NOT_FOUND("APPLICATION_ORDER_001", "không tìm thấy đơn hàng"),
    ORDER_PAYMENT_NOT_PAID("APPLICATION_ORDER_002", "đơn hàng thanh toán online chưa được thanh toán"),
    ORDER_IDEMPOTENCY_PAYLOAD_MISMATCH("APPLICATION_ORDER_003", "Idempotency-Key đã được dùng cho dữ liệu checkout khác"),
    ORDER_CANCELLATION_NOT_ALLOWED("APPLICATION_ORDER_004", "đơn hàng không còn ở trạng thái có thể hủy"),
    ORDER_PAID_REFUND_REQUIRED("ORDER_PAID_REFUND_REQUIRED", "đơn hàng đã thanh toán phải được xử lý qua quy trình hoàn tiền"),

    RETURN_REQUEST_NOT_FOUND("APPLICATION_RETURN_REQUEST_001", "không tìm thấy yêu cầu trả hàng"),
    RETURN_REQUEST_ORDER_NOT_DELIVERED("APPLICATION_RETURN_REQUEST_002", "chỉ có thể tạo yêu cầu trả hàng cho đơn đã giao"),
    RETURN_REQUEST_ALREADY_EXISTS("APPLICATION_RETURN_REQUEST_003", "đơn hàng đã có yêu cầu trả hàng đang xử lý hoặc đã được duyệt"),
    RETURN_REQUEST_NOT_PENDING("APPLICATION_RETURN_REQUEST_004", "chỉ có thể thao tác với yêu cầu đang chờ xử lý"),
    RETURN_REQUEST_REFUND_AMOUNT_INVALID("APPLICATION_RETURN_REQUEST_005", "số tiền hoàn không hợp lệ"),
    RETURN_REQUEST_REJECT_NOTE_REQUIRED("APPLICATION_RETURN_REQUEST_006", "lý do từ chối không được để trống"),

    REFUND_NOT_FOUND("REFUND_NOT_FOUND", "không tìm thấy yêu cầu hoàn tiền"),
    REFUND_ORDER_NOT_PAID("REFUND_ORDER_NOT_PAID", "chỉ có thể hoàn tiền đơn đã thanh toán"),
    REFUND_AMOUNT_INVALID("REFUND_AMOUNT_INVALID", "số tiền hoàn không hợp lệ"),
    REFUND_AMOUNT_EXCEEDS_REMAINING("REFUND_AMOUNT_EXCEEDS_REMAINING", "số tiền hoàn vượt quá số tiền còn có thể hoàn"),
    REFUND_INVALID_TRANSITION("REFUND_INVALID_TRANSITION", "chuyển trạng thái hoàn tiền không hợp lệ"),
    REFUND_EVIDENCE_REQUIRED("REFUND_EVIDENCE_REQUIRED", "hoàn tiền thành công phải có mã đối soát và bằng chứng"),
    REFUND_FAILURE_REASON_REQUIRED("REFUND_FAILURE_REASON_REQUIRED", "lý do hoàn tiền thất bại không được để trống"),
    REFUND_RETURN_REQUEST_INVALID("REFUND_RETURN_REQUEST_INVALID", "yêu cầu trả hàng không hợp lệ để hoàn tiền"),
    REFUND_CURRENCY_INVALID("REFUND_CURRENCY_INVALID", "loại tiền hoàn không được hỗ trợ"),

    OUTBOX_EVENT_NOT_FOUND("OUTBOX_EVENT_NOT_FOUND", "không tìm thấy sự kiện outbox"),
    OUTBOX_EVENT_RETRY_NOT_ALLOWED("OUTBOX_EVENT_RETRY_NOT_ALLOWED", "sự kiện outbox không thể retry ở trạng thái hiện tại"),
    OUTBOX_PAYLOAD_INVALID("OUTBOX_PAYLOAD_INVALID", "dữ liệu outbox không hợp lệ hoặc chứa thông tin nhạy cảm"),

    SHIPMENT_NOT_FOUND("APPLICATION_SHIPMENT_001", "không tìm thấy phiếu giao hàng"),
    SHIPMENT_ORDER_NOT_READY("APPLICATION_SHIPMENT_002", "đơn hàng không ở trạng thái sẵn sàng giao"),
    SHIPMENT_ORDER_ALREADY_HAS_ACTIVE_ASSIGNMENT("APPLICATION_SHIPMENT_003", "đơn hàng đang có phiếu giao hàng chưa kết thúc"),

    PAYMENT_WEBHOOK_UNAUTHORIZED("APPLICATION_PAYMENT_001", "sepay webhook không hợp lệ"),
    PAYMENT_NOT_FOUND("APPLICATION_PAYMENT_002", "không tìm thấy thanh toán"),
    PAYMENT_RECONCILIATION_NOT_FOUND("APPLICATION_PAYMENT_003", "không tìm thấy vấn đề đối soát thanh toán"),
    PAYMENT_RECONCILIATION_NOT_OPEN("APPLICATION_PAYMENT_004", "vấn đề đối soát không còn ở trạng thái mở"),

    USER_ADDRESS_NOT_FOUND("APPLICATION_USER_ADDRESS_001", "không tìm thấy địa chỉ nhận hàng"),

    NOTIFICATION_NOT_FOUND("APPLICATION_NOTIFICATION_001", "không tìm thấy thông báo"),
    CHAT_CONVERSATION_NOT_FOUND("APPLICATION_CHAT_001", "không tìm thấy cuộc trò chuyện"),
    CHAT_MESSAGE_NOT_FOUND("APPLICATION_CHAT_002", "không tìm thấy tin nhắn"),
    CHAT_CONVERSATION_FORBIDDEN("APPLICATION_CHAT_003", "không có quyền truy cập cuộc trò chuyện này"),
    CHAT_CONVERSATION_CLOSED("APPLICATION_CHAT_004", "cuộc trò chuyện đã đóng"),
    CHAT_ASSIGNEE_NOT_FOUND("APPLICATION_CHAT_005", "không tìm thấy nhân viên phụ trách"),
    CHAT_ASSIGNEE_ROLE_INVALID("APPLICATION_CHAT_006", "tài khoản được phân công phải là ADMIN hoặc STAFF"),
    CHAT_ADMIN_ROLE_REQUIRED("APPLICATION_CHAT_007", "chỉ ADMIN hoặc STAFF mới được thao tác chat quản trị"),
    CHAT_CUSTOMER_ROLE_REQUIRED("APPLICATION_CHAT_008", "chỉ USER mới được sử dụng chat hỗ trợ"),

    REVIEW_NOT_FOUND("APPLICATION_REVIEW_001", "không tìm thấy đánh giá"),
    REVIEW_ALREADY_EXISTS("APPLICATION_REVIEW_002", "order item đã được đánh giá"),
    REVIEW_BOOK_NOT_PURCHASED("APPLICATION_REVIEW_003", "không thể đánh giá sách chưa mua"),

    PERMISSION_ALREADY_EXISTS("APPLICATION_PERMISSION_001", "permission đã tồn tại"),
    PERMISSION_NOT_FOUND("APPLICATION_PERMISSION_002", "không tìm thấy permission"),
    PERMISSION_CODE_ALREADY_EXISTS("APPLICATION_PERMISSION_003", "permission đã tồn tại"),
    PERMISSION_IN_USE("APPLICATION_PERMISSION_004", "permission đang được sử dụng"),

    PROFILE_ALREADY_EXISTS("APPLICATION_PROFILE_001", "profile đã tồn tại"),
    PROFILE_NOT_FOUND("APPLICATION_PROFILE_002", "không tìm thấy profile"),
    PROFILE_USER_NOT_FOUND("APPLICATION_PROFILE_003", "không tìm thấy user"),
    PROFILE_USER_ALREADY_HAS_PROFILE("APPLICATION_PROFILE_004", "user đã có profile"),

    NEWSLETTER_SUBSCRIPTION_NOT_FOUND("APPLICATION_NEWSLETTER_001", "Không tìm thấy đăng ký nhận tin"),
    NEWSLETTER_RATE_LIMITED("APPLICATION_NEWSLETTER_002", "Bạn thao tác quá nhiều lần. Vui lòng thử lại sau");

    private final String code;
    private final String messageTemplate;

    ApplicationErrorCode(String code, String messageTemplate) {
        this.code = code;
        this.messageTemplate = messageTemplate;
    }

    public String getCode() {
        return code;
    }

    public String message(Object... args) {
        return String.format(messageTemplate, args);
    }
}

