package com.bookstore.bookstore.application.exception;

public enum ApplicationErrorCode {

    INVALID_ARGUMENT("APPLICATION_001", "%s khong duoc null"),

    INVALID_AUTH_PASSWORD("APPLICATION_AUTH_001", "password khong duoc null"),
    AUTH_USER_NOT_FOUND("APPLICATION_AUTH_003", "khong tim thay user"),
    AUTH_INVALID_PASSWORD("APPLICATION_AUTH_006", "mat khau khong dung"),
    AUTH_INVALID_REFRESH_TOKEN("APPLICATION_AUTH_007", "refresh token khong hop le"),
    AUTH_REFRESH_TOKEN_EXPIRED("APPLICATION_AUTH_008", "refresh token da het han"),
    AUTH_INVALID_PASSWORD_RESET_TOKEN("APPLICATION_AUTH_009", "reset token khong hop le"),
    AUTH_PASSWORD_RESET_TOKEN_EXPIRED("APPLICATION_AUTH_010", "reset token da het han"),
    AUTH_PASSWORD_LOGIN_NOT_AVAILABLE("APPLICATION_AUTH_011", "tai khoan nay khong ho tro dang nhap bang mat khau"),
    AUTH_GOOGLE_INVALID_ID_TOKEN("APPLICATION_AUTH_012", "Google ID token khong hop le"),
    AUTH_GOOGLE_EMAIL_NOT_VERIFIED("APPLICATION_AUTH_013", "email Google chua duoc xac thuc"),
    AUTH_GOOGLE_ACCOUNT_ALREADY_LINKED("APPLICATION_AUTH_014", "tai khoan nay da lien ket voi mot tai khoan Google khac"),

    OTP_INVALID("APPLICATION_OTP_001", "otp khong hop le"),
    OTP_EXPIRED("APPLICATION_OTP_002", "otp da het han"),
    OTP_EMAIL_NOT_CONFIGURED("APPLICATION_OTP_003", "cau hinh gui email chua day du"),
    OTP_EMAIL_SEND_FAILED("APPLICATION_OTP_004", "gui otp that bai"),
    OTP_NOT_VERIFIED("APPLICATION_OTP_005", "otp chua duoc xac thuc"),
    OTP_RATE_LIMITED("APPLICATION_OTP_006", "vui long thu lai sau %s giay"),

    USER_ALREADY_EXISTS("APPLICATION_USER_001", "user da ton tai"),
    USER_NOT_FOUND("APPLICATION_USER_002", "khong tim thay user"),
    USER_USERNAME_ALREADY_EXISTS("APPLICATION_USER_003", "username da ton tai"),
    USER_PHONE_ALREADY_EXISTS("APPLICATION_USER_004", "phoneNumber da ton tai"),
    USER_EMAIL_ALREADY_EXISTS("APPLICATION_USER_005", "email da ton tai"),
    USER_NOT_ADMIN("APPLICATION_USER_006", "khong co quyen xoa user nay"),
    USER_ROLE_NOT_ALLOWED("APPLICATION_USER_007", "role chi duoc la STAFF hoac ADMIN"),
    STAFF_NOT_FOUND("APPLICATION_USER_008", "khong tim thay nhan vien"),
    USER_SELF_MANAGEMENT_NOT_ALLOWED("APPLICATION_USER_009", "khong the thao tac tai khoan cua chinh minh"),

    ROLE_ALREADY_EXISTS("APPLICATION_ROLE_001", "role da ton tai"),
    ROLE_NOT_FOUND("APPLICATION_ROLE_002", "khong tim thay role"),
    ROLE_NAME_ALREADY_EXISTS("APPLICATION_ROLE_003", "role da ton tai"),

    CATEGORY_NOT_FOUND("APPLICATION_CATEGORY_001", "khong tim thay category"),
    CATEGORY_NAME_ALREADY_EXISTS("APPLICATION_CATEGORY_002", "category da ton tai"),

    AUTHOR_NOT_FOUND("APPLICATION_AUTHOR_001", "khong tim thay author"),
    AUTHOR_NAME_ALREADY_EXISTS("APPLICATION_AUTHOR_002", "author da ton tai"),

    PUBLISHER_NOT_FOUND("APPLICATION_PUBLISHER_001", "khong tim thay publisher"),
    PUBLISHER_NAME_ALREADY_EXISTS("APPLICATION_PUBLISHER_002", "publisher da ton tai"),

    SUPPLIER_NOT_FOUND("APPLICATION_SUPPLIER_001", "khong tim thay supplier"),
    SUPPLIER_NAME_ALREADY_EXISTS("APPLICATION_SUPPLIER_002", "supplier da ton tai"),

    IMPORT_RECEIPT_NOT_FOUND("APPLICATION_IMPORT_RECEIPT_001", "khong tim thay phieu nhap"),

    COUPON_NOT_FOUND("APPLICATION_COUPON_001", "khong tim thay coupon"),
    COUPON_CODE_ALREADY_EXISTS("APPLICATION_COUPON_002", "coupon da ton tai"),
    COUPON_TYPE_NOT_MATCH("APPLICATION_COUPON_003", "coupon khong dung loai ap dung"),

    BOOK_NOT_FOUND("APPLICATION_BOOK_001", "khong tim thay book"),

    CART_NOT_FOUND("APPLICATION_CART_001", "khong tim thay gio hang"),
    CART_EMPTY("APPLICATION_CART_002", "gio hang dang trong"),
    CART_ITEM_NOT_FOUND("APPLICATION_CART_003", "khong tim thay san pham trong gio hang"),

    ORDER_NOT_FOUND("APPLICATION_ORDER_001", "khong tim thay don hang"),

    PAYMENT_WEBHOOK_UNAUTHORIZED("APPLICATION_PAYMENT_001", "sepay webhook khong hop le"),

    USER_ADDRESS_NOT_FOUND("APPLICATION_USER_ADDRESS_001", "khong tim thay dia chi nhan hang"),

    NOTIFICATION_NOT_FOUND("APPLICATION_NOTIFICATION_001", "khong tim thay thong bao"),

    REVIEW_NOT_FOUND("APPLICATION_REVIEW_001", "khong tim thay danh gia"),
    REVIEW_ALREADY_EXISTS("APPLICATION_REVIEW_002", "order item da duoc danh gia"),
    REVIEW_BOOK_NOT_PURCHASED("APPLICATION_REVIEW_003", "khong the danh gia sach chua mua"),

    PERMISSION_ALREADY_EXISTS("APPLICATION_PERMISSION_001", "permission da ton tai"),
    PERMISSION_NOT_FOUND("APPLICATION_PERMISSION_002", "khong tim thay permission"),
    PERMISSION_CODE_ALREADY_EXISTS("APPLICATION_PERMISSION_003", "permission da ton tai"),
    PERMISSION_IN_USE("APPLICATION_PERMISSION_004", "permission dang duoc su dung"),

    PROFILE_ALREADY_EXISTS("APPLICATION_PROFILE_001", "profile da ton tai"),
    PROFILE_NOT_FOUND("APPLICATION_PROFILE_002", "khong tim thay profile"),
    PROFILE_USER_NOT_FOUND("APPLICATION_PROFILE_003", "khong tim thay user"),
    PROFILE_USER_ALREADY_HAS_PROFILE("APPLICATION_PROFILE_004", "user da co profile");

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
