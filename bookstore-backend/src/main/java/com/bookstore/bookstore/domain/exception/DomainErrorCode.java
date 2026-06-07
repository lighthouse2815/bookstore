package com.bookstore.bookstore.domain.exception;

public enum DomainErrorCode {

    INVALID_USER_ID("DOMAIN_USER_001", "%s khong duoc null"),
    INVALID_USER_USERNAME("DOMAIN_USER_002", "%s khong duoc de trong"),
    INVALID_USER_PASSWORD_HASH("DOMAIN_USER_003", "%s khong duoc de trong"),
    INVALID_USER_PHONE_NUMBER("DOMAIN_USER_004", "%s phai co dung 10 chu so va bat dau bang 0"),
    INVALID_USER_EMAIL("DOMAIN_USER_005", "%s phai ket thuc bang @gmail.com"),
    INVALID_USER_STATUS("DOMAIN_USER_006", "%s khong duoc null"),
    INVALID_USER_ROLES("DOMAIN_USER_007", "%s khong duoc chua phan tu null"),
    INVALID_USER_CREATED_AT("DOMAIN_USER_008", "%s khong duoc nam trong tuong lai"),
    INVALID_USER_UPDATED_AT("DOMAIN_USER_009", "%s khong duoc nam trong tuong lai"),
    INVALID_USER_DELETED_AT("DOMAIN_USER_010", "%s khong duoc nam trong tuong lai"),
    INVALID_USER_AUDIT_ORDER("DOMAIN_USER_011", "%s khong duoc som hon %s"),
    USER_ALREADY_EXISTS("DOMAIN_USER_012", "user da ton tai"),
    USER_NOT_FOUND("DOMAIN_USER_013", "khong tim thay user"),
    USER_USERNAME_ALREADY_EXISTS("DOMAIN_USER_014", "username da ton tai"),
    USER_ALREADY_ACTIVE("DOMAIN_USER_015", "user da duoc kich hoat"),
    BLOCKED_USER_CANNOT_BE_ACTIVATED("DOMAIN_USER_016", "tai khoan bi khoa khong the kich hoat"),
    DELETED_USER_CANNOT_BE_ACTIVATED("DOMAIN_USER_017", "tai khoan da xoa khong the kich hoat"),
    USER_ALREADY_DELETED("DOMAIN_USER_018", "user da bi xoa"),
    USER_NOT_ACTIVE_CANNOT_UPDATE_ACCOUNT_INFO("DOMAIN_USER_019", "tai khoan khong hoat dong khong the cap nhat thong tin"),
    BLOCKED_USER_CANNOT_UPDATE_ACCOUNT_INFO("DOMAIN_USER_020", "tai khoan bi khoa khong the cap nhat thong tin"),
    DELETED_USER_CANNOT_UPDATE_ACCOUNT_INFO("DOMAIN_USER_021", "tai khoan da xoa khong the cap nhat thong tin"),
    USER_PHONE_ALREADY_EXISTS("DOMAIN_USER_022", "phoneNumber da ton tai"),
    USER_EMAIL_ALREADY_EXISTS("DOMAIN_USER_023", "email da ton tai"),
    USER_NOT_ACTIVE_CANNOT_LOGIN("DOMAIN_USER_024", "tai khoan khong hoat dong khong the dang nhap"),
    BLOCKED_USER_CANNOT_LOGIN("DOMAIN_USER_025", "tai khoan bi khoa khong the dang nhap"),
    DELETED_USER_CANNOT_LOGIN("DOMAIN_USER_026", "tai khoan da xoa khong the dang nhap"),

    INVALID_PROFILE_ID("DOMAIN_PROFILE_001", "%s khong duoc null"),
    INVALID_PROFILE_USER_ID("DOMAIN_PROFILE_002", "%s khong duoc null"),
    INVALID_PROFILE_LAST_NAME("DOMAIN_PROFILE_003", "%s khong duoc de trong"),
    INVALID_PROFILE_FIRST_NAME("DOMAIN_PROFILE_004", "%s khong duoc de trong"),
    INVALID_PROFILE_AVATAR_URL("DOMAIN_PROFILE_005", "%s khong duoc de trong"),
    INVALID_PROFILE_GENDER("DOMAIN_PROFILE_006", "%s khong duoc null"),
    INVALID_PROFILE_DATE_OF_BIRTH("DOMAIN_PROFILE_007", "%s khong duoc nam trong tuong lai"),
    INVALID_PROFILE_CREATED_AT("DOMAIN_PROFILE_008", "%s khong duoc nam trong tuong lai"),
    INVALID_PROFILE_UPDATED_AT("DOMAIN_PROFILE_009", "%s khong duoc nam trong tuong lai"),
    INVALID_PROFILE_DELETED_AT("DOMAIN_PROFILE_010", "%s khong duoc nam trong tuong lai"),
    INVALID_PROFILE_AUDIT_ORDER("DOMAIN_PROFILE_011", "%s khong duoc som hon %s"),
    PROFILE_ALREADY_EXISTS("DOMAIN_PROFILE_012", "profile da ton tai"),
    PROFILE_NOT_FOUND("DOMAIN_PROFILE_013", "khong tim thay profile"),
    PROFILE_USER_NOT_FOUND("DOMAIN_PROFILE_014", "khong tim thay user"),
    PROFILE_USER_ALREADY_HAS_PROFILE("DOMAIN_PROFILE_015", "user da co profile"),
    PROFILE_ALREADY_DELETED("DOMAIN_PROFILE_016", "profile da bi xoa"),
    DELETED_PROFILE_CANNOT_UPDATE_PROFILE_INFO("DOMAIN_PROFILE_017", "profile da xoa khong the cap nhat thong tin"),

