package com.bookstore.bookstore.application.exception;

public enum ApplicationErrorCode {

    INVALID_ARGUMENT("APPLICATION_001", "%s khong duoc null"),

    INVALID_AUTH_PASSWORD("APPLICATION_AUTH_001", "password khong duoc null"),
    AUTH_USER_NOT_FOUND("APPLICATION_AUTH_003", "khong tim thay user"),
    AUTH_INVALID_PASSWORD("APPLICATION_AUTH_006", "mat khau khong dung"),
    AUTH_INVALID_REFRESH_TOKEN("APPLICATION_AUTH_007", "refresh token khong hop le"),
    AUTH_REFRESH_TOKEN_EXPIRED("APPLICATION_AUTH_008", "refresh token da het han"),

    USER_ALREADY_EXISTS("APPLICATION_USER_001", "user da ton tai"),
    USER_NOT_FOUND("APPLICATION_USER_002", "khong tim thay user"),
    USER_USERNAME_ALREADY_EXISTS("APPLICATION_USER_003", "username da ton tai"),
    USER_PHONE_ALREADY_EXISTS("APPLICATION_USER_004", "phoneNumber da ton tai"),
    USER_EMAIL_ALREADY_EXISTS("APPLICATION_USER_005", "email da ton tai"),
    USER_NOT_ADMIN("APPLICATION_USER_006", "khong co quyen xoa user nay"),

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

    BOOK_NOT_FOUND("APPLICATION_BOOK_001", "khong tim thay book"),

    CART_NOT_FOUND("APPLICATION_CART_001", "khong tim thay gio hang"),
    CART_EMPTY("APPLICATION_CART_002", "gio hang dang trong"),

    ORDER_NOT_FOUND("APPLICATION_ORDER_001", "khong tim thay don hang"),

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
