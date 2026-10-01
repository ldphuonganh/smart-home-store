package vn.edu.smarthome.authservice.entity;

/**
 * Hệ thống chỉ có 2 role tài khoản (Tài liệu thống nhất - mục 24).
 * PARTNER là actor bên ngoài, dùng API Key, không phải role trong bảng users.
 */
public enum Role {
    CUSTOMER,
    ADMIN
}
