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

    INVALID_ROLE_ID("DOMAIN_ROLE_001", "%s khong duoc null"),
    INVALID_ROLE_NAME("DOMAIN_ROLE_002", "%s khong duoc null"),
    INVALID_ROLE_DESCRIPTION("DOMAIN_ROLE_003", "%s khong duoc de trong"),
    INVALID_ROLE_PERMISSIONS("DOMAIN_ROLE_004", "%s khong duoc chua phan tu null"),
    INVALID_ROLE_CREATED_AT("DOMAIN_ROLE_005", "%s khong duoc nam trong tuong lai"),
    INVALID_ROLE_UPDATED_AT("DOMAIN_ROLE_006", "%s khong duoc nam trong tuong lai"),
    INVALID_ROLE_DELETED_AT("DOMAIN_ROLE_007", "%s khong duoc nam trong tuong lai"),
    INVALID_ROLE_AUDIT_ORDER("DOMAIN_ROLE_008", "%s khong duoc som hon %s"),

    INVALID_PERMISSION_ID("DOMAIN_PERMISSION_001", "%s khong duoc null"),
    INVALID_PERMISSION_CODE("DOMAIN_PERMISSION_002", "%s khong duoc null"),
    INVALID_PERMISSION_DESCRIPTION("DOMAIN_PERMISSION_003", "%s khong duoc de trong"),
    INVALID_PERMISSION_CREATED_AT("DOMAIN_PERMISSION_004", "%s khong duoc nam trong tuong lai"),
    INVALID_PERMISSION_UPDATED_AT("DOMAIN_PERMISSION_005", "%s khong duoc nam trong tuong lai"),
    INVALID_PERMISSION_DELETED_AT("DOMAIN_PERMISSION_006", "%s khong duoc nam trong tuong lai"),
    INVALID_PERMISSION_AUDIT_ORDER("DOMAIN_PERMISSION_007", "%s khong duoc som hon %s"),

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