    INVALID_CATEGORY_ID("DOMAIN_CATEGORY_001", "%s khong duoc null"),
    INVALID_CATEGORY_NAME("DOMAIN_CATEGORY_002", "%s khong duoc de trong"),
    INVALID_CATEGORY_CREATED_AT("DOMAIN_CATEGORY_003", "%s khong duoc nam trong tuong lai"),
    INVALID_CATEGORY_UPDATED_AT("DOMAIN_CATEGORY_004", "%s khong duoc nam trong tuong lai"),
    INVALID_CATEGORY_DELETED_AT("DOMAIN_CATEGORY_005", "%s khong duoc nam trong tuong lai"),
    INVALID_CATEGORY_AUDIT_ORDER("DOMAIN_CATEGORY_006", "%s khong duoc som hon %s"),
    CATEGORY_ALREADY_DELETED("DOMAIN_CATEGORY_007", "category da bi xoa"),
    CATEGORY_DATA_NOT_CHANGED("DOMAIN_CATEGORY_008", "du lieu category phai khac du lieu hien tai"),

    INVALID_AUTHOR_ID("DOMAIN_AUTHOR_001", "%s khong duoc null"),
    INVALID_AUTHOR_NAME("DOMAIN_AUTHOR_002", "%s khong duoc de trong"),
    INVALID_AUTHOR_CREATED_AT("DOMAIN_AUTHOR_003", "%s khong duoc nam trong tuong lai"),
    INVALID_AUTHOR_UPDATED_AT("DOMAIN_AUTHOR_004", "%s khong duoc nam trong tuong lai"),
    INVALID_AUTHOR_DELETED_AT("DOMAIN_AUTHOR_005", "%s khong duoc nam trong tuong lai"),
    INVALID_AUTHOR_AUDIT_ORDER("DOMAIN_AUTHOR_006", "%s khong duoc som hon %s"),
    AUTHOR_ALREADY_DELETED("DOMAIN_AUTHOR_007", "author da bi xoa"),
    AUTHOR_DATA_NOT_CHANGED("DOMAIN_AUTHOR_008", "du lieu author phai khac du lieu hien tai"),

    INVALID_PUBLISHER_ID("DOMAIN_PUBLISHER_001", "%s khong duoc null"),
    INVALID_PUBLISHER_NAME("DOMAIN_PUBLISHER_002", "%s khong duoc de trong"),
    INVALID_PUBLISHER_CREATED_AT("DOMAIN_PUBLISHER_003", "%s khong duoc nam trong tuong lai"),
    INVALID_PUBLISHER_UPDATED_AT("DOMAIN_PUBLISHER_004", "%s khong duoc nam trong tuong lai"),
    INVALID_PUBLISHER_DELETED_AT("DOMAIN_PUBLISHER_005", "%s khong duoc nam trong tuong lai"),
    INVALID_PUBLISHER_AUDIT_ORDER("DOMAIN_PUBLISHER_006", "%s khong duoc som hon %s"),
    PUBLISHER_ALREADY_DELETED("DOMAIN_PUBLISHER_007", "publisher da bi xoa"),
    PUBLISHER_DATA_NOT_CHANGED("DOMAIN_PUBLISHER_008", "du lieu publisher phai khac du lieu hien tai"),

    INVALID_BOOK_ID("DOMAIN_BOOK_001", "%s khong duoc null"),
    INVALID_BOOK_TITLE("DOMAIN_BOOK_002", "%s khong duoc de trong"),
    INVALID_BOOK_PRICE("DOMAIN_BOOK_003", "%s khong duoc am"),
    INVALID_BOOK_STOCK_QUANTITY("DOMAIN_BOOK_004", "%s khong duoc am"),
    INVALID_BOOK_CATEGORY_ID("DOMAIN_BOOK_005", "%s khong duoc null"),
    INVALID_BOOK_AUTHOR_ID("DOMAIN_BOOK_006", "%s khong duoc null"),
    INVALID_BOOK_PUBLISHER_ID("DOMAIN_BOOK_007", "%s khong duoc null"),
    INVALID_BOOK_CREATED_AT("DOMAIN_BOOK_008", "%s khong duoc nam trong tuong lai"),
    INVALID_BOOK_UPDATED_AT("DOMAIN_BOOK_009", "%s khong duoc nam trong tuong lai"),
    INVALID_BOOK_DELETED_AT("DOMAIN_BOOK_010", "%s khong duoc nam trong tuong lai"),
    INVALID_BOOK_AUDIT_ORDER("DOMAIN_BOOK_011", "%s khong duoc som hon %s"),
    BOOK_ALREADY_DELETED("DOMAIN_BOOK_012", "book da bi xoa"),
    BOOK_DATA_NOT_CHANGED("DOMAIN_BOOK_013", "du lieu book phai khac du lieu hien tai"),

    INVALID_ROLE_ID("DOMAIN_ROLE_001", "%s khong duoc null"),
    INVALID_ROLE_NAME("DOMAIN_ROLE_002", "%s khong duoc null"),
    INVALID_ROLE_DESCRIPTION("DOMAIN_ROLE_003", "%s khong duoc de trong"),
    INVALID_ROLE_PERMISSIONS("DOMAIN_ROLE_004", "%s khong duoc chua phan tu null"),
    INVALID_ROLE_CREATED_AT("DOMAIN_ROLE_005", "%s khong duoc nam trong tuong lai"),
    INVALID_ROLE_UPDATED_AT("DOMAIN_ROLE_006", "%s khong duoc nam trong tuong lai"),
    INVALID_ROLE_DELETED_AT("DOMAIN_ROLE_007", "%s khong duoc nam trong tuong lai"),
    INVALID_ROLE_AUDIT_ORDER("DOMAIN_ROLE_008", "%s khong duoc som hon %s"),
    ROLE_ALREADY_DELETED("DOMAIN_ROLE_009", "role da bi xoa"),
    ROLE_DATA_NOT_CHANGED("DOMAIN_ROLE_010", "du lieu role phai khac du lieu hien tai"),

    INVALID_PERMISSION_ID("DOMAIN_PERMISSION_001", "%s khong duoc null"),
    INVALID_PERMISSION_CODE("DOMAIN_PERMISSION_002", "%s khong duoc null"),
    INVALID_PERMISSION_DESCRIPTION("DOMAIN_PERMISSION_003", "%s khong duoc de trong"),
    INVALID_PERMISSION_CREATED_AT("DOMAIN_PERMISSION_004", "%s khong duoc nam trong tuong lai"),
    INVALID_PERMISSION_UPDATED_AT("DOMAIN_PERMISSION_005", "%s khong duoc nam trong tuong lai"),
    INVALID_PERMISSION_DELETED_AT("DOMAIN_PERMISSION_006", "%s khong duoc nam trong tuong lai"),
    INVALID_PERMISSION_AUDIT_ORDER("DOMAIN_PERMISSION_007", "%s khong duoc som hon %s"),
    PERMISSION_ALREADY_DELETED("DOMAIN_PERMISSION_008", "permission da bi xoa"),
    PERMISSION_CODE_NOT_CHANGED("DOMAIN_PERMISSION_009", "code permission phai khac code hien tai"),

    INVALID_REFRESH_TOKEN_ID("DOMAIN_REFRESH_TOKEN_001", "%s khong duoc null"),
    INVALID_REFRESH_TOKEN_USER_ID("DOMAIN_REFRESH_TOKEN_002", "%s khong duoc null"),
    INVALID_REFRESH_TOKEN_TOKEN("DOMAIN_REFRESH_TOKEN_003", "%s khong duoc de trong"),
    INVALID_REFRESH_TOKEN_EXPIRES_AT("DOMAIN_REFRESH_TOKEN_004", "%s phai nam trong tuong lai"),
    INVALID_REFRESH_TOKEN_CREATED_AT("DOMAIN_REFRESH_TOKEN_005", "%s khong duoc nam trong tuong lai"),
    INVALID_REFRESH_TOKEN_AUDIT_ORDER("DOMAIN_REFRESH_TOKEN_006", "%s phai sau %s");

    private final String code;
    private final String messageTemplate;

    DomainErrorCode(String code, String messageTemplate) {
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
